## ADDED Requirements

### Requirement: 4-way 路由决策
系统 SHALL 实现 router 节点，根据用户输入选择四种执行模式之一。

#### Scenario: Flash 模式选择
- **WHEN** 用户输入为简单问题或闲聊
- **THEN** router 选择 flash 模式，路由到 respond 节点

#### Scenario: Thinking 模式选择
- **WHEN** 用户输入需要深度推理
- **THEN** router 选择 thinking 模式，路由到 think_respond 节点

#### Scenario: Pro 模式选择
- **WHEN** 用户输入需要规划执行
- **THEN** router 选择 pro 模式，路由到 plan 节点

#### Scenario: Ultra 模式选择
- **WHEN** 用户输入需要并行研究
- **THEN** router 选择 ultra 模式，路由到 plan 节点（后续进入 dispatch）

### Requirement: 状态图构建
系统 SHALL 使用 LangGraph4J 的 StateGraph 构建完整的状态机。

#### Scenario: 图结构验证
- **WHEN** 系统启动时构建图
- **THEN** 图包含所有节点：router、respond、think_respond、plan、dispatch、skill_node、execute、reflector、merge
- **THEN** 图包含所有条件边和终止条件

### Requirement: 状态管理
系统 SHALL 使用 GraphState Record 管理状态，支持消息累加。

#### Scenario: 状态更新
- **WHEN** 节点返回更新后的状态
- **THEN** LangGraph4J 正确合并状态（消息列表追加，其他字段覆盖）

### Requirement: Checkpoint 支持
系统 SHALL 启用 checkpoint 机制，支持对话状态持久化。

#### Scenario: 状态恢复
- **WHEN** 使用相同 thread_id 恢复对话
- **THEN** 系统从 checkpoint 加载之前的状态
