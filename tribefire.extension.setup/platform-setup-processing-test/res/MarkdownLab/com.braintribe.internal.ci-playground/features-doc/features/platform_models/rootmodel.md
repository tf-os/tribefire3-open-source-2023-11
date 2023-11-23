# Root Model
>The root-model is the basis for every other model in tribefire.

## General
The root-model is the minimal dependency for all <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">models</a> in tribefire. 

It provides the type <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.generic_entity}}">`GenericEntity`</a>, which acts as a marker telling tribefire that an element is to be treated as an <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a>.

## Structure
Below you can find an overview of the root-model's structure, depicting its elements.

{% include image.html file="root-model/root-model_structure.jpeg" max-width=900 %}

## Elements
<div class="datatable-begin"></div>

Type | Abstract | Purpose | Properties | Overrides
-------- | -------- | -------- | -------- | --------  | -------- 
`GenericEntity` | true | Instance of `EntityType`.<br>Super type for all entity types in<br>tribefire. Provides the `id`-property<br>as `object`. | id: `object`<br><br>globalId: `string`<br><br>partition: `string` | 
`StandardIdentifiable`<br>extends:<br>`GenericEntity` | true | Provides the id-property as `long`. | | propertyOverrides:`id`<br><br>declaringTypeInfo:<br>`StandardIdentifiable`<br><br>metaData:`TypeSpecification`<br><br>type:<br>`long`
`StandardIntegerIdentifiable`<br>extends:<br>`GenericEntity` | true | Provides the id-property as `integer`. | | propertyOverrides:`id`<br><br>declaringTypeInfo:<br>`StandardIntegerIdentifiable`<br><br>metaData:`TypeSpecification`<br><br>type:<br>`integer`
`StandardStringIdentifiable`<br>extends:<br>`GenericEntity` | true | Provides the id-property as `string`. | | propertyOverrides:`id`<br><br>declaringTypeInfo:<br>`StandardIntegerIdentifiable`<br><br>metaData:`TypeSpecification`<br><br>type:<br>`string`
`object` | false | Instance of `GmBaseType`, represents the object type | |
`string` | false | Instance of `GmStringType`, represents the simple string type | |
`boolean` | false | Instance of `GmBooleanType`, represents the simple boolean type | |
`integer` | false | Instance of `GmIntegerType`, represents the simple integer type | |
`long` | false | Instance of `GmLongType`, represents the simple long type | |
`double` | false | Instance of `GmDoubleType`, represents the simple double type | |
`decimal` | false | Instance of `GmDecimalType`, represents the simple decimal type | |
`float` | false | Instance of `GmFloatType`, represents the simple float type | |
`date` | false | Instance of `GmDateType`, represents the simple date type | |

<div class="datatable-end"></div>

{% include tip.html content="For information about overrides, see [Metamodel](metamodel.html)."%}

## GenericEntity
`GenericEntity` is a special entity type in tribefire. It is on the top of the inheritance hierarchy of any entity type. That means, except itself, every other type has at least the `GenericEntity` as a supertype. 

This is also true for the `GmEntityType` although it is at the same time an instance of it. This is a consequence of the fact that the `meta-model` is able to describe itself and one of the major cortex paradigms. 

If you create a model via the graphical modeler the `GenericEntity` is added as the supertype for your entity types automatically. When you define your entity types via interfaces, then you have to add it (or a type deriving from it) as a supertype by yourself.

As described in the table it inherits its `id`-property to its sub-types. That means, whithout further configuration the id's type is `Object` and it's up to the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">access</a> to specify its actual type. In case you're required to restrict its type to `long`, `integer` or `string` you can do this either with the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> `TypeSpecification` or by using one of the three `StandardIdentifable`-types.

## StandardIdentifiables
The three standard identifable types `StandardIdentifiable`, `StandardIntegerIdentifiable` and `StandardStringIdentifiable` are convenience types, which allow you to restrict the entity type's `id`-property to `long`, `integer` or `string`.

## Simple Types
Simple <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.property}}">properties</a> with primitive types are defined via `Object` (the most common base type) or simple types like string, data, boolean, integer, etc. The root model instantiates the respective entity types to provided them already (e.g. selected in the graphical modeler).
