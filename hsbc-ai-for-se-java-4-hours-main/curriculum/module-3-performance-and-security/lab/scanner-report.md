# Scanner report: metals-inventory-service (audit-target)

> **Simulated for the lab.** This is a hand-written summary of the kind of output a code scanner and a dependency
> scanner give for this project. It was not produced by a real scanner run. Your instructor may show you real output instead.

## Code scanner (for example SonarQube)

| # | Rule | File and line | What the scanner says |
|---|---|---|---|
| S1 | SQL injection (CWE-89) | `InventoryRepository.java`, `findByMetal` | A query is built by joining strings with user input. Use a parameterised query. |
| S2 | Hard-coded password (CWE-798) | `AppConfig.java`, `DB_PASSWORD` | A constant whose name contains "PASSWORD" holds a literal value. |

## Dependency scanner (for example OWASP Dependency-Check or Trivy)

| # | Library in `pom.xml` | Known vulnerability | Scanner severity |
|---|---|---|---|
| D1 | `org.apache.logging.log4j:log4j-core` 2.14.1 | CVE-2021-44228 (Log4Shell), also CVE-2021-45046 and CVE-2021-45105 | Critical |
| D2 | `org.apache.commons:commons-text` 1.9 | CVE-2022-42889 (Text4Shell) | Critical |

## What the scanners did NOT report

Nothing else. Read the code yourself: is the scanners' list complete?
(Hint: look at what `StatementExporter` prints and logs, and at the other constant in `AppConfig`.)
