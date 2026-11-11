## 项目概览

MoneyNote 后端接口项目

## 技术栈

- **技术栈**：Java21、Spring Boot 4.1.0、Spring Data JPA / Hibernate、Maven Wrapper 3.3.4
- **数据库**：首次启动时通过配置向导选择
- **前端**：预构建的静态资源，提交在 `src/main/resources/public/` 下（umi.js、umi.css、index.html）
- **AI 集成**：Spring AI OpenAI starter 已配置阿里云 DashScope（Qwen 模型）

### 数据库
- **数据库类型不固定**：应用首次启动时由用户在安装向导中选择数据库类型（如 MySQL / PostgreSQL / H2 等），选择结果写入配置后持久化。
- 因此后端持久层需保持**多数据库兼容**：避免使用数据库方言专属的 SQL 语法，JPA 层的实现应能适配所有支持的数据库类型
- 建表方式：JPA 自动建表
- 首次启动安装向导的代码位置：package com.moneynote.setup;生成的数据库配置目录：backend/config/db-config.properties
- 本地开发时默认使用h2数据库

## 后端驱动开发
- 以后端为开发主导，先实现后端 Controller / Service / Entity
- 前端同步更新 `src/api/` 下的请求函数和类型定义
- 后端是数据模型的唯一事实来源，前端类型以后端返回结构为准

## 项目结构

参考 `docs/file-structure.md`

## 编码规范

参考 `docs/code-convention.md`

## 常用命令

```bash
./mvnw clean package        # 打包
./mvnw spring-boot:run      # 本地运行
./mvnw test                 # 跑测试
```

## 当前开发状态