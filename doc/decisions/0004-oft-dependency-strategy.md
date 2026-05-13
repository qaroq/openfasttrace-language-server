---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# OFT Dependency Strategy: Individual Modules, Pinned to 4.3.0

## Context and Problem Statement

OpenFastTrace is available on Maven Central both as an uber-JAR (`org.itsallcode:openfasttrace`) and as individual modules (`org.itsallcode.openfasttrace:openfasttrace-api`, `-core`, `-importer-markdown`, `-importer-tag`, …). The project must not reimplement OFT's parsing or linking logic.

## Considered Options

* Individual OFT modules (api, core, importer-markdown, importer-tag)
* OFT uber-JAR

## Decision Outcome

Chosen option: **Individual modules, pinned to 4.3.0**, because it avoids pulling in unneeded exporters and reporters, reduces the fat JAR size, and makes the dependency surface explicit.

Modules included:

| Artifact | Reason |
|---|---|
| `openfasttrace-api` | Core interfaces: `SpecificationItem`, `Location`, `ImportSettings` |
| `openfasttrace-core` | `Oft.create()`, linker, tracer |
| `openfasttrace-importer-markdown` | Parses spec items from `.md` files |
| `openfasttrace-importer-tag` | Parses coverage tags from source files |

**Update policy:** OFT version upgrades require an explicit decision (update this ADR, verify API compatibility, bump the `openfasttrace.version` property).

### Consequences

* Good, because unused OFT modules (reporters, specobject exporter, zip importer) are not on the classpath.
* Good, because the exact OFT API surface used is visible from the dependency list.
* Bad, because individual module coordinates must be listed separately; the uber-JAR is one line.
* Bad, because OFT's internal module boundaries may shift between releases, requiring dependency list updates.

## Pros and Cons of the Options

### Uber-JAR

* Good, because a single dependency declaration covers everything.
* Bad, because it pulls in all exporters, reporters, and importers regardless of need.
* Bad, because it is the CLI-oriented artefact and not intended for library use.
