import { PlusOutlined } from '@ant-design/icons';
import { App, Button, Tag } from 'antd';
import { useRef } from 'react';
import { bookApi } from '@/api/book';
import BaseProTable from '@/components/BaseProTable';
import { useModalForm } from '@/hooks/useModalForm';
import { useT } from '@/utils/i18n';
import ActionForm from './ActionForm';

export default () => {
  const t = useT();
  // 静态 message/modal 不受 ConfigProvider 控制，组件内统一走 App.useApp()
  const { message, modal } = App.useApp();

  const modalForm = useModalForm();
  const actionRef = useRef(null);

  const updateHandler = (record) => {
    modalForm.showUpdate(record);
  };

  const setDefaultHandler = async (record) => {
    await bookApi.setDefault(record.id);
    message.success(t('operation.success'));
    actionRef.current?.reload();
  };

  // 删除：二次确认后调接口，失败提示由 request errorHandler 统一处理（确认框保持打开）
  const deleteHandler = (record) => {
    modal.confirm({
      title: t('book.delete.confirm', { name: record.name }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await bookApi.delete(record.id);
        message.success(t('operation.success'));
        actionRef.current?.reload();
      },
    });
  };

  // 当前行整行高亮（样式见 global.less 的 table-current-row，与 AI 配置列表共用）
  const rowClassName = (record) => (record.isCurrent ? 'table-current-row' : '');

  const columns = [
    {
      title: t('book.fields.name'),
      dataIndex: 'name',
      render: (_, record) => (
        <>
          {record.name}
          {record.isCurrent && (
            <Tag color="blue" style={{ marginLeft: 8 }}>
              {t('book.current')}
            </Tag>
          )}
        </>
      ),
    },
    {
      title: t('book.fields.currencyName'),
      dataIndex: 'currencyName',
    },
    {
      title: t('notes'),
      dataIndex: 'notes',
      ellipsis: true,
    },
    {
      title: t('operation'),
      align: 'center',
      render: (_, record) => [
        <Button
          key="setDefault"
          size="small"
          type="link"
          disabled={record.isCurrent}
          onClick={() => setDefaultHandler(record)}
        >
          {t('book.action.setDefault')}
        </Button>,
        <Button
          key="update"
          size="small"
          type="link"
          onClick={() => updateHandler(record)}
        >
          {t('update')}
        </Button>,
        // 当前账本后端禁止删除（user.currentBookId 会悬空），按钮置灰提前拦截
        <Button
          key="delete"
          disabled={record.isCurrent}
          size="small"
          type="link"
          onClick={() => deleteHandler(record)}
        >
          {t('delete')}
        </Button>,
      ],
    },
  ];

  return (
    <>
      <BaseProTable
        actionRef={actionRef}
        columns={columns}
        rowClassName={rowClassName}
        search={false}
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={() => modalForm.showAdd()}>
            <PlusOutlined />
            {t('add')}
          </Button>,
        ]}
        request={bookApi.list}
      />
      <ActionForm
        modal={modalForm}
        onFinish={() => {
          actionRef.current?.reload();
        }}
      />
    </>
  );
};
