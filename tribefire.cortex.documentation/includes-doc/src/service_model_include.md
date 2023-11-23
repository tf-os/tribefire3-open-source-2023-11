A service usually consists of a `Request` and a `Response`, for example: 
* `GetInvoicesForCFY` as a request 
* List of `Invoice` as the response

![](asset://tribefire.cortex.documentation:includes-doc/images/service_model.png)

When a service is triggered, a service request entity is sent to tribefire. Then, tribefire checks its registry for which service handler to call, evaluates the service, and returns the result to the caller.

The entity types in the diagram above (and maybe more) are part of the service model. They are separated from the data model (e.g., `Invoice`) because they exclusively deal with the services. The data model itself is free of any service entity types. This allows for later reuse of the same model with different experts, i.e. services that deal with the actual business logic.

The service model, however, can be influenced by the data model, because a service model normally operates on entities found in the data model. 

