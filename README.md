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
/backend     后端服务
/help        用户使用文档
/docs        开发经验文档
```

Docker 相关文件统一放在**仓库根目录**：`Dockerfile`（生产多阶段构建，镜像内构建前端）、
`docker-compose.yml`、`docker-compose.release.yml`、`entrypoint.sh`、`.dockerignore`。
构建上下文即仓库根目录。

## 快速开始

### Docker 一键运行（推荐）

官方镜像已发布到 Docker Hub（[markliu2018/moneynote](https://hub.docker.com/r/markliu2018/moneynote)），无需克隆源码、无需本地构建，一条命令即可启动：

```bash
docker run -d --name moneynote --restart unless-stopped -p 9096:9096 -v $(pwd)/data:/app/data -v $(pwd)/config:/app/config markliu2018/moneynote:latest
```

访问 `http://localhost:9096`，首次启动进入安装向导，按提示选择数据库（MySQL / PostgreSQL / H2）并完成配置。

- 容器内服务端口为 `9096`；如需换端口，只改 `-p` 左侧的宿主机端口，例如 `-p 9098:9096`。
- `./data` 存放数据库文件，`./config` 存放安装配置，均已挂载到容器外，删除或重建容器不会丢数据，请勿删除这两个目录。
- `--restart unless-stopped` 会让容器开机自启、异常退出后自动拉起；升级时执行 `docker pull markliu2018/moneynote:latest`，再 `docker rm -f moneynote` 后用同一条命令重建即可。
- Windows 环境请把 `$(pwd)` 换成 PowerShell 的 `$PWD` 或 CMD 的 `%cd%`；路径含空格时记得加引号。

### Docker Compose 部署（本地源码构建）

在**仓库根目录**执行：

```bash
docker compose up -d
```

访问 `http://localhost:9096`，首次启动会进入安装向导，按提示选择数据库并完成配置。

数据持久化在根目录的 `./data`（数据库文件）和 `./config`（安装配置）两个目录，已挂载到容器外。

只跑已发布镜像（不本地构建）用 `docker compose -f docker-compose.release.yml up -d`；
本地开发后端直接 `cd backend && ./mvnw spring-boot:run`，不需要 dev compose。

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

## 社区交流

- QQ 群：**639653091**（部署答疑、使用问题、功能建议）

## License

[MIT](backend/LICENSE)
