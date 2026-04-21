# TinyFlow Backend (Java)

基于 Spring Boot + LangGraph4J 的 TinyFlow 后端实现。

## 技术栈

- **Spring Boot 3.3.0** - Web 框架
- **Spring AI** - LLM 抽象层
- **LangGraph4J** - 图执行引擎 (简化版实现)
- **LangChain4J** - 备选 LLM 客户端
- **Project Reactor** - 响应式编程

## 项目结构

```
tinyflow-backend/
├── src/main/java/com/tinyflow/
│   ├── TinyFlowApplication.java      # 主应用类
│   ├── config/                       # 配置类
│   │   ├── AppConfig.java
│   │   └── TinyFlowProperties.java
│   ├── controller/                   # API 控制器
│   │   ├── ChatController.java
│   │   └── ThreadController.java
│   ├── service/                      # 业务服务
│   │   └── GraphService.java
│   ├── graph/                        # 图执行引擎
│   │   ├── AgentGraph.java
│   │   ├── GraphBuilder.java
│   │   └── node/                     # 图节点
│   │       ├── GraphNode.java
│   │       ├── RouterNode.java
│   │       ├── RespondNode.java
│   │       ├── ThinkRespondNode.java
│   │       ├── PlanNode.java
│   │       ├── DispatchNode.java
│   │       ├── SkillNode.java
│   │       ├── ExecuteNode.java
│   │       ├── ReflectorNode.java
│   │       └── MergeNode.java
│   ├── memory/                       # 内存系统
│   │   ├── MemoryStorage.java
│   │   ├── InMemoryStorage.java
│   │   ├── MemoryEngine.java
│   │   ├── MemoryInjector.java
│   │   ├── FactExtractor.java
│   │   ├── FactScorer.java
│   │   └── FactMerger.java
│   ├── skill/                        # 技能路由
│   │   ├── SkillLoader.java
│   │   ├── SkillRegistry.java
│   │   ├── KeywordFilter.java
│   │   ├── SkillSelector.java
│   │   └── SkillRouter.java
│   ├── middleware/                   # 中间件框架
│   │   ├── Middleware.java
│   │   ├── MiddlewareChain.java
│   │   ├── TodoMiddleware.java
│   │   ├── LoopDetectionMiddleware.java
│   │   └── ContextCompactionMiddleware.java
│   └── model/                        # 数据模型
│       ├── GraphState.java
│       ├── Message.java
│       ├── TaskSpec.java
│       ├── TaskResult.java
│       ├── TodoItem.java
│       ├── ToolCall.java
│       ├── Skill.java
│       └── Fact.java
└── src/main/resources/
    ├── application.yml
    └── application-dev.yml
```

## 快速开始

### 1. 配置环境变量

```bash
export OPENAI_API_KEY=your_api_key
export DEFAULT_MODEL=gpt-4o-mini
```

### 2. 构建项目

```bash
mvn clean package -DskipTests
```

### 3. 运行应用

```bash
mvn spring-boot:run
```

或使用 jar:

```bash
java -jar target/tinyflow-backend-0.1.0.jar
```

### 4. 测试 API

```bash
# 创建线程
curl -X POST http://localhost:8000/api/threads

# 发送消息 (SSE 流式响应)
curl -X POST http://localhost:8000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"threadId": "test", "message": "你好"}'
```

## API 接口

### Chat

- `POST /api/chat` - SSE 流式聊天接口

### Threads

- `GET /api/threads` - 获取线程列表
- `POST /api/threads` - 创建新线程
- `GET /api/threads/{threadId}/messages` - 获取线程消息
- `DELETE /api/threads/{threadId}` - 删除线程

## 执行模式

- **flash** - 快速回答，直接响应
- **thinking** - 深度推理，展示思考过程
- **pro** - 规划执行，单任务处理
- **ultra** - 并行研究，多任务并行处理
