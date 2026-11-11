import { request as umiRequest } from '@umijs/max';

/**
 * 统一请求出口。
 * 以后如果要换成axios/自己实现的fetch，只需要改这个文件内部实现，
 * 对外的函数签名保持不变，其他地方不用动。
 *
 * 返回值统一为后端响应体的 data 字段（业务数据本身），
 * 与 useRequest 拿到的结构一致；外层的 success/message
 * 由 errorThrower / errorHandler 在拦截器层消费，业务代码不需要关心。
 */
export function request(url, options) {
  return umiRequest(url, options).then((body) => body?.data);
}
