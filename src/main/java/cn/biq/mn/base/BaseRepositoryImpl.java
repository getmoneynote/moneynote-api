package cn.biq.mn.base;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public class BaseRepositoryImpl<T extends BaseEntity> extends QuerydslJpaRepository<T, Integer> implements BaseRepository<T> {

    private final JPAQueryFactory jpaQueryFactory;

    public BaseRepositoryImpl(JpaEntityInformation<T, Integer> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public BigDecimal calcSum(Predicate predicate, NumberPath<BigDecimal> column, EntityPathBase<?> entityPathBase) {
        JPAQuery<BigDecimal> result = jpaQueryFactory.select(column.sum()).from(entityPathBase).where(predicate);
        return Optional.ofNullable(result.fetch().get(0)).orElse(BigDecimal.ZERO);
    }

}
