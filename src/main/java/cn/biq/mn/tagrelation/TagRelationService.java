package cn.biq.mn.tagrelation;

import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.account.Account;
import cn.biq.mn.balanceflow.BalanceFlow;
import cn.biq.mn.book.Book;
import cn.biq.mn.group.Group;
import cn.biq.mn.tag.Tag;
import cn.biq.mn.tag.TagRepository;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class TagRelationService {

    private final SessionUtil sessionUtil;
    private final TagRepository tagRepository;
    private final TagRelationRepository tagRelationRepository;

    public void addRelation(Set<Integer> tags, BalanceFlow balanceFlow, Book book, Account account) {
        if (CollectionUtils.isEmpty(tags)) return;
        tags.forEach(i -> {
            Tag tag = tagRepository.findOneByBookAndId(book, i).orElseThrow(ItemNotFoundException::new);
            TagRelation relation = new TagRelation();
            relation.setBalanceFlow(balanceFlow);
            relation.setTag(tag);
            relation.setAmount(balanceFlow.getAmount());
            if (account == null || account.getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                relation.setConvertedAmount(balanceFlow.getAmount());
            } else {
                relation.setConvertedAmount(balanceFlow.getConvertedAmount());
            }
            balanceFlow.getTags().add(relation);
            // 保存expense后，relation自动保存
        });
    }

    public boolean update(Integer id, TagRelationUpdateForm form) {
        Group group = sessionUtil.getCurrentGroup();
        TagRelation entity = tagRelationRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        BalanceFlow flow = entity.getBalanceFlow();
        // 只能修改当前账本下面的
        if (!Objects.equals(flow.getGroup().getId(), group.getId())) {
            throw new ItemNotFoundException();
        }
        entity.setAmount(form.getAmount());
        if (flow.getBook().getDefaultCurrencyCode().equals(flow.getAccount().getCurrencyCode())) {
            entity.setConvertedAmount(form.getAmount());
        } else {
            entity.setConvertedAmount(form.getConvertedAmount());
        }
        return true;
    }

}
