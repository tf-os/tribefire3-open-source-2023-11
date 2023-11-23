# Implementing a Binary Processor

A binary processor is an extension point that allows you to stream binary data.

## General

A binary processor allows you to upload and download binary data. By default, you can use the out-of-the-box streamers shipped with tribefire which support file-system- or SQL/JDBC-based persistence. To stream data from other locations (like a not yet supported database), you must create a custom binary processor.

You may choose to only implement **binary retrieval** (resource download) or **binary persistence** (resource manipulation) behavior or both.

## Denotation Types

Depending on what you want to support, there are two different denotation base types
* `com.braintribe.model.extensiondeployment.BinaryRetrieval`
* `com.braintribe.model.extensiondeployment.BinaryPersistence`

To be able to later map and use your streamer you need your denotation type to extend one or both the interfaces, depending on your needs or choose the respective hardwired binding if you prefer to bind hardwired. Further information can be found below in the [section about binding](#binding).

## Expert Implementation

Your expert needs to be a common `com.braintribe.model.processing.service.api.ServiceProcessor` for
* `BinaryRetrievalRequest`, `BinaryRetrievalResponse` in the `com.braintribe.model.resourceapi.stream` package in case you only want to handle **binary retrieval**
* `BinaryPersistenceRequest`, `BinaryPersistenceResponse` in the `com.braintribe.model.resourceapi.persistence` package in case you only want to handle **binary persistence**
* `BinaryRequest`, `BinaryResponse` in the `com.braintribe.model.resourceapi.base` package if you want to handle both kinds of requests

### `BinaryRetrievalRequest`s - Resource Download

There are 2 concrete subtypes of the abstract `BinaryRetrievalRequest` your ServiceProcessor needs to support:
* `GetBinary` which returns a `GetBinaryResponse` with the full `Resource` entity
* `StreamBinary` which returns a `StreamBinaryResponse` without any additional information about the resource and operates with a `CallStreamCapture`

#### `GetBinary`
The expert needs to find the binary content of the resource requested with the `resource` request property and return a transient `Resource` entity in the `resource` property of the response entity. If the `name` or `mimeType` properties are set in the request resource they should also be transferred to the response resource entity. Typically you will find the binary data with a key that is stored in a property in the `ResourceSource` of the request resource (e.g. the `path` property of the `FileSystemSource`).

If the `condition` request property is set, the resource must not be returned if the respective condition is not met. In that case the `resource` response property must be set to `null`.

If the `range` request property is set, only the respective part of the resource binary must be streamed. In that case also the following response properties must be set:
* `rangeStart` - the actual start position of the stream
* `rangeEnd` - the actual end position of the stream
* `size` - the full size of the resource binary (which is greater or equal of the size of the streamed binary range)
* Further the `fileSize` property of the response resource must be set to the exact size of the streamed binary range.

#### `StreamBinary`

The expert needs to find the binary content of the resource requested with the `resource` request property and write its binary data to the `OutputStream` provided by the `openStream()` method of the `CallStreamCapture` provided in the `capture` request property.

If the `condition` request property is set, the resource must not be streamed if the respective condition is not met. In that case the `notStreamend` response property must be set to `true`.

If the `range` request property is set, only the respective part of the resource binary must be streamed.

To notify other components evaluating the `StreamBinary` request about `StreamBinaryResponse` information before the stream is written to, implementations must notify a `StreamBinaryResponse` instance to the `ServiceRequestContext` via the `notifyResponse()` method before calling the `StreamBinary.getCapture.openStream()` method.

### `BinaryPersistenceRequest`s - Resource Manipulation

There are 2 concrete subtypes of the abstract `BinaryPersistenceRequest` your ServiceProcessor needs to support:
* `StoreBinary` which returns a `StoreBinaryResponse` and is used to upload new binary content to your storage.
* `DeleteBinary` which returns a `DeleteBinaryResponse` and is used to delete binary content from your storage.

Please note that your expert is not meant to commit or manipulate any entities in an access but is only responsible for manipulating binary content.

#### `StoreBinary`

The expert must persist new binary data in your storage, which it gets from the `InputStream` returned by the `openStream()` method of the Resource provided in the `createFrom` request property.

The `resource` response property must contain a transient `Resource` entity with a transient `ResourceSource` in its `resourceSource` property. You will need the ResourceSource to be able to later reference and retrieve the binary data again. Your resource source will contain a key that points to your data (like the `path` property of the `FileSystemSource`). Often you will want to create your custom subtype of `ResourceSource` for that purpose.

Please also make sure to transfer all properties of the request resource to the response resource because metadata properties like `name` or `md5` of the resource are usually enriched before your streamer is called and otherwise would get lost.

#### `DeleteBinary`

The expert needs to find the binary content of the resource requested with the `resource` request property and delete it from your storage. If successful, it returns a plain `DeleteBinaryResponse` entity with no properties set.

## Implementation Convenience

In Order to conveniently handle multiple requests with a single ServiceProcessor instance you can derive from AbstractDispatchingServiceProcessor like that:

```java
public class MyBinaryProcessor extends AbstractDispatchingServiceProcessor<BinaryRequest, BinaryResponse> {

	@Override
	protected void configureDispatching(DispatchConfiguration<BinaryRequest, BinaryResponse> dispatching) {
		dispatching.register(StreamBinary.T, this::stream);
		dispatching.register(GetBinary.T, this::get);
		dispatching.register(StoreBinary.T, this::store);
		dispatching.register(DeleteBinary.T, this::delete);
	}

    private StreamBinaryResponse stream(ServiceRequestContext context, StreamBinary originalRequest){/* ... */}
	private GetBinaryResponse get(ServiceRequestContext context, GetBinary originalRequest){/* ... */}
	private DeleteBinaryResponse delete(ServiceRequestContext context, DeleteBinary originalRequest){/* ... */}
	private StoreBinaryResponse store(ServiceRequestContext context, StoreBinary request){/* ... */}
}
```

The AbstractDispatchingServiceProcessor will inspect the incoming request for you and dispatch to your respective implementation method that can handle that kind of request.

## Binding

To be able to use the binary processor, you need to bind it in your module either to a custom denotation type or hardwired.

The following example shows how to bind your custom denotation type to your expert implementation in the main space of your module. In that example the expert supports both *binary persistence* and *binary retrieval*. If your expert only supports one of them, simply omit the respective two lines.

```java
@Import
private TribefireWebPlatformContract tfPlatform;

@Import
private MyDeployablesSpace deployables;

@Override
public void bindDeployables(DenotationBindingBuilder bindings) {
    bindings.bind(MyCustomBinaryProcessor.T)
				.component(tfPlatform.binders().binaryPersistenceProcessor())
				.expertFactory(deployables::binaryProcessor)
				.component(tfPlatform.binders().binaryRetrievalProcessor())
				.expertFactory(deployables::binaryProcessor);
}
```

Above code example assumes a method `binaryProcessor(ExpertContext<MyCustomBinaryProcessor> context)` in `MyDeployablesSpace`, where the method parameter can be used to supply the respective denotation type instance which can be used to configure the expert instance with custom properties.

The following example shows how to bind your expert implementation hardwired. There are three binder methods depending whether you want to bind your expert for *binary persistence*, *binary retrieval* or both

```java
@Override
public void bindHardwired() {
    tfPlatform.hardwiredDeployables().bindBinaryPersistenceProcessor(
        "My Binary Persistence Processor", // Descriptive name for the expert instance
        "my.binary.persistence.processor", // Unique id (externalId) for the expert instance
        deployables.binaryProcessor() // Actual expert instance
    );

    tfPlatform.hardwiredDeployables().bindBinaryRetrievalProcessor(
        "My Binary Retrieval Processor", // Descriptive name for the expert instance
        "my.binary.retrieval.processor", // Unique id (externalId) for the expert instance
        deployables.binaryProcessor() // Actual expert instance
    );

    tfPlatform.hardwiredDeployables().bindBinaryServiceProcessor(
        "My Binary Processor", // Descriptive name for the expert instance
        "my.binary.processor", // Unique id (externalId) for the expert instance
        deployables.binaryProcessor() // Actual expert instance
    );
}
```

## Mapping

To configure when to use your binary processor you need to map it with metadata on the respective `ResourceSource` entity type.

> For more information on resource streamer metadata, see [Resource Streamer Metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/resource_streamer_metadata.md)
