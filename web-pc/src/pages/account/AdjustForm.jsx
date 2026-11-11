import { DatePicker, Form, Input, InputNumber, Switch } from 'antd';
import dayjs from 'dayjs';
import { useEffect } from 'react';

import { accountApi } from '@/api/account';
import BaseModalForm from '@/components/BaseModalForm';
import { displayToMinor } from '@/utils/money';
import { useT } from '@/utils/i18n';

/**
 * 余额调整弹窗：把账户余额覆盖为目标值，后端写一条 type=4(ADJUST) 流水历史。
 * 目标余额默认回填为当前余额；余额允许为负（信用/贷款账户）。
 */
export default function AdjustForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, onOpenChange } = modal;
  const [form] = Form.useForm();

  // 币种精度由列表接口保证非空（Currency.decimalPlaces 列非空 + 创建强校验），
  // 缺失属契约破坏，交由 money.js 严格校验抛错，不做默认兜底。
  // currentRow 为 null 是弹窗未打开的正常态，此时精度不参与任何换算。
  const decimalPlaces = currentRow?.currencyDecimalPlaces;

  // 打开时重置并填默认值：时间=现在、计入统计=true；目标余额留空待用户填写（必填）
  useEffect(() => {
    if (!open) return;
    form.resetFields();
    form.setFieldsValue({
      createTime: dayjs(),
      include: true,
    });
  }, [open, currentRow, form]);

  const handleFinish = async (values) => {
    await accountApi.adjustBalance(currentRow.id, {
      balance: displayToMinor(values.balance, decimalPlaces),
      createTime: values.createTime ? values.createTime.valueOf() : undefined,
      title: values.title,
      notes: values.notes,
      include: values.include,
    });
    onFinish();
    return true;
  };

  return (
    <BaseModalForm
      form={form}
      title={t('account.adjust.title', { name: currentRow?.name })}
      open={open}
      onOpenChange={onOpenChange}
      layout="horizontal"
      labelCol={{ span: 8 }}
      wrapperCol={{ span: 16 }}
      modalProps={{ width: 520 }}
      onFinish={handleFinish}
    >
      <Form.Item
        name="balance"
        label={t('account.adjust.fields.targetBalance')}
        rules={[{ required: true }]}
      >
        <InputNumber precision={decimalPlaces} style={{ width: '100%' }} />
      </Form.Item>

      <Form.Item
        name="createTime"
        label={t('account.adjust.fields.createTime')}
        rules={[{ required: true }]}
      >
        <DatePicker showTime style={{ width: '100%' }} />
      </Form.Item>

      <Form.Item name="title" label={t('account.adjust.fields.title')}>
        <Input maxLength={32} />
      </Form.Item>

      <Form.Item name="notes" label={t('notes')}>
        <Input.TextArea rows={3} maxLength={1024} showCount />
      </Form.Item>

      <Form.Item
        name="include"
        label={t('account.adjust.fields.include')}
        valuePropName="checked"
      >
        <Switch />
      </Form.Item>
    </BaseModalForm>
  );
}