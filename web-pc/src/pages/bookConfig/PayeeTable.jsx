import { PlusOutlined } from '@ant-design/icons';
import { App, Button, Tabs } from 'antd';
import { useRef, useState } from 'react';
import { payeeApi } from '@/api/payee';
import BaseProTable from '@/components/BaseProTable';
import EditableSwitch from '@/components/EditableSwitch';
import { useModalForm } from '@/hooks/useModalForm';
import { useT } from '@/utils/i18n';
import PayeeForm from './PayeeForm';

const TAB_KEYS = {
  ALL: 'all',
  EXPENSE: 'expense',
  INCOME: 'income',
};

export default function PayeeTable() {
  const t = useT();
  const { message, modal } = App.useApp();
  const modalForm = useModalForm();
  const actionRef = useRef(null);
  const [activeTab, setActiveTab] = useState(TAB_KEYS.ALL);

  const togglePayee = async (record, field, checked) => {
    await payeeApi.update(record.id, { [field]: checked });
    actionRef.current?.reload();
  };

  const handleDelete = (record) => {
    modal.confirm({
      title: t('bookConfig.payee.delete.confirm', { name: record.name }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await payeeApi.delete(record.id);
        message.success(t('operation.success'));
        actionRef.current?.reload();
      },
    });
  };

  const columns = [
    {
      title: t('bookConfig.payee.fields.name'),
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: t('bookConfig.payee.fields.canExpense'),
      dataIndex: 'canExpense',
      key: 'canExpense',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <EditableSwitch
          value={record.canExpense}
          onChange={(checked) => togglePayee(record, 'canExpense', checked)}
        />
      ),
    },
    {
      title: t('bookConfig.payee.fields.canIncome'),
      dataIndex: 'canIncome',
      key: 'canIncome',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <EditableSwitch
          value={record.canIncome}
          onChange={(checked) => togglePayee(record, 'canIncome', checked)}
        />
      ),
    },
    {
      title: t('notes'),
      dataIndex: 'notes',
      key: 'notes',
      ellipsis: true,
    },
    {
      title: t('operation'),
      key: 'action',
      width: 140,
      align: 'center',
      render: (_, record) => [
        <Button
          key="edit"
          type="link"
          size="small"
          onClick={() => modalForm.showUpdate(record)}
        >
          {t('update')}
        </Button>,
        <Button
          key="delete"
          type="link"
          size="small"
          danger
          onClick={() => handleDelete(record)}
        >
          {t('delete')}
        </Button>,
      ],
    },
  ];

  const tabItems = [
    { key: TAB_KEYS.ALL, label: t('bookConfig.payee.tabs.all') },
    { key: TAB_KEYS.EXPENSE, label: t('bookConfig.payee.tabs.expense') },
    { key: TAB_KEYS.INCOME, label: t('bookConfig.payee.tabs.income') },
  ];

  // 切 tab 只改变过滤参数；两个布尔不互斥，"全部"即不传任何过滤条件。
  const getFilterParams = (tab) => {
    switch (tab) {
      case TAB_KEYS.EXPENSE:
        return { canExpense: true };
      case TAB_KEYS.INCOME:
        return { canIncome: true };
      default:
        return {};
    }
  };

  return (
    <>
      <BaseProTable
        actionRef={actionRef}
        columns={columns}
        search={false}
        headerTitle={
          <Tabs
            activeKey={activeTab}
            items={tabItems}
            onChange={(key) => {
              setActiveTab(key);
              // 切换 tab 属于更换数据集，重置回第 1 页，否则停留在旧页码可能看到空列表
              actionRef.current?.reload(true);
            }}
          />
        }
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={() => modalForm.showAdd()}>
            <PlusOutlined />
            {t('add')}
          </Button>,
        ]}
        request={(params) => payeeApi.list({ ...params, ...getFilterParams(activeTab) })}
        pagination={{ pageSize: 10 }}
        rowSelection={false}
      />
      <PayeeForm
        modal={modalForm}
        onFinish={() => {
          // 新增的记录落在第 1 页，重置页码才能立即看到
          actionRef.current?.reload(true);
        }}
      />
    </>
  );
}