package com.moneynote.chat.agent;

import java.time.Instant;

import org.springframework.ai.chat.client.ChatClient;

/**
 * 一次 Agent 执行的环境：请求线程上算好，随执行链一路传下去。
 *
 * <p>存在的<b>唯一目的</b>是给工具用：工具回调运行在流管线线程（Netty event loop /
 * boundedElastic）上，{@code CurrentUserHolder}（ThreadLocal）在那里是 null
 * ——本项目已经踩过这个坑（SSE + ThreadLocal）。所以用户身份必须显式携带，
 * 让工具线程不再回头查 ThreadLocal。
 *
 * <p>{@code client} 也放在这里而不是注入 Agent 构造器：模型是按用户的
 * {@code LlmConfig} 选的，注入即锁死成单例配置。
 *
 * <p>约束：
 * <ul>
 *   <li>不放 Service / Repository——业务依赖由 Agent 构造器注入，Context 只带本次动态数据；</li>
 *   <li>不放可变状态，每次执行 new 一个，避免跨用户 / 跨请求污染；</li>
 *   <li>敏感信息由服务端从登录身份取，绝不相信前端传入的 userId。</li>
 * </ul>
 *
 * @param userId 当前登录用户（服务端解析，非请求体传入）
 * @param bookId 当前账本，接口未就绪时为空（阶段 3 接入 {@code MessageSendForm}）
 * @param now    一次执行内的时间基准，避免多次 {@code Instant.now()} 跨零点不一致
 * @param client 当前用户 LlmConfig 解析出的客户端（请求线程解析，勿在订阅线程重新解析）
 */
public record AgentContext(
        Long userId,
        Long bookId,
        Instant now,
        ChatClient client) {

}
