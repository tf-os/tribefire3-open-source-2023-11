# Service Processor

A service processor is a custom transformation engine that receives a request, processes it (hence the name) and returns a response.

## General

A service processor is an essential part of creating a custom DDSA service in Tribefire. A custom service is normally a functionality not available using any of the default extension points and consists of the following elements:

* service request
* service response
    > Modeling a custom service response is optional.
* service processor

A service processor is essentially a processing engine which takes input arguments (service request), processes them (service processor with custom logic), and returns them (service response). 

> For more information on DDSA, see [Denotation-driven Service Architecture (DDSA)](asset://tribefire.cortex.documentation:concepts-doc/features/ddsa.md). <!--<br/> For a list of available extension points, see [Developing Cartridges](developing_cartridges.md).-->

This document describes a service processor implemented in Java. <!--You can also create a scripted service processor to quickly provide simple customized actions.{%include tip.md content="For more information, see [Creating a Scripted Service Processor](creating_a_scripted_service_processor.md)."%}-->

For more information, see:
* [`AccessRequestProcessor`](javadoc:com.braintribe.model.extensiondeployment.access.AccessRequestProcessor)
* [`HardwiredParameterizedCheckProcessor`](javadoc:com.braintribe.model.extensiondeployment.check.HardwiredParameterizedCheckProcessor)
* [`HardwiredServiceProcessor`](javadoc:com.braintribe.model.extensiondeployment.HardwiredServiceProcessor) 
* [`HardwiredServiceProcessor`](javadoc:com.braintribe.model.extensiondeployment.HardwiredServiceProcessor) 
* [`ParameterizedCheckProcessor`](javadoc:com.braintribe.model.extensiondeployment.check.ParameterizedCheckProcessor)
* [`ScriptedAccessRequestProcessor`](javadoc:com.braintribe.model.extensiondeployment.script.ScriptedAccessRequestProcessor)
* [`ScriptedServiceProcessor`](javadoc:com.braintribe.model.extensiondeployment.script.ScriptedServiceProcessor)

## Service Processors and Custom Services

By creating the service model, you create the data the service processor will work on, but it is the processor that does the actual work, i.e. processes the request according to your custom logic. 

For more information on creating a service model, see [Creating a Service Model - Best Practices](asset://tribefire.cortex.documentation:concepts-doc/features/service-model/service_model.md).

Each extension point in tribefire consists of two main parts: the denotation type and the expert type. A service processor is no different. 

### Service Processor Denotation Type

Every service processor must have a denotation type and an expert. The denotation type must be an interface extending one or more available processor denotation types. See our API documentation for the list of all available denotation types.

You can also simply extend the `com.braintribe.model.extensiondeployment.ServiceProcessor` if you don't want to work in a predefined context the specializations of `ServiceProcessor` provide. In terms of code, your service processor's denotation type is an interface. If you want you can add properties to the denotation type, for example when you know your processor will use some form of an authentication token.

Let's see how the `FindByTextProcessor` denotation type looks like:

```java
package tribefire.demo.model.deployment;
import //...

public interface FindByTextProcessor extends AccessRequestProcessor {
	
	EntityType<FindByTextProcessor> T = EntityTypes.T(FindByTextProcessor.class);
}
```

As you can see, the `FindByTextProcessor` extends the `com.braintribe.model.extensiondeployment.access.AccessRequestProcessor` type, which is the denotation type for a processor that can operate in an access. 

<!--{%include tip.md content="For more information about the `FindByTextProcessor`, see [DDSA Service Processor - findByText](ddsa_findbytext.md)."%}-->

### Service Processor Expert Type

The expert type for your processor must implement one of the available expert interfaces. Even though they may provide different functionality, there are some areas in which they do not differ. Every service processor expert interface defines the `process()` method and all processors receive a `ServiceRequestContext` object as a parameter. It is then passed to the respective processor interfaces' `process()` method to supply it with meta information about the request. Furthermore, the `ServiceRequestContext` is an `Evaluator` that allows to invoke further requests of any kind. Thus, any implementation can build its functionality based on other DDSA functionality.

Your service processor expert type must know what its incoming type and the return types are. It is in the class signature that you define the request and response. In the example below, `FindByTextProcessor` implements the `com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor` interface and takes the `FindByText` request as the incoming type and uses `List<GenericEntity>` as the return type.  

[](asset://tribefire.cortex.documentation:includes-doc/findByText_expert_type_code.md?INCLUDE)

If you are working with data in an access, it is advisable to implement the `com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor< P extends AccessRequest, R >` interface as it uses the `AccessRequestContext` to operate within an access. If you are not working with accesses, we recommend to implement the `com.braintribe.model.processing.service.api.ServiceProcessor< P extends ServiceRequest, R extends Object >` interface.

In terms of code, your custom service processor's expert is a class implementing one of the available expert interfaces. Moreover, every service processor expert type must have the `process()` method, which should return the result of the processing. 

<!--{%include tip.md content="For information about how an example `process()` method implementation may look like, see [DDSA Service Processor - findByText](ddsa_findbytext.md) and [DDSA Service Processor - employeesByGender](ddsa_employeesbygender.md)."%}-->


## Service Processors and Wire

Wiring the denotation type and the expert type is how you let tribefire know what logic to apply when a custom service processor is invoked. You can have one processor bound to several denotation types.

Before you do that though, you must register your service processor's expert type as a managed instance. You do that by adding a proper declaration in a Wire space. 

<!--{%include tip.md content="For more information on Wire and managed instances, see [Wire in Detail](wire_in_detail.md#managedinstance)."%}-->

Let's see how the expert type for the `FindByTextProcessor` is declared. The following is a snippet from the `com.braintribe.cartridge.extension.wire.space.DeployablesSpace` class:

[](asset://tribefire.cortex.documentation:includes-doc/findByText_bean_code.md?INCLUDE)

>Note that it is the **expert** type that is being declared.

Within the context of the `findByTextProcessor`, you must implement the `findByTextProcessor()` method in your Wire space to instantiate your service processor expert. Without that, your custom extension is not complete.

When you declared your expert type as a managed instance, you must then bind a denotation type to an expert implementation. You start by passing the expert type's `T` value to the `bind()` method of a `DenotationTypeBindingsConfig` instance. Then, you follow with a component declaration taken from a component contract. The component declaration is the interface your denotation type extends. In the `FindByText` example, binding the denotation type to an expert implementation is done in the `com.braintribe.cartridge.extension.wire.space.spacevariants.CustomCartridgeSpaceMinimal` class, but you can register your expert type as a managed instance and bind the denotation type to the expert type in the same Wire space. 

[](asset://tribefire.cortex.documentation:includes-doc/findByText_binding_code.md?INCLUDE)

## Available Metadata

Assigning proper metadata is an essential step in creating a service processor because it is the metadata that binds a service request to a service processor. 

>You can map different service requests to the same processor.

You can use metadata selectors to make the mapping context-sensitive, for example role- or date-specific. 
>For more information on available selectors, see [Metadata Selectors](asset://tribefire.cortex.documentation:concepts-doc/metadata/selectors/metadata_selectors.md).

The available metadata are:

* `com.braintribe.model.extensiondeployment.meta.ProcessWith` 
    [`ProcessWith`](asset://tribefire.cortex.documentation:concepts-doc/metadata/ProcessWith.md) is an exclusive meta data whose resolution always results in maximum one processor used as a handler. It has a `processor` property that maps to a `ServiceProcessor`.

* `com.braintribe.model.extensiondeployment.meta.InterceptWith`
    * `com.braintribe.model.extensiondeployment.meta.PreProcessWith` 
    * `com.braintribe.model.extensiondeployment.meta.AroundProcessWith` 
    * `com.braintribe.model.extensiondeployment.meta.PostProcessWith`

    `InterceptWith`, `PreProcessWith`, `AroundProcessWith` and `PostProcessWith` are multi meta-data that allow for multiple results on resolution. Therefore a number of cross-cutting handlers can be assigned on the meta data's individual `processor` property. Their call sequence is determined by the value of the conflict priority parameter of the respective metadata.

    > For more information on conflict priority, see [General Metadata Properties](asset://tribefire.cortex.documentation:concepts-doc/metadata/general_metadata_properties.md).

Using the `ProcessWith` metadata, you can define which service processor is used for which request on the service model level. To assign the metadata, open your service model and find your service request entity type. Then, add a new instance of the `processWith` metadata and make sure to assign your service processor as the processor for the metadata. 

## Triggering a Service Processor

You can trigger a service processor in the following ways:

* programmatically in Java

    In Java, you will need an `Evaluator<ServiceRequest>` to invoke requests. If you have a processor that has a `ServiceRequestContext` you have that evaluator because the context is such. 

    To trigger a service request, you simply need a prepared request instance. With the evaluator, you actually invoke the request and receive the response.

    ```java
    Evaluator<ServiceRequest> evaluator = ... // get it from somewhere ;-)
    ValidateUserSession request = ValidateUserSession.T.create();
    request.setSessionId(“someSessionId”);

    UserSession userSession = request.eval(evaluator).get();
    ```

* using a REST call

* using Control Center by finding the service request and evaluating it

## Service Processor Metadata Validation

[](asset://tribefire.cortex.documentation:includes-doc/service_processor_metadata_validation.md?INCLUDE)

## What's Next?

For more information, see [Creating Service Requests](asset://tribefire.cortex.documentation:tutorials-doc/extension-points/creating_service_requests.md).
