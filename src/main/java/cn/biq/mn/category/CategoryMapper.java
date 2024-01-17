package cn.biq.mn.category;

import cn.biq.mn.book.tpl.CategoryTemplate;

public class CategoryMapper {

    public static CategoryDetails toDetails(Category entity) {
        if (entity == null) return null;
        var details = new CategoryDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setType( entity.getType() );
        details.setNotes( entity.getNotes() );
        details.setEnable( entity.getEnable() );
        if (entity.getParent() != null) {
            details.setParentId( entity.getParent().getId() );
        }
        details.setLevel(entity.getLevel());
        details.setSort(entity.getSort());
        return details;
    }

    public static Category toEntity(CategoryAddForm form) {
        Category category = new Category();
        category.setName( form.getName() );
        category.setNotes( form.getNotes() );
        category.setType( form.getType() );
        category.setSort(form.getSort());
        category.setEnable(true);
        return category;
    }

    public static void updateEntity(CategoryUpdateForm form, Category category) {
        category.setName( form.getName() );
        category.setNotes( form.getNotes() );
        category.setSort(form.getSort());
    }

    public static Category toEntity(CategoryTemplate template) {
        if (template == null) return null;
        Category category = new Category();
        category.setLevel( template.getLevel() );
        category.setName( template.getName() );
        category.setNotes( template.getNotes() );
        category.setType( CategoryType.fromCode(template.getType()) );
        category.setSort(template.getSort());
        return category;
    }

    public static Category toEntity(CategoryDetails details) {
        if (details == null) return null;
        Category category = new Category();
        category.setLevel( details.getLevel() );
        category.setName( details.getName() );
        category.setNotes( details.getNotes() );
        category.setType( details.getType() );
        category.setSort(details.getSort());
        return category;
    }

}
