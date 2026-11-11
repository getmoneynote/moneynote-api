package com.moneynote.chat.block;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * parts 列 JSON 的编解码器：{@code List<MessageBlock>} ↔ TEXT 的唯一转换点。
 *
 * <p>静态工具类（与 ChatResponseParser 同规约：无状态纯转换 → private 构造器，
 * 不经 Spring 管理）。payload 结构完全由本包类型控制，专用 mapper 即可，
 * 不占用 Spring 管理的 ObjectMapper。
 *
 * <p>data 的多态不走 @JsonTypeInfo 注解，而是在 {@link #fromJson} 里按 type 显式
 * 分发——配对关系集中在 {@link BlockType#dataType()} 一处登记，解析路径直白可查。
 * Jackson 3 的异常均为 unchecked，损坏的 parts JSON 直接向上抛（数据契约破坏，
 * 不静默跳过）。
 */
public final class MessageBlocks {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private MessageBlocks() {
    }

    /** 序列化落库；空列表返回 null（parts 列保持 null 而非 "[]"，与无块消息形态一致）。 */
    public static String toJson(List<MessageBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return null;
        }
        return MAPPER.writeValueAsString(blocks);
    }

    /** 读回；null/空白返回 null（历史消息与无块消息的统一形态）。 */
    public static List<MessageBlock> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        JsonNode array = MAPPER.readTree(json);
        List<MessageBlock> blocks = new ArrayList<>(array.size());
        for (JsonNode node : array) {
            MessageBlock block = new MessageBlock();
            block.setId(node.path("id").asText());
            BlockType type = BlockType.fromWireName(node.path("type").asText());
            block.setType(type);
            JsonNode summaryNode = node.path("summary");
            block.setSummary(summaryNode.isMissingNode() || summaryNode.isNull()
                    ? null : summaryNode.asText());
            JsonNode dataNode = node.path("data");
            block.setData(dataNode.isMissingNode() || dataNode.isNull()
                    ? null
                    : MAPPER.treeToValue(dataNode, type.dataType()));
            blocks.add(block);
        }
        return blocks;
    }

    /**
     * 历史回读用的模型视角扩展（方案 B 口径）：把每个块的 summary 依次拼到正文之后
     * （每块一行，以换行开头）。无块或全部无摘要时返回空串，调用方直接拼接到
     * content 末尾即可，无需判空。
     */
    public static String appendSummaries(String partsJson) {
        List<MessageBlock> blocks = fromJson(partsJson);
        if (blocks == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (MessageBlock block : blocks) {
            if (block.getSummary() != null && !block.getSummary().isBlank()) {
                sb.append('\n').append(block.getSummary());
            }
        }
        return sb.toString();
    }

}
