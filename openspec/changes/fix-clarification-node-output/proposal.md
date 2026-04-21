## Why

澄清节点 (ClarificationNode) 在执行后，即使用户意图清晰不需要澄清（返回 `route=continue`），系统仍然显示 "抱歉，无法生成回复"。

从日志分析：
1. `ClarificationNode` 正确执行并返回 `route=continue`
2. 图路由正确转到 `plan` 节点
3. 但执行流程在 `plan` 节点后异常终止，没有继续执行 `skill_node` → `execute` → `reflector` 的完整流程
4. 最终 `ChatController.extractResponse()` 无法找到助手消息，返回默认错误消息

根本原因是：**当 `PlanNode` 生成的 TODO 列表为空或执行链路中断时，`ReflectorNode` 无法正确生成最终回复，导致状态中没有助手消息。**

## What Changes

- **修复 PlanNode 执行链路**: 确保 `PlanNode` 执行后，即使没有生成 TODO 也能正确流转到执行节点
- **增强错误处理**: 在关键节点添加更详细的日志和错误处理，便于问题定位
- **修复 ReflectorNode  finalizeState**: 确保 `previousRoundOutput` 为空时也能生成合理的回复
- **添加执行路径兜底**: 当 `PlanNode` 或 `SkillNode` 执行异常时，提供直接回复的 fallback 机制

## Capabilities

### New Capabilities
- `error-recovery`: 执行链路异常时的自动恢复机制

### Modified Capabilities
- `agent-graph`: 修复澄清节点后的执行流程，确保消息正确传递

## Impact

- **后端**: `tinyflow-backend` 的图执行节点（PlanNode, SkillNode, ExecuteNode, ReflectorNode）
- **API**: 无 API 变更
- **数据库**: 无数据库变更
- **前端**: 无前端变更
