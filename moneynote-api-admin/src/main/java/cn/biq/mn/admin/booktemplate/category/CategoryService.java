package cn.biq.mn.admin.booktemplate.category;

import cn.biq.mn.admin.entity.admin.Book;
import cn.biq.mn.admin.entity.admin.Category;
import cn.biq.mn.admin.repository.admin.BookRepository;
import cn.biq.mn.admin.repository.admin.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemExistsException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.base.tree.TreeUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public boolean add(CategoryAddForm form) {
        Book book = bookRepository.findById(form.getBookId()).orElseThrow(ItemNotFoundException::new);
        Category parent = null;
        if (form.getPId() != null) {
            parent = categoryRepository.findOneByBookAndId(book, form.getPId()).orElseThrow(ItemNotFoundException::new);
        }
        // 检查重复
        if (categoryRepository.existsByBookAndParentAndTypeAndName(book, parent, form.getType(), form.getName())) {
            throw new ItemExistsException();
        }
        Category entity = new Category();
        entity.setBook(book);
        entity.setType(form.getType());
        entity.setParent(parent);
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        if (parent == null) entity.setLevel(0);
        else entity.setLevel(parent.getLevel() + 1);
        categoryRepository.save(entity);
        return true;
    }

    @Transactional(readOnly = true)
    public List<CategoryDetails> query(CategoryQueryForm form) {
        List<Category> entityList = categoryRepository.findAll(form.buildPredicate());
        List<CategoryDetails> detailsList = entityList.stream().map(CategoryMapper::toDetails).toList();
        return TreeUtils.buildTree(detailsList);
    }

    public boolean update(Integer id, CategoryUpdateForm form) {
        Category entity = categoryRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        Category parent = null;
        if ((entity.getParent() == null && form.getPId() == null) ||
                (entity.getParent() != null && entity.getParent().getId().equals(form.getPId()))) {
            parent = entity.getParent();
        } else {
            if (form.getPId() != null) {
                parent = categoryRepository.findById(form.getPId()).orElseThrow(ItemNotFoundException::new);
                if (!parent.getBook().getId().equals(entity.getBook().getId())) {
                    throw new FailureMessageException("valid.fail");
                }
            }
        }
        if (!entity.getName().equals(form.getName())) {
            if (categoryRepository.existsByBookAndParentAndName(entity.getBook(), entity.getParent(), form.getName())) {
                throw new ItemExistsException();
            }
        }
        // 不能父类改成自己
        if (parent != null && entity.getId().equals(parent.getId())) {
            throw new FailureMessageException("valid.fail");
        }
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setParent(parent);
        if (parent == null) entity.setLevel(0);
        else entity.setLevel(parent.getLevel() + 1);
        categoryRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        // 后代会自动删除
        categoryRepository.deleteById(id);
        return true;
    }

}
