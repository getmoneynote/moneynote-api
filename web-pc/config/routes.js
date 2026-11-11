export default [
  {
    path: '/login',
    name: 'login',
    layout: false,
    component: './user/login',
  },
  {
    path: '/register',
    name: 'register',
    layout: false,
    component: './user/register',
  },
  {
    path: '/',
    redirect: '/report/dashboard',
  },
  {
    path: '/report',
    name: 'report',
    icon: 'barChart',
    routes: [
      {
        path: '/report',
        redirect: '/report/dashboard',
      },
      {
        path: '/report/dashboard',
        name: 'dashboard',
        component: './report/dashboard',
      },
      {
        path: '/report/expense-category',
        name: 'expense.category',
        component: './report/expenseCategory',
      },
      {
        path: '/report/income-category',
        name: 'income.category',
        component: './report/incomeCategory',
      },
    ],
  },
  {
    path: '/account',
    name: 'account',
    icon: 'bank',
    component: './account',
  },
  {
    path: '/money-flow',
    name: 'moneyFlow',
    icon: 'transaction',
    component: './moneyFlow',
  },
  {
    path: '/book',
    name: 'book',
    icon: 'book',
    component: './book',
  },
  {
    path: '/book-config',
    name: 'bookConfig',
    icon: 'setting',
    component: './bookConfig',
  },
  {
    path: '/currency',
    name: 'currency',
    icon: 'dollar',
    component: './currency',
  },
  {
    path: '/reminder',
    name: 'reminder',
    icon: 'bell',
    component: './reminder',
  },
  {
    // 旧路径兼容：AI 配置已并入 AI 助手子菜单，保留书签/历史链接可用。
    // 不设 name，因此不会生成额外的菜单项。
    path: '/llm-config',
    redirect: '/ai-assistant/llm-config',
  },
  {
    // AI 助手分组：首位是 AI 配置，其后 4 个 Agent 各一个独立菜单 = 一条固定时间线。
    // 四个聊天子路由复用同一个 ChatPage，各自用一个轻量 wrapper 把 agent 静态传入
    // （不解析 URL，不依赖路由 hooks）。子路由 name 是相对名，菜单 i18n key
    // 由框架拼成 menu.aiAssistant.xxx。子路由 path 必须以父路径开头。
    path: '/ai-assistant',
    name: 'aiAssistant',
    icon: 'comment',
    routes: [
      {
        // 父菜单默认仍进记账助手（配置页是低频设置，不做落地页）
        path: '/ai-assistant',
        redirect: '/ai-assistant/bookkeeping',
      },
      {
        // 非聊天页，但归入 AI 助手分组且排在首位；菜单文案取 menu.aiAssistant.llmConfig
        path: '/ai-assistant/llm-config',
        name: 'llmConfig',
        icon: 'robot',
        component: './llmConfig',
      },
      {
        path: '/ai-assistant/bookkeeping',
        name: 'bookkeeping',
        icon: 'wallet',
        component: './aiAssistant/bookkeeping',
      },
      {
        path: '/ai-assistant/account-analysis',
        name: 'accountAnalysis',
        icon: 'pieChart',
        component: './aiAssistant/accountAnalysis',
      },
      {
        path: '/ai-assistant/money-flow-analysis',
        name: 'moneyFlowAnalysis',
        icon: 'fund',
        component: './aiAssistant/moneyFlowAnalysis',
      },
      {
        path: '/ai-assistant/general-chat',
        name: 'generalChat',
        icon: 'message',
        component: './aiAssistant/generalChat',
      },
    ],
  },
  {
    component: './exception/404',
    layout: false,
    path: '*',
  },
];
