package cn.biq.mn.account;

public enum AccountType {

    CHECKING(100),
    CREDIT(200),
    ASSET(300),
    DEBT(400);

    private int code;

    AccountType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AccountType fromCode(int code) {
        for (AccountType type : AccountType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + code);
    }

}
