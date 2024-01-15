package cn.biq.mn.exception;

import cn.biq.mn.response.MessageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 返回200
public class FailureMessageException extends RuntimeException {

    private int showType;

    public FailureMessageException() { }

    public FailureMessageException(String message) {
        super(message);
        showType = MessageType.SHOW_TYPE_ERROR_MESSAGE;
    }

    public FailureMessageException(String message, int showType) {
        super(message);
        this.showType = showType;
    }

}
