package cn.biq.mn.base.response;

import cn.biq.mn.base.utils.Constant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataMessageResponse<T> extends DataResponse<T> {

    private String message;
    private int showType;

    public DataMessageResponse(T data, String message) {
        super(data);
        this.message = message;
        setShowType(Constant.SHOW_TYPE_SUCCESS);
    }
}
