## Execution of Checks via REST

This is a list of examples showing how check services can be called.

### Example 1
Using the distributed check request with `aggregateBy` and filter for `Module` via DDRA.

#### URL Syntax
```
http://localhost:8080/tribefire-services/api/v1/checkDistributed?depth=reachable&aggregateBy=node&aggregateBy=bundle&module=module.demo
```

#### Parameter Description

Key | Value | Description
--- | --- | ---
`aggregateBy` | node | The first level of aggregation is **node**
`aggregateBy` | bundle | The second level of aggregation inside **node** is **bundle**
`module` | module.demo | Filtering for module 

### Example 2
Using local check request with no specific filtering or aggregation via DDRA.

#### URL Syntax
```
http://localhost:8080/tribefire-services/api/v1/check
```

### Example 3

Executing check on a specific node via REST.

#### URL Syntax
```
http://localhost:8080/tribefire-services/api/v1/cortex/com.braintribe.model.deploymentapi.check.request.RunAimedCheckBundles?accept=text/html;spec=check-bundles-response&node=someNode
```

#### Parameter Description

Key | Value | Description
--- | --- | ---
`accept` | text/html | We want to render the result as HTML
`spec` |check-bundles-response | This **spec** tells the framework that a specific marshaller should be used for rendering
`depth` | reachable | reachable as the response contains in depth information

## See Also
* [Execution of Checks via tribefire Landing Page](executing-checks-landing-page.md)
* [Health Checks via cURL](executing-health-checks.md)