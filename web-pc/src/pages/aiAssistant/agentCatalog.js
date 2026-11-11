// 前端静态 Agent 目录：四个子菜单各对应一个 Agent、一条固定时间线。
// 后端 AgentRegistry 的 CODE 是事实来源，这里只做「菜单 → code / 文案 key」的映射，
// 不向后端拉动态目录。新增 Agent 时三处要同步：
//   ① 后端 @Component + CODE + resources/prompts/<name>/system.md；
//   ② config/routes.js 增加子路由 + locales/*/menu.js 增加菜单文案；
//   ③ 本表增加一项 + 一个 wrapper 文件 + locales/*/aiAssistant.js 的 welcome/hint 文案。
export const AGENTS = {
  BOOKKEEPING: {
    code: 'BOOKKEEPING',
    welcomeKey: 'aiAssistant.welcome.BOOKKEEPING',
    hintKey: 'aiAssistant.hint.BOOKKEEPING',
  },
  ACCOUNT_ANALYSIS: {
    code: 'ACCOUNT_ANALYSIS',
    welcomeKey: 'aiAssistant.welcome.ACCOUNT_ANALYSIS',
    hintKey: 'aiAssistant.hint.ACCOUNT_ANALYSIS',
  },
  MONEY_FLOW_ANALYSIS: {
    code: 'MONEY_FLOW_ANALYSIS',
    welcomeKey: 'aiAssistant.welcome.MONEY_FLOW_ANALYSIS',
    hintKey: 'aiAssistant.hint.MONEY_FLOW_ANALYSIS',
  },
  GENERAL_CHAT: {
    code: 'GENERAL_CHAT',
    welcomeKey: 'aiAssistant.welcome.GENERAL_CHAT',
    hintKey: 'aiAssistant.hint.GENERAL_CHAT',
  },
};
