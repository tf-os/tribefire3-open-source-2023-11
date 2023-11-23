# Referenceable

This metadata properties allow you to configure whether you can select a existing entity for a property or only allow a new one to be created.

Metadata Property Name  | Type Signature  
------- | -----------
`Referenceable` | `com.braintribe.model.meta.data.constraint.Referenceable`
`NonReferenceable` | `com.braintribe.model.meta.data.constraint.NonReferenceable`

## General

If you configure the Referenceable metadata on a property, then you can select preexisting entities. If you configure the NonReferenceable metadata however, you can only assign newly created entities.

## Example

If you configure the NonReferenceable metadata on a property, the **Add** button is not displayed in Explorer. The only available option is the **Open** button, which only gives you the option to create a new instance.
