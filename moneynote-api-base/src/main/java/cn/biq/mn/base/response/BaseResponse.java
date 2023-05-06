package cn.biq.mn.base.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class BaseResponse {

    private boolean success = false;

    public BaseResponse(boolean success) {
        this.success = success;
    }
}
