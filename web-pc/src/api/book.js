import { createCrudApi } from './crud';
import { request } from './request';

const crud = createCrudApi('/books');

export const bookApi = {
  ...crud,
  setDefault: (id) => {
    return request(`/books/${id}/setDefault`, {
      method: 'PATCH',
    });
  },
};
