import {
  ProFormDigit,
  ProFormSelect,
  ProFormSwitch,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Col, Form, Row } from 'antd';
import { useEffect, useMemo } from 'react';

import { accountApi } from '@/api/account';
import BaseModalForm from '@/components/BaseModalForm';
import { useCurrencyOptions } from '@/hooks/useCurrencyOptions';
import { useT } from '@/utils/i18n';
import { displayToMinor, minorToDisplay } from '@/utils/money';

import { buildTypeOptions, isCreditOrLoan, TYPE_LOAN } from './constants';

export default function ActionForm({ modal, defaultType, onFinish }) {
  const t = useT();
  const { open, currentRow, isEdit, onOpenChange } = modal;
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
  // 本位币等用户会话态全局唯一来源：app 启动时 getInitialState 拉取，
  // 变更接口（如 setBaseCurrency）的响应负责局部更新
  const { initialState } = useModel('@@initialState');
  const currentUser = initialState?.currentUser;

  const typeOptions = useMemo(() => buildTypeOptions(t), [t]);

  const [form] = Form.useForm();
  const typeValue = Form.useWatch('type', form);
  const currencyId = Form.useWatch('currencyId', form);

  const decimalPlaces = useMemo(() => {
    if (!currencyId) return undefined;
    const opt = currencyOptions.find((o) => o.value === currencyId);
    if (opt) return opt.decimalPlaces;
    // options 未加载完成、且当前选中本位币时，用全局用户态已下发的 decimalPlaces 兜底，
    // 避免首屏 precision 跳变（本位币如 VND decimalPlaces=0）
    if (currentUser?.baseCurrencyId === currencyId) {
      return currentUser.baseCurrencyDecimalPlaces;
    }
    // 解析不出精度（options 未加载/币种配置异常）时不猜测：precision 暂不受限，
    // 提交换算时 money.js 会对缺失精度抛错，禁止静默按 2 位小数写库
    return undefined;
  }, [currencyOptions, currencyId, currentUser]);

  const showCreditLimit = isCreditOrLoan(typeValue);
  const showBillDay = isCreditOrLoan(typeValue);
  const showApr = typeValue === TYPE_LOAN;

  // 后端 update 对 null 字段跳过赋值（partial update），这些数值字段清空提交会"保存无效"、
  // 重新打开又出现旧值。约定：已填写的字段不允许清空，只能改值——用表单校验显式拦截。
  const clearableRules = (original) =>
    original !== undefined && original !== null && original !== ''
      ? [{ required: true, message: t('account.validation.notClearable') }]
      : [];

  // 编辑：弹窗打开时按当前行回填。编辑态 currencyId 禁改、decimalPlaces 稳定，进依赖无「切换回退」风险
  useEffect(() => {
    if (!open || !isEdit || !currentRow) return;

    // 列表接口保证 currencyDecimalPlaces 非空（列非空 + 创建强校验），缺失属契约破坏，
    // 直接取用交由 money.js 校验抛错，不做默认兜底
    const dp = currentRow.currencyDecimalPlaces;
    form.setFieldsValue({
      name: currentRow.name,
      type: currentRow.type,
      currencyId: currentRow.currencyId,
      balance: minorToDisplay(currentRow.balance, dp),
      no: currentRow.no,
      include: currentRow.include,
      canExpense: currentRow.canExpense,
      canIncome: currentRow.canIncome,
      canTransferFrom: currentRow.canTransferFrom,
      canTransferTo: currentRow.canTransferTo,
      creditLimit: minorToDisplay(currentRow.creditLimit, dp),
      billDay: currentRow.billDay,
      apr: currentRow.apr,
      displayOrder: currentRow.displayOrder,
      notes: currentRow.notes,
    });
  }, [open, isEdit, currentRow, form]);

  // 新增：弹窗打开时重置并填默认值。不能依赖 decimalPlaces——它随所选币种变化，仅当切到「精度不同」的币种时
  // 数值 2→0 才变、才触发本 effect 重跑，把 currencyId 覆盖回本位币（精度相同时数值不变、不触发，故此前难以复现）。
  useEffect(() => {
    if (!open || isEdit) return;

    form.resetFields();
    form.setFieldsValue({
      type: defaultType,
      currencyId: currentUser?.baseCurrencyId,
      include: true,
      canExpense: true,
      canIncome: true,
      canTransferFrom: true,
      canTransferTo: true,
    });
  }, [open, isEdit, form, defaultType, currentUser]);

  const handleFinish = async (values) => {
    const balanceMinor = displayToMinor(values.balance, decimalPlaces);
    const creditLimitMinor = displayToMinor(values.creditLimit, decimalPlaces);

    if (isEdit) {
      await accountApi.update(currentRow.id, {
        name: values.name,
        include: values.include,
        canExpense: values.canExpense,
        canIncome: values.canIncome,
        canTransferFrom: values.canTransferFrom,
        canTransferTo: values.canTransferTo,
        no: values.no,
        creditLimit: creditLimitMinor,
        billDay: values.billDay,
        apr: values.apr,
        displayOrder: values.displayOrder,
        notes: values.notes,
      });
    } else {
      await accountApi.create({
        name: values.name,
        type: values.type,
        currencyId: values.currencyId,
        balance: balanceMinor,
        include: values.include,
        canExpense: values.canExpense,
        canIncome: values.canIncome,
        canTransferFrom: values.canTransferFrom,
        canTransferTo: values.canTransferTo,
        no: values.no,
        creditLimit: creditLimitMinor,
        billDay: values.billDay,
        apr: values.apr,
        displayOrder: values.displayOrder,
        notes: values.notes,
      });
    }
    onFinish();
    return true;
  };

  return (
    <BaseModalForm
      form={form}
      title={isEdit ? t('account.action.edit') : t('account.action.add')}
      open={open}
      onOpenChange={onOpenChange}
      layout="horizontal"
      labelCol={{ span: 8 }}
      wrapperCol={{ span: 16 }}
      modalProps={{ width: 600 }}
      onFinish={handleFinish}
    >
      <ProFormText
        name="name"
        label={t('account.fields.name')}
        rules={[{ required: true }]}
      />

      <ProFormSelect
        name="type"
        label={t('account.fields.type')}
        disabled={isEdit}
        rules={[{ required: true }]}
        options={typeOptions}
      />

      <ProFormSelect
        name="currencyId"
        label={t('account.fields.currencyName')}
        disabled={isEdit}
        rules={[{ required: true }]}
        options={currencyOptions}
        loading={currencyLoading}
        fieldProps={{
          onOpenChange: (nextOpen) => nextOpen && refreshCurrencyOptions(),
        }}
      />

      <ProFormDigit
        name="balance"
        label={t('account.fields.balance')}
        disabled={isEdit}
        min={-Infinity}
        rules={[{ required: true }]}
        fieldProps={{
          precision: decimalPlaces,
          formatter: (value) => {
            if (value === undefined || value === null || value === '')
              return '';
            const str = String(value);
            return str.replace(/\.0+$/, '').replace(/(\.\d*?[1-9])0+$/, '$1');
          },
        }}
      />

      <Row gutter={16}>
        <Col span={12}>
          <ProFormText name="no" label={t('account.fields.no')} />
        </Col>
        <Col span={12}>
          <ProFormSwitch name="include" label={t('account.fields.include')} />
        </Col>
      </Row>

      {(showCreditLimit || showBillDay) && (
        <Row gutter={16}>
          {showCreditLimit && (
            <Col span={12}>
              <ProFormDigit
                name="creditLimit"
                label={t('account.fields.creditLimit')}
                min={0}
                rules={clearableRules(currentRow?.creditLimit)}
                fieldProps={{ precision: decimalPlaces }}
              />
            </Col>
          )}
          {showBillDay && (
            <Col span={12}>
              <ProFormDigit
                name="billDay"
                label={t('account.fields.billDay')}
                min={1}
                max={31}
                rules={clearableRules(currentRow?.billDay)}
                fieldProps={{ precision: 0 }}
              />
            </Col>
          )}
        </Row>
      )}

      {showApr && (
        <ProFormDigit
          name="apr"
          label={t('account.fields.apr')}
          min={0}
          max={100}
          rules={clearableRules(currentRow?.apr)}
          fieldProps={{ precision: 4 }}
        />
      )}

      <Row gutter={16}>
        <Col span={12}>
          <ProFormSwitch
            name="canExpense"
            label={t('account.fields.canExpense')}
          />
        </Col>
        <Col span={12}>
          <ProFormSwitch
            name="canIncome"
            label={t('account.fields.canIncome')}
          />
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={12}>
          <ProFormSwitch
            name="canTransferFrom"
            label={t('account.fields.canTransferFrom')}
          />
        </Col>
        <Col span={12}>
          <ProFormSwitch
            name="canTransferTo"
            label={t('account.fields.canTransferTo')}
          />
        </Col>
      </Row>

      <ProFormTextArea
        name="notes"
        label={t('notes')}
        fieldProps={{ rows: 3, maxLength: 1024, showCount: true }}
      />

      <ProFormDigit
        name="displayOrder"
        label={t('account.fields.displayOrder')}
        min={0}
        rules={clearableRules(currentRow?.displayOrder)}
        fieldProps={{ precision: 0 }}
      />
    </BaseModalForm>
  );
}
