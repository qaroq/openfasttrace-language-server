---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# URI and Position Mapping Between LSP and OFT

## Context and Problem Statement

LSP represents file locations as `DocumentUri` (a `file://` URI string) and positions as 0-based `{line, character}` pairs. OFT's `Location` represents positions as a file path (String), a 1-based line number, and an optional column number (`NO_LINE = -1`, `NO_COLUMN = -1`). A consistent, tested conversion layer is needed.

## Decision Outcome

Chosen option: **Explicit conversion utilities in a `LocationMapper` class**, with the following rules:

| Concern | Rule |
|---|---|
| URI → Path | `Path.of(URI.create(documentUri))` |
| Path → URI | `path.toUri().toString()` |
| OFT line → LSP line | `oftLine - 1` (OFT is 1-based, LSP is 0-based) |
| LSP line → OFT line | `lspLine + 1` |
| OFT column → LSP character | `oftColumn - 1` when `oftColumn != Location.NO_COLUMN`; otherwise `0` |
| Range end character | Scan the source line text for the tag end boundary when column is `NO_COLUMN` |

The tag importer provides column information for coverage tags; the markdown importer typically provides only the line. For markdown items, the LSP range covers the full line (character 0 to line length).

### Consequences

* Good, because the conversion rules are in one place and unit-tested independently of the LSP server.
* Good, because off-by-one errors in line/column conversion are a common source of bugs; isolating the logic makes them easy to catch.
* Bad, because scanning the source line to compute the end range is an extra read per hover/definition request. Acceptable in M1; can be cached later.

### Confirmation

Unit tests for `LocationMapper` cover: URI round-trip, line offset conversion for both 0→1 and 1→0, column absent (falls back to 0), and column present (subtracts 1).
