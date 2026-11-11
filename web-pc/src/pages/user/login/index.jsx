import { LockOutlined, UserOutlined } from '@ant-design/icons';
import {
  LoginForm,
  ProFormCheckbox,
  ProFormText,
} from '@ant-design/pro-components';
import { Helmet, Link, SelectLang } from '@umijs/max';
import { App } from 'antd';
import { createStyles } from 'antd-style';
import React from 'react';
import Settings from '@/../config/defaultSettings';
import { userApi } from '@/api';
import { Footer } from '@/components';
import { setAccessToken } from '@/utils/auth';
import { useT } from '@/utils/i18n';

const useStyles = createStyles(({ token, css }) => ({
  container: css`
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: auto;
    background: ${token.colorBgLayout};

    @media (min-width: ${token.screenMD}px) {
      background-image: url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr');
      background-size: cover;
    }
  `,
  lang: css`
    width: 100%;
    height: 40px;
    line-height: 44px;
    text-align: right;

    .ant-dropdown-trigger {
      margin-right: 24px;
    }
  `,
  content: css`
    flex: 1;
    padding: 32px 0;
  `,
  prefixIcon: css`
    margin-left: 8px;
    color: rgba(0, 0, 0, 0.2);
    font-size: 24px;
    vertical-align: middle;
    cursor: pointer;
    transition: color 0.3s;

    &:hover {
      color: ${token.colorPrimaryActive};
    }
  `,
}));

/**
 * 登录成功后的跳转地址。
 * 优先取 URL 上的 redirect 参数（经同源校验防开放重定向），否则默认 /report/dashboard。
 * 应用目前只有 /report/dashboard 一个登录后页面，统一落到这里。
 */
const LOGIN_LANDING = '/report/dashboard';

const getSafeRedirectUrl = (redirect) => {
  if (!redirect?.startsWith('/')) return LOGIN_LANDING;

  if (redirect.startsWith('//')) return LOGIN_LANDING;

  try {
    const parsed = new URL(redirect, window.location.origin);
    if (parsed.origin !== window.location.origin) return LOGIN_LANDING;
    return `${parsed.pathname}${parsed.search}${parsed.hash}`;
  } catch {
    return LOGIN_LANDING;
  }
};

const Login = () => {
  const { styles } = useStyles();
  const t = useT();
  const { message } = App.useApp();

  const handleSubmit = async (values) => {
    const loginResponse = await userApi.login({
      username: values.username,
      password: values.password,
    });
    setAccessToken(loginResponse.accessToken, values.remember !== false);
    message.success(t('pages.login.success'));
    const urlParams = new URL(window.location.href).searchParams;
    const redirectUrl = getSafeRedirectUrl(urlParams.get('redirect'));
    window.location.href = redirectUrl;
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          {t('menu.login')}
          {Settings.title && ` - ${Settings.title}`}
        </title>
      </Helmet>
      <div className={styles.lang} data-lang>
        <SelectLang />
      </div>
      <div className={styles.content}>
        <LoginForm
          logo={<img alt="logo" src="/logo.svg" />}
          title="MoneyNote"
          subTitle=" "
          initialValues={{ remember: true }}
          onFinish={handleSubmit}
          actions={
            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <Link to="/register">{t('pages.login.noAccount')}</Link>
            </div>
          }
        >
          <ProFormText
            name="username"
            fieldProps={{
              size: 'large',
              prefix: <UserOutlined className={styles.prefixIcon} />,
            }}
            placeholder={t('pages.login.username.placeholder')}
            rules={[{ required: true }]}
          />
          <ProFormText.Password
            name="password"
            fieldProps={{
              size: 'large',
              prefix: <LockOutlined className={styles.prefixIcon} />,
            }}
            placeholder={t('pages.login.password.placeholder')}
            rules={[{ required: true }]}
          />
          <div style={{ marginBottom: 24 }}>
            <ProFormCheckbox noStyle name="remember">
              {t('pages.login.remember')}
            </ProFormCheckbox>
          </div>
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};

export default Login;
