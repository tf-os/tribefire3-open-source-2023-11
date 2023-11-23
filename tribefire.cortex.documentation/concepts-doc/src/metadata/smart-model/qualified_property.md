# Qualified Property Assignment

This metadata defines qualified mapping between integration- and smart-level properties.

Metadata Property Name  | Type Signature  
------- | -----------
`QualifiedPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.QualifiedPropertyAssignment`

## General

Using this metadata, the property belonging to an integration entity is mapped exactly to the smart entity. Data described by the integration property is displayed as is in the smart property.

This metadata contains two mapping-specific properties:

* conversion
* property

Property | Description | Type
------| --------- | -------
`conversion` | Property which converts a primitive type (Boolean, Date, decimal, etc.) to a String | `SmartConversion`
`property` | Integration-level property that should be mapped to the smart-level property that this metadata is assigned to	 | `GmProperty`
