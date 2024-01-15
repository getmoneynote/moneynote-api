package cn.biq.mn.account;

import org.springframework.util.StringUtils;

public class AccountMapper {

    public static AccountDetails toDetails(Account entity) {
        if (entity == null) return null;
        var details = new AccountDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setType( entity.getType() );
        details.setNo( entity.getNo() );
        details.setBalance( entity.getBalance() );
        details.setEnable( entity.getEnable() );
        details.setInclude( entity.getInclude() );
        details.setCanExpense( entity.getCanExpense() );
        details.setCanIncome( entity.getCanIncome() );
        details.setCanTransferFrom( entity.getCanTransferFrom() );
        details.setCanTransferTo( entity.getCanTransferTo() );
        details.setNotes( entity.getNotes() );
        details.setCurrencyCode( entity.getCurrencyCode() );
        details.setCreditLimit( entity.getCreditLimit() );
        details.setBillDay( entity.getBillDay() );
        details.setApr( entity.getApr() );
        return details;
    }

    public static Account toEntity(AccountAddForm form) {
        Account account = new Account();
        account.setInitialBalance( form.getBalance() );
        account.setName( form.getName() );
        account.setType( form.getType() );
        account.setNotes( form.getNotes() );
        account.setNo( form.getNo() );
        account.setBalance( form.getBalance() );
        account.setInclude( form.getInclude() );
        account.setCanExpense( form.getCanExpense() );
        account.setCanIncome( form.getCanIncome() );
        account.setCanTransferFrom( form.getCanTransferFrom() );
        account.setCanTransferTo( form.getCanTransferTo() );
        account.setCurrencyCode( form.getCurrencyCode() );
        account.setCreditLimit( form.getCreditLimit() );
        account.setBillDay( form.getBillDay() );
        account.setApr( form.getApr() );
        account.setEnable(true);
        account.setCurrencyCode(form.getCurrencyCode());
        return account;
    }

    public static void updateEntity(AccountUpdateForm form, Account account) {
        if (StringUtils.hasText(form.getName())) {
            account.setName( form.getName() );
        }
        account.setNotes( form.getNotes() );
        account.setNo( form.getNo() );
        if ( form.getInclude() != null ) {
            account.setInclude( form.getInclude() );
        }
        if ( form.getCanExpense() != null ) {
            account.setCanExpense( form.getCanExpense() );
        }
        if ( form.getCanIncome() != null ) {
            account.setCanIncome( form.getCanIncome() );
        }
        if ( form.getCanTransferFrom() != null ) {
            account.setCanTransferFrom( form.getCanTransferFrom() );
        }
        if ( form.getCanTransferTo() != null ) {
            account.setCanTransferTo( form.getCanTransferTo() );
        }
        account.setCreditLimit( form.getCreditLimit() );
        account.setBillDay( form.getBillDay() );
        account.setApr( form.getApr() );
    }

}
