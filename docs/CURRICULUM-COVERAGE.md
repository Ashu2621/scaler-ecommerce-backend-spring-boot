# Screenshot curriculum coverage

The screenshots mix four different things: application features, Java language
concepts, infrastructure/system-design lessons, and product/data coursework.
Putting fake Hadoop/Uber/Facebook code inside the Spring request path would make
the project worse. Each topic is therefore represented in its correct form:
production code, deployable infrastructure, an executable lab, or an explicit
design/product document.

Legend: **Code** = runtime implementation, **Test** = automated verification,
**Infra** = deployable configuration, **Lab** = executable analytical example,
**Design** = reasoned architecture/product coverage.

## Spring backend and deployment

| Screenshot topic | Coverage | Location |
|---|---|---|
| Spring Boot, DI, IoC, MVC | Code | Application, constructor-injected controllers/services |
| REST, APIs, HTTP | Code | `/api/v1/**`, status codes, headers, DTOs |
| Third-party APIs, exception handling | Code | Feign FakeStore client, global advice |
| ORM, models, cardinalities | Code | JPA entities and relationships |
| Inheritance, IDs, JPA/custom queries | Code | `BaseModel`, UUID IDs, repositories |
| Fetch types, N+1, schema versioning | Code | Lazy relations, entity graphs/batching, Flyway |
| AWS, EC2, RDS | Infra/Design | `infrastructure/aws`, architecture notes |
| EBS, VPC, security groups, Route53, metrics | Infra/Design | Terraform plus EC2/EBS rationale |
| Payment callbacks/webhooks | Code/Test | Payment gateway interface, verified webhook |
| Reconciliation, CORS, Stripe | Code | Scheduler, security CORS, Stripe adapter |
| Pagination, searching, sorting | Code/Test | Catalog endpoint and service |
| API optimization and resume framing | Code/Design | Cache, indexes, batching, metrics, README |

## LLD, Java, and design patterns

| Screenshot topic | Coverage | Location |
|---|---|---|
| OOP, modifiers, constructors | Code/Design | Domain entities and pattern notes |
| Inheritance and polymorphism | Code | `BaseModel`, gateway/publisher interfaces |
| Interfaces and abstract classes | Code | Payment/event contracts, abstract base entity |
| Processes and threads | Design/Code | Async event executor and container process model |
| Executors and callables | Code/Design | Spring async executor boundary |
| Synchronization and semaphores | Code/Design | Atomic limiter, DB locking; trade-offs documented |
| Generics | Code | `PageResponse<T>`, typed repositories |
| Collections | Code | Role sets, line maps, immutable responses |
| Streams and lambdas | Code | Mapping, grouping, strategy lookup |
| Exceptions/miscellaneous | Code | Typed domain/API exceptions |
| Singleton | Code/Design | Spring singleton services |
| Builder | Code | Stripe/JWT builders |
| Prototype | Design | Immutable-copy approach; cloning intentionally avoided |
| Adapter, strategy, factory | Code | Payment gateway architecture |
| Observer, decorator | Code | Transactional events, cache/security/transaction proxies |
| UML | Design | Mermaid class diagram |

## Testing, authentication, operations

| Screenshot topic | Coverage | Location |
|---|---|---|
| Unit testing/best practices | Test | JUnit 5 service tests |
| Mocking and Web MVC tests | Test | Mockito and standalone MockMvc |
| Unit-test implementation | Test/CI | Maven verify, JaCoCo, GitHub Actions |
| Auth vs authentication, tokens, BCrypt | Code/Test | Auth service and security config |
| JWT, OAuth2 | Code | JWT issuer plus OAuth2 resource server/client dependency |
| User service self | Code | `AppUser`, repository, register/login |
| Implementing OAuth2 | Code/Config | Standards-based resource server; provider creds remain external |
| Email and message queues | Code/Infra | Mail sender, Kafka publisher, Mailpit/Kafka compose |
| Spring Cloud | Code | OpenFeign client and compatible release train |
| Logging and monitoring | Code/Infra | trace filter, Actuator, Prometheus, CloudWatch |
| Containerization | Infra/CI | Dockerfile, Compose, image-build workflow |

## System design

| Screenshot topic | Coverage |
|---|---|
| CN 101, load balancing, consistent hashing | `SYSTEM-DESIGN.md`, ALB Terraform |
| Caching and Facebook feed | Redis implementation plus design comparison |
| CAP, primary/replica, SQL vs NoSQL | Architecture/system-design notes |
| NoSQL types/internals | System-design selection and internals checklist |
| Typeahead | Search evolution design |
| Messaging, ZooKeeper/Kafka | Kafka code/compose and KRaft/coordination notes |
| Elasticsearch | Search projection design |
| S3 | Encrypted/versioned Terraform bucket and object design |
| Uber | Transferable geospatial/delivery lesson, intentionally not fake runtime code |
| Popular interview questions | System-design checklist |
| Unique IDs and rate limiter | UUID entities and working request limiter |
| Hotstar | Burst/flash-sale capacity comparison |
| Microservices 1/2 | Modular-monolith extraction plan |

## Data engineering

| Screenshot topic | Coverage |
|---|---|
| Introduction, RDBMS, SQL basics | Operational schema and `commerce_analytics.sql` |
| Joins, unions, pivots | SQL lab |
| Window functions and case study | Ranking, rolling GMV, LAG queries |
| Efficient queries parts 1/2 | Indexes, query guidance, partition pruning |
| Hadoop 1/2 | HDFS/YARN concepts in `DATA-PLATFORM.md` |
| AWS data services | S3 data-lake architecture and Terraform |
| Hive basics/advanced | External partitioned Parquet table and statistics |
| Spark/PySpark basics/programming/operations | Batch job with typed transforms and partitions |
| Streaming/Spark Streaming/advanced streaming | Kafka structured-streaming job with watermark/checkpoint |
| Additional streaming services | Kafka→Spark→S3/Hive architecture |
| Modern data architectures | Data-platform diagram and zone separation |
| Data modelling | OLTP schema, immutable analytical events, partitions |
| Pipeline orchestration | Airflow 3 DAG |
| Distributed coordination/load balancing | Kafka partitions/groups and orchestration notes |
| Data pillars | Correctness, reliability, quality, security, observability, cost |

## Product management

| Screenshot topic | Coverage |
|---|---|
| Product management introduction | `PRODUCT.md` problem/users/outcomes |
| Product lifecycle 1/2 | Discover→improve lifecycle and exit criteria |
| Critical thinking 1/2 | Explicit decision questions and reliability trade-offs |
| Market/technical structure 1/2 | Alternatives and technical differentiation |
| MVP, prototyping, growth 1/2 | MVP boundaries, prototype test, growth metrics |
| Concept validation/developer role 1/2 | Assumptions, reversibility, evidence, SLO guardrails |
| Product analytics | Event taxonomy and north-star/guardrail metrics |
