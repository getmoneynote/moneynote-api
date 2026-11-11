package com.moneynote.moneyflow;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Custom repository methods for MoneyFlow.
 */
public interface MoneyFlowRepositoryCustom {

    /**
     * Groups money flows by currency and sums the amount in original currency.
     *
     * @param spec the specification to filter money flows
     * @param type the type of money flow (expense, income, transfer)
     * @return list of Object[] where [0] is currencyId (Long) and [1] is sum of amount (Long)
     */
    List<Object[]> sumAmountGroupedByCurrency(Specification<MoneyFlow> spec, Integer type);

    /**
     * Groups money flows by category and currency, summing the amount in original currency.
     *
     * @param spec the specification to filter money flows
     * @param type the type of money flow (expense or income)
     * @return list of Object[] where [0] is categoryId (Long, nullable), [1] is currencyId (Long)
     *         and [2] is sum of amount (Long)
     */
    List<Object[]> sumAmountGroupedByCategoryAndCurrency(Specification<MoneyFlow> spec, Integer type);
}