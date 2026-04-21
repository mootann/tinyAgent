## ADDED Requirements

### Requirement: 事实提取
系统 SHALL 实现 extract_facts 功能，从对话中提取结构化事实。

#### Scenario: 提取用户偏好
- **WHEN** 对话中包含用户偏好信息（如"我喜欢 Java"）
- **THEN** 系统提取事实："用户喜欢 Java"

#### Scenario: 提取关键信息
- **WHEN** 对话中包含重要信息（如"我的邮箱是 xxx@example.com"）
- **THEN** 系统提取事实："用户邮箱是 xxx@example.com"

### Requirement: 事实评分
系统 SHALL 实现 score_facts 功能，对新提取的事实进行置信度评分。

#### Scenario: 高置信度事实
- **WHEN** 事实明确且具体
- **THEN** 系统给予高置信度评分（>= 0.7）

#### Scenario: 低置信度事实
- **WHEN** 事实模糊或推测性
- **THEN** 系统给予低置信度评分（< 0.7）

### Requirement: 事实合并
系统 SHALL 实现 merge_facts 功能，合并新旧事实，处理冲突。

#### Scenario: 新增事实
- **WHEN** 新事实与现有事实无冲突
- **THEN** 系统将新事实添加到内存

#### Scenario: 更新事实
- **WHEN** 新事实与现有事实冲突
- **THEN** 系统使用新事实更新旧事实

### Requirement: 事实注入
系统 SHALL 实现 inject 功能，将相关事实注入到提示词中。

#### Scenario: 上下文增强
- **WHEN** 处理用户请求时
- **THEN** 系统将相关事实作为上下文注入到系统提示词

### Requirement: 记忆衰减
系统 SHALL 实现记忆衰减机制，降低旧事实的权重。

#### Scenario: 时间衰减
- **WHEN** 事实超过配置的 decay_days
- **THEN** 系统应用 decay_factor 降低事实权重
