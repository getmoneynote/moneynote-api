package cn.biq.mn.user.category;

import cn.biq.mn.user.book.tpl.CategoryTemplate;
import org.springframework.util.StringUtils;
import cn.biq.mn.user.template.category.CategoryTemplateDetails;

public class CategoryMapper {

    public static CategoryDetails toDetails(Category entity) {
        if (entity == null) return null;
        var details = new CategoryDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setType( entity.getType() );
        details.setNotes( entity.getNotes() );
        details.setEnable( entity.getEnable() );
        details.setCanExpense( entity.getType() == CategoryType.EXPENSE );
        details.setCanIncome( entity.getType() == CategoryType.INCOME );
        if (entity.getParent() != null) {
            details.setParentId( entity.getParent().getId() );
        }
        return details;
    }

    public static Category toEntity(CategoryAddForm form) {
        Category category = new Category();
        category.setName( form.getName() );
        category.setNotes( form.getNotes() );
        category.setType( form.getType() );
        category.setEnable(true);
        return category;
    }

    public static void updateEntity(CategoryUpdateForm form, Category category) {
        if (StringUtils.hasText(form.getName())) {
            category.setName( form.getName() );
        }
        category.setNotes( form.getNotes() );
    }

    public static Category toEntity(CategoryTemplate template) {
        if (template == null) return null;
        Category category = new Category();
        category.setLevel( template.getLevel() );
        category.setName( template.getName() );
        category.setNotes( template.getNotes() );
        category.setType( template.getType() );
        return category;
    }

}
