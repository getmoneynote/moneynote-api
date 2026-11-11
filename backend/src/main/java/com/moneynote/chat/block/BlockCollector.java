package com.moneynote.chat.block;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一次 Agent 执行的块收集器：工具产出展示块的唯一入口。
 *
 * <p>每次执行由 Agent 骨架 new 一个（与 ChatResponseProcessor 同生命周期，绝不做成
 * 字段/单例），随 {@code tools(context, blocks)} 传给工具实例；模型流结束后由骨架
 * 一次性 drain 为 {@code AgentEvent.Block} 事件（见 {@code AbstractLlmAgent.stream}）。
 * 工具在流管线线程上执行、快照在上游 onComplete 之后读取，reactive 链的信号顺序
 * 保证可见性，{@link CopyOnWriteArrayList} 再兜一层。
 *
 * <p>块 id 在所属消息内唯一即可（表单提交按「消息 id + 块 id」定位），用单调序号生成。
 */
public final class BlockCollector {

    private final List<MessageBlock> blocks = new CopyOnWriteArrayList<>();
    private final AtomicInteger seq = new AtomicInteger(0);

    public void add(BlockType type, String summary, BlockData data) {
        MessageBlock block = new MessageBlock();
        block.setId("b" + seq.incrementAndGet());
        block.setType(type);
        block.setSummary(summary);
        block.setData(data);
        blocks.add(block);
    }

    /**
     * 模型流结束后的块快照（不清空：本收集器随本次执行一同废弃）。
     * error / cancel 路径下未取走的块随部分文本一并丢弃——刻意取舍：
     * 块是整体产物，半截生成态的块没有保留价值。
     */
    public List<MessageBlock> snapshot() {
        return List.copyOf(blocks);
    }

}
