/**
 * 金额换算工具函数
 *
 * 项目里金额以"最小货币单位"存储（如人民币是"分"、日元是"円"），
 * 用整数 Long 保存，避免浮点数精度问题。展示时换算为正常小数。
 *
 * 换算关系：显示值 = 最小单位值 / 10^decimalPlaces
 * 例如：人民币 decimalPlaces=2，1050 分 → 10.50 元
 *
 * 契约（三层职责）：
 * - 币种小数位数是决定金额解释方式的业务元数据，不是显示偏好。
 *   缺失或非法直接抛错，绝不默认兜底——否则零小数位币种（如 VND）
 *   的金额会被静默错算 100 倍。
 * - 小数位数合法范围 0~4，与后端 CurrencyCreateForm 的 @Range(0,4) 对齐。
 * - 最小单位值必须为整数（数据库按 Long 存储），非整数说明数据异常。
 * - 「用户允许为空的输入」返回 undefined/'' 空态；「数据异常」抛错，
 *   两者不可混同，禁止把异常数据伪装成空值或 0。
 */

/** 币种小数位数合法上限，与后端 CurrencyCreateForm 的 @Range(min=0, max=4) 对齐 */
const MAX_DECIMAL_PLACES = 4;

/**
 * 校验并返回小数位数。缺失、非整数、越界一律抛错，不做默认兜底。
 *
 * 注意 0 是合法值（日元/越南盾等零小数位币种），不能用 falsy 判断。
 *
 * @param {number} decimalPlaces - 币种的小数位数（必填，0~4 整数）
 * @returns {number} 校验通过的小数位数
 * @throws {Error} 缺失或非法时抛出，属契约破坏，应尽早暴露
 *
 * @example
 * getDecimalPlaces(0)    // 0，合法
 * getDecimalPlaces(2)    // 2，合法
 * getDecimalPlaces(2.5)  // Error
 * getDecimalPlaces(-1)   // Error
 * getDecimalPlaces(undefined) // Error
 */
export function getDecimalPlaces(decimalPlaces) {
  if (
    !Number.isInteger(decimalPlaces) ||
    decimalPlaces < 0 ||
    decimalPlaces > MAX_DECIMAL_PLACES
  ) {
    throw new Error(`无效的币种小数位数: ${decimalPlaces}`);
  }
  return decimalPlaces;
}

/**
 * 最小单位值 → 显示用数字（Number 类型）。
 *
 * 用于表单回填：把数据库里存的"分"换算成元，赋给数字输入框。
 * 输入为空（undefined/null/''）时返回 undefined，让表单字段保持空态。
 * 输入非空但非法（非有限数、非整数）时抛错——那是数据异常，不是空态。
 *
 * @param {number|string|undefined|null} value - 最小单位整数值（如 1050 表示 10.50 元）
 * @param {number} decimalPlaces - 币种小数位数（必填，见 getDecimalPlaces）
 * @returns {number|undefined} 换算后的正常小数；输入为空则返回 undefined
 * @throws {Error} 非空输入非法、或小数位数缺失/非法时抛出
 *
 * @example
 * minorToDisplay(1050, 2)   // 10.5
 * minorToDisplay(0, 2)      // 0
 * minorToDisplay(null, 2)   // undefined（空态）
 * minorToDisplay(10.5, 2)   // Error（最小单位必须为整数）
 */
export function minorToDisplay(value, decimalPlaces) {
  if (value === undefined || value === null || value === '') return undefined;
  const num = Number(value);
  if (!Number.isFinite(num)) {
    throw new Error(`无效的最小单位金额: ${value}`);
  }
  if (!Number.isInteger(num)) {
    throw new Error(`最小单位金额必须为整数: ${value}`);
  }
  const places = getDecimalPlaces(decimalPlaces);
  return num / 10 ** places;
}

/**
 * 显示用数字 → 最小单位整数值。
 *
 * 用于表单提交：把用户输入的元换算成分，传给后端接口。
 * Math.round 同时承担浮点误差修正（10.5 * 100 = 1050.0000000000001）；
 * 超出币种精度的输入由表单层 InputNumber 的 precision 拦截，这里不重复校验。
 * 输入为空时返回 undefined；输入非空但非法时抛错。
 *
 * @param {number|string|undefined|null} value - 显示值（如 10.5 表示 10.5 元）
 * @param {number} decimalPlaces - 币种小数位数（必填，见 getDecimalPlaces）
 * @returns {number|undefined} 换算后的最小单位整数；输入为空则返回 undefined
 * @throws {Error} 非空输入非法、或小数位数缺失/非法时抛出
 *
 * @example
 * displayToMinor(10.5, 2)    // 1050
 * displayToMinor(0, 2)       // 0
 * displayToMinor('', 2)      // undefined（空态）
 */
export function displayToMinor(value, decimalPlaces) {
  if (value === undefined || value === null || value === '') return undefined;
  const num = Number(value);
  if (!Number.isFinite(num)) {
    throw new Error(`无效的金额输入: ${value}`);
  }
  const places = getDecimalPlaces(decimalPlaces);
  return Math.round(num * 10 ** places);
}

/**
 * 最小单位值 → 固定小数位字符串。
 *
 * 保留全部小数位（如 10.00），适合需要严格对齐的场景。
 * 输入为空或非有限数时返回空字符串，绝不把 null/'' 强转成 0
 * （Number(null) === 0，直接 Number 会把"无数据"伪装成 "0.00"）。
 *
 * @param {number|string|undefined|null} value - 最小单位整数值
 * @param {number} decimalPlaces - 币种小数位数（必填，见 getDecimalPlaces）
 * @returns {string} 固定小数位的金额字符串，输入为空/非法则返回 ''
 * @throws {Error} 小数位数缺失/非法时抛出
 *
 * @example
 * formatAmount(1050, 2)  // "10.50"
 * formatAmount(0, 2)     // "0.00"
 * formatAmount(null, 2)  // ""（空态，不是 "0.00"）
 */
export function formatAmount(value, decimalPlaces) {
  if (value === undefined || value === null || value === '') return '';
  const num = Number(value);
  if (!Number.isFinite(num)) return '';
  const places = getDecimalPlaces(decimalPlaces);
  return (num / 10 ** places).toFixed(places);
}

/**
 * 最小单位值 → 去掉末尾无效零的字符串。
 *
 * 末尾零规则：整数去掉 .00，小数去掉末尾无效零（10.50 → 10.5，10.00 → 10）。
 * 适合表格列、汇总统计等展示场景，比固定小数位更清爽。
 *
 * 注意：金额数值为零（"0.00" → "0"）与金额数据缺失（''）是两回事，
 * 调用方需用 str === '' 判空，不能用 !str（会把 "0" 误判为空）。
 *
 * @param {number|string|undefined|null} value - 最小单位整数值
 * @param {number} decimalPlaces - 币种小数位数（必填，见 getDecimalPlaces）
 * @returns {string} 去末尾零的金额字符串，输入为空/非法则返回 ''
 * @throws {Error} 小数位数缺失/非法时抛出
 *
 * @example
 * formatAmountDisplay(1050, 2)  // "10.5"
 * formatAmountDisplay(1000, 2)  // "10"
 * formatAmountDisplay(1055, 2)  // "10.55"
 * formatAmountDisplay(null, 2)  // ""
 */
export function formatAmountDisplay(value, decimalPlaces) {
  const str = formatAmount(value, decimalPlaces);
  if (str === '') return '';
  return str.replace(/\.0+$/, '').replace(/(\.\d*?[1-9])0+$/, '$1');
}

/**
 * 最小单位值 → 带货币符号的展示字符串。
 *
 * 在 formatAmountDisplay 基础上拼上货币符号前缀，
 * 是表格余额列最常用的格式化方式。
 *
 * @param {number|string|undefined|null} value - 最小单位整数值
 * @param {number} decimalPlaces - 币种小数位数（必填，见 getDecimalPlaces）
 * @param {string|undefined|null} symbol - 货币符号（如 "¥"、"$"），为空则只返回数字
 * @returns {string} 带符号的金额字符串，输入为空/非法则返回 ''
 * @throws {Error} 小数位数缺失/非法时抛出
 *
 * @example
 * formatMoney(1050, 2, '¥')   // "¥10.5"
 * formatMoney(1000, 2, '$')   // "$10"
 * formatMoney(null, 2, '¥')   // ""
 * formatMoney(1050, 2)        // "10.5"
 */
export function formatMoney(value, decimalPlaces, symbol) {
  const amountStr = formatAmountDisplay(value, decimalPlaces);
  if (amountStr === '') return '';
  return symbol ? `${symbol}${amountStr}` : amountStr;
}
