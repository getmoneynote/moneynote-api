import { createCrudApi } from './crud';

export const reminderApi = {
  ...createCrudApi('/reminders'),
};
