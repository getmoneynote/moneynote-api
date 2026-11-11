package com.moneynote.llm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

import com.moneynote.base.BaseEntity;
import com.moneynote.user.User;

@Entity
@Table(
        name = "t_llm_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"})
)
@Getter @Setter
public class LlmConfig extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 配置名称（用户自定义，如"我的 DeepSeek"），按用户唯一
     */
    @Column(length = 64, nullable = false)
    private String name;

    /**
     * OpenAI 兼容端点，如 https://api.openai.com/v1
     */
    @Column(length = 256, nullable = false)
    private String baseUrl;

    /**
     * 明文存储（私有部署，与用户密码同口径）；对外接口永远只回传掩码
     */
    @Column(length = 256, nullable = false)
    private String apiKey;

    @Column(length = 128, nullable = false)
    private String model;

    /**
     * 备注（用户自定义，如用途说明），可空
     */
    @Column(length = 1024, nullable = true)
    private String notes;

}
