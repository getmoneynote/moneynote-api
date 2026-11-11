package com.moneynote.chat;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import com.moneynote.base.BaseEntity;
import com.moneynote.user.User;

/**
 * 消息（一条 Agent 固定时间线里的一行）。
 *
 * <p><b>分区模型：</b>不再有 Conversation 实体。{@code (user_id, agent_code)} 唯一确定
 * 一条「固定会话」——每个用户在每个 Agent 下只有一条时间线，按 createdAt 升序即对话历史。
 * agentCode 因而是<b>分区键</b>（not null），而不再是意图路由的结果；它由菜单选定，
 * 随请求路径 {@code /chat/agents/{agentCode}/messages} 传入。清空该时间线走同一路径的
 * DELETE 方法。系统提示词在请求时注入，不落库为 SYSTEM 消息。
 *
 * <p><b>【后期扩展点 · 上下文窗口】</b>固定会话里消息只增不减，当前历史是<b>全量</b>加载
 * （{@link MessageRepository#findByUser_IdAndAgentCodeOrderByCreatedAtAsc}）并全量喂给模型，
 * token 成本与首字延迟会随使用线性增长；这是已知且本版刻意不处理的技术债。将来要加窗口时：
 * <ol>
 *   <li>Repository 改为 {@code findTopNBy...OrderByCreatedAtDesc} 再把结果反转为升序；</li>
 *   <li>截断后必须保证首条是 USER（截断在 ASSISTANT 会让部分 OpenAI 兼容端点报 400）；</li>
 *   <li>必须先排除 GENERATING 占位（见 {@code ChatTransactionService.initialize}）；</li>
 *   <li>模型窗口与前端展示（{@code MessageService.list}）口径要一致，否则用户看到的
 *       历史与模型看到的不一致，前端也应同步分页而不是渲染全量气泡。</li>
 * </ol>
 * 三处实现位置都留了同样的注释，改的时候请一起改。
 *
 * content 用 TEXT/CLOB 存，适配长回复。
 */
@Entity
@Table(
        name = "t_message",
        indexes = {
                @Index(name = "idx_msg_agent_time", columnList = "user_id, agent_code, created_at")
        }
)
@Getter @Setter
public class Message extends BaseEntity {

    public static final int ROLE_USER = 1;
    public static final int ROLE_ASSISTANT = 2;

    /**
     * 消息生成状态（分阶段持久化），数字常量约定：
     * <ul>
     *   <li>1 = {@link #STATUS_GENERATING}：已创建、AI 生成中（发送时先落此状态拿 messageId）</li>
     *   <li>2 = {@link #STATUS_COMPLETED}：正常完成（唯一成功终态）</li>
     *   <li>3 = {@link #STATUS_ERROR}：流式生成出错（终态，保留已生成部分）</li>
     *   <li>4 = {@link #STATUS_STOPPED}：用户中止（终态，保留已生成部分）</li>
     * </ul>
     * 唯一终态原则：一次生成只允许落入 COMPLETED / ERROR / STOPPED 之一。
     * 历史数据为 null（无分阶段持久化前的旧消息，前端视为已完成）。
     * 注意：不要把 COMPLETED 定义为模型已经生成完毕就行，而应该定义为最终结果已经成功持久化。
     */
    public static final int STATUS_GENERATING = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_ERROR = 3;
    public static final int STATUS_STOPPED = 4;

    /** 归属用户：会话分区的第一维，全部查询/删除都必须带 userId，不能只按 agentCode 过滤 */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Agent Code（引用 chat.agent 包注册表，如 BOOKKEEPING）：会话分区的第二维。
     * 菜单选定后即确定，USER 与 ASSISTANT 消息取同一个值；一个分区内不会混入别的 Agent。
     */
    @Column(name = "agent_code", length = 32, nullable = false)
    private String agentCode;

    @Column(nullable = false)
    private Integer role; // 取值见 ROLE_USER / ROLE_ASSISTANT

    /** 消息正文，可能较长。模型视角的原始文本：模型写的、前端 Markdown 渲染的，
     * 下一轮全量回灌的内容全部在此；不含任何展示块数据。
     */
    @Lob
    @Column(nullable = false)
    private String content;

    /**
     * 展示块列表 JSON（TEXT 存，多数据库兼容，不用方言 JSON 类型）：前端视角的自定义内容
     * （图表/表单等），与 {@code content} 解耦。null 表示无块。
     *
     * <p>两条读者的分工：{@code data} 供前端渲染，{@code summary} 供模型阅读——历史回读时
     * {@link ChatTransactionService#initialize} 会把摘要拼进模型输入，正文与块数据
     * 不互相污染。
     */
    @Lob
    @Column(nullable = true)
    private String parts;

    /**
     * 思考内容（推理模型的 reasoning_content，如 deepseek-reasoner）。
     * 推理模型生成时先吐思考再吐正文；普通模型无此数据，历史数据为 null。
     */
    @Lob
    @Column(nullable = true)
    private String reasoningContent;

    /** 落库时使用的模型快照（展示用，如 deepseek-v4.1-flash） */
    @Column(length = 128, nullable = true)
    private String model;

    /** token 用量（可空） */
    @Column(name = "tokens", nullable = true)
    private Long tokens;

    /**
     * 生成状态（分阶段持久化），取值见 {@link #STATUS_GENERATING} 等常量注释。
     * 发送时先落 GENERATING，流结束按信号更新为 COMPLETED / STOPPED / ERROR。
     * 历史数据为 null（前端按已完成的旧消息处理）。
     */
    @Column(name = "status", nullable = true)
    private Integer status;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

}
