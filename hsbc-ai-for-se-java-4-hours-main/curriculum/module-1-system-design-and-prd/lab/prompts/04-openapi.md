# Prompt 4 - OpenAPI specification

Write an OpenAPI 3.0.3 specification (YAML) for the service described in the attached PRD. Save it as bonus/my-openapi.yaml (do NOT overwrite openapi.yaml). Do not run any terminal commands.
First list the assumptions you are making for the gaps we found, as a comment block at the top of the file.

Use exactly these schema names: CreateCashFlowTradeRequest, CashFlowTradeResponse, ErrorResponse.
Metal values: GOLD, SILVER, PLATINUM, PALLADIUM. Trade status values: NEW, CONFIRMED, SETTLED, CANCELLED.
Quantities are troy ounces. Make duplicate submission safe. Give every error response the ErrorResponse body.
