import { PlusOutlined } from '@ant-design/icons';
import { useModel } from '@umijs/max';
import { App, Button } from 'antd';
import { useEffect, useRef, useState } from 'react';
import { currencyApi } from '@/api/currency';
import { userApi } from '@/api/user';
import BaseProTable from '@/components/BaseProTable';
import { useCurrencyOptions } from '@/hooks/useCurrencyOptions';
import { useModalForm } from '@/hooks/useModalForm';
import { useT } from '@/utils/i18n';
import { useTableStyles } from '@/utils/tableStyles';
import ActionForm from './ActionForm';

export default () => {
  const t = useT();
  const { styles } = useTableStyles();
  // 静态 message/modal 不受 ConfigProvider 控制，组件内统一走 App.useApp()
  const { message, modal } = App.useApp();
  // 响应即失效通知：setBaseCurrency 返回最新会话 VO，直接写回全局用户态
  const { setInitialState } = useModel('@@initialState');

  const modalForm = useModalForm();
  const actionRef = useRef(null);

  // —— 搜索区：换算基准币种默认选中美元，无美元时回退到第一个 ——
  const formRef = useRef(null);
  const {
    options: currencyOptions,
    loading: currencyLoading,
    ensureLoaded: ensureCurrencyOptions,
    refresh: refreshCurrencyOptions,
  } = useCurrencyOptions();
  // 搜索区默认值（换算基准币种）驱动列表首查，币种字典必须在进页面时就有
  useEffect(() => {
    ensureCurrencyOptions();
  }, [ensureCurrencyOptions]);
  const [baseOption, setBaseOption] = useState(); // 搜索区选中的换算基准 option（value=币种id，label=币种名）
  useEffect(() => {
    if (currencyOptions.length && !formRef.current?.getFieldValue('base')) {
      const defaultItem =
        currencyOptions.find((o) => o.code === 'USD') || currencyOptions[0];
      if (defaultItem) {
        formRef.current?.setFieldsValue({ base: defaultItem.value });
        setBaseOption(defaultItem);
        formRef.current?.submit(); // 用默认 base 重新查询列表
      }
    }
  }, [currencyOptions]);

  const addHandler = () => {
    modalForm.showAdd();
  };

  const updateHandler = (record) => {
    modalForm.showUpdate(record);
  };

  // 删除：二次确认后调接口，失败提示由 request errorHandler 统一处理（确认框保持打开）
  const deleteHandler = (record) => {
    modal.confirm({
      title: t('currency.delete.confirm', { name: record.name }),
      okButtonProps: { danger: true },
      onOk: async () => {
        await currencyApi.delete(record.id);
        message.success(t('operation.success'));
        // 如果删的是当前搜索区选中的换算基准，先清空 base，
        // 等 currencyOptions 刷新后 useEffect 会自动选第一个并 submit
        const wasCurrentBase = record.id === baseOption?.value;
        if (wasCurrentBase) {
          formRef.current?.setFieldsValue({ base: undefined });
          setBaseOption(undefined);
        }
        await refreshCurrencyOptions();
        // 删的是当前基准时，useEffect 会在 options 刷新后自动选第一个并 submit，
        // 这里再 reload 会同参数连发两次请求
        if (!wasCurrentBase) {
          actionRef.current?.reload();
        }
      },
    });
  };

  // 设为本位币：成功后联动搜索区的换算基准，汇率列立即以新本位币换算
  const setBaseHandler = async (record) => {
    const currentUser = await userApi.setBaseCurrency(record.id);
    setInitialState((prev) => ({ ...prev, currentUser }));
    message.success(t('operation.success'));
    actionRef.current?.reload();
  };

  const columns = [
    {
      title: t('currency.query.base'),
      dataIndex: 'base',
      hideInTable: true,
      valueType: 'select',
      fieldProps: {
        allowClear: false,
        options: currencyOptions,
        loading: currencyLoading,
        // 每次展开下拉重新拉取，保证拿到最新币种列表
        onOpenChange: (open) => open && refreshCurrencyOptions(),
        labelInValue: false,
        onChange: (_value, option) => {
          setBaseOption(option);
          formRef.current?.submit();
        },
      },
    },
    {
      title: t('currency.fields.name'),
      dataIndex: 'name',
      search: false,
    },
    {
      title: t('currency.fields.code'),
      dataIndex: 'code',
      search: false,
    },
    {
      title: t('currency.fields.symbol'),
      dataIndex: 'symbol',
      search: false,
    },
    {
      title: t('currency.fields.baseRate', { name: baseOption?.label }),
      dataIndex: 'baseRate',
      search: false,
      align: 'right',
      className: styles.numericCell,
    },
    {
      title: t('currency.fields.decimalPlaces'),
      dataIndex: 'decimalPlaces',
      search: false,
      align: 'center',
    },
    {
      title: t('operation'),
      search: false,
      align: 'center',
      width: 250,
      render: (_, record) => {
        return [
          <Button
            key="update"
            size="small"
            type="link"
            onClick={() => updateHandler(record)}
          >
            {t('update')}
          </Button>,
          <Button
            key="delete"
            disabled={record.deletable === false}
            size="small"
            type="link"
            onClick={() => deleteHandler(record)}
          >
            {t('delete')}
          </Button>,
          <Button
            key="setBase"
            disabled={record.isBase}
            size="small"
            type="link"
            onClick={() => setBaseHandler(record)}
          >
            {t('currency.set.base')}
          </Button>,
        ];
      },
    },
  ];

  return (
    <>
      <BaseProTable
        formRef={formRef}
        actionRef={actionRef}
        columns={columns}
        search={{ optionRender: () => [] }}
        toolBarRender={() => [
          <Button key="add" type="primary" onClick={() => addHandler()}>
            <PlusOutlined />
            {t('add')}
          </Button>,
        ]}
        request={(params) => {
          if (!currencyOptions.length || !params.base) {
            return Promise.resolve({ data: [], success: true, total: 0 });
          }
          return currencyApi.list(params);
        }}
      />
      <ActionForm
        modal={modalForm}
        onFinish={() => {
          message.success(t('operation.success'));
          actionRef.current?.reload();
        }}
      />
    </>
  );
};
