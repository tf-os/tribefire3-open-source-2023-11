# hibernate-module

Core module for functionality built on top of `Hibernate` ORM framework, especially the `Hibernate Acess`.

## bindHardwired()

### Denotation Transformers

Source Type | Target Type | Details
-|-|-
DatabaseConnectionPool | HibernateAccess | -
HibernateAccess | JdbcUserSessionService | SessionService's connection pool is `HibernateEnhancedConnectionPool`, which creates the DB schema on first usage
HibernateAccess | JdbcCleanupUserSessionsProcessor | Same as `JdbcUserSessionService` case.

### MetaData Selector Experts

Binds experts for selectors from `hibernate-deployment-models`:
* `HibernateDialectSelector`
* `HibernateDbVendorSelector`

## bindDeployables()

Denotation Type | Deployable Component
-|-
`HibernateAccess` | `IncrementalAccess`
`HibernateEnhancedConnectionPool` | `DatabaseConnectionPool`

## bindInitializers()

Access: `cortex`

On `HibernateAccess` type
* configures an icon
* configures `AccessModelExtension` with `basic-hbm-configuration-model` (from [hibernate-shared-md-priming](../hibernate-shared-md-priming/readme.md)), thus making `globalId` and `partition` unmapped by default for every `HibernateAccess`
* configures `ServiceModelExtension` to add `native-persistence-api-model`, thus ensuring every `HibernateAccess` support `NativePersistenceRequest`


On `HibernateSessionFactory` type
* hides it in `GME`, as it is not fully supported

// TODO next 2 might need improvements:

Maps the following properties (directly on the model types) as CLOBS:
Type | Properties
-|-
`ServiceRequestJob` | `serializedRequest`, `serializedResult`, `errorMessage`, `stackTrace`
`AsynchronousRequestRestCallback` | `url`, `customData`
`AsynchronousRequestProcessorCallback` | `callbackProcessorCustomData`

Un-maps `FileResource` entity.

## See also:

* [hibernate-accesses-edr2cc-module](../hibernate-accesses-edr2cc-module/readme.md)
* [hibernate-leadership-edr2cc-module](../hibernate-leadership-edr2cc-module/readme.md)
* [hibernate-shared-md-priming](../hibernate-shared-md-priming/readme.md)