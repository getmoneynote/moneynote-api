import { useRequest } from '@umijs/max';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Form,
  Input,
  Row,
  Select,
  TreeSelect,
} from 'antd';
import { useEffect, useState } from 'react';

import { accountApi } from '@/api/account';
import { bookApi } from '@/api/book';
import { categoryApi } from '@/api/category';
import { moneyFlowApi } from '@/api/moneyFlow';
import { payeeApi } from '@/api/payee';
import { tagApi } from '@/api/tag';
import CardPie from '@/components/CardPie';
import { useLazyDropdown } from '@/hooks/useLazyDropdown';
import { toCategoryTreeData } from '@/utils/categoryTree';
import { radioValueToTimeRange, TIME_RANGE } from '@/utils/date';
import { useT } from '@/utils/i18n';

/** 流水类型 → 卡片标题的 i18n key（复用菜单文案）。 */
const TYPE_TITLE_KEY = {
  1: 'menu.report.expense.category',
  2: 'menu.report.income.category',
};

/** 搜索表单原始值 → 后端 MoneyFlowQueryForm（过滤掉空值）。 */
function buildQuery(values = {}) {
  // 统计口径与流水页统计条一致：只统计「计入统计」的流水
  const query = { include: true };
  const { bookId, timeRange, title, accountId, categoryIds, tagIds, payeeIds } =
    values || {};

  if (bookId !== undefined && bookId !== null) query.bookId = bookId;
  if (title) query.title = title;
  if (accountId !== undefined && accountId !== null)
    query.accountId = accountId;
  if (categoryIds && categoryIds.length)
    query.categoryIds = categoryIds.join(',');
  if (tagIds && tagIds.length) query.tagIds = tagIds.join(',');
  if (payeeIds && payeeIds.length) query.payeeIds = payeeIds.join(',');
  if (timeRange?.[0]) query.minTime = timeRange[0].startOf('day').valueOf();
  if (timeRange?.[1]) query.maxTime = timeRange[1].endOf('day').valueOf();
  return query;
}

/**
 * 支出/收入分类统计页的共享实现（对齐 old 的 FlowFilter + Chart）：
 * 上方为搜索区域（账本/时间段/标题/账户/分类/标签/交易对象），下方为分类环形图。
 * 数据复用 /money-flows/category-statistics，type 区分支出/收入，不另设接口。
 * 所属账本默认选中当前账本（isCurrent），仍可清除（查全部账本）或改选其它账本。
 *
 * @param {number} type - 流水类型：1=支出 2=收入（与后端 MoneyFlow.type 一致）
 */
const CategoryReport = ({ type }) => {
  const t = useT();
  const [form] = Form.useForm();
  // null = 尚未按当前账本初始化（图表此时不发请求）；初始化后为 buildQuery 的结果
  const [query, setQuery] = useState(null);

  // 分类/标签/交易对象是账本级资源：未选账本时禁用（ID 离开账本无意义），与流水页搜索同约定；
  // 切换账本会改变候选集合，同时清掉已选值避免串账本
  const watchedBook = Form.useWatch('bookId', form);
  const hasBook = watchedBook !== undefined && watchedBook !== null;

  // 账本列表进页面即请求：搜索区的所属账本默认选中当前账本（isCurrent，与概览分类图同源）；
  // 用户仍可清除（查全部账本）或改选其它账本
  const { data: bookPage, loading: booksLoading } = useRequest(() =>
    bookApi.list({ page: 0, size: 1000 }),
  );
  const currentBookId = booksLoading
    ? undefined
    : ((bookPage?.content || []).find((b) => b.isCurrent)?.id ?? null);

  // 当前账本解析完成后做一次默认初始化：表单回填默认账本 + 图表按默认口径首查
  useEffect(() => {
    if (query !== null || currentBookId === undefined) return;
    if (currentBookId != null) {
      form.setFieldValue('bookId', currentBookId);
    }
    setQuery(buildQuery({ bookId: currentBookId }));
  }, [query, currentBookId, form]);

  const {
    data: accountOptionsRaw,
    loading: accountsLoading,
    onOpenChange: onAccountsOpen,
  } = useLazyDropdown(() => accountApi.options());
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
    data: categoryTreeData,
    loading: categoriesLoading,
    onOpenChange: onCategoriesOpen,
  } = useLazyDropdown(
    () =>
      hasBook
        ? categoryApi.tree(type, watchedBook).then(toCategoryTreeData)
        : Promise.resolve([]),
    [watchedBook],
  );

  const { data, loading } = useRequest(
    () => moneyFlowApi.categoryStatistics({ type, ...query }),
    { refreshDeps: [query], ready: query !== null },
  );

  const handleSearch = () => {
    setQuery(buildQuery(form.getFieldsValue(true)));
  };

  // 重置回到默认口径：当前账本 + 其余条件清空；无当前账本时回退全账本
  const handleReset = () => {
    form.resetFields();
    if (currentBookId != null) {
      form.setFieldValue('bookId', currentBookId);
    }
    setQuery(buildQuery({ bookId: currentBookId }));
  };

  const handleBookChange = () => {
    form.setFieldValue('categoryIds', undefined);
    form.setFieldValue('tagIds', undefined);
    form.setFieldValue('payeeIds', undefined);
  };

  // 时间段快捷预设（与概览分类图同一套选项）
  const timePresets = [
    {
      label: t('timeRange.last30Days'),
      value: radioValueToTimeRange(TIME_RANGE.LAST_30_DAYS),
    },
    {
      label: t('timeRange.last1Year'),
      value: radioValueToTimeRange(TIME_RANGE.LAST_1_YEAR),
    },
    {
      label: t('timeRange.thisMonth'),
      value: radioValueToTimeRange(TIME_RANGE.THIS_MONTH),
    },
    {
      label: t('timeRange.thisYear'),
      value: radioValueToTimeRange(TIME_RANGE.THIS_YEAR),
    },
    {
      label: t('timeRange.lastYear'),
      value: radioValueToTimeRange(TIME_RANGE.LAST_YEAR),
    },
  ];

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

  const fieldStyle = { width: '100%' };

  return (
    <>
      <Card
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '16px 24px' } }}
      >
        <Form form={form} name="categoryReportSearch">
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
                  onChange={handleBookChange}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item
                name="timeRange"
                label={t('moneyFlow.fields.createTime')}
              >
                <DatePicker.RangePicker
                  allowEmpty={[true, true]}
                  presets={timePresets}
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
          </Row>
          <Row gutter={16}>
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
                  disabled={!hasBook}
                  loading={categoriesLoading}
                  onOpenChange={onCategoriesOpen}
                  placeholder={
                    hasBook
                      ? t('moneyFlow.boolean.all')
                      : t('moneyFlow.search.need.book')
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
            <Col span={6} style={{ textAlign: 'right' }}>
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

      <CardPie
        title={t(TYPE_TITLE_KEY[type])}
        data={data?.categories}
        currency={{
          symbol: data?.currencySymbol,
          decimalPlaces: data?.currencyDecimalPlaces,
        }}
        loading={loading}
      />
    </>
  );
};

export default CategoryReport;
