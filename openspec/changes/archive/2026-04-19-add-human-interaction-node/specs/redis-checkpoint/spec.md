## ADDED Requirements

### Requirement: Checkpoint state is saved to Redis
The system SHALL save graph execution state to Redis when interruption occurs.

#### Scenario: Save checkpoint on interruption
- **WHEN** graph execution is interrupted (e.g., waiting for clarification)
- **THEN** system serializes current state
- **AND** saves to Redis with thread_id as key

### Requirement: Checkpoint can be restored from Redis
The system SHALL restore graph execution from a previously saved checkpoint.

#### Scenario: Resume from checkpoint
- **GIVEN** a checkpoint exists in Redis for the thread
- **WHEN** user provides missing input
- **THEN** system loads checkpoint from Redis
- **AND** resumes execution from interruption point

### Requirement: Checkpoints have TTL
The system SHALL set a TTL on Redis checkpoint keys to prevent unlimited storage growth.

#### Scenario: Checkpoint expires
- **GIVEN** a checkpoint exists with 24h TTL
- **WHEN** 24 hours pass without resumption
- **THEN** checkpoint is automatically deleted from Redis

### Requirement: Checkpoint is session-scoped
The system SHALL scope checkpoints to conversation threads, allowing users to close and reopen browser to continue.

#### Scenario: Continue after page refresh
- **GIVEN** user had a pending clarification
- **WHEN** user closes and reopens browser
- **AND** navigates back to the same thread
- **THEN** system retrieves checkpoint from Redis
- **AND** shows pending clarification question
