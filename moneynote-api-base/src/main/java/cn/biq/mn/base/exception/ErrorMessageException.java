package cn.biq.mn.base.exception;

import cn.biq.mn.base.utils.Constant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// 返回503
public class ErrorMessageException extends RuntimeException {

    private int showType;

    public ErrorMessageException() { }

    public ErrorMessageException(String message) {
        super(message);
        showType = Constant.SHOW_TYPE_ERROR_MESSAGE;
    }

    public ErrorMessageException(String message, int showType) {
        super(message);
        this.showType = showType;
    }

}
