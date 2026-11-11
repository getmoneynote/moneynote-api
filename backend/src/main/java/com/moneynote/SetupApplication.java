package com.moneynote;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "com.moneynote.setup"   // 只扫这一个包，业务代码碰都不碰
        // 注意:不要在这里用 exclude 排除 JPA/JDBC 自动配置。
        // SetupApplication 与 Application 同包,会被 Application 的组件扫描扫到,
        // 注解里的 exclude 会全局生效,导致正式模式下 Repository 全部不注册。
        // 排除逻辑统一放在 Application.main 里,仅在未配置(setup)模式才设置。
)
public class SetupApplication {



}
