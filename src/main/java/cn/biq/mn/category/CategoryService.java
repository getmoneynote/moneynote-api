package cn.biq.mn.category;

import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemExistsException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.tree.TreeUtils;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.Book;
import cn.biq.mn.book.BookRepository;
import cn.biq.mn.categoryrelation.CategoryRelationRepository;
import cn.biq.mn.utils.Limitation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BaseService baseService;
    private final CategoryRelationRepository categoryRelationRepository;
    private final BookRepository bookRepository;

    public boolean add(CategoryAddForm form) {
        Book book = baseService.findBookById(form.getBookId());
        Category parent = null;
        if (form.getPId() != null) {
            parent = categoryRepository.findOneByBookAndId(book, form.getPId()).orElseThrow(ItemNotFoundException::new);
            if (parent.getLevel().equals(Limitation.category_max_level - 1)) {
                throw new FailureMessageException("category.max.level");
            }
        }
        if (categoryRepository.countByBook(book) >= Limitation.category_max_count) {
            throw new FailureMessageException("category.max.count");
        }
        // 检查重复
        if (categoryRepository.existsByBookAndParentAndTypeAndName(book, parent, form.getType(), form.getName())) {
            throw new ItemExistsException();
        }
        Category entity = CategoryMapper.toEntity(form);
        entity.setBook(book);
        entity.setParent(parent);
        if (parent == null) entity.setLevel(0);
        else entity.setLevel(parent.getLevel() + 1);
        categoryRepository.save(entity);
        return true;
    }

    @Transactional(readOnly = true)
    public List<CategoryDetails> query(CategoryQueryForm form, Pageable page) {
        // 确保传入的bookId是自己组里面的。
        baseService.findBookById(form.getBookId());
        List<Category> entityList = categoryRepository.findAll(form.buildPredicate(), page.getSort());
        List<CategoryDetails> detailsList = entityList.stream().map(CategoryMapper::toDetails).toList();
        return TreeUtils.buildTree(detailsList);
    }

    @Transactional(readOnly = true)
    public List<CategoryDetails> queryAll(CategoryQueryForm form) {
        // TODO 重构
        // 搜索的时候不输入，返回空
        if (form.getBookId() == null) {
            return new ArrayList<>();
        }
        // 确保传入的bookId是自己组里面的。
        baseService.findBookById(form.getBookId());
        form.setEnable(true);
        List<Category> entityList = categoryRepository.findAll(form.buildPredicate(), Sort.by(Sort.Direction.ASC, "sort"));
        List<CategoryDetails> detailsList = entityList.stream().map(CategoryMapper::toDetails).toList();
        return TreeUtils.buildTree(detailsList);
    }

    public boolean toggle(Integer id) {
        Category entity = baseService.findCategoryById(id);
        // 账本默认的分类不能操作
        if (bookRepository.existsByDefaultExpenseCategory(entity)) {
            throw new FailureMessageException("category.action.DefaultExpenseCategory");
        }
        if (bookRepository.existsByDefaultIncomeCategory(entity)) {
            throw new FailureMessageException("category.action.DefaultIncomeCategory");
        }
        entity.setEnable(!entity.getEnable());
        categoryRepository.save(entity);
        return true;
    }

    public boolean update(Integer id, CategoryUpdateForm form) {
        // 在当前组里面找 Category
        Category entity = baseService.findCategoryById(id);
        Book book = entity.getBook();
        // 修改父类了
        if(!Objects.equals(Optional.ofNullable(entity.getParent()).map(i->i.getId()).orElse(null), form.getPId())) {
            // 如果form id为空，则parent会被置空
            entity.setParent(baseService.findCategoryById(form.getPId()));
            // 限制最多层级
            if (entity.getParent() != null && entity.getParent().getLevel().equals(Limitation.category_max_level - 1)) {
                throw new FailureMessageException("category.max.level");
            }
        }
        // 不能重名
        if (!entity.getName().equals(form.getName())) {
            if (StringUtils.hasText(form.getName())) {
                if (categoryRepository.existsByBookAndParentAndTypeAndName(book, entity.getParent(), entity.getType(), form.getName())) {
                    throw new ItemExistsException();
                }
            }
        }
        // 不能父类改成自己
        if (entity.getParent() != null && entity.getId().equals(entity.getParent().getId())) {
            throw new FailureMessageException("error.parent.itself");
        }
        CategoryMapper.updateEntity(form, entity);
        if (entity.getParent() == null) {
            entity.setLevel(0);
        } else {
            entity.setLevel(entity.getParent().getLevel() + 1);
        }
        categoryRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        Category entity = baseService.findCategoryById(id);
        // 分类有账单关联
        if (categoryRelationRepository.existsByCategory(entity)) {
            throw new FailureMessageException("category.delete.has.flow");
        }
        // 账本默认的分类不能删除
        if (bookRepository.existsByDefaultExpenseCategory(entity)) {
            throw new FailureMessageException("category.action.DefaultExpenseCategory");
        }
        if (bookRepository.existsByDefaultIncomeCategory(entity)) {
            throw new FailureMessageException("category.action.DefaultIncomeCategory");
        }
        // 取消关联的子分类
        categoryRepository.unChildren(entity);
        categoryRepository.delete(entity);
        return true;
    }

}
