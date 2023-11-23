# OnEdit

This metadata allows you to assign a service processor or [Service Domain](asset://tribefire.cortex.documentation:concepts-doc/features/service_domain.md) that takes effect when a property has been edited and the changes have not been persisted yet. A property can have one or more `onEdit` metadata set, and in this case service processors are called in a row.

Metadata Property Name  | Type Signature  
------- | -----------
`OnEdit` | `com.braintribe.model.extensiondeployment.meta.OnEdit`

## General

The `OnEdit` metadata allows for sending a snapshot of an entity to a service processor where the entity has changes that have not been persisted yet. The service request that triggers the service processor carries only scalar properties (simple types and enum types) of the entity that is currently being edited. 

In the `requestProcessing` field, you must assign a `requestProcessing` instance that has a `serviceDomain` or a `serviceProcessor` or both. The following rules apply:

* The service model of the `serviceDomain` or `serviceProcessor` must depend on the `data-editing-api-model`.
* The service request in the service model must derive from the `onEditFired` type.
* the `domainId` of the request is set according to the `requestProcessing` in the `OnEdit` metadata. If it has a `serviceDomain`, then its `externalId` is used. Otherwise, the access of the current session is used as `domainId`.
* If there is a `serviceProcessor` set in the `requestProcessing`, then its `externalId` is used as `serviceId`.

## Example

In this example, we will describe a service processor that is triggered every time a property is changed. Let's say you have a `Content` entity that contains (along others) the following properties:

* `name` (String)
* `owner` (String)

You configure the `OnEdit` metadata on the `name` property of the `Content` type. In your `custom-deployment-model` you introduce a `CustomOnEditProcessor` which is bound with a `ProcessWith` metadata to the `OnEditFired` type.
The `data-editing-api-model` (which contains `OnEditFired`) is added as a dependency to the `custom-service-model`.

For the `CustomOnEditProcessor` denotation type, create and accordingly bind a service processor implementation. The implementation basically just sends back a `MessageWithCommand` notification to the client to show a message **Property changed** and updates the `owner` property for the entity to `New Owner`.

According to the setup, this service processor is always triggered whenever the `name` property of a `Content` instance is changed.

Here's the sample code of the processor implementation utilizing the notifications builder API to define `MessageWithCommand` notifications including an `ApplyManipulation` command fluently:

```java
@Override
	public Object process(ServiceRequestContext requestContext, OnEditFired request) {
		Content snapshot = (Content) request.getSnapshot();
		
		//@formatter:off
		Notify response = 
			Notifications.build()
				.add()
					.message()
						.info("Property Changed")
					.command()
						.applyManipulation(
							"Change Owner", 
							BasicManagedGmSession::new, 
							snapshot, c -> {
								c.setOwner("New Owner");
							},
							ManipulationRemotifier::remotify)
				.close()
				.toServiceRequest();
		//@formatter:on
		return response;
	}
```