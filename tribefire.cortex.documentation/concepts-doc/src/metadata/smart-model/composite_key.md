# Composite Key Property Assignment

This metadata is used to map a complex property using multiple Key Property Assignment instances.

Metadata Property Name  | Type Signature  
------- | -----------
`CompositeKeyPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.CompositeKeyPropertyAssignment`

## General

This metadata functions as a conjunction, since all references must be matched before the data is provided. This means the join is created using multiple Key Property Assignment references on properties belonging to the integration entity and the complex property type.

> For more information about Key Property Assignment, see [Key Property Assignment](key_property.md).

Property | Description | Type
------| --------- | -------
Key Property Assignments | A set containing all Key Property Assignments a property should be joined on | `Set<KeyPropertyAssignment>`
