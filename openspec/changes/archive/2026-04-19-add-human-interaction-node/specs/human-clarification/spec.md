## ADDED Requirements

### Requirement: AI can request clarification for ambiguous questions
The system SHALL allow AI to request clarification when user questions in thinking/pro/ultra mode are ambiguous or lack necessary context.

#### Scenario: AI requests clarification
- **WHEN** user asks an ambiguous question in thinking mode
- **THEN** AI generates clarification questions
- **AND** system pauses execution waiting for user response

### Requirement: User can respond to clarification requests
The system SHALL provide an API for users to respond to pending clarification requests.

#### Scenario: User responds to clarification
- **GIVEN** a pending clarification request exists
- **WHEN** user submits a response via API
- **THEN** system resumes execution from checkpoint
- **AND** incorporates user response into the conversation

### Requirement: Clarification status is tracked
The system SHALL track the status of each clarification request (pending, responded, cancelled).

#### Scenario: Check clarification status
- **WHEN** frontend queries clarification status for a thread
- **THEN** system returns current status and question if pending

### Requirement: Clarification only applies to thinking+ modes
The system SHALL skip clarification for flash mode and only apply to thinking, pro, and ultra modes.

#### Scenario: Flash mode skips clarification
- **WHEN** user asks a question in flash mode
- **THEN** system responds directly without clarification

## MODIFIED Requirements

None - this is a new capability.
