package cn.biq.mn.user.group;


public class GroupMapper {

    public static GroupDetails toDetails(Group entity) {
        if (entity == null) return null;
        var details = new GroupDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setNotes( entity.getNotes() );
        details.setEnable( entity.getEnable() );
        details.setDefaultCurrencyCode( entity.getDefaultCurrencyCode() );
        return details;
    }

    public static GroupSessionVo toSessionVo(Group group) {
        if (group == null) return null;
        var sessionVo = new GroupSessionVo();
        sessionVo.setId( group.getId() );
        sessionVo.setName( group.getName() );
        sessionVo.setDefaultCurrencyCode( group.getDefaultCurrencyCode() );
        return sessionVo;
    }

}
