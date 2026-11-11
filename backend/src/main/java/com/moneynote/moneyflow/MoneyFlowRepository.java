package com.moneynote.moneyflow;

import com.moneynote.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MoneyFlowRepository extends BaseRepository<MoneyFlow>, JpaSpecificationExecutor<MoneyFlow>, MoneyFlowRepositoryCustom {

    /** 按 id + 所属用户取流水（流水无直接 user 关联，经 book.user 收敛）。 */
    @Query("select mf from MoneyFlow mf where mf.id = :id and mf.book.user.id = :userId")
    Optional<MoneyFlow> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

}