import { useRequest } from '@umijs/max';
import { useState } from 'react';

import { moneyFlowApi } from '@/api/moneyFlow';
import CardPie from '@/components/CardPie';
import { radioValueToTimeRange, TIME_RANGE } from '@/utils/date';

import TimeRangeExtra from './TimeRangeExtra';

/**
 * 支出/收入分类环形图（概览页）。
 * 数据来自 /money-flows/category-statistics：后端按根分类汇总，金额折算本位币、
 * 降序返回，百分比由 CardPie 按占比自行计算。固定统计「当前账本」。
 *
 * @param {number} type - 流水类型：1=支出 2=收入（与后端 MoneyFlow.type 一致）
 * @param {string} title - 卡片标题
 * @param {number|null|undefined} [bookId] - 当前账本 id（页面解析后传入）；
 *   undefined = 尚未解析完成（不发请求），null = 无当前账本（兜底回退全账本）
 */
const FlowCategory = ({ type, title, bookId }) => {
  const [timeRange, setTimeRange] = useState(TIME_RANGE.LAST_30_DAYS);
  const [start, end] = radioValueToTimeRange(timeRange);

  // 固定查当前账本：bookId 未解析完成前不发请求，避免先按全账本查一遍再重查；
  // 统计口径与流水页统计条一致：只统计「计入统计」的流水（include=true）
  const { data, loading } = useRequest(
    () =>
      moneyFlowApi.categoryStatistics({
        type,
        include: true,
        ...(bookId != null ? { bookId } : {}),
        minTime: start.valueOf(),
        maxTime: end.valueOf(),
      }),
    { refreshDeps: [timeRange, bookId], ready: bookId !== undefined },
  );

  return (
    <CardPie
      title={title}
      data={data?.categories}
      currency={{
        symbol: data?.currencySymbol,
        decimalPlaces: data?.currencyDecimalPlaces,
      }}
      loading={loading}
      extra={<TimeRangeExtra value={timeRange} onChange={setTimeRange} />}
      style={{ height: '100%' }}
    />
  );
};

export default FlowCategory;
