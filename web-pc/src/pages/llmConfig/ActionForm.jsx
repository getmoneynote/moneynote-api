import { ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import { Alert, App, Button } from 'antd';
import { useRef, useState } from 'react';
import { llmConfigApi } from '@/api/llmConfig';
import BaseModalForm from '@/components/BaseModalForm';
import { useT } from '@/utils/i18n';

// 手动挑表单需要的字段；apiKey 不回填（列表只给了掩码），留空表示不修改
const toFormValues = (row) =>
  row
    ? {
        name: row.name,
        baseUrl: row.baseUrl,
        model: row.model,
        notes: row.notes,
      }
    : undefined;

export default function ActionForm({ modal, onFinish }) {
  const t = useT();
  const { message } = App.useApp();
  const { open, currentRow, isEdit, onOpenChange } = modal;

  // 测试连接要拿当前表单值，用 formRef 透传给 BaseModalForm
  const formRef = useRef();
  const [testing, setTesting] = useState(false);

  // 不保存直接测：新增时三个值都来自表单；编辑时 apiKey 留空则由后端取库存 key
  const testHandler = async () => {
    const values = await formRef.current?.validateFields();
    setTesting(true);
    try {
      await llmConfigApi.test({
        id: currentRow?.id,
        baseUrl: values.baseUrl,
        apiKey: values.apiKey,
        model: values.model,
      });
      message.success(t('llmConfig.test.success'));
    } finally {
      // 失败提示由 request errorHandler 统一处理
      setTesting(false);
    }
  };

  return (
    <BaseModalForm
      title={isEdit ? t('llmConfig.action.edit') : t('llmConfig.action.add')}
      open={open}
      onOpenChange={onOpenChange}
      formRef={formRef}
      initialValues={toFormValues(currentRow)}
      onFinish={async (values) => {
        if (isEdit) {
          // 只提交契约允许的字段；apiKey 留空后端不修改；notes 回填过值，空串即清空
          await llmConfigApi.update(currentRow.id, {
            name: values.name,
            baseUrl: values.baseUrl,
            apiKey: values.apiKey,
            model: values.model,
            notes: values.notes,
          });
        } else {
          await llmConfigApi.create(values);
        }
        onFinish();
        return true;
      }}
    >
      {/* 协议范围提示：当前后端只实现了 OpenAI Compatible 一种接入方式 */}
      <Alert
        type="info"
        showIcon
        message={t('llmConfig.protocol.hint')}
        style={{ marginBottom: 16 }}
      />
      <ProFormText
        name="name"
        label={t('llmConfig.fields.name')}
        rules={[{ required: true, max: 64 }]}
        fieldProps={{ maxLength: 64 }}
      />
      <ProFormText
        name="baseUrl"
        label={t('llmConfig.fields.baseUrl')}
        placeholder={t('llmConfig.fields.baseUrl.placeholder')}
        rules={[{ required: true, max: 256 }]}
        fieldProps={{ maxLength: 256 }}
      />
      <ProFormText.Password
        name="apiKey"
        label={t('llmConfig.fields.apiKey')}
        placeholder={
          isEdit ? t('llmConfig.fields.apiKey.placeholder') : undefined
        }
        rules={[{ required: !isEdit, max: 256 }]}
        fieldProps={{ maxLength: 256 }}
      />
      <ProFormText
        name="model"
        label={t('llmConfig.fields.model')}
        placeholder={t('llmConfig.fields.model.placeholder')}
        rules={[{ required: true, max: 128 }]}
        fieldProps={{ maxLength: 128 }}
      />
      <ProFormTextArea
        name="notes"
        label={t('notes')}
        fieldProps={{ rows: 3, maxLength: 1024, showCount: true }}
      />
      <Button block loading={testing} onClick={testHandler}>
        {t('llmConfig.test')}
      </Button>
    </BaseModalForm>
  );
}
