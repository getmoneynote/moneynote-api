import { ProFormDigit, ProFormText } from '@ant-design/pro-components';
import { currencyApi } from '@/api/currency';
import BaseModalForm from '@/components/BaseModalForm';
import { useT } from '@/utils/i18n';

// 手动挑表单需要的字段，避免 id / isBase / baseRate 等后端字段被带进表单
const toFormValues = (row) =>
  row
    ? {
        code: row.code,
        name: row.name,
        symbol: row.symbol,
        decimalPlaces: row.decimalPlaces,
        usdRate: row.usdRate,
      }
    : undefined;

export default function ActionForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, isEdit, onOpenChange } = modal;

  const rateEditable = currentRow?.rateEditable !== false;

  return (
    <BaseModalForm
      title={isEdit ? t('currency.action.edit') : t('currency.action.add')}
      open={open}
      onOpenChange={onOpenChange}
      initialValues={toFormValues(currentRow)}
      onFinish={async (values) => {
        if (isEdit) {
          await currencyApi.update(currentRow.id, {
            name: values.name,
            usdRate: values.usdRate,
          });
        } else {
          await currencyApi.create(values);
        }
        onFinish();
        return true;
      }}
    >
      <ProFormText
        disabled={ isEdit }
        name="code"
        label={t('currency.fields.code')}
        rules={[
          { required: true },
          { pattern: '^[A-Z]{3}$', message: t('currency.validation.code') },
        ]}
        fieldProps={{ maxLength: 3 }}
      />
      <ProFormText
        name="name"
        label={t('currency.fields.name')}
        rules={[{ required: true, max: 16 }]}
        fieldProps={{ maxLength: 16 }}
      />

      {/* 后端 update 不接收 symbol，编辑态一律禁用，避免改了被静默丢弃 */}
      <ProFormText
        name="symbol"
        disabled={ isEdit }
        label={t('currency.fields.symbol')}
        rules={[{ required: true, max: 16 }]}
        fieldProps={{ maxLength: 16 }}
      />

      <ProFormDigit
        disabled={ isEdit }
        name="decimalPlaces"
        label={t('currency.fields.decimalPlaces')}
        rules={[{ required: true }]}
        min={0}
        max={4}
      />

      <ProFormDigit
        disabled={ isEdit && !rateEditable }
        name="usdRate"
        label={t('currency.fields.usdRate')}
        rules={[
          { required: true },
          {
            validator: (_, value) => {
              if (value === undefined || value === null || value === '') {
                return Promise.resolve();
              }
              // number 类型可能被序列化成科学计数法（如 1e-8），用 toFixed 还原成普通小数
              const str = typeof value === 'number' ? value.toFixed(8) : String(value);
              if (!/^\d{1,12}(\.\d{1,8})?$/.test(str)) {
                return Promise.reject(new Error(t('currency.validation.usdRate')));
              }
              if (Number(value) <= 0) {
                return Promise.reject(new Error(t('currency.validation.usdRate')));
              }
              return Promise.resolve();
            },
          },
        ]}
        fieldProps={{ stringMode: true, placeholder: t('currency.fields.usdRate.placeholder') }}
      />
    </BaseModalForm>
  );
}
