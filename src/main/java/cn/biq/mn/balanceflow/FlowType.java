package cn.biq.mn.balanceflow;


public enum FlowType {

    EXPENSE(100),
    INCOME(200),
    TRANSFER(300),
    ADJUST(400);

    private int code;

    FlowType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static FlowType fromCode(int code) {
        for (FlowType type : FlowType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

}
