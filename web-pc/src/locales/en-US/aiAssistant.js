export default {
  'aiAssistant.stream.failed': 'Generation failed',
  // Waiting state between submit and the first token (routing removed; SSE has no bytes yet)
  'aiAssistant.generating': 'Generating...',
  'aiAssistant.thinking': 'Thinking',
  'aiAssistant.thinking.process': 'Thought process',
  'aiAssistant.clear': 'Clear history',
  'aiAssistant.clear.confirm': 'Clear all chat history for this assistant? This cannot be undone.',
  // Structured blocks (pushed by the backend block event / message parts)
  'aiAssistant.block.chart.title': 'Chart',
  'aiAssistant.block.form.moneyFlowCreate': 'Confirm entry',
  'aiAssistant.block.form.submit': 'Confirm entry',
  'aiAssistant.block.form.submitted': 'Entry saved',
  'aiAssistant.block.form.done': 'Submitted',
  'aiAssistant.block.form.amount.invalid': 'Enter an amount greater than 0',
  'aiAssistant.block.form.account.placeholder': 'Select an account (optional)',
  // Welcome title and input hint for each standalone menu (suffix is the backend agentCode)
  'aiAssistant.welcome.BOOKKEEPING': 'Bookkeeping',
  'aiAssistant.hint.BOOKKEEPING': 'Describe an income or expense and I will organize the details',
  'aiAssistant.welcome.ACCOUNT_ANALYSIS': 'Assets',
  'aiAssistant.hint.ACCOUNT_ANALYSIS': 'Ask about your current assets, liabilities and net worth',
  'aiAssistant.welcome.MONEY_FLOW_ANALYSIS': 'Income & Expense',
  'aiAssistant.hint.MONEY_FLOW_ANALYSIS': 'Discuss income, spending, balance and analysis methods',
  'aiAssistant.welcome.GENERAL_CHAT': 'General Assistant',
  'aiAssistant.hint.GENERAL_CHAT': 'Ask me anything outside your MoneyNote account data',
};
