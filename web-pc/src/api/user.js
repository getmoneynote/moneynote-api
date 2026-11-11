import { request } from './request';

export const userApi = {
  register: (body) => {
    return request('/register', {
      method: 'POST',
      data: body,
    });
  },

  login: (body) => {
    return request('/login', {
      method: 'POST',
      data: body,
    });
  },

  logout: () => {
    return request('/logout', {
      method: 'POST',
      skipErrorHandler: true,
    });
  },

  version: () => {
    return request('/version', {
      method: 'GET',
      skipErrorHandler: true,
    });
  },

  setBaseCurrency: (id) => {
    return request(`/setBaseCurrency/${id}`, {
      method: 'PATCH',
    });
  },

  getInitState: () => {
    return request('/initState', {
      method: 'GET',
    });
  },
};
