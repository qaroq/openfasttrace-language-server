---
status: accepted
date: 2026-05-13
decision-makers: Felix Gorke
---

# License: GPL-3.0-or-later

## Context and Problem Statement

OpenFastTrace is licensed under the GNU General Public License v3.0 without a Classpath Exception. This project links OFT on the classpath (static linking at the JVM level). Under the GPL, a work that incorporates GPL-licensed code must itself be distributed under a GPL-compatible license. Because there is no Classpath Exception in OFT's license, the GPL propagates to this project.

## Decision Outcome

This project is licensed under **GPL-3.0-or-later**. This is a compliance requirement, not an architectural choice.

Evidence of OFT's license: https://github.com/itsallcode/openfasttrace/blob/main/LICENSE

### Consequences

* Neutral, because the project was always intended to be open-source.
* Good, because GPL-3.0-or-later is compatible with the OFT license and satisfies the copyleft requirement.
* Good, because the SPDX identifier `GPL-3.0-or-later` in each source file makes the license machine-readable.
* Bad, because downstream projects that embed this language server as a library must also be GPL-3.0-or-later (or obtain a separate license).

### Implementation

- `LICENSE` file contains the GPL-3.0 full text.
- Every source file begins with: `// SPDX-License-Identifier: GPL-3.0-or-later`
- `README.md` prominently states the license.
