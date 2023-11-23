# Available Services

In this section the available services are listed and the possible ways to call this services are explained.

## Available services

### General Checks

Service | Description
-----| -----------
`RunCheckBundles` | Runs checks on the current node (local).
`RunAimedCheckBundles` | Runs checks on a dedicated node.
`RunDistributedCheckBundles` | Runs checks on all available nodes in a distributed environment.

Each of the above mentioned services can be further qualified with filters. For filtering see [Check Bundle Filters](checks.md#check-bundle-filters)

> Link to base request see [JavaDoc](javadoc:com.braintribe.model.deploymentapi.check.request.CheckBundlesRequest).

### Vitality Checks

`RunVitalityChecks` runs all checks of coverage `vitality` 
without requiring permission. The aggregation order can be configured via `aggregateBy`. Vitality checks do not support further filtering.

## Prepared DDRA Endpoints

There are following DDRA endpoints available:

Service | Description
-----| -----------
`/api/v1/healthz` | Executes vitality checks and returns results in the old format of the formerly known `HealthzServlet` in JSON format.
`/api/v1/checkVitality` | Executes vitality checks and returns results in rendered HTML format.
`/api/v1/check` | Executes checks and returns results in rendered HTML format.
`/api/v1/checkDistributed` | Executes checks distributed via Multicast and returns results in rendered HTML format.
`/api/v1/checkAimed` | Executes checks on specified `nodeId` and returns results in rendered HTML format.
`/api/v1/checkPlatform` | Executes checks on specified `nodeId` and returns results in rendered HTML format.

> Examples how to execute checks via DDRA see [Executing Checks via REST](executing-checks-rest.md)

## What's Next?

As we are familiar with available services and how they are called, we are ready to proceed to [Implementation of Checks](implementing-checks.md).
