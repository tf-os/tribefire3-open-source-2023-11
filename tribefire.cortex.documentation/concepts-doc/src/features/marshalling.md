# Tribefire Marshallers

This document explains data marshallers provided by Tribefire, along with their API. Marshallers are at the very core of Tribefire, utilizing our type reflection to serialize Tribefire model data into the following formats, while preserving data types and references:

* Binary (Tribefire native/proprietary format)
* JSON 
* YAML
* XML with StAX ([Streaming API for XML](https://en.wikipedia.org/wiki/StAX)) parsing
* JSE (Tribefire native javascript - a code-based serialization format)
* MAN

> Marshallers are maintained in the [com.braintribe.gm](https://github.com/braintribehq/com.braintribe.gm) repository. Note that you will find more marshallers in the code, however only the ones listed here are guaranteed to be up-to-date and working.

## Available Marshallers

### Binary
You can use this marshaller to serialize `GenericModel` assemblies to a `binary` format. Note that you will find three different marshallers in the package - `Bin2Marshaller` is the one to use.

> This marshaller is the fastest, and binary is the most compact format, however keep in mind that it's not human-readable. It's the standard for [GmWebRpc](javadoc:com.braintribe.model.deployment.GmWebRpc) entities.

For more information on the binary marshaller, see its [Javadoc](javadoc:com.braintribe.codec.marshaller.bin.Bin2Marshaller).

### JSON
You will find the following JSON marshallers in the package:

* `JsonMarshaller` (deprecated) - the oldest one, using DOM while writing/reading JSON.
* `FastJsonMarshaller` (deprecated) - an improvement on the `JsonMarshaller`, using DOM only to read JSON, and writing directly (hence fast).
* `JsonStreamMarshaller` (currently in use) - uses Jackson parsing instead of DOM, resulting in multiple advantages over the older marshallers.

Note that JSON output, while commonly used with REST APIs, has the following disadvantages:

* No native reference support, resulting in problematic indentation, large file sizes, and uncomfortable editing. This problem can be partially solved using YAML, which supports referencing natively (but not forward referencing). To keep references in JSON, we're using either pseudo-properties (`"_id"`, `"_ref"`) or recurrence (which you can control with the `entityRecurrenceDepth` option).
* No native data type support - types must be inferred or provided by a pseudo-property (`"_type":`). See Type Explicitness below.

#### Type Explicitness
`GmSerializationOptions` allow you to control how data types should be handled while writing JSON, utilizing the `TypeExplicitness` option. The example below shows how to set it, in this case to `always`:

```java
marshaller.marshall(out, statusMappings, GmSerializationOptions.defaults.set(TypeExplicitnessOption.class, TypeExplicitness.always));
    	}
```

You can set the following values:

* Always - data type is explicitly stated for each object written to JSON as the `_type` property, as in:

   ```json
    "longValue": {
       "_type": "long",
       "value": "234231544363542352435"
   }
   ```

* Entities - data type is only explicitly stated on entity instances, and inferred elsewhere (with the exception of polymorphic objects).
* Polymorphic - data type is only explicitly stated on polymorphic objects (`objectValue` as an example), and inferred otherwise.

#### References
`JsonStreamMarshaller` is capable of decoding references and IDs added to objects as pseudo-properties (`"_ref"` and `"_id"`  respectively). In the example below, we assign an ID to `address`, and then refer to it from `address2`:

```json
{"_type": "com.braintribe.model.company.Person",
   "address": {"_type": "com.braintribe.model.company.Address", "_id": "1",
       "street": "Karlstraße",
       "city": "Karlsruhe",
       "info": {
           "description": "Hello World!"
       }
   },
   "address2": {"_ref": "1"},

```

Another possible strategy is to use recurrence. First, you need to set the entity recurrence depth (by default, it's set to zero, resulting in the generation of the above pseudo-properties):

```java
marshaller.marshall(out, statusMappings, GmSerializationOptions.defaults.set(EntityRecurrenceDepth.class, context.getEndpoint().getEntityRecurrenceDepth());
    	}
```

With entity recurrence depth set to one, the result would be similar to the one below:

```json
{"_type": "com.braintribe.model.company.Person",
   "name": "Dirk",
      "friends": {
         "name": "Grzegorz",
            "friends": {
               "name": "Dirk"
            }
      }
   }   
```

Note that the pseudo-properties are absent, however, there could be potential problems with a big nested structure.

For more information on `JsonStreamMarshaller`, see its [Javadoc](javadoc:com.braintribe.codec.marshaller.json.JsonStreamMarshaller).

### YAML
You can use this marshaller to convert model data to a YAML-formatted stream. YAML has some advantages over JSON and XML - it supports native references (but without forward references, unfortunately) and custom data types, while still being well-known and widely used in the community. Type declaration of the same `address` property as shown above for JSON would look as follows in YAML:

```yaml
!com.braintribe.model.company.Person
   address: &1 
       street: Karlstraße
       city: Karlsruhe
       info: 
           description: Hello World!
       
   address2: *1
```

Note that `TypeExplicitness` works with YAML as well as with JSON.

> No in-memory DOM is used by this marshaller (this is also true for JSON and XML).

For more information, see [Javadoc](javadoc:com.braintribe.codec.marshaller.yaml.YamlMarshaller).

### XML (StAX)
A `CharacterMarshaller` implementation used to marshall and unmarshall XML. Note that XML has the same problems as JSON, that is no native support for references.

For more information, see [Javadoc](javadoc:com.braintribe.codec.marshaller.stax.StaxMarshaller).

### MAN
Tribefire manipulation parser, capable of understanding the GMML grammar/format, including references. It's using manipulation parser grammar (GMML) to serialize objects in a compact way, similarly as in JSE. There is no unnecessary indentation and forward/backward references are supported, resulting in a simpler file structure (no waste of memory, no scrolling) compared to XML or JSON.

For more information, see [Javadoc](javadoc:com.braintribe.model.processing.manipulation.marshaller.ManMarshaller).

### JSE
This is a write-only marshaller that marshalls model data to a stream containing javascript statements that can be consumed by any javascript client (such as Tribefire explorer, `Node.js`, or any others). By running the generated javascript, you can construct Tribefire entities using fast, direct calls. The file itself is as compact and lightweight as possible, and can be easily split into smaller chunks:

```javascript
//JSE version=4.0
//BEGIN_TYPES
P.J=$.T("com.braintribe.model.meta.selector.UseCaseSelector");
P.K=$.T("com.braintribe.model.meta.selector.DisjunctionSelector");
//END_TYPES
P.L=$.P(P.a,'dependencies');P.M=$.P(P.a,'globalId');P.N=$.P(P.a,'id');P.O=$.P(P.a,'metaData');P.P=$.P(P.a,'name');P.Q=$.P(P.a,'partition');P.R=$.P(P.a,'types');
P.S=$.P(P.a,'version');P.T=$.P(P.a,'enumConstantMetaData');P.U=$.P(P.a,'enumTypeMetaData');P.V=$.P(P.a,'typeOverrides');P.W=$.P(P.b,'globalId');P.X=$.P(P.b,'id');
```

The only implementation required on the client side is adding the functions used in the `.js` output, which should be a matter of a few hours at most. Function meaning is explained below:

```javascript
// reflection functions
$.T -> function(typeSignature) // resolves an entity type by its typeSignature
$.P -> function(entityType, propertyName) // resolves a property from an entity type by property name
$.e -> function(typeSignature) // resolves an enum type by its typeSignature
$.E -> function(enumType, constantName) // resolves an enum constant from an enum type by constant name

// instantiation and assignment functions
$.C -> function(type) // creates an instance of type

$.A -> function(entity, property, absenceInformation) // absentify property of an entity with a specific absence information
$.a -> function(entity, property) // absentify property // absentify property of an entity with the standard absence information
$.s -> function(entity, property, value) // assign value to a property of an entity

// value functions
$.t -> function(longLiteral) // creates a date object from a longLiteral -> {"l": lowValue, "m": midValue, "h": highValue}
$.D -> function(str) // creates a decimal value from a string
$.d -> function(number) // creates a double value from a number
$.f -> function(number) // creates a float value from a number
$.i -> function(number) // creates an integer value from a number
$.y -> true
$.n -> false
$.L -> function(array) // creates a list from an array of values
$.S -> function(array) // creates a set from an array of values
$.M -> function(array) // creates a map from an array of values where keys and values are follow each other
$.m -> function(object) // creates a map from an object who's properties are the string named entries of the map
$.m -> function(object) // creates a map from an object who's properties are the string named entries of the map


var $ = {
   T: function(typeSignature) { return typeReflection.lookupType(typeSignature); },
   P: ...,
};

var P = {};
```

For more information, see [Javadoc](javadoc:com.braintribe.codec.marshaller.jse.JseMarshaller).

## Marshaller API
You can explore this API to understand the foundations behind Tribefire marshallers. For an overview of actual implementations, see the javadoc links for each component. Key API components are listed below.

### ConfigurableGmSerializationOptions
This class implements a number of methods useful for feeding configuration options to the marshaller. Let's take a look at the outcome of using these methods in the marshaller:

```java
	private OutputPrettiness outputPrettiness = OutputPrettiness.none;
	private boolean useDirectPropertyAccess;
	private boolean writeEmptyProperties;
	private boolean stabilizeOrder;
	private boolean writeAbsenceInformation = true;
	private Map<Class<?>, Object> attributes;
	private GenericModelType inferredRootType = BaseType.INSTANCE;

   // Normally, empty properties are skipped to save space in the output. The options enforces them to be written in the output.
   public void setWriteEmptyProperties(boolean writeEmptyProperties) {
		this.writeEmptyProperties = writeEmptyProperties;
	}
	
   //Use this option carefully. This option improves marshalling performance but skips PropertyAccessInterceptors (PAI). If a marshaller supports it, and your assembly is statically available (no AbsenceInformations and other ValueDescriptors that would have to be resolve with PAIs) it can improve. 
	public void setUseDirectPropertyAccess(boolean useDirectPropertyAccess) {
		this.useDirectPropertyAccess = useDirectPropertyAccess;
	}
	
   // This option specifies the human readability of the output (none, low, mid, high). Note that its efficiency depends on the output type.
	public void setOutputPrettiness(OutputPrettiness outputPrettiness) {
		this.outputPrettiness = outputPrettiness;
	}
	
   // Applies order to the sequence of entities, properties, collection elements (normally these elements are not ordered). In consequence, minor changes in an assembly should be consistently reflected by minor changes in the serialization. Supported by StAX and MAN marshallers.
	public void setStabilizeOrder(boolean stabilizeOrder) {
		this.stabilizeOrder = stabilizeOrder;
	}
	
   // True by default. It can be set to false to skip properties with AbsenceInformation in the output.
	public void setWriteAbsenceInformation(boolean writeAbsenceInformation) {
		this.writeAbsenceInformation = writeAbsenceInformation;
	}
	
   // Inferred root type can be used to skip output of the type.
	public void setInferredRootType(GenericModelType inferredRootType) {
		this.inferredRootType = inferredRootType;
	}

   // Sets type-safe custom options.
   <T, O extends MarshallerOption<T>> void set(Class<O> option, T value)

```

### GmSerializationOptions
These options are used when implementing the marshaller. They work exactly as described in [ConfigurableConfigurableGmSerializationOptions](#ConfigurableGmSerializationOptions).

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.GmSerializationOptions).

### GmDeserializationOptions
Complimentary to `GmSerializationOptions`, this interface defining how deserialization is performed.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.GmDeserializationOptions).

### CharsetOption
Used when implementing [Marshaller](#Marshaller) to determine the charset to be used when encoding/decoding characters of an OutputStream/InputStream.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.CharsetOption)

### DateFormatOption
Used when implementing marshallers to determine how dates are formatted, in case it's not fixed by the marshaller.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.DateFormatOption)

### EntityFactory
This is an important option that should be implemented by every marshaller. It allows to outsource the creation of a required instance to an external expert (when this option is not implemented, the default creation method is `EntityType.createRaw()`). For example, you could use an EntityFactory to create with `EntityType.create()`, to allow the use of default values (`@Intializer`) when unmarshalling, which is especially usefull when reading config files.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.EntityFactory)

### EntityRecurrenceDepth
Controls how many levels of recurrence are considered when marshalling recursive data.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.EntityRecurrenceDepth)

### EntityVisitorOption
In order to inform the outside about entities being marshalled/unmarshalled, an entity visitor can be supported by a marhsaller.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.EntityVisitorOption)

### IdentityManagementModeOption
The idea behind this option is to avoid creating duplicates when importing entities. Thus, whenever an ID is available in the database, it is used to avoid duplication. The following options are available:

```java
    /**
     * No identity management done at all. 
     */
    off, 
    
    /**
     * Depending on the parsed assembly the marshaler automatically detects the identification information and uses it for identity management.
     */
    auto, 

    /**
     * The internal generated _id information will be used if available. 
     */
    _id,
    

    /**
     * The id property will be used if available. 
     */
    id
```

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.IdentityManagementModeOption)



### IdTypeSupplier
This is a JSON-specific option. As the general type of the `GenericEntity.id` property is `Object`, it would lead to a type explicit object literal. To avoid this, you can use this option to set your own ID type.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.IdTypeSupplier)


### MimeTypeOption
MimeTypes can have additional information (e.g `text/xml`;`extra=true`)

A Marshaller can use the full information of the mimetype that led to its usage in order to set internal modes.

The same marshaller could be registered to different mime types (e.g `text/xml`, `gm/xml` -> StaxMarshaller) and further mimetypes could have additional attributes (e.g `text/xml`;`version=2.0`). The concrete mimetype a marshaller was resolved with, and its concrete attributions, could influence the way the marshaller works. The **MimeTypeOption** therefore allows to pass the specific MimeType to the marshaller.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.MimeTypeOption)

### StringifyNumbersOption
When this option is set to `true` and TypeExplicitness is set to either `entity` or `polymorphic`, then `long` and `decimal` types are represented as `string` in the output.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.StringifyNumbersOption)


### TypeExplicitnessOption
See [Type Explicitness](#type-explicitness)

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.TypeExplicitnessOption)


### TypeLookup
Optionally, a marshaller could interpret type signatures indirectly. To convert a signature to an actual type the TypeLookup options can be used.

For implementation details, see [Javadoc](javadoc:com.braintribe.codec.marshaller.api.TypeLookup)

