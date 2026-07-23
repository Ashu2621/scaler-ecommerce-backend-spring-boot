# Product brief and analytics

## Problem and users

Small commerce teams need a backend that can safely list products, prevent
overselling, accept payments, and expose enough operational data to grow without
rebuilding core financial flows.

Primary users:

- Shopper: discover, order, pay, and receive confirmation.
- Catalog operator: maintain categories, price, imagery, and stock.
- Operations/finance: reconcile gateway state and investigate failures.
- Growth/product: understand conversion, GMV, retention, and catalog performance.

## Product lifecycle

| Stage | Evidence / deliverable | Exit criterion |
|---|---|---|
| Discover | Interviews and failed-order analysis | Repeated, costly problem confirmed |
| Define | Shopper journey and measurable outcomes | Scope and non-goals agreed |
| MVP | Auth, catalog, order, mock payment, webhook | End-to-end happy path and failure tests |
| Validate | Funnel events and cohort feedback | Conversion/reliability thresholds met |
| Scale | Stripe, Redis, Kafka, AWS, data lake | SLOs and unit economics hold |
| Improve | Experiments and incident learning | Measurable lift without SLO regression |

## MVP and prototyping

MVP excludes reviews, coupons, marketplace sellers, returns, and recommendations.
Those are valuable only after the order/payment invariant is reliable. A UI
prototype should validate search→product→order→checkout before investing in them.

## Critical product questions

- Is the biggest checkout drop caused by UX, payment failure, price surprise, or stock?
- Which catalog segment has demand but poor availability?
- Does a new feature improve customer value or merely add operational load?
- What happens to trust if a webhook is duplicated, delayed, or forged?
- Which assumption can be tested with a reversible, low-cost experiment?

## Market and technical structure

Alternatives include hosted commerce platforms, headless commerce APIs, and a
custom build. The defensible technical angle is not generic CRUD; it is a clear
domain model, safe payment/order state, extensible integrations, and useful data.

## Analytics events

| Event | Required fields |
|---|---|
| `product_viewed` | anonymous/user ID, product ID, category, timestamp |
| `search_performed` | query, result count, filters, latency |
| `order_created` | order ID, item count, amount, currency |
| `checkout_started` | order ID, provider |
| `payment_succeeded` | order ID, amount, provider, latency |
| `payment_failed` | order ID, provider, normalized failure code |

Core metrics: search success, product→order conversion, checkout completion,
payment success, duplicate-event count, stock-conflict rate, GMV, AOV, repeat
purchase, p95 latency, and 5xx rate. Every experiment needs a primary metric and
guardrails for failures, latency, and refunds.
