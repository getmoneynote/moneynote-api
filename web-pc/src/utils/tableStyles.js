import { createStyles } from 'antd-style';

/**
 * 表格数值列的统一等宽数字样式。
 *
 * 对齐（text-align）由列配置的 align: 'right' 控制，这里只补 font-variant-numeric，
 * 让金额 / 数量纵向比较时小数点逐位对齐。见 docs/表格规范.md。
 */
export const useTableStyles = createStyles(({ css }) => ({
  numericCell: css`
    font-variant-numeric: tabular-nums;
  `,
}));