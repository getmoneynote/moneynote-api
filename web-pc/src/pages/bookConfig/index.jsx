import { PageContainer } from '@ant-design/pro-components';
import { Tabs } from 'antd';
import { useMemo, useState } from 'react';
import CategoryTree from './CategoryTree';
import PayeeTable from './PayeeTable';
import TagTable from './TagTable';
import { useT } from '@/utils/i18n';

const TAB_EXPENSE = 'expense';
const TAB_INCOME = 'income';
const TAB_TAG = 'tag';
const TAB_PAYEE = 'payee';

const TYPE_EXPENSE = 1;
const TYPE_INCOME = 2;

export default () => {
  const t = useT();
  const [activeTab, setActiveTab] = useState(TAB_EXPENSE);

  const tabItems = useMemo(
    () => [
      { key: TAB_EXPENSE, label: t('bookConfig.tabs.expenseCategory') },
      { key: TAB_INCOME, label: t('bookConfig.tabs.incomeCategory') },
      { key: TAB_TAG, label: t('bookConfig.tabs.transactionTag') },
      { key: TAB_PAYEE, label: t('bookConfig.tabs.transactionPayee') },
    ],
    [t],
  );

  const renderContent = () => {
    switch (activeTab) {
      case TAB_EXPENSE:
        return <CategoryTree key={TAB_EXPENSE} type={TYPE_EXPENSE} />;
      case TAB_INCOME:
        return <CategoryTree key={TAB_INCOME} type={TYPE_INCOME} />;
      case TAB_TAG:
        return <TagTable />;
      case TAB_PAYEE:
        return <PayeeTable />;
      default:
        return null;
    }
  };

  return (
    <PageContainer>
      <Tabs
        activeKey={activeTab}
        items={tabItems}
        onChange={setActiveTab}
      />
      {renderContent()}
    </PageContainer>
  );
};
