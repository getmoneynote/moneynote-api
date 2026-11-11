import account from './zh-CN/account';
import aiAssistant from './zh-CN/aiAssistant';
import book from './zh-CN/book';
import bookConfig from './zh-CN/bookConfig';
import common from './zh-CN/common';
import currency from './zh-CN/currency';
import llmConfig from './zh-CN/llmConfig';
import menu from './zh-CN/menu';
import moneyFlow from './zh-CN/moneyFlow';
import network from './zh-CN/network';
import pages from './zh-CN/pages';
import reminder from './zh-CN/reminder';
import report from './zh-CN/report';

export default {
  'navBar.lang': '语言',
  ...menu,
  ...network,
  ...pages,
  ...report,
  ...common,
  ...book,
  ...currency,
  ...llmConfig,
  ...account,
  ...aiAssistant,
  ...bookConfig,
  ...reminder,
  ...moneyFlow,
};
