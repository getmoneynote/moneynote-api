import { useRef } from 'react';
import { useRequest } from '@umijs/max';

/**
 * 懒加载下拉数据：manual 请求，dropdown 每次打开时按需触发；
 * deps 变化（如切换账本/类型）后再次打开会重新请求，从未打开则不发请求。
 * 返回 useRequest 的全部字段 + 可直接绑到下拉组件的 onOpenChange。
 */
export function useLazyDropdown(service, deps = []) {
  const loadedKey = useRef();
  const req = useRequest(service, { manual: true });

  const onOpenChange = (open) => {
    if (!open) return;
    // undefined 序列化为 null，仅作 key 比较用
    const key = JSON.stringify(deps);
    if (loadedKey.current !== key) {
      loadedKey.current = key;
      req.run();
    }
  };

  return { ...req, onOpenChange };
}
