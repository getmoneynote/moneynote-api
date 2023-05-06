package cn.biq.mn.admin.booktemplate.book;

public class BookMapper {

    public static BookDetails toDetails(Book entity) {
        if (entity == null) return null;
        var details = new BookDetails();
        details.setId(entity.getId());
        details.setName(entity.getName());
        details.setNotes(entity.getNotes());
        details.setPreviewUrl(entity.getPreviewUrl());
        details.setVisible(entity.getVisible());
        return details;
    }

}
