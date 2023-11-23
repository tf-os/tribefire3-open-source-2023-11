# Key Property Assignment

This metadata is used to create a simple join between complex properties, either single or multiple aggregations.

Metadata Property Name  | Type Signature  
------- | -----------
`KeyPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment`

## General

The simple join functions by creating a reference between a property belonging to a mapped integration entity and a property in the entity type of the complex property. When these properties are matched, the data is provided. Key Property Assignment contains two mapping-specific properties:

* `keyProperty`
* `property`

Property | Description | Type
------| --------- | -------
`keyProperty` | The key property, against which the Property will be compared. In a simple join this will be the entity type of the complex property. | `GmProperty`
`property` | This is the property that will be compared against the key property. In a simple join this will be a property belonging to the integration entity being mapped | `QualifiedProperty`
