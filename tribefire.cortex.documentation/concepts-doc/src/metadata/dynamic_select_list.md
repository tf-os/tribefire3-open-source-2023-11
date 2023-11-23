# Dynamic Select List

Metadata Property Name  | Type Signature  
------- | -----------
`DynamicSelectList` | `com.braintribe.model.extensiondeployment.meta.DynamicSelectList`

This metadata is similar to the VirtualEnum metadata. The only difference is that instead of having the constants defined in the metadata itself, a ServiceRequest can be configured on it, which will then return a list of possible values.

## General

One can configure the following properties for this metadata:

**requestProcessing** - the request processing is configured here. The client then triggers an execution of a GetSelectList service request in the services side, and it waits for a List of objects to be sent back. These objects are then the possible values to be assigned for that property.

**outlined** - used for knowing which editor should be in place for the property containing the metadata. If false (default), then an editor similar to the one used for the VirtualEnum should be used, this means a combo box is shown. If true, then a simplified selection UI should be used instead.

**disableCache** - by default, the possible values are cached, which means, after the first load of possible values, every consecutive call will directly return the cached values, without any call to the services. Setting this to true will force the reload of the possible values.
