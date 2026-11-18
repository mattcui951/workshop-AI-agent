"""Keeps file tools inside one repository folder: no absolute paths, no "..", no symlink escapes."""
from pathlib import Path


def resolve_inside(root: Path, relative: str) -> Path:
    if not relative or "\0" in relative:
        raise ValueError("A relative path is required")
    root_real = Path(root).resolve()
    candidate = Path(relative)
    if candidate.is_absolute():
        raise ValueError("Absolute paths are not allowed")
    resolved = (root_real / candidate).resolve()          # resolve() follows symlinks, so a symlink escape is caught too
    if resolved != root_real and root_real not in resolved.parents:
        raise ValueError("Path escapes the repository")
    return resolved
