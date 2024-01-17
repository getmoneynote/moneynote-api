package cn.biq.mn.category;

import jakarta.persistence.AttributeConverter;

public class CategoryTypeAttributeConverter implements AttributeConverter<CategoryType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CategoryType type) {
        if (type == null) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public CategoryType convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return CategoryType.fromCode(code);
    }

}
