import { Column, Pie } from '@ant-design/plots';
import { Card, Empty, Flex, theme } from 'antd';
import { useMemo } from 'react';

import { useT } from '@/utils/i18n';

/** AntV 经典色板（与 CardPie 同源，图表风格保持一致） */
const PALETTE = [
  '#5B8FF9',
  '#5AD8A6',
  '#5D7092',
  '#F6BD16',
  '#E8684A',
  '#6DC8EC',
  '#9270CA',
  '#FF9D4D',
  '#269A99',
  '#FF99C3',
];

/** 主单位金额展示：只做千分位，不补零（小数位由后端按币种精度给好） */
const formatValue = (value) => Number(value).toLocaleString();

/**
 * 图表块：渲染后端下发的 ChartBlockData（chartType / title / currencyCode / items）。
 *
 * <p>数据的权威来源是后端工具（从业务快照确定性映射），前端只按 chartType 选组件、
 * 按 items 画图，不做任何聚合或解释——这样图上的数字与账务数据永远一致。
 * 未知 chartType 降级为环形图，避免新增图表形态时前端直接空白。
 *
 * @param {object} block - MessageBlock，data 为 ChartBlockData
 */
export default function ChartBlock({ block }) {
  const t = useT();
  const { token } = theme.useToken();
  const data = block?.data || {};
  const { chartType, title, currencyCode } = data;

  // 数值统一成 Number：后端用 BigDecimal 序列化，个别网关会当字符串传
  const items = useMemo(
    () =>
      (data.items || [])
        .map((item) => ({ name: item.name, value: Number(item.value) }))
        .filter((item) => Number.isFinite(item.value) && item.value > 0),
    [data.items],
  );

  const colorMap = useMemo(
    () =>
      new Map(items.map((item, i) => [item.name, PALETTE[i % PALETTE.length]])),
    [items],
  );
  const total = items.reduce((sum, item) => sum + item.value, 0);

  const renderChart = () => {
    if (chartType === 'column') {
      return (
        <Column
          data={items}
          xField="name"
          yField="value"
          height={260}
          autoFit
          legend={false}
          style={{ fill: (d) => colorMap.get(d.name) }}
          axis={{
            y: {
              labelFormatter: (v) => formatValue(v),
              grid: true,
            },
          }}
          tooltip={{
            title: false,
            items: [
              (d) => ({
                name: d.name,
                value: `${formatValue(d.value)} ${currencyCode ?? ''}`,
              }),
            ],
          }}
        />
      );
    }
    // 默认（含未知形态）走环形图
    return (
      <Pie
        data={items}
        angleField="value"
        colorField="name"
        height={260}
        autoFit
        radius={0.82}
        innerRadius={0.5}
        legend={false}
        style={{ fill: (d) => colorMap.get(d.name) }}
        label={{
          position: 'outside',
          text: (d) => `${d.name}: ${((d.value / total) * 100).toFixed(1)}%`,
          style: { fontSize: 12 },
          transform: [{ type: 'overlapDodgeY' }],
        }}
        tooltip={{
          title: false,
          items: [
            (d) => ({
              name: d.name,
              value: `${formatValue(d.value)} ${currencyCode ?? ''}`,
            }),
          ],
        }}
      />
    );
  };

  return (
    <Card
      size="small"
      title={title || t('aiAssistant.block.chart.title')}
      extra={
        currencyCode ? (
          <span style={{ color: token.colorTextTertiary }}>{currencyCode}</span>
        ) : null
      }
      style={{ maxWidth: 560 }}
      styles={{ body: { padding: '8px 8px 0' } }}
    >
      {items.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
      ) : (
        <>
          {renderChart()}
          {/* 自绘图例：环形图的 outside 标签只放得下名称+占比，金额放这里更稳 */}
          <Flex wrap gap={12} style={{ padding: '8px 8px 12px' }}>
            {items.map((item) => (
              <Flex key={item.name} align="center" gap={6}>
                <span
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: '50%',
                    background: colorMap.get(item.name),
                    flexShrink: 0,
                  }}
                />
                <span style={{ color: token.colorTextSecondary }}>
                  {item.name}
                </span>
                <span style={{ color: token.colorText }}>
                  {formatValue(item.value)}
                </span>
              </Flex>
            ))}
          </Flex>
        </>
      )}
    </Card>
  );
}
