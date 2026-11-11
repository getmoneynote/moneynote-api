import { PlusOutlined } from '@ant-design/icons';
import { useModel } from '@umijs/max';
import { App, Button, Tag } from 'antd';
import { useRef } from 'react';
import { llmConfigApi } from '@/api/llmConfig';
import BaseProTable from '@/components/BaseProTable';
import { useModalForm } from '@/hooks/useModalForm';
import { useT } from '@/utils/i18n';
import ActionForm from './ActionForm';

export default () => {
  const t = useT();
  // 静态 message/modal 不受 ConfigProvider 控制，组件内统一走 App.useApp()
  const { message, modal } = App.useApp();
  const { setInitialState } = useModel('@@initialState');

  const modalForm = useModalForm();
  const actionRef = useRef(null);

  const addHandler = () => {
    modalForm.showAdd();
  };

  const updateHandler = (record) => {
    modalForm.showUpdate(record);
  };

  // 删除：二次确认后调接口，失败提示由 request errorHandler 统一处理（确认框保持打开）
  const deleteHandler = (record) => {
    modal.confirm({
      title: t('llmConfig.delete.confirm', { name: record.name }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await llmConfigApi.delete(record.id);
        message.success(t('operation.success'));
        actionRef.current?.reload();
      },
    });
  };

  // 设为当前：响应即最新会话 VO，直接写回全局用户态（与设本位币同口径）
  const activateHandler = async (record) => {
    const currentUser = await llmConfigApi.activate(record.id);
    setInitialState((prev) => ({ ...prev, currentUser }));
    message.success(t('operation.success'));
    actionRef.current?.reload();
  };

  // 当前行整行高亮（样式见 global.less 的 table-current-row，与账本列表共用）
  const rowClassName = (record) => (record.isCurrent ? 'table-current-row' : '');

  const columns = [
    {
      title: t('llmConfig.fields.name'),
      dataIndex: 'name',
      search: false,
      // 「当前配置」的标识收进名称列：Tag + 整行高亮（rowClassName），不再单开状态列
      render: (_, record) => (
        <>
          {record.name}
          {record.isCurrent && (
            <Tag color="blue" style={{ marginLeft: 8 }}>
              {t('llmConfig.current')}
            </Tag>
          )}
        </>
      ),
    },
    {
      title: t('llmConfig.fields.baseUrl'),
      dataIndex: 'baseUrl',
      search: false,
      ellipsis: true,
    },
    {
      title: t('llmConfig.fields.model'),
      dataIndex: 'model',
      search: false,
    },
    {
      title: t('llmConfig.fields.apiKey'),
      dataIndex: 'maskedApiKey',
      search: false,
    },
    {
      title: t('notes'),
      dataIndex: 'notes',
      search: false,
      ellipsis: true,
    },
    {
      title: t('operation'),
      search: false,
      align: 'center',
      width: 200,
      render: (_, record) => {
        return [
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
            // 正在使用中的配置不允许删除（后端同口径拦截）
            disabled={record.isCurrent}
            onClick={() => deleteHandler(record)}
          >
            {t('delete')}
          </Button>,
          <Button
            key="activate"
            disabled={record.isCurrent}
            size="small"
            type="link"
            onClick={() => activateHandler(record)}
          >
            {t('llmConfig.set.current')}
          </Button>,
        ];
      },
    },
  ];

  return (
    <>
      <BaseProTable
        actionRef={actionRef}
        columns={columns}
        rowClassName={rowClassName}
        search={false}
        pagination={false}
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={() => addHandler()}>
            <PlusOutlined />
            {t('add')}
          </Button>,
        ]}
        // 配置数量极少，不分页：list 返回数组，包装成 ProTable 结构原样透传
        request={async () => {
          const data = await llmConfigApi.list();
          return { data, success: true };
        }}
      />
      <ActionForm
        modal={modalForm}
        onFinish={() => {
          message.success(t('operation.success'));
          actionRef.current?.reload();
        }}
      />
    </>
  );
};
