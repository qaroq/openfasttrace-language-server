---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# LSP Library: LSP4J 1.0.0

## Context and Problem Statement

The server needs a Java library that implements the Language Server Protocol wire format and data model, so we can focus on domain logic rather than JSON-RPC plumbing.

## Considered Options

* LSP4J (Eclipse)
* Hand-rolled JSON-RPC over stdio

## Decision Outcome

Chosen option: **LSP4J 1.0.0** (released February 2026), because it is the de-facto standard Java LSP implementation, maintained by the Eclipse Foundation, and supports LSP 3.18.

Key entry points used:
- `LanguageServer` interface — implemented by `OftLanguageServer`
- `TextDocumentService` — handles `textDocument/*` requests
- `WorkspaceService` — handles `workspace/*` notifications
- `Launcher.createServerLauncher(...)` — wires stdio transport

### Consequences

* Good, because LSP4J handles all JSON-RPC serialisation, LSP data types, and async message dispatch.
* Good, because LSP4J 1.0.0 is stable and aligns with LSP 3.18 (current specification).
* Good, because future IDE clients (IntelliJ in M2, VSCode in M3) connect to the same server without changes.
* Bad, because LSP4J pulls in several Eclipse dependencies (Xtend runtime, Guava) that increase the fat JAR size.

## More Information

- LSP4J releases: https://github.com/eclipse-lsp4j/lsp4j/releases
- LSP specification 3.18: https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/
