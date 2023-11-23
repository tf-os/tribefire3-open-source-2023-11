# Qualified Entity Assignment

This metadata is used to determine smart mapping at the entity level.

Metadata Property Name  | Type Signature  
------- | -----------
`QualifiedEntityAssignment` | `com.braintribe.model.accessdeployment.smart.meta.QualifiedEntityAssignment`

## General

This metadata contains two mapping-specific properties:

* access
* entity type

Property | Description | Type
------| --------- | -------
Access | The access through which the integration entity can be accessed | `IncrementalAccess`
Entity | The integration entity which is mapped to the smart entity this metadata is assigned to | `EntityType`
