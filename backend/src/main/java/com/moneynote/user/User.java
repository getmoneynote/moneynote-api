package com.moneynote.user;

import com.moneynote.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "t_user")
@Getter @Setter
public class User extends BaseEntity {

    @Column(length = 16, unique = true, nullable = false)
    private  String username;

    @Column(length = 128, nullable = false)
    private String password;

    @Column(length = 256)
    private String photo;

    // 这里有三个方案：manytoone，onetoone，currentBookId，可以分析优劣
    @Column(name = "current_book_id", nullable = true)
    private Long currentBookId;

    // 统计资产的时候使用
    @Column(name = "base_currency_id", nullable = true)
    private Long baseCurrencyId;

    // 当前使用的 AI 配置（t_llm_config.id），与 currentBookId 同构
    @Column(name = "current_llm_config_id", nullable = true)
    private Long currentLlmConfigId;

}
