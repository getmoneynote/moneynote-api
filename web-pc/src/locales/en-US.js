import account from './en-US/account';
import aiAssistant from './en-US/aiAssistant';
import book from './en-US/book';
import bookConfig from './en-US/bookConfig';
import common from './en-US/common';
import currency from './en-US/currency';
import llmConfig from './en-US/llmConfig';
import menu from './en-US/menu';
import moneyFlow from './en-US/moneyFlow';
import network from './en-US/network';
import pages from './en-US/pages';
import reminder from './en-US/reminder';
import report from './en-US/report';

export default {
  'navBar.lang': 'Languages',
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
