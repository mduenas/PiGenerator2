# Copilot Instructions - PiGenerator2

PiGenerator - API/Pi generator utility app. Kotlin Multiplatform targeting Android and iOS.

## Long-Term Memory (SimpleMem)

You have access to a **SimpleMem** MCP server via the `simplemem` tool. Use it to maintain persistent memory across sessions for this project.

### When to store memories
- Architecture decisions and the reasoning behind them
- Project conventions (naming, patterns, file structure)
- Known bugs, gotchas, or constraints
- User preferences and recurring feedback
- Task progress, TODOs, and open questions
- KMP/platform-specific implementation details
- Key dependencies and their versions

### When to retrieve memories
- At the start of any session or new task, query for relevant context
- Before making architectural decisions, check if a prior decision exists

### Tools
- `memory_add`: store a dialogue or fact
- `memory_add_batch`: store multiple facts at once
- `memory_query`: ask a natural language question about stored memories
- `memory_retrieve`: browse raw stored facts
- `memory_stats`: check memory status
- `memory_clear`: clear all memories (use with caution)

At the start of each session, run `memory_query`: "What is the current state of this project and any open tasks?"
After significant decisions or discoveries, use `memory_add` to record what was decided and why.

