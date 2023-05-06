package cn.biq.mn.user.template.tag;



public class TagTemplateMapper {

    public static TagTemplateDetails toDetails(TagTemplate entity) {
        if (entity == null) return null;
        var details = new TagTemplateDetails();
        details.setId(entity.getId());
        if (entity.getParent() != null) {
            details.setParentId(entity.getParent().getId());
        }
        details.setName(entity.getName());
        details.setNotes(entity.getNotes());
        details.setCanExpense(entity.getCanExpense());
        details.setCanIncome(entity.getCanIncome());
        details.setCanTransfer(entity.getCanTransfer());
        details.setLevel(entity.getLevel());
        return details;
    }

}
