# SSE 事件文档

## 概述

TinyFlow 使用 Server-Sent Events (SSE) 向客户端推送实时事件。这使得前端能够显示：
- 实时思考过程
- 工具调用状态
- 任务执行进度
- 内容流式生成

## 连接方式

```javascript
const eventSource = new EventSource('/api/chat', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    thread_id: 'abc123',
    message: '你好'
  })
});

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  console.log(data.event, data.data);
};
```

## 事件类型详解

### 1. thinking

表示当前正在执行的节点。

**触发时机**: 每个节点开始执行时

**数据格式**:
```json
{
  "event": "thinking",
  "data": {
    "node": "router",
    "content": "正在分析您的问题..."
  }
}
```

**节点说明**:
- `router`: 路由决策节点
- `respond`: 直接响应节点
- `think_respond`: 深度推理节点
- `plan`: 任务规划节点
- `dispatch`: 任务分发节点
- `skill_node`: 技能匹配节点
- `execute`: 任务执行节点
- `reflector`: 结果反思节点
- `merge`: 结果合并节点

### 2. mode_selected

表示系统选择的执行模式。

**触发时机**: Router 节点完成决策后

**数据格式**:
```json
{
  "event": "mode_selected",
  "data": {
    "mode": "flash",
    "reason": "自动选择 ⚡ 快速回答 模式"
  }
}
```

**模式说明**:
- `flash`: 简单问题，直接回答
- `thinking`: 需要深度推理
- `pro`: 需要规划执行
- `ultra`: 需要并行研究

### 3. content

流式输出的内容片段。

**触发时机**: LLM 生成内容时（Java 后端目前只发送最终结果）

**数据格式**:
```json
{
  "event": "content",
  "data": {
    "content": "这是生成的内容片段..."
  }
}
```

### 4. tool_call

表示即将调用工具。

**触发时机**: ExecuteNode 检测到工具调用时

**数据格式**:
```json
{
  "event": "tool_call",
  "data": {
    "name": "web_search",
    "query": "搜索关键词",
    "preview": ""
  }
}
```

### 5. tool_result

表示工具执行结果。

**触发时机**: 工具执行完成后

**数据格式**:
```json
{
  "event": "tool_result",
  "data": {
    "name": "web_search",
    "preview": "搜索结果摘要（前200字符）..."
  }
}
```

### 6. subagent_status

表示子代理任务状态更新。

**触发时机**: DispatchNode 创建任务时

**数据格式**:
```json
{
  "event": "subagent_status",
  "data": {
    "task_id": "task_001",
    "status": "running",
    "type": "subagent",
    "label": "技能 [research] 执行中"
  }
}
```

### 7. subagent_result

表示子代理任务完成。

**触发时机**: ExecuteNode 完成任务执行后

**数据格式**:
```json
{
  "event": "subagent_result",
  "data": {
    "task_id": "task_001",
    "status": "completed",
    "label": "研究完成 (5.2s)"
  }
}
```

**状态值**:
- `completed`: 成功完成
- `failed`: 执行失败
- `timed_out`: 任务超时

### 8. todo_update

表示任务列表更新。

**触发时机**: PlanNode 创建任务或 ExecuteNode 更新任务状态时

**数据格式**:
```json
{
  "event": "todo_update",
  "data": {
    "todos": [
      {
        "id": "todo_001",
        "content": "搜索相关信息",
        "status": "in_progress",
        "error": null
      },
      {
        "id": "todo_002",
        "content": "整理结果",
        "status": "pending",
        "error": null
      }
    ]
  }
}
```

**状态值**:
- `pending`: 待执行
- `in_progress`: 执行中
- `completed`: 已完成
- `failed`: 失败

### 9. loop_warning

表示检测到循环执行。

**触发时机**: LoopDetectionMiddleware 检测到循环时

**数据格式**:
```json
{
  "event": "loop_warning",
  "data": {
    "iteration": 3,
    "message": "达到最大迭代次数"
  }
}
```

### 10. context_compacted

表示上下文已被压缩。

**触发时机**: ContextCompactionMiddleware 压缩消息历史时

**数据格式**:
```json
{
  "event": "context_compacted",
  "data": {
    "original_messages": 15,
    "compacted_to": 8
  }
}
```

### 11. memory_update

表示记忆已更新。

**触发时机**: MemoryEngine 异步处理完新事实后

**数据格式**:
```json
{
  "event": "memory_update",
  "data": {
    "facts": [
      {
        "id": "fact_001",
        "content": "用户新的事实",
        "category": "preference",
        "confidence": 0.9
      }
    ]
  }
}
```

### 12. done

表示对话完成。

**触发时机**: 图执行完成后

**数据格式**:
```json
{
  "event": "done",
  "data": {}
}
```

### 13. error

表示发生错误。

**触发时机**: 执行过程中发生异常时

**数据格式**:
```json
{
  "event": "error",
  "data": {
    "error": "错误描述信息"
  }
}
```

## 典型事件序列

### Flash 模式

```
thinking(router) -> mode_selected(flash) -> thinking(respond) -> content -> done
```

### Pro 模式

```
thinking(router) -> mode_selected(pro) -> thinking(plan) -> todo_update ->
thinking(skill_node) -> thinking(execute) -> tool_call -> tool_result ->
todo_update -> thinking(reflector) -> content -> done
```

### Ultra 模式

```
thinking(router) -> mode_selected(ultra) -> thinking(plan) -> todo_update ->
thinking(dispatch) -> subagent_status x N -> thinking(execute) ->
subagent_result x N -> todo_update x N -> thinking(merge) ->
thinking(reflector) -> content -> done
```

## 前端处理建议

### React 示例

```typescript
interface SSEEvent {
  event: string;
  data: any;
}

function useChatStream() {
  const [thinking, setThinking] = useState<string>('');
  const [content, setContent] = useState<string>('');
  const [todos, setTodos] = useState<Todo[]>([]);
  const [mode, setMode] = useState<string>('');

  const handleEvent = (event: SSEEvent) => {
    switch (event.event) {
      case 'thinking':
        setThinking(event.data.content);
        break;
      case 'content':
        setContent(prev => prev + event.data.content);
        break;
      case 'mode_selected':
        setMode(event.data.mode);
        break;
      case 'todo_update':
        setTodos(event.data.todos);
        break;
      case 'tool_call':
        console.log('Tool called:', event.data.name);
        break;
      case 'done':
        setThinking('');
        break;
    }
  };

  return { thinking, content, todos, mode, handleEvent };
}
```

## 注意事项

1. **事件顺序**: 事件按实际发生顺序发送，但网络延迟可能导致客户端接收顺序略有不同
2. **重复事件**: 某些事件（如 `todo_update`）可能多次发送，表示状态变化
3. **缺失事件**: 如果某个节点未执行，相关事件不会发送
4. **Java 后端限制**: 目前 token-level 流式输出尚未完全实现，content 事件只发送一次
