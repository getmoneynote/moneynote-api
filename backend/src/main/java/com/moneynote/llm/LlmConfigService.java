package com.moneynote.llm;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.moneynote.exception.BusinessException;
import com.moneynote.exception.ResourceDuplicateException;
import com.moneynote.exception.ResourceNotFoundException;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.user.SessionManager;
import com.moneynote.user.User;
import com.moneynote.user.UserRepository;
import com.moneynote.user.UserService;
import com.moneynote.user.UserSessionVo;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigService {

    private final LlmConfigRepository llmConfigRepository;
    private final LlmClientFactory llmClientFactory;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public void create(LlmConfigCreateForm form) {
        Long userId = CurrentUserHolder.get();
        if (llmConfigRepository.existsByUserIdAndName(userId, form.getName())) {
            throw new ResourceDuplicateException("resource.llmConfig", form.getName());
        }
        LlmConfig entity = new LlmConfig();
        entity.setUser(userRepository.getReferenceById(userId));
        entity.setName(form.getName());
        entity.setBaseUrl(form.getBaseUrl());
        entity.setApiKey(form.getApiKey());
        entity.setModel(form.getModel());
        entity.setNotes(form.getNotes());
        llmConfigRepository.save(entity);
    }

    @Transactional
    public void update(Long id, LlmConfigUpdateForm form) {
        Long userId = CurrentUserHolder.get();
        LlmConfig entity = llmConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.llmConfig"));
        if (StringUtils.hasText(form.getName()) && !form.getName().equals(entity.getName())) {
            if (llmConfigRepository.existsByUserIdAndName(userId, form.getName())) {
                throw new ResourceDuplicateException("resource.llmConfig", form.getName());
            }
            entity.setName(form.getName());
        }
        if (StringUtils.hasText(form.getBaseUrl())) {
            entity.setBaseUrl(form.getBaseUrl());
        }
        // apiKey 留空表示不修改（前端只持有掩码，不能原样写回）
        if (StringUtils.hasText(form.getApiKey())) {
            entity.setApiKey(form.getApiKey());
        }
        if (StringUtils.hasText(form.getModel())) {
            entity.setModel(form.getModel());
        }
        // notes 例外口径：null 不修改、空串清空（前端编辑弹窗总是回填该字段，与 Book.update 同口径）
        if (form.getNotes() != null) {
            entity.setNotes(form.getNotes());
        }
        // entity 为持久态对象，事务提交时自动 flush；缓存按内容快照比对自动失效
    }

    @Transactional
    public void delete(Long id) {
        Long userId = CurrentUserHolder.get();
        llmConfigRepository.findByIdAndUserId(id, userId).ifPresent(entity -> {
            // 不允许删除正在使用的配置，需先切换到其他配置（与 CurrencyService 禁删本位币同口径）
            if (Objects.equals(id, SessionManager.getCurrentUser().getCurrentLlmConfigId())) {
                throw new BusinessException("error.llm.config.remove.current");
            }
            llmConfigRepository.delete(entity);
            llmClientFactory.evict(id);
        });
    }

    /**
     * 设为当前使用的配置。与 setBaseCurrency 同口径：返回最新会话 VO，
     * 前端直接更新全局用户态，无需二次拉取。
     */
    @Transactional
    public UserSessionVo activate(Long id) {
        Long userId = CurrentUserHolder.get();
        if (llmConfigRepository.findByIdAndUserId(id, userId).isEmpty()) {
            throw new ResourceNotFoundException("resource.llmConfig");
        }
        User user = SessionManager.getCurrentUser();
        user.setCurrentLlmConfigId(id);
        userRepository.save(user);
        return userService.buildUserSessionVo(user);
    }

    @Transactional(readOnly = true)
    public List<LlmConfigListItem> list() {
        Long userId = CurrentUserHolder.get();
        Long currentConfigId = SessionManager.getCurrentUser().getCurrentLlmConfigId();
        // 排序保证跨数据库稳定（无 ORDER BY 时各数据库返回顺序不定）
        return llmConfigRepository.findByUserId(userId, Sort.by(Sort.Order.asc("id")))
                .stream()
                .map(item -> {
                    LlmConfigListItem listItem = new LlmConfigListItem();
                    listItem.setId(item.getId());
                    listItem.setName(item.getName());
                    listItem.setBaseUrl(item.getBaseUrl());
                    listItem.setModel(item.getModel());
                    // 完整 key 永不出库，前端展示和编辑回填都用掩码
                    listItem.setMaskedApiKey(maskApiKey(item.getApiKey()));
                    listItem.setNotes(item.getNotes());
                    listItem.setIsCurrent(Objects.equals(item.getId(), currentConfigId));
                    return listItem;
                })
                .toList();
    }

    /**
     * 不保存直接验证连通性：一次真实 chat 调用同时覆盖 baseUrl 可达、key 鉴权、model 存在三类问题。
     * apiKey 留空时要求带 id，取库里的 key 测（编辑弹窗里不改 key 也能测）。
     */
    @Transactional(readOnly = true)
    public void test(LlmConfigTestForm form) {
        String apiKey = form.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            if (form.getId() == null) {
                throw new BusinessException("error.validation.failed");
            }
            Long userId = CurrentUserHolder.get();
            apiKey = llmConfigRepository.findByIdAndUserId(form.getId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("resource.llmConfig"))
                    .getApiKey();
        }
        try {
            llmClientFactory.buildClient(form.getBaseUrl(), apiKey, form.getModel())
                    .prompt("ping")
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("LLM config test failed: {}", e.getMessage());
            throw new BusinessException("error.llm.config.test.failed");
        }
    }

    /** 掩码只保留后 4 位；短 key 全掩。 */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }

}
