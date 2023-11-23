# MinLength

These metadata properties allow you to place a constraint on the minimum and maximum amounts of certain elements.

Metadata Property Name  | Type Signature  
------- | -----------
`MinLength` | `com.braintribe.model.meta.data.constraint.MinLength`
`MaxLength` | `com.braintribe.model.meta.data.constraint.MaxLength`

## General

There are two definable variables which you can use to limit your collection: MinLength and MaxLength. Min Length allows you to define the minimum amount of elements, whereas the Max Length configures the maximum amount allowed in this properties collections. Both variables are configured using an integer.

You can configure the maximum and minimum number of:

* items in a collection entity
* characters in a string

## Example

After adding either of these metadata to a property, you can define the minimum and maximum amount of elements in a collection.

Even though you can also use this metadata with strings, this example is based on a collection. In this example, the minimum amount of elements is `1` and the maximum amount allowed is `3`. As a result, if you attempt to commit a property with less than `1` or more than `3` items, you receive a validation error.
