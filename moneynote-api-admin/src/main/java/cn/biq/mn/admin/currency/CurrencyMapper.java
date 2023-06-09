package cn.biq.mn.admin.currency;


public class CurrencyMapper {

    public static CurrencyDetails toDetails(Currency entity) {
        if (entity == null) return null;
        var details = new CurrencyDetails();
        details.setId( entity.getId() );
        details.setName( entity.getName() );
        details.setDescription( entity.getDescription() );
        details.setRate( entity.getRate() );
        details.setEnable( entity.getEnable() );
        return details;
    }

}
