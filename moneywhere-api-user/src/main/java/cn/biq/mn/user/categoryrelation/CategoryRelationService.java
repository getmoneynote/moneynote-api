package cn.biq.mn.user.categoryrelation;

import cn.biq.mn.user.account.Account;
import cn.biq.mn.user.balanceflow.BalanceFlow;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.category.Category;
import cn.biq.mn.user.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cn.biq.mn.base.exception.ItemNotFoundException;

@Service
@RequiredArgsConstructor
public class CategoryRelationService {

    private final CategoryRepository categoryRepository;

    public void addRelation(Iterable<CategoryRelationForm> categories, BalanceFlow balanceFlow, Book book, Account account) {
        categories.forEach(i -> {
            Category category = categoryRepository.findOneByBookAndId(book, i.getCategoryId()).orElseThrow(ItemNotFoundException::new);
            CategoryRelation relation = new CategoryRelation();
            relation.setBalanceFlow(balanceFlow);
            relation.setCategory(category);
            relation.setAmount(i.getAmount());
            if (account == null || account.getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                relation.setConvertedAmount(i.getAmount());
            } else {
                relation.setConvertedAmount(i.getConvertedAmount());
            }
            balanceFlow.getCategories().add(relation);
            // 保存expense后，relation自动保存
        });
    }


}
