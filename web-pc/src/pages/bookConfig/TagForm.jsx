import { ProFormSwitch, ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import { Row, Col } from 'antd';
import BaseModalForm from '@/components/BaseModalForm';
import { tagApi } from '@/api/tag';
import { useT } from '@/utils/i18n';

const toPayload = (values) => ({
  name: values.name,
  notes: values.notes || null,
  canExpense: values.canExpense ?? false,
  canIncome: values.canIncome ?? false,
  canTransfer: values.canTransfer ?? false,
});

export default function TagForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, isEdit, onOpenChange } = modal;

  return (
    <BaseModalForm
      title={isEdit ? t('bookConfig.tag.action.edit') : t('bookConfig.tag.action.add')}
      open={open}
      onOpenChange={onOpenChange}
      initialValues={currentRow}
      horizontal
      onFinish={async (values) => {
        const payload = toPayload(values);
        if (isEdit) {
          await tagApi.update(currentRow.id, payload);
        } else {
          await tagApi.create(payload);
        }
        onFinish?.();
        return true;
      }}
    >
      <ProFormText
        name="name"
        label={t('bookConfig.tag.fields.name')}
        placeholder={t('bookConfig.tag.fields.name')}
        rules={[{ required: true, max: 64 }]}
        fieldProps={{ maxLength: 64 }}
      />
      <Row style={{ width: '100%' }}>
        <Col span={8}>
          <ProFormSwitch
            name="canExpense"
            label={t('bookConfig.tag.fields.canExpense')}
            initialValue={true}
          />
        </Col>
        <Col span={8}>
          <ProFormSwitch
            name="canIncome"
            label={t('bookConfig.tag.fields.canIncome')}
            initialValue={true}
          />
        </Col>
        <Col span={8}>
          <ProFormSwitch
            name="canTransfer"
            label={t('bookConfig.tag.fields.canTransfer')}
            initialValue={false}
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
