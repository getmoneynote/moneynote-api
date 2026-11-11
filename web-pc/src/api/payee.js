import { createCrudApi } from './crud';

export const payeeApi = {
  ...createCrudApi('/payees'),
};
