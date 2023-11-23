# Meta Model
>The meta-model is the central self-reflective model that allows to describe the system without using any specific programming or declaration language. All models are instances of the meta-model - even the meta-model itself.

## General
The meta-model is the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">model</a> that models models. It defines any model in tribefire, including itself and has dependencies to other models (defined by the meta-model again) to reuse their types (e.g. `GenericEntity`/`StandardIdentifiable` from the root-model). 

In this article, we will explore its structure and inspect its elements.

{%include tip.html content="For general information about models, see [Models](models.html). "%}

## Basic Structure
Below you can find an overview of the meta-model's basic structure, depicting its most relevant elements.

{% include image.html file="meta-model/01-mm_basic-structure.jpeg" max-width=850 %}

### Important Elements
<div class="datatable-begin"></div>

Type | Abstract | Purpose | Properties 
---- | ---- | ---- | ---- | ---- 
`GmMetaModel`<br>extends:<br>`GmModelElement` | false | Depicts models and the meta-model itself | name: `string`<br><br>version: `string`<br><br>types: `set<GmType>`
`GmModelElement`<br>extends:<br>`StandardIdentifiable` | true | Super type for meta-model elements |
`GmType`<br>extends:<br>`GmModelElement` | true | Super type for all types | typeSignature: `string`<br><br>declaringModel: `GmMetaModel`
`GmEntityType`<br>extends:<br>`GmCustomType` | false | Custom GM-types | superTypes: `set<GmEntityType>`<br><br>properties: `set<GmProperty>`<br><br>evaluatesTo: `GmType`<br><br>isAbstract: `boolean`
`GmProperty`<br>extends:<br>`GmPropertyInfo` | false | Custom GM-model element to depict details of an entity type | name: `string`<br><br>isAbstract: `boolean`<br><br>type: `GmType`<br>declaringType: `GmEntityType`
`GmMapType`<br>extends:<br>`GmCollectionType` | false | GM-collection type allowing to hold elements<br>depicted by key-type and value-type | keyType: `GmType`<br><br>valueType: `GmType`
`GmLinearCollectionType`<br>extends:<br>`GmCollectionType` | true | Super type for linear collection types | elementType: `GmType` |
`GmListType`<br>extends:<br>`GmLinearCollectionType` | false | Linear GM-collection type allowing to hold<br>a number of ordered elements depicted by elementType |
`GmSetType`<br>extends:<br>`GmLinearCollectionType` | false | Linear GM-collection type allowing to hold<br>a number of un-ordered elements depicted by elementType |
`GmBaseType`<br>extends:<br>`GmType` | false | `GmType` of properties with the type `object` |
`GmSimpleType`<br>extends:<br>`GmScalarType` | true | Super type for simple scalar types |
`GmBooleanType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `boolean` |
`GmDateType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `date` |
`GmDecimalType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `decimal` |
`GmFloatType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `float` |
`GmIntegerType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `integer` |
`GmLongType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `long` |
`GmStringType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `string` |
`GmDoubleType`<br>extends:<br>`GmSimpleType` | false | `GmType` of properties with the type `double` |

<div class="datatable-end"></div>

## GmProperty
Allows to instantiate properties, which depict the details of an <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity type</a>.

{% include image.html file="meta-model/02-mm_gmproperty.jpeg" max-width=600 %}

### Important Elements

Type | Abstract | Purpose | Properties
-------- | -------- | -------- | -------- | -------- 
`GmProperty`<br>extends:<br>`GmPropertyInfo` | false | Custom GM-model element<br>to depict details of an entity type | name: `string`<br><br>nullable: `boolean`<br><br>type: `GmType`<br/>declaringType: `GmEntityType`<br><br>typeRestriction: `GmTypeRestriction`
`GmTypeRestriction`<br>extends:<br>`GmModelElement` | false | Allows to restrict<br>which type a property can have | types: `list<GmType>`<br>Definition of allowed types<br><br>keyTypes: `list<GmType>`<br>Used for restriction on map-keys<br><br>allowVd: `boolean`<br>Allow the use of value descriptors, e.g. `now()` for date-values<br><br>keyAllowVd: `boolean`<br>Similar to `allowVd`, but for map-keys
`QualifiedProperty`<br>extends:<br>`StandardIdentifiable` | false | Used to reference a property<br>along with a given entity | property: `GmProperty`<br><br>entityType: `GmEntityType`
`PropertyPath`<br>extends:<br>`StandardIdentifiable` | false | Standard representation of property path, e.g. `Company.ceo.companyCar.brand` | properties: `list<GmProperty>`

{% include apidoc_url.html className="GmType" link="interfacecom_1_1braintribe_1_1model_1_1meta_1_1_gm_type.html"%}

## Info Types
Info-Types allow to enrich model elements to make them more specific to their nature.

{% include image.html file="meta-model/03-mm_infotypes.jpeg" max-width=900 %}

### Important Elements

Type | Abstract | Purpose | Properties
-------- | -------- | -------- | -------- | -------- 
`GmCustomModelElement`<br>extends:<br>`GmModelElement` | true | Super-type for all info-types |
`GmCustomTypeInfo`<br>extends:<br>`GmCustomModelElement`<br>`HasMetadata` | true | Provides the properties `metadata` and `declaringModel` to its sub-types. | declaringModel: `GmMetaModel`
`GmEntityTypeInfo`<br>extends:<br>`GmCustomTypeInfo` | true | Enriching of entity types. | See [Overrides](#overrides) and [Metadata](#metadata)
`GmEnumTypeInfo`<br>extends:<br>`GmCustomTypeInfo` | true | Enriching of enum types. | See [Overrides](#overrides) and [Metadata](#metadata)
`GmEnumConstantInfo`<br>extends:<br>`GmCustomModelElement`,<br>`HasMetadata` | true | Enriching of enum constants. | See [Overrides](#overrides) and [Metadata](#metadata)
`GmPropertyInfo`<br>extends:<br>`GmCustomModelElement`,<br>`HasMetadata` | true | Enriching of properties. | initializer: `object`<br>See [Overrides](#overrides) and [Metadata](#metadata)

## Overrides
Overrides allow to apply <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a> without changing the original entity. Without an override a metadata configuration on a type from model dependency will apply on that model globally. With an override this can be avoided - the configuration will only apply to your local model. 

In addition, metadata allows you to change the `declaringTypeInfo`, i.e. although a property might be inherited, its origin can be mimicked such, that the sub-type seems to be the declaring type. Further, overrides allow to specify initial values on inherited properties without changing them at the origin.

{% include tip.html content="See the [Metadata](#metadata) section for a use-case."%}

{% include image.html file="meta-model/04-mm_overrides.jpeg" max-width=800 %}

### Important Elements

Type | Abstract | Purpose | Properties
-------- | -------- | -------- | -------- | -------- 
`GmCustomTypeOverride`<br>extends:<br>`GmCustomTypeInfo` | false | Abstract super-type for<br>GmEntityOverride and GmEnumTypeOverride.<br>Referenced from `GmMetaModel` via `typeOverrides` |
`GmEntityTypeOverride`<br>extends:<br>`GmCustomTypeInfo` | false | Specific override for entity types | propertyOverrides: `list<GmPropertyOverride>`<br><br>entityType: `GmEntityType`
`GmEnumTypeOverride`<br>extends:<br>`GmCustomTypeInfo` | false | Specific override for enum types | constantOverrides: `list<GmEnumConstantOverride>`<br><br>enumType: `GmEnumType`
`GmEnumConstantOverride`<br>extends:<br>`GmEnumConstantInfo` | false | Overrides enum constants | declaringTypeOverride: `GmEnumOverride`<br><br>enumConstant: `GmEnumConstant`
`GmPropertyOverride`<br>extends:<br>`GmCustomModelElement` | false | Overrides properties | declaringTypeInfo: `GmEntityTypeInfo`<br><br>property: `GmProperty`

## Metadata
Metadata are used to configure models specifically to use cases. To enable this, the meta-model has a dependency to the basic meta-data-model.

{% include tip.html content="For more information, see [Metadata](metadata.html)."%}

{% include image.html file="meta-model/05-mm_metadata.jpeg" max-width=800 %}

### Important Elements

Type | Abstract | Purpose | Properties | Overrides
-------- | -------- | -------- | -------- | --------  | -------- 
`HasMetadata`<br>extends:<br>`GenericEntity` | true | Abstract type from meta-data-model.<br>Has property `metadata` to be inherited by<br>custom types, properties and enum constants | metadata: `Metadata`
`Metadata`<br>extends:<br>`StandardIdentifiable` | true | Abstract type from meta-data-model.<br>Provides the actual metadata configuration. | conflictPriority: `double`<br><br>important: `boolean`<br><br>inherited: `boolean`<br><br>selector: `MetaDataSelector`
`GmEntityTypeInfo`<br>extends:<br>`GmCustomTypeInfo` | true | Enriching of entity types.<br>Makes use of [overriding](#overrides). | propertyMetadata: `set<Metadata>` | propertyOverride:`TypeRestriction`<br><br>types:`PropertyMetaData`<br>`UniversalMetaData`
`GmEnumTypeInfo`<br>extends:<br>`GmCustomTypeInfo` | true | Enriching of entity types.<br>Makes use of [overriding](#overrides). | enumConstantMetadata: `set<Metadata>` | propertyOverride:`TypeRestriction`<br><br>types:<br>`EnumConstantMetaData`<br>`UniversalMetaData`<br><br>propertyOverride:<br>`metadata`<br><br>declaringTypeInfo:<br>`GmEntityTypeInfo`
`GmPropertyInfo`<br>extends:<br>`GmCustomModelElement`,<br>`HasMetadata` | true | Enriching of properties.<br>Makes use of [overriding](#overrides). | initializer: `object`<br> | propertyOverride:`metadata`<br><br>declaringTypeInfo:<br>`GmPropertyInfo`
`GmEnumConstantInfo`<br>extends:<br>`GmCustomModelElement`,<br>`HasMetadata` | true | Enriching of enum constants.<br>Makes use of [overriding](#overrides). | initializer: `object`<br> | propertyOverride:`metadata`<br><br>declaringTypeInfo:<br><br>`GmEnumConstantInfo`