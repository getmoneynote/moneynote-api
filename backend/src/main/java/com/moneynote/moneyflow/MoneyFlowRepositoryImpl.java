package com.moneynote.moneyflow;

import com.moneynote.account.Account;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementation of custom repository methods for MoneyFlow.
 */
@Repository
public class MoneyFlowRepositoryImpl implements MoneyFlowRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> sumAmountGroupedByCurrency(Specification<MoneyFlow> spec, Integer type) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<MoneyFlow> root = query.from(MoneyFlow.class);

        // LEFT JOIN account 及其 currency，避免 account_id 为 null 的流水被 INNER JOIN 排除
        Join<MoneyFlow, Account> accountJoin = root.join("account", JoinType.LEFT);

        // Apply the specification (which includes user, book, etc.)
        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        // Add type condition
        query.where(cb.and(query.getRestriction(),
                cb.equal(root.get("type"), type)));

        // Group by currency and sum amount
        // account 为 null 时回退到账本币种；accountJoin 已 LEFT JOIN，null 行不会被排除
        Expression<Long> currencyIdExpression = cb.selectCase()
                .when(cb.isNotNull(accountJoin),
                        accountJoin.get("currency").get("id"))
                .otherwise(root.get("book").get("currency").get("id"))
                .as(Long.class);

        query.multiselect(
                currencyIdExpression,
                cb.sum(root.get("amount"))
        ).groupBy(currencyIdExpression);

        // Execute query and convert Tuple list to Object[] list
        List<Tuple> resultList = entityManager.createQuery(query).getResultList();
        return resultList.stream()
                .map(tuple -> new Object[]{
                        tuple.get(0, Long.class),
                        tuple.get(1, Long.class) == null ? 0L : tuple.get(1, Long.class)
                })
                .toList();
    }

    @Override
    public List<Object[]> sumAmountGroupedByCategoryAndCurrency(Specification<MoneyFlow> spec, Integer type) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<MoneyFlow> root = query.from(MoneyFlow.class);

        // LEFT JOIN account 及其 currency，避免 account_id 为 null 的流水被 INNER JOIN 排除
        Join<MoneyFlow, Account> accountJoin = root.join("account", JoinType.LEFT);

        // Apply the specification (which includes user, book, etc.)
        Predicate predicate = spec.toPredicate(root, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        // Add type condition
        query.where(cb.and(query.getRestriction(),
                cb.equal(root.get("type"), type)));

        // 金额计量币种：account 为 null 时回退到账本币种（同 sumAmountGroupedByCurrency 口径）
        Expression<Long> currencyIdExpression = cb.selectCase()
                .when(cb.isNotNull(accountJoin),
                        accountJoin.get("currency").get("id"))
                .otherwise(root.get("book").get("currency").get("id"))
                .as(Long.class);

        // 按 分类 + 币种 分组求和：组数 = 实际用到的 (分类, 币种) 组合数，远少于流水条目数；
        // 分组维度只到叶子分类，根分类的归并在 Java 层完成（层级归并无法用一条 SQL 表达）
        query.multiselect(
                root.get("category").get("id"),
                currencyIdExpression,
                cb.sum(root.get("amount"))
        ).groupBy(root.get("category").get("id"), currencyIdExpression);

        // Execute query and convert Tuple list to Object[] list
        List<Tuple> resultList = entityManager.createQuery(query).getResultList();
        return resultList.stream()
                .map(tuple -> new Object[]{
                        tuple.get(0, Long.class),
                        tuple.get(1, Long.class),
                        tuple.get(2, Long.class) == null ? 0L : tuple.get(2, Long.class)
                })
                .toList();
    }
}