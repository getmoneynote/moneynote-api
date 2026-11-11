import { Alert, Typography } from 'antd';

import ChartBlock from './ChartBlock';
import FormBlock from './FormBlock';

// 未知块类型降级为原始数据折叠展示：后端新增块类型而前端还没跟上时，
// 至少让内容可见、可复制，而不是白屏或直接吞掉。
const UnknownBlock = ({ block }) => (
  <Alert
    type="warning"
    showIcon
    message={`未支持的展示块：${block?.type ?? 'unknown'}`}
    description={
      <Typography.Paragraph
        copyable
        style={{ marginBottom: 0, maxHeight: 160, overflow: 'auto' }}
      >
        <pre style={{ margin: 0, fontSize: 12 }}>
          {JSON.stringify(block?.data, null, 2)}
        </pre>
      </Typography.Paragraph>
    }
  />
);

const BLOCK_RENDERERS = {
  chart: ChartBlock,
  form: FormBlock,
};

/**
 * 一条 AI 消息里的结构化展示块（图表/表单）渲染入口。
 *
 * 块的类型与数据全部由后端决定（SSE block 事件 / 历史消息 parts）：
 * 这里只按 type 分发，不认识就降级展示原始 JSON。块排在正文之后渲染（后端保证块事件
 * 在文本流之后到达），所以不需要处理图文交错。
 *
 * @param {object} block - MessageBlock：{id, type, summary, data}；summary 是给模型看的，前端忽略
 * @param {string} agentCode - 当前分区，表单提交路径需要
 * @param {number|string} messageId - 块所属消息，表单提交路径需要（块 id 只在消息内唯一）
 * @param {Function} [onSubmitted] - 表单提交成功后的回调（父级用它刷新消息列表）
 */
export default function ChatBlock({
  block,
  agentCode,
  messageId,
  onSubmitted,
}) {
  const Renderer = BLOCK_RENDERERS[block?.type];
  return (
    <div style={{ marginTop: 12 }}>
      {Renderer ? (
        <Renderer
          block={block}
          agentCode={agentCode}
          messageId={messageId}
          onSubmitted={onSubmitted}
        />
      ) : (
        <UnknownBlock block={block} />
      )}
    </div>
  );
}
