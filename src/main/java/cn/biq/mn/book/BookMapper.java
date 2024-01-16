package cn.biq.mn.book;

import cn.biq.mn.base.IdAndNameMapper;
import cn.biq.mn.account.Account;
import cn.biq.mn.account.AccountMapper;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.category.CategoryMapper;
import cn.biq.mn.group.Group;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final SessionUtil sessionUtil;
    private final BaseService baseService;

    public static BookDetails toDetails(Book entity) {
        if (entity == null) return null;
        var details = new BookDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setGroup(IdAndNameMapper.toDetails(entity.getGroup()));
        details.setNotes( entity.getNotes() );
        details.setEnable( entity.getEnable() );
        details.setDefaultCurrencyCode( entity.getDefaultCurrencyCode() );
        details.setDefaultExpenseAccount( AccountMapper.toDetails( entity.getDefaultExpenseAccount() ) );
        details.setDefaultIncomeAccount( AccountMapper.toDetails( entity.getDefaultIncomeAccount() ) );
        details.setDefaultTransferFromAccount( AccountMapper.toDetails( entity.getDefaultTransferFromAccount() ) );
        details.setDefaultTransferToAccount( AccountMapper.toDetails( entity.getDefaultTransferToAccount() ) );
        details.setDefaultExpenseCategory( CategoryMapper.toDetails( entity.getDefaultExpenseCategory() ) );
        details.setDefaultIncomeCategory( CategoryMapper.toDetails( entity.getDefaultIncomeCategory() ) );
        details.setSort(entity.getSort());
        return details;
    }

    public static BookSessionVo toSessionVo(Book book) {
        if (book == null) return null;
        var sessionVo = new BookSessionVo();
        sessionVo.setId( book.getId() );
        sessionVo.setName( book.getName() );
        sessionVo.setDefaultCurrencyCode( book.getDefaultCurrencyCode() );
        sessionVo.setDefaultExpenseAccount( AccountMapper.toDetails( book.getDefaultExpenseAccount() ) );
        sessionVo.setDefaultIncomeAccount( AccountMapper.toDetails( book.getDefaultIncomeAccount() ) );
        sessionVo.setDefaultTransferFromAccount( AccountMapper.toDetails( book.getDefaultTransferFromAccount() ) );
        sessionVo.setDefaultTransferToAccount( AccountMapper.toDetails( book.getDefaultTransferToAccount() ) );
        sessionVo.setDefaultExpenseCategory( CategoryMapper.toDetails( book.getDefaultExpenseCategory() ) );
        sessionVo.setDefaultIncomeCategory( CategoryMapper.toDetails( book.getDefaultIncomeCategory() ) );
        return sessionVo;
    }

    public Book toEntity(BookAddForm form) {
        Book entity = new Book();
        entity.setName( form.getName() );
        entity.setNotes( form.getNotes() );
        entity.setDefaultCurrencyCode( form.getDefaultCurrencyCode() );
        Group group = sessionUtil.getCurrentGroup();
        if (form.getDefaultExpenseAccountId() != null) {
            Account account = baseService.findAccountById(form.getDefaultExpenseAccountId());
            entity.setDefaultExpenseAccount(account);
        }
        if (form.getDefaultIncomeAccountId() != null) {
            Account account = baseService.findAccountById(form.getDefaultIncomeAccountId());
            entity.setDefaultIncomeAccount(account);
        }
        if (form.getDefaultTransferFromAccountId() != null) {
            Account account = baseService.findAccountById(form.getDefaultTransferFromAccountId());
            entity.setDefaultTransferFromAccount(account);
        }
        if (form.getDefaultTransferToAccountId() != null) {
            Account account = baseService.findAccountById(form.getDefaultTransferToAccountId());
            entity.setDefaultTransferToAccount(account);
        }
        entity.setGroup(group);
        entity.setEnable(true);
        entity.setSort(form.getSort());
        return entity;
    }

    public void updateEntity(BookUpdateForm form, Book entity) {
        if (StringUtils.hasText(form.getName())) {
            entity.setName( form.getName() );
        }
        entity.setNotes( form.getNotes() );

//        if (!((entity.getDefaultExpenseAccount() == null && form.getDefaultExpenseAccountId() == null)||(
//                entity.getDefaultExpenseAccount() != null && entity.getDefaultExpenseAccount().getId().equals(form.getDefaultExpenseAccountId())
//                ))) {
//            if (form.getDefaultExpenseAccountId() != null) {
//                entity.setDefaultExpenseAccount(baseService.findAccountById(form.getDefaultExpenseAccountId()));
//            } else {
//                entity.setDefaultExpenseAccount(null);
//            }
//        }
//
//       if (entity.getDefaultExpenseAccount() == null) { // 账本之前没有默认支出账户
//            if (form.getDefaultExpenseAccountId() != null) { // 如果传入，则进行设置
//                entity.setDefaultExpenseAccount(baseService.findAccountById(form.getDefaultExpenseAccountId()));
//            }
//        } else {
//            // 账本之前有默认账户，没传则置空，如果传入的和之前不同，则更新。
//            if (form.getDefaultExpenseAccountId() == null) {
//                entity.setDefaultExpenseAccount(null);
//            } else {
//                if (!entity.getDefaultExpenseAccount().getId().equals(form.getDefaultExpenseAccountId())) {
//                    entity.setDefaultExpenseAccount(baseService.findAccountById(form.getDefaultExpenseAccountId()));
//                }
//            }
//        }

        if(!Objects.equals(Optional.ofNullable(entity.getDefaultExpenseAccount()).map(i->i.getId()).orElse(null), form.getDefaultExpenseAccountId())) {
            entity.setDefaultExpenseAccount(baseService.findAccountById(form.getDefaultExpenseAccountId()));
        }

        if(!Objects.equals(Optional.ofNullable(entity.getDefaultIncomeAccount()).map(i->i.getId()).orElse(null), form.getDefaultIncomeAccountId())) {
            entity.setDefaultIncomeAccount(baseService.findAccountById(form.getDefaultIncomeAccountId()));
        }

        if(!Objects.equals(Optional.ofNullable(entity.getDefaultTransferFromAccount()).map(i->i.getId()).orElse(null), form.getDefaultTransferFromAccountId())) {
            entity.setDefaultTransferFromAccount(baseService.findAccountById(form.getDefaultTransferFromAccountId()));
        }

        if(!Objects.equals(Optional.ofNullable(entity.getDefaultTransferToAccount()).map(i->i.getId()).orElse(null), form.getDefaultTransferToAccountId())) {
            entity.setDefaultTransferToAccount(baseService.findAccountById(form.getDefaultTransferToAccountId()));
        }

        if(!Objects.equals(Optional.ofNullable(entity.getDefaultExpenseCategory()).map(i->i.getId()).orElse(null), form.getDefaultExpenseCategoryId())) {
            entity.setDefaultExpenseCategory(baseService.findCategoryByBookAndId(entity, form.getDefaultExpenseCategoryId()));
        }

        if(!Objects.equals(Optional.ofNullable(entity.getDefaultIncomeCategory()).map(i->i.getId()).orElse(null), form.getDefaultIncomeCategoryId())) {
            entity.setDefaultIncomeCategory(baseService.findCategoryByBookAndId(entity, form.getDefaultIncomeCategoryId()));
        }
        entity.setSort(form.getSort());

    }

}
