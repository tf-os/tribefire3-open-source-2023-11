# Default Navigation

This metadata allows you to configure which property is displayed by default when opening an entity in Explorer.

Metadata Property Name  | Type Signature  
------- | -----------
`DefaultNavigation` | `com.braintribe.model.meta.data.prompt.DefaultNavigation`

## General

This metadata defines a default property to be used when navigating an entity. Normally, when using the **Open** action or double clicking an entity, you navigate to an entity, showing all its properties. By defining this metadata, you can specify a particular property which you navigate to.
>The property you want to navigate to must be a complex property.

## Example

You assign this metadata on the entity level. When you create the metadata, in the `DefaultNavigation()` window, you specify the property you want to navigate to by assigning it to the property `field`.
