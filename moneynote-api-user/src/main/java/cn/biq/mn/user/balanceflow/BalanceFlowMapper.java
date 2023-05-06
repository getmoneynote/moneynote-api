package cn.biq.mn.user.balanceflow;

import cn.biq.mn.user.account.AccountMapper;
import cn.biq.mn.user.account.AdjustBalanceUpdateForm;
import cn.biq.mn.user.book.BookMapper;
import cn.biq.mn.user.utils.EnumUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import cn.biq.mn.base.base.IdAndNameMapper;
import cn.biq.mn.user.categoryrelation.CategoryRelation;
import cn.biq.mn.user.categoryrelation.CategoryRelationDetails;
import cn.biq.mn.user.categoryrelation.CategoryRelationMapper;
import cn.biq.mn.user.tagrelation.TagRelation;
import cn.biq.mn.user.tagrelation.TagRelationDetails;
import cn.biq.mn.user.tagrelation.TagRelationMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BalanceFlowMapper {

    private final EnumUtils enumUtils;

    public BalanceFlowDetails toDetails(BalanceFlow entity) {
        if (entity == null) return null;
        var details = new BalanceFlowDetails();
        details.setAccountName( BalanceFlowMapper.accountName( entity ) );
        details.setCategoryName( BalanceFlowMapper.categoryName( entity ) );
        details.setId( entity.getId() );
        details.setBook( BookMapper.toDetails( entity.getBook() ) );
        details.setType( entity.getType() );
        details.setTitle( entity.getTitle() );
        details.setNotes( entity.getNotes() );
        details.setCreateTime( entity.getCreateTime() );
        details.setAmount( entity.getAmount() );
        details.setConvertedAmount( entity.getConvertedAmount() );
        details.setAccount( AccountMapper.toDetails( entity.getAccount() ) );
        details.setConfirm( entity.getConfirm() );
        details.setInclude( entity.getInclude() );
        details.setCategories( categoryRelationSetToCategoryRelationDetailsList( entity.getCategories() ) );
        details.setTags( tagRelationSetToTagRelationDetailsList( entity.getTags() ) );
        details.setTo( AccountMapper.toDetails( entity.getTo() ) );
        details.setPayee( IdAndNameMapper.toDetails( entity.getPayee() ) );
        details.setTypeName( enumUtils.translateFlowType(entity.getType()) );
        return details;
    }

    public static BalanceFlow toEntity(BalanceFlowAddForm form) {
        BalanceFlow balanceFlow = new BalanceFlow();
        balanceFlow.setType( form.getType() );
        balanceFlow.setAmount( form.getAmount() );
        balanceFlow.setConvertedAmount( form.getConvertedAmount() );
        balanceFlow.setCreateTime( form.getCreateTime() );
        balanceFlow.setTitle( form.getTitle() );
        balanceFlow.setNotes( form.getNotes() );
        balanceFlow.setConfirm( form.getConfirm() );
        balanceFlow.setInclude( form.getInclude() );
        return balanceFlow;
    }

    public static void updateEntity(BalanceFlowUpdateForm form, BalanceFlow flow) {
        if (form.getCreateTime() != null) {
            flow.setCreateTime( form.getCreateTime() );
        }
        flow.setTitle( form.getTitle() );
        flow.setNotes( form.getNotes() );
        if (form.getInclude() != null) {
            flow.setInclude( form.getInclude() );
        }
    }

    public static void updateEntity(AdjustBalanceUpdateForm form, BalanceFlow flow) {
        flow.setCreateTime( form.getCreateTime() );
        flow.setTitle( form.getTitle() );
        flow.setNotes( form.getNotes() );
    }

    private List<CategoryRelationDetails> categoryRelationSetToCategoryRelationDetailsList(Set<CategoryRelation> set) {
        List<CategoryRelationDetails> list = new ArrayList<>(set.size());
        for ( CategoryRelation categoryRelation : set ) {
            list.add( CategoryRelationMapper.toDetails( categoryRelation ) );
        }
        return list;
    }

    private List<TagRelationDetails> tagRelationSetToTagRelationDetailsList(Set<TagRelation> set) {
        if ( set == null ) {
            return null;
        }
        List<TagRelationDetails> list = new ArrayList<>(set.size());
        for ( TagRelation tagRelation : set ) {
            list.add( TagRelationMapper.toDetails( tagRelation ) );
        }
        return list;
    }

    public static String accountName(BalanceFlow entity) {
        if (entity.getType() == FlowType.TRANSFER) {
            return entity.getAccount().getName() + " -> " + entity.getTo().getName();
        } else {
            return entity.getAccount() != null ? entity.getAccount().getName() : null;
        }
    }

    public static String categoryName(BalanceFlow entity) {
        if (entity.getType() == FlowType.EXPENSE || entity.getType() == FlowType.INCOME) {
            return entity.getCategories().stream().map(i -> i.getCategory().getName() + "(" + i.getAmount().stripTrailingZeros().toPlainString()+")").collect(Collectors.joining(", "));
        }
        return null;
    }

}
