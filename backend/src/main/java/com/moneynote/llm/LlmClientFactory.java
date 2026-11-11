package com.moneynote.llm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.http.okhttp.SpringAiOpenAiHttpClient;
import org.springframework.stereotype.Component;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientImpl;
import com.openai.core.ClientOptions;

import com.moneynote.exception.BusinessException;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.user.SessionManager;

/**
 * 运行时构建 ChatClient：LLM 配置存数据库（按用户隔离），不走 spring.ai.* 启动期自动配置
 * （application.properties 已用 spring.ai.model.*=none 关闭，避免无 api-key 时启动失败）。
 *
 * Spring AI 2.x 的 OpenAI 集成基于官方 openai-java SDK（OpenAIClientImpl + ClientOptions），
 * 所有 OpenAI 兼容服务（DeepSeek / DashScope 兼容模式 / Moonshot / Ollama 等）共用这一套接入。
 */
@Component
@RequiredArgsConstructor
public class LlmClientFactory {

    /**
     * 按配置 id 缓存；命中时比对配置内容快照，配置被改过时自动重建，无需显式失效。
     *（BaseEntity 没有更新时间字段，内容快照就是最可靠的版本依据）
     */
    private final Map<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    private final LlmConfigRepository llmConfigRepository;

    /**
     * 当前用户激活配置的 ChatClient；未配置或配置已被删除时 422，AI 功能调用方直接可用。
     */
    public ChatClient currentClient() {
        return cachedClient(currentConfig());
    }

    /**
     * 当前用户激活的 LLM 配置；未配置或配置已被删除时 422。
     * 调用方需要配置本体时（如取 model 快照落库）用此方法。
     */
    public LlmConfig currentConfig() {
        Long userId = CurrentUserHolder.get();
        Long configId = SessionManager.getCurrentUser().getCurrentLlmConfigId();
        LlmConfig config = configId == null ? null
                : llmConfigRepository.findByIdAndUserId(configId, userId).orElse(null);
        if (config == null) {
            throw new BusinessException("error.llm.config.not.activated");
        }
        return config;
    }

    /**
     * 按入参临时构建（测试连接用，不进缓存）。
     */
    public ChatClient buildClient(String baseUrl, String apiKey, String model) {
        // ClientOptions 唯一必填项是 httpClient，SpringAiOpenAiHttpClient 全默认值即可
        ClientOptions clientOptions = ClientOptions.builder()
                .httpClient(SpringAiOpenAiHttpClient.builder().build())
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        OpenAIClient openAIClient = new OpenAIClientImpl(clientOptions);
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiClient(openAIClient)
                // 必须同时给 async client：builder 里只设 sync 版时，Spring AI 会对未设置的
                // openAiClientAsync 走默认构建（OpenAiSetup.setupAsyncClient 重新 new ClientOptions），
                // 那条路径拿不到 apiKey，直接抛 "At least one credential source must be specified"
                .openAiClientAsync(openAIClient.async())
                .options(OpenAiChatOptions.builder().model(model).build())
                .build();
        return ChatClient.create(chatModel);
    }

    public void evict(Long configId) {
        cache.remove(configId);
    }

    private ChatClient cachedClient(LlmConfig config) {
        CacheEntry entry = cache.get(config.getId());
        if (entry != null && entry.matches(config)) {
            return entry.client();
        }
        ChatClient client = buildClient(config.getBaseUrl(), config.getApiKey(), config.getModel());
        cache.put(config.getId(),
                new CacheEntry(config.getBaseUrl(), config.getApiKey(), config.getModel(), client));
        return client;
    }

    private record CacheEntry(String baseUrl, String apiKey, String model, ChatClient client) {

        boolean matches(LlmConfig config) {
            return baseUrl.equals(config.getBaseUrl())
                    && apiKey.equals(config.getApiKey())
                    && model.equals(config.getModel());
        }

    }

}
