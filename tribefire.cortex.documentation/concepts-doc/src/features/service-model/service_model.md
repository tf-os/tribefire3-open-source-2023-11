# Service Model

## General
Like any object in tribefire, services are also modeled. Because of this, the same principles which are applied to models that define data can be applied to models that define services. This leads to a microservices-like approach as each service is provided using a model and its own API. Even though they are separate from each other, a service model usually depends on the business model.

>For more information on models, see [Models](../models/models.md) and [Models-in-details](../models/models_in_detail.md).


## General

Service model defines the parameters and the return values of requests and responses in tribefire - not the business logic. An implementation of a service evaluates custom business logic you require in your application, which, for example, may include returning certain information from an access.

[](asset://tribefire.cortex.documentation:includes-doc/service_model_include.md?INCLUDE)

As every service available in tribefire is also modeled using a service model, you might notice you can query those out-of-the-box service models using GMQL.

When you create a brand new service model, you have to remember that your service model will not have all the dependencies the tribefire service models have by default. By managing the dependencies of your service model, you can influence its behavior, like being able to query your service model using GMQL. 

> Treat the following as best practices, not ready-made solutions.

## Modeling a Request

When modeling a request, you normally create an interface which extends one of the available service types. 

[](asset://tribefire.cortex.documentation:includes-doc/service_model_api.md?INCLUDE)

To introduce custom properties to your request, create the appropriate getters and setters for them. Make sure that the getter for your property has the exact return type you're passing as an argument in the setter for your property.

The type of the property can be a simple or a complex type as long as it is an entity type in tribefire. 

> You cannot use a class from an external library as a property in a model.

[](asset://tribefire.cortex.documentation:includes-doc/allowed_property_types.md?INCLUDE)

### Request Model Example

```java
public interface GetEmployeeRequest extends AccessDataRequest, AuthorizedRequest {

    EntityType<GetEmployeeRequest> T = EntityTypes.T(GetEmployeeRequest.class);

    EmployeeID getEmployeeId();
    void setEmployeeId(EmployeeID employeeId);

    String getPosition();
    void setPosition(String position);
```

An example implementation of the `GetEmployeeRequest` could look like so: 

```java
public EmployeeID process(AccessRequestContext<GetEmployeeRequest> context) {
	final GetEmployeeRequest request = context.getRequest();
	
	final EmployeeID employeeId = request.getEmployeeId();
	
	return employeeId;
}

``` 

## Modeling a Response

When modeling a response, you also create an interface which extends one of the available service types. The response can be any type you want as long as it directly or indirectly extends `GenericEntity`. This is because every entity type must have an ID. tribefire handles every ID as `Object` but we provide convenience interfaces if you wish your ID to be of a different data type.

Note that you don't necessarily have to model a response for your service. It is advisable to model a response when your actual response will contain more than one data type or when you want to explicitly influence how it looks.

If you don’t have specific ID property needs, we recommend to extend `StandardIdentifiable`.

Extendable Types                 | Description
------                           | ------
`StandardIdentifiable`           | Provides the ID type - `Long`
`StandardStringIdentifiable`     | Provides the ID type - `String`
`StandardIntegerIdentifiable`    | Provides the ID type - `Integer`


You can also extend one of the available response types:

[](asset://tribefire.cortex.documentation:includes-doc/service_model_api_response.md?INCLUDE)

### Response Model Example

```java
public interface GetEmployeeResponse extends StandardIdentifiable {
    EntityType<GetEmployeeResponse> T = EntityTypes.T(GetEmployeeResponse.class);
    
    Employee getEmployee();
    void setEmployee(Employee e);
}
```

## Asynchronous Requests

Every DDSA request can be wrapped by a `com.braintribe.model.service.api.AsynchronousRequest`. This means that the wrapped request is not executed immediately, but rather executed asynchronously in the background. The returned `com.braintribe.model.service.api.result.AsynchronousResponse` contains a correlation ID. The correlation ID is the unique ID of a request which, later, when you get the callback helps you to match the callback with the original request.

If specified in the `AsynchronousRequest`, a callback can be performed once the asynchronous processing has finished. This callback can be:

* the invocation of a callback processor
* a REST URL

The callback processor is a regular service processor which has to process a `com.braintribe.model.service.api.callback.AsynchronousRequestCallbackRequest` request. In this request, you must provide the correlation ID and the result of a `Failure` in case of an exception.

In a REST callback, you must provide the callback URL. Apart from the mandatory URL, you can provide any custom data may as part of the callback which means you can create your own correlation information. If you want to use the correlation ID, it is sent in a custom HTTP header: `X-TF-Async-Correlation-Id`.

### Asynchronous Request Example

In the example below, the `GetHotThreads` service request is wrapped into an `AsynchronousRequest` - the original request is set as a property of the asynchronous one. Then a REST callback is specified so when the service is evaluated, the callback is sent.

```java
PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("https://localhost:8443/tribefire-services").authentication("cortex", "cortex").done();
PersistenceGmSession session = sessionFactory.newSession("cortex");

GetHotThreads ght = GetHotThreads.T.create();

AsynchronousRequest asyncReq = AsynchronousRequest.T.create();
    asyncReq.setServiceRequest(ght);

AsynchronousRequestRestCallback callback = AsynchronousRequestRestCallback.T.create();
    callback.setCustomData("hello, world custom data");
    callback.setUrl("http://localhost:1800/test-app/callback");

    asyncReq.setCallback(callback);

AsynchronousResponse response = asyncReq.eval(session).get();

String correlationId = response.getCorrelationId();
System.out.println("Sent request. Correlation Id: " + correlationId);
```

## Best Practices

* Describe the scenarios and the use cases you are interested in detailing the data which should be sent/received.

    For general information on best practices for modeling, see [Modeling Best Practices](../models/modeling_best_practices.md).

* Remember that requests and responses are simple information holders - they do not have any logic apart from declaring pairs of getters and setters for properties.

* Decide whether you want to expose your data model or hide it from the client. 

    Depending on the situation (stability of the data model, size of the data model), you might want to hide the data model from the client by abstracting it in the service model. But, this means your service needs to translate between the underlying data model and the data model which is used in the service model.

    Say your underlying data model has a type which has 200 properties. If you add an instance of that type to a service response, you will return all the properties. This is not always desirable. You might be able to construct traversing criteria, to remove the unnecessary data, but you have still exposed all those properties in the data model.

    Instead, you could have an intermediate data model inside your service model. Then, the service processor would translate between the (large) underlying data model and the (small) intermediate data model. It is, however, a lot simpler if you simply re-use the underlying data model in the service requests and just pass data straight through, without any translation.

* Define the request/response types (in terms of the data they use) which are appropriate for the client. 
    > Reusability is key.

    Having too many types means you might need to abstract more, too few means you might be conflating use cases into monolithic chunks.

* Define the required service processors. 
    > One processor might be able to handle multiple different request types.

    The service processor is just an intermediary between the incoming/outgoing requests/responses, the underlying data model (e.g. the database) and tribefire.

    > For more information on implementing a service processor, see the example implementations of DDSA Service Processor - employeesByGender and DDSA Service Processor - findByText in the Demo cartridge.


* Enable GMQL querying for your model.

    By default, you cannot use GMQL to query your model. To enable it, include the `access-api-model` as a dependency in your service model.

    >For more information on GMQL, see [GMQL](asset://tribefire.cortex.documentation:concepts-doc/features/gmql.md)

* Enable custom resource creation and streaming

    Sometimes you may not want to use the out-the-box streaming functionality offered by tribefire. 

    > See [Manipulating Resources](asset://tribefire.cortex.documentation:tutorials-doc/resources/manipulating_resources.md) for Java-specific information on streaming resources.
    
    There may be times when you want to create the file during the service request, for example when you want to implement downloading a log file or a `.zip` file containing other files. To do that, make sure you have a dependency to the `resource-api-model` artifact and your service model response uses the `Resource` entity type:

    ```java
    import com.braintribe.model.generic.reflection.EntityType;
    import com.braintribe.model.generic.reflection.EntityTypes;
    import com.braintribe.model.resource.Resource;


    public interface Logs extends LogsResponse {

	final EntityType<Logs> T = EntityTypes.T(Logs.class);
	
	void setLog(Resource log);
	Resource getLog();
	
    }
    ```

    After introducing the `Resource` entity type (in this example, the `Logs` entity contains the `Resource` entity type) in your service model, make sure that your expert implementation uses the entity type which contains the `Resource` entity:

    ```java
    public Logs getLog(ServiceRequestContext context, GetLogs request) throws Exception {
    ...    
    }
    ```

    For information on how to implement a resource streamer, see [Implementing a Resource Streamer](asset://tribefire.cortex.documentation:tutorials-doc/resources/resource_streamer.md)

* Make sure that the name of the service describes what the service does. It should be clear from the name of the request type what it is supposed to do. For example, if you have a service that returns employees, it's better to call it `GetEmployeeService` than simply `EmployeeService`.

## What's Next?

See the following documents for more information:

* [Creating a Scripted Service Processor](asset://tribefire.cortex.documentation:tutorials-doc/extension-points/creating_a_scripted_service_processor.md)
* [Creating Service Requests](asset://tribefire.cortex.documentation:tutorials-doc/extension-points/creating_service_requests.md)