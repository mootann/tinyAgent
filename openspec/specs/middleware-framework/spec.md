## ADDED Requirements

### Requirement: 中间件链
系统 SHALL 实现 MiddlewareChain，支持多个中间件按顺序执行。

#### Scenario: 链式执行
- **WHEN** 节点执行前
- **THEN** 中间件按配置顺序依次处理状态
- **THEN** 节点执行后，中间件按相反顺序处理结果

### Requirement: TODO 中间件
系统 SHALL 实现 TodoMiddleware，管理执行计划。

#### Scenario: 计划创建
- **WHEN** plan 节点生成 TODO 列表
- **THEN** TodoMiddleware 跟踪每个 TODO 的状态

#### Scenario: 状态更新
- **WHEN** TODO 状态变化（pending -> in_progress -> completed/failed）
- **THEN** TodoMiddleware 更新状态并触发事件

### Requirement: 循环检测中间件
系统 SHALL 实现 LoopDetectionMiddleware，检测并防止无限循环。

#### Scenario: 循环检测
- **WHEN** 检测到重复的执行模式
- **THEN** LoopDetectionMiddleware 标记循环并终止执行

#### Scenario: 循环警告
- **WHEN** 检测到循环
- **THEN** 系统发送 `loop_warning` 事件

### Requirement: 上下文压缩中间件
系统 SHALL 实现 ContextCompactionMiddleware，在上下文过长时压缩历史消息。

#### Scenario: 上下文压缩
- **WHEN** 消息数量超过阈值
- **THEN** ContextCompactionMiddleware 压缩早期消息为摘要

#### Scenario: 压缩事件
- **WHEN** 执行上下文压缩
- **THEN** 系统发送 `context_compacted` 事件

### Requirement: 中间件配置
系统 SHALL 支持按执行模式配置不同的中间件栈。

#### Scenario: 模式特定配置
- **WHEN** flash 模式：不使用中间件
- **WHEN** thinking 模式：使用 ContextCompactionMiddleware
- **WHEN** pro/ultra 模式：使用全部中间件
