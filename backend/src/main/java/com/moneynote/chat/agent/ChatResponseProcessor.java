package com.moneynote.chat.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;

/**
 * 把一个上游 chunk 转换为领域事件（{@link AgentEvent}）：
 * 解析（{@link ChatResponseParser}）→ 思考语义归一（{@link ReasoningNormalizer}）→
 * 类型化 + 非空过滤 → 0~3 个有序事件。
 * 推理模型同一 chunk 可同时含思考与正文增量，chunk 内顺序保留（先思考后正文）；
 * 结束原因（仅末 chunk 非空）追加在该 chunk 事件之后，保证时序与上游一致。
 *
 * <p>思考内容必须归一：线路上游 reasoning_content 本是增量，但 Spring AI 2.0.2 的
 * OpenAI 流式路径会自己跨 chunk 累计（见 {@link ReasoningNormalizer} 与
 * {@link ChatResponseParser} 类注释），每个 chunk 的 metadata["reasoningContent"]
 * 都是"从头到当前"的完整文本——因此只对 reasoning 归一（前缀对齐还原为增量），
 * content 不受影响、直通。
 *
 * <p>归一器有状态，作为本类的私有字段存在：本类<b>每次执行 new 一个</b>
 * （{@code AbstractLlmAgent.stream} 内创建），绝不做成 Spring 单例或 Agent 字段。
 * 本类不上抛任何协议类型，产出全靠 {@link AgentEvent} 表达。
 */
public final class ChatResponseProcessor {

    private final ReasoningNormalizer reasoningNormalizer = new ReasoningNormalizer();

    public List<AgentEvent> process(ChatResponse response) {
        ChatResponseParser.ParsedChatChunk chunk = ChatResponseParser.parse(response);
        List<AgentEvent> events = new ArrayList<>(3);
        // 累计式服务商重复携带全文，归一后为空串则跳过，避免 reasoning 事件反复重放；
        // hasLength 同时防 null（normalize 契约上只返回 "" 或非空串，此处防御其契约变化）。
        // 空白增量（空格/换行）不是 empty，会照常发出
        String reasoningDelta = reasoningNormalizer.normalize(chunk.reasoning());
        if (StringUtils.hasLength(reasoningDelta)) {
            events.add(new AgentEvent.Reasoning(reasoningDelta));
        }
        // hasLength() 可以同时判断 null 和空字符串。
        // content 是流式文本，空格、换行符也可能是有意义的输出，不应该随意过滤。所以不能使用 isBlank
        if (StringUtils.hasLength(chunk.content())) {
            events.add(new AgentEvent.Content(chunk.content()));
        }
        // hasText 与 MessageDraft.updateFinishReason 的判空口径一致（null / 空白都不算结束原因）
        if (StringUtils.hasText(chunk.finishReason())) {
            events.add(new AgentEvent.FinishReason(chunk.finishReason()));
        }
        return List.copyOf(events);
    }

}
