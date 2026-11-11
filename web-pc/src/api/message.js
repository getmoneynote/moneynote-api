import { getAccessToken } from '@/utils/auth';
import { request } from './request';

// 一条固定时间线由 (当前用户, agentCode) 唯一确定，没有会话 id。
// 路径前缀 /chat/agents/{agentCode}/messages；code 由菜单选定，服务端做白名单校验。
const API_PREFIX = '/api/v1';

// 事件类型常量，镜像后端 ChatEventType 的 wireName（后端是协议事实来源，变更以后端为准）
export const ChatEventType = {
  START: 'start',
  REASONING: 'reasoning',
  CONTENT: 'content',
  // 展示块（图表/表单）事件：eventData 为一个 MessageBlock {id, type, summary, data}，
  // 与 content 分开累积——它不进 Markdown 正文，由块组件渲染
  BLOCK: 'block',
  FINISH: 'finish',
  ERROR: 'error',
};

const messagesPath = (agentCode) => `/chat/agents/${agentCode}/messages`;

/**
 * 流式发送消息（SSE，antd-x v2 已无 XStream，手写解析）。
 * 后端为统一事件协议 ChatStreamEvent：每个 SSE 事件带 event: 字段（与 JSON 内 eventType 一致），
 * data 为单行 JSON：{eventType, messageId, seq, eventData}（会话实体移除后不再有 conversationId）。
 * 事件类型：start（携带 messageId）/ reasoning（思考文本增量，仅推理模型）/ content（eventData
 * 为文本增量）/ block（结构化展示块，eventData 为 MessageBlock，排在正文之后）/ finish（成功终态，
 * 收到即代表服务端已落库完成）/ error（失败终态，eventData 为 {code, message}）。
 * onEvent({eventType, messageId, seq, eventData}) 按事件类型分发；
 * Promise 在整个流结束时 resolve（中止/出错不视为本层异常，由事件本身表达）。
 *
 * eventData 结构按事件类型：start={model, agentCode}（菜单选定的分区键）；reasoning/content=文本增量；
 * finish={finishReason}；error={code, message}。
 * fetch 不走 umi request 的拦截器，需自带 Authorization 头；
 * 流开始前的错误（参数/未知 agentCode）仍走 HTTP 状态码，此处抛 Error（message 取后端错误体）。
 */
async function send(agentCode, data, { onEvent, signal } = {}) {
  const token = getAccessToken();
  const response = await fetch(`${API_PREFIX}${messagesPath(agentCode)}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data),
    signal,
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.message || `请求失败（${response.status}）`);
  }
  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  // 后端事件均为单行 JSON，normalize \r\n 不会破坏 payload
  let buffer = '';
  for (;;) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n');
    // SSE 事件以空行分隔；buffer 末段可能是不完整事件，留到下一轮
    let sepIndex = buffer.indexOf('\n\n');
    while (sepIndex !== -1) {
      const rawEvent = buffer.slice(0, sepIndex);
      buffer = buffer.slice(sepIndex + 2);
      const event = parseEvent(rawEvent);
      if (event) {
        onEvent?.(event);
      }
      sepIndex = buffer.indexOf('\n\n');
    }
  }
}

/** 解析单个 SSE 事件块：取 event: 行与 data: 行，data 解为 ChatStreamEvent 结构 */
function parseEvent(rawEvent) {
  const lines = rawEvent.split('\n');
  let eventType;
  const dataLines = [];
  lines.forEach((line) => {
    if (line.startsWith('event:')) {
      eventType = line.slice(6).trim();
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart());
    }
  });
  if (!dataLines.length) {
    return null;
  }
  const payload = JSON.parse(dataLines.join('\n'));
  // event: 字段与 JSON 内 eventType 一致，以 JSON 为准
  return {
    eventType: payload.eventType ?? eventType,
    messageId: payload.messageId,
    seq: payload.seq,
    eventData: payload.eventData,
  };
}

export const messageApi = {
  // 【后期扩展点】当前全量返回该 Agent 的历史；将来上下文窗口上线时这里要同步分页/最近 N 条，
  // 与后端 ChatTransactionService.initialize 的模型窗口口径保持一致。
  list: (agentCode) => request(messagesPath(agentCode), { method: 'GET' }),
  /** 清空该 Agent 的固定时间线（替代原来的「删除会话」） */
  clear: (agentCode) => request(messagesPath(agentCode), { method: 'DELETE' }),
  /**
   * 提交消息里的表单块（如「记账确认」）。普通 JSON 请求，不是 SSE：
   * 服务端在同一事务里执行动作（记账）+ 把块置为 done，返回更新后的块。
   * blockId 只在所属消息内唯一，所以路径里必须带 messageId。
   * 失败走全局 errorHandler 提示（重复提交、字段不合法等），Promise 会 reject。
   */
  submitBlock: (agentCode, messageId, blockId, values) =>
    request(`${messagesPath(agentCode)}/${messageId}/blocks/${blockId}/submit`, {
      method: 'POST',
      data: { values },
    }),
  send,
};
