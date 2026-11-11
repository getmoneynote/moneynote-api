package com.moneynote.chat.event;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

import com.moneynote.chat.agent.AgentEvent;
import com.moneynote.chat.block.MessageBlock;

/**
 * 领域事件 → SSE 协议事件的纯转换器：类型驱动分发（与出现顺序无关，思考/正文
 * 交叉、正文后再次出现思考都原样转发）。
 *
 * <p>无状态、可单例、可脱离 Spring 与数据库单测：<b>不回写内容缓冲、不分配 seq、
 * 不碰持久化</b>。缓冲由 {@code chat.stream.MessageDraft} 以「已发出的 ChatEvent」
 * 为输入回写，seq 由 {@link ChatEventFactory} 统一分配。
 *
 * <p>sealed {@link AgentEvent} + 模式匹配 switch 不写 default：Agent 层新增事件类型时，
 * 编译器强制这里同步补分支，不会静默丢弃。返回值是 0..N 个事件的流，
 * {@code Flux.empty()} 表示该事件只携带元数据、不对外发送（不再用 null 隐式约定）。
 *
 * <p>{@link AgentEvent.FinishReason} 只携带 provider 结束原因，由
 * {@code MessageDraft.observe} 提取、最终经 finish 事件携带，自身不发事件。
 */
@Component
public class ChatEventConverter {

    public Flux<ChatEvent> convert(AgentEvent event, ChatEventFactory factory) {
        return switch (event) {
            case AgentEvent.Reasoning(String delta) -> Flux.just(factory.reasoning(delta));
            case AgentEvent.Content(String delta) -> Flux.just(factory.content(delta));
            case AgentEvent.FinishReason(String reason) -> Flux.empty();
            case AgentEvent.Block(MessageBlock block) -> Flux.just(factory.block(block));
        };
    }

}
