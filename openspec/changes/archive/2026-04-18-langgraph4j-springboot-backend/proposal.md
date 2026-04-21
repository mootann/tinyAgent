## Why

TinyFlow 项目目前使用 Python (FastAPI + LangGraph) 实现后端，但为了更好地融入 Java 企业级生态系统、利用 Spring Boot 的成熟生态（如 Spring Security、Spring Data、监控等），以及满足团队技术栈统一的需求，需要基于 LangGraph4J（LangGraph 的 Java 版本）重新实现一个功能对等的 Spring Boot 后端版本。

## What Changes

- **新增 Spring Boot 后端模块**：在 `tinyflow-backend/` 目录下创建完整的 Spring Boot 项目
- **复刻核心功能**：完整移植 Python 版本的 4-way 路由智能体图、内存系统、技能路由、中间件机制
- **API 兼容**：保持与前端现有的 REST API 和 SSE 流式接口完全兼容
- **基于 LangGraph4J + Spring AI**：使用 LangGraph4J 作为图执行引擎，Spring AI 作为 LLM 抽象层

## Capabilities

### New Capabilities
- `chat-api`: SSE 流式聊天接口，支持 4 种执行模式（flash/thinking/pro/ultra）
- `thread-management`: 对话线程的 CRUD 管理
- `agent-graph`: 基于 LangGraph4J 的智能体状态图实现
- `memory-system`: 事实提取、评分、合并、注入的完整内存管道
- `skill-routing`: 两阶段技能路由（关键词预过滤 + LLM 语义选择）
- `middleware-framework`: 可插拔的中间件机制（TODO、循环检测、上下文压缩）
- `subagent-executor`: 并行子代理任务执行器

### Modified Capabilities
- 无（此为全新模块，不修改现有代码）

## Impact

- **新增模块**: `tinyflow-backend/` - 完整的 Spring Boot 项目
- **依赖新增**: Spring Boot 3.x, Spring AI, LangGraph4J Core, Spring AI Agent
- **API 兼容**: 保持与现有前端 `/api/chat` 和 `/api/threads` 接口兼容
- **配置方式**: 使用 `application.yml` 替代 Python 的 `config.yaml`
