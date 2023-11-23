# Composite Inverse Key Property Assignment

This metadata is used map a complex property using multiple Inverse Key Property Assignment instances. This means the join's direction is inverted and created using multiple references on properties belonging to the integration entity and the complex property type.

Metadata Property Name  | Type Signature  
------- | -----------
`CompositeInverseKeyPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.CompositeInverseKeyPropertyAssignment`

## General

This metadata functions as a conjunction. All references must be matched before the data is provided. The Composite Inverse Key Property Assignment metadata is similar to Inverse Key Property Assignment, but more properties are being compared. The valid type of a property on which this is configured is an entity or a set of entities.

> For more information about Inverse Key Property Assignment, see [Inverse Key Property Assignment](inverse_key.md).

Property | Description | Type
------| --------- | ------
Inverse Key Property Assignments | Set containing all Inverse Key Property Assignments the property should be joined on | `Set<InverseKeyPropertyAssignment>`
