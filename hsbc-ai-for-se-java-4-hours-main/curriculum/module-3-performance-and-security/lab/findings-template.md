# Audit findings: metals-inventory-service

**Core task:** fill in rows 1 and 2 (the two findings you fix today). Rows 3 and 4 are bonus.
One short line per cell is enough.

| # | Finding (what and where) | Found by (scanner / reading / test) | Severity (Critical / High / Medium / Low) | Fix (one line) | Proven by (test) | Reviewer sign-off (your name) |
|---|---|---|---|---|---|---|
| 1 | SQL injection in `InventoryRepository.findByMetal` | | | | `bash scripts/test-security.sh sql` | |
| 2 | Full account number in the statement and the log (`StatementExporter`) | | | | `bash scripts/test-security.sh mask` | |
| 3 (bonus) | Hard-coded secrets in `AppConfig` | | | | `bash scripts/test-security.sh secrets` | |
| 4 (bonus) | Vulnerable libraries in `pom.xml` (log4j-core 2.14.1, commons-text 1.9) | | | | `mvn -q compile` still works | |

Governance questions before this can merge (bonus):
- Did a person (not the AI) read every changed line?
- Does any fix change behaviour a client or downstream system depends on?
- Is any sensitive data (credentials, account numbers) still written to a log, file or response?
- Who approved, and where is that recorded?
