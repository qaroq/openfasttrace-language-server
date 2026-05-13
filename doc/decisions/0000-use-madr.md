---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# Use MADR for Architectural Decision Records

## Context and Problem Statement

Architectural decisions made during this project should be documented in a way that is lightweight, version-controlled alongside the code, and easy to review.

## Decision Outcome

Chosen option: **MADR 4.0** (Markdown Architectural Decision Records), because it is a well-established, minimal format that lives in the repository and requires no external tooling.

Records are stored in `doc/decisions/` with sequential four-digit prefixes (`0001-`, `0002-`, …).

### Consequences

* Good, because decisions are co-located with the code and reviewed in pull requests.
* Good, because the format is plain Markdown, renderable by GitHub without plugins.
* Bad, because there is no enforcement tooling — discipline is required to keep records up to date.
