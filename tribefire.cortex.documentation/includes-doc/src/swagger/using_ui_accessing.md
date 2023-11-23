## Accessing Swagger UI

You can access the Swagger UI by clicking a link to a specific Swagger endpoint in the **Service Domains** section on the tribefire services landing page.

![](asset://tribefire.cortex.documentation:api-doc/images/swagger_endpoints.png)

<br/>

You can find the cortex access and the platform setup Swagger endpoints in the **Administration** section of the landing page.
Depending on the link you select, Swagger UI will display different endpoints. You can open the Swagger UI so that it displays all available REST API operations for an access or just a single endpoint, be it an entity type, a property, or a service request.

You can select the following Swagger endpoints:

* **CRUD-Entities**, which displays CRUD operations on entities
* **CRUD-Properties**, which displays CRUD operations on properties
* **API**, which displays service requests

### Switching Between Different API Endpoints via URL

Clicking a specific Swagger UI endpoint link on the tribefire services landing page changes the URL. You can use the URL to change the displayed Swagger endpoint but we recommend you use the links found on the landing page.

If you're not logged in and try to access the Swagger endpoint via URL, you will be asked to log in and your session ID will be stored in a cookie. Your subsequent calls from the Swagger UI will use that session ID so you will not have to add it manually to every call. 

> In the URLs below, it is assumed you are already logged in. Hence, the `sessionId=yourSessionId` parameter is omitted.

To display different endpoints in the Swagger UI, you change the URL you access the Swagger UI with. By changing the URL you can display the operations on:

* all entities in an access:

  `hostname:port/tribefire-services/rest/v2/entities/accessId`
  > You can also display all properties (`/properties/`) by changing the `/entities/` part of the URL. Also, you can display resources in a given package only by adding the `resource=fullyQualifiedName`, for example: `resource=com.braintribe.user`.

* a specific resource in an access, for example `com.braintribe.model.user.User`:

  `hostname:port/tribefire-services/rest/v2/entities/accessId?resource=com.braintribe.model.user.User`
 
  > If you don't specify an access but specify a resource, the list will contain **all** accesses that use the specific resource.

* all properties in an access:

  `hostname:port/tribefire-services/rest/v2/properties/accessId`

  > You can also display the properties for a specific resource by adding the `&resource=` parameter to the URL, for example `&resource=com.braintribe.model.accessapi`.


* all service requests in an access:

  `hostname:port/tribefire-services/api/v1/accessId`

You can also add the `enablePartition=true` parameter to each URL to trigger the display of the `partition` parameter in the endpoints. 