package cn.biq.mn.categoryrelation;

import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.account.Account;
import cn.biq.mn.balanceflow.BalanceFlow;
import cn.biq.mn.book.Book;
import cn.biq.mn.category.Category;
import cn.biq.mn.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryRelationService {

    private final CategoryRepository categoryRepository;

    public void addRelation(Iterable<CategoryRelationForm> categories, BalanceFlow balanceFlow, Book book, Account account) {
        categories.forEach(i -> {
            Category category = categoryRepository.findOneByBookAndId(book, i.getCategory()).orElseThrow(ItemNotFoundException::new);
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
