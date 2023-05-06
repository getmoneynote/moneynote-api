package cn.biq.mn.user.tag;

import org.springframework.util.StringUtils;
import cn.biq.mn.user.template.tag.TagTemplateDetails;

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
        return tag;
    }

    public static void updateEntity(TagUpdateForm form, Tag tag) {
        if (StringUtils.hasText(form.getName())) {
            tag.setName( form.getName() );
        }
        tag.setNotes( form.getNotes() );
        if (form.getCanExpense() != null) {
            tag.setCanExpense( form.getCanExpense() );
        }
        if (form.getCanIncome() != null) {
            tag.setCanIncome( form.getCanIncome() );
        }
        if (form.getCanTransfer() != null) {
            tag.setCanTransfer( form.getCanTransfer() );
        }
    }

    public static Tag toEntity(TagTemplateDetails template) {
        if (template == null) return null;
        Tag tag = new Tag();
        tag.setName( template.getName() );
        tag.setLevel( template.getLevel() );
        tag.setNotes( template.getNotes() );
        tag.setCanExpense( template.getCanExpense() );
        tag.setCanIncome( template.getCanIncome() );
        tag.setCanTransfer( template.getCanTransfer() );
        return tag;
    }

}
