package com.moneynote.chat.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.moneynote.chat.ChatTransactionService;
import com.moneynote.chat.event.ChatEvent;
import com.moneynote.chat.event.ChatEventConverter;
import com.moneynote.chat.event.ChatEventFactory;

/**
 * 一次生成的流式生命周期编排：start → Agent 事件流 → 终态落库 → finish，
 * 以及 error / cancel 两条终态路径。
 *
 * <p>{@code MessageService} 只负责「校验 → 初始化 → 装配上下文」，本类持有全部
 * Reactor 细节；协议构造在 {@link ChatEventFactory}、事件映射在
 * {@link ChatEventConverter}、终态写库在 {@link TerminalFinalizer}、内容累积在
 * {@link MessageDraft}，彼此不互相穿透。
 *
 * <p><b>事务边界</b>：本类无事务注解。阶段一 initialize 已在 {@code MessageService}
 * 的请求线程上以独立短事务完成，模型生成期间不持有事务；阶段三三处终态落库各自
 * 独立短事务，经 {@code subscribeOn(boundedElastic)} 搬离 Netty event loop。
 *
 * <p><b>唯一终态原则</b>：一次生成只允许出现 finish 或 error 之一；用户中止无事件
 * （连接已断），落库 STOPPED + 已生成部分。finish 事件在落库完成后才发出，
 * 前端收到即可安全刷新。
 *
 * <p>P1 注意：当前 finish 仍同时承担「消息完成」与「流结束」两个语义，等待用户
 * 交互的正常关闭流会留下 GENERATING。拆 stream.end 时只改本类，协议层零改动。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationPipeline {

    private final ChatTransactionService chatTransactionService;
    private final ChatEventConverter converter;

    public Flux<ChatEvent> run(GenerationInput input) {
        Long msgId = input.messageId();
        ChatEventFactory factory = new ChatEventFactory(msgId);
        MessageDraft draft = new MessageDraft();
        TerminalFinalizer finalizer = new TerminalFinalizer(chatTransactionService, msgId, draft);

        // Agent 领域事件 → 协议事件。observe 先提取不产事件的元数据（FinishReason），
        // concatMap 保序，append 以「已发出的事件」回写缓冲，保证缓冲 == 前端收到的内容。
        Flux<ChatEvent> modelEvents = input.execution().events()
                .doOnNext(draft::observe)
                .concatMap(event -> converter.convert(event, factory))
                .doOnNext(draft::append);

        return Flux.just(factory.start(input.model(), input.agentCode()))
                .concatWith(modelEvents)
                // 落库编排进流尾：落库完成后才发 finish；persistTerminal 是阻塞 JDBC，
                // 搬去 boundedElastic；finish 经 defer 延迟构造，保证 seq 在落库后递增
                .concatWith(Mono.fromRunnable(finalizer::complete)
                        .subscribeOn(Schedulers.boundedElastic())
                        .thenMany(Flux.defer(() -> Flux.just(factory.finish(draft.finishReason())))))
                // 出错路径：转换成 error 事件而非让流报错；此时 finish 不会被订阅
                // （concat 在上游错误时短路），唯一终态由 persisted 标志双重保证
                .onErrorResume(e -> {
                    log.warn("AI 回复流中断，已生成部分以 ERROR 落库: {}", e.getMessage());
                    // hasLength：空串/空白 message 也回退到类名，避免给前端发空错误详情
                    String detail = StringUtils.hasLength(e.getMessage())
                            ? e.getMessage()
                            : e.getClass().getSimpleName();
                    return Mono.fromRunnable(finalizer::fail)
                            .subscribeOn(Schedulers.boundedElastic())
                            .then(Mono.fromSupplier(() -> factory.error("error.llm.stream", detail)));
                })
                // cancel 不向下游传播，subscribeOn/publishOn 管不到 doOnCancel，
                // 在回调内用 schedule 把落库挪去 boundedElastic（fire-and-forget）
                .doOnCancel(() -> {
                    log.warn("客户端中止生成，已生成部分以 STOPPED 落库");
                    Schedulers.boundedElastic().schedule(finalizer::stop);
                });
    }

}
