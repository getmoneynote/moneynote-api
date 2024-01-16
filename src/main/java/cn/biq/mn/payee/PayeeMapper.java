package cn.biq.mn.payee;

import cn.biq.mn.book.tpl.PayeeTemplate;
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
        details.setSort(entity.getSort());
        return details;
    }

    public static Payee toEntity(PayeeAddForm form) {
        Payee payee = new Payee();
        payee.setName( form.getName() );
        payee.setNotes( form.getNotes() );
        payee.setCanExpense( form.getCanExpense() );
        payee.setCanIncome( form.getCanIncome() );
        payee.setSort(form.getSort());
        return payee;
    }

    public static Payee toEntity(PayeeTemplate template) {
        Payee payee = new Payee();
        payee.setName( template.getName() );
        payee.setNotes( template.getNotes() );
        payee.setCanExpense( template.getCanExpense() );
        payee.setCanIncome( template.getCanIncome() );
        payee.setSort( template.getSort() );
        return payee;
    }

    public static Payee toEntity(Payee entity) {
        Payee payee = new Payee();
        payee.setName( entity.getName() );
        payee.setNotes( entity.getNotes() );
        payee.setCanExpense( entity.getCanExpense() );
        payee.setCanIncome( entity.getCanIncome() );
        payee.setSort( entity.getSort() );
        return payee;
    }

    public static void updateEntity(PayeeUpdateForm form, Payee payee) {
        payee.setName( form.getName() );
        payee.setNotes( form.getNotes() );
        payee.setCanExpense( form.getCanExpense() );
        payee.setCanIncome( form.getCanIncome() );
        payee.setSort(form.getSort());
    }

}
