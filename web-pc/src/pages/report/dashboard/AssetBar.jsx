import { StatisticCard } from '@ant-design/pro-components';
import { useRequest } from '@umijs/max';
import { theme } from 'antd';

import { accountApi } from '@/api/account';
import { useT } from '@/utils/i18n';
import { formatAmountDisplay } from '@/utils/money';

/**
 * 净资产概览条：总资产 / 总负债 / 净资产。
 * 数据来自 /accounts/netWorth，后端已按本位币折算并只统计"计入净资产"的账户。
 */
const AssetBar = () => {
  const t = useT();
  const { token } = theme.useToken();

  const { data, loading } = useRequest(() => accountApi.netWorth());

  return (
    <StatisticCard.Group
      direction="row"
      variant="borderless"
      headerBordered
      loading={loading}
      title={t('report.netWorth.title')}
    >
      <StatisticCard
        variant="borderless"
        statistic={{
          title: t('report.netWorth.assets'),
          value: formatAmountDisplay(data?.totalAssets, data?.decimalPlaces),
          prefix: data?.currencySymbol,
          valueStyle: { color: token.colorSuccess },
        }}
      />
      <StatisticCard.Divider type={'vertical'} />
      <StatisticCard
        variant="borderless"
        statistic={{
          title: t('report.netWorth.liabilities'),
          value: formatAmountDisplay(
            data?.totalLiabilities,
            data?.decimalPlaces,
          ),
          prefix: data?.currencySymbol,
          valueStyle: { color: token.colorError },
        }}
      />
      <StatisticCard.Divider type={'vertical'} />
      <StatisticCard
        variant="borderless"
        statistic={{
          title: t('report.netWorth.netWorth'),
          value: formatAmountDisplay(data?.netWorth, data?.decimalPlaces),
          prefix: data?.currencySymbol,
          valueStyle: {
            color: (data?.netWorth ?? 0) < 0 ? token.colorError : undefined,
            fontWeight: 'bold',
          },
        }}
      />
    </StatisticCard.Group>
  );
};

export default AssetBar;
