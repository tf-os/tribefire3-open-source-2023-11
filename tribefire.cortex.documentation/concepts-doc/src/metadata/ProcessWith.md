# Process With

Metadata Property Name  | Type Signature  
------- | -----------
`ProcessWith` | `com.braintribe.model.extensiondeployment.meta.ProcessWith`

You can use this metadata to bind a Service Processor to a Service Request. When you add this metadata on the Service Request and assign a processor instance to its `processor` property, the system will automatically use this processor implementation when the Service Request is evaluated.

## Example

You can see an example of `ProcessWith` implementation in the [Creating a Scripted Service Processor](asset://tribefire.cortex.documentation:tutorials-doc/extension-points/creating_a_scripted_service_processor.md) tutorial.