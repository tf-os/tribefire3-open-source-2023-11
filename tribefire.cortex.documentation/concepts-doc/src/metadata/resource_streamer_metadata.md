# Resource Streamer Metadata

You can use these metadata to influence the streaming of resources.

Metadata Property Name  | Type Signature  
------- | -----------
`StreamWith` | `com.braintribe.model.extensiondeployment.meta.StreamWith`
`PersistWith` | `com.braintribe.model.extensiondeployment.meta.PersistWith`
`BinaryProcessWith` | `com.braintribe.model.extensiondeployment.meta.BinaryProcessWith`
`PreEnrichWith` | `com.braintribe.model.extensiondeployment.meta.PreEnrichWith`

> For information about implementing a resource streamer, see [Implementing a Binary Processor](asset://tribefire.cortex.documentation:tutorials-doc/resources/resource_streamer.md) and [Implementing a Resource Enricher](asset://tribefire.cortex.documentation:tutorials-doc/resources/resource_enricher.md).

## StreamWith

The `com.braintribe.model.extensiondeployment.meta.StreamWith` metadata binds a `ResourceSource` entity type with a `BinaryRetrieval` denotation instance which references an `BinaryRetrieval` processor capable of retrieving the binary data based on instances of `ResourceSource`.

## PersistWith

The `com.braintribe.model.extensiondeployment.meta.PersistWith ` metadata binds a `ResourceSource` entity type with a `BinaryPersistence` denotation instance which references an `BinaryPersistence` processor capable of persisting the binary data associated with instances of `ResourceSource`.


## BinaryProcessWith

The `com.braintribe.model.extensiondeployment.meta.BinaryProcessWith` metadata is a convenience type which simply extends from both `StreamWith` and `UploadWith`.

## PreEnrichWith

The `com.braintribe.model.extensiondeployment.meta.PreEnrichWith ` metadata binds a `ResourceSource` entity type with `ResourceEnricher` denotation instances, which are used to select `ResourceEnricher` processors to be invoked before the `BinaryPersistence` processor in order to enrich the `Resource` instance being created.

## Usecase Specific Mapping

All of above metadata can be further specialized with a `UseCaseSelector`. This usecase can be specified in resource upload requests which makes it possible to use multiple binary processors per access or select between multiple resource enrichers during upload.

For example you might have a local and a remote storage for the same access. So you create two `PersistWith` metadata on the  `ResourceSource` type. One points to a BinaryProcessor that stores the binary data in a remote storage in the cloud. This one does not have a `UseCaseSelector`, because it should be used by default. The other `PersistWith` metadata points to a `FileSystemBinaryProcessor` that stores binary content locally on the hard disk, which makes it available faster. This second one has a `UseCaseSelector` with its `useCase` property set to the string `"local-storage"`. This string can now be used to select this binary processor during upload like that:

```java
Resource resource =  resourceAccess
                            .create()
                            .name("myFileName.dat")
                            .useCase("local-storage")
                            .store(myInputStream);
```

Note that the usecase does not need to be specified again to download or delete the content because it will be stored in the `ResourceSource` of the uploaded resource and can be found automatically.

> Make sure the metadata with the `UseCaseSelector` has higher priority than the default one or otherwise it might be ignored.
