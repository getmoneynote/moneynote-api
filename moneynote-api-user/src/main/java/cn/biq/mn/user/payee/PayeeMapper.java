package cn.biq.mn.user.payee;

import org.springframework.util.StringUtils;

public class PayeeMapper {

    public static PayeeDetails toDetails(Payee entity) {
        if (entity == null) return null;
        var details = new PayeeDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setNotes( entity.getNotes() );
        details.setEnable( entity.getEnable() );
        details.setCanExpense( entity.getCanExpense() );
        details.setCanIncome( entity.getCanIncome() );
        details.setEnable( entity.getEnable() );
        return details;
    }

    public static Payee toEntity(PayeeAddForm payeeAddForm) {
        Payee payee = new Payee();
        payee.setName( payeeAddForm.getName() );
        payee.setNotes( payeeAddForm.getNotes() );
        payee.setCanExpense( payeeAddForm.getCanExpense() );
        payee.setCanIncome( payeeAddForm.getCanIncome() );
        return payee;
    }

    public static void updateEntity(PayeeUpdateForm form, Payee payee) {
        if (StringUtils.hasText(form.getName())) {
            payee.setName( form.getName() );
        }
        payee.setNotes( form.getNotes() );
        if (form.getCanExpense() != null) {
            payee.setCanExpense( form.getCanExpense() );
        }
        if (form.getCanIncome() != null) {
            payee.setCanIncome( form.getCanIncome() );
        }
    }

}
