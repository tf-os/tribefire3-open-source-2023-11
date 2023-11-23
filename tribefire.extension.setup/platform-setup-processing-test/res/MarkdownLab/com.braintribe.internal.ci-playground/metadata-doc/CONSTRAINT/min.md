# Min

These metadata properties allow you to place a constraint on a number.

Metadata Property Name  | Type Signature  
------- | -----------
`Min` | `com.braintribe.model.meta.data.constraint.Min`
`Max` | `com.braintribe.model.meta.data.constraint.Max`

## General

You can set the maximal and minimal boundaries for a number. This means that if you try to commit a number in a property that is outside this boundary, you receive a validation error.

## Example

When you set the Max metadata to the value `10`, you are not able to enter and commit a value higher this number.
