import { useCallback, useState } from 'react';

/** 弹窗模式：add=新增，copy=复制新增，edit=编辑 */

/**
 * 弹窗统一状态管理：open / 模式 / 当前行。
 * 每个列表页持有自己的实例，通过 props 传给 ActionForm。
 *
 * 约定：isEdit=true 时 currentRow 一定有值（showUpdate(row) 保证），
 * 业务代码不需要在 isEdit 分支内再判空 currentRow。
 */
export function useModalForm() {
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState('add');
  const [currentRow, setCurrentRow] = useState();

  const showAdd = useCallback(() => {
    setMode('add');
    setCurrentRow(undefined);
    setOpen(true);
  }, []);

  const showCopy = useCallback((row) => {
    setMode('copy');
    setCurrentRow(row);
    setOpen(true);
  }, []);

  const showUpdate = useCallback((row) => {
    setMode('edit');
    setCurrentRow(row);
    setOpen(true);
  }, []);

  const hide = useCallback(() => setOpen(false), []);

  return {
    open,
    mode,
    isEdit: mode === 'edit',
    isCopy: mode === 'copy',
    currentRow,
    onOpenChange: setOpen,
    showAdd,
    showCopy,
    showUpdate,
    hide,
  };
}
