package cn.biq.mn.user.template.book;


public class BookTemplateMapper {

    public static BookTemplateDetails toDetails(BookTemplate entity) {
        if (entity == null) return null;
        var details = new BookTemplateDetails();
        details.setId(entity.getId());
        details.setName(entity.getName());
        details.setNotes(entity.getNotes());
        details.setPreviewUrl(entity.getPreviewUrl());
        return details;
    }

}
