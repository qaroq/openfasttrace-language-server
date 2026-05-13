---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# Workspace Indexing Strategy: Full Import on Start, File-Level Re-Import on Save

## Context and Problem Statement

The LSP server must maintain an up-to-date index of all OFT spec items and coverage tags in the workspace to answer definition, reference, hover, and diagnostic requests quickly. OFT's `Oft.importItems()` performs a full workspace scan; it is not designed for incremental updates. We need a strategy that balances correctness and performance.

## Considered Options

* Full re-import on every `didChange` notification
* Full re-import on `didSave` + `workspace/didChangeWatchedFiles`, debounced
* Intra-file incremental update (parse only the changed file on `didChange`)

## Decision Outcome

Chosen option: **Full import on server start; file-level re-import on `didSave` and `workspace/didChangeWatchedFiles`, with a 300 ms debounce**, because it is the simplest correct approach for M1 and avoids the complexity of tracking inter-file dependencies incrementally.

A `didChange` event (keystroke-level) does **not** trigger a re-import — only saves do.

### Consequences

* Good, because the index is always consistent with the saved state of the workspace.
* Good, because the implementation is straightforward: call `Oft.importItems()` with the full workspace path, rebuild the internal maps.
* Neutral, because the index lags behind unsaved edits; users see fresh diagnostics after saving. This matches the behaviour of most build-tool-backed language servers.
* Bad, because a large workspace will re-import all files on every save, not just the changed file. This is revisited in a future milestone if profiling shows unacceptable latency.
* Bad, because the debounce adds up to 300 ms of intentional delay before the index reflects the latest save.

### Confirmation

Integration tests will verify that after a file save, the next definition request returns the updated location.

## More Information

A potential future improvement is to switch to file-scoped re-import: re-parse only the saved file and merge the result into the existing index. This requires understanding OFT's import pipeline well enough to drive it per-file, which is deferred to M2/M3.
