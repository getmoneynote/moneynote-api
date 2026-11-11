---
description: Review Spring Boot code including JPA, transaction, controller and service layer. Use when reviewing backend code.
---

# Spring Boot Review Rules

检查：

## Entity

- 是否正确使用 @Entity
- ID 是否 Long
- 是否存在 N+1 查询风险
- Lazy Loading 是否合理


## Repository

检查：

- 查询方法命名
- 是否需要索引


## Service

检查：

- @Transactional 是否正确
- 是否存在事务边界问题


## Controller

检查：

- 参数校验
- REST API 风格
- 返回统一响应结构