import { getLocale } from '@umijs/max';
import dayjs from 'dayjs';

// 日期展示格式随当前语言变化；未列出的语言回退到 ISO 风格 YYYY-MM-DD。
const DATE_FORMAT_BY_LOCALE = {
  'zh-CN': 'YYYY-MM-DD',
  'en-US': 'MM/DD/YYYY',
};

export function formatDate(timestamp) {
  if (!timestamp) return '';
  return dayjs(timestamp).format(
    DATE_FORMAT_BY_LOCALE[getLocale()] ?? 'YYYY-MM-DD',
  );
}

export function getDaysRemaining(timestamp) {
  if (!timestamp) return null;
  return dayjs(timestamp).startOf('day').diff(dayjs().startOf('day'), 'day');
}

// 时间段快捷选项值：1-今天 2-本周 3-本月 4-今年 5-去年 6-7天内 7-30天内 8-1年内（与 old 版同语义）
export const TIME_RANGE = {
  THIS_MONTH: 3,
  THIS_YEAR: 4,
  LAST_YEAR: 5,
  LAST_30_DAYS: 7,
  LAST_1_YEAR: 8,
};

/** 快捷选项值 → [起, 止] dayjs 数组（未匹配时回退最近 30 天）。 */
export function radioValueToTimeRange(value) {
  switch (value) {
    case TIME_RANGE.THIS_MONTH:
      return [dayjs().startOf('month'), dayjs().endOf('month')];
    case TIME_RANGE.THIS_YEAR:
      return [dayjs().startOf('year'), dayjs().endOf('year')];
    case TIME_RANGE.LAST_YEAR:
      return [
        dayjs().subtract(1, 'year').startOf('year'),
        dayjs().subtract(1, 'year').endOf('year'),
      ];
    case TIME_RANGE.LAST_1_YEAR:
      return [dayjs().subtract(1, 'year'), dayjs()];
    default:
      // 含 LAST_30_DAYS：与 old 版同语义，未知值回退最近 30 天
      return [dayjs().subtract(30, 'day'), dayjs()];
  }
}
