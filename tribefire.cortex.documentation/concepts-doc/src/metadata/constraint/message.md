# Message

When executing a service request, the client always displays a generic message while waiting for its execution to finish.

These metadata allow you to configure custom messages to be shown to the user while awaiting for a service request execution to finish.

Metadata Property Name  | Type Signature  
------- | -----------
`Message` | `com.braintribe.model.meta.data.constraint.Message`
`StaticMessage` | `com.braintribe.model.meta.data.constraint.StaticMessage`
`DynamicMessage` | `com.braintribe.model.meta.data.constraint.DynamicMessage`

## General

If the Message metadata is configured, then while executing a service request, the user is signaled with a custom message.
One can configure the following properties for these metadata:

**icon** - an optional icon to be displayed alongside the message. By default, no icon is shown;

Furthermore, at certain cases, an static message is not enough. You may need to present a different message depending on the selected number of items, for example. Thus, that is why we have the two different types of `Message`.

`StaticMessage` - as the name suggests, it displays a simple static message, which can be configured via this property:

**message** - a `LocalizedString` which represents the localized message to be shown;

`DynamicMessage` - as the name suggests, it displays a dynamic message, which is returned via a `RequestProcessing` configuration, done via this property:

**requestProcessing** - the request processing is configured here. The client then triggers an execution of a `GetMessageData` service request in the services side, and it waits for a `MessageData` to be sent back.

The `MessageData` returned by the `GetMessageData` used by the `DynamicMessage` may have the following data on it:

**icon** - an optional icon which is dynamic. If present, it replaces the also optional static icon which may be configured directly in the `Message` metadata itself;

**message** - the required dynamic string message, which is the message to be shown;
