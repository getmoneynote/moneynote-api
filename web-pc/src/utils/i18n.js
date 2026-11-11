// src/utils/i18n.js

import { useIntl } from '@umijs/max';
import { useCallback } from 'react';

/**
 * 国际化 Hook。
 *
 * 用于 React 组件、页面、ProTable columns 等场景。
 */
export function useT() {
  const intl = useIntl();

  return useCallback(
    (id, values) => {
      return intl.formatMessage({ id }, values);
    },
    [intl],
  );
}
