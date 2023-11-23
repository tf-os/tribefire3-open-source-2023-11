# Metadata
>Metadata are an optional way of adding extra, context-sensitive information to a model.

## About Metadata
Code-wise, metadata are simply entity types which derive from `Metadata`. You can configure metadata instances for model elements of the following types:
* Model
* Entity type
* Property
* Enum type
* Enum constant

Metadata enriches a model, entity type, property or an enumeration. The metadata functionality is handled by the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metamodel}}">metamodel</a>, and is split into different packages according to the type of function it is responsible for.

For example, functionality related to control and display of model components are handled by either `Display` or `Prompt`, while `Constraints` control metadata that affect model components, such as `Mandatory` and `Optional`.

The base entity that handles metadata for the metamodel is the abstract type `Metadata`. It is also the base class for all implementations of metadata, including the main component abstract types:
* ModelMetadata
* EntityTypeMetadata
* PropertyMetadata
* EnumTypeMetadata
* EnumConstantMetadata

In addition, there is another type called `UniversalMetadata` which can be configured for any model element type. For example, `Deletable` is a metadata which makes sense for all the element types so, if needed, `Deletable` would probably be an instance of `UniversalMetadata` and you could enrich a property and a model with this metadata on top of enriching them with `PropertyMetadata` and `ModelMetadata`, respectively.


## Multiple Element Metadata
tribefire gives you an easy way of configuring common metadata for multiple elements of a given entity. Rather than creating each property individually, you can add the desired metadata to the `GmEntityType.propertyMetaData` collection.

You can assign multiple element metadata using the following collections:

<div class="datatable-begin"></div>

Collection  | Use For
------- | -----------
`GmMetaModel.enumTypeMetaData`  | All model enum types.
`GmMetaModel.enumConstantMetaData`  | All model enum constants.
`GmEnumTypeInfo.enumConstantMetaData` | All enum type enum constants.
`GmEntityTypeInfo.propertyMetaData`	 | All GM enum type properties

<div class="datatable-end"></div>

{% include note.html content="To configure the metadata for all entity types and all properties in a model, you must configure the metadata for `GenericEntity` and its properties in a model." %}

## Available Metadata
{% include tip.html content="For more information, see [Metadata](general_metadata_properties.html)" %}
