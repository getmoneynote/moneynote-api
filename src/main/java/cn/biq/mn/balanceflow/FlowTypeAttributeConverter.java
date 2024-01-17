package cn.biq.mn.balanceflow;

import jakarta.persistence.AttributeConverter;

public class FlowTypeAttributeConverter implements AttributeConverter<FlowType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(FlowType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public FlowType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return FlowType.fromCode(code);
    }

}
