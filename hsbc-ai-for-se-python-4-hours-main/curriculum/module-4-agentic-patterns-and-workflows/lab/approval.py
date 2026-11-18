"""Human-in-the-loop: nothing consequential runs until a person says yes.

A gate is any function  gate(action: str, details: dict) -> bool.
"""
import json
import os
from typing import Callable

ApprovalGate = Callable[[str, dict], bool]


def console_gate(action: str, details: dict) -> bool:
    print(f"\n[approval gate] The agent wants to: {action} {json.dumps(details)}")
    return input("Approve? (y/n): ").strip().lower() == "y"


def auto_approve_gate(action: str, details: dict) -> bool:
    print(f"[approval] AUTO_APPROVE set: approving {action}")
    return True


def deny_all(action: str, details: dict) -> bool:
    return False


def from_environment() -> ApprovalGate:
    """Ask on the console. AUTO_APPROVE=1 is for unattended test runs only: never use it for real."""
    return auto_approve_gate if os.environ.get("AUTO_APPROVE") == "1" else console_gate
