# Link Property Assignment

This metadata is used to map a complex property, either a single or multiple aggregation, using an additional linking entity.

Metadata Property Name  | Type Signature  
------- | -----------
`LinkPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.LinkPropertyAssignment`

## General

This entity links the integration entity with the complex property entity type using two joins:

* from the integration entity to the linking entity
* from the linking entity to the complex property

This metadata contains five properties; four handle the two separate joins required, a key and other property for each, while the fifth defines the access through which the linking entity can be accessed.

Property | Description | Type
------| --------- | -------
Link Access | The access through which the linking entity can be accessed.  | `IncrementalAccess`
Other Key | The property belonging to the complex property type that is joined to a corresponding property in the linking entity, as defined by Link Other Key | `GmProperty`
Key | The property belonging to the integration entity that is joined to a corresponding property in the linking entity, as defined by Link Key | `GmProperty`
Link Other Key | The property belonging to the linking entity that is joined to a corresponding property in the complex property type, as defined by Other Key | `GmProperty`
Link Key | The property belonging to the linking entity that is joined to a corresponding property in the integration entity, as defined by Key | `GmProperty`
