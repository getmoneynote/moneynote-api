export const TYPE_EXPENSE = 1;
export const TYPE_INCOME = 2;
export const TYPE_TRANSFER = 3;
export const TYPE_ADJUST = 4;

export const MONEY_FLOW_TYPES = [TYPE_EXPENSE, TYPE_INCOME, TYPE_TRANSFER, TYPE_ADJUST];

export function getTypeLabelKey(type) {
  switch (type) {
    case TYPE_EXPENSE:
      return 'moneyFlow.type.expense';
    case TYPE_INCOME:
      return 'moneyFlow.type.income';
    case TYPE_TRANSFER:
      return 'moneyFlow.type.transfer';
    case TYPE_ADJUST:
      return 'moneyFlow.type.adjust';
    default:
      return '';
  }
}

export function buildTypeOptions(t) {
  return MONEY_FLOW_TYPES.map((type) => ({
    label: t(getTypeLabelKey(type)),
    value: type,
  }));
}