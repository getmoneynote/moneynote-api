package cn.biq.mn.admin.booktemplate.category;


import cn.biq.mn.admin.entity.admin.Category;

public class CategoryMapper {

    public static CategoryDetails toDetails(Category entity) {
        if (entity == null) return null;
        var details = new CategoryDetails();
        details.setId(entity.getId());
        if (entity.getParent() != null) {
            details.setParentId(entity.getParent().getId());
        }
        details.setName(entity.getName());
        details.setNotes(entity.getNotes());
        details.setType(entity.getType());
        details.setCanExpense(entity.getType() == CategoryType.EXPENSE);
        details.setCanIncome(entity.getType() == CategoryType.INCOME);
        details.setLevel(entity.getLevel());
        return details;
    }

}
