import { useRequest } from '@umijs/max';
import {
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Switch,
  Tabs,
  TreeSelect,
} from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo } from 'react';

import { accountApi } from '@/api/account';
import { bookApi } from '@/api/book';
import { categoryApi } from '@/api/category';
import { moneyFlowApi } from '@/api/moneyFlow';
import { payeeApi } from '@/api/payee';
import { tagApi } from '@/api/tag';
import BaseModalForm from '@/components/BaseModalForm';
import { useCurrencyOptions } from '@/hooks/useCurrencyOptions';
import { useLazyDropdown } from '@/hooks/useLazyDropdown';
import { useT } from '@/utils/i18n';
import { displayToMinor, minorToDisplay } from '@/utils/money';

import {
  getTypeLabelKey,
  TYPE_EXPENSE,
  TYPE_INCOME,
  TYPE_TRANSFER,
} from './constants';

// 新增/编辑只暴露三种手动流水类型；余额调整（TYPE_ADJUST）走账户模块的 adjust 端点，不在此创建
const FORM_TYPES = [TYPE_EXPENSE, TYPE_INCOME, TYPE_TRANSFER];

// 新增时「账户」下拉的过滤参数按流水类型收敛：支出限可支出、收入限可收入、转账转出限可转出。
// 编辑/复制（有回填值）不带参数返回全量，避免历史流水的账户已被关掉用途开关时下拉回填失效。
const ACCOUNT_OPTIONS_PARAMS = {
  [TYPE_EXPENSE]: { canExpense: true },
  [TYPE_INCOME]: { canIncome: true },
  [TYPE_TRANSFER]: { canTransferFrom: true },
};

function toAccountOption(a) {
  return {
    label: a.label,
    value: a.value,
    currencyId: a.currencyId,
    currencyDecimalPlaces: a.currencyDecimalPlaces,
  };
}

function toCategoryTreeData(nodes) {
  return (nodes || []).map((n) => ({
    title: n.name,
    value: n.id,
    children:
      n.children && n.children.length
        ? toCategoryTreeData(n.children)
        : undefined,
  }));
}

export default function ActionForm({ modal, onFinish }) {
  const t = useT();
  const { open, currentRow, isEdit, isCopy, onOpenChange } = modal;
  const [form] = Form.useForm();
  const typeValue = Form.useWatch('type', form);
  const accountId = Form.useWatch('accountId', form);
  const toAccountId = Form.useWatch('toAccountId', form);
  const bookId = Form.useWatch('bookId', form);

  // 编辑/复制（有回填值）不过滤：历史流水的账户可能已被关掉用途开关，过滤会导致回填失效
  const prefilled = isEdit || isCopy;

  // 账户/账本在弹窗打开时加载：编辑回填和金额币种换算都依赖它们，无法再做懒加载；
  // 「账户」下拉按流水类型带 canXxx 过滤（新增态），切类型时重新请求；
  // 币种 options 另走全局缓存（见下方 useCurrencyOptions）
  const { data: accountList } = useRequest(
    () =>
      accountApi.options(
        prefilled ? {} : ACCOUNT_OPTIONS_PARAMS[typeValue] || {},
      ),
    { refreshDeps: [prefilled, typeValue] },
  );
  // 转账「转入账户」用独立的过滤参数（canTransferTo），只在新增转账时按需拉取；
  // 编辑/复制态复用全量 accountList（见下方 toAccountOptions）。
  // 注意不能引用下方才声明的 isTransfer，deps 在渲染期求值会踩 TDZ
  const isTransferType = typeValue === TYPE_TRANSFER;
  const { data: toAccountList, run: runToAccountOptions } = useRequest(
    () => accountApi.options({ canTransferTo: true }),
    { manual: true },
  );
  useEffect(() => {
    if (open && isTransferType && !prefilled) {
      runToAccountOptions();
    }
  }, [open, isTransferType, prefilled, runToAccountOptions]);
  // 币种 options 走全局缓存：编辑回填和金额换算依赖它，随账户/账本一起在挂载时加载
  const { options: currencyOptions, ensureLoaded: ensureCurrencyOptions } =
    useCurrencyOptions();
  useEffect(() => {
    ensureCurrencyOptions();
  }, [ensureCurrencyOptions]);
  const { data: bookPage } = useRequest(() =>
    bookApi.list({ page: 0, size: 1000 }),
  );

  const isTransfer = typeValue === TYPE_TRANSFER;
  const isExpenseOrIncome =
    typeValue === TYPE_EXPENSE || typeValue === TYPE_INCOME;

  // 归属账本：新增默认当前账本；切换账本后分类/标签/交易对象随之下拉刷新
  const currentBookId = (bookPage?.content || []).find((b) => b.isCurrent)?.id;
  const effectiveBookId = bookId ?? currentBookId;

  // 标签/交易对象/分类懒加载：点开对应下拉才请求（对齐 old 版）
  const {
    data: tagPage,
    loading: tagsLoading,
    onOpenChange: onTagsOpen,
  } = useLazyDropdown(
    () =>
      tagApi.list({
        page: 0,
        size: 1000,
        ...(effectiveBookId != null ? { bookId: effectiveBookId } : {}),
      }),
    [effectiveBookId],
  );
  const {
    data: payeePage,
    loading: payeesLoading,
    onOpenChange: onPayeesOpen,
  } = useLazyDropdown(
    () =>
      payeeApi.list({
        page: 0,
        size: 1000,
        ...(effectiveBookId != null ? { bookId: effectiveBookId } : {}),
      }),
    [effectiveBookId],
  );

  const categoryEnabled = isExpenseOrIncome;
  const {
    data: categoryTreeData,
    loading: categoriesLoading,
    onOpenChange: onCategoriesOpen,
  } = useLazyDropdown(
    () =>
      categoryEnabled
        ? categoryApi.tree(typeValue, effectiveBookId).then(toCategoryTreeData)
        : Promise.resolve([]),
    [categoryEnabled, typeValue, effectiveBookId],
  );

  const accountOptions = useMemo(
    () => (accountList || []).map(toAccountOption),
    [accountList],
  );

  // 转入账户选项：新增转账用 canTransferTo 过滤的独立列表；编辑/复制态与「账户」一样用全量
  const toAccountOptions = useMemo(
    () =>
      prefilled ? accountOptions : (toAccountList || []).map(toAccountOption),
    [prefilled, accountOptions, toAccountList],
  );

  // 所选账本币种：按当前选中账本取 currencyId，再对照币种 options 拿 decimalPlaces
  const bookCurrency = useMemo(() => {
    const selected = (bookPage?.content || []).find(
      (b) => b.id === effectiveBookId,
    );
    const opt = currencyOptions.find((o) => o.value === selected?.currencyId);
    return opt
      ? { id: opt.value, code: opt.code, decimalPlaces: opt.decimalPlaces }
      : undefined;
  }, [bookPage, currencyOptions, effectiveBookId]);

  const bookOptions = useMemo(
    () =>
      (bookPage?.content || []).map((b) => ({ label: b.name, value: b.id })),
    [bookPage],
  );
  const tagOptions = useMemo(
    () => (tagPage?.content || []).map((i) => ({ label: i.name, value: i.id })),
    [tagPage],
  );
  const payeeOptions = useMemo(
    () =>
      (payeePage?.content || []).map((i) => ({ label: i.name, value: i.id })),
    [payeePage],
  );
  const tabItems = useMemo(
    () =>
      FORM_TYPES.map((type) => ({
        key: type,
        label: `${t('add')}${t(getTypeLabelKey(type))}`,
      })),
    [t],
  );

  // 编辑/复制弹窗标题按类型动态：修改支出/…、复制支出/…
  const editTitle =
    isEdit && currentRow
      ? t('moneyFlow.action.editByType', {
          type: t(getTypeLabelKey(currentRow.type)),
        })
      : null;
  const copyTitle =
    isCopy && currentRow
      ? t('moneyFlow.action.copyByType', {
          type: t(getTypeLabelKey(currentRow.type)),
        })
      : null;

  const accountOpt = accountOptions.find((o) => o.value === accountId);
  const toOpt = toAccountOptions.find((o) => o.value === toAccountId);

  // 金额所在币种：选了账户用账户币种，未选（未归属收/支）用账本币种
  const amountCurrencyId = accountOpt
    ? accountOpt.currencyId
    : bookCurrency?.id;
  // 精度解析不出来时不猜测兜底：提交换算时 money.js 会对缺失精度抛错，
  // 禁止静默按 2 位小数写库（零小数位币种会错 100 倍）
  const amountDP = accountOpt
    ? accountOpt.currencyDecimalPlaces
    : bookCurrency?.decimalPlaces;

  // 折算金额目标币种：转账为转入账户币种，收/支为账本币种
  const targetCurrencyId = isTransfer ? toOpt?.currencyId : bookCurrency?.id;
  const targetDP = isTransfer
    ? toOpt?.currencyDecimalPlaces
    : bookCurrency?.decimalPlaces;

  // label 上标注具体币种用 code（USD/CNY），全局唯一、无 symbol 多币种撞车
  const amountCurrencyCode =
    amountCurrencyId != null
      ? currencyOptions.find((o) => o.value === amountCurrencyId)?.code
      : undefined;
  const targetCurrencyCode =
    targetCurrencyId != null
      ? currencyOptions.find((o) => o.value === targetCurrencyId)?.code
      : undefined;

  const needConvert =
    amountCurrencyId != null &&
    targetCurrencyId != null &&
    amountCurrencyId !== targetCurrencyId;

  // 编辑/复制：弹窗打开时按当前行回填。编辑保留原时间/备注/确认/计入统计；
  // 复制仅在编辑基础上重置时间为现在、清空备注、置为「已确认 + 计入统计」（对齐 old 版复制的约定）。
  useEffect(() => {
    if (!open || (!isEdit && !isCopy) || !currentRow) return;

    const accountOptRow = accountOptions.find(
      (o) => o.value === currentRow.accountId,
    );
    const toOptRow = accountOptions.find(
      (o) => o.value === currentRow.toAccountId,
    );
    const rowAmountCurrencyId = accountOptRow?.currencyId ?? bookCurrency?.id;
    const rowTargetCurrencyId =
      currentRow.type === TYPE_TRANSFER
        ? toOptRow?.currencyId
        : bookCurrency?.id;
    const rowNeedConvert =
      rowAmountCurrencyId != null &&
      rowTargetCurrencyId != null &&
      rowAmountCurrencyId !== rowTargetCurrencyId;

    // 金额精度：列表项已带 amountCurrencyDecimalPlaces（账户币种，未归属时为账本币种），直接取用；
    // 均缺失时不猜测兜底，交由 money.js 校验抛错
    const amountDP2 =
      currentRow.amountCurrencyDecimalPlaces ??
      bookCurrency?.decimalPlaces;
    // 折算金额精度：convertedAmount 币种的小数位（转账=转入账户，收/支=账本），列表项已带
    const convertedDP2 =
      currentRow.convertedCurrencyDecimalPlaces ??
      bookCurrency?.decimalPlaces;

    form.setFieldsValue({
      bookId: currentRow.bookId ?? currentBookId,
      type: currentRow.type,
      title: currentRow.title,
      notes: isEdit ? currentRow.notes : undefined,
      createTime: isEdit
        ? currentRow.createTime
          ? dayjs(currentRow.createTime)
          : undefined
        : dayjs().startOf('minute'),
      accountId: currentRow.accountId,
      amount: minorToDisplay(currentRow.amount, amountDP2),
      convertedAmount: rowNeedConvert
        ? minorToDisplay(currentRow.convertedAmount, convertedDP2)
        : undefined,
      // 分类/交易对象/标签用 labelInValue（对齐 old 版）：值带 label，回显不依赖下拉 options 已加载
      categoryId:
        currentRow.categoryId != null
          ? { value: currentRow.categoryId, label: currentRow.categoryName }
          : undefined,
      tagIds: (currentRow.tags || []).map((tag) => ({
        value: tag.id,
        label: tag.name,
      })),
      payeeId:
        currentRow.payeeId != null
          ? { value: currentRow.payeeId, label: currentRow.payeeName }
          : undefined,
      toAccountId: currentRow.toAccountId,
      confirm: isEdit ? currentRow.confirm : true,
      include: isEdit ? currentRow.include : true,
    });
  }, [
    open,
    isEdit,
    isCopy,
    currentRow,
    form,
    accountOptions,
    bookCurrency,
    currentBookId,
  ]);

  // 新增：弹窗打开时重置并填默认值。不能依赖 bookCurrency/effectiveBookId——它们随所选账本变化，
  // 混进依赖会在「切换账本」时重跑 effect、把 bookId 覆盖回当前账本（正是切不动账本的 bug 根因）。
  useEffect(() => {
    if (!open || isEdit || isCopy) return;

    form.resetFields();
    form.setFieldsValue({
      bookId: currentBookId,
      type: TYPE_EXPENSE,
      confirm: true,
      include: true,
      createTime: dayjs().startOf('minute'),
    });
  }, [open, isEdit, isCopy, form, currentBookId]);

  // 类型/账户/转入账户变化会改变折算关系或关联字段，清掉失效的旧值
  // （交易对象仅收/支有，转账不展示，切类型一并清掉）
  const handleTypeChange = () => {
    form.setFieldValue('categoryId', undefined);
    form.setFieldValue('toAccountId', undefined);
    form.setFieldValue('payeeId', undefined);
    form.setFieldValue('convertedAmount', undefined);
  };
  const handleAccountChange = () =>
    form.setFieldValue('convertedAmount', undefined);
  const handleToAccountChange = () =>
    form.setFieldValue('convertedAmount', undefined);

  // 切换账本后，旧账本的分类/标签/交易对象失效，一并清空（账户为全用户维度，保留）
  const handleBookChange = () => {
    form.setFieldValue('categoryId', undefined);
    form.setFieldValue('tagIds', undefined);
    form.setFieldValue('payeeId', undefined);
    form.setFieldValue('convertedAmount', undefined);
  };

  // 新增模式下切换 tab 会改变类型，同步写入隐藏字段并清掉失效的关联值
  const handleTabChange = (value) => {
    form.setFieldValue('type', value);
    handleTypeChange();
  };

  const toAccountRules = [
    { required: true },
    {
      validator: (_, value) => {
        if (value != null && accountId != null && value === accountId) {
          return Promise.reject(
            new Error(t('moneyFlow.validation.to.account.same')),
          );
        }
        return Promise.resolve();
      },
    },
  ];

  const handleFinish = async (values) => {
    const amountMinor = displayToMinor(values.amount, amountDP);
    const convertedAmountMinor = needConvert
      ? displayToMinor(values.convertedAmount, targetDP)
      : undefined;

    const payload = {
      bookId: values.bookId,
      type: values.type,
      title: values.title,
      notes: values.notes,
      createTime: values.createTime ? values.createTime.valueOf() : undefined,
      accountId: values.accountId ?? null,
      amount: amountMinor,
      convertedAmount: convertedAmountMinor,
      categoryId: isExpenseOrIncome
        ? (values.categoryId?.value ?? null)
        : undefined,
      tagIds: (values.tagIds || []).map((tag) => tag.value),
      toAccountId: isTransfer ? values.toAccountId : undefined,
      // 转账没有交易对象，恒传 null；收/支取表单值（字段已隐藏时 handleTypeChange 已清空）
      payeeId: isTransfer ? null : (values.payeeId?.value ?? null),
      confirm: values.confirm,
      include: values.include,
    };

    if (isEdit) {
      await moneyFlowApi.update(currentRow.id, payload);
    } else {
      await moneyFlowApi.create(payload);
    }
    onFinish();
    return true;
  };

  return (
    <BaseModalForm
      form={form}
      title={editTitle ?? copyTitle}
      open={open}
      onOpenChange={onOpenChange}
      layout="horizontal"
      labelCol={{ span: 7 }}
      wrapperCol={{ span: 17 }}
      modalProps={{ width: 640 }}
      onFinish={handleFinish}
    >
      {/* 类型：新增用 tab 选择，编辑仅展示不可修改；真实值存隐藏字段供提交 */}
      <Form.Item name="type" hidden>
        <Input />
      </Form.Item>

      {!isEdit && !isCopy && (
        <Tabs
          activeKey={typeValue ?? TYPE_EXPENSE}
          onChange={handleTabChange}
          items={tabItems}
          style={{ marginBottom: 8 }}
        />
      )}

      <Form.Item name="bookId" label={t('moneyFlow.fields.book')}>
        <Select
          showSearch
          optionFilterProp="label"
          options={bookOptions}
          disabled={isEdit || isCopy}
          onChange={handleBookChange}
        />
      </Form.Item>

      <Form.Item name="title" label={t('moneyFlow.fields.title')}>
        <Input maxLength={32} />
      </Form.Item>

      <Form.Item
        name="createTime"
        label={t('moneyFlow.fields.createTime')}
        rules={[{ required: true }]}
      >
        <DatePicker
          showTime={{ format: 'HH:mm' }}
          format="YYYY-MM-DD HH:mm"
          style={{ width: '100%' }}
        />
      </Form.Item>

      <Form.Item
        name="accountId"
        label={t('moneyFlow.fields.account')}
        rules={isTransfer ? [{ required: true }] : undefined}
      >
        <Select
          allowClear
          showSearch
          optionFilterProp="label"
          options={accountOptions}
          onChange={handleAccountChange}
        />
      </Form.Item>

      {isTransfer && (
        <Form.Item
          name="toAccountId"
          label={t('moneyFlow.fields.toAccount')}
          rules={toAccountRules}
        >
          <Select
            allowClear
            showSearch
            optionFilterProp="label"
            options={toAccountOptions}
            onChange={handleToAccountChange}
          />
        </Form.Item>
      )}

      {/* 支出/收入允许负数（负数 = 退款记录，余额/统计按符号自然回冲）；转账金额恒非负 */}
      <Form.Item
        name="amount"
        label={
          amountCurrencyCode
            ? t('moneyFlow.fields.amountByCurrency', {
                code: amountCurrencyCode,
              })
            : t('moneyFlow.fields.amount')
        }
        rules={[{ required: true }]}
      >
        <InputNumber
          min={isTransfer ? 0 : undefined}
          precision={amountDP}
          style={{ width: '100%' }}
        />
      </Form.Item>

      {needConvert && (
        <Form.Item
          name="convertedAmount"
          label={
            targetCurrencyCode
              ? t('moneyFlow.fields.convertedAmountByCurrency', {
                  code: targetCurrencyCode,
                })
              : t('moneyFlow.fields.convertedAmount')
          }
          rules={[{ required: true }]}
        >
          <InputNumber
            min={isTransfer ? 0 : undefined}
            precision={targetDP}
            style={{ width: '100%' }}
          />
        </Form.Item>
      )}

      {isExpenseOrIncome && (
        <Form.Item
          name="categoryId"
          label={t('moneyFlow.fields.category')}
          rules={[{ required: true }]}
        >
          <TreeSelect
            treeData={categoryTreeData}
            allowClear
            showSearch
            treeNodeFilterProp="title"
            labelInValue
            loading={categoriesLoading}
            onOpenChange={onCategoriesOpen}
            style={{ width: '100%' }}
          />
        </Form.Item>
      )}

      {/* 交易对象仅收/支有，转账不展示（对齐 old 版 tabKey !== 'TRANSFER'） */}
      {!isTransfer && (
        <Form.Item name="payeeId" label={t('moneyFlow.fields.payee')}>
          <Select
            allowClear
            showSearch
            optionFilterProp="label"
            labelInValue
            options={payeeOptions}
            loading={payeesLoading}
            onOpenChange={onPayeesOpen}
          />
        </Form.Item>
      )}

      <Form.Item name="tagIds" label={t('moneyFlow.fields.tags')}>
        <Select
          mode="multiple"
          allowClear
          showSearch
          optionFilterProp="label"
          labelInValue
          options={tagOptions}
          loading={tagsLoading}
          onOpenChange={onTagsOpen}
        />
      </Form.Item>

      <Row gutter={16}>
        <Col span={12}>
          <Form.Item
            name="confirm"
            label={t('moneyFlow.fields.confirm')}
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>
        </Col>
        <Col span={12}>
          <Form.Item
            name="include"
            label={t('moneyFlow.fields.include')}
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>
        </Col>
      </Row>

      <Form.Item name="notes" label={t('notes')}>
        <Input.TextArea rows={3} maxLength={1024} showCount />
      </Form.Item>
    </BaseModalForm>
  );
}
