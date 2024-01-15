package cn.biq.mn.response;

public final class MessageType {

    public final static int SHOW_TYPE_SILENT = 0;
    public final static int SHOW_TYPE_WARN_MESSAGE = 1;
    public final static int SHOW_TYPE_ERROR_MESSAGE = 2;
    public final static int SHOW_TYPE_NOTIFICATION = 3;
    public final static int SHOW_TYPE_SUCCESS = 4;
    public final static int SHOW_TYPE_REDIRECT = 9;

    public static int showType(boolean success) {
        if (success) {
            return MessageType.SHOW_TYPE_SUCCESS;
        } else {
            return MessageType.SHOW_TYPE_ERROR_MESSAGE;
        }
    }


}

