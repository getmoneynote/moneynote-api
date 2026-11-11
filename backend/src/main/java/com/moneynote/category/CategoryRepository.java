package com.moneynote.category;

import com.moneynote.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends BaseRepository<Category> {

    List<Category> findByBookIdAndType(Long bookId, Integer type);

    /** 用户全部账本下的指定类型分类（跨账本统计时用于叶子 → 根分类归并）。 */
    List<Category> findByBook_UserIdAndType(Long userId, Integer type);

    Optional<Category> findByIdAndBookId(Long id, Long bookId);

    boolean existsByParentId(Long parentId);

    List<Category> findByParentId(Long parentId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
            "WHERE c.book.id = :bookId AND c.type = :type AND c.name = :name " +
            "AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.id = :parentId)")
    boolean existsByBookIdAndTypeAndNameAndParentId(
            @Param("bookId") Long bookId,
            @Param("type") Integer type,
            @Param("name") String name,
            @Param("parentId") Long parentId
    );

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c " +
            "WHERE c.book.id = :bookId AND c.type = :type AND c.name = :name " +
            "AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.id = :parentId) " +
            "AND c.id <> :excludeId")
    boolean existsByBookIdAndTypeAndNameAndParentIdAndIdNot(
            @Param("bookId") Long bookId,
            @Param("type") Integer type,
            @Param("name") String name,
            @Param("parentId") Long parentId,
            @Param("excludeId") Long excludeId
    );

}
