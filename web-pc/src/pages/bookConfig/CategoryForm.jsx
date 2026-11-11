import { ProFormSelect, ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import { useMemo } from 'react';
import BaseModalForm from '@/components/BaseModalForm';
import { useT } from '@/utils/i18n';
import { categoryApi } from '@/api/category';
import { useRequest } from '@umijs/max';

export default function CategoryForm({ modal, parentNode, type, onFinish }) {
  const t = useT();

  const isAddChild = !modal.isEdit && !!parentNode;

  // 下拉展开时才拉取，保证列表始终是提交后的最新数据
  const { data: optionsData, loading: optionsLoading, run: loadOptions } = useRequest(
    () => categoryApi.tree(type),
    { manual: true },
  );

  const initialValues = useMemo(() => {
    if (modal.isEdit) {
      return {
        name: modal.currentRow?.name,
        notes: modal.currentRow?.notes,
        parentId: modal.currentRow?.parentId ?? undefined,
      };
    }
    return {
      parentId: parentNode?.id,
    };
  }, [modal.isEdit, modal.currentRow, parentNode]);

  const handleFinish = async (values) => {
    const payload = {
      name: values.name,
      notes: values.notes || null,
      parentId: values.parentId ?? null,
    };
    if (modal.isEdit) {
      await categoryApi.update(modal.currentRow.id, payload);
    } else {
      await categoryApi.create({
        ...payload,
        type,
      });
    }
    await onFinish?.();
    return true;
  };

  const flatOptions = (nodes, level = 0, disabledIds = new Set()) => {
    if (!nodes) return [];
    const result = [];
    for (const node of nodes) {
      result.push({
        value: node.id,
        label: `${'　'.repeat(level)}${node.name}`,
        disabled: disabledIds.has(node.id),
      });
      if (node.children?.length) {
        result.push(...flatOptions(node.children, level + 1, disabledIds));
      }
    }
    return result;
  };

  const collectDescendantIds = (nodes, targetId) => {
    for (const node of nodes || []) {
      if (node.id === targetId) {
        const ids = new Set();
        const walk = (children) => {
          for (const c of children || []) {
            ids.add(c.id);
            walk(c.children);
          }
        };
        walk(node.children);
        return ids;
      }
      const found = collectDescendantIds(node.children, targetId);
      if (found) return found;
    }
    return null;
  };

  const parentOptions = useMemo(() => {
    // 编辑时「自身 + 自身所有后代」都不能选作父级，否则后端会拒（self / descendant）
    const disabledIds = new Set();
    if (modal.isEdit && modal.currentRow?.id != null) {
      disabledIds.add(modal.currentRow.id);
      const descendants = collectDescendantIds(optionsData || [], modal.currentRow.id);
      descendants?.forEach((id) => disabledIds.add(id));
    }
    const options = flatOptions(optionsData || [], 0, disabledIds);
    const ensureOption = (id, name) => {
      if (id != null && name && !options.find((o) => o.value === id)) {
        options.unshift({ value: id, label: name });
      }
    };
    if (parentNode) {
      ensureOption(parentNode.id, parentNode.name);
    }
    if (modal.isEdit && modal.currentRow?.parent) {
      ensureOption(modal.currentRow.parent.id, modal.currentRow.parent.name);
    }
    return options;
  }, [optionsData, parentNode, modal.isEdit, modal.currentRow]);

  return (
    <BaseModalForm
      title={modal.isEdit
        ? t('bookConfig.category.title.edit')
        : t('bookConfig.category.title.add')}
      open={modal.open}
      initialValues={initialValues}
      onOpenChange={modal.onOpenChange}
      onFinish={handleFinish}
      horizontal
    >
      <ProFormSelect
        name="parentId"
        label={t('bookConfig.category.fields.parent')}
        placeholder={t('bookConfig.category.parentPlaceholder')}
        options={parentOptions}
        allowClear
        disabled={isAddChild}
        fieldProps={{
          loading: optionsLoading,
          onDropdownVisibleChange: (open) => {
            if (open) loadOptions();
          },
        }}
      />
      <ProFormText
        name="name"
        label={t('bookConfig.category.fields.name')}
        placeholder={t('bookConfig.category.fields.name')}
        rules={[{ required: true, max: 64 }]}
        fieldProps={{ maxLength: 64 }}
      />
      <ProFormTextArea
        name="notes"
        label={t('notes')}
        placeholder={t('notes')}
        fieldProps={{ rows: 3, maxLength: 1024, showCount: true }}
      />
    </BaseModalForm>
  );
}
