import { PageContainer } from '@ant-design/pro-components';

import CategoryReport from '../components/CategoryReport';

/** 收入分类统计页：搜索区域 + 环形图，共享实现见 CategoryReport（type=2 收入）。 */
const IncomeCategoryReport = () => (
  <PageContainer>
    <CategoryReport type={2} />
  </PageContainer>
);

export default IncomeCategoryReport;
