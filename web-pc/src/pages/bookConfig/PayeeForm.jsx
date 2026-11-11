import { ProFormSwitch, ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import { Row, Col } from 'antd';
import BaseModalForm from '@/components/BaseModalForm';
import { payeeApi } from '@/api/payee';
import { useT } from '@/utils/i18n';

const toPayload = (values) => ({
  name: values.name,
  notes: values.notes || null,
  canExpense: values.canExpense ?? false,
  canIncome: values.canIncome ?? false,
});

export default function PayeeForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, isEdit, onOpenChange } = modal;

  return (
    <BaseModalForm
      title={isEdit ? t('bookConfig.payee.action.edit') : t('bookConfig.payee.action.add')}
      open={open}
      onOpenChange={onOpenChange}
      initialValues={currentRow}
      horizontal
      onFinish={async (values) => {
        const payload = toPayload(values);
        if (isEdit) {
          await payeeApi.update(currentRow.id, payload);
        } else {
          await payeeApi.create(payload);
        }
        onFinish?.();
        return true;
      }}
    >
      <ProFormText
        name="name"
        label={t('bookConfig.payee.fields.name')}
        placeholder={t('bookConfig.payee.fields.name')}
        rules={[{ required: true, max: 64 }]}
        fieldProps={{ maxLength: 64 }}
      />
      <Row style={{ width: '100%' }}>
        <Col span={12}>
          <ProFormSwitch
            name="canExpense"
            label={t('bookConfig.payee.fields.canExpense')}
            initialValue={true}
          />
        </Col>
        <Col span={12}>
          <ProFormSwitch
            name="canIncome"
            label={t('bookConfig.payee.fields.canIncome')}
            initialValue={true}
          />
        </Col>
      </Row>
      <ProFormTextArea
        name="notes"
        label={t('notes')}
        placeholder={t('notes')}
        fieldProps={{ rows: 3, maxLength: 1024, showCount: true }}
      />
    </BaseModalForm>
  );
}
