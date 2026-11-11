import { useCallback, useState } from 'react';

import { currencyApi } from '@/api/currency';

// 币种字典：全局唯一缓存，currency 管理页增删后 refresh 即全站生效；
// 弹窗隐藏时组件不卸载，不能靠 mount 触发请求，统一由 ensureLoaded/refresh 按需触发
let cache;
let inflight = null;

// 并发去重：首个调用发请求，期间的重复调用复用同一 promise，不叠加请求
const load = () => {
  if (!inflight) {
    inflight = currencyApi
      .options()
      .then((data) => {
        cache = data;
      })
      .finally(() => {
        inflight = null;
      });
  }
  return inflight;
};

/**
 * 币种 options 共享数据。返回：
 * - options：选项数组（未加载时为 []）
 * - loading
 * - ensureLoaded()：缓存为空才请求，缓存已在则什么都不做（回调引用稳定，可放心进 effect 依赖）
 * - refresh()：重新请求并更新缓存（请求进行中则复用，不叠加）
 */
export function useCurrencyOptions() {
  const [options, setOptions] = useState(cache ?? []);
  const [loading, setLoading] = useState(cache == null);

  const ensureLoaded = useCallback(() => {
    if (cache != null) return;
    setLoading(true);
    // 失败由 request 全局 errorHandler 提示，这里仅收敛本地 loading 态
    load()
      .catch(() => {})
      .finally(() => {
        setOptions(cache ?? []);
        setLoading(false);
      });
  }, []);

  const refresh = useCallback(() => {
    setLoading(true);
    load()
      .catch(() => {})
      .finally(() => {
        setOptions(cache ?? []);
        setLoading(false);
      });
  }, []);

  return { options, loading, ensureLoaded, refresh };
}
