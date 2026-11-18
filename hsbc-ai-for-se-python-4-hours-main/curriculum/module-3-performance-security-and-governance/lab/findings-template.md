# Audit findings: audit_target

**Core task:** fill in rows 1 and 2 (the two findings you fix today). Rows 3 to 5 are bonus.
One short line per cell is enough.

| # | Finding (what and where) | Found by (bandit / reading / test) | Severity (Critical / High / Medium / Low) | Fix (one line) | Proven by | Reviewer sign-off (your name) |
|---|---|---|---|---|---|---|
| 1 | SQL injection in `repository.py` | | | | `bash scripts/test-security.sh sql` | |
| 2 | Full account number in the statement and the log (`exporter.py`) | | | | `bash scripts/test-security.sh mask` | |
| 3 (bonus) | Hard-coded secrets with a silent default (`config.py`) | | | | `bash scripts/test-security.sh secrets` | |
| 4 (bonus) | Untrusted data loaded with `pickle` (`session_cache.py`) | | | | `bash scripts/test-security.sh pickle` | |
| 5 (bonus) | Unsalted, fast password hash (`auth.py`) | | | | `bash scripts/test-security.sh hash` | |

Governance questions before this can merge (bonus):

- Did a person (not the AI) read every changed line?
- Does any fix change behaviour that a client or a downstream system depends on?
- Is any sensitive data (a credential, a full account number) still written to a log, a file or a response?
- Who approved, and where is that recorded?
