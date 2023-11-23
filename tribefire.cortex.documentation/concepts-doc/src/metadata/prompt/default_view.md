# DefaultView

This metadata allows you to configure which view is the preferred one to be used for opening a given type.

Metadata Property Name  | Type Signature  
------- | -----------
`DefaultView` | `com.braintribe.model.meta.data.prompt.DefaultView`

## General

If the DefaultView metadata is configured, then we use the configured view as the default view for opening a given type.
You can attach this metadata to entity types or templates.

The **viewIdentification** property can be configured with: `assembly` which is our list view, `thumbnail` which is our grid view or `gima` for using GIMA directly.
