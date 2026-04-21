## ADDED Requirements

### Requirement: 关键词预过滤
系统 SHALL 实现 keyword_filter 功能，基于关键词匹配快速筛选候选技能。

#### Scenario: 关键词匹配
- **WHEN** 用户查询包含技能关键词
- **THEN** 系统返回匹配的技能列表，按匹配度和优先级排序

#### Scenario: 无匹配技能
- **WHEN** 用户查询不包含任何技能关键词
- **THEN** 系统返回空列表

### Requirement: LLM 语义选择
系统 SHALL 实现 select_best_skill 功能，使用 LLM 从候选中选择最佳技能。

#### Scenario: 单候选技能
- **WHEN** 关键词过滤后只剩一个候选技能
- **THEN** 系统直接返回该技能

#### Scenario: 多候选选择
- **WHEN** 关键词过滤后有多个候选技能
- **THEN** 系统使用 LLM 选择最匹配的技能

#### Scenario: 无合适技能
- **WHEN** LLM 判断没有合适的技能
- **THEN** 系统返回 null，使用默认处理方式

### Requirement: Skill 定义
系统 SHALL 支持从 Markdown 文件加载 Skill 定义。

#### Scenario: Skill 加载
- **WHEN** 系统启动时
- **THEN** 系统从配置的目录加载所有 Skill 定义
- **THEN** 每个 Skill 包含名称、描述、关键词、优先级、系统提示词

### Requirement: 技能执行
系统 SHALL 支持通过 Skill 执行子代理任务。

#### Scenario: Skill 子代理
- **WHEN** 选择特定 Skill 执行任务
- **THEN** 系统使用 Skill 的系统提示词创建子代理
- **THEN** 子代理执行完成后返回结果
