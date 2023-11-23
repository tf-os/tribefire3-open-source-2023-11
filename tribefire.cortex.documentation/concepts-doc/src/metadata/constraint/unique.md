# Unique

These metadata allow you to configure whether a property must contain unique values.

Metadata Property Name  | Type Signature  
------- | -----------
`Unique` | `com.braintribe.model.meta.data.constraint.Unique`
`NonUnique` | `com.braintribe.model.meta.data.constraint.NonUnique`

## General

If the Unique metadata is configured, then this property must have a unique value before it can be committed. If the NonUnique(or no) metadata is assigned, then a property does not require a unique value.
> If a property is unique, this means no two instances of this property can have the same value.
