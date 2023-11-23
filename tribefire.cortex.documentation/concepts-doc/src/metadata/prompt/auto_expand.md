# AutoExpand

This metadata allows you to configure whether the nodes within the list view will be automatically expanded or not.

Metadata Property Name  | Type Signature  
------- | -----------
`AutoExpand` | `com.braintribe.model.meta.data.prompt.AutoExpand`

## General

If the AutoExpand metadata is configured, then we auto expand nodes within the list view.

We use the property called **depth** to prepare a custom `TraversingCriterion` for assuring that the required data will be fetched from the services.
The **depth** can be configured with either a number, or some known depth configuration (either "shallow" or "reachable").

You can attach this metadata to entity types or templates.
