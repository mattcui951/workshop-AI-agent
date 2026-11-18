![General Assembly](/curriculum/assets/ga-logo-black-background.png)

# Product Requirements: Payment Status Service

> **Synthetic training document.** Simplified for the lab. It is not an HSBC product definition.
> Status: DRAFT v0.3. Sprint planning is on Thursday.

## 1. Background
Operations and trading-desk tooling keep asking the payments team whether an outbound payment has settled.
The payment rail already sends a notification each time a payment changes state. We want a small Python
service that keeps the latest state of every payment and lets internal callers look it up.

## 2. Goals
- Let internal callers check the status of an outbound payment without contacting the payments team.
- Keep the status up to date from the rail's notifications.

## 3. Functional requirements
| # | Requirement |
|---|---|
| FR1 | The rail sends the service a notification (a webhook) whenever a payment changes state. |
| FR2 | The service stores the latest state of each payment. |
| FR3 | A caller can retrieve a payment's current status by its id. |
| FR4 | The response shows when the status last changed. |
| FR5 | Receiving the same notification twice must not corrupt the status. |
| FR6 | Asking for a payment the service has never seen returns an error. |
| FR7 | A payment is `submitted`, `settled` or `failed`. |

## 4. Non-functional requirements
- The service should respond quickly.
- The service should be highly available.
- The service should be secure.

## 5. Out of scope
- Starting or cancelling payments.
- Reporting and dashboards.

## 6. Success criteria
- The payments team stops receiving "has it settled?" requests.

## 7. Open questions
_None recorded._
