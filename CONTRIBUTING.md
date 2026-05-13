# Contributing

## Development Setup

1. Install Java 17+
2. Clone the repository
3. Build: `mvn verify`

## Code Style

- Java 17, no preview features
- One class per file; files stay under 800 lines
- SPDX license header on every source file: `// SPDX-License-Identifier: GPL-3.0-or-later`

## Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add hover documentation for spec items
fix: correct 0-based line offset in LSP position mapping
docs: add ADR for workspace indexing strategy
test: cover DefinitionProvider with outdated version tag
```

## Requirement Tracing

All features must have a corresponding spec item in `doc/spec/` before implementation.
Coverage tags belong in the source file closest to the implementation.

Run OFT tracing locally:

```bash
mvn verify  # OFT trace runs as part of the build
```

## Pull Requests

- Target the `main` branch
- CI must be green (build + tests + OFT trace)
- Reference the relevant spec item ID in the PR description
