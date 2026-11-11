import { useRequest } from '@umijs/max';
import { Divider } from 'antd';
import { createStyles } from 'antd-style';
import React from 'react';
import { userApi } from '@/api';

const useStyles = createStyles(({ token, css }) => ({
  footer: css`
    padding: 16px 24px;
    text-align: center;
    color: ${token.colorTextDescription};
    font-size: ${token.fontSizeSM}px;
    line-height: ${token.lineHeight};
    background: transparent;
    a {
      color: inherit;
    }
  `,
}));

const Footer = () => {
  const { styles } = useStyles();
  const year = new Date().getFullYear();

  const { data: { version } = {} } = useRequest(userApi.version);

  return (
    <div className={styles.footer}>
      &copy; {year}{' '}
      <a href="https://www.moneynote.com" target="_blank" rel="noreferrer">moneynote.com</a>
      <Divider orientation="vertical" />
      v{__APP_VERSION__}
      <Divider orientation="vertical" />
      api-version: {version}
    </div>
  );
};

export default Footer;
