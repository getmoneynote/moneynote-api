import { pickCrudApi } from './crud';
import { request } from './request';

const PREFIX = '/categories';

export const categoryApi = {
  ...pickCrudApi(PREFIX, ['create', 'update', 'delete']),
  tree: (type, bookId) =>
    request(`${PREFIX}/tree`, { method: 'GET', params: { type, ...(bookId != null ? { bookId } : {}) } }),
};
