package cn.biq.mn.base.base;


public class IdAndNameMapper {

    public static IdAndNameDetails toDetails(IdAndNameEntity entity) {
        if (entity == null) return null;
        IdAndNameDetails idAndNameDetails = new IdAndNameDetails();
        idAndNameDetails.setId( entity.getId() );
        idAndNameDetails.setName( entity.getName() );
        return idAndNameDetails;
    }

}
