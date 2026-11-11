import { LogoutOutlined } from '@ant-design/icons';
import { App, Avatar, Button } from 'antd';
import { userApi } from '@/api';
import { useT } from '@/utils/i18n';
import { clearAccessToken, getAccessToken } from '../../utils/auth';
import HeaderDropdown from '../HeaderDropdown';
import useHeaderActionStyles from './style';

// 先作废后端会话，再清本地登录态。
// 接口失败（断网、会话已被其他端清掉等）忽略，不让用户卡在登录态里退不出去
const logout = async () => {
  try {
    await userApi.logout();
  } catch {
    // 忽略：本地清理不依赖接口结果
  }
  clearAccessToken();
  // 整页跳转，与 401 处理一致：顺带清掉 initialState / useRequest 缓存等内存态，
  // 避免退出后按“后退”短暂看到旧用户数据
  window.location.href = '/login';
};

const UserMenu = () => {
  const { styles } = useHeaderActionStyles();
  const { modal } = App.useApp();
  const t = useT();

  // 无登录态时不显示用户菜单
  if (!getAccessToken()) {
    return null;
  }

  return (
    <HeaderDropdown
      placement="bottomRight"
      arrow
      menu={{
        selectedKeys: [],
        onClick: ({ key }) => {
          if (key === 'logout') {
            modal.confirm({
              title: t('menu.logout'),
              content: t('menu.logout.confirm'),
              onOk: logout,
            });
          }
        },
        items: [
          {
            key: 'logout',
            icon: <LogoutOutlined />,
            label: t('menu.logout'),
          },
        ],
      }}
    >
      <Button type="text" className={styles.action} aria-label="用户菜单">
        <Avatar
          size="small"
          src="https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png"
        />
      </Button>
    </HeaderDropdown>
  );
};

export default UserMenu;
