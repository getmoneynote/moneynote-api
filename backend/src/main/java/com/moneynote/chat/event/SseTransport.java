package com.moneynote.chat.event;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;

/**
 * 协议事件 → SSE 帧的唯一转换点，供所有流式端点复用（当前只有发送消息，
 * P2 的交互提交端点会复用同一实现）。
 *
 * <p>SSE 的 {@code event:} 字段与 JSON 内 eventType 一致（第一版约定，便于调试）。
 * P0 不写 {@code id:}——eventId 与断线恢复留到 P1，避免改变线上帧。
 */
@Component
public class SseTransport {

    public ServerSentEvent<ChatEvent> toSse(ChatEvent event) {
        return ServerSentEvent.<ChatEvent>builder()
                .event(event.eventType().wireName())
                .data(event)
                .build();
    }

}
