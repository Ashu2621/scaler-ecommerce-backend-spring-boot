# System design notes

This document connects the listed HLD case studies to the commerce platform. It
does not pretend that an e-commerce API is Facebook, Uber, or Hotstar; it
extracts the relevant reusable architecture decisions.

## Networking and load balancing

DNS/Route53 resolves the API name to an ALB. TLS terminates at the load balancer,
which checks `/actuator/health/readiness` and forwards only to healthy stateless
tasks. Security groups act as stateful network firewalls. Timeouts must shrink
through the call chain, and clients retry only idempotent operations with jitter.

Consistent hashing is useful when a distributed cache or partitioned consumer
needs minimal key movement as nodes change. The application does not implement
its own ring; managed Redis/Kafka already own partition placement.

## Caching

Product reads use cache-aside through Spring Cache:

1. Read the cache.
2. On miss, load MySQL and populate cache.
3. On product write/delete, evict product entries.

Cache is not the source of truth. TTL, bounded memory, hit rate, stampede
protection, and invalidation lag must be observed. Orders and payments are never
accepted from stale cache state.

Facebook-feed lesson: fan-out-on-write optimizes reads but explodes for celebrity
fan-out; fan-out-on-read saves writes but costs read latency. Commerce uses the
same hybrid idea for notifications, not for order truth.

## CAP, replication, SQL, and NoSQL

During a partition a distributed system chooses between availability and
consistent answers for a given operation. Checkout/stock/payment favor
consistency; product browsing may serve cached, slightly stale data.

RDS primary/replica topology can scale reads, but read-after-write flows must
stay on the primary or carry a consistency token. Replication is not a backup.

SQL fits orders and payments because constraints and multi-row transactions
matter. Key-value Redis fits reconstructable cache. Search documents fit
Elasticsearch/OpenSearch. Event logs fit Kafka. Object history fits S3. A single
database should not be forced to serve every access pattern.

NoSQL internals to evaluate: partition key distribution, LSM vs B-tree storage,
compaction, quorum semantics, secondary-index consistency, tombstones, and
hot-partition limits.

## Typeahead and Elasticsearch

At small scale MySQL prefix search is sufficient. At large scale:

- normalize catalog documents into OpenSearch;
- use edge n-grams/completion fields for typeahead;
- route CDC/outbox events through Kafka;
- return product IDs from search and hydrate authoritative fields;
- measure zero-result rate and indexing lag.

Search is eventually consistent and must never determine price or stock at checkout.

## Messaging, ZooKeeper, and Kafka

Kafka partitions preserve order only inside one partition, so commerce events use
`orderId` as the key. Consumer groups scale processing. Delivery is at-least-once;
consumers need event IDs and idempotent side effects. Current Kafka uses KRaft for
metadata quorum; ZooKeeper concepts remain useful for leases, membership, and
distributed coordination but a new Kafka deployment does not require ZooKeeper.

## S3 design

Product media and analytical files belong in a private, versioned, encrypted
bucket. Clients receive short-lived pre-signed upload/download URLs. Object keys
are immutable IDs, metadata lives in SQL, CloudFront serves public derivatives,
and lifecycle rules tier or expire old versions. The Terraform creates the
private encrypted/versioned bucket; application upload endpoints are a later
product feature, not part of checkout correctness.

## Unique IDs and rate limiting

Application entities use random UUID strings, avoiding a central ID service.
Time-sortable UUIDv7/Snowflake IDs are an upgrade when index locality and event
ordering are important. Clock rollback, node IDs, and information leakage must
then be addressed.

The API includes a thread-safe fixed-window limiter for a single instance. A
multi-instance production limit should use Redis with a Lua token-bucket/sliding
window keyed by authenticated user/API key—not only IP—and separate limits for
login, search, and writes.

## Case-study transfer

| Case study | Main lesson | Commerce application |
|---|---|---|
| Facebook News Feed | Hybrid fan-out and ranking | Async customer notifications/recommendations |
| Typeahead | Prefix index, ranking, low latency | Catalog suggestions |
| Messaging | Ordered partitions and idempotent delivery | Payment/order events |
| Elasticsearch | Inverted index and eventual consistency | Product discovery, never checkout truth |
| S3 | Durable objects, metadata split, CDN | Product media and data lake |
| Uber | Geospatial partitioning and real-time matching | Not needed for this domain; useful for delivery tracking |
| Hotstar | Extreme burst traffic, CDN, admission control | Flash sales, cached catalog, queue/backpressure |

## Microservices evolution

Start as the current modular monolith. Extract only when team ownership, scaling,
deployment cadence, or isolation justifies the distributed-systems tax.

Likely order:

1. Notification worker—naturally async and failure-isolated.
2. Search indexing—eventually consistent projection.
3. Catalog service—read-heavy and independently scalable.
4. Order/payment—last, because their transaction boundary is valuable.

Extraction needs API versioning, outbox/CDC, idempotency, tracing, service
authentication, circuit breakers, deployment ownership, and a rollback plan.

## Popular interview checks

- Define SLOs and peak traffic before drawing boxes.
- Name the source of truth and consistency requirement for every write.
- Estimate storage, bandwidth, QPS, and hot-key risk.
- Explain failure modes: duplicate request, slow dependency, lost webhook,
  partial deployment, DB failover, cache outage, consumer lag.
- Describe observability, security, data migration, and cost—not only the happy path.
