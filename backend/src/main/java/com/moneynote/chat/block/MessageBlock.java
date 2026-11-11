package com.moneynote.chat.block;

import lombok.Getter;
import lombok.Setter;

/**
 * 一条助手消息里的结构化展示块（图表/表单等自定义内容）。
 *
 * <p>一个类型三处复用：parts 列 JSON 的元素、SSE block 事件的载荷、list 接口的返回项。
 * 两个读者的分工：{@code summary} 是<b>模型视角</b>摘要（历史回读时拼进模型输入，见
 * {@code ChatTransactionService.initialize}），{@code data} 是<b>前端视角</b>的渲染数据。
 * {@code id} 在所属消息内唯一（b1、b2…），表单提交按「消息 id + 块 id」定位。
 */
@Getter @Setter
public class MessageBlock {

    private String id;
    private BlockType type;
    private String summary;
    private BlockData data;

}
