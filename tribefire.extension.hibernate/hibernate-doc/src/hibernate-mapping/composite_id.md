# Composite ID

This metadata allows you to create a composite key.

Metadata Property Name  | Type Signature  
------- | -----------
`JpaCompositeId` | `com.braintribe.model.accessdeployment.jpa.meta.JpaCompositeId`

## General

You can only attach this metadata to an ID property. Only a composite ID consisting of up to 30 columns is supported.

## Configuration

To configure, simply add the `JpaCompositeId` metadata to the ID property of your entity. Read, update, and delete operations in the database are fully supported.

Creating new instances in a database is supported only if the ID is given explicitly, so when a properly formatted String value is set to the `id` property by the user. The proper format is a comma-separated concatenation of the individual values. The format for different types is a `GM string`.
> For more information, see the `instanceToGmString()` method of the `ScalarType` class.