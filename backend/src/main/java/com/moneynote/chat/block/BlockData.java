package com.moneynote.chat.block;

/**
 * 消息块展示数据的类型根：sealed 限定全集，与 {@link BlockType#dataType()}
 * 互为镜像（两处登记、编译器兜底），新增块类型时不存在「加了 data 类型但
 * 解析点不知道」的静默路径。
 *
 * <p>只承载前端渲染需要的数据；给模型看的摘要放在 {@link MessageBlock#getSummary()}，
 * 不进本接口。JSON 多态不走注解，由 {@link MessageBlocks#fromJson} 按
 * {@link MessageBlock#getType()} 显式分发到本接口的具体实现。
 */
public sealed interface BlockData permits ChartBlockData, FormBlockData {
}
