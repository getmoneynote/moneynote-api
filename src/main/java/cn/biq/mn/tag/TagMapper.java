package cn.biq.mn.tag;

import cn.biq.mn.book.tpl.TagTemplate;

public class TagMapper {

    public static TagDetails toDetails(Tag entity) {
        if (entity == null) return null;
        var details = new TagDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setNotes( entity.getNotes() );
        details.setEnable( entity.getEnable() );
        details.setCanExpense( entity.getCanExpense() );
        details.setCanIncome( entity.getCanIncome() );
        details.setCanTransfer( entity.getCanTransfer() );
        if (entity.getParent() != null) {
            details.setParentId( entity.getParent().getId() );
        }
        details.setLevel(entity.getLevel());
        details.setSort(entity.getSort());
        return details;
    }

    public static Tag toEntity(TagAddForm form) {
        Tag tag = new Tag();
        tag.setName( form.getName() );
        tag.setNotes( form.getNotes() );
        tag.setCanExpense( form.getCanExpense() );
        tag.setCanIncome( form.getCanIncome() );
        tag.setCanTransfer( form.getCanTransfer() );
        tag.setEnable(true);
        tag.setSort(form.getSort());
        return tag;
    }

    public static void updateEntity(TagUpdateForm form, Tag tag) {
        tag.setName( form.getName() );
        tag.setNotes( form.getNotes() );
        tag.setCanExpense( form.getCanExpense() );
        tag.setCanIncome( form.getCanIncome() );
        tag.setCanTransfer( form.getCanTransfer() );
        tag.setSort(form.getSort());
    }

    public static Tag toEntity(TagTemplate template) {
        if (template == null) return null;
        Tag tag = new Tag();
        tag.setName( template.getName() );
        tag.setLevel( template.getLevel() );
        tag.setNotes( template.getNotes() );
        tag.setCanExpense( template.getCanExpense() );
        tag.setCanIncome( template.getCanIncome() );
        tag.setCanTransfer( template.getCanTransfer() );
        tag.setSort(template.getSort());
        return tag;
    }

    public static Tag toEntity(TagDetails details) {
        if (details == null) return null;
        Tag tag = new Tag();
        tag.setName( details.getName() );
        tag.setLevel( details.getLevel() );
        tag.setNotes( details.getNotes() );
        tag.setCanExpense( details.getCanExpense() );
        tag.setCanIncome( details.getCanIncome() );
        tag.setCanTransfer( details.getCanTransfer() );
        tag.setSort(details.getSort() );
        return tag;
    }

}
