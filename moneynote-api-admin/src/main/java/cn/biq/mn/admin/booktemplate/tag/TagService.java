package cn.biq.mn.admin.booktemplate.tag;

import cn.biq.mn.admin.entity.admin.Tag;
import cn.biq.mn.admin.repository.admin.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.biq.mn.admin.entity.admin.Book;
import cn.biq.mn.admin.repository.admin.BookRepository;
import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemExistsException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.base.tree.TreeUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final BookRepository bookRepository;
    private final TagRepository tagRepository;

    public boolean add(TagAddForm form) {
        Book book = bookRepository.findById(form.getBookId()).orElseThrow(ItemNotFoundException::new);
        Tag parent = null;
        if (form.getPId() != null) {
            parent = tagRepository.findOneByBookAndId(book, form.getPId()).orElseThrow(ItemNotFoundException::new);
        }
        // 检查重复
        if (tagRepository.existsByBookAndParentAndName(book, parent, form.getName())) {
            throw new ItemExistsException();
        }
        Tag entity = new Tag();
        entity.setBook(book);
        entity.setParent(parent);
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setCanExpense(form.getCanExpense());
        entity.setCanIncome(form.getCanIncome());
        entity.setCanTransfer(form.getCanTransfer());
        if (parent == null) entity.setLevel(0);
        else entity.setLevel(parent.getLevel() + 1);
        tagRepository.save(entity);
        return true;
    }

    @Transactional(readOnly = true)
    public List<TagDetails> query(TagQueryForm form) {
        List<Tag> entityList = tagRepository.findAll(form.buildPredicate());
        List<TagDetails> detailsList = entityList.stream().map(TagMapper::toDetails).toList();
        return TreeUtils.buildTree(detailsList);
    }

    public boolean update(Integer id, TagUpdateForm form) {
        Tag entity = tagRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        Tag parent = null;
        if ((entity.getParent() == null && form.getPId() == null) ||
                (entity.getParent() != null && entity.getParent().getId().equals(form.getPId()))) {
            parent = entity.getParent();
        } else {
            if (form.getPId() != null) {
                parent = tagRepository.findById(form.getPId()).orElseThrow(ItemNotFoundException::new);
                if (!parent.getBook().getId().equals(entity.getBook().getId())) {
                    throw new FailureMessageException("valid.fail");
                }
            }
        }
        if (!entity.getName().equals(form.getName())) {
            if (tagRepository.existsByBookAndParentAndName(entity.getBook(), entity.getParent(), form.getName())) {
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
        entity.setCanExpense(form.getCanExpense());
        entity.setCanIncome(form.getCanIncome());
        entity.setCanTransfer(form.getCanTransfer());
        if (parent == null) entity.setLevel(0);
        else entity.setLevel(parent.getLevel() + 1);
        tagRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        // 后代会自动删除
        tagRepository.deleteById(id);
        return true;
    }

}
