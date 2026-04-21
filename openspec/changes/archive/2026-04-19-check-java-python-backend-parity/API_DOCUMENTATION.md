# TinyFlow API 文档

## 概述

TinyFlow 提供两个后端实现：
- **Python 后端** (`backend/`): FastAPI + LangGraph
- **Java 后端** (`tinyflow-backend/`): Spring Boot + LangGraph4J

两个后端提供兼容的 API，支持对话管理、记忆系统和流式响应。

## 基础信息

- **Base URL**: `http://localhost:8000` (Python) / `http://localhost:8080` (Java)
- **Content-Type**: `application/json`
- **CORS**: 已启用，允许所有来源

## API 端点

### 对话管理

#### 1. 获取所有对话

```http
GET /api/threads
```

**响应**:
```json
[
  {
    "thread_id": "abc123",
    "title": "对话标题",
    "created_at": "2024-01-15T10:30:00",
    "updated_at": "2024-01-15T10:35:00",
    "message_count": 5
  }
]
```

#### 2. 创建新对话

```http
POST /api/threads
```

**响应**:
```json
{
  "thread_id": "abc123",
  "title": "新对话",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T10:30:00",
  "message_count": 0
}
```

#### 3. 获取对话详情

```http
GET /api/threads/{thread_id}
GET /api/threads/{thread_id}?messages=true
```

**响应**:
```json
{
  "thread_id": "abc123",
  "title": "对话标题",
  "created_at": "2024-01-15T10:30:00",
  "updated_at": "2024-01-15T10:35:00",
  "message_count": 5,
  "messages": [
    {
      "role": "user",
      "content": "你好",
      "timestamp": "2024-01-15T10:30:00"
    }
  ]
}
```

#### 4. 更新对话

```http
PATCH /api/threads/{thread_id}
```

**请求体**:
```json
{
  "title": "新标题",
  "first_message": "第一条消息内容",
  "messages": [
    {"role": "user", "content": "你好"}
  ]
}
```

#### 5. 删除对话

```http
DELETE /api/threads/{thread_id}
```

**响应**:
```json
{
  "status": "deleted"
}
```

#### 6. 获取对话消息

```http
GET /api/threads/{thread_id}/messages
```

### 聊天

#### 发送消息（SSE 流式响应）

```http
POST /api/chat
Content-Type: application/json
```

**请求体**:
```json
{
  "thread_id": "abc123",
  "message": "你好，请介绍一下自己",
  "model": "gpt-4"
}
```

**响应**: `text/event-stream`

```
data: {"event": "thinking", "data": {"node": "router", "content": "正在分析您的问题..."}}

data: {"event": "mode_selected", "data": {"mode": "flash", "reason": "自动选择 ⚡ 快速回答 模式"}}

data: {"event": "content", "data": {"content": "你好！我是 TinyFlow AI 助手..."}}

data: {"event": "done", "data": {}}
```

### 记忆管理

#### 1. 获取所有记忆

```http
GET /api/memory
```

**响应**:
```json
{
  "facts": [
    {
      "id": "fact_001",
      "content": "用户喜欢Python编程",
      "category": "preference",
      "confidence": 0.95
    }
  ],
  "stats": {
    "total": 1
  }
}
```

#### 2. 添加记忆

```http
POST /api/memory
Content-Type: application/json
```

**请求体**:
```json
{
  "content": "用户喜欢Java编程",
  "category": "preference",
  "confidence": 0.9
}
```

**响应**:
```json
{
  "id": "fact_002",
  "content": "用户喜欢Java编程",
  "category": "preference",
  "confidence": 0.9
}
```

#### 3. 删除记忆

```http
DELETE /api/memory/{fact_id}
```

**响应**:
```json
{
  "status": "deleted"
}
```

## SSE 事件类型

### 事件列表

| 事件类型 | 描述 | 数据格式 |
|---------|------|---------|
| `thinking` | 节点执行状态 | `{"node": "router", "content": "正在分析..."}` |
| `mode_selected` | 执行模式选择 | `{"mode": "flash", "reason": "..."}` |
| `content` | 内容流式输出 | `{"content": "..."}` |
| `tool_call` | 工具调用 | `{"name": "web_search", "query": "...", "preview": ""}` |
| `tool_result` | 工具结果 | `{"name": "web_search", "preview": "..."}` |
| `subagent_status` | 子代理状态 | `{"task_id": "...", "status": "running", "type": "...", "label": "..."}` |
| `subagent_result` | 子代理结果 | `{"task_id": "...", "status": "completed", "label": "..."}` |
| `todo_update` | 任务列表更新 | `{"todos": [{"id": "...", "content": "...", "status": "..."}]}` |
| `loop_warning` | 循环检测警告 | `{"iteration": 3, "message": "..."}` |
| `context_compacted` | 上下文压缩 | `{"original_messages": 15, "compacted_to": 8}` |
| `memory_update` | 记忆更新 | `{"facts": [{"id": "...", "content": "..."}]}` |
| `done` | 完成 | `{}` |
| `error` | 错误 | `{"error": "..."}` |

### 执行模式

- **flash**: ⚡ 快速回答 - 简单问题直接回答
- **thinking**: 🧠 深度推理 - 需要深度思考的问题
- **pro**: 📋 规划执行 - 多步骤任务
- **ultra**: 🚀 并行研究 - 复杂任务并行处理

## 字段命名规范

### Python 后端
使用 `snake_case`:
- `thread_id`
- `created_at`
- `message_count`

### Java 后端
使用 `camelCase`:
- `threadId`
- `createdAt`
- `messageCount`

**注意**: 前端需要处理两种命名风格，或使用转换层统一处理。

## 工具调用

Java 后端支持工具调用，格式如下：

```xml
<tool_call>{"name": "web_search", "arguments": {"query": "搜索内容"}}</tool_call>
```

### 可用工具

| 工具名 | 描述 | 参数 |
|-------|------|------|
| `web_search` | 网页搜索 | `query`: 搜索关键词, `max_results`: 最大结果数 |

## 环境变量

### Java 后端

```bash
# Tavily API 密钥（用于网页搜索）
TAVILY_API_KEY=your_api_key_here

# 或配置在 application.properties
```

## 错误处理

### HTTP 状态码

- `200 OK`: 请求成功
- `400 Bad Request`: 请求参数错误
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器内部错误

### 错误响应

```json
{
  "error": "错误描述信息"
}
```

## 版本历史

- **v0.1.0**: 初始版本，支持基础对话和记忆功能
- **v0.2.0**: 添加 SSE 流式响应和工具调用
