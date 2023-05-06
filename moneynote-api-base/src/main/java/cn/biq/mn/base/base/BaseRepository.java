package cn.biq.mn.base.base;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.math.BigDecimal;
import java.util.List;


/*
https://www.baeldung.com/spring-data-jpa-method-in-all-repositories
https://github.com/pkainulainen/spring-data-jpa-examples/blob/master/custom-method-all-repos/src/main/java/net/petrikainulainen/springdata/jpa/common/BaseRepository.java
 */
// A base repository interface declaring a custom method shared amongst all repositories.
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> extends JpaRepository<T, Integer>, QuerydslPredicateExecutor<T> {

    @Override
    List<T> findAll(Predicate predicate);

    @Override
    List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

    BigDecimal calcSum(Predicate predicate, NumberPath<BigDecimal> column, EntityPathBase<?> entityPathBase);

}
