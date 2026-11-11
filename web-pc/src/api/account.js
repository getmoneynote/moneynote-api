import { createCrudApi } from './crud';
import { request } from './request';

const crud = createCrudApi('/accounts');

export const accountApi = {
  ...crud,
  balanceSummary: (params) =>
    request('/accounts/balanceSummary', { method: 'GET', params }),
  netWorth: () => request('/accounts/netWorth', { method: 'GET' }),
  balanceDistribution: () =>
    request('/accounts/balanceDistribution', { method: 'GET' }),
  options: (params) => request('/accounts/options', { method: 'GET', params }),
  adjustBalance: (id, data) =>
    request(`/accounts/${id}/adjust`, { method: 'POST', data }),
};
