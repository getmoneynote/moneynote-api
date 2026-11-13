# chat 模块架构分析与待办（交接文档）

> 生成时间：2026-11-13。基于对 `backend/src/main/java/com/moneynote/chat/` 全量代码的走读。

## 一、架构总览

整体是清晰的五层单向结构，一次生成的生命周期：

**Controller → MessageService（装配）→ ChatTransactionService（阶段一短事务）→ AgentOrchestrator → Agent → GenerationPipeline（阶段二/三流式编排）→ SseTransport**

```
chat/                    入口与持久化
  MessageController          REST/SSE 端点（list / send / clear / submitBlock）
  MessageService             入口装配：白名单校验→LLM 配置→初始化→交给 pipeline
  ChatTransactionService     所有 DB 短事务的唯一持有者（initialize/complete/fail/stop/clear/sweep）
  Message/MessageRepository  实体与查询
chat/stream/             流式生命周期（全部 Reactor 细节）
  GenerationPipeline         start→事件流→终态落库→finish，及 error/cancel 两条终态
  MessageDraft               本次生成缓冲（不变量：缓冲 == 已发出的事件）
  TerminalFinalizer          CAS 保证唯一终态，分发三种终态
chat/event/              协议层
  ChatEventFactory           唯一构造点 + seq 分配（per-generation new）
  ChatEventConverter         AgentEvent → ChatEvent 纯转换
  SseTransport               ChatEvent → SSE 帧
chat/agent/              领域层
  Agent 接口（R1：只吐 AgentEvent）+ AgentRegistry（启动期索引）+ AgentOrchestrator（按 code 分发）
  AbstractLlmAgent           骨架：prompt 加载、流式调用、chunk→事件
  ChatResponseParser/ReasoningNormalizer/ChatResponseProcessor  Spring AI 适配层（契约 R2，只此一份）
  bookkeeping/account/moneyflow/general  四个业务 Agent + 各自工具
chat/block/              展示块
  MessageBlocks（JSON 编解码唯一入口）、BlockCollector（工具产块入口）
  ChatBlockActionService     表单提交 → 真实写库（R6：写库不进 LLM 工具集）
```

职责边界的设计约束（R1~R6）在每个类的 Javadoc 里都写明了，整体质量较高。

## 二、待办问题（按严重程度排序）

### 1. `AgentOrchestrator` + `AgentExecution` —— 路由时代的残留抽象

路由删除后，`dispatch()` 退化成「查注册表 + 调用 stream」一行透传；`AgentExecution` 的存在理由是「聊天层必须先知道 agentCode，而 code 来自路由决策」——现在 code 是路径入参，理由已不成立，`AgentExecution` 只是把入参回显。

**建议**：两者作为扩展接缝可以保留，但需更新 `AgentExecution` 注释承认现状；或者让 `MessageService` 直接调 `AgentRegistry` + `agent.stream()`，删掉这层透传。

### 2. `ChatTransactionService.initialize` —— 事务边界服务里混入了模型输入装配策略

名义上是「事务边界」，实际方法体（`ChatTransactionService.java:93-112`）还承担：GENERATING 占位过滤、parts 摘要拼接进 content（方案 B 口径）、Entity→DTO 转换。注释解释了「为什么在事务内做转换」（防 detached 实体懒加载），动机成立，但「事务管理」和「模型输入构造规则」两个职责挤在一个 60 行的方法里。

**建议**：把历史→`ChatHistoryMessage` 的转换抽成独立方法/类，事务服务只做编排。

### 3. `MessageService` —— 自称「只做入口装配」，实际兼做读模型

类注释说本类只做装配、不持有 Reactor 细节，但 `list()` 直接持有 `MessageRepository` 做查询 + `toListItem` 做 DTO 映射（含 parts 反序列化）。同时 `list()` 的事务在本类、`clear()` 却委托给 `ChatTransactionService`，读写路径的事务归属不一致。

**建议**：要么更新注释承认它是「入口装配 + 时间线读模型」，要么把 `list()` 也下沉到 `ChatTransactionService`（或独立查询服务），保持事务归属一致。

### 4. 流水类型常量三处分散，无单一事实来源 ⚠️ 优先处理

`TYPE_EXPENSE=1 / TYPE_INCOME=2` 在三处各定义了一份（均为 private）：

- `MoneyFlowService.java:50`
- `BookkeepingDraftTool.java:41`
- `ChatBlockActionService.java:50`

三处口径必须永远一致但没有编译期约束，MoneyFlow 域才是权威。

**建议**：提升为 moneyflow 包的公共常量（或枚举），chat 两处改为引用。

### 5. `AccountChartTool` 靠中文字符串匹配判断资产类账户 ⚠️ 优先处理

`AccountAnalysisItem` DTO 只暴露 `typeName`（"活期"/"资产"），导致 `AccountChartTool.java:57` 要用 `AccountAnalysisService.ASSET_TYPE_NAMES` 按中文名回判资产类——筛选逻辑依赖展示文案，改名或 i18n 就会悄悄漏账户。

**建议**：DTO 把原始 `type`（int）也带上，工具按类型码判断，删除 `ASSET_TYPE_NAMES`。

### 6. `ChatBlockActionService` 的包位置

`chat.block` 包其余类都是纯结构/编解码，唯独它依赖 Book/Account/Currency/MoneyFlow 五个业务服务做真实写库。类本身职责清晰（注释说明了为什么不进 LLM 工具），但它让 block 包的内聚性从「展示块结构」变成「结构 + 业务动作桥」。

**建议**：不紧急。若动作类型增多，拆出独立的 `chat.action` 包。

## 三、跨模块风险（非 chat 内部问题）

`AccountAnalysisService.snapshot()` 与 `AccountService.netWorth()` 是同口径净资产的**两份平行实现**（注释说「同路径」，但代码各自写了一遍折算/汇总），将来口径调整要双改，容易漏。

**建议**：中长期把净资产计算收敛成一份（如抽出一个不依赖 ThreadLocal、显式传 userId 的领域服务，供 AccountService 与 chat 工具共用）。
