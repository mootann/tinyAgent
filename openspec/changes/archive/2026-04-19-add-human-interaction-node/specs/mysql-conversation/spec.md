## ADDED Requirements

### Requirement: Messages are persisted to MySQL
The system SHALL persist all conversation messages to MySQL database.

#### Scenario: Save user message
- **WHEN** user sends a message
- **THEN** system stores message in MySQL with thread_id, role, content, timestamp

#### Scenario: Save assistant response
- **WHEN** assistant generates a response
- **THEN** system stores response in MySQL with thread_id, role, content, timestamp

### Requirement: Conversation history can be retrieved
The system SHALL provide query capability to retrieve conversation history for a thread.

#### Scenario: Load thread history
- **WHEN** frontend requests messages for a thread
- **THEN** system returns all messages ordered by sequence

### Requirement: History is concatenated to requests
The system SHALL concatenate conversation history with the latest user question when forming LLM requests.

#### Scenario: Form context-aware prompt
- **GIVEN** a thread with existing messages
- **WHEN** user sends a new question
- **THEN** system queries MySQL for history
- **AND** concatenates history with new question for LLM prompt

### Requirement: Thread metadata is stored in MySQL
The system SHALL store thread metadata (title, created_at, updated_at, status) in MySQL.

#### Scenario: Create new thread
- **WHEN** user creates a new conversation
- **THEN** system inserts thread record to MySQL

#### Scenario: Update thread status
- **WHEN** thread status changes (e.g., to waiting_for_clarification)
- **THEN** system updates thread record in MySQL
