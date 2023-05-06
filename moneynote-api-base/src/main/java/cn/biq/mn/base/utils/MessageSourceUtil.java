package cn.biq.mn.base.utils;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

// https://blog.csdn.net/nthack5730/article/details/82870368
// https://blog.csdn.net/u012702547/article/details/104667731
@Component
public class MessageSourceUtil {

    @Resource
    private MessageSource messageSource;

    public String getMessage(String code) {
        return getMessage(code, null);
    }

    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, "");
    }

    public String getMessage(String code, Object[] args, String defaultMsg) {
        //这里使用比较方便的方法，不依赖request.
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMsg, locale);
    }

}