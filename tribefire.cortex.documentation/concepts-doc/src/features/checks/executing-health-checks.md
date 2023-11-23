# Executing Health Checks via cURL

This section describes the execution and interpretation of checks.

## Get Health status

The Tribefire framework provides endpoint `/healthz` which is responsible for the execution of checks. 

### Sample Call using cURL
```
curl --insecure --request GET https://hostname:port/tribefire-services/api/v1/healthz --header "Accept: application/json"
```

Parameter | Default | Description
- | - | -
insecure | false | optional;  Should be set in case cURL does not accept the certificate provided by the server 
header/Accept | application/json| optional; Supported formats: `application/json`, `text/html` and `text/plain`

## JSON Response

In case the accept header is set to `application/json`, the endpoint answers with a JSON structured as `check results` per `node`.  

Sample output:

```json
{"_type": "flatmap", "value":[
 {"_type": "com.braintribe.model.service.api.InstanceId", "_id": "0",
   "applicationId": "master",
   "nodeId": "tf@NB-VIE01-CWI02#200128101854615f41f7f263064628ab"
  },[
   {"_type": "com.braintribe.model.check.service.CheckResult", "_id": "1",
    "entries": [
     {"_type": "com.braintribe.model.check.service.CheckResultEntry", "_id": "2",
      "checkStatus": "ok",
      "details": "Check infrastructure is ok",
      "name": "Base Check"
     },
     {"_type": "com.braintribe.model.check.service.CheckResultEntry", "_id": "3",
      "checkStatus": "ok",
      "details": "Active Threads: 0, Total Executions: 1, Average Execution Time: 182 ms, Pool Size: 0, Core Pool Size: 5",
      "name": "Thread Pool: Activation"
     },
     ...
     ...
   }
}
```
The maps's key value defines the type `InstanceId` which reflects the node (`applicationId`@`nodeId`) that was responsible for the check execution.  
The map's value defines the list of `CheckResult`s. A `CheckResult` returns a list of `CheckResultEntry`.  

A `CheckResultEntry` is qualified by:

Value | Description
-----| -----------
`status` | Set to one of the following values: `ok`, `warn` or `fail`
`name` | The name of the executed check e.g. "DB Connectivity Check"
`message` | The summarized check result message. 
`details` | Contains check result details like an exception stacktrace or further information of the check result

> For detailed information on check API related types see [Tribefire Checks](checks.md).

## Status Codes

If you're using a monitoring system, you might be interested in the different HTTP status codes returned.  

A `200 OK` is returned when all checks have passed, while a `503 Service Unavailable` is returned when at least one check has failed.  
If check result entries contain at least one `warn` or `fail`, the returned HTTP status code is always `503 Service Unavailable`. For example, if you have 4 checks:

- [`ok`, `ok`, `ok`, `ok`] the status is `200 OK`
- [`ok`, `ok`, `ok`, `warn`] the status is `503 Service Unavailable`

### Custom Status Code

You can define a custom status code for when a `warn` is thrown. You can do this by adding the `warnStatusCode=123` parameter to the URL when calling the endpoint. Sample call:

```
https://hostname:port/tribefire-services/api/v1/healthz?warnStatusCode=123
```

If the parameter is set, the defined status code is returned in case a `CheckResult` results in a `warn`. If this parameter is not defined, the default HTTP status code `503` is used.

## See also
### Executing Checks in General

* [Execution of Checks via tribefire Landing Page](executing-checks-landing-page.md)
* [Execution of Checks via REST](executing-checks-rest.md)
