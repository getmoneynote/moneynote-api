package com.moneynote.chat.stream;

import java.util.ArrayList;
import java.util.List;

import com.moneynote.chat.agent.AgentEvent;
import com.moneynote.chat.block.MessageBlock;
import com.moneynote.chat.event.ChatEvent;

/**
 * 一次生成的消息草稿：正文/思考内容/展示块缓冲 + 结束原因，供终态落库使用。
 *
 * <p><b>不变量：缓冲内容 == 已发出的事件内容。</b>因此 {@link #append(ChatEvent)}
 * 挂在「已转换出的 ChatEvent 流」的 doOnNext 上，而不是转换器内部；
 * {@link #observe(AgentEvent)} 只提取不产生事件的元数据（FinishReason），
 * 这样 cancel/error 终态落库不会多出未发出的半截增量。
 * 展示块是会发出 block 事件的，与文本一样走 append 回写，不走 observe。
 *
 * <p>线程安全：chunk 在 Netty event loop 上追加，取消时的落库快照在 boundedElastic
 * 上读取，两者间没有天然 happens-before；内容/思考/块/结束原因的所有读写走同一把锁，
 * snapshot 与 append/observe 互斥，且所有缓冲区在同一把锁下一起拷出。
 *
 * <p>seq 已移入 {@code ChatEventFactory}，终态占位已移入 {@link TerminalFinalizer}，
 * 本类只负责「这条消息最终要存什么」。
 */
final class MessageDraft {

    /** content / reasoning / finishReason / blocks 的统一锁 */
    private final Object lock = new Object();

    private final StringBuilder content = new StringBuilder();
    private final StringBuilder reasoning = new StringBuilder();
    // 所有读写都在 lock 内，普通 ArrayList 即可（不需要 COW 的额外并发保证）
    private final List<MessageBlock> blocks = new ArrayList<>();

    /** 真实结束原因（stop/length/content_filter 等），provider 未上报时按正常结束兜底 */
    private String finishReason = "stop";

    /** 以已发出的协议事件为输入回写缓冲，保证与前端收到的内容一致 */
    void append(ChatEvent event) {
        switch (event.eventType()) {
            case CONTENT -> appendContent((String) event.eventData());
            case REASONING -> appendReasoning((String) event.eventData());
            case BLOCK -> appendBlock((MessageBlock) event.eventData());
            default -> {
            }
        }
    }

    /** 只提取不产生 SSE 事件的元数据；这里是窄过滤而非事件分发，故不用 switch */
    void observe(AgentEvent event) {
        if (event instanceof AgentEvent.FinishReason(String reason)) {
            updateFinishReason(reason);
        }
    }

    private void appendContent(String text) {
        synchronized (lock) {
            content.append(text);
        }
    }

    private void appendReasoning(String text) {
        synchronized (lock) {
            reasoning.append(text);
        }
    }

    private void appendBlock(MessageBlock block) {
        synchronized (lock) {
            blocks.add(block);
        }
    }

    /** 非空才覆盖：流式协议里仅最后一个 chunk 的 finishReason 非 null（中间恒 null） */
    private void updateFinishReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return;
        }
        synchronized (lock) {
            finishReason = reason;
        }
    }

    String finishReason() {
        synchronized (lock) {
            return finishReason;
        }
    }

    /**
     * 终态落库用的一致快照：正文/思考/块在同一把锁下一起拷出。
     * 思考内容为空时返回 null，块为空时返回空列表（由 MessageBlocks.toJson 落为 null）。
     */
    TerminalSnapshot snapshot() {
        synchronized (lock) {
            return new TerminalSnapshot(content.toString(),
                    reasoning.isEmpty() ? null : reasoning.toString(),
                    List.copyOf(blocks));
        }
    }

    /** 终态落库快照：正文 + 思考内容 + 展示块列表 */
    record TerminalSnapshot(String content, String reasoningContent, List<MessageBlock> blocks) {
    }

}
