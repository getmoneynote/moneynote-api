import { PlusOutlined } from '@ant-design/icons';
import { useRequest } from '@umijs/max';
import { App, Button, Card, Col, Form, Input, Row, Select, Statistic, Switch, Tabs, theme } from 'antd';
import { useMemo, useRef, useState } from 'react';

import { accountApi } from '@/api/account';
import { currencyApi } from '@/api/currency';
import BaseProTable from '@/components/BaseProTable';
import { useLazyDropdown } from '@/hooks/useLazyDropdown';
import { useModalForm } from '@/hooks/useModalForm';
import { useT } from '@/utils/i18n';
import { formatAmountDisplay, formatMoney } from '@/utils/money';
import { useTableStyles } from '@/utils/tableStyles';

import ActionForm from './ActionForm';
import AdjustForm from './AdjustForm';
import NotesForm from './NotesForm';
import {
  TYPE_CHECKING,
  TYPE_CREDIT,
  TYPE_LOAN,
  buildQueryType,
  buildTabItems,
  isCreditOrLoan,
} from './constants';

/** 搜索表单原始值 → 后端 AccountQueryForm（过滤掉空值；type 由 tab 单独传递）。 */
function buildQuery(values = {}) {
  const query = {};
  const { name, currencyId, include, canExpense, canIncome, canTransferFrom, canTransferTo } = values || {};

  if (name) query.name = name;
  if (currencyId !== undefined && currencyId !== null) query.currencyId = currencyId;
  if (include !== undefined && include !== null) query.include = include;
  if (canExpense !== undefined && canExpense !== null) query.canExpense = canExpense;
  if (canIncome !== undefined && canIncome !== null) query.canIncome = canIncome;
  if (canTransferFrom !== undefined && canTransferFrom !== null) query.canTransferFrom = canTransferFrom;
  if (canTransferTo !== undefined && canTransferTo !== null) query.canTransferTo = canTransferTo;
  return query;
}

export default () => {
  const t = useT();
  const { styles } = useTableStyles();
  const { message, modal } = App.useApp();
  // 颜色一律用 antd token，暗色模式自动跟随（见 docs/前端经验.md §19）
  const { token } = theme.useToken();

  const [form] = Form.useForm();
  const [query, setQuery] = useState({});
  const modalForm = useModalForm();
  const adjustModal = useModalForm();
  const notesModal = useModalForm();
  const actionRef = useRef(null);
  // activeType 为 tab 的字符串 key：'all' 或 '1'-'4'；typeValue 为数字类型，供列/查询使用
  const [activeType, setActiveType] = useState(String(TYPE_CHECKING));
  const typeValue = buildQueryType(activeType);
  // 切换请求进行中的 `id:field`，对应 Switch 禁用，防止连点造成两次翻转竞争
  const [pendingFlags, setPendingFlags] = useState({});

  const { data: currencyOptionsData, loading: currenciesLoading, onOpenChange: onCurrenciesOpen } = useLazyDropdown(() =>
    currencyApi.options(),
  );
  const currencyOptions = (currencyOptionsData || []).map((item) => ({ label: item.label, value: item.value }));

  const booleanOptions = [
    { label: t('account.boolean.true'), value: true },
    { label: t('account.boolean.false'), value: false },
  ];

  // 汇总与列表同条件（type 来自 tab，其余来自搜索表单），任一变化时自动刷新
  const { data: summaryData, run: runSummary } = useRequest(
    () => accountApi.balanceSummary({ ...query, type: typeValue }),
    { refreshDeps: [activeType, query] },
  );

  const refreshSummary = () => runSummary();

  const tabItems = useMemo(() => buildTabItems(t), [t]);

  const handleTabChange = (key) => {
    setActiveType(key);
    // 切换 tab 属于更换数据集，重置回第 1 页，否则停留在旧页码可能看到空列表
    actionRef.current?.reload(true);
  };

  const handleSearch = () => {
    setQuery(buildQuery(form.getFieldsValue(true)));
    // 搜索后结果集变化，同样重置回第 1 页
    actionRef.current?.reload(true);
  };

  const handleReset = () => {
    form.resetFields();
    setQuery({});
    actionRef.current?.reload();
  };

  const updateHandler = (record) => {
    modalForm.showUpdate(record);
  };

  const adjustHandler = (record) => {
    adjustModal.showUpdate(record);
  };

  const notesHandler = (record) => {
    notesModal.showUpdate(record);
  };

  // Switch 受控于 record 的当前值：请求失败时不更新 record，视觉上即回滚到原值；
  // 仍主动 reload 一次，兜底同步后端真实值（如请求实际已落库但响应超时）
  const handleFlagChange = async (record, field, checked) => {
    const key = `${record.id}:${field}`;
    setPendingFlags((prev) => ({ ...prev, [key]: true }));
    try {
      await accountApi.update(record.id, { [field]: checked });
      message.success(t('operation.success'));
    } finally {
      // 失败提示由 request errorHandler 统一处理（全局唯一一条，不在这里重复 catch）
      actionRef.current?.reload();
      refreshSummary();
      setPendingFlags((prev) => ({ ...prev, [key]: false }));
    }
  };

  const deleteHandler = (record) => {
    modal.confirm({
      title: t('account.delete.confirm', { name: record.name }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await accountApi.delete(record.id);
        message.success(t('operation.success'));
        actionRef.current?.reload();
        refreshSummary();
      },
    });
  };

  // 余额原始值与 ≈ 换算值各自带币种符号（两种货币，符号是区分标记）；额度列不带符号
  const renderBalance = (_, record) => {
    const original = formatMoney(record.balance, record.currencyDecimalPlaces, record.currencySymbol);
    // 是否本位币由后端直接下发（isBaseCurrency），不靠 balance==baseBalance 反推
    if (record.isBaseCurrency || record.baseBalance === undefined || record.baseBalance === null) {
      return <span>{original}</span>;
    }
    const baseText = formatMoney(record.baseBalance, record.baseCurrencyDecimalPlaces, record.baseCurrencySymbol);
    return (
      <span>
        {original}
        <span style={{ color: token.colorTextSecondary, marginLeft: 6, fontSize: 12 }}>
          ≈ {baseText}
        </span>
      </span>
    );
  };

  // 金额格不重复币种符号：币种已由"币种"列表达（信用额度/剩余额度列共用）
  const renderMoney = (value, record) => {
    return formatMoney(value, record.currencyDecimalPlaces, record.currencySymbol) || '-';
  };

  // 布尔标志列统一渲染为可编辑 Switch，请求期间禁用防止竞争
  const renderFlag = (field) => (_, record) => (
    <Switch
      size="small"
      checked={record[field]}
      disabled={pendingFlags[`${record.id}:${field}`]}
      onChange={(checked) => handleFlagChange(record, field, checked)}
    />
  );

  const baseColumns = [
    {
      title: t('account.fields.name'),
      dataIndex: 'name',
      sorter: true,
    },
    {
      title: t('account.fields.currencyName'),
      dataIndex: 'currencyName',
      sorter: true,
    },
    {
      title: t('account.fields.balance'),
      dataIndex: 'balance',
      align: 'right',
      className: styles.numericCell,
      render: renderBalance,
      sorter: true,
    },
    {
      title: t('account.fields.no'),
      dataIndex: 'no',
    },
    {
      title: t('account.fields.include'),
      dataIndex: 'include',
      align: 'center',
      sorter: true,
      render: renderFlag('include'),
    },
    ...[
      { title: t('account.fields.canExpense'), dataIndex: 'canExpense' },
      { title: t('account.fields.canIncome'), dataIndex: 'canIncome' },
      { title: t('account.fields.canTransferFrom'), dataIndex: 'canTransferFrom' },
      { title: t('account.fields.canTransferTo'), dataIndex: 'canTransferTo' },
    ].map((col) => ({ ...col, align: 'center', sorter: true, render: renderFlag(col.dataIndex) })),
    // 排序列不提供表头排序：list 默认即按 displayOrder（空在后）+ balance 排，避免 DB 对 NULL 的
    // 默认排位与默认视图不一致（MySQL ASC 时 NULL 在前）
    {
      title: t('account.fields.displayOrder'),
      dataIndex: 'displayOrder',
      align: 'center',
    },
  ];

  // 列随 activeType 动态变化，每次 render 直接重建，不用 useMemo 缓存：
  // 闭包引用 handler/t/token，依赖项易漏，漏了就是 stale 闭包（与 currency 样板保持一致）
  const buildColumns = () => {
    const cols = [...baseColumns];
    if (typeValue === TYPE_CREDIT) {
      cols.splice(3, 0, {
        title: t('account.fields.creditLimit'),
        dataIndex: 'creditLimit',
        align: 'right',
        className: styles.numericCell,
        render: renderMoney,
      });
      // 剩余额度是后端算出的派生值（非实体列），不支持排序
      cols.splice(4, 0, {
        title: t('account.fields.remainLimit'),
        dataIndex: 'remainLimit',
        align: 'right',
        className: styles.numericCell,
        render: renderMoney,
      });
      cols.splice(5, 0, {
        title: t('account.fields.billDay'),
        dataIndex: 'billDay',
        align: 'left',
        render: (_, record) => (record.billDay ? t('account.fields.billDay.value', { day: record.billDay }) : '-'),
      });
    } else if (typeValue === TYPE_LOAN) {
      cols.splice(3, 0, {
        title: t('account.fields.creditLimit'),
        dataIndex: 'creditLimit',
        align: 'right',
        className: styles.numericCell,
        render: renderMoney,
      });
      // 剩余额度是后端算出的派生值（非实体列），不支持排序
      cols.splice(4, 0, {
        title: t('account.fields.remainLimit'),
        dataIndex: 'remainLimit',
        align: 'right',
        className: styles.numericCell,
        render: renderMoney,
      });
      cols.splice(5, 0, {
        title: t('account.fields.apr'),
        dataIndex: 'apr',
        align: 'right',
        className: styles.numericCell,
        render: (_, record) => (record.apr !== undefined && record.apr !== null ? `${record.apr}%` : '-'),
      });
    }
    cols.push({
      title: t('operation'),
      align: 'center',
      width: 280,
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
          key="adjust"
          size="small"
          type="link"
          onClick={() => adjustHandler(record)}
        >
          {t('account.adjust')}
        </Button>,
        <Button
          key="notes"
          size="small"
          type="link"
          onClick={() => notesHandler(record)}
        >
          {t('notes')}
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
    });
    return cols;
  };

  const columns = buildColumns();

  const fieldStyle = { width: '100%' };

  return (
    <>
      <Card style={{ marginBottom: 16 }} styles={{ body: { padding: '16px 24px' } }}>
        <Form form={form} name="accountSearch">
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="name" label={t('account.fields.name')}>
                <Input allowClear placeholder={t('account.fields.name')} style={fieldStyle} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="currencyId" label={t('account.fields.currencyName')}>
                <Select
                  allowClear
                  showSearch
                  optionFilterProp="label"
                  placeholder={t('account.boolean.all')}
                  options={currencyOptions}
                  loading={currenciesLoading}
                  onOpenChange={onCurrenciesOpen}
                  style={fieldStyle}
                />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="include" label={t('account.fields.include')}>
                <Select allowClear placeholder={t('account.boolean.all')} options={booleanOptions} style={fieldStyle} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="canExpense" label={t('account.fields.canExpense')}>
                <Select allowClear placeholder={t('account.boolean.all')} options={booleanOptions} style={fieldStyle} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="canIncome" label={t('account.fields.canIncome')}>
                <Select allowClear placeholder={t('account.boolean.all')} options={booleanOptions} style={fieldStyle} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="canTransferFrom" label={t('account.fields.canTransferFrom')}>
                <Select allowClear placeholder={t('account.boolean.all')} options={booleanOptions} style={fieldStyle} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="canTransferTo" label={t('account.fields.canTransferTo')}>
                <Select allowClear placeholder={t('account.boolean.all')} options={booleanOptions} style={fieldStyle} />
              </Form.Item>
            </Col>
            <Col span={6} style={{ textAlign: 'right' }}>
              <Button type="primary" onClick={handleSearch} style={{ marginRight: 8 }}>
                {t('account.search')}
              </Button>
              <Button onClick={handleReset}>{t('account.reset')}</Button>
            </Col>
          </Row>
        </Form>
      </Card>
      <Card
        style={{ marginBottom: 16 }}
        styles={{ body: { padding: '12px 24px' } }}
      >
        <Tabs
          activeKey={String(activeType)}
          items={tabItems}
          onChange={handleTabChange}
          style={{ marginBottom: 12 }}
        />
        <div
          style={{
            display: 'flex',
            gap: 32,
            flexWrap: 'wrap',
          }}
        >
          <Statistic
            title={t('account.balance.total')}
            prefix={summaryData?.currencySymbol}
            value={formatAmountDisplay(summaryData?.total, summaryData?.decimalPlaces)}
            valueStyle={{ color: summaryData?.total < 0 ? token.colorError : undefined }}
          />
          {isCreditOrLoan(typeValue) && (
            <>
              <Statistic
                title={t('account.creditLimit.total')}
                prefix={summaryData?.currencySymbol}
                value={formatAmountDisplay(summaryData?.totalCreditLimit, summaryData?.decimalPlaces)}
              />
              <Statistic
                title={t('account.available.total')}
                prefix={summaryData?.currencySymbol}
                value={formatAmountDisplay(summaryData?.totalAvailable, summaryData?.decimalPlaces)}
                valueStyle={{ color: token.colorSuccess }}
              />
            </>
          )}
        </div>
      </Card>
      <BaseProTable
        actionRef={actionRef}
        columns={columns}
        search={false}
        // 币种列 dataIndex 是 DTO 的 currencyName，排序需映射到实体的 currency.name（nested path 由 Spring Data 解析）
        sortMap={{ currencyName: 'currency.name' }}
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={() => modalForm.showAdd()}>
            <PlusOutlined />
            {t('add')}
          </Button>,
        ]}
        request={(params) => accountApi.list({ ...params, ...query, type: typeValue })}
        pagination={{ pageSize: 10 }}
      />
      <ActionForm
        modal={modalForm}
        defaultType={typeValue ?? TYPE_CHECKING}
        onFinish={() => {
          // 新增的记录落在第 1 页，重置页码才能立即看到；编辑/调整不改变记录数，不需要重置
          actionRef.current?.reload(true);
          refreshSummary();
        }}
      />
      <AdjustForm
        modal={adjustModal}
        onFinish={() => {
          message.success(t('operation.success'));
          actionRef.current?.reload();
          refreshSummary();
        }}
      />
      <NotesForm
        modal={notesModal}
        onFinish={() => {
          message.success(t('operation.success'));
          actionRef.current?.reload();
        }}
      />
    </>
  );
};
