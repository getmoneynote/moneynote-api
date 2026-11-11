package com.moneynote.chat.event.payload;

/** error 事件载荷：流开始后的失败终态；流开始前的错误仍走 HTTP 状态码 + 全局异常处理器。 */
public record ErrorData(String code, String message) {

}
