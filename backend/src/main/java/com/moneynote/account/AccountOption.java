package com.moneynote.account;

import lombok.Getter;
import lombok.Setter;

/**
 * 账户下拉选项。比 AccountListItem 轻量：只含下拉所需字段（金额换算、卡号、利率等管理字段不下发）。
 * 币种三件套（id/symbol/decimalPlaces）是流水表单的金额精度控制依赖，不可裁剪。
 */
@Getter @Setter
public class AccountOption {

    private Long value;
    private String label;
    /** 账户类型：1-活期 2-信用 3-资产 4-贷款 */
    private Integer type;
    private Long currencyId;
    private String currencySymbol;
    private Integer currencyDecimalPlaces;

    public String getTitle() {
        return label;
    }

}
