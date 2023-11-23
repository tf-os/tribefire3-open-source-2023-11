# Ordered Link Property Assignment

This metadata is used to map a complex property, either a single or multiple aggregation, using an additional linking entity.

Metadata Property Name  | Type Signature  
------- | -----------
`OrderedLinkPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.OrderedLinkPropertyAssignment`

## General

This metadata functions in exactly the same manner as Link Property Assignment, except that you can map lists using this metadata.

> For more information about Link Property Assignment, see [Link Property Assignment](link_property.md).

Inheriting the same five properties from Link Property Assignment, this metadata adds an additional property, called Link Index.

Property | Description | Type
------| --------- | -------
Link Index | Controls the ordering of properties assigned to list. The values for this property must be of the type integer, begin at 0 and be sequential | `Int`
Link Access | The access through which the linking entity can be accessed.  | `IncrementalAccess`
Other Key | The property belonging to the complex property type that is joined to a corresponding property in the linking entity, as defined by Link Other Key | `GmProperty`
Key | The property belonging to the integration entity that is joined to a corresponding property in the linking entity, as defined by Link Key | `GmProperty`
Link Other Key | The property belonging to the linking entity that is joined to a corresponding property in the complex property type, as defined by Other Key | `GmProperty`
Link Key | The property belonging to the linking entity that is joined to a corresponding property in the integration entity, as defined by Key | `GmProperty`
