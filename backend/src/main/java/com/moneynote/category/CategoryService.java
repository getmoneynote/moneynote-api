package com.moneynote.category;

import com.moneynote.base.IdAndNameDetails;
import com.moneynote.book.BookRepository;
import com.moneynote.book.BookService;
import com.moneynote.exception.ResourceDuplicateException;
import com.moneynote.exception.ResourceInvalidException;
import com.moneynote.exception.ResourceNotFoundException;
import com.moneynote.exception.ResourceProtectedException;
import com.moneynote.tree.TreeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final int MAX_LEVEL = 4;

    private final CategoryRepository categoryRepository;
    private final BookService bookService;
    private final BookRepository bookRepository;

    private CategoryTreeNode toTreeNode(Category entity) {
        CategoryTreeNode node = new CategoryTreeNode();
        node.setId(entity.getId());
        node.setName(entity.getName());
        node.setNotes(entity.getNotes());
        node.setType(entity.getType());
        node.setBookId(entity.getBook().getId());
        if (entity.getParent() != null) {
            node.setParentId(entity.getParent().getId());
            node.setParent(new IdAndNameDetails(entity.getParent().getId(), entity.getParent().getName()));
        }
        return node;
    }

    private int depth(Category category) {
        int depth = 0;
        for (Category cur = category; cur != null; cur = cur.getParent()) {
            depth++;
        }
        return depth;
    }

    /** 以 root 为根的子树的「高度」：root 到最深后代的节点数（叶子为 1） */
    private int subtreeHeight(Category root) {
        int height = 1;
        for (Category child : categoryRepository.findByParentId(root.getId())) {
            height = Math.max(height, 1 + subtreeHeight(child));
        }
        return height;
    }

    private void assignLevel(List<CategoryTreeNode> nodes, int level) {
        for (CategoryTreeNode node : nodes) {
            node.setLevel(level);
            if (node.getChildren() != null) {
                assignLevel(node.getChildren(), level + 1);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeNode> tree(Integer type, Long bookId) {
        Long resolvedBookId = bookService.resolveBookId(bookId);
        List<Category> list = categoryRepository.findByBookIdAndType(resolvedBookId, type);
        List<CategoryTreeNode> flat = list.stream()
                .map(this::toTreeNode)
                .collect(Collectors.toList());
        List<CategoryTreeNode> roots = TreeUtils.buildTree(flat);
        assignLevel(roots, 1);
        return roots;
    }

    @Transactional
    public void create(CategoryCreateForm form) {
        Long bookId = bookService.getCurrentBookId();
        Integer type = form.getType();

        Category parent = null;
        int level = 1;
        if (form.getParentId() != null) {
            parent = categoryRepository.findByIdAndBookId(form.getParentId(), bookId)
                    .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
            if (!type.equals(parent.getType())) {
                throw new ResourceInvalidException("error.category.type.mismatch");
            }
            level = depth(parent) + 1;
            if (level > MAX_LEVEL) {
                throw new ResourceInvalidException("error.category.max.level");
            }
        }

        if (categoryRepository.existsByBookIdAndTypeAndNameAndParentId(bookId, type, form.getName(), form.getParentId())) {
            throw new ResourceDuplicateException("resource.category", form.getName());
        }

        Category entity = new Category();
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setType(type);
        entity.setParent(parent);
        entity.setBook(bookRepository.getReferenceById(bookId));
        categoryRepository.save(entity);
    }

    @Transactional
    public void update(Long id, CategoryUpdateForm form) {
        Long bookId = bookService.getCurrentBookId();
        Category entity = categoryRepository.findByIdAndBookId(id, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.category"));

        changeParent(entity, bookId, form.getParentId());

        // 同级重名校验：基于变更后的父级与最终名称（排除自身，名称/父级未变也不会误报）
        Long parentId = entity.getParent() != null ? entity.getParent().getId() : null;
        if (categoryRepository.existsByBookIdAndTypeAndNameAndParentIdAndIdNot(
                bookId, entity.getType(), form.getName(), parentId, id)) {
            throw new ResourceDuplicateException("resource.category", form.getName());
        }

        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
    }

    private void changeParent(Category entity, Long bookId, Long newParentId) {
        Long currentParentId = entity.getParent() != null ? entity.getParent().getId() : null;
        if (java.util.Objects.equals(newParentId, currentParentId)) {
            return;
        }

        Category newParent = null;
        int newDepth = 1;
        if (newParentId != null) {
            newParent = categoryRepository.findByIdAndBookId(newParentId, bookId)
                    .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
            if (!entity.getType().equals(newParent.getType())) {
                throw new ResourceInvalidException("error.category.type.mismatch");
            }
            // 不能移动到自身的后代节点下：等价于「id 是 newParent 的祖先」
            for (Category cur = newParent; cur != null; cur = cur.getParent()) {
                if (cur.getId().equals(entity.getId())) {
                    throw new ResourceInvalidException(cur.getId().equals(newParentId)
                            ? "error.category.parent.self"
                            : "error.category.parent.descendant");
                }
            }
            newDepth = depth(newParent) + 1;
        }
        // 节点自身不超层还不够，要保证整个子树移动后都不超层（否则深后代会被顶出 MAX_LEVEL）
        int maxLevel = newDepth + subtreeHeight(entity) - 1;
        if (maxLevel > MAX_LEVEL) {
            throw new ResourceInvalidException("error.category.max.level");
        }

        entity.setParent(newParent);
    }

    @Transactional
    public void delete(Long id) {
        Long bookId = bookService.getCurrentBookId();
        Category entity = categoryRepository.findByIdAndBookId(id, bookId).orElse(null);
        if (entity == null) {
            return;
        }
        if (categoryRepository.existsByParentId(id)) {
            throw new ResourceProtectedException("error.category.remove.hasChildren");
        }
        categoryRepository.delete(entity);
    }
}
