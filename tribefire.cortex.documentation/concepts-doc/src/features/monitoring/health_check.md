# Health Check

Health check allows you to quickly obtain information about the status of your instance in a cloud container environment.

## General

When running Tribefire instances behind a load balancer, you frequently want to check the status of each instance so that the load balancer knows whether the instance is still alive (or if it must be deactivated/replaced). Also, in a project environment you want to ensure that your Tribefire instance is healthy and up and running.

Both `tribefire-services` and extensions (aka. cartridges) need to be checked for their respective status so Tribefire provides the possibility to execute health checks.

You can check for:

* Status of all/selected deployables, especially accesses
* Availability of database connections
* Reachability of extensions
* CPU load

The Health Check page of an individual cartridge (or the master cartridge) shows which node in the cluster actually responds and includes the status of the other nodes.

> Only the status of the responding node has effect on the return code, the other nodes are for information only.

## Custom Health Checks

The health checks for an extension can be extended with custom implementations of the `CheckProcessor` extension point. The health checks for `tribefire-services` can be extended with instances of `ScriptedCheckProcessor`. In both cases, `HealthCheck` instances in the cortex database activate `CheckProcessors` for the appropriate health check use case.

The health checks for an extension can be extended with custom implementations of the `CheckProcessor` extension point.

As a running extension can be activated or deactivated and generally depends on the presence of `tribefire-services`, it may execute a reduced set of checks (skip operations like authorization or cortex querying) when it's deactivated or when `tribefire-services` is missing . This reduction is done automatically.

In case extensions are in place, a base health check of type `ExtensionBaseHealthCheck` is executed for each extension:

* Name: Base Check
* Details: Check infrastructure is ok

### Custom Status Codes

You can define a custom status code for when a `WARN` is thrown. You can do this by adding the `warnStatusCode=statusCode` parameter to the URL when calling the health check endpoint, for example: `hostname:port/extensionId/healthz?warnStatusCode=123`.

If the parameter is set, the defined status code is returned in case a `CheckResult` results in a `WARN`. If this parameter is not defined, the default HTTP status code `503` is used.

## `HealthCheck`

The `com.braintribe.model.extensiondeployment.check.HealthCheck` entity links a list of check processors to the respective extension point and is persisted in the cortex database. When executing a health check, those instances are queried and further processed by checking the extension set and executing the respective checks.

Property | Description
-------- | ----------
`extension` | The respective extension for which health checks should be executed. If empty, the system assumes that `master/tribefire-services` should be checked.
`checks` | The list of health check processors to be executed.

## Executing Health Checks

To execute a health check for `tribefire-services`:

1. Using Postman (or your browser's address bar) send a GET HTTP request to the `hostname:port/tribefire-services/healthz` address.

> If you want to execute a health check for a different extension, change the URL to `hostname:port/extensionId/healthz`

2. Inspect the returned the `CheckResult` JSON file. For more information, see the **CheckResult** section of this document.

> If you're using a monitoring system, you might be interested in the different HTTP status codes returned. A `200 OK` is returned when all checks have passed, while a `503 Service Unavailable` is returned when at least one check has failed.

`CheckResult` has a property called `entries`. These entries are also entity types `CheckResultEntry`. If the entries contain at least one `warn` or `fail`, the returned HTTP status code is always `503 Service Unavailable`. For example, if you have 4 checks:

- [`ok`, `ok`, `ok`, `ok`] the status is `200 OK`
- [`ok`, `ok`, `ok`, `warn`] the status is `503 Service Unavailable`

### `CheckResult`

Based on the executed `CheckRequest`, a `CheckResult` is returned.

A `CheckResult` consists of `CheckResultEntry` instances. A `CheckResultEntry` holds a status property `checkStatus` which can have the following values:
* `ok`
*  `warn`
*  `fail`

```json
{"_type": "map", "value":[
 {"key":{"_type": "com.braintribe.model.check.service.CheckRequest", "_id": "0",
   "serviceId": "checkProcessor.hardwired.BaseHealthCheckProcessor"
  }, "value":{"_type": "com.braintribe.model.check.service.CheckResult", "_id": "1",
   "entries": [
    {"_type": "com.braintribe.model.check.service.CheckResultEntry", "_id": "2",
     "checkStatus": {"value":"ok", "_type":"com.braintribe.model.check.service.CheckStatus"}
    }
   ]
  }}
]}
```

## Creating Custom Health Checks

1. Make sure your expert implements the interface `com.braintribe.model.processing.check.api.CheckProcessor`.
2. Override the `check()` method and provide your custom health check logic there. The return type is `CheckResult`, so do your checks and create an instance with the respective check status:

```java
    @Override
    public CheckResult check(ServiceRequestContext requestContext, CheckRequest request) {

        CheckResult response  = CheckResult.T.create();

        CheckResultEntry entry = CheckResultEntry.T.create();

        // your custom logic here
        
        entry.setCheckStatus(CheckStatus.ok);

        response.getEntries().add(entry);

        return response;

    }
```

3. Bind the processor with `commonComponents.checkProcessor()`

    ```java
    bean.bind(YourCheckProcessor.T)
        .component(commonComponents.checkProcessor())
        .expertSupplier(deployables::yourCheckProcessor);
    ```

If you want your processor to be executed, add it to an existing `HealthCheck` instance or create a new one with the respective extension attached. The extension needs to match with the origin of the check processor.
