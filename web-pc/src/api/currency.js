import { createCrudApi } from './crud';
import { request } from './request';

const PREFIX = '/currencies';

export const currencyApi = {
  ...createCrudApi(PREFIX),
  options: () => request(`${PREFIX}/options`, { method: 'GET' }),
};

