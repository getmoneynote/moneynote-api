import { Link } from '@umijs/max';
import { Button, Card, Result } from 'antd';
import React from 'react';
import { useT } from '@/utils/i18n';

const Exception404 = () => {
  const t = useT();
  return (
    <Card variant="borderless">
      <Result
        status="404"
        title="404"
        subTitle={t('pages.404.subTitle')}
        extra={
          <Link to="/" prefetch>
            <Button type="primary">{t('pages.404.buttonText')}</Button>
          </Link>
        }
      />
    </Card>
  );
};

export default Exception404;
