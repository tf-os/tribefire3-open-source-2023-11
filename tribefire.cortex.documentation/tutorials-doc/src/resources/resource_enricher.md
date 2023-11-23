# Implementing a Resource Enricher

A resource enricher is used to analyze the binary content of a `Resource` to set its metadata properties like `md5`, `fileSize` and most importantly a `resourceSpecification`, usually during upload. It may also change the binary content of a resource.

## Denotation Types
Your denotation type must extend the `com.braintribe.model.extensiondeployment.ResourceEnricher` entity type.

## Expert Implementation

Your expert needs to be a common `com.braintribe.model.processing.service.api.ServiceProcessor` for the `EnrichResource` request returning a `EnrichResourceResponse`.

```java
public class MyResourceEnricher implements ServiceProcessor<EnrichResource,EnrichResourceResponse> {

    public EnrichResourceResponse process(ServiceRequestContext context, EnrichResource request){
        // Get the resource from the request
        Resource resource = request.getResource();

        // Stream the resource binary content, set resource properties, ...

        // Create a response entity
        EnrichResourceResponse response = EnrichResourceResponse.T.create();

        // Attach the modified resource to the response if it was changed in any way
        if (resourceWasEnriched)
          response.setResource(resource);

        return response;
    }

}
```

Implementations can overwrite the binary data of a `Resource` being persisted if they are configured as pre enrichers in the `PreEnrichWith` metadata. To do so, the resource property on the returned `EnrichResourceResponse` must hold a new transient `Resource` which holds the overwritten data. Properties set to this returned `Resource` also enrich the `Resource` being persisted.

> For more information on resource streamer metadata, see [Resource Streamer Metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/resource_streamer_metadata.md)

Implementations may also bypass any enrichment. For example, imagine we have an enricher that sets the MIME type (`resource.setMimeType()`) on a `Resource` and this enricher was called for a resource which already had a MIME type set (`resource.getMimeType() != null`). This enricher then can simply do nothing.

## Binding

To be able to use the resource enricher, you need to bind it in your module either to a custom denotation type or hardwired.

The following example shows how to bind your custom denotation type to your expert implementation in the main space of your module.

```java
@Import
private TribefireWebPlatformContract tfPlatform;

@Import
private MyDeployablesSpace deployables;

@Override
public void bindDeployables(DenotationBindingBuilder bindings) {
    bindings.bind(MyCustomResourceEnricher.T)
				.component(tfPlatform.binders().resourceEnricherProcessor())
				.expertFactory(deployables::resourceEnricher);
}
```

Above code example assumes a method `resourceEnricher(ExpertContext<MyCustomResourceEnricher> context)` in `MyDeployablesSpace`, where the method parameter can be used to supply the respective denotation type instance which can be used to configure the expert instance with custom properties.

The following example shows how to bind your expert implementation hardwired.

```java
@Override
public void bindHardwired() {
    tfPlatform.hardwiredDeployables().bindResourceEnricherProcessor(
        "My Resource Enricher", // Descriptive name for the expert instance
        "my.resource.enricher", // Unique id (externalId) for the expert instance
        deployables::resourceEnricher // Actual expert instance supplier
    );
}
```

This time the resourceEnricher method must be without a parameter.
