# Tribefire Checks

Either in a local setup or a distributed setup the status of an instance needs to be reflected to decide if the instance is still alive or if it must be deactivated/replaced. The check API provides a standardized procedure to define and implement checks in order to obtain information about the status of a tribefire instance.  Such checks may include:

* Availability of database connections
* Reachability of extensions
* CPU load
* Status of deployables (e.g. accesses)

## Checks

The check can be of any kind: low level (e.g. Deadlock Detection) connectivity (e.g. remote components availability check) or a functional check (e.g. is my processor acting as expected).

The way a check is implemented is standardized via the check API:  
Your expert implementation returns a `CheckResult` which holds a list of entries:

An entry is qualified by:

Value | Description
-----| -----------
`status` | Set to one of the following values: `ok` `warn` or `fail`
`name` | The name of the executed check e.g. "DB Connectivity Check"
`message` | The summarized check result message. Keep it as short as possible and use `details` for further information
`details` | Details that can be attached here e.g. an exception stacktrace or further information for evaluation of the check result

For further details on how to implement a check refer to [Implementation and Execution of Checks](implementing-checks.md).

## The CheckBundle

The `CheckBundle` is the central place where checks are bundled and qualified. Usually `CheckBundle` instances are created within an initializer. Later on we will see an example how to  specify a `CheckBundle`.

Following bundle qualifications are available:

Coverage | Description
-----| -----------
`name` (mandatory) | The name of the bundle.
`checks` | The list of checks to be bundled for execution see [Checks](#checks).
`module` | The `Module` this bundle is executing checks for. 
`deployable` | The `Deployable` this bundle is executing checks for.
`coverage` | The coverage see [CheckCoverage](#check-coverage).
`weight` | The weight see [CheckWeight](#check-weight).
`labels` | A set of labels that qualify the bundle. This is of interest for later filtering of check results.
`roles` | The set of roles priviledged to execute this check bundle.
`isPlatformRelevant` | A boolean telling if the check bundle covers platform related checks.

## Qualifications

### Check Weight

The weight classifies the expected duration a bundle execution. There are following weight qualifications available:

* under100ms
* under1s
* under10s
* under1m
* under10m
* under1h
* under10h
* under1d
* under1w
* unlimited

### Check Coverage

The coverage classifies different purposes of checks. This classification does not make a statement about the performance.

Coverage | Description
-----| -----------
`vitality` | Covers checks in order to check the ability to process functionality and not the functionality itself. Such checks must not involve connectivity to remote components as the meaning of these checks is to control the lifecycle of processing instances in a cluster. Examples:<ul><li>Memory<li>CPU load<li>Pooling<ul><li>Threads<li>HTTP Connections<li>Database Connections</ul></li><li>Deadlock Detection</ul> **Such checks are also known as health checks.**
`connectivity` | Covers checks in order to check the ability to connect to remote components like databases hosts queues... This can evolve minimal functional usage like doing a query to a systable in order to provoke a roundtrip in case a library does not offer dedicated connectivity testing.
`functional` | Covers checks of any kind of actual functionality.

## Check Bundle Filters

Filters can be used to narrow the set of available check requests to a well-defined set. Several filters can be combined and applied to a check bundle service request:

Filter | Type | Description
-----| ----------- | -----------
`module` | Set\<String\> | Set of module global ids
`deployable` | Set\<String\> | Set of deployable external ids
`label` | Set\<String\> | Set of label strings
`name` | Set\<String\> | Set of check bundle names
`node` | Set\<String\> | Set of node ids
`weight` | Set\<CheckWeight\> | Set of check weights
`coverage` | Set\<CheckCoverage\> | Set of check coverage
`role` | Set\<String\> | Set of roles
`isPlatformRelevant` | boolean | Boolean for filtering platform checks

## Check Bundle Aggregation

A check bundle response (the result of an exected check bundle service) can be aggregated (argument on service level is `aggregateBy`) by several of following kinds:
* node
* label
* module
* deployable
* processor
* weight
* effectiveWeight
* coverage
* status
* bundle
* role

Depending on the representation endpoint, the result is rendered based on this configured aggregation kind(s). This is especially helpful when you want to group the executed requests for example by processor or the check weight.

> Examples how to configure aggregations see [Example Services](executing-checks-rest.md)

## What's Next?

As we are familiar with required terms now, we are ready to proceed to [Available Services](check-api.md).
