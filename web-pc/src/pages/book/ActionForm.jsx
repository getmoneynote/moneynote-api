import {
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { useEffect } from 'react';
import { bookApi } from '@/api/book';
import BaseModalForm from '@/components/BaseModalForm';
import { useCurrencyOptions } from '@/hooks/useCurrencyOptions';
import { useT } from '@/utils/i18n';

export default function ActionForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, isEdit, onOpenChange } = modal;
  // 新增默认币种 = 用户本位币（与 account 表单约定一致）；会话态由 getInitialState 全局下发
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;
  const {
    options: currencyOptions,
    loading: currencyLoading,
    ensureLoaded: ensureCurrencyOptions,
    refresh: refreshCurrencyOptions,
  } = useCurrencyOptions();
  // 弹窗打开时才拉币种字典（进页面不发请求）；缓存已在别处加载则直接复用
  useEffect(() => {
    if (open) ensureCurrencyOptions();
  }, [open, ensureCurrencyOptions]);

  return (
    <BaseModalForm
      title={isEdit ? t('book.action.edit') : t('book.action.add')}
      open={open}
      onOpenChange={onOpenChange}
      initialValues={
        isEdit ? currentRow : { currencyId: currentUser?.baseCurrencyId }
      }
      onFinish={async (values) => {
        if (isEdit) {
          await bookApi.update(currentRow.id, {
            name: values.name,
            notes: values.notes,
          });
        } else {
          await bookApi.create(values);
        }
        onFinish();
        return true;
      }}
    >
      <ProFormText
        name="name"
        label={t('book.fields.name')}
        rules={[{ required: true }]}
      />

      <ProFormSelect
        name="currencyId"
        disabled={isEdit}
        label={t('book.fields.currencyName')}
        rules={[{ required: true }]}
        options={currencyOptions}
        loading={currencyLoading}
        fieldProps={{
          // 每次展开下拉重新拉取，保证拿到最新币种列表
          onOpenChange: (nextOpen) => nextOpen && refreshCurrencyOptions(),
        }}
      />

      <ProFormTextArea
        name="notes"
        label={t('notes')}
        fieldProps={{ rows: 3, maxLength: 1024, showCount: true }}
      />
    </BaseModalForm>
  );
}
