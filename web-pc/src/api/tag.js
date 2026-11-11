import { createCrudApi } from './crud';

export const tagApi = {
  ...createCrudApi('/tags'),
};
