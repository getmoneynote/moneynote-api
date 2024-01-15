package cn.biq.mn.balanceflow;

import cn.biq.mn.base.BaseDetails;
import cn.biq.mn.base.IdAndNameDetails;
import cn.biq.mn.account.AccountDetails;
import cn.biq.mn.book.BookDetails;
import cn.biq.mn.categoryrelation.CategoryRelationDetails;
import cn.biq.mn.tagrelation.TagRelationDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter @Setter
public class BalanceFlowDetails extends BaseDetails {

    private BookDetails book;
    private FlowType type;
    private String typeName;
    private String title;
    private String notes;
    private Long createTime;
    private BigDecimal amount;
    private BigDecimal convertedAmount;
    private AccountDetails account;
    private Boolean confirm;
    private Boolean include;
    private List<CategoryRelationDetails> categories;
    private List<TagRelationDetails> tags;
    private String accountName;
    private String categoryName;
    private AccountDetails to;
    private IdAndNameDetails payee;

    public String getListTitle() {
        StringBuilder result = new StringBuilder();
        if (StringUtils.hasText(title)) {
            result.append(title);
        } else {
            if (type == FlowType.EXPENSE || type == FlowType.INCOME) {
                result.append(getCategories().stream().map(CategoryRelationDetails::getCategoryName).collect(Collectors.joining(", ")));
            } else if (type == FlowType.TRANSFER) {
                result.append(accountName);
            } else {
                result.append("调整余额");
            }
        }
        if (payee != null) {
            result.append(" - ").append(payee.getName());
        }
        return result.toString();
    }

    public String getTagsName() {
        return tags.stream().map(TagRelationDetails::getTagName).collect(Collectors.joining(", "));
    }

    public boolean getNeedConvert() {
        if (type == FlowType.EXPENSE || type == FlowType.INCOME) {
            return !Objects.equals(book.getDefaultCurrencyCode(), account != null ? account.getCurrencyCode() : null);
        } else if (type == FlowType.TRANSFER) {
            return !Objects.equals(account.getCurrencyCode(), to.getCurrencyCode());
        }
        return false;
    }

    public String getConvertCode() {
        if (type == FlowType.EXPENSE || type == FlowType.INCOME) {
            return book.getDefaultCurrencyCode();
        } else if (type == FlowType.TRANSFER) {
            return to.getCurrencyCode();
        }
        return null;
    }

    public int getTypeIndex() {
        return type.ordinal();
    }

}
