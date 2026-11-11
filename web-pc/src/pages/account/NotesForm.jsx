import { Form, Input } from 'antd';
import { useEffect } from 'react';

import { accountApi } from '@/api/account';
import BaseModalForm from '@/components/BaseModalForm';
import { useT } from '@/utils/i18n';

/**
 * 备注弹窗：单独查看/编辑账户备注，不必打开完整编辑表单。
 * 大文本域便于阅读长备注；清空保存即清除备注（PATCH 只跳过 null，空串会正常落库）。
 */
export default function NotesForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, onOpenChange } = modal;
  const [form] = Form.useForm();

  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue({ notes: currentRow?.notes });
  }, [open, currentRow, form]);

  const handleFinish = async (values) => {
    await accountApi.update(currentRow.id, { notes: values.notes ?? '' });
    onFinish();
    return true;
  };

  return (
    <BaseModalForm
      form={form}
      title={t('account.notes.title', { name: currentRow?.name })}
      open={open}
      onOpenChange={onOpenChange}
      modalProps={{ width: 700 }}
      onFinish={handleFinish}
    >
      <Form.Item name="notes">
        <Input.TextArea rows={15} showCount style={{ resize: 'vertical' }} />
      </Form.Item>
    </BaseModalForm>
  );
}
