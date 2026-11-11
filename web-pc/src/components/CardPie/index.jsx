import { Pie } from '@ant-design/plots';
import { Card, Empty, Flex, theme } from 'antd';
import { useMemo, useState } from 'react';

import { useT } from '@/utils/i18n';
import { formatAmountDisplay } from '@/utils/money';

/** AntV 经典 20 色板（old/bizcharts 的默认视觉），扇区与自绘图例共用同一映射。 */
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
  '#A0D911',
  '#13C2C2',
  '#722ED1',
  '#FA8C16',
  '#EB2F96',
  '#52C41A',
  '#2F54EB',
  '#FAAD14',
  '#F5222D',
  '#1DA57A',
];

/**
 * 构成饼图卡片：账户维度环形图，视觉对齐 old（bizcharts 版）——
 * 扇区外侧标「名称: 百分比」、圆环中心显总金额、右侧图例含名称/百分比/金额，
 * 点击图例可隐藏对应扇区（总数随之重算）。
 *
 * @param {string} title - 卡片标题
 * @param {Array<{name: string, amount: number}>} data - 构成明细（amount 为本位币最小单位，恒为正）
 * @param {{symbol?: string, decimalPlaces?: number}} currency - 币种信息，用于 tooltip 金额格式化
 * @param {boolean} loading
 * @param {object} [style] - 透传给 Card 的样式（如并排时配 height: '100%' 等高）
 * @param {ReactNode} [extra] - 卡片右上角附加内容（如时间段快捷切换）
 */
const CardPie = ({
  title,
  data = [],
  currency,
  loading = false,
  style,
  extra,
}) => {
  const t = useT();
  const { token } = theme.useToken();

  // 名称 → 颜色固定映射（按下标取色板），过滤后各扇区颜色不漂移
  const colorMap = useMemo(
    () =>
      new Map(data.map((item, i) => [item.name, PALETTE[i % PALETTE.length]])),
    [data],
  );

  const [hidden, setHidden] = useState(() => new Set());
  const toggle = (name) => {
    setHidden((prev) => {
      const next = new Set(prev);
      if (next.has(name)) {
        next.delete(name);
      } else {
        // 至少保留一个可见扇区，避免全隐藏后空图
        if (next.size >= data.length - 1) return prev;
        next.add(name);
      }
      return next;
    });
  };

  const visible = data.filter((item) => !hidden.has(item.name));
  const total = visible.reduce((sum, item) => sum + item.amount, 0);
  const percentOf = (amount) => `${((amount / total) * 100).toFixed(2)}%`;

  return (
    <Card
      variant="borderless"
      title={title}
      extra={extra}
      loading={loading}
      style={style}
      styles={{ body: { padding: '16px 8px' } }}
    >
      {data.length === 0 ? (
        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
      ) : (
        <Flex gap={12} align="center">
          <div style={{ flex: 1, minWidth: 0, height: 400 }}>
            <Pie
              data={visible}
              angleField="amount"
              colorField="name"
              // 半径缩到 0.8（默认 1 会撑满短边、外缘贴画布），给 outside 标签
              // 的引导线+文字留出上下空间，否则顶部标签被画布裁掉
              radius={0.8}
              innerRadius={0.6}
              height={400}
              autoFit
              // 颜色用固定映射（不走 scale），保证过滤后各扇区颜色不变
              style={{ fill: (d) => colorMap.get(d.name) }}
              legend={false}
              label={{
                position: 'outside',
                text: (d) => `${d.name}: ${percentOf(d.amount)}`,
                style: { fontSize: 12 },
                // outside 标签默认不避让，小扇区（如一堆 0.00%）的标签会
                // 全部叠在顶部；overlapDodgeY 自动上下错开，old 同款效果
                transform: [{ type: 'overlapDodgeY' }],
              }}
              tooltip={{
                // 单行文案「名称 百分比 金额」，与右侧图例同一套格式化；
                // marker:false 去掉行首圆点，title:false 去掉默认的标题行
                // （G2 5 默认取 colorField 值作为标题，会和 items 里的名称重复）
                marker: false,
                title: false,
                items: [
                  (d) => ({
                    name: d.name,
                    value: `${percentOf(d.amount)} ${formatAmountDisplay(
                      d.amount,
                      currency?.decimalPlaces,
                    )}`,
                  }),
                ],
              }}
              annotations={[
                {
                  type: 'text',
                  style: {
                    x: '50%',
                    y: '46%',
                    text: t('report.gross.amount'),
                    fontSize: 14,
                    fill: token.colorTextSecondary,
                    textAlign: 'center',
                    textBaseline: 'middle',
                  },
                },
                {
                  type: 'text',
                  style: {
                    x: '50%',
                    y: '57%',
                    text: formatAmountDisplay(total, currency?.decimalPlaces),
                    fontSize: 26,
                    fontWeight: 600,
                    fill: token.colorText,
                    textAlign: 'center',
                    textBaseline: 'middle',
                  },
                },
              ]}
            />
          </div>

          {/* 自绘图例：名称 / 百分比 / 金额 三列，点击隐藏（old 同款交互） */}
          <Flex
            vertical
            gap={4}
            style={{
              width: 230,
              maxHeight: 400,
              overflowY: 'auto',
              flexShrink: 0,
            }}
          >
            {data.map((item) => {
              const off = hidden.has(item.name);
              return (
                <Flex
                  key={item.name}
                  align="center"
                  gap={8}
                  onClick={() => toggle(item.name)}
                  style={{
                    cursor: 'pointer',
                    padding: '4px 6px',
                    borderRadius: token.borderRadiusLG,
                    opacity: off ? 0.4 : 1,
                  }}
                >
                  <span
                    style={{
                      width: 8,
                      height: 8,
                      borderRadius: '50%',
                      background: colorMap.get(item.name),
                      flexShrink: 0,
                    }}
                  />
                  <span
                    style={{
                      flex: 1,
                      minWidth: 0,
                      wordBreak: 'break-all',
                      color: token.colorText,
                    }}
                  >
                    {item.name}
                  </span>
                  <span
                    style={{
                      width: 56,
                      textAlign: 'right',
                      color: token.colorTextTertiary,
                      flexShrink: 0,
                    }}
                  >
                    {percentOf(item.amount)}
                  </span>
                  <span
                    style={{
                      width: 64,
                      textAlign: 'right',
                      color: token.colorText,
                      flexShrink: 0,
                    }}
                  >
                    {formatAmountDisplay(item.amount, currency?.decimalPlaces)}
                  </span>
                </Flex>
              );
            })}
          </Flex>
        </Flex>
      )}
    </Card>
  );
};

export default CardPie;
