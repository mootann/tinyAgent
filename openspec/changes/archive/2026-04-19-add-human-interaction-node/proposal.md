## Why

当前 tinyflow-backend Java 版本缺少人机交互澄清机制，对于复杂问题（thinking 及以上模式）无法主动询问用户以获取更清晰的上下文。同时，系统缺乏持久化的对话历史管理和基于 Redis 的 Checkpoint 中断恢复能力，导致用户无法在中断后恢复对话。需要引入这些能力来提升用户体验和系统可靠性。

## What Changes

1. **新增人机交互澄清节点 (ClarificationNode)**: 在 thinking/pro/ultra 模式下，AI 可以主动提出澄清问题，等待用户回复后再继续执行
2. **Redis Checkpoint 机制**: 使用 langgraph4j 的 RedisCheckpointer 实现会话级别的状态保存和中断恢复
3. **MySQL 多轮对话持久化**: 使用 MyBatis-Plus 将对话历史存储到 MySQL，支持历史消息查询和拼接
4. **Markdown 长期记忆存储**: 将提取的长期记忆事实保存为 Markdown 文件，按类别组织
5. **Clarification API**: 新增获取待处理澄清和提交回复的 REST API

## Capabilities

### New Capabilities
- `human-clarification`: 人机交互澄清机制，支持 AI 主动提问和用户回复
- `redis-checkpoint`: 基于 Redis 的 Checkpoint 保存和中断恢复
- `mysql-conversation`: MySQL 持久化的多轮对话管理
- `markdown-memory`: Markdown 文件格式的长期记忆存储

### Modified Capabilities
- `agent-graph`: 在 graph 中添加 clarification 节点和 checkpoint 支持

## Impact

- **数据库**: 新增 MySQL 依赖，需要创建 thread、message、clarification 表
- **Redis**: 新增 Redis 配置用于 Checkpoint 存储
- **API 变更**: 新增 `/api/clarifications` 相关端点
- **Graph 流程**: Router 后新增 Clarification 节点，影响 thinking/pro/ultra 模式的执行流程
- **存储变更**: 对话历史从文件存储迁移到 MySQL，长期记忆使用 Markdown 文件
