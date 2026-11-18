"""Helpers for the security tests."""
EXECUTED: list[str] = []


def mark() -> None:
    """If this ever runs, a malicious payload executed code."""
    EXECUTED.append("code ran")


class Evil:
    """When unpickled, this object runs helpers.mark(): a stand-in for a real attacker's payload."""

    def __reduce__(self):
        return (mark, ())
