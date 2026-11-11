package com.moneynote.chat.agent;

/**
 * 思考内容的累计→增量归一器（随 {@link ChatResponseProcessor} 每次执行创建，
 * 对编排层不可见；只被流式管线线程串行调用，非线程安全）。
 *
 * <p><b>有状态</b>：绝不挂到单例 Agent 字段上——那会造成跨请求思考内容串线。
 *
 * <p>为什么需要：线路上游的 reasoning_content 本来是增量，但 Spring AI 2.0.2 的 OpenAI
 * 流式路径会自己跨 chunk 累计（{@code OpenAiChatModel} 里 {@code reasoningMap.merge(...)}
 * 把片段拼进累计值），并把【从头到当前的完整文本】放进每个 chunk 的
 * {@code metadata["reasoningContent"]}。按 chunk 消费的我们因此看到累计语义，
 * 必须在此还原为增量，否则前端会把同一段思考反复追加。
 *
 * <p><b>比较基准是「上一帧原文」，不是「累计拼接」</b>——这一点是踩坑后的结论：
 * 带工具调用时一次对话里有<b>多轮模型调用</b>（先决定调工具、再基于工具结果作答），
 * 每轮各自从空串开始累计。若拿拼接后的全量做前缀比较，第二轮的第一帧（如 "We"）
 * 与拼接串毫无前缀关系，会被误判为增量而整块透传，之后每一帧都和前一轮的尾巴对不上，
 * 于是**每帧都发出整段思考**（现象：思考内容重复、且越滚越长）。
 *
 * <p>策略（三种情况互斥）：
 * <ol>
 *   <li>与上一帧完全相同 → 无新增，返回空串；</li>
 *   <li>以上一帧为前缀 → 累计语义，截取新增后缀；</li>
 *   <li>与上一帧无前缀关系 → 新一轮调用的累计、或本来就是增量流，整块即为新增内容。</li>
 * </ol>
 *
 * <p>适用前提：单 choice 流（n=1）。Spring AI 内部按 responseId:choiceIndex 分桶累计，
 * 本类只维护单一序列状态；这与 {@link ChatResponseParser} 只读
 * {@code response.getResult()}（choices[0]）的前提一致——n>1 在 Parser 层即不支持。
 */
public final class ReasoningNormalizer {

    /** 上一帧的原始文本（累计语义下即"到上一帧为止的全文"），null 表示流尚未开始 */
    private String lastRaw;

    /**
     * 把当前 chunk 归一为增量：累计式返回新增后缀，增量式原样返回，无内容返回空串。
     */
    public String normalize(String current) {
        if (current == null || current.isEmpty()) {
            // 空帧不参与比较，也不更新基准：否则下一帧的前缀关系会被空串抹掉
            return "";
        }
        String previous = lastRaw;
        lastRaw = current;
        if (previous == null) {
            return current;
        }
        if (current.equals(previous)) {
            return "";
        }
        if (current.startsWith(previous)) {
            return current.substring(previous.length());
        }
        // 无前缀关系：新一轮模型调用重新开始累计（工具调用后的第二轮），
        // 或上游本来就是增量流——两种情况整块都是新内容
        return current;
    }

}
