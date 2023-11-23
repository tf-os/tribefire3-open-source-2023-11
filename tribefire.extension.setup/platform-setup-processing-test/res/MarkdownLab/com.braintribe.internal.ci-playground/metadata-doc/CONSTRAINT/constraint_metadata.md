# Constraint Metadata

The package `Constraint` gathers together all metadata which affect model components: either a entity type or property.

## General

Using any of the constraint metadata means that the standard behavior for either a property or entity is changed in some way. One such example is the String Regex Constraint, which only allows strings matching a specific regular expression to be committed.
> You can only apply a constraint to an entity or a property.

The main type is Constraint, which defines all the main properties for all constraint metadata, and is used to define two subtypes:

* Entity Constraint
* Property Constraint

In addition to this, the Entity Constraint and Property Constraint extend their respective metadata types (`EntityTypeMetadata` and `PropertyMetadata`), so that all individual metadata belonging to Constraint have all the required inherited, as well as their own specific, properties.

## Available Metadata

Name    | Description  
------- | -----------
Cascading Delete | Allows to you delete actual entity instance in a chain. For example, if you have an entity which contains a collection property, and this metadata is set to `true`, deleting said entity also deletes the entities contained in the collection.
Deployable Component | Defines a base type which you can associate an concrete expert interface for.
[Bidirectional](bidirectional.md) |  This metadata creates a relationship between two properties, so that when one is defined with a new value, the second property is automatically updated.
[Date Clipping](date_clipping.md) | You can use this metadata to influence how a tribefire's general date and time is displayed.
[Deletable and NonDeletable](deletable.md) | Can be used to enable/disable the deletion of an entity.
[Instantiable and NonInstantiable](instantiable.md) | Determines whether or not you can create a new entity instance when assigning a value to a single aggregation property.
[Mandatory and Optional](mandatory.md) | Defines whether a property must have a value before saving or not.
[Min and Max](min.md) | Sets a boundary on a decimal value. This can metadata can provide either a max or min value for this number.
[MinLenght and MaxLength](minlenght.md) | Determines the min and max length of a property.
[Modifiable and Unmodifiable](modifiable.md) | These metadata allow you to configure whether an element can be edited after instantiation.
[Pattern](pattern.md) | Allows you to define a regular expression that must be met before the instance can be committed.
[Referenceable and NonReferenceable](referenceable.md) | This defines whether you can define this property with an existing entity instance.
[Unique and NonUnique](unique.md) | Defines whether the value of this property should be unique against all other instances of the same property in a model.
