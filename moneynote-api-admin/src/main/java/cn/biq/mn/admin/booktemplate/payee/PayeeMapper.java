package cn.biq.mn.admin.booktemplate.payee;


public class PayeeMapper {

    public static PayeeDetails toDetails(Payee entity) {
        if (entity == null) return null;
        var details = new PayeeDetails();
        details.setId(entity.getId());
        details.setName(entity.getName());
        details.setNotes(entity.getNotes());
        details.setCanExpense(entity.getCanExpense());
        details.setCanIncome(entity.getCanIncome());
        return details;
    }

}
