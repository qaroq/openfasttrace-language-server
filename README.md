# OpenFastTrace Language Server

An [LSP](https://microsoft.github.io/language-server-protocol/) server that brings IDE-native navigation, hover documentation, and quick fixes for [OpenFastTrace](https://github.com/itsallcode/openfasttrace) requirement tracing.

## Features

- **Go to Definition** – jump from a coverage tag in code to the spec item in Markdown, and back
- **Find References** – list all code artifacts that cover a given spec item
- **Hover Documentation** – display requirement title and description when hovering over a coverage tag
- **Quick Fixes** – detect outdated version numbers in coverage tags and update them automatically

## Status

Work in progress — Milestone 1 (standalone LSP server).

## Requirements

- Java 17 or later
- A workspace using [OpenFastTrace](https://github.com/itsallcode/openfasttrace) for requirement tracing

## Building

```bash
mvn verify
```

The build produces a standalone fat JAR at `target/openfasttrace-language-server-*-standalone.jar`.

## Running

```bash
java -jar target/openfasttrace-language-server-*-standalone.jar
```

The server communicates over stdio (LSP standard transport).

## Tracing

This project traces its own requirements using OpenFastTrace. Specification items are in `doc/spec/`. Architecture decisions are in `doc/decisions/`.

## License

Copyright (C) 2026 Felix Gorke

This program is free software: you can redistribute it and/or modify it under the terms of the **GNU General Public License** as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

See [LICENSE](LICENSE) for the full text.

OpenFastTrace is licensed under GPL-3.0. Because this project links OFT on the classpath, this project is also GPL-3.0-or-later (see `doc/decisions/0005-license.md`).
