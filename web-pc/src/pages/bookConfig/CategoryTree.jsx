import { PlusOutlined } from '@ant-design/icons';
import { App, Button } from 'antd';
import { useRef, useState } from 'react';
import { categoryApi } from '@/api/category';
import BaseProTable from '@/components/BaseProTable';
import { useModalForm } from '@/hooks/useModalForm';
import { useT } from '@/utils/i18n';
import CategoryForm from './CategoryForm';

export default function CategoryTree({ type }) {
  const t = useT();
  const { message, modal } = App.useApp();
  const modalForm = useModalForm();
  const actionRef = useRef(null);
  const [parentNode, setParentNode] = useState(null);

  const handleAddRoot = () => {
    setParentNode(null);
    modalForm.showAdd();
  };

  const handleAddChild = (record) => {
    setParentNode(record);
    modalForm.showAdd();
  };

  const handleEdit = (record) => {
    modalForm.showUpdate(record);
  };

  const handleDelete = (record) => {
    modal.confirm({
      title: t('bookConfig.category.delete.confirm', { name: record.name }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await categoryApi.delete(record.id);
        message.success(t('operation.success'));
        actionRef.current?.reload();
      },
    });
  };

  const handleFinish = async () => {
    message.success(t('operation.success'));
    actionRef.current?.reload();
    return true;
  };

  const columns = [
    {
      title: t('bookConfig.category.fields.name'),
      dataIndex: 'name',
      key: 'name',
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
      width: 220,
      align: 'center',
      render: (_, record) => [
        <Button
          key="addChild"
          type="link"
          size="small"
          disabled={record.level >= 4}
          onClick={() => handleAddChild(record)}
        >
          {t('bookConfig.category.action.addChild')}
        </Button>,
        <Button
          key="edit"
          type="link"
          size="small"
          onClick={() => handleEdit(record)}
        >
          {t('bookConfig.category.action.edit')}
        </Button>,
        <Button
          key="delete"
          type="link"
          size="small"
          danger
          disabled={record.children?.length > 0}
          title={record.children?.length > 0 ? t('bookConfig.category.delete.hasChildren') : undefined}
          onClick={() => handleDelete(record)}
        >
          {t('bookConfig.category.action.delete')}
        </Button>,
      ],
    },
  ];

  return (
    <>
      <BaseProTable
        columns={columns}
        search={false}
        rowKey="id"
        pagination={false}
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={handleAddRoot}>
            <PlusOutlined />
            {t('bookConfig.category.action.addRoot')}
          </Button>,
        ]}
        request={async () => {
          const res = await categoryApi.tree(type);
          return {
            data: res || [],
            success: true,
          };
        }}
        actionRef={actionRef}
        tableAlertRender={false}
        rowSelection={false}
        expandable={{ defaultExpandAllRows: true }}
        childrenColumnName="children"
      />
      <CategoryForm
        modal={modalForm}
        parentNode={parentNode}
        type={type}
        onFinish={handleFinish}
      />
    </>
  );
}
