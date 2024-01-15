package cn.biq.mn.config;

import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.SimpleResponse;
import cn.biq.mn.utils.MessageSourceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@ControllerAdvice
@RequiredArgsConstructor
public class SuccessResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final MessageSourceUtil messageSourceUtil;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body.getClass().equals(BaseResponse.class)) {
            var body1 = (BaseResponse) body;
            if (body1.isSuccess()) {
                return new SimpleResponse(true, messageSourceUtil.getMessage("response.message.success"));
            }
        }
        return body;
    }
}

