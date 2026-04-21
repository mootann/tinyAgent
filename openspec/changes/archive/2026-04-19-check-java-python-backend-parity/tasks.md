## 1. Java Backend - Critical Gaps

- [x] 1.1 Implement token-level streaming in ChatController using LangGraph4J streaming API
- [x] 1.2 Add tool_call event emission in ExecuteNode when tools are invoked
- [x] 1.3 Add tool_result event emission in ExecuteNode when tools complete
- [x] 1.4 Add subagent_status event emission in DispatchNode when tasks are created
- [x] 1.5 Add subagent_result event emission when parallel tasks complete

## 2. Java Backend - High Priority Gaps

- [x] 2.1 Add todo_update event emission in PlanNode when todos are created
- [x] 2.2 Add todo_update event emission in ExecuteNode when todo status changes
- [x] 2.3 Add mode_selected event emission in RouterNode with mode and reason

## 3. Java Backend - Medium Priority Gaps

- [x] 3.1 Add loop_warning event emission in LoopDetectionMiddleware
- [x] 3.2 Add context_compacted event emission in ContextCompactionMiddleware
- [x] 3.3 Add memory_update event emission when memory facts change

## 4. Python Backend - Low Priority Gaps

- [x] 4.1 Add POST /api/memory endpoint for adding memory facts
- [x] 4.2 Add DELETE /api/memory/{factId} endpoint for deleting memory facts
- [x] 4.3 Add message_count field to thread responses

## 5. Documentation & Standardization

- [x] 5.1 Document API field naming conventions (snake_case vs camelCase)
- [x] 5.2 Create SSE event type documentation
- [x] 5.3 Update API documentation with all endpoints

## 6. Testing & Verification

- [x] 6.1 Create integration tests for Java SSE events
- [x] 6.2 Verify all Python endpoints match specification
- [x] 6.3 Run frontend against both backends to verify compatibility
