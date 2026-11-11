import {
  CheckCircleFilled,
  CloseCircleFilled,
  PlusOutlined,
} from '@ant-design/icons';
import { useRequest } from '@umijs/max';
import {
  App,
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Statistic,
  Tag,
  TreeSelect,
  theme,
} from 'antd';
import dayjs from 'dayjs';
import { useRef, useState } from 'react';

import { accountApi } from '@/api/account';
import { bookApi } from '@/api/book';
import { categoryApi } from '@/api/category';
import { moneyFlowApi } from '@/api/moneyFlow';
import { payeeApi } from '@/api/payee';
import { tagApi } from '@/api/tag';
import BaseProTable from '@/components/BaseProTable';
import { useLazyDropdown } from '@/hooks/useLazyDropdown';
import { useModalForm } from '@/hooks/useModalForm';
import { toCategoryTreeData } from '@/utils/categoryTree';
import { useT } from '@/utils/i18n';
import { formatAmountDisplay, formatMoney } from '@/utils/money';
import { useTableStyles } from '@/utils/tableStyles';

import ActionForm from './ActionForm';
import {
  buildTypeOptions,
  getTypeLabelKey,
  TYPE_ADJUST,
  TYPE_EXPENSE,
  TYPE_INCOME,
  TYPE_TRANSFER,
} from './constants';

// 账户下拉数据走 /accounts/options（全量、轻量），与 currency 下拉同模式
const TYPE_COLOR = {
  [TYPE_EXPENSE]: 'error',
  [TYPE_INCOME]: 'success',
  [TYPE_TRANSFER]: 'processing',
};

// 标签后端无 color 字段，用 antd 预设色按 id 稳定分配：同一标签颜色一致、并列标签尽量错开
const TAG_COLORS = [
  'blue',
  'cyan',
  'geekblue',
  'purple',
  'gold',
  'orange',
  'green',
  'lime',
  'volcano',
  'magenta',
];

/** 搜索表单原始值 → 后端 MoneyFlowQueryForm（过滤掉空值）。 */
function buildQuery(values = {}) {
  const query = {};
  const {
    title,
    notes,
    type,
    minAmount,
    maxAmount,
    accountId,
    categoryIds,
    tagIds,
    payeeIds,
    confirm,
    include,
    minTime,
    maxTime,
    bookId,
  } = values || {};

  if (title) query.title = title;
  if (notes) query.notes = notes;
  if (type !== undefined && type !== null) query.type = type;
  if (bookId !== undefined && bookId !== null) query.bookId = bookId;
  if (minAmount !== undefined && minAmount !== null)
    query.minAmount = minAmount;
  if (maxAmount !== undefined && maxAmount !== null)
    query.maxAmount = maxAmount;
  if (accountId !== undefined && accountId !== null)
    query.accountId = accountId;
  if (categoryIds && categoryIds.length)
    query.categoryIds = categoryIds.join(',');
  if (tagIds && tagIds.length) query.tagIds = tagIds.join(',');
  if (payeeIds && payeeIds.length) query.payeeIds = payeeIds.join(',');
  if (confirm !== undefined && confirm !== null) query.confirm = confirm;
  if (include !== undefined && include !== null) query.include = include;
  if (minTime) query.minTime = minTime.startOf('day').valueOf();
  if (maxTime) query.maxTime = maxTime.endOf('day').valueOf();
  return query;
}

export default () => {
  const t = useT();
  const { styles } = useTableStyles();
  const { token } = theme.useToken();
  const { message, modal } = App.useApp();
  const [form] = Form.useForm();
  const actionRef = useRef(null);
  // 默认「计入统计」：统计条/列表默认排除 include=false 的流水，用户可清空 include 下拉查看全部
  const [query, setQuery] = useState({ include: true });

  const modalForm = useModalForm();

  // 分类树跟随「类型 + 账本」过滤条件：只有支出/收入有分类，且分类是账本级资源，必须先选账本
  const watchedType = Form.useWatch('type', form);
  const hasType = watchedType === TYPE_EXPENSE || watchedType === TYPE_INCOME;

  // 账本过滤：选了账本则分类/标签/交易对象切换到该账本；
  // 不选账本 = 跨账本搜索，但分类/标签/交易对象是账本级资源（ID 离开账本无意义），未选账本时禁用
  const watchedBook = Form.useWatch('bookId', form);
  const hasBook = watchedBook !== undefined && watchedBook !== null;

  // 下拉数据全部懒加载：点开对应下拉才请求（对齐 old 版），首屏只有列表+统计两个请求。
  // 依赖（账本/类型）变化后再次打开下拉会重新拉取，避免串数据
  const {
    data: bookPage,
    loading: booksLoading,
    onOpenChange: onBooksOpen,
  } = useLazyDropdown(() => bookApi.list({ page: 0, size: 1000 }));
  const {
    data: tagPage,
    loading: tagsLoading,
    onOpenChange: onTagsOpen,
  } = useLazyDropdown(
    () =>
      tagApi.list({
        page: 0,
        size: 1000,
        ...(watchedBook != null ? { bookId: watchedBook } : {}),
      }),
    [watchedBook],
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
        ...(watchedBook != null ? { bookId: watchedBook } : {}),
      }),
    [watchedBook],
  );
  const {
    data: accountOptionsRaw,
    loading: accountsLoading,
    onOpenChange: onAccountsOpen,
  } = useLazyDropdown(() => accountApi.options());
  const {
    data: categoryTreeData,
    loading: categoriesLoading,
    onOpenChange: onCategoriesOpen,
  } = useLazyDropdown(
    () =>
      hasType
        ? categoryApi.tree(watchedType, watchedBook).then(toCategoryTreeData)
        : Promise.resolve([]),
    [hasType, watchedType, watchedBook],
  );

  // 统计条与列表同条件（后端忽略类型），condition 变化时自动刷新
  const { data: summary, refresh: refreshSummary } = useRequest(
    () => moneyFlowApi.statistics(query),
    { refreshDeps: [query] },
  );

  const bookOptions = (bookPage?.content || []).map((item) => ({
    label: item.name,
    value: item.id,
  }));
  const tagOptions = (tagPage?.content || []).map((item) => ({
    label: item.name,
    value: item.id,
  }));
  const payeeOptions = (payeePage?.content || []).map((item) => ({
    label: item.name,
    value: item.id,
  }));
  const accountOptions = (accountOptionsRaw || []).map((item) => ({
    label: item.label,
    value: item.value,
  }));
  const typeOptions = buildTypeOptions(t);
  const booleanOptions = [
    { label: t('moneyFlow.boolean.true'), value: true },
    { label: t('moneyFlow.boolean.false'), value: false },
  ];

  const handleSearch = () => {
    setQuery(buildQuery(form.getFieldsValue(true)));
    actionRef.current?.reload();
  };

  const handleReset = () => {
    form.resetFields();
    setQuery({ include: true });
    actionRef.current?.reload();
  };

  const handleTypeChange = () => {
    // 分类依赖类型，切类型时清掉已选分类，避免串上另一类型的分支
    form.setFieldValue('categoryIds', undefined);
  };

  // 账本变化会改变分类/标签/交易对象候选（含清空账本 → 三个控件转为禁用），清掉已选值避免串账本
  const handleBookChange = () => {
    form.setFieldValue('categoryIds', undefined);
    form.setFieldValue('tagIds', undefined);
    form.setFieldValue('payeeIds', undefined);
  };

  const updateHandler = (record) => {
    modalForm.showUpdate(record);
  };

  // 删除：二次确认后调接口；失败提示由 request errorHandler 统一处理（确认框保持打开）
  const deleteHandler = (record) => {
    modal.confirm({
      title: t('moneyFlow.delete.confirm', {
        title: record.title || t(getTypeLabelKey(record.type)),
      }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await moneyFlowApi.delete(record.id);
        message.success(t('operation.success'));
        actionRef.current?.reload();
        refreshSummary();
      },
    });
  };

  // 新增/编辑成功后刷新列表与统计条
  const handleFinish = () => {
    message.success(t('operation.success'));
    actionRef.current?.reload();
    refreshSummary();
  };

  const renderType = (value) => (
    <Tag color={TYPE_COLOR[value]}>{t(getTypeLabelKey(value))}</Tag>
  );

  const renderAmount = (_, record) => {
    const original = formatMoney(
      record.amount,
      record.amountCurrencyDecimalPlaces,
      record.amountCurrencySymbol,
    );
    if (!original) return '-';
    if (!record.needConvert) return original;
    const converted = formatMoney(
      record.convertedAmount,
      record.convertedCurrencyDecimalPlaces,
      record.convertedCurrencySymbol,
    );
    return (
      <span>
        {original}
        <span
          style={{
            color: token.colorTextSecondary,
            marginLeft: 6,
            fontSize: 12,
          }}
        >
          ≈ {converted}
        </span>
      </span>
    );
  };

  const renderAccount = (_, record) => {
    if (record.type === TYPE_TRANSFER && record.toAccountName) {
      return `${record.accountName || '-'} → ${record.toAccountName}`;
    }
    return record.accountName || '-';
  };

  const renderTags = (_, record) => {
    const tags = record.tags || [];
    if (!tags.length) return '-';
    return (
      <Space size={4} wrap>
        {tags.map((tag) => (
          <Tag key={tag.id} color={TAG_COLORS[tag.id % TAG_COLORS.length]}>
            {tag.name}
          </Tag>
        ))}
      </Space>
    );
  };

  const renderTime = (value) =>
    value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-';

  // 布尔列：仅左侧图标（绿勾=是 / 红叉=否），参考旧版 valueEnum（Success/Error 状态色）
  const renderBoolean = (value) =>
    value ? (
      <CheckCircleFilled style={{ color: token.colorSuccess }} />
    ) : (
      <CloseCircleFilled style={{ color: token.colorError }} />
    );

  // 列在每次 render 直接重建（闭包引用 t/token，避免 stale 闭包，与 account 样板一致）
  const columns = [
    {
      title: t('moneyFlow.fields.book'),
      dataIndex: 'bookName',
      ellipsis: true,
      width: 120,
    },
    { title: t('moneyFlow.fields.title'), dataIndex: 'title', ellipsis: true },
    {
      title: t('moneyFlow.fields.type'),
      dataIndex: 'type',
      render: renderType,
      width: 90,
      align: 'center',
    },
    {
      title: t('moneyFlow.fields.amount'),
      dataIndex: 'amount',
      render: renderAmount,
      sorter: true,
      align: 'right',
      className: styles.numericCell,
    },
    {
      title: t('moneyFlow.fields.createTime'),
      dataIndex: 'createTime',
      render: renderTime,
      sorter: true,
      width: 160,
    },
    {
      title: t('moneyFlow.fields.account'),
      dataIndex: 'accountName',
      render: renderAccount,
      ellipsis: true,
    },
    {
      title: t('moneyFlow.fields.category'),
      dataIndex: 'categoryName',
      ellipsis: true,
    },
    {
      title: t('moneyFlow.fields.tags'),
      dataIndex: 'tags',
      render: renderTags,
    },
    {
      title: t('moneyFlow.fields.payee'),
      dataIndex: 'payeeName',
      ellipsis: true,
    },
    {
      title: t('moneyFlow.fields.confirm'),
      dataIndex: 'confirm',
      render: renderBoolean,
      width: 90,
      align: 'center',
    },
    {
      title: t('moneyFlow.fields.include'),
      dataIndex: 'include',
      render: renderBoolean,
      width: 90,
      align: 'center',
    },
    {
      title: t('operation'),
      align: 'center',
      width: 200,
      render: (_, record) => [
        <Button
          key="update"
          size="small"
          type="link"
          onClick={() => updateHandler(record)}
        >
          {t('update')}
        </Button>,
        <Button
          key="copy"
          size="small"
          type="link"
          disabled={record.type === TYPE_ADJUST}
          onClick={() => modalForm.showCopy(record)}
        >
          {t('copy')}
        </Button>,
        <Button
          key="delete"
          size="small"
          type="link"
          danger
          onClick={() => deleteHandler(record)}
        >
          {t('delete')}
        </Button>,
      ],
    },
  ];

  const fieldStyle = { width: '100%' };

  return (
    <>
      <Card
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '16px 24px' } }}
      >
        <Form form={form} name="moneyFlowSearch">
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="bookId" label={t('moneyFlow.fields.book')}>
                <Select
                  allowClear
                  showSearch
                  optionFilterProp="label"
                  placeholder={t('moneyFlow.boolean.all')}
                  options={bookOptions}
                  loading={booksLoading}
                  onOpenChange={onBooksOpen}
                  onChange={handleBookChange}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="type" label={t('moneyFlow.fields.type')}>
                <Select
                  allowClear
                  placeholder={t('moneyFlow.boolean.all')}
                  options={typeOptions}
                  onChange={handleTypeChange}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="title" label={t('moneyFlow.fields.title')}>
                <Input
                  allowClear
                  placeholder={t('moneyFlow.fields.title')}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="notes" label={t('notes')}>
                <Input allowClear placeholder={t('notes')} style={fieldStyle} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="minTime" label={t('moneyFlow.fields.minTime')}>
                <DatePicker allowClear style={fieldStyle} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="maxTime" label={t('moneyFlow.fields.maxTime')}>
                <DatePicker allowClear style={fieldStyle} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name="minAmount"
                label={t('moneyFlow.fields.minAmount')}
              >
                <InputNumber
                  min={0}
                  placeholder={t('moneyFlow.fields.minAmount')}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name="maxAmount"
                label={t('moneyFlow.fields.maxAmount')}
              >
                <InputNumber
                  min={0}
                  placeholder={t('moneyFlow.fields.maxAmount')}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="accountId" label={t('moneyFlow.fields.account')}>
                <Select
                  allowClear
                  showSearch
                  optionFilterProp="label"
                  placeholder={t('moneyFlow.boolean.all')}
                  options={accountOptions}
                  loading={accountsLoading}
                  onOpenChange={onAccountsOpen}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name="categoryIds"
                label={t('moneyFlow.fields.category')}
              >
                <TreeSelect
                  treeData={categoryTreeData}
                  treeCheckable
                  // showCheckedStrategy 默认 SHOW_CHILD：勾父节点 = 传全部叶子 id，正好对应后端「含子节点」语义
                  allowClear
                  showSearch
                  treeNodeFilterProp="title"
                  disabled={!hasType || !hasBook}
                  loading={categoriesLoading}
                  onOpenChange={onCategoriesOpen}
                  placeholder={
                    hasType && hasBook
                      ? t('moneyFlow.boolean.all')
                      : !hasBook
                        ? t('moneyFlow.search.need.book')
                        : t('moneyFlow.category.need.type')
                  }
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="tagIds" label={t('moneyFlow.fields.tags')}>
                <Select
                  mode="multiple"
                  allowClear
                  showSearch
                  optionFilterProp="label"
                  placeholder={
                    hasBook
                      ? t('moneyFlow.boolean.all')
                      : t('moneyFlow.search.need.book')
                  }
                  options={tagOptions}
                  disabled={!hasBook}
                  loading={tagsLoading}
                  onOpenChange={onTagsOpen}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="payeeIds" label={t('moneyFlow.fields.payee')}>
                <Select
                  mode="multiple"
                  allowClear
                  showSearch
                  optionFilterProp="label"
                  placeholder={
                    hasBook
                      ? t('moneyFlow.boolean.all')
                      : t('moneyFlow.search.need.book')
                  }
                  options={payeeOptions}
                  disabled={!hasBook}
                  loading={payeesLoading}
                  onOpenChange={onPayeesOpen}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="confirm" label={t('moneyFlow.fields.confirm')}>
                <Select
                  allowClear
                  placeholder={t('moneyFlow.boolean.all')}
                  options={booleanOptions}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name="include"
                label={t('moneyFlow.fields.include')}
                initialValue={true}
              >
                <Select
                  allowClear
                  placeholder={t('moneyFlow.boolean.all')}
                  options={booleanOptions}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row>
            <Col span={24} style={{ textAlign: 'right' }}>
              <Button
                type="primary"
                onClick={handleSearch}
                style={{ marginRight: 8 }}
              >
                {t('moneyFlow.search')}
              </Button>
              <Button onClick={handleReset}>{t('moneyFlow.reset')}</Button>
            </Col>
          </Row>
        </Form>
      </Card>

      <Card
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '12px 24px' } }}
      >
        <div style={{ display: 'flex', gap: 32, flexWrap: 'wrap' }}>
          <Statistic
            title={`${t('moneyFlow.summary.expense')}${summary?.currencyCode ? `(${summary.currencyCode})` : ''}`}
            prefix={summary?.currencySymbol}
            value={formatAmountDisplay(
              summary?.expense,
              summary?.currencyDecimalPlaces,
            )}
            valueStyle={{ color: token.colorError }}
          />
          <Statistic
            title={`${t('moneyFlow.summary.income')}${summary?.currencyCode ? `(${summary.currencyCode})` : ''}`}
            prefix={summary?.currencySymbol}
            value={formatAmountDisplay(
              summary?.income,
              summary?.currencyDecimalPlaces,
            )}
            valueStyle={{ color: token.colorSuccess }}
          />
          <Statistic
            title={`${t('moneyFlow.summary.surplus')}${summary?.currencyCode ? `(${summary.currencyCode})` : ''}`}
            prefix={summary?.currencySymbol}
            value={formatAmountDisplay(
              summary?.surplus,
              summary?.currencyDecimalPlaces,
            )}
            valueStyle={{
              color:
                (summary?.surplus ?? 0) < 0
                  ? token.colorError
                  : token.colorSuccess,
            }}
          />
        </div>
      </Card>

      <BaseProTable
        actionRef={actionRef}
        columns={columns}
        search={false}
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={() => modalForm.showAdd()}>
            <PlusOutlined />
            {t('add')}
          </Button>,
        ]}
        request={(params) => moneyFlowApi.list({ ...params, ...query })}
        pagination={{ pageSize: 10 }}
      />
      {/* 弹窗按需挂载：不开弹窗其内部的请求不会发出 */}
      {modalForm.open && (
        <ActionForm modal={modalForm} onFinish={handleFinish} />
      )}
    </>
  );
};
