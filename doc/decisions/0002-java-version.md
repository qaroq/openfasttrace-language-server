---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# Java Version: 17 LTS

## Context and Problem Statement

OpenFastTrace 4.x requires Java 17 as its minimum supported runtime. The language server must run on the same JVM as OFT.

## Decision Outcome

Chosen option: **Java 17**, configured as `maven.compiler.release=17`.

Using a higher language level would exclude users still on JDK 17, and OFT itself targets 17, so there is no benefit to raising the level in M1.

### Consequences

* Good, because the language server and OFT run on the same minimum JVM.
* Good, because Java 17 is an LTS release with broad IDE and CI support.
* Good, because modern Java features (records, sealed classes, pattern matching preview) are available.
* Neutral, because the LSP4J 1.0.0 runtime also supports Java 17+.
