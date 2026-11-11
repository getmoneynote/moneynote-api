import { DatePicker, Radio, Space } from 'antd';
import { radioValueToTimeRange, TIME_RANGE } from '@/utils/date';
import { useT } from '@/utils/i18n';

/**
 * 时间段快捷切换（分类环形图卡片右上角）：快捷选项 + 只读区间回显，
 * 交互对齐 old 版 CardExtra（选项语义：最近30天/最近1年/本月/今年/去年）。
 */
const TimeRangeExtra = ({ value, onChange }) => {
  const t = useT();

  const options = [
    { label: t('timeRange.last30Days'), value: TIME_RANGE.LAST_30_DAYS },
    { label: t('timeRange.last1Year'), value: TIME_RANGE.LAST_1_YEAR },
    { label: t('timeRange.thisMonth'), value: TIME_RANGE.THIS_MONTH },
    { label: t('timeRange.thisYear'), value: TIME_RANGE.THIS_YEAR },
    { label: t('timeRange.lastYear'), value: TIME_RANGE.LAST_YEAR },
  ];

  const [start, end] = radioValueToTimeRange(value);

  return (
    <Space>
      <Radio.Group
        options={options}
        optionType="button"
        size="small"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
      {/* 只读回显当前选项覆盖的实际区间，帮助用户确认统计口径 */}
      <DatePicker.RangePicker
        disabled
        allowClear={false}
        size="small"
        value={[start, end]}
      />
    </Space>
  );
};

export default TimeRangeExtra;
