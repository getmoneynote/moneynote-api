# MoneyNote

开源、可自托管的个人 / 小微企业记账应用。数据存储在你自己的服务器上，隐私完全由自己做主。

## 功能特性

- **数据自主可控**：开源自托管，不依赖任何第三方云服务
- **多账户管理**：支持活期、信用、贷款、资产四类账户，真实映射现金流，随时掌握净资产
- **完善的记账体系**：分类、标签、交易对象三者结合，一笔流水可添加多个分类，让报表真正反映消费本质
- **多币种支持**：不同账户可使用不同币种，出国旅行、跨境消费也能记清楚
- **多账本**：按场景建立独立账本，生活、生意、项目互不干扰
- **报表统计**：多维度报表，收支构成一目了然
- **AI 助手**：接入大模型（阿里云 DashScope / Qwen），支持智能记账与问答
- **多数据库支持**：首次启动通过安装向导选择 MySQL / PostgreSQL / H2，无需手动建表

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 21、Spring Boot 4、Spring Data JPA / Hibernate、Spring AI、Maven |
| 前端 | React 19、Ant Design Pro v6、Umi Max、纯 JavaScript（Node ≥ 22） |
| 数据库 | MySQL / PostgreSQL / H2（安装向导中选择，JPA 自动建表） |

## 项目结构

```
/web-pc      前端应用（浏览器端界面）
/backend     后端服务（含 Docker 部署文件）
/help        用户使用文档
/docs        开发经验文档
```

## 快速开始

### Docker 部署（推荐）

```bash
cd backend
docker compose up -d
```

访问 `http://localhost:9098`，首次启动会进入安装向导，按提示选择数据库并完成配置。

数据持久化在 `backend/data`（数据库文件）和 `backend/config`（安装配置）两个目录，已挂载到容器外。

### 本地开发

**后端**（端口 9096，默认使用 H2 数据库）：

```bash
cd backend
./mvnw spring-boot:run
```

**前端**（端口 8000）：

```bash
cd web-pc
npm install
npm start
```

更多后端命令：`./mvnw clean package`（打包）、`./mvnw test`（测试）。

## 文档

- 使用文档见 `help/` 目录：[核心概念](help/conceptions)、[搭建记账体系](help/build.md)、[记账案例](help/cases)
- 开发约定见 `CLAUDE.md`、`web-pc/CLAUDE.md`、`backend/CLAUDE.md`

## License

[MIT](backend/LICENSE)
