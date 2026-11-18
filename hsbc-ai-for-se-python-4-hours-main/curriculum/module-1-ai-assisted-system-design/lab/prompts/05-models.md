![General Assembly](/curriculum/assets/ga-logo-black-background.png)

# Prompt 5: Pydantic models from the OpenAPI spec

From the attached openapi.yaml, create the file models.py in this lab folder (next to openapi.yaml).

- Pydantic v2 models. Use exactly these names: PaymentState (an Enum with submitted, settled, failed), PaymentEvent, PaymentStatus, ErrorResponse.
- Each model has exactly the same fields as the properties of its schema in the spec, with the same names. Fields listed as required in the spec are required in the model; all other fields are optional.
- Validate inputs: non-empty ids, a real state value, and a timezone-aware datetime for occurred_at.
- No other libraries.

Create the file. Do not run any terminal commands. Then tell me, in two lines, which choice you are least sure about.
