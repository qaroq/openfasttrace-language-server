---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# Build Tool: Maven

## Context and Problem Statement

The project needs a JVM build tool for dependency management, compilation, testing, packaging (fat JAR), and CI integration.

## Considered Options

* Maven
* Gradle (Kotlin DSL)

## Decision Outcome

Chosen option: **Maven**, because OpenFastTrace itself uses Maven, which makes the dependency ecosystem consistent and a potential future contribution to the itsallcode organisation straightforward.

### Consequences

* Good, because the OFT dependency resolution is identical to the upstream project's setup.
* Good, because Maven's conventional project layout (`src/main/java`, `src/test/java`) is immediately understood by most Java developers.
* Good, because plugins for shade (fat JAR), JaCoCo (coverage), and Surefire (JUnit 5) are mature and well-documented.
* Bad, because Maven's XML syntax is more verbose than Gradle's Kotlin DSL.
* Bad, because incremental builds and build caching are less capable than Gradle.

## Pros and Cons of the Options

### Gradle (Kotlin DSL)

* Good, because the Kotlin DSL is more concise and type-safe.
* Good, because incremental compilation and build caching are superior.
* Bad, because it introduces a toolchain inconsistency with the OFT upstream project.
* Bad, because future integration as an OFT submodule or in the itsallcode org would require toolchain alignment.
