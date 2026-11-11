package com.moneynote.chat;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageListItem {

    private Long id;
    /** 1=用户，2=助手，见 {@link Message#ROLE_USER} */
    private Integer role;
    private String content;
    /**
     * 展示块列表（图表/表单等自定义内容）：前端视角的渲染数据，不包含模型摘要。
     * null 表示无块；历史消息共用同一渲染路径，前端无需区分。
     */
    private List<com.moneynote.chat.block.MessageBlock> parts;
    /** 思考内容（推理模型的 reasoning_content），普通模型/历史数据为 null */
    private String reasoningContent;
    /** 落库时的模型快照，用户消息为 null */
    private String model;
    /**
     * 消息所属 Agent（引用 chat.agent 包注册的 code，如 BOOKKEEPING）。
     * 菜单选定后即确定，USER 与 ASSISTANT 消息同值；也是消息分区键，恒非空。
     */
    private String agentCode;
    /** 生成状态（1=生成中 2=已完成 3=错误 4=已中止，见 {@link com.moneynote.chat.Message} 状态常量注释），历史数据为 null（视为已完成） */
    private Integer status;
    private Instant createdAt;

}
