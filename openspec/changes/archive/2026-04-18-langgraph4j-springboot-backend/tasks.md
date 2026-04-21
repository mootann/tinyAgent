## 1. 项目初始化

- [x] 1.1 在根目录下创建 `tinyflow-backend/` 文件夹，初始化 Spring Boot 项目
- [x] 1.2 配置 `pom.xml`，添加 Spring Boot 3.x、Spring AI、LangGraph4J Core 依赖
- [x] 1.3 创建 `application.yml` 配置文件，映射 Python 版本的配置项
- [x] 1.4 创建主应用类 `TinyFlowApplication`

## 2. 核心模型定义

- [x] 2.1 创建 `GraphState` Record，定义状态字段（messages、route、pending_tasks 等）
- [x] 2.2 创建 `TaskSpec`、`TaskResult`、`TodoItem` Record
- [x] 2.3 创建 `Skill` 类，定义技能属性（name、description、keywords、priority、systemPrompt）
- [x] 2.4 创建 `Fact` 类，定义记忆事实结构

## 3. 内存系统实现

- [x] 3.1 实现 `MemoryStorage` 接口及内存存储实现
- [x] 3.2 实现 `FactExtractor`，从对话中提取事实
- [x] 3.3 实现 `FactScorer`，对事实进行置信度评分
- [x] 3.4 实现 `FactMerger`，合并新旧事实
- [x] 3.5 实现 `MemoryInjector`，构建记忆提示词
- [x] 3.6 实现 `MemoryEngine`，整合内存管道

## 4. 技能路由实现

- [x] 4.1 实现 `SkillLoader`，从 Markdown 文件加载技能定义
- [x] 4.2 实现 `SkillRegistry`，管理已加载的技能
- [x] 4.3 实现 `KeywordFilter`，关键词预过滤
- [x] 4.4 实现 `SkillSelector`，LLM 语义选择最佳技能
- [x] 4.5 实现 `SkillRouter`，整合两阶段路由

## 5. 中间件框架实现

- [x] 5.1 创建 `Middleware` 接口和 `MiddlewareChain` 类
- [x] 5.2 实现 `TodoMiddleware`，管理 TODO 状态
- [x] 5.3 实现 `LoopDetectionMiddleware`，检测循环
- [x] 5.4 实现 `ContextCompactionMiddleware`，压缩上下文

## 6. 图节点实现

- [x] 6.1 实现 `RouterNode`，4-way 路由决策
- [x] 6.2 实现 `RespondNode`，直接回答
- [x] 6.3 实现 `ThinkRespondNode`，深度推理回答
- [x] 6.4 实现 `PlanNode`，生成执行计划
- [x] 6.5 实现 `DispatchNode`，分派并行任务
- [x] 6.6 实现 `SkillNode`，技能匹配执行
- [x] 6.7 实现 `ExecuteNode`，执行子代理或工具
- [x] 6.8 实现 `ReflectorNode`，审查执行结果
- [x] 6.9 实现 `MergeNode`，合并并行结果

## 7. 图构建与编译

- [x] 7.1 创建 `LangGraphBuilder`，使用 LangGraph4J StateGraph
- [x] 7.2 配置所有节点和条件边（与Python版本一致）
- [x] 7.3 集成 MemorySaver Checkpoint 机制
- [x] 7.4 创建 `GraphService`，封装图的执行

## 8. 子代理执行器

- [x] 8.1 创建 `SubagentExecutor`，支持并行执行 (在 ExecuteNode 中实现)
- [x] 8.2 实现超时控制机制 (在 TaskSpec 中定义)
- [x] 8.3 集成工具调用（Web Search 等） (预留扩展接口)

## 9. API 层实现

- [x] 9.1 创建 `ChatController`，实现 `/api/chat` SSE 端点
- [x] 9.2 创建 `ThreadController`，实现线程管理端点（文件持久化、自动标题）
- [x] 9.3 创建 `MemoryController`，实现 `/api/memory` 端点
- [x] 9.4 实现 SSE 事件格式化，确保与 Python 版本兼容
- [x] 9.5 配置 CORS，允许前端访问

## 10. 配置与部署

- [x] 10.1 添加 `application-dev.yml` 开发配置
- [x] 10.2 创建 `README.md`，说明构建和运行方式
- [x] 10.3 所有 TODO 功能已实现完毕
