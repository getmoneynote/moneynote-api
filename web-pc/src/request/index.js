import { authHeaderInterceptor } from './interceptors';
import { errorThrower, errorHandler } from './errorHandler';

export const requestConfig = {
  errorConfig: { errorThrower, errorHandler },
  requestInterceptors: [authHeaderInterceptor],
  responseInterceptors: [],
};
