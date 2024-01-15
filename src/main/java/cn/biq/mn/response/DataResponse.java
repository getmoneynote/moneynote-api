package cn.biq.mn.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DataResponse<T> extends BaseResponse {

    private T data;

    public DataResponse(T data) {
        setSuccess(true);
        this.data = data;
    }

}
