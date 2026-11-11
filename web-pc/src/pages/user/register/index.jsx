import {
  LockOutlined,
  UserOutlined,
  VerifiedOutlined,
} from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { Helmet, Link, SelectLang } from '@umijs/max';
import { App } from 'antd';
import { createStyles } from 'antd-style';
import React from 'react';
import Settings from '@/../config/defaultSettings';
import { userApi } from '@/api';
import { Footer } from '@/components';
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

const Register = () => {
  const { styles } = useStyles();
  const t = useT();
  const { message } = App.useApp();

  const handleSubmit = async (values) => {
    await userApi.register(values);
    message.success(t('pages.register.success'));
    // 延迟跳转，先让用户看到“注册成功”提示，再回到登录页
    setTimeout(() => {
      window.location.href = '/login';
    }, 1500);
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          {t('menu.register')}
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
          onFinish={handleSubmit}
          actions={
            <div style={{ textAlign: 'center', marginTop: 12 }}>
              <Link to="/login">{t('pages.register.haveAccount')}</Link>
            </div>
          }
          submitter={{
            searchConfig: {
              submitText: t('menu.register'),
            },
          }}
        >
          <ProFormText
            name="username"
            fieldProps={{
              size: 'large',
              autoComplete: 'off',
              prefix: <UserOutlined className={styles.prefixIcon} />,
            }}
            placeholder={t('pages.register.username.placeholder')}
            rules={[
              { required: true },
              {
                pattern: /^[a-zA-Z0-9_]{1,16}$/,
                message: t('pages.register.username.invalid'),
              },
            ]}
          />
          <ProFormText.Password
            name="password"
            fieldProps={{
              size: 'large',
              autoComplete: 'new-password',
              prefix: <LockOutlined className={styles.prefixIcon} />,
            }}
            placeholder={t('pages.register.password.placeholder')}
            rules={[
              { required: true },
              {
                min: 6,
                max: 32,
                message: t('pages.register.password.invalid'),
              },
            ]}
          />
          <ProFormText
            name="inviteCode"
            fieldProps={{
              size: 'large',
              autoComplete: 'off',
              prefix: <VerifiedOutlined className={styles.prefixIcon} />,
            }}
            placeholder={t('pages.register.inviteCode.placeholder')}
            rules={[{ required: true }]}
          />
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};

export default Register;
