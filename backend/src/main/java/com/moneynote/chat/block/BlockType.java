package com.moneynote.chat.block;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 消息块类型全集（协议定义的权威位置）。
 *
 * <p>wireName 即线上格式（SSE 事件、parts 列 JSON、前端分发键共用），显式声明而不从
 * 枚举名推导——避免重命名枚举常量时静默破坏前后端协议与存量 JSON；
 * {@link #dataType()} 声明该类型 data 的反序列化目标，与 {@link BlockData} 的
 * permits 清单互为镜像，新增块类型时两处登记、编译器兜底。
 */
public enum BlockType {

    CHART("chart", ChartBlockData.class),
    FORM("form", FormBlockData.class);

    private final String wireName;
    private final Class<? extends BlockData> dataType;

    BlockType(String wireName, Class<? extends BlockData> dataType) {
        this.wireName = wireName;
        this.dataType = dataType;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }

    public Class<? extends BlockData> dataType() {
        return dataType;
    }

    /** parts JSON 读回时的类型解析：未知类型属数据契约破坏（开发期无兼容负担），直接抛异常。 */
    public static BlockType fromWireName(String wireName) {
        for (BlockType type : values()) {
            if (type.wireName.equals(wireName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的消息块类型: " + wireName);
    }

}
