package com.moneynote.chat.stream;

import java.util.concurrent.atomic.AtomicBoolean;

import com.moneynote.chat.ChatTransactionService;

/**
 * 一次生成的终态落库入口：唯一终态占位 + 三种终态的短事务分发。
 *
 * <p>per-generation 实例（不跨请求共享）。{@code persisted} 保证 COMPLETED / ERROR /
 * STOPPED 只落一次；实际的独立短事务写库委托给 {@link ChatTransactionService}
 * 的 complete/fail/stop——跨 Bean 调用经 Spring 代理，注解事务生效。
 *
 * <p>占位成功 ≠ 落库成功：写库抛异常时归还占位，否则下游兜底路径（如 error 的
 * ERROR 落库）会因 CAS 已被消费而静默跳过，消息永远卡在 GENERATING。
 *
 * <p>用明确方法替代原来 {@code switch (int status)} 上 {@code Message.STATUS_*}
 * 整数常量的分发，终态语义由方法名表达。
 */
final class TerminalFinalizer {

    private final ChatTransactionService chatTransactionService;
    private final Long messageId;
    private final MessageDraft draft;
    private final AtomicBoolean persisted = new AtomicBoolean(false);

    TerminalFinalizer(ChatTransactionService chatTransactionService, Long messageId, MessageDraft draft) {
        this.chatTransactionService = chatTransactionService;
        this.messageId = messageId;
        this.draft = draft;
    }

    void complete() {
        persist(Terminal.COMPLETED);
    }

    void fail() {
        persist(Terminal.ERROR);
    }

    void stop() {
        persist(Terminal.STOPPED);
    }

    private void persist(Terminal terminal) {
        if (!persisted.compareAndSet(false, true)) {
            return;
        }
        MessageDraft.TerminalSnapshot snapshot = draft.snapshot();
        try {
            switch (terminal) {
                case COMPLETED -> chatTransactionService.complete(
                        messageId, snapshot.content(), snapshot.reasoningContent(), snapshot.blocks());
                case ERROR -> chatTransactionService.fail(
                        messageId, snapshot.content(), snapshot.reasoningContent(), snapshot.blocks());
                case STOPPED -> chatTransactionService.stop(
                        messageId, snapshot.content(), snapshot.reasoningContent(), snapshot.blocks());
            }
        } catch (RuntimeException e) {
            persisted.set(false);
            throw e;
        }
    }

    private enum Terminal {
        COMPLETED,
        ERROR,
        STOPPED
    }

}
