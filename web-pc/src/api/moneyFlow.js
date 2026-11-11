import { request } from './request';

export const moneyFlowApi = {
  list: (params) => request('/money-flows', { method: 'GET', params }),
  statistics: (params) =>
    request('/money-flows/statistics', { method: 'GET', params }),
  // 分类统计（概览页支出/收入分类环形图）：type 必填 1=支出 / 2=收入
  categoryStatistics: (params) =>
    request('/money-flows/category-statistics', { method: 'GET', params }),
  create: (data) => request('/money-flows', { method: 'POST', data }),
  update: (id, data) =>
    request(`/money-flows/${id}`, { method: 'PATCH', data }),
  delete: (id) => request(`/money-flows/${id}`, { method: 'DELETE' }),
};
