export const TYPE_CHECKING = 1;
export const TYPE_CREDIT = 2;
export const TYPE_ASSET = 3;
export const TYPE_LOAN = 4;

export const TYPE_ALL = 'all';

export const ACCOUNT_TYPES = [TYPE_CHECKING, TYPE_CREDIT, TYPE_ASSET, TYPE_LOAN];

/** activeType 用 tab 的字符串 key（'all' 或 '1'-'4'）；type 不传后端返回全部 */
export function buildQueryType(type) {
  return type === TYPE_ALL ? undefined : Number(type);
}

export function getAccountTypeLabelKey(type) {
  switch (type) {
    case TYPE_CHECKING:
      return 'account.type.checking';
    case TYPE_CREDIT:
      return 'account.type.credit';
    case TYPE_ASSET:
      return 'account.type.asset';
    case TYPE_LOAN:
      return 'account.type.loan';
    default:
      return '';
  }
}

export function buildTypeOptions(t) {
  return ACCOUNT_TYPES.map((type) => ({
    label: t(getAccountTypeLabelKey(type)),
    value: type,
  }));
}

export function buildTabItems(t) {
  return [
    ...ACCOUNT_TYPES.map((type) => ({
      key: String(type),
      label: t(getAccountTypeLabelKey(type)),
    })),
    { key: TYPE_ALL, label: t('all') },
  ];
}

export function isCreditOrLoan(type) {
  return type === TYPE_CREDIT || type === TYPE_LOAN;
}
