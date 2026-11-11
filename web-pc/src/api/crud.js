import { request } from './request';

// 分页参数直接透传后端契约：page（0 起）/ size。
// antd ProTable 的 current（1 起）/ pageSize 只在 BaseProTable 边界转换，API 层不做转换。
const CRUD_METHODS = {
  create: (prefix) => (data) => request(prefix, { method: 'POST', data }),
  list: (prefix) => (params) => request(prefix, { method: 'GET', params }),
  update: (prefix) => (id, data) => request(`${prefix}/${id}`, { method: 'PATCH', data }),
  delete: (prefix) => (id) => request(`${prefix}/${id}`, { method: 'DELETE' }),
};

export function createCrudApi(prefix) {
  return Object.fromEntries(
    Object.entries(CRUD_METHODS).map(([name, fn]) => [name, fn(prefix)]),
  );
}

export function pickCrudApi(prefix, keys) {
  return Object.fromEntries(
    keys.map((key) => [key, CRUD_METHODS[key](prefix)]),
  );
}
