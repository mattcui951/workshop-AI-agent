![General Assembly](/curriculum/assets/ga-logo-black-background.png)

# Prompt 4: OpenAPI specification

Write an OpenAPI 3.0.3 specification (YAML) for the service described in the attached PRD. Save it as bonus/my-openapi.yaml (do NOT overwrite openapi.yaml). Do not run any terminal commands.
Start with a comment block listing the assumptions you are making for the gaps we found.

Use exactly these names:
- schemas: PaymentEvent, PaymentStatus, ErrorResponse
- operations: POST /events (operationId receiveEvent) and GET /payments/{payment_id} (operationId getPaymentStatus)
- states: submitted, settled, failed

Every error response must use the ErrorResponse body. Make duplicate notifications safe.
