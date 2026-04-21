## Context

tinyflow-backend 是一个基于 LangGraph4J 的 AI 工作流后端，当前使用内存存储和文件存储管理对话历史和记忆。随着功能扩展，需要：
1. 在复杂问题处理中引入人机交互澄清机制
2. 支持会话级别的 Checkpoint 中断恢复
3. 使用 MySQL 持久化多轮对话历史
4. 使用 Markdown 文件存储长期记忆

## Goals / Non-Goals

**Goals:**
- 实现 ClarificationNode，在 thinking/pro/ultra 模式下主动询问用户
- 使用 Redis 存储 Checkpoint，支持中断后恢复
- 使用 MySQL 存储对话历史，支持历史消息查询和拼接
- 使用 Markdown 文件存储长期记忆事实
- 提供 Clarification REST API 供前端调用

**Non-Goals:**
- 不支持 flash 模式的澄清（简单问题不需要）
- 不修改现有的技能路由和执行逻辑
- 不改变前端 UI（仅新增 API 支持）

## Decisions

### 1. Checkpoint 存储选择 Redis 而非 MySQL
- **决策**: 使用 Redis 存储 Checkpoint 状态
- **理由**: Redis 的高性能和 TTL 特性适合临时状态存储，langgraph4j 原生支持 RedisCheckpointer
- **替代方案**: MySQL 存储 - 实现复杂度高，性能不如 Redis

### 2. 长期记忆使用 Markdown 而非 MySQL
- **决策**: 使用 Markdown 文件存储长期记忆
- **理由**: 便于人工查看和编辑，与 Python 版本的 JSON 存储思路一致，按类别组织清晰
- **替代方案**: MySQL 存储 - 查询复杂，不利于人工阅读

### 3. Clarification 状态存储在 MySQL
- **决策**: Clarification 的待回复/已回复状态存储在 MySQL
- **理由**: 需要持久化跟踪用户回复状态，与对话历史统一管理
- **替代方案**: Redis 存储 - 存在数据丢失风险

### 4. 使用 MyBatis-Plus 而非 JPA
- **决策**: 使用 MyBatis-Plus 作为 ORM 框架
- **理由**: 用户明确要求，且 MyBatis-Plus 在复杂查询上更灵活
- **替代方案**: Spring Data JPA - 不符合用户要求

### 5. Clarification 节点位置
- **决策**: 在 Router 后、ThinkRespond/Plan 前插入 Clarification 节点
- **理由**: 需要在执行复杂逻辑前获取清晰的用户意图
- **替代方案**: 放在 Plan 后 - 会增加不必要的规划开销

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Redis 数据丢失导致无法恢复 | 配置 Redis 持久化，设置合理的 TTL |
| Clarification 流程增加延迟 | 仅在 thinking+ 模式启用，flash 模式跳过 |
| MySQL 连接池耗尽 | 配置 HikariCP 参数，设置最大连接数 20 |
| Markdown 文件并发写入冲突 | 使用文件锁或按 thread_id 分目录存储 |
| Checkpoint 序列化/反序列化失败 | 添加异常处理，降级为重新执行 |

## Migration Plan

1. **数据库准备**: 执行 schema.sql 创建表
2. **配置更新**: 更新 application.yml 添加 MySQL 和 Redis 配置
3. **依赖添加**: 添加 MyBatis-Plus、MySQL 驱动、Redis 依赖
4. **代码部署**: 按 tasks.md 顺序实现各组件
5. **数据迁移**: 现有文件存储的对话历史可选择性导入 MySQL
6. **回滚策略**: 保留文件存储逻辑作为降级方案

## Open Questions

1. Clarification 的最大等待时间是否需要配置？
2. Checkpoint 的 TTL 设置为多长时间合适？
3. Markdown 记忆文件是否需要定期归档？
