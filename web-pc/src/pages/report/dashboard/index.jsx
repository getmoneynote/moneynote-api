import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from '@umijs/max';
import { Col, Row, Space } from 'antd';

import { bookApi } from '@/api/book';
import { useT } from '@/utils/i18n';

import AssetBar from './AssetBar';
import BalanceSheet from './BalanceSheet';
import FlowCategory from './FlowCategory';

/** 与 old 概览页同构的垂直排布属性。 */
const spaceVProp = {
  direction: 'vertical',
  size: 'small',
  style: { width: '100%' },
};

/**
 * 统计概览页：净资产概览条 + 资产/负债构成双饼图 + 支出/收入分类双环形图。
 * 数据获取与展示分别在 AssetBar、BalanceSheet、FlowCategory 内完成。
 *
 * 支出/收入分类图固定统计「当前账本」（isCurrent 标记，与新增账单默认账本同源）；
 * 账户是用户级资源（不挂账本），净资产/资产负债构成天然跨账本，不走账本口径。
 */
const ReportDashboard = () => {
  const t = useT();

  // 当前账本解析：bookPage 未返回时为 undefined（分类图此时不发请求），
  // 返回后取 isCurrent 的 id；理论上有当前账本必命中，兜底 null（回退全账本）
  const { data: bookPage, loading: booksLoading } = useRequest(() =>
    bookApi.list({ page: 0, size: 1000 }),
  );
  const currentBookId = booksLoading
    ? undefined
    : ((bookPage?.content || []).find((b) => b.isCurrent)?.id ?? null);

  return (
    <PageContainer>
      <Space {...spaceVProp}>
        <AssetBar />
        <BalanceSheet />
        <Row gutter={[12, 12]} style={{ width: '100%' }}>
          <Col xs={24} lg={12}>
            <FlowCategory
              type={1}
              bookId={currentBookId}
              title={t('report.expense.category')}
            />
          </Col>
          <Col xs={24} lg={12}>
            <FlowCategory
              type={2}
              bookId={currentBookId}
              title={t('report.income.category')}
            />
          </Col>
        </Row>
      </Space>
    </PageContainer>
  );
};

export default ReportDashboard;
