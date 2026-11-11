package com.moneynote.chat.agent;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.moneynote.exception.ResourceInvalidException;

/**
 * Agent 注册中心：Spring 容器里的所有 {@link Agent} Bean 启动期自动收集建索引，
 * 只负责「注册、按 code 查找、校验外部传入的 code」，不参与执行、不参与任何意图决策。
 *
 * <p><b>注册方式：Spring 集合注入，没有显式注册动作。</b>构造器声明 {@code List<Agent>}
 * 参数后，Spring 按类型把容器中所有 {@link Agent} 实现 Bean 收集成集合传入，再以
 * {@link Agent#getCode()} 为 key 建立 {@code code -> 实例} 索引。建索引发生在启动期
 * （单例 Bean 实例化时），此后字段 private final 且无写入口，运行期只读，
 * 不支持动态注册/热插拔。
 *
 * <p><b>谁会被注册：</b>所有实现了 {@link Agent} 接口的 Spring Bean。当前是四个
 * {@code @Component} 业务 Agent，各在自己的包内（{@code agent.bookkeeping} /
 * {@code agent.account} / {@code agent.moneyflow} / {@code agent.general}），
 * 均继承 {@code AbstractLlmAgent}：
 * BOOKKEEPING / ACCOUNT_ANALYSIS / MONEY_FLOW_ANALYSIS / GENERAL_CHAT。
 * 其中 GENERAL_CHAT 是<b>独立的用户可选 Agent</b>（负责非账务问题），不是任何兜底目标。
 * code 取自各类的 {@code public static final String CODE}；system prompt 在构造期
 * 由 {@code AbstractLlmAgent} 从 classpath 加载，资源缺失立即启动失败（fail-fast）。
 *
 * <p><b>新增 Agent：</b>在自己的包里加一个 {@code @Component} 实现类（或继承
 * {@code AbstractLlmAgent}）、给全局唯一 CODE、放好提示词文件，并在前端
 * {@code config/routes.js} + 菜单 i18n 里加一条，本类与 {@code AgentOrchestrator}
 * 都无需改动。code 重复时 toMap 会在启动期抛 {@code Duplicate key}，属有意的 fail-fast。
 *
 * <p><b>运行期消费方：</b>① {@link #requireRegistered(String)} 在 Controller 入口校验
 * 菜单传来的 code；② {@code AgentOrchestrator.dispatch()} 用 {@link #get(String)}
 * 取实例并调用 {@code stream()}（冷流，订阅才发起模型调用）。查找 O(1)，
 * 生成阶段本类完全不参与。
 *
 * <p><b>边界：</b>只读发布靠 final 字段的安全发布语义，读路径无锁、无 volatile；
 * 集合注入会在启动期实例化所有 Agent Bean（目前四个构造都很轻，若将来某个构造很重
 * 需改用 {@code ObjectProvider} / {@code Supplier} 懒实例化）；不支持按用户/账本过滤。
 */
@Component
public class AgentRegistry {

    /** code -> Agent 实例；启动期一次性建好，之后只读（final 安全发布，无需同步） */
    private final Map<String, Agent> agents;

    public AgentRegistry(List<Agent> agentList) {
        this.agents = agentList.stream()
                .collect(Collectors.toMap(Agent::getCode, Function.identity()));
    }

    /**
     * 校验外部（菜单/请求路径）传入的 agentCode：必须是已注册的 code，
     * 否则属于客户端传参错误，映射为 400 与标准错误体。
     * 所有对外入口（list / send / clear）都先调本方法，且必须在流开始/写库之前。
     */
    public String requireRegistered(String code) {
        if (!contains(code)) {
            throw new ResourceInvalidException("error.chat.agent.unknown");
        }
        return code;
    }

    /**
     * 按 code 查找；code 恒来自已校验的菜单参数或系统内部，
     * 查不到属编程错误而非可恢复的业务异常，直接抛出。
     */
    public Agent get(String code) {
        Agent agent = code == null ? null : agents.get(code);
        if (agent == null) {
            throw new IllegalStateException("未注册的 Agent: " + code);
        }
        return agent;
    }

    /** 白名单校验（含 null 安全）：{@link #requireRegistered(String)} 的唯一判定依据 */
    public boolean contains(String code) {
        return code != null && agents.containsKey(code);
    }

}
