package cn.biq.mn.tag;


import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemExistsException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.tree.TreeUtils;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.Book;
import cn.biq.mn.tagrelation.TagRelationRepository;
import cn.biq.mn.utils.Limitation;
import cn.biq.mn.utils.SessionUtil;
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
public class TagService {

    private final TagRepository tagRepository;
    private final BaseService baseService;
    private final TagRelationRepository tagRelationRepository;
    private final SessionUtil sessionUtil;

    public boolean add(TagAddForm form) {
        Book book = baseService.findBookById(form.getBookId());
        Tag parent = null;
        if (form.getPId() != null) {
            parent = tagRepository.findOneByBookAndId(book, form.getPId()).orElseThrow(ItemNotFoundException::new);
            if (parent.getLevel().equals(Limitation.category_max_level - 1)) {
                throw new FailureMessageException("category.max.level");
            }
        }
        // 限制每个账本的交易标签数量
        if (tagRepository.countByBook(book) >= Limitation.tag_max_count) {
            throw new FailureMessageException("category.max.count");
        }
        // 检查重复
        if (tagRepository.existsByBookAndParentAndName(book, parent, form.getName())) {
            throw new ItemExistsException();
        }
        Tag entity = TagMapper.toEntity(form);
        entity.setBook(book);
        entity.setParent(parent);
        if (parent == null) entity.setLevel(0);
        else entity.setLevel(parent.getLevel() + 1);
        tagRepository.save(entity);
        return true;
    }

    @Transactional(readOnly = true)
    public List<TagDetails> query(TagQueryForm form, Pageable page) {
        // 确保传入的bookId是自己组里面的。
        if (form.getBookId() != null) {
            baseService.findBookById(form.getBookId());
        } else {
            form.setBookId(sessionUtil.getCurrentBook().getId());
        }
        List<Tag> entityList = tagRepository.findAll(form.buildPredicate(), page.getSort());
        List<TagDetails> detailsList = entityList.stream().map(TagMapper::toDetails).toList();
        return TreeUtils.buildTree(detailsList);
    }

    @Transactional(readOnly = true)
    public List<TagDetails> queryAll(TagQueryForm form) {
        // TODO 重构
        if (form.getBookId() == null) {
            return new ArrayList<>();
        }
        // 确保传入的bookId是自己组里面的。
        baseService.findBookById(form.getBookId());
        form.setEnable(true);
        List<Tag> entityList = tagRepository.findAll(form.buildPredicate(), Sort.by(Sort.Direction.ASC, "sort"));
        List<TagDetails> detailsList = entityList.stream().map(TagMapper::toDetails).toList();
        return TreeUtils.buildTree(detailsList);
    }

    public boolean toggle(Integer id) {
        Tag entity = baseService.findTagById(id);
        entity.setEnable(!entity.getEnable());
        tagRepository.save(entity);
        return true;
    }

    public boolean toggleCanExpense(Integer id) {
        Tag entity = baseService.findTagById(id);
        entity.setCanExpense(!entity.getCanExpense());
        tagRepository.save(entity);
        return true;
    }

    // 不加transaction会多一条联合parent的select语句
    public boolean toggleCanIncome(Integer id) {
        Tag entity = baseService.findTagById(id);
        entity.setCanIncome(!entity.getCanIncome());
        tagRepository.save(entity);
        return true;
    }

    public boolean toggleCanTransfer(Integer id) {
        Tag entity = baseService.findTagById(id);
        entity.setCanTransfer(!entity.getCanTransfer());
        tagRepository.save(entity);
        return true;
    }

    public boolean update(Integer id, TagUpdateForm form) {
        Tag entity = baseService.findTagById(id);
        Book book = entity.getBook();
        // 修改父类了
        if(!Objects.equals(Optional.ofNullable(entity.getParent()).map(i->i.getId()).orElse(null), form.getPId())) {
            // 如果form id为空，则parent会被置空
            entity.setParent(baseService.findTagById(form.getPId()));
            // 限制最多层级
            if (entity.getParent() != null && entity.getParent().getLevel().equals(Limitation.category_max_level - 1)) {
                throw new FailureMessageException("category.max.level");
            }
        }
        // 不能重名
        if (!Objects.equals(entity.getName(), form.getName())) {
            if (StringUtils.hasText(form.getName())) {
                if (tagRepository.existsByBookAndParentAndName(book, entity.getParent(), form.getName())) {
                    throw new ItemExistsException();
                }
            }
        }
        // 不能父类改成自己
        if (entity.getParent() != null && entity.getId().equals(entity.getParent().getId())) {
            throw new FailureMessageException("error.parent.itself");
        }
        TagMapper.updateEntity(form, entity);
        if (entity.getParent() == null) {
            entity.setLevel(0);
        } else {
            entity.setLevel(entity.getParent().getLevel() + 1);
        }
        tagRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        Tag entity = baseService.findTagById(id);
        // 分类有账单关联
        if (tagRelationRepository.existsByTag(entity)) {
            throw new FailureMessageException("tag.delete.has.flow");
        }
        // 取消关联的子分类
        tagRepository.unChildren(entity);
        tagRepository.delete(entity);
        return true;
    }

}
