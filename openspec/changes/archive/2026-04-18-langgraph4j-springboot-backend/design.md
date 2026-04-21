## Context

TinyFlow Python 后端基于 FastAPI + LangGraph 构建，核心架构包括：
- **4-way 路由智能体图**: flash（直接回答）、thinking（深度推理）、pro（规划执行）、ultra（并行研究）
- **内存系统**: 提取-评分-合并-注入的完整管道
- **技能路由**: 关键词预过滤 + LLM 语义选择的两阶段路由
- **中间件框架**: TODO、循环检测、上下文压缩的可插拔中间件
- **子代理执行器**: 并行任务执行和结果合并

本设计目标是在 LangGraph4J + Spring AI 技术栈上复刻以上功能。

## Goals / Non-Goals

**Goals:**
- 完整复刻 Python 版本的 4-way 路由智能体图逻辑
- 实现与前端完全兼容的 REST API 和 SSE 流式接口
- 使用 LangGraph4J 的 StateGraph 实现状态机
- 使用 Spring AI 作为 LLM 抽象层，支持多模型提供商
- 实现内存系统的完整管道（提取、评分、合并、注入）
- 实现技能路由的两阶段机制
- 实现中间件框架的可插拔机制

**Non-Goals:**
- 不修改现有 Python 后端代码
- 不修改前端代码（保持 API 兼容）
- 不实现用户认证/授权（可作为后续扩展）
- 不实现持久化存储（使用内存存储，后续可扩展）

## Decisions

### 1. 项目结构
**决策**: 在 `tinyflow-backend/` 根目录下创建独立 Spring Boot 项目
**理由**: 
- 与 Python 后端 `backend/` 保持同级，结构清晰
- 独立项目便于单独构建、部署和维护
- 不依赖 LangGraph4J 的 Maven 父 POM，版本管理更灵活
- 便于后续开源或独立发布

### 2. LangGraph4J 版本选择
**决策**: 使用 LangGraph4J Core 最新版本
**理由**:
- StateGraph API 与 Python LangGraph 概念对齐
- 支持 checkpoint、streaming、subgraph 等高级特性
- 与 Spring AI 集成良好

### 3. 状态管理
**决策**: 使用 Java Record 定义 GraphState，配合 LangGraph4J 的 StateSnapshot
**理由**:
- Record 是不可变的，符合函数式编程风格
- LangGraph4J 原生支持 Record 类型作为状态
- 便于线程安全的状态传递

### 4. SSE 流式实现
**决策**: 使用 Spring WebFlux + Flux<ServerSentEvent>
**理由**:
- 响应式编程模型适合流式输出
- 与 LangGraph4J 的 AsyncGenerator 集成良好
- 支持背压控制

### 5. 内存存储
**决策**: 先实现内存存储，接口设计预留持久化扩展点
**理由**:
- 快速验证核心功能
- 通过接口隔离，后续可轻松替换为数据库存储

### 6. 技能系统
**决策**: 使用 Markdown 文件定义 Skill，启动时加载到内存
**理由**:
- 与 Python 版本保持一致的技能定义方式
- 便于热重载和版本管理

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| LangGraph4J API 与 Python 版本差异 | 仔细阅读 LangGraph4J 文档和示例，必要时查看源码 |
| SSE 流式事件格式兼容性 | 完全复刻 Python 版本的事件类型和 JSON 结构 |
| Spring AI 的模型抽象限制 | 必要时直接使用 LangChain4J 的模型客户端 |
| 性能差异 | Java 的并发性能通常优于 Python，但需验证流式场景 |

## Migration Plan

1. **开发阶段**: 在 `tinyflow-backend/` 目录独立开发
2. **测试阶段**: 使用现有前端对接测试，验证 API 兼容性
3. **部署阶段**: 可选择并行部署或完全替换 Python 后端

## Open Questions

1. 是否需要支持 Python 版本的 checkpoint 数据迁移？
2. Spring AI 的 Tool 调用机制是否能完全满足需求？
