import { useRequest } from '@umijs/max';
import { Col, Row } from 'antd';

import { accountApi } from '@/api/account';
import CardPie from '@/components/CardPie';
import { useT } from '@/utils/i18n';

/**
 * 资产/负债构成双饼图（账户维度）。
 * 数据来自 /accounts/balanceDistribution，后端已按本位币折算并只统计
 * "计入净资产"且余额非 0 的账户，按金额降序。
 */
const BalanceSheet = () => {
  const t = useT();

  const { data, loading } = useRequest(() => accountApi.balanceDistribution());

  const pies = [
    { title: t('report.asset.category'), list: data?.assets ?? [] },
    { title: t('report.liability.category'), list: data?.liabilities ?? [] },
  ];

  return (
    <Row gutter={[12, 12]}>
      {pies.map((pie) => (
        <Col key={pie.title} xs={24} lg={12}>
          <CardPie
            title={pie.title}
            data={pie.list}
            currency={{
              symbol: data?.currencySymbol,
              decimalPlaces: data?.decimalPlaces,
            }}
            loading={loading}
            style={{ height: '100%' }}
          />
        </Col>
      ))}
    </Row>
  );
};

export default BalanceSheet;
