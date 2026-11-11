package com.moneynote.chat;

import lombok.Getter;
import lombok.Setter;

/**
 * 历史消息的只读快照 DTO：只携带构造模型输入所需的 role/content。
 *
 * 为什么不直接传 {@link Message} Entity：阶段一事务提交后实体已 detached，
 * 任何对懒加载关联（如 user）的事务外访问都会抛
 * LazyInitializationException；用 DTO 后该约束由类型系统结构保证，
 * 且模型生成阶段不可能意外修改数据库实体。
 * 转换发生在 {@link ChatTransactionService#initialize} 的事务内，访问字段绝对安全。
 */
@Getter @Setter
public class ChatHistoryMessage {

    /** 1=用户，2=助手，见 {@link Message#ROLE_USER} */
    private int role;
    private String content;

    public boolean isUser() {
        return role == Message.ROLE_USER;
    }
}
