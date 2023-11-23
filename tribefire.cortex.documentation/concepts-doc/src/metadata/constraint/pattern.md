# Pattern

This metadata allows you to apply constraints on string properties using a regular expression.

Metadata Property Name  | Type Signature  
------- | -----------
`Pattern` | `com.braintribe.model.meta.data.constraint.Pattern`

## General

When you enter a regular expression in the expression field, any string value entered in the property is validated against it. If the result of this validation is false, that is, doesn't match the regular expression, a validation error is thrown.
