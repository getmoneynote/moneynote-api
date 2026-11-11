import { PlusOutlined } from '@ant-design/icons';
import { App, Button, Checkbox, Tabs, theme } from 'antd';
import { useRef, useState } from 'react';

import { reminderApi } from '@/api/reminder';
import BaseProTable from '@/components/BaseProTable';
import { useModalForm } from '@/hooks/useModalForm';
import { formatDate, getDaysRemaining } from '@/utils/date';
import { useT } from '@/utils/i18n';
import ActionForm from './ActionForm';

const TAB_KEYS = {
  ALL: 'all',
  PENDING: 'pending',
  COMPLETED: 'completed',
};

export default () => {
  const t = useT();
  const { message, modal } = App.useApp();
  const { token } = theme.useToken();

  const modalForm = useModalForm();
  const actionRef = useRef(null);
  const [activeTab, setActiveTab] = useState(TAB_KEYS.PENDING);
  const [toggling, setToggling] = useState(false);

  const addHandler = () => {
    modalForm.showAdd();
  };

  const updateHandler = (record) => {
    modalForm.showUpdate(record);
  };

  const toggleCompleteHandler = async (record) => {
    if (toggling) return;
    setToggling(true);
    try {
      await reminderApi.update(record.id, { completed: !record.completed });
      message.success(t('operation.success'));
      actionRef.current?.reload();
    } finally {
      setToggling(false);
    }
  };

  const deleteHandler = (record) => {
    modal.confirm({
      title: t('reminder.delete.confirm', { title: record.title }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await reminderApi.delete(record.id);
        message.success(t('operation.success'));
        actionRef.current?.reload();
      },
    });
  };

  const copyHandler = (record) => {
    modalForm.showCopy(record);
  };

  const renderDaysRemaining = (_, record) => {
    const days = getDaysRemaining(record.executeDate);
    if (days === null) return '';
    if (record.completed) {
      return <span style={{ color: token.colorTextDisabled }}>{t('reminder.days.completed')}</span>;
    }
    if (days < 0) {
      return <span style={{ color: token.colorError }}>{t('reminder.days.overdue', { days: -days })}</span>;
    }
    if (days === 0) {
      return <span style={{ color: token.colorWarning }}>{t('reminder.days.today')}</span>;
    }
    return <span style={{ color: days <= 3 ? token.colorWarning : token.colorSuccess }}>{t('reminder.days.remaining', { days })}</span>;
  };

  const columns = [
    {
      title: t('reminder.fields.title'),
      dataIndex: 'title',
      render: (text, record) => (
        <span style={{ textDecoration: record.completed ? 'line-through' : 'none', color: record.completed ? token.colorTextDisabled : 'inherit' }}>
          {text}
        </span>
      ),
    },
    {
      title: t('reminder.fields.executeDate'),
      dataIndex: 'executeDate',
      width: 140,
      sorter: true,
      defaultSortOrder: 'ascend',
      align: 'left',
      render: formatDate,
    },
    {
      title: t('notes'),
      dataIndex: 'notes',
      ellipsis: true,
    },
    {
      title: t('reminder.fields.daysRemaining'),
      dataIndex: 'executeDate',
      width: 120,
      align: 'center',
      render: renderDaysRemaining,
    },
    {
      title: t('reminder.fields.completed'),
      dataIndex: 'completed',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Checkbox
          checked={record.completed}
          disabled={toggling}
          onChange={() => toggleCompleteHandler(record)}
        />
      ),
    },
    {
      title: t('operation'),
      align: 'center',
      width: 220,
      render: (_, record) => {
        return [
          <Button
            key="copy"
            size="small"
            type="link"
            onClick={() => copyHandler(record)}
          >
            {t('copy')}
          </Button>,
          <Button
            key="update"
            size="small"
            type="link"
            onClick={() => updateHandler(record)}
          >
            {t('update')}
          </Button>,
          <Button
            key="delete"
            size="small"
            type="link"
            danger
            onClick={() => deleteHandler(record)}
          >
            {t('delete')}
          </Button>,
        ];
      },
    },
  ];

  const tabItems = [
    { key: TAB_KEYS.ALL, label: t('reminder.tabs.all') },
    { key: TAB_KEYS.PENDING, label: t('reminder.tabs.pending') },
    { key: TAB_KEYS.COMPLETED, label: t('reminder.tabs.completed') },
  ];

  const getCompletedParam = (tab) => {
    switch (tab) {
      case TAB_KEYS.PENDING:
        return false;
      case TAB_KEYS.COMPLETED:
        return true;
      default:
        return undefined;
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
          <Button key="add" type="primary" onClick={() => addHandler()}><PlusOutlined />{t('add')}</Button>
        ]}
        request={(params) => reminderApi.list({ ...params, completed: getCompletedParam(activeTab) })}
      />
      <ActionForm
        modal={modalForm}
        onFinish={() => {
          message.success(t('operation.success'));
          // 新增的记录落在第 1 页，重置页码才能立即看到
          actionRef.current?.reload(true);
        }}
      />
    </>
  );
};
