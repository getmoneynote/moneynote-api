package cn.biq.mn.account;

import jakarta.persistence.AttributeConverter;

public class AccountTypeAttributeConverter implements AttributeConverter<AccountType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AccountType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public AccountType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return AccountType.fromCode(code);
    }

}
