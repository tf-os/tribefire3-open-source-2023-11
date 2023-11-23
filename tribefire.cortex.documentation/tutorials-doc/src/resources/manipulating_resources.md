# Manipulating Resources

Tribefire provides several methods allowing you to easily interact with resources.

## General

In Java, the `com.braintribe.model.processing.session.api.resource.ResourceAccess` interface is the entry point for manipulating resources.

## Retrieving Resources

You can use the `ResourceAccess.retrieve(Resource)` method to download binary data associated with a given resource, for example:

```java
InputStream in = resourceAccess.retrieve(resource).stream();
// OR
resourceAccess.retrieve(resource).stream(outputStream);
```

When you retrieve data via an `OutputStream`, you can be notified with the response details before the data is streamed. This comes in handy when you want to set cache headers based on the return values of the `BinaryRetrieval` processor, for example:

```java
resourceAccess
       .retrieve(myResource)
       .onResponse(
           (r) -> { // this is done before myOutputStream is created.
           }
         )
       .stream(myOutputStream);
```

### Conditions

When requesting binary data you can provide conditions upon which the data is to be retrieved. Currently, we have the following types of `StreamCondition` available:

* `com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch`
* `com.braintribe.model.resourceapi.stream.condition.ModifiedSince`

#### FingerprintMismatch

You can use this condition to provide the fingerprint (checksum) of the data. If the provided resource fingerprint matches the one from the server-side resource, the resource is not retrieved. This comes in handy when you want to check if you have to latest version of the resource, for example.

#### ModifiedSince

You can use this condition to provide a last modification date. If the provided resource was not modified since the provided date, the data is not retrieved.

## Creating Resources

You can use the `ResourceAccess.create()` method to create a new resource while uploading binary data.

```java
Resource resource =  resourceAccess
                            .create()
                            .name(“myFileName.dat”)
                            .store(myInputStream);
```

During the creation of a new resource, you can provide the `ResourceSource` source type to be used. The type you provide is used to select the designated `BinaryPersistence` capable of handling it, based on the `UploadWith` metadata configuration.

For more information on resource metadata, see [Resource Streamer Metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/resource_streamer_metadata.md)

If you do not specify a source type, `BinaryPersistence` associated with the `ResourceSource` type is used.

```java
Resource resource =  resourceAccess
                            .create()
                            .name(“myFileName.dat”)
                            .sourceType(FileSystemSource.T)
                            .store(myInputStream);
```

When creating a new resource, you can provide a use case description (String) to assist the resolution of the `UploadWith` metadata configuration which provides `BinaryPersistence` to be used for creating the resource:

```java
Resource resource =  resourceAccess
                           .create()
                           .name(“myFileName.dat”)
                           .sourceType(FileSystemSource.T)
                           .useCase(“myUseCase”)
                           .store(myInputStream);
```

## Deleting Resources

You can use the `ResourceAccess.delete()` method to delete an existing resource.

```java
resourceAccess.delete(myResource).delete();
```

> The `ResourceAccess.delete()` method only deletes the binary data. References to that data still remain in the system.

To create a custom resource streamer used to retrieve data, you must make sure that:

* your denotation type interface extends the `com.braintribe.model.extensiondeployment.BinaryRetrieval` interface.
* your expert type implements the `com.braintribe.model.processing.resource.stream.BinaryRetrieval` interface.

### BinaryRetrieval - get()

The `get()` method of the the `com.braintribe.model.processing.resource.stream.BinaryRetrieval` interface processes the `GetBinary` requests and returns a `GetBinaryResponse` object. Based on the information provided in the `GetBinary` request, implementations return a `CallResource` containing the relevant binary data as the `GetBinaryResponse.resource` property.  

```java
public GetBinaryResponse get(AccessRequestContext<GetBinary> context) {

    GetBinary request = context.getRequest();
    Resource resource = request.getResource();
    CallGmSession callGmSession = context.callGmSession();
    GetBinaryResponse response = callGmSession.create(GetBinaryResponse.T);

        response.setCacheControl(createCacheControl(resource));

        if (matchesCondition(resource, request.getCondition())) {
            CallResource callResource = callGmSession.callResource(createInputStream(resource)));
            response.setResource(callResource);
        }
    return response;
    }
```

### BinaryRetrieval - stream()

The `stream()` method of the `com.braintribe.model.processing.resource.stream.BinaryRetrieval` interface processes `StreamBinary` requests and returns a `StreamBinaryResponse` object. Based on the information provided in the `StreamBinary` request, implementations write the binary data to the `OutputStream` provided by the `StreamBinary.getCapture.openStream()` method.  

To notify other components evaluating the `StreamBinary` request about `StreamBinaryResponse` information before the stream is written to, implementations must notify a `StreamBinaryResponse` instance to the `ServiceRequestContext` via the `notifyResponse()` method before calling the `StreamBinary.getCapture.openStream()` method.

### Binding

Normally, you bind your custom denotation type to your custom processor implementation using the `BinaryRetrievalBinder` class which is returned by the `com.braintribe.cartridge.common.wire.contract.CommonComponentsContract.binaryRetrieval()` method, like in `CustomCartridgeSpace.java`:

```java
BasicDenotationTypeBindings bean = new BasicDenotationTypeBindings();
bean.bind(YourCustomDenotationType.T, commonComponents.binaryRetrieval(), deployables::yourCustomExpertImplementation);
```

The code above binds your custom denotation type to your custom expert implementation. You can, however, use the default `com.braintribe.model.extensiondeployment.BinaryRetrieval` denotation type shipped with tribefire and an expert implementing your custom logic for the default methods. This approach, although convenient when you do not want to introduce any custom methods and use the default ones only, has several disadvantages:

* You are not able to provide custom properties to your expert. The only available properties are the ones the default denotation type already declares.
* You must know the `externalId` property of the deployable beforehand.

The following example shows the code that binds the default denotation type to a custom expert implementation:

```java
BasicDenotationTypeBindings bean = new BasicDenotationTypeBindings();
bean.bind(BinaryRetrieval.T, "myExpertId", commonComponents.binaryRetrieval(), deployables::myExpert)
```

## BinaryPersistence

To create a custom resource streamer used to persist data, you must make sure that:

* your denotation type interface extends the `com.braintribe.model.extensiondeployment.BinaryPersistence` interface.
* your expert class implements the `com.braintribe.model.processing.resource.stream.BinaryPersistence` interface.

### BinaryPersistence - store()

The `store()` method of the `com.braintribe.model.processing.resource.persistence.BinaryPersistence` class processes `StoreBinary` requests and returns a `StoreBinaryResponse` object. Your implementation of this method must use an `InputStream` returned by the `StoreBinary.getCreateFrom.openStream()` method to store the binary data.

```java
public StoreBinaryResponse store(AccessRequestContext<StoreBinary> context) {
       StoreBinary request = context.getRequest();
       Resource resource = request.getCreateFrom();
       PersistenceGmSession session = context.getSession();
       stream(context, resource);
       Resource managedResource = createResource(session, resource);
       StoreBinaryResponse response = StoreBinaryResponse.T.create();
       response.setResource(managedResource);

       return response;
   }
```

### BinaryPersistence - delete()

The `delete()` method of the `com.braintribe.model.processing.resource.persistence.BinaryPersistence` class processes `DeleteBinary` requests and returns a `StoreBinaryResponse` object. Based on the `Resource` information provided in the incoming `DeleteBinary` request, your implementation must delete the binary data associated with it.

> The `delete()` method only deletes the binary data. References to that data still remain in the system.

```java
public DeleteBinaryResponse delete(AccessRequestContext<DeleteBinary> context) throws IOException {
        DeleteBinary request = context.getRequest();
        Resource resource = request.getResource();     
        deleteIfExists(resource);

     DeleteBinaryResponse response = DeleteBinaryResponse.T.create();
        return response;
    }
```

### Binding

Normally, you bind your custom denotation type to your custom processor implementation using the `BinaryPersistenceBinder` class which is returned by the `com.braintribe.cartridge.common.wire.contract.CommonComponentsContract.binaryPersistence()` method, like in `CustomCartridgeSpace.java`:

```java
BasicDenotationTypeBindings bean = new BasicDenotationTypeBindings();
bean.bind(YourCustomDenotationType.T, commonComponents.binaryPersistence(), deployables::yourCustomExpertImplementation);
```

The code above binds your custom denotation type to your custom expert implementation. You can, however, use the default `com.braintribe.model.extensiondeployment.BinaryPersistence` denotation type shipped with tribefire and an expert implementing your custom logic for the default methods. This approach, although convenient when you do not want to introduce any custom methods and use the default ones only, has several disadvantages:

* You are not able to provide custom properties to your expert. The only available properties are the ones the default denotation type already declares.
* You must know the `externalId` property of the deployable beforehand.

The following example shows the code that binds the default denotation type to a custom expert implementation:

```java
BasicDenotationTypeBindings bean = new BasicDenotationTypeBindings();
bean.bind(BinaryPersistence.T, "myExpertId", commonComponents.binaryPersistence(), deployables::myExpert)
```

## ResourceEnricher

To create a custom resource streamer used to enrich resources, you must make sure that:

* your denotation type interface extends the `com.braintribe.model.extensiondeployment.ResourceEnricher` interface.
* your expert class implements the `com.braintribe.model.processing.resource.stream.ResourceEnricher` interface.

### enrich()

The `enrich()` method of the `com.braintribe.model.processing.resource.stream.ResourceEnricher` interface processes `EnrichResource` requests and returns `EnrichResourceResponse` instances. This method changes (enriches) a Resource by setting properties and, if needed, overwriting the binary data.

Implementations can enrich the `Resource` being persisted in terms of properties. To do so, you must set the desired properties on the returned `EnrichResourceResponse`.

Implementations can overwrite the binary data of a `Resource` being persisted if they are configured as pre enrichers in the `UploadWith` metadata. To do so, the resource property on the returned `EnrichResourceResponse` must be a `CallResource` backed by an `InputStream` of the overwritten data. Properties set to this returned `CallResource` also enrich the `Resource` being persisted.

> For more information on resource streamer metadata, see [Resource Streamer Metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/resource_streamer_metadata.md)

Implementations may also bypass any enrichment. For example, imagine we have an enricher that sets the MIME type (`resource.setMimeType()`) on a `Resource` and this enricher was called for a resource which already had a MIME type set (`resource.getMimeType() != null`). This enricher then can simply do nothing.
