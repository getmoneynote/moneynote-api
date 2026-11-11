import { ModalForm } from '@ant-design/pro-components';

/**
 * 统一弹窗表单壳：所有业务弹窗统一走这里，公共行为集中管理。
 * - destroyOnHidden：关闭即销毁表单，保证每次打开都是全新状态，initialValues 才可靠
 * - centered / mask.closable=false：统一交互，防止误点遮罩丢表单
 * - horizontal：快捷开启水平布局，默认 labelCol=6 / wrapperCol=16
 * 业务可通过 modalProps / layout / labelCol / wrapperCol 覆盖这些默认值。
 */
export default function BaseModalForm(props) {
  const { modalProps, horizontal, labelCol, wrapperCol, layout, ...rest } = props;

  const computedLayout = layout ?? (horizontal ? 'horizontal' : undefined);
  const computedLabelCol = labelCol ?? (horizontal ? { span: 6 } : undefined);
  const computedWrapperCol = wrapperCol ?? (horizontal ? { span: 16 } : undefined);

  return (
    <ModalForm
      {...rest}
      layout={computedLayout}
      labelCol={computedLabelCol}
      wrapperCol={computedWrapperCol}
      modalProps={{
        centered: true,
        destroyOnHidden: true,
        mask: { closable: false },
        ...modalProps,
      }}
    />
  );
}
