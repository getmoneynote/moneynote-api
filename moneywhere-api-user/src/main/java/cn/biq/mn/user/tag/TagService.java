package cn.biq.mn.user.tag;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemExistsException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.base.tree.TreeUtils;
import cn.biq.mn.base.utils.MyCollectionUtil;
import cn.biq.mn.user.base.BaseService;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.tagrelation.TagRelationRepository;
import cn.biq.mn.user.utils.Limitation;
import cn.biq.mn.user.utils.SessionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final SessionUtil sessionUtil;
    private final TagRepository tagRepository;
    private final BaseService baseService;
    private final TagRelationRepository tagRelationRepository;

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
    public List<TagDetails> query(TagQueryForm form) {
        // 确保传入的bookId是自己组里面的。
        baseService.findBookById(form.getBookId());
        List<Tag> entityList = tagRepository.findAll(form.buildPredicate());
        List<TagDetails> detailsList = entityList.stream().map(TagMapper::toDetails).toList();
        return TreeUtils.buildTree(detailsList);
    }

    @Transactional(readOnly = true)
    public List<TagDetails> queryAll(TagQueryForm form) {
        if (form.getBookId() == null) {
            return new ArrayList<>();
        }
        // 确保传入的bookId是自己组里面的。
        Book book = baseService.findBookById(form.getBookId());
        form.setEnable(true);
        List<Tag> entityList = tagRepository.findAll(form.buildPredicate());
        List<Tag> keeps = baseService.findTagsByBookAndIds(book, form.getKeeps());
        List<Tag> result = MyCollectionUtil.unionWithoutDuplicates(keeps, entityList);
        List<TagDetails> detailsList = result.stream().map(TagMapper::toDetails).toList();
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

        if(!Objects.equals(Optional.ofNullable(entity.getParent()).map(i->i.getId()).orElse(null), form.getPId())) {
            entity.setParent(baseService.findTagById(form.getPId()));
            if (entity.getParent() != null && entity.getParent().getLevel().equals(Limitation.category_max_level - 1)) {
                throw new FailureMessageException("category.max.level");
            }
        }

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
