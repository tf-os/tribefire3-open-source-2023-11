# Min

These metadata properties allow you to place a constraint on a number or a date.

Metadata Property Name  | Type Signature  
------- | -----------
`Min` | `com.braintribe.model.meta.data.constraint.Min`
`Max` | `com.braintribe.model.meta.data.constraint.Max`

## General

You can set the maximal and minimal boundaries for a number or a date. This means that if you try to commit a number or a date in a property that is outside this boundary, you receive a validation error.

## Example

When you set the Max metadata to the value `10`, you are not able to enter and commit a value higher this number. You set this metadata using an annotation on the getter of the respective property:

```java
@Max("10")
```
