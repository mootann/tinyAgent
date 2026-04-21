## ADDED Requirements

### Requirement: Memory facts are stored in Markdown format
The system SHALL store extracted memory facts as Markdown files organized by category.

#### Scenario: Save extracted facts
- **WHEN** memory engine extracts facts from conversation
- **THEN** system writes facts to `data/memory/{thread_id}/facts.md`
- **AND** organizes facts by category (preference, context, behavior, knowledge)

### Requirement: Memory files include metadata
The system SHALL include confidence score, creation date, and access count in memory entries.

#### Scenario: View memory with metadata
- **WHEN** reading memory file
- **THEN** each entry shows confidence, date, and access count
- **AND** format is: `- [0.95] content | 2024-01-15 | verified: 3`

### Requirement: Memory supports decay
The system SHALL apply confidence decay to memory facts based on age and access frequency.

#### Scenario: Apply memory decay
- **GIVEN** a fact with confidence 0.9 created 60 days ago
- **AND** fact has been accessed 1 time
- **WHEN** decay job runs
- **THEN** confidence is reduced according to decay factor

### Requirement: Memory can be injected into prompts
The system SHALL read memory from Markdown files and inject relevant facts into LLM prompts.

#### Scenario: Inject memory context
- **GIVEN** a conversation with stored memories
- **WHEN** forming LLM prompt
- **THEN** system reads relevant memories from Markdown
- **AND** injects into prompt with token budget consideration

### Requirement: Memory files are human-readable
The system SHALL maintain Markdown format that is easily readable and editable by humans.

#### Scenario: Human reviews memory
- **WHEN** developer opens memory file
- **THEN** content is organized with clear headers
- **AND** each fact is on separate line with clear formatting
