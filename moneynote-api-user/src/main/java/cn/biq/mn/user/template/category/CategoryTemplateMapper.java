package cn.biq.mn.user.template.category;



public class CategoryTemplateMapper {

    public static CategoryTemplateDetails toDetails(CategoryTemplate entity) {
        if (entity == null) return null;
        var details = new CategoryTemplateDetails();
        details.setId(entity.getId());
        if (entity.getParent() != null) {
            details.setParentId(entity.getParent().getId());
        }
        details.setName(entity.getName());
        details.setNotes(entity.getNotes());
        details.setType(entity.getType());
        details.setLevel(entity.getLevel());
        return details;
    }

}
