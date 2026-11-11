package com.moneynote.chat.agent;

import java.util.List;

import com.moneynote.chat.ChatHistoryMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * 一次 Agent 执行的输入：会话历史快照（含当前这条用户消息）。
 * 只读 DTO（{@link ChatHistoryMessage}），不含 JPA Entity——
 * 阶段一事务提交后实体已 detached，杜绝懒加载与意外写库。
 */
public record AgentRequest(List<ChatHistoryMessage> history) {

    public AgentRequest {
        history = List.copyOf(history);
    }

    /** 历史快照 → Spring AI 消息序列（USER/ASSISTANT 交替） */
    public static List<Message> toAiMessages(
            List<ChatHistoryMessage> history) {
        return history.stream().map(AgentRequest::toAiMessage).toList();
    }

    private static Message toAiMessage(ChatHistoryMessage message) {
        // 与 Spring AI 的 Message 同名，用全限定名区分
        return message.isUser()
                ? new UserMessage(message.getContent())
                : new AssistantMessage(message.getContent());
    }

}
