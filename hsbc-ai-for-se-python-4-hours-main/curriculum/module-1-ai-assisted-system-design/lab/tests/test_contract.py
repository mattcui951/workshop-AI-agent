"""
Contract check: do your models.py and your openapi.yaml agree with each other?

It fails until both files exist, and again whenever one drifts from the other.
Run it from the lab folder:  pytest -q tests/test_contract.py
"""
from pathlib import Path

import pytest
import yaml

SPEC_PATH = Path(__file__).resolve().parent.parent / "openapi.yaml"


@pytest.fixture(scope="module")
def spec():
    assert SPEC_PATH.exists(), "openapi.yaml not found: open VS Code and the terminal in the lab folder"
    return yaml.safe_load(SPEC_PATH.read_text())


@pytest.fixture(scope="module")
def models():
    try:
        import models as m
    except ImportError as e:   # fail loudly (not skip) so a missing models.py is never mistaken for a pass
        pytest.fail(f"models.py could not be imported ({e}): generate it with prompts/05-models.md")
    return m


def schema(spec, name):
    schemas = spec.get("components", {}).get("schemas", {})
    assert name in schemas, f"schema {name} is missing from openapi.yaml"
    return schemas[name]


def test_the_spec_is_valid_openapi(spec):
    validator = pytest.importorskip("openapi_spec_validator")
    validator.validate(spec)


@pytest.mark.parametrize("name", ["PaymentEvent", "PaymentStatus", "ErrorResponse"])
def test_model_fields_match_the_schema(spec, models, name):
    cls = getattr(models, name, None)
    assert cls is not None, f"models.py has no class named {name}"
    schema_props = set(schema(spec, name).get("properties", {}))
    model_fields = set(cls.model_fields)
    assert model_fields == schema_props, (
        f"{name}: models.py has {sorted(model_fields)} but the spec has {sorted(schema_props)}"
    )


def test_required_fields_match_the_schema(spec, models):
    for name in ["PaymentEvent", "PaymentStatus"]:
        cls = getattr(models, name)
        required_in_model = {f for f, info in cls.model_fields.items() if info.is_required()}
        required_in_spec = set(schema(spec, name).get("required", []))
        assert required_in_model == required_in_spec, f"{name}: required fields differ between models.py and openapi.yaml"


def test_states_match_the_spec(spec, models):
    expected = {"submitted", "settled", "failed"}
    assert {s.value for s in models.PaymentState} == expected
    spec_states = set(schema(spec, "PaymentStatus")["properties"]["state"].get("enum", []))
    assert spec_states == expected, "the state enum in openapi.yaml should list submitted, settled and failed"


def test_every_prd_requirement_has_an_operation(spec):
    ops = {op.get("operationId") for item in spec["paths"].values() for op in item.values() if isinstance(op, dict)}
    assert {"receiveEvent", "getPaymentStatus"} <= ops
    assert "/payments/{payment_id}" in spec["paths"] and "/events" in spec["paths"]
