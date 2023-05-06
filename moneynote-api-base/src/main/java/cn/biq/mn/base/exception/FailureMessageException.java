package cn.biq.mn.base.exception;

import cn.biq.mn.base.utils.Constant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureMessageException extends RuntimeException {

    private int showType;

    public FailureMessageException() { }

    public FailureMessageException(String message) {
        super(message);
        showType = Constant.SHOW_TYPE_ERROR_MESSAGE;
    }

    public FailureMessageException(String message, int showType) {
        super(message);
        this.showType = showType;
    }

}
