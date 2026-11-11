import { ProFormDatePicker, ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import dayjs from 'dayjs';
import { reminderApi } from '@/api/reminder';
import BaseModalForm from '@/components/BaseModalForm';
import { useT } from '@/utils/i18n';

const toFormValues = (row) =>
  row
    ? {
        title: row.title,
        notes: row.notes,
        executeDate: row.executeDate ? dayjs(row.executeDate) : undefined,
      }
    : undefined;

const toPayload = (values) => ({
  title: values.title,
  notes: values.notes,
  // ProFormDatePicker 默认 dateFormatter='string'，提交的是 "YYYY-MM-DD" 字符串，需转回时间戳
  executeDate: values.executeDate ? dayjs(values.executeDate).startOf('day').valueOf() : undefined,
});

export default function ActionForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, isEdit, onOpenChange } = modal;

  return (
    <BaseModalForm
      title={isEdit ? t('reminder.action.edit') : t('reminder.action.add')}
      open={open}
      onOpenChange={onOpenChange}
      initialValues={toFormValues(currentRow)}
      horizontal
      onFinish={async (values) => {
        const payload = toPayload(values);

        if (isEdit) {
          await reminderApi.update(currentRow.id, payload);
        } else {
          await reminderApi.create(payload);
        }

        onFinish();
        return true;
      }}
    >
      <ProFormText
        name="title"
        label={t('reminder.fields.title')}
        rules={[{ required: true, max: 64 }]}
        fieldProps={{ maxLength: 64 }}
        placeholder={t('reminder.fields.title.placeholder')}
      />
      <ProFormDatePicker
        name="executeDate"
        label={t('reminder.fields.executeDate')}
        rules={[{ required: true }]}
      />
      <ProFormTextArea
        name="notes"
        label={t('notes')}
        rows={4}
        maxLength={1024}
        showCount
      />
    </BaseModalForm>
  );
}
