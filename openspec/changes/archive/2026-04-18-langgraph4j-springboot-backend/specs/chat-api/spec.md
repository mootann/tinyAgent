## ADDED Requirements

### Requirement: SSE 流式聊天接口
系统 SHALL 提供 `/api/chat` POST 端点，接收用户消息并返回 SSE 流式响应。

#### Scenario: Flash 模式直接回答
- **WHEN** 用户发送消息且路由选择 flash 模式
- **THEN** 系统通过 SSE 流返回 `thinking` 事件（router、respond 节点）
- **THEN** 系统通过 SSE 流返回 `content` 事件（生成的回答内容）
- **THEN** 系统发送 `done` 事件结束流

#### Scenario: Thinking 模式深度推理
- **WHEN** 用户发送消息且路由选择 thinking 模式
- **THEN** 系统通过 SSE 流返回 `thinking` 事件（router、think_respond 节点）
- **THEN** 系统通过 SSE 流返回 `content` 事件（深度推理后的回答）
- **THEN** 系统发送 `done` 事件结束流

#### Scenario: Pro 模式规划执行
- **WHEN** 用户发送消息且路由选择 pro 模式
- **THEN** 系统通过 SSE 流返回 `mode_selected` 事件（显示选择的模式）
- **THEN** 系统通过 SSE 流返回 `thinking` 事件（router、plan、skill_node、execute、reflector 节点）
- **THEN** 系统通过 SSE 流返回 `todo_update` 事件（执行计划更新）
- **THEN** 系统通过 SSE 流返回 `content` 事件（最终回答）
- **THEN** 系统发送 `done` 事件结束流

#### Scenario: Ultra 模式并行研究
- **WHEN** 用户发送消息且路由选择 ultra 模式
- **THEN** 系统通过 SSE 流返回 `mode_selected` 事件（显示选择的模式）
- **THEN** 系统通过 SSE 流返回 `thinking` 事件（router、plan、dispatch、execute、merge、reflector 节点）
- **THEN** 系统通过 SSE 流返回 `subagent_status` 事件（子代理任务状态）
- **THEN** 系统通过 SSE 流返回 `subagent_result` 事件（子代理任务结果）
- **THEN** 系统通过 SSE 流返回 `content` 事件（合并后的最终回答）
- **THEN** 系统发送 `done` 事件结束流

#### Scenario: 工具调用事件
- **WHEN** 执行节点调用工具（如 web_search）
- **THEN** 系统通过 SSE 流返回 `tool_call` 事件（工具名称和查询参数）
- **THEN** 系统通过 SSE 流返回 `tool_result` 事件（工具执行结果预览）

#### Scenario: 错误处理
- **WHEN** 处理过程中发生异常
- **THEN** 系统通过 SSE 流返回 `error` 事件（错误信息）
- **THEN** 系统关闭 SSE 流

### Requirement: 事件格式兼容
系统 SHALL 确保 SSE 事件格式与 Python 后端完全兼容。

#### Scenario: 事件结构验证
- **WHEN** 任何事件被发送
- **THEN** 事件包含 `id`、`event`、`data` 字段
- **THEN** `data` 字段为 JSON 字符串，包含事件特定的字段

### Requirement: 线程隔离
系统 SHALL 根据 `thread_id` 隔离不同对话的上下文。

#### Scenario: 多线程对话
- **WHEN** 不同 thread_id 的请求并发处理
- **THEN** 各线程的状态和记忆相互隔离
- **THEN** 相同 thread_id 的请求共享上下文
