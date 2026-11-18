"""Stores a user's session between requests."""
import pickle


def save(session: dict) -> bytes:
    return pickle.dumps(session)


def load(data: bytes) -> dict:
    return pickle.loads(data)
