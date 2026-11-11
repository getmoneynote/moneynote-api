import { createStyles } from 'antd-style';

export const useStyles = createStyles(({ css, token }) => ({
  // 会话侧边栏移除后的单栏时间线容器
  layout: css`
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;
    background: ${token.colorBgContainer};
  `,

  messages: css`
    flex: 1;
    overflow-y: auto;
    padding: ${token.paddingMD}px;
    display: flex;
    flex-direction: column;
    align-items: center;

    > * {
      width: 100%;
    }
  `,

  footer: css`
    padding: ${token.paddingMD}px;
    border-top: 1px solid ${token.colorBorderSecondary};
    display: flex;
    justify-content: center;
  `,

  footerCenter: css`
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: ${token.paddingLG}px;
    gap: 24px;
    margin-top: -10%;
  `,

  welcomeTitle: css`
    font-size: 32px;
    font-weight: 600;
    color: ${token.colorText};
    text-align: center;
  `,

  welcomeHint: css`
    font-size: ${token.fontSize}px;
    color: ${token.colorTextSecondary};
    text-align: center;
  `,

  loading: css`
    margin: auto;
  `,

  // 生成等待态：提交到首个 token 之间（SSE 尚未写出任何字节）的一行反馈
  generatingHint: css`
    display: inline-flex;
    align-items: center;
    gap: ${token.marginXS}px;
    font-size: ${token.fontSizeSM}px;
    color: ${token.colorTextSecondary};
  `,

  cursor: css`
    animation: chatbot-blink 0.8s step-end infinite;

    @keyframes chatbot-blink {
      0%, 100% { opacity: 1; }
      50% { opacity: 0; }
    }
  `,
}));
