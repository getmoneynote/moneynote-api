package com.moneynote.chat;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

import com.moneynote.chat.block.ChatBlockActionService;
import com.moneynote.chat.block.MessageBlockSubmitForm;
import com.moneynote.chat.event.ChatEvent;
import com.moneynote.chat.event.SseTransport;
import com.moneynote.response.DataResponse;
import com.moneynote.response.SimpleResponse;

/**
 * 一条 Agent 固定时间线的消息接口。
 * 没有会话 id：{@code (当前用户, agentCode)} 唯一确定一条时间线；agentCode 由菜单选定，
 * 走路径参数，服务端在流开始前用注册表白名单校验（未知 code 返回标准错误体）。
 */
@RestController
@RequestMapping("/chat/agents/{agentCode}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ChatBlockActionService chatBlockActionService;
    private final SseTransport sseTransport;

    /** 读取该 Agent 时间线的全部历史（无窗口，见 MessageRepository 的扩展点注释）。 */
    @GetMapping
    public SimpleResponse list(@PathVariable("agentCode") String agentCode) {
        return new DataResponse<>(messageService.list(agentCode));
    }

    /**
     * 发送消息，SSE 流式返回 AI 回复，载荷为统一事件协议 {@link ChatEvent}。
     * SSE 的 {@code event:} 字段与 JSON 内 eventType 保持一致（第一版约定，便于调试）；
     * 帧构建统一委托 {@link SseTransport}，本类不感知 SSE 细节。
     * 流式响应逐块写出，不走 SimpleResponse 包装；参数/业务错误在流开始前抛出，
     * 仍由全局异常处理器返回标准错误体；流开始后错误走 error 事件。
     */
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatEvent>> send(
        @PathVariable("agentCode") String agentCode,
        @Valid @RequestBody MessageSendForm form
    ) {
        return messageService.send(agentCode, form).map(sseTransport::toSse);
    }

    /**
     * 清空该 Agent 的时间线（「清空记录」）。前端需先中止进行中的流并等终态落库完成
     * 再调用，服务端终态落库对已删除的消息行做了静默跳过兜底。
     */
    @DeleteMapping
    public SimpleResponse clear(@PathVariable("agentCode") String agentCode) {
        messageService.clear(agentCode);
        return new SimpleResponse();
    }

    /**
     * 提交消息里的表单块（如「记账确认」）：用户在表单里核对/补全后点击提交。
     *
     * <p>这是一次<b>普通 JSON 请求</b>而非 SSE：动作是同步写库，结果直接返回更新后的块
     * （status=done + resultText）。写库入口必须由用户点击触发，不进 LLM 工具集（R6）。
     * 已提交过的块再次提交返回 400（error.chat.block.not.pending），并发提交由悲观锁拦下。
     *
     * <p>路径里的 messageId / blockId 同时参与归属校验：块 id 只在所属消息内唯一。
     */
    @PostMapping("/{messageId}/blocks/{blockId}/submit")
    public SimpleResponse submitBlock(
        @PathVariable("agentCode") String agentCode,
        @PathVariable("messageId") Long messageId,
        @PathVariable("blockId") String blockId,
        @RequestBody(required = false) MessageBlockSubmitForm form
    ) {
        return new DataResponse<>(chatBlockActionService.submit(
                agentCode, messageId, blockId, form == null ? null : form.getValues()));
    }

}
