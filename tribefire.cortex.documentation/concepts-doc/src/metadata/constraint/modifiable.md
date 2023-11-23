# Modifiable

These metadata allow you to configure whether an element can be edited.

Metadata Property Name  | Type Signature  
------- | -----------
`Modifiable` | `com.braintribe.model.meta.data.constraint.Modifiable`
`UnModifiable` | `com.braintribe.model.meta.data.constraint.Unmodifiable`

## General

If you assign the Modifiable (or no) metadata to a property, you are able to edit this property. If you assign the Unmodifiable metadata to a property, you are not able to edit the property under **any** circumstances.

If a property marked as Unmodifiable is also marked as [Mandatory](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/mandatory.md), it is possible to set the property value during its creation.
