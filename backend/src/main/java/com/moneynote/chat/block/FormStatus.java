package com.moneynote.chat.block;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 表单块状态：PENDING = 待用户提交；DONE = 已完成（动作生效，只读展示）。
 * wireName 即线上格式（parts JSON 与前端共用），显式声明不从枚举名推导。
 */
public enum FormStatus {

    PENDING("pending"),
    DONE("done");

    private final String wireName;

    FormStatus(String wireName) {
        this.wireName = wireName;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }

    /**
     * parts JSON 读回入口：显式按 wireName 解析，不依赖 Jackson 对 @JsonValue 的隐式复用
     * （枚举读不回来会直接让整条历史消息反序列化失败）。
     */
    @JsonCreator
    public static FormStatus fromWireName(String wireName) {
        for (FormStatus status : values()) {
            if (status.wireName.equals(wireName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的表单块状态: " + wireName);
    }

}
