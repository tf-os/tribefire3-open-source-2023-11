# Embedded and Embeddable

This metadata allows you to create embedded properties.

Metadata Property Name  | Type Signature  
------- | -----------
`JpaEmbeddable` | `com.braintribe.model.accessdeployment.jpa.meta.JpaEmbeddable`
`JpaEmbedded`   | `com.braintribe.model.accessdeployment.jpa.meta.JpaEmbedded`

## General

An embedded property is a property where its type in the model is an entity, but in the database both the owner and its embedded entity are stored in a single table. Only read operations are supported on embedded properties.

## Configuration

To configure, the embedded entity type has to be marked as embeddable with the `JpaEmbeddable` metadata. The owner entity's property must then be annotated with a `JpaEmbedded` metadata, with a map where keys are property names of the embedded entity type and values are any property mappings.
Currently, only `PropertyMapping` is supported there, with `columnName` and `type` properties being considered mandatory.
