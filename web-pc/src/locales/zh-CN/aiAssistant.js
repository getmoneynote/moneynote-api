export default {
  'aiAssistant.stream.failed': '生成失败',
  // 提交到首个 token 之间的等待态（路由已移除，只是 SSE 尚未写出字节）
  'aiAssistant.generating': '正在生成…',
  'aiAssistant.thinking': '思考中',
  'aiAssistant.thinking.process': '思考过程',
  'aiAssistant.clear': '清空记录',
  'aiAssistant.clear.confirm': '确定清空这个助手的全部聊天记录吗？删除后不可恢复。',
  // 结构化展示块（后端 SSE block 事件 / 历史消息 parts 下发）
  'aiAssistant.block.chart.title': '图表',
  'aiAssistant.block.form.moneyFlowCreate': '记账确认',
  'aiAssistant.block.form.submit': '确认记账',
  'aiAssistant.block.form.submitted': '已记账',
  'aiAssistant.block.form.done': '已提交',
  'aiAssistant.block.form.amount.invalid': '请输入大于 0 的金额',
  'aiAssistant.block.form.account.placeholder': '选择账户（可空）',
  // 四个独立菜单各自的欢迎语与输入框提示（key 后缀为后端 agentCode）
  'aiAssistant.welcome.BOOKKEEPING': '记账助手',
  'aiAssistant.hint.BOOKKEEPING': '描述一笔收支，我来帮你整理记账要素',
  'aiAssistant.welcome.ACCOUNT_ANALYSIS': '资产分析',
  'aiAssistant.hint.ACCOUNT_ANALYSIS': '问问当前的资产、负债与净资产状况',
  'aiAssistant.welcome.MONEY_FLOW_ANALYSIS': '收支分析',
  'aiAssistant.hint.MONEY_FLOW_ANALYSIS': '聊聊收入、支出、结余与统计口径',
  'aiAssistant.welcome.GENERAL_CHAT': '通用助手',
  'aiAssistant.hint.GENERAL_CHAT': '账务数据之外的问题可以问我',
};
