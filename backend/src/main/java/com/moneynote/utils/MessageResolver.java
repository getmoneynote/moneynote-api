package com.moneynote.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 统一的国际化消息解析器,供 Controller、Service 等各层复用。
 * 找不到 key 时不做静默兜底,直接抛出 NoSuchMessageException,
 * 交由 GlobalExceptionHandler 的兜底处理器捕获并记录日志。
 */
@Component
@RequiredArgsConstructor
public class MessageResolver {

    private final MessageSource messageSource;

    public String resolve(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}