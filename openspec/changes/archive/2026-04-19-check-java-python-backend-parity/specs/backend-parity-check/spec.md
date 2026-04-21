## ADDED Requirements

### Requirement: Backend parity check process
The system SHALL provide a systematic process to compare Python and Java backend implementations for functional equivalence.

#### Scenario: Complete parity audit
- **WHEN** the parity check process is initiated
- **THEN** all API endpoints, core functions, data models, and event streams are compared
- **AND** a comprehensive report is generated showing matches and gaps

### Requirement: Version tracking
The system SHALL record the exact code versions (commit hashes) being compared to ensure reproducibility.

#### Scenario: Version documentation
- **WHEN** performing a parity check
- **THEN** the report includes Python backend commit hash and Java backend version
- **AND** the date/time of the comparison is recorded

## API Endpoint Comparison Matrix

| Endpoint | Python | Java | Status | Notes |
|----------|--------|------|--------|-------|
| POST /api/chat | ✅ | ✅ | MATCH | Java lacks full streaming events |
| GET /api/threads | ✅ | ✅ | MATCH | Both return thread list |
| POST /api/threads | ✅ | ✅ | MATCH | Both create new thread |
| GET /api/threads/{id} | ✅ | ✅ | MATCH | Java has messages param |
| PATCH /api/threads/{id} | ✅ | ✅ | MATCH | Both update thread |
| DELETE /api/threads/{id} | ✅ | ✅ | MATCH | Both delete thread |
| GET /api/threads/{id}/messages | ✅ | ✅ | MATCH | Java implemented |
| GET /api/memory | ✅ | ✅ | MATCH | Both return facts |
| POST /api/memory | ❌ | ✅ | GAP | Python missing POST endpoint |
| DELETE /api/memory/{id} | ❌ | ✅ | GAP | Python missing DELETE endpoint |

## Core Function Comparison

### Graph Nodes
| Node | Python | Java | Status |
|------|--------|------|--------|
| router | ✅ | ✅ | MATCH |
| respond | ✅ | ✅ | MATCH |
| think_respond | ✅ | ✅ | MATCH |
| plan | ✅ | ✅ | MATCH |
| dispatch | ✅ | ✅ | MATCH |
| skill_node | ✅ | ✅ | MATCH |
| execute | ✅ | ✅ | MATCH |
| reflector | ✅ | ✅ | MATCH |
| merge | ✅ | ✅ | MATCH |

### Middleware
| Middleware | Python | Java | Status |
|------------|--------|------|--------|
| TodoMiddleware | ✅ | ✅ | MATCH |
| LoopDetectionMiddleware | ✅ | ✅ | MATCH |
| ContextCompactionMiddleware | ✅ | ✅ | MATCH |

### SSE Events
| Event Type | Python | Java | Status |
|------------|--------|------|--------|
| thinking | ✅ | ✅ | MATCH |
| content | ✅ | ⚠️ | PARTIAL | Java sends once, not streaming |
| tool_call | ✅ | ❌ | GAP |
| tool_result | ✅ | ❌ | GAP |
| subagent_status | ✅ | ❌ | GAP |
| subagent_result | ✅ | ❌ | GAP |
| memory_update | ✅ | ❌ | GAP |
| todo_update | ✅ | ❌ | GAP |
| mode_selected | ✅ | ❌ | GAP |
| loop_warning | ✅ | ❌ | GAP |
| context_compacted | ✅ | ❌ | GAP |
| done | ✅ | ✅ | MATCH |
| error | ✅ | ✅ | MATCH |

## Feature Gap Summary

### Critical Gaps (Break Frontend)
1. **Token-level streaming**: Java only sends final response, no real-time streaming
2. **Tool call events**: Java doesn't emit tool_call/tool_result events
3. **Subagent events**: Java doesn't emit subagent_status/subagent_result

### Medium Priority Gaps
4. **Todo updates**: Java doesn't emit todo_update events
5. **Mode selection**: Java doesn't emit mode_selected events
6. **Loop detection**: Java has middleware but doesn't emit events
7. **Context compaction**: Java has middleware but doesn't emit events

### Low Priority Gaps
8. **Memory API**: Python missing POST/DELETE for memory management
