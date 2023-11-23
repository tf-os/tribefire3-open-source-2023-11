# Confirmation

When clicking in GME for the evaluation of a service, the service is always executed, no matter if it is a critical operation (for example, deleting multiple entries in the database) or not.

These metadata allow you to configure whether to display a confirmation message prior to executing those critical operations or not, giving also the possibility to cancel the execution if triggered by mistake.

Metadata Property Name  | Type Signature  
------- | -----------
`Confirmation` | `com.braintribe.model.meta.data.constraint.Confirmation`
`StaticConfirmation` | `com.braintribe.model.meta.data.constraint.StaticConfirmation`
`DynamicConfirmation` | `com.braintribe.model.meta.data.constraint.DynamicConfirmation`

## General

If the Confirmation metadata is configured, then prior to executing a service request, the user is prompted with a dialog where a confirmation message is shown, together with a button to allow, and another button to decline the execution of that service request.
One can configure the following properties for these metadata:

**icon** - an optional icon to be displayed alongside the confirmation message. By default, no icon is shown;

**okDisplay** - an optional `LocalizedString` which represents the localized value for the OK button in the dialog. If not present, then "OK" (or a localized version of it) is displayed;

**cancelDisplay** - an optional `LocalizedString` which represents the localized value for the Cancel button in the dialog. If not present, then "Cancel" (or a localized version of it) is displayed;

Furthermore, at certain cases, an static confirmation message is not enough. You may need to present a different message depending on the selected number of items, for example. Thus, that is why we have the two different types of `Confirmation`.

`StaticConfirmation` - as the name suggests, it displays a simple static confirmation message, which can be configured via this property:

**message** - a `LocalizedString` which represents the localized confirmation message shown in the dialog;

`DynamicConfirmation` - as the name suggests, it displays a dynamic confirmation message, which is returned via a `RequestProcessing` configuration, done via this property:

**requestProcessing** - the request processing is configured here. The client then triggers an execution of a `GetConfirmationData` service request in the services side, and it waits for a `ConfirmationData` to be sent back.

**mouseClick** - sometimes, we really must be aware that the user actually read the confirmation message, so we can configured that clicking in OK is not enough, and thus we can force the user to proceed only if they click OK while holding Shift, Ctrl or Alt. Of course then if this is the case, the dynamic confirmation message should inform that this is required.

The `ConfirmationData` returned by the `GetConfirmationData` used by the `DynamicConfirmation` may have the following data on it:

**icon** - an optional icon which is dynamic. If present, it replaces the also optional static icon which may be configured directly in the `Confirmation` metadata itself;

**message** - the required dynamic string message, which is the dynamic confirmation message to be shown in the dialog;

So, in the end, if the OK button is pressed, then we will proceed with the execution of the service request, and if the Cancel button is pressed, then we will cancel its execution.
> In case the Confirmation occurs for a template based action which shows the gima, then the gima will be only closed if the confirmation is OK. If cancel is used, then the dialog will remain opened.
