package cn.biq.mn.category;

public enum CategoryType {

    EXPENSE(100),
    INCOME(200);

    private int code;

    CategoryType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CategoryType fromCode(int code) {
        for (CategoryType type : CategoryType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

}
