package com.moneynote.chat.agent;

import java.util.Map;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

/**
 * Spring AI 响应结构解析器：把 ChatResponse 拆成原始字段快照（各字段保持上游原样）。
 * Spring AI 的对象结构知识（getResult().getOutput().getMetadata()）只收敛在这一个类里，
 * 将来切换模型接入方式时，Agent 与上层编排都不需要跟着改。
 * 无状态纯转换，静态工具类，不经 Spring 管理。
 *
 * <p>属于 Agent 层的共享适配器（契约 R2）：解析与归一只有这一份实现，
 * 任何「每个 Agent 自己解析一遍」的写法都是错的。
 *
 * <p>重要背景（易误判，实测于 2026-11-05）：模型线路上游的 reasoning_content 是<b>增量</b>
 * （抓包 DashScope 公共端点与专属 MaaS 实例的原始 SSE 均逐 chunk 只带新增片段），
 * 但 Spring AI 2.0.2 的流式路径会<b>自己跨 chunk 累计</b>——OpenAiChatModel 内部
 * reasoningMap.merge(...) 把每个片段拼进累计值，再将"从头到当前"的完整文本放进
 * 每个 chunk 的 AssistantMessage metadata["reasoningContent"]。设计目的是让
 * MessageAggregator 这类对 metadata 做 putAll（last-wins 覆盖）的聚合器
 * 拿到完整全文。因此本类读到的 reasoningContent 是<b>累计快照</b>而非增量，
 * 增量还原见 {@link ReasoningNormalizer}。升级 Spring AI 2.1 后应改读
 * AssistantMessage.getParts() 的 ReasoningPart（增量语义），届时 Normalizer 可删。
 * 完整证据链与演进清单见 docs/大模型流式接入适配层设计.md。
 */
public final class ChatResponseParser {

    private ChatResponseParser() {
    }

    public static ParsedChatChunk parse(ChatResponse response) {
        Generation result = response.getResult();
        // finishReason 挂在 result.metadata 上、与 output 无关：末 chunk 可能只有
        // metadata 没有正文，必须先于 output 判空提取，否则结束原因会丢
        String finishReason = result == null ? null : result.getMetadata().getFinishReason();
        if (result == null || result.getOutput() == null) {
            return new ParsedChatChunk(null, null, finishReason);
        }
        AssistantMessage output = result.getOutput();
        Map<String, Object> metadata = output.getMetadata();
        // Spring AI 2.x OpenAI 集成把 reasoning_content / reasoning 两种字段归一后
        // 放进 AssistantMessage metadata 的 "reasoningContent" 键
        Object raw = metadata == null ? null : metadata.get("reasoningContent");
        String reasoning = raw instanceof String text && StringUtils.hasLength(text) ? text : null;
        return new ParsedChatChunk(reasoning, output.getText(), finishReason);
    }

    /** parse 的产物：一个上游 chunk 的原始字段快照（reasoning 未做归一，保持上游原样） */
    public record ParsedChatChunk(String reasoning, String content, String finishReason) {
    }

}
