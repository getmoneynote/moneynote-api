import { PageContainer } from '@ant-design/pro-components';

import CategoryReport from '../components/CategoryReport';

/** 支出分类统计页：搜索区域 + 环形图，共享实现见 CategoryReport（type=1 支出）。 */
const ExpenseCategoryReport = () => (
  <PageContainer>
    <CategoryReport type={1} />
  </PageContainer>
);

export default ExpenseCategoryReport;
