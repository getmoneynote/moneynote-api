import { createCrudApi } from './crud';
import { request } from './request';

const PREFIX = '/llm-configs';

export const llmConfigApi = {
  ...createCrudApi(PREFIX),
  // 设为当前使用的配置；返回最新会话 VO（响应即失效通知，与 setBaseCurrency 同口径）
  activate: (id) => request(`${PREFIX}/${id}/activate`, { method: 'PATCH' }),
  // 不保存直接测连通性；apiKey 传空时后端取 id 对应配置的库存 key
  test: (data) => request(`${PREFIX}/test`, { method: 'POST', data }),
};
