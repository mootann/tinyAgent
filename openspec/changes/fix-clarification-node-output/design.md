## Context

当前 TinyFlow 的 Java 后端使用 LangGraph4J 构建 Agent 执行图。执行流程如下：

```
START → router → clarification → plan → skill_node → execute → reflector → END
```

从日志分析发现问题：
1. `ClarificationNode` 正确返回 `route=continue`
2. `LangGraphBuilder` 正确路由到 `plan` 节点
3. 但 `ChatController` 在 21:36:03 立即收到 "抱歉，无法生成回复"
4. 中间没有 `PlanNode`、`SkillNode`、`ExecuteNode`、`ReflectorNode` 的执行日志

这表明图执行在 `clarification` 节点之后、`plan` 节点之前或之中异常终止。

## Goals / Non-Goals

**Goals:**
- 修复澄清节点后执行链路中断的问题
- 确保 `ReflectorNode` 始终能生成有效的助手消息
- 添加关键节点的详细日志，便于问题定位
- 提供执行失败的兜底机制

**Non-Goals:**
- 不修改前端代码
- 不修改 API 接口
- 不引入新的外部依赖
- 不修改数据库结构

## Decisions

### Decision 1: 修复 PlanNode 路由逻辑
**问题**: `PlanNode` 返回 `route=dispatch`，但 `LangGraphBuilder` 中 `plan` 节点的条件边只处理 `skill_node` 和 `dispatch` 两种情况。

**解决方案**: 
- 检查 `PlanNode` 是否正确返回路由标识
- 确保 `LangGraphBuilder` 的条件边正确处理所有路由值

### Decision 2: 修复 ReflectorNode finalizeState
**问题**: 当 `previousRoundOutput` 为空时，`ReflectorNode` 添加的助手消息也是空的。

**解决方案**:
- 在 `finalizeState` 中检查 `previousRoundOutput` 是否为空
- 如果为空，使用 LLM 基于对话历史生成回复
- 添加兜底回复："我正在处理您的请求，请稍候..."

### Decision 3: 添加执行链路日志
**问题**: 缺乏关键节点的执行日志，难以定位问题。

**解决方案**:
- 在 `PlanNode`、`SkillNode`、`ExecuteNode`、`ReflectorNode` 添加入口和出口日志
- 记录状态关键字段（如 `previousRoundOutput`、`messages` 大小）

### Decision 4: 添加直接执行兜底
**问题**: 当 `PlanNode` 或 `SkillNode` 执行异常时，没有 fallback 机制。

**解决方案**:
- 在 `ExecuteNode` 中添加直接执行逻辑（已有 `executeDirect` 方法）
- 确保即使没有 `pendingTasks` 也能生成回复

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 修改引入新的 bug | 添加详细日志，便于快速定位问题 |
| LLM 调用增加延迟 | 仅在 `previousRoundOutput` 为空时才进行额外调用 |
| 兜底回复质量不高 | 使用对话上下文生成回复，保持上下文连贯性 |

## Migration Plan

无需迁移，这是纯代码修复，不涉及数据变更。

部署步骤：
1. 更新代码
2. 重新编译部署
3. 验证澄清节点后的执行流程

## Open Questions

1. 是否需要添加单元测试覆盖这些修复？
2. 是否需要在前端显示执行进度（如 "正在规划..."、"正在执行..."）？
