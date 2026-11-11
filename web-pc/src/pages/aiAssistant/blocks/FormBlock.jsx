import { CheckCircleFilled } from '@ant-design/icons';
import { useRequest } from '@umijs/max';
import {
  Alert,
  App,
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Select,
  Tag,
  TreeSelect,
} from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';

import { accountApi } from '@/api/account';
import { bookApi } from '@/api/book';
import { categoryApi } from '@/api/category';
import { messageApi } from '@/api/message';
import { useCurrencyOptions } from '@/hooks/useCurrencyOptions';
// 收支方向常量与文案 key 与流水页同源，避免两处各写一份数字
import {
  TYPE_EXPENSE,
  TYPE_INCOME,
  getTypeLabelKey,
} from '@/pages/moneyFlow/constants';
import { useT } from '@/utils/i18n';

// 类型标签配色与流水列表一致：支出红、收入绿
const TYPE_COLOR = {
  [TYPE_EXPENSE]: 'error',
  [TYPE_INCOME]: 'success',
};

// 表单动作标识，镜像后端 BlockActions（契约定义点在后端，变更以后端为准）
const ACTION_MONEY_FLOW_CREATE = 'money-flow.create';

/** 分类树 → TreeSelect 数据（与流水页同构，只为拿到 id） */
const toTreeData = (nodes) =>
  (nodes || []).map((node) => ({
    title: node.name,
    value: node.id,
    children: node.children?.length
      ? toTreeData(node.children)
      : undefined,
  }));

/**
 * 表单块：按 FormBlockData.action 渲染业务表单，提交后由后端在事务内执行动作。
 *
 * <p>v1 只有一种 action（money-flow.create，记账确认）：模型预填收支方向/金额/摘要/时间，
 * <b>账户与分类由用户在这里选</b>——模型看不到这两类 id，猜出来的只会是错的。
 * <b>收支方向固定为模型判定的值</b>（只读展示）：方向是模型对用户原话的理解，
 * 让用户随手切换会与「分类树、账户可支出/可收入过滤、金额符号」三处口径纠缠，
 * 判错了应当由用户重新描述、模型重新出表，而不是在表单里改一个字段。
 * 账本固定为「当前账本」，金额按账本币种主单位提交，主单位→最小单位的换算在后端做。
 *
 * <p>status=done 时只读展示 resultText：块的状态是服务端事实（提交成功后才翻），
 * 前端不自行推断，刷新后依旧显示已提交。
 *
 * @param {object} block - MessageBlock，data 为 FormBlockData
 * @param {string} agentCode - 分区键，提交路径需要
 * @param {number|string} messageId - 块所属消息 id，提交路径需要
 * @param {Function} [onSubmitted] - 提交成功回调（父级重新拉取消息列表）
 */
export default function FormBlock({
  block,
  agentCode,
  messageId,
  onSubmitted,
}) {
  const t = useT();
  const { message } = App.useApp();
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  const data = block?.data || {};
  const fields = data.fields || {};
  const done = data.status === 'done';

  // 当前账本：金额币种与分类树都以它为准（与流水页同一口径）
  const { data: bookPage } = useRequest(
    () => bookApi.list({ page: 0, size: 1000 }),
    { ready: !done },
  );
  const currentBook = (bookPage?.content || []).find((book) => book.isCurrent);

  // 币种精度：金额输入框按账本币种小数位收敛，零小数位币种（日元等）不会被输成 10.5
  const { options: currencyOptions, ensureLoaded: ensureCurrencyOptions } =
    useCurrencyOptions();
  useEffect(() => {
    if (!done) {
      ensureCurrencyOptions();
    }
  }, [done, ensureCurrencyOptions]);
  const bookCurrency = currencyOptions.find(
    (option) => option.value === currentBook?.currencyId,
  );

  // 收支方向由模型判定后固定，不再随表单输入变化：账户过滤、分类树都锚在这个值上
  const typeValue = fields.type ?? TYPE_EXPENSE;

  // 账户下拉按类型收敛可支出/可收入（与流水页 ACCOUNT_OPTIONS_PARAMS 同口径）
  const { data: accountList, loading: accountsLoading } = useRequest(
    () =>
      accountApi.options(
        typeValue === TYPE_INCOME ? { canIncome: true } : { canExpense: true },
      ),
    { refreshDeps: [typeValue], ready: !done },
  );
  const accountOptions = useMemo(
    () => (accountList || []).map((a) => ({ label: a.label, value: a.value })),
    [accountList],
  );

  const { data: categoryTree, loading: categoriesLoading } = useRequest(
    () => categoryApi.tree(typeValue, currentBook?.id),
    { refreshDeps: [typeValue, currentBook?.id], ready: !done },
  );
  const categoryTreeData = useMemo(
    () => toTreeData(categoryTree),
    [categoryTree],
  );

  // 初始值来自模型预填：时间转 dayjs，账户/分类通常是空的（由用户选）；类型是只读展示，不进表单值
  const initialValues = useMemo(
    () => ({
      amount: fields.amount != null ? Number(fields.amount) : undefined,
      title: fields.title,
      notes: fields.notes,
      createTime: fields.createTime ? dayjs(fields.createTime) : dayjs(),
      accountId: fields.accountId ?? undefined,
      categoryId: fields.categoryId ?? undefined,
    }),
    // 块 id 在消息内唯一，块内容不会原地变化（提交后走 done 分支）
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [block?.id],
  );

  const handleFinish = async (values) => {
    if (messageId == null) {
      return;
    }
    setSubmitting(true);
    try {
      // 不提交 type：收支方向以后端块里存的为准，避免请求被改造成反向记账
      await messageApi.submitBlock(agentCode, messageId, block.id, {
        // 主单位数字直接提交，后端按账本币种精度换算成最小单位
        amount: values.amount,
        title: values.title,
        notes: values.notes ?? null,
        createTime: values.createTime ? values.createTime.valueOf() : null,
        accountId: values.accountId ?? null,
        categoryId: values.categoryId ?? null,
      });
      message.success(t('aiAssistant.block.form.submitted'));
      onSubmitted?.();
    } catch {
      // 失败原因由 request 全局 errorHandler 提示；表单内容保留，用户改完可重试
    } finally {
      setSubmitting(false);
    }
  };

  const amountLabel = bookCurrency?.code
    ? t('moneyFlow.fields.amountByCurrency', { code: bookCurrency.code })
    : t('moneyFlow.fields.amount');

  return (
    <Card
      size="small"
      // 已知动作的标题走 i18n，未知动作用后端下发的标题兜底
      title={
        data.action === ACTION_MONEY_FLOW_CREATE
          ? t('aiAssistant.block.form.moneyFlowCreate')
          : data.title
      }
      style={{ maxWidth: 520 }}
    >
      {done ? (
        <Alert
          type="success"
          showIcon
          icon={<CheckCircleFilled />}
          message={data.resultText || t('aiAssistant.block.form.done')}
        />
      ) : (
        <Form
          form={form}
          layout="vertical"
          initialValues={initialValues}
          onFinish={handleFinish}
          disabled={submitting}
        >
          {/* 收支方向固定为模型判定的值，只做陈述不给编辑：判错了请用户重新描述、重新出表 */}
          <Form.Item label={t('moneyFlow.fields.type')}>
            <Tag
              color={TYPE_COLOR[typeValue]}
              style={{ marginInlineEnd: 0 }}
            >
              {t(getTypeLabelKey(typeValue))}
            </Tag>
          </Form.Item>

          <Form.Item
            name="amount"
            label={amountLabel}
            rules={[
              {
                validator: (_, value) =>
                  value > 0
                    ? Promise.resolve()
                    : Promise.reject(
                        new Error(t('aiAssistant.block.form.amount.invalid')),
                      ),
              },
            ]}
          >
            <InputNumber
              precision={bookCurrency?.decimalPlaces}
              style={{ width: '100%' }}
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

          <Form.Item name="accountId" label={t('moneyFlow.fields.account')}>
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              options={accountOptions}
              loading={accountsLoading}
              placeholder={t('aiAssistant.block.form.account.placeholder')}
            />
          </Form.Item>

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
              loading={categoriesLoading}
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item name="notes" label={t('notes')}>
            <Input.TextArea rows={2} maxLength={1024} />
          </Form.Item>

          <Button
            type="primary"
            htmlType="submit"
            loading={submitting}
            block
          >
            {t('aiAssistant.block.form.submit')}
          </Button>
        </Form>
      )}
    </Card>
  );
}
