## ADDED Requirements

### Requirement: API endpoint mapping
The system SHALL provide a complete mapping of all API endpoints between Python and Java backends.

#### Scenario: Endpoint discovery
- **WHEN** comparing API surfaces
- **THEN** every endpoint in Python is mapped to its Java equivalent (if exists)
- **AND** differences in path, method, or parameters are documented

### Requirement: Request/Response format comparison
The system SHALL compare request and response formats for each endpoint.

#### Scenario: Format validation
- **WHEN** comparing endpoint implementations
- **THEN** request schemas are compared field-by-field
- **AND** response schemas are compared field-by-field
- **AND** incompatibilities are flagged

## Detailed API Comparison

### Chat Endpoint

**Python (FastAPI)**
```python
@router.post("/chat")
async def chat(request: ChatRequest):
    # Returns EventSourceResponse with streaming events
```

**Java (Spring Boot)**
```java
@PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> chat(@RequestBody ChatRequest request)
```

**Differences:**
- Python uses `sse-starlette` for SSE with full event support
- Java uses `Flux<String>` but only emits 3 events (thinking, content, done)
- Python supports 12+ event types with real-time streaming

### Thread Endpoints

Both implementations have equivalent thread management:
- `GET /api/threads` - List all threads
- `POST /api/threads` - Create new thread
- `GET /api/threads/{id}` - Get thread details
- `GET /api/threads/{id}?messages=true` - Get with messages
- `PATCH /api/threads/{id}` - Update thread
- `DELETE /api/threads/{id}` - Delete thread
- `GET /api/threads/{id}/messages` - Get messages

**Status**: ✅ FULLY MATCHED

### Memory Endpoints

**Python**
```python
@router.get("/memory")
async def get_memory():
    # Returns facts and stats
```

**Java**
```java
@GetMapping
public MemoryResponse getMemory()

@PostMapping
public FactResponse addMemory(@RequestBody AddFactRequest request)

@DeleteMapping("/{factId}")
public Map<String, String> deleteMemory(@PathVariable String factId)
```

**Gap**: Python missing POST and DELETE endpoints for memory management.

## Response Format Differences

### Thread Response

**Python:**
```json
{
  "thread_id": "string",
  "title": "string",
  "created_at": "ISO datetime",
  "updated_at": "ISO datetime"
}
```

**Java:**
```json
{
  "threadId": "string",
  "title": "string",
  "createdAt": "ISO datetime",
  "updatedAt": "ISO datetime",
  "messageCount": 0
}
```

**Note**: Java uses camelCase, Python uses snake_case. Java includes messageCount.

## Compatibility Assessment

| Aspect | Compatibility | Notes |
|--------|--------------|-------|
| Thread API | HIGH | Minor field naming differences |
| Chat API | LOW | Java lacks streaming events |
| Memory API | MEDIUM | Python missing write endpoints |
| SSE Events | LOW | Java severely limited |
