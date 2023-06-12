package cn.biq.mn.user.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import cn.biq.mn.base.utils.MessageSourceUtil;
import cn.biq.mn.user.account.AccountType;
import cn.biq.mn.user.balanceflow.FlowType;

@Component
@RequiredArgsConstructor
public class EnumUtils {

    private final MessageSourceUtil messageSourceUtil;

    public String translateFlowType(FlowType value) {
        return switch (value) {
            case EXPENSE -> messageSourceUtil.getMessage("label.expense");
            case INCOME -> messageSourceUtil.getMessage("label.income");
            case TRANSFER -> messageSourceUtil.getMessage("label.transfer");
            case ADJUST -> messageSourceUtil.getMessage("label.adjust.balance");
        };
    }

    public String translateAccountType(AccountType value) {
        return switch (value) {
            case CHECKING -> messageSourceUtil.getMessage("account.type.checking");
            case CREDIT -> messageSourceUtil.getMessage("account.type.credit");
            case ASSET -> messageSourceUtil.getMessage("account.type.asset");
            case DEBT -> messageSourceUtil.getMessage("account.type.debt");
        };
    }

    public String translateRoleType(Integer value) {
        return switch (value) {
            case 1 -> messageSourceUtil.getMessage("role.type.own");
            case 2 -> messageSourceUtil.getMessage("role.type.operator");
            case 3 -> messageSourceUtil.getMessage("role.type.guest");
            case 4 -> messageSourceUtil.getMessage("role.type.invited");
            default -> "未知";
        };
    }

    public String translateNoteDayRepeatType(int type) {
        switch (type) {
            case 1:
                return messageSourceUtil.getMessage("day");
            case 2:
                return messageSourceUtil.getMessage("month");
            case 3:
                return messageSourceUtil.getMessage("year");
        }
        return "Unknown";
    }



}
