## ADDED Requirements

### Requirement: Feature gap identification
The system SHALL identify all feature gaps between Python and Java backends with severity classification.

#### Scenario: Gap discovery
- **WHEN** analyzing feature parity
- **THEN** each missing or partial feature is documented
- **AND** severity is assigned (Critical/High/Medium/Low)
- **AND** effort estimate is provided

### Requirement: Priority recommendations
The system SHALL provide prioritized recommendations for addressing gaps.

#### Scenario: Priority assignment
- **WHEN** gaps are identified
- **THEN** they are ranked by impact on user experience
- **AND** implementation complexity is considered
- **AND** a recommended order is provided

## Feature Gap Analysis

### Critical Gaps (Must Fix)

#### 1. Real-time Token Streaming
**Python**: Full token-level streaming via `on_chat_model_stream` events
**Java**: Only sends final response
**Impact**: HIGH - Breaks real-time UX
**Effort**: HIGH - Requires LangGraph4J streaming support

#### 2. Tool Call Events
**Python**: Emits `tool_call` and `tool_result` events
**Java**: No tool events emitted
**Impact**: HIGH - Users don't see tool usage
**Effort**: MEDIUM - Add event emission to ExecuteNode

#### 3. Subagent Status Events
**Python**: Emits `subagent_status` and `subagent_result` events
**Java**: No subagent events
**Impact**: HIGH - No visibility into parallel tasks
**Effort**: MEDIUM - Add to DispatchNode and ExecuteNode

### High Priority Gaps

#### 4. Todo Update Events
**Python**: Real-time `todo_update` events from plan/execute nodes
**Java**: No todo events
**Impact**: MEDIUM - No task visibility
**Effort**: LOW - Add event emission

#### 5. Mode Selection Events
**Python**: `mode_selected` event with mode and reason
**Java**: No mode selection event
**Impact**: MEDIUM - Users don't know which mode was selected
**Effort**: LOW - Add to RouterNode

### Medium Priority Gaps

#### 6. Loop Detection Events
**Python**: `loop_warning` event when loop detected
**Java**: Has middleware but no event emission
**Impact**: LOW - Internal feature
**Effort**: LOW - Add event to middleware

#### 7. Context Compaction Events
**Python**: `context_compacted` event with stats
**Java**: Has middleware but no event emission
**Impact**: LOW - Internal feature
**Effort**: LOW - Add event to middleware

#### 8. Memory Update Events
**Python**: `memory_update` event when facts change
**Java**: No memory events
**Impact**: LOW - Background feature
**Effort**: LOW - Add to memory operations

### Low Priority Gaps

#### 9. Memory Write API
**Python**: Missing POST/DELETE for memory
**Java**: Full CRUD for memory
**Impact**: LOW - Admin feature
**Effort**: LOW - Add endpoints to Python

#### 10. Message Count in Thread
**Java**: Includes messageCount field
**Python**: Missing message count
**Impact**: LOW - UI convenience
**Effort**: LOW - Add field to response

## Implementation Roadmap

### Phase 1: Critical (Week 1-2)
1. Implement token streaming in Java ChatController
2. Add tool_call/tool_result events
3. Add subagent_status/subagent_result events

### Phase 2: High Priority (Week 3)
4. Add todo_update events
5. Add mode_selected events

### Phase 3: Medium Priority (Week 4)
6. Add loop_warning events
7. Add context_compacted events
8. Add memory_update events

### Phase 4: Polish (Week 5)
9. Add memory write API to Python
10. Add messageCount to Python thread response
11. Standardize field naming (snake_case vs camelCase)

## Effort Summary

| Backend | Total Effort | Critical | High | Medium | Low |
|---------|-------------|----------|------|--------|-----|
| Java | 4-5 weeks | 2 weeks | 1 week | 1 week | 1 week |
| Python | 1 week | 0 | 0 | 0 | 1 week |
