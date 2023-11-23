# gm-db-locking-module

## bindHardwired()

### Denotation Transformers

#### Morphers:

Source Type | Target Type
-|-
DatabaseConnectionPool | DbLockManager

#### Edr2CC Enrichers 

Following enricher is **only relevant for the platform locking**, recognized by "`tribefire-locking-db`" or "`tribefire-locking-manager`" bindIds.

`DbLockManagerEdr2ccEnricher`: For locking related `DbLockManager` this configures its `name`, `globalid`, `externalId` and `lockTtlInMillis` (based on `TRIBEFIRE_DBLOCK_TIMEOUTMS` variable).

## bindDeployables()

Denotation Type | Deployable Component
-|-
`DbLockManager` | `LockManager`
