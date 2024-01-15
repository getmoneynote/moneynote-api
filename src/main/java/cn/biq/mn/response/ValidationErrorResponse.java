package cn.biq.mn.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


// https://reflectoring.io/bean-validation-with-spring-boot/
@Getter
@Setter
@NoArgsConstructor
public class ValidationErrorResponse extends SimpleResponse {

    private List<Violation> violations = new ArrayList<>();

    public ValidationErrorResponse(Integer errorCode, String errorMsg) {
        setSuccess(false);
        setCode(errorCode);
        setMessage(errorMsg);
        setShowType(MessageType.SHOW_TYPE_ERROR_MESSAGE);
    }

}
