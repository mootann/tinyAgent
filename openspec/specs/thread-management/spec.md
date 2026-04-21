## ADDED Requirements

### Requirement: 线程列表查询
系统 SHALL 提供 `/api/threads` GET 端点，返回所有对话线程列表。

#### Scenario: 获取线程列表
- **WHEN** 客户端发送 GET 请求到 `/api/threads`
- **THEN** 系统返回线程列表，包含 thread_id、创建时间、最后更新时间、消息数量

### Requirement: 线程创建
系统 SHALL 提供 `/api/threads` POST 端点，创建新的对话线程。

#### Scenario: 创建新线程
- **WHEN** 客户端发送 POST 请求到 `/api/threads`
- **THEN** 系统创建新线程并返回 thread_id

### Requirement: 线程消息获取
系统 SHALL 提供 `/api/threads/{thread_id}/messages` GET 端点，返回指定线程的消息历史。

#### Scenario: 获取消息历史
- **WHEN** 客户端发送 GET 请求到 `/api/threads/{thread_id}/messages`
- **THEN** 系统返回该线程的所有消息，按时间顺序排列

### Requirement: 线程删除
系统 SHALL 提供 `/api/threads/{thread_id}` DELETE 端点，删除指定线程。

#### Scenario: 删除线程
- **WHEN** 客户端发送 DELETE 请求到 `/api/threads/{thread_id}`
- **THEN** 系统删除该线程及其所有消息
- **THEN** 系统返回 204 No Content
