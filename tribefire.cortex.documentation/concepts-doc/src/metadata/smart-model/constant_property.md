# Constant Property Assignment

Metadata Property Name  | Type Signature  
------- | -----------
`ConstantPropertyAssignment` | `com.braintribe.model.accessdeployment.smart.meta.ConstantPropertyAssignment`

## General

The metadata contains the following properties:

* `prop1`
* `prop2`

Property | Description | Type
------| --------- | -------
`prop1` | Description | `GmProperty`
`prop2` | Description | `QualifiedProperty`

## Example

Currently, several upper and lower boundary configurations are supported:

Lower Boundary | Upper Boundary
------| ---------
month | null, year
day | null, year, month
year | null, year
second | null, year
null, milisecond | null, year

> If no valid combination is found, the default pattern is used - MM/dd/yyyy HH:mm for the English language.