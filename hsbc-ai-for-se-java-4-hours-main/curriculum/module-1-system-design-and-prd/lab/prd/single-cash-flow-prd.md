# Product Requirements: Single Cash Flow Trade Service

> **Synthetic training document.** The product is simplified for the lab and is not an HSBC product definition.
> Status: DRAFT v0.3 - sprint planning is on Thursday.

## 1. Background
The bullion desk wants to offer a new "single cash flow" precious-metals trade. The client and the bank agree
today on one payment, linked to a quantity of metal, due on one future settlement date. Sales needs to capture
these trades, operations needs to see their status, and the settlement system needs to receive them.

## 2. Goals
- Let sales and trading capture a single cash flow trade quickly and without re-keying.
- Give operations a clear view of each trade's status through to settlement.
- Hand confirmed trades to the settlement system automatically.

## 3. Functional requirements
| # | Requirement |
|---|---|
| FR1 | A user can create a single cash flow trade with: client, metal, quantity, agreed price, currency and settlement date. |
| FR2 | The system assigns each trade a unique trade id. |
| FR3 | A trade can be amended before it settles. |
| FR4 | A trade can be cancelled. |
| FR5 | A user can retrieve a trade by its id, and can search trades by client and by settlement date. |
| FR6 | The settlement date must be a valid business day. |
| FR7 | Quantities are entered in ounces. |
| FR8 | Submitting the same trade twice must not create two trades. |
| FR9 | All changes to a trade must be auditable. |

## 4. Non-functional requirements
- The service should respond quickly.
- Confirmed trades are sent to the settlement system in near real time.
- The service should be highly available.

## 5. Out of scope
- Pricing and market data.
- The settlement system itself.
- Management reporting.

## 6. Success criteria
- Sales can capture a trade in a single step.
- Operations stop asking sales for the status of trades.

## 7. Open questions
_None recorded._
