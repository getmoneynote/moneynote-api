package cn.biq.mn.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SimpleResponse extends BaseResponse {

    private String message;
    private Integer code;
    private int showType;

    public SimpleResponse(boolean success, Integer code, String message) {
        setSuccess(success);
        this.code = code;
        this.message = message;
        this.showType = MessageType.showType(success);
    }

    public SimpleResponse(boolean success, String message) {
        setSuccess(success);
        this.message = message;
        this.showType = MessageType.showType(success);
    }

    public SimpleResponse(boolean success, Integer code, String message, int showType) {
        setSuccess(success);
        this.code = code;
        this.message = message;
        this.showType = showType;
    }

    public SimpleResponse(boolean success, String message, int showType) {
        setSuccess(success);
        this.message = message;
        this.showType = showType;
    }

}
