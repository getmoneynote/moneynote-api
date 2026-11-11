package com.moneynote.chat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.moneynote.base.BaseRepository;
import com.moneynote.user.User;

@Repository
public interface MessageRepository extends BaseRepository<Message> {

    /**
     * 读取一条固定会话（user_id + agent_code）的<b>全量</b>历史，按时间升序。
     *
     * <p><b>【后期扩展点 · 上下文窗口】</b>当前不设窗口：一个 Agent 一条永久时间线，
     * 消息只增不减，历史越长模型输入越大。将来改为
     * {@code findTopNByUser_IdAndAgentCodeOrderByCreatedAtDesc} 后必须把结果反转为升序，
     * 并保证截断后首条是 USER（截断在 ASSISTANT 会让部分 OpenAI 兼容端点报 400）；
     * 窗口口径要与 {@code MessageService.list} 的前端展示一致。窗口截断的实现位置在
     * {@code ChatTransactionService.initialize}（排除 GENERATING 占位之后）。
     */
    List<Message> findByUser_IdAndAgentCodeOrderByCreatedAtAsc(Long userId, String agentCode);

    /**
     * 表单块提交用：按「消息 id + 用户 + agentCode」取消息并加悲观写锁（SELECT ... FOR UPDATE）。
     *
     * <p>为什么用悲观锁而不是 {@code @Version} 乐观锁：{@code BaseEntity} 没有版本列，
     * 而提交动作要「校验块仍是 pending → 记账 → 回写 done」，是跨行的读改写；
     * 双击提交/两个标签页同时提交时，后到的事务阻塞到前一个提交完成，再读到 done 被拒，
     * 不会记出两笔账。锁粒度就是单条消息行，不影响其它消息的读写。
     *
     * <p>必须带 userId 与 agentCode：块 id 只在消息内唯一，不能只按 id 取。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Message m WHERE m.id = :id AND m.user.id = :userId AND m.agentCode = :agentCode")
    Optional<Message> findForUpdate(@Param("id") Long id,
                                    @Param("userId") Long userId,
                                    @Param("agentCode") String agentCode);

    /**
     * 清空一条固定会话（「清空记录」功能）：必须同时按 userId 与 agentCode 过滤，
     * 绝不能退化成「按 agentCode 全表删」，否则会删掉别的用户的时间线。
     * 用 JPQL 批量删除而非派生 deleteBy（逐条查出来再删），一次往返完成。
     */
    @Modifying
    // 直接比较 User 引用（Hibernate 用外键列），避免在批量删除里解引用 m.user.id 的隐式关联
    @Query("DELETE FROM Message m WHERE m.user = :user AND m.agentCode = :agentCode")
    int deletePartition(@Param("user") User user, @Param("agentCode") String agentCode);

    /**
     * 启动清扫用：把 cutoff 之前创建、仍停在 fromStatus 的消息批量置为 toStatus。
     * 用 JPQL 批量更新而非逐条 save，多数据库兼容且一次往返完成。
     * 与会话/Agent 分区无关：残留的 GENERATING 消息在任何分区里都要收敛。
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :toStatus " +
            "WHERE m.status = :fromStatus AND m.createdAt < :cutoff")
    int markStatusBefore(@Param("fromStatus") int fromStatus,
                         @Param("toStatus") int toStatus,
                         @Param("cutoff") Instant cutoff);

}
