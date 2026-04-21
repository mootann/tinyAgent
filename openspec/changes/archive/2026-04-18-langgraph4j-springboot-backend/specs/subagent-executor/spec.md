## ADDED Requirements

### Requirement: 任务定义
系统 SHALL 支持定义 TaskSpec，描述子代理任务的规格。

#### Scenario: 任务创建
- **WHEN** dispatch 节点创建任务
- **THEN** 任务包含 ID、类型、描述、代理类型/技能名称、超时等属性

### Requirement: 并行执行
系统 SHALL 实现并行子代理执行器，同时执行多个任务。

#### Scenario: Ultra 模式并行
- **WHEN** ultra 模式创建多个子任务
- **THEN** 系统并行执行所有子任务
- **THEN** 系统等待所有任务完成

### Requirement: 任务结果
系统 SHALL 支持 TaskResult，封装任务执行结果。

#### Scenario: 成功完成
- **WHEN** 子任务成功完成
- **THEN** 返回状态为 completed，包含输出内容和执行时间

#### Scenario: 任务失败
- **WHEN** 子任务执行失败
- **THEN** 返回状态为 failed，包含错误信息

#### Scenario: 任务超时
- **WHEN** 子任务执行超过超时时间
- **THEN** 返回状态为 timed_out

### Requirement: 结果合并
系统 SHALL 实现 merge 节点，合并多个子任务的结果。

#### Scenario: 多结果合并
- **WHEN** 多个子任务完成
- **THEN** merge 节点将所有结果合并为统一的输出
- **THEN** 合并后的结果传递给 reflector 节点

### Requirement: 超时控制
系统 SHALL 支持任务级别的超时控制。

#### Scenario: 超时配置
- **WHEN** 创建任务时指定 timeout
- **THEN** 系统确保任务在超时时间内完成或标记为 timed_out
