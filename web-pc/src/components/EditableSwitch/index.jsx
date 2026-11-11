import { Switch } from 'antd';
import { useEffect, useState } from 'react';

// 就地切换的 Switch：本地乐观态 + 失败回滚，避免直接受控于远端数据导致点击无反应。
export default function EditableSwitch({ value, onChange }) {
  const [loading, setLoading] = useState(false);
  const [checked, setChecked] = useState(value);

  useEffect(() => {
    setChecked(value);
  }, [value]);

  const handleChange = async (next) => {
    setChecked(next);
    setLoading(true);
    try {
      await onChange(next);
    } catch {
      setChecked(value);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Switch
      size="small"
      checked={checked}
      loading={loading}
      onChange={handleChange}
    />
  );
}