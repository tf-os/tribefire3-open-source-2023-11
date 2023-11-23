# Legacy REST Metadata Resolving

The metadata resolving calls allow you to query different components of a model regarding specific metadata. The call returns only the information regarding instances of the metadata given, and thus you must provide the type signature for the metadata you wish to query.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Calls

Name    | Syntax | Methods | Parameters   
------- | -----------
[Model Metadata](rest_metadata_resolving.md#model-metadata) | `.../rest/model-md` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `metadata`, `useCase`, `exclusive`
[Entity Metadata](rest_metadata_resolving.md#entity-metadata) | `.../rest/entity-md` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `metadata`, `type`, `property`, `useCase`, `exclusive`
[Property Metadata](rest_metadata_resolving.md#property-metadata) | `...rest/property-md` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `metadata`, `type`, `useCase`, `exclusive`
[Enum Metadata](rest_metadata_resolving.md#enum-metadata) | `.../rest/enum-md` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `metadata`, `useCase`, `exclusive`
[Enum Constant Metadata](rest_metadata_resolving.md#enum-constant-metadata) | `.../rest/enum-constant-md` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `metadata`, `type`, `constant`  `useCase`, `exclusive`


### Model Metadata 

The model metadata call is used to query instances of a specific metadata defined at the model level.


#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/model-md?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the model metadata can be queried. | Yes
`metadata`  | The type signature for the metadata you wish to resolve. | Yes
`useCase`  | Metadata can be restricted to resolve only when specific parameters are met. The use of the `useCase` selector is one. Here you can return all metadata (restricted to the type provided by the type signature) that should be resolved according to a use case by giving it as a value of this `useCase` parameter. | No
`exclusive` | Defines what metadata objects should be returned. If there are more than one metadata belonging to the model then, using the Conflict Priority, a decision is made. The default value for exclusive is `true`. This means only the winning metadata is displayed. However, if you use `false` as a parameter in the REST call, all metadata belonging to the model is displayed. | No

#### Projections

Name    | Description
------- | -----------
`simplify`  | In this case the simplify projection returns the same information as `payload`.
`payload`  | The metadata instances for this particular metadata. This is the default projection.

#### Example

The model metadata allows you to resolve metadata belonging to the model, that is metadata that has some affect on the model level (as opposed to entity, property or enum levels).

In addition to the three required parameters, there are also two optional ones which allow you to resolve metadata for a particular use case (`useCase`) or to return an instance based on its conflict priority property (`exclusive`).

##### Simple Model Metadata Resolving

This uses only three parameters and returns only the 'winning' metadata, based on a comparison of each instance's **Conflict Priority** property.
Call:

```
http://localhost:8080/tribefire-services/rest/model-md?sessionId=yourSessionId&accessId=SalesModel&metaData=com.braintribe.model.meta.data.display.ModelDisplayInfo
```

Response:

```json
{
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.ModelDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 1.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3868
  },
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : null,
  "origin" : null,
  "selector" : null
}
```

> Complex properties are returned as null values, unless you use the `depth=reachable` parameter.

##### Exclusive

By default the parameter `exclusive` is `true`, meaning that only one instance of a metadata is returned. However, you can use `exclusive=false` as part of your REST call to return all instances of the metadata requested.

Call:
```
http://localhost:8080/tribefire-services/rest/model-md?sessionId=yourSessionId&accessId=SalesModel&metaData=com.braintribe.model.meta.data.display.ModelDisplayInfo&exclusive=false
```

Response:

```json
[ {
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.ModelDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 1.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3870
  },
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : null,
  "origin" : null,
  "selector" : null
}, {
  "_id" : "1",
  "_type" : "com.braintribe.model.meta.data.display.ModelDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 0.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3868
  },
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : null,
  "origin" : null,
  "selector" : null
} ]
```

> Without using `depth=reachable` as part of the REST call, complex properties are displayed as null.

### Entity Metadata

The entity metadata call is used to query instances of a specific metadata defined at the entity level.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/entity-md?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the entity metadata can be queried. | Yes
`metadata`  | The type signature for the metadata you wish to resolve. | Yes
`type` | The entity type which contains the metadata you wish to resolve. | Yes
`useCase`  | Metadata can be restricted to resolve only when specific parameters are met. The use of the `useCase` selector is one. Here you can return all metadata (restricted to the type provided by the type signature) that should be resolved according to a use case by giving it as a value of this `useCase` parameter. | No
`exclusive` | Defines what metadata objects should be returned. If there are more than one metadata belonging to the model then, using the Conflict Priority, a decision is made. The default value for exclusive is `true`. This means only the winning metadata is displayed. However, if you use `false` as a parameter in the REST call, all metadata belonging to the model is displayed. | No

#### Projections

Name    | Description
------- | -----------
`simplify`  | In this case the simplify projection returns the same information as `payload`.
`payload`  | The metadata instances for this particular metadata. This is the default projection.

#### Example

The entity metadata resolving REST call resolves metadata at an entity level, that is, all metadata assigned at the entity level (as opposed to a model, property or enum level).

##### Simple Metadata Resolving

Using only the required parameter returns only one instance of a particular metadata, with the decision being based on the 'winner' of the **Conflict Priority**.

Call:
```
http://localhost:8080/tribefire-services/rest/entity-md?sessionId=yourSessionId&accessId=SalesModel&type=com.braintribe.model.sales.Person&metaData=com.braintribe.model.meta.data.display.EntityTypeDisplayInfo&depth=reachable
```

This returns the 'winning' instance of `EntityTypeDisplayInfo`, for the entity `Person`. Because the parameter `depth=reachable` has been used, this displays extended information, that is, underlying complex property information.
Response:

```json
{
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.EntityTypeDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 1.0
  },
  "description" : {
    "_id" : "1",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2572
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : "This is a second example of a metadata instance"
      } ]
    }
  },
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3872
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : {
    "_id" : "2",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2571
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : "A Person"
      } ]
    }
  },
  "?origin" : {
    "_id" : "3",
    "_type" : "com.braintribe.model.generic.pr.AbsenceInformation",
    "size" : null
  },
  "selector" : null,
  "_partial" : "true"
}
```

##### Exclusive

As noted above, only the 'winning' instance of a particular metadata is displayed by default. This is because the optional parameter exclusive defaults to `true`. However, if you use this parameter with the value `false`, all instances the requested metadata are returned.
Call:

```
http://localhost:8080/tribefire-services/rest/entity-md?sessionId=yourSessionId&accessId=SalesModel&type=com.braintribe.model.sales.Person&metaData=com.braintribe.model.meta.data.display.EntityTypeDisplayInfo&exclusive=false
```

This returns all instances of the `EntityTypeDisplayInfo` metadata for the entity `Person`. Because the parameter `depth` is not used, it defaults to the value `shallow` and displays only simple properties. Any complex property values are displayed as null.
Response:

```json
[ {
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.EntityTypeDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 1.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3872
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : null,
  "origin" : null,
  "selector" : null
}, {
  "_id" : "1",
  "_type" : "com.braintribe.model.meta.data.display.EntityTypeDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 0.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3825
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "disabled"
  },
  "name" : null,
  "origin" : null,
  "selector" : null
} ]
```

### Property Metadata

The property metadata call is used to query instances of a specific metadata defined at the property level.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/property-md?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the property metadata can be queried. | Yes
`metadata`  | The type signature for the metadata you wish to resolve. | Yes
`type` | The entity type which contains the metadata you wish to resolve. | Yes
`property` | The name of the property that contains the metadata you wish to query. | Yes
`useCase`  | Metadata can be restricted to resolve only when specific parameters are met. The use of the `useCase` selector is one. Here you can return all metadata (restricted to the type provided by the type signature) that should be resolved according to a use case by giving it as a value of this `useCase` parameter. | No
`exclusive` | Defines what metadata objects should be returned. If there are more than one metadata belonging to the model then, using the Conflict Priority, a decision is made. The default value for exclusive is `true`. This means only the winning metadata is displayed. However, if you use `false` as a parameter in the REST call, all metadata belonging to the model is displayed. | No

#### Projections

Name    | Description
------- | -----------
`simplify`  | In this case the simplify projection returns the same information as `payload`.
`payload`  | The metadata instances for this particular metadata. This is the default projection.

#### Example

The property metadata resolving REST call is used to resolve metadata assigned a specific property. This is done through the use of five required parameters: sessionId, accessId, type, property and metaData. The type property determines the entity type, through the use of its type signature, and property the specific property belonging to the entity where the metadata should be found. metaData then specifies that particular metadata that is required. This is done using the specific type signature of the metadata, and can be found in the individual property metadata pages.

##### Simple Metadata Resolving

Without the use of `exclusive=false` only one instance of a particular metadata is returned.
Call:

```
http://localhost:8080/tribefire-services/rest/property-md?sessionId=yourSessionId&accessId=SalesModel&type=com.braintribe.model.sales.Department&property=colorCode&metaData=com.braintribe.model.meta.data.display.PropertyDisplayInfo
```

The example above displays the 'winning' metadata instance for `PropertyDisplayInfo`. Also, because the `depth` parameter has not been used, only simple properties are displayed; complex properties display the value null. This returns the following:
Response:

```json
{
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.PropertyDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 1.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3627
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : null,
  "origin" : null,
  "selector" : null
}
```

##### Exclusive

As noted above, only the 'winning' instance of a particular metadata is displayed by default.
Call:

```
http://localhost:8080/tribefire-services/rest/property-md?sessionId=yourSessionId&accessId=SalesModel&type=com.braintribe.model.sales.Department&property=colorCode&metaData=com.braintribe.model.meta.data.display.PropertyDisplayInfo&exclusive=false&depth=reachable
```

This returns all instances of `PropertyDisplayInfo`, because `exclusive=false`, and also displays information for complex properties because `depth=reachable`.
Response:

```json
[ {
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.PropertyDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 1.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3627
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : {
    "_id" : "1",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2410
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : " Color  Code"
      } ]
    }
  },
  "?origin" : {
    "_id" : "2",
    "_type" : "com.braintribe.model.generic.pr.AbsenceInformation",
    "size" : null
  },
  "selector" : null,
  "_partial" : "true"
}, {
  "_id" : "3",
  "_type" : "com.braintribe.model.meta.data.display.PropertyDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 0.0
  },
  "description" : {
    "_id" : "4",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2573
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : "A second instance of this metadata"
      } ]
    }
  },
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3873
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : {
    "_id" : "5",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2574
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : "ClrCode"
      } ]
    }
  },
  "?origin" : {
    "_id" : "6",
    "_type" : "com.braintribe.model.generic.pr.AbsenceInformation",
    "size" : null
  },
  "selector" : null,
  "_partial" : "true"
} ]
```

### Enum Metadata

The enum metadata call is used to query instances of a specific metadata defined at the enum level.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/enum-md?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the enum metadata can be queried. | Yes
`metadata`  | The type signature for the metadata you wish to resolve. | Yes
`type` | The type signature of the enum you wish to query. | Yes
`useCase`  | Metadata can be restricted to resolve only when specific parameters are met. The use of the `useCase` selector is one. Here you can return all metadata (restricted to the type provided by the type signature) that should be resolved according to a use case by giving it as a value of this `useCase` parameter. | No
`exclusive` | Defines what metadata objects should be returned. If there are more than one metadata belonging to the model then, using the Conflict Priority, a decision is made. The default value for exclusive is `true`. This means only the winning metadata is displayed. However, if you use `false` as a parameter in the REST call, all metadata belonging to the model is displayed. | No

#### Projections

Name    | Description
------- | -----------
`simplify`  | In this case the simplify projection returns the same information as `payload`.
`payload`  | The metadata instances for this particular metadata. This is the default projection.

#### Example

The enum metadata resolving REST call is used to resolve metadata assigned to an enum.

##### Simple Metadata Resolving

By default, when using only the required parameters, only one instance of a metadata is returned.
Call:

```
http://localhost:8080/tribefire-services/rest/enum-md?sessionId=yourSessionId&accessId=SalesModel&type=com.braintribe.model.sales.KeyfactType&metaData=com.braintribe.model.meta.data.display.EnumTypeDisplayInfo&depth=reachable
```

This returns only the 'winning' instance of `EnumTypeDisplayInfo`, because `exclusive` is not used. Also, because `depth=reachable` is used, complex property information is displayed.
Response:

```json
{
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.EnumTypeDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 0.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3874
  },
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : {
    "_id" : "1",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2575
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : "Keyfact Enum"
      } ]
    }
  },
  "?origin" : {
    "_id" : "2",
    "_type" : "com.braintribe.model.generic.pr.AbsenceInformation",
    "size" : null
  },
  "selector" : null,
  "_partial" : "true"
}
```

##### Exclusive

As noted above, only the 'winning' instance of a particular metadata is displayed by default. To display all instances of a particular metadata, you must use `exclusive=false`.
Call:

```
http://localhost:8080/tribefire-services/rest/enum-md?sessionId=yourSessionId&accessId=SalesModel&type=com.braintribe.model.sales.KeyfactType&metaData=com.braintribe.model.meta.data.display.EnumTypeDisplayInfo&exclusive=false
```

This returns all instances of `PropertyDisplayInfo`, because `exclusive=false`, and also displays information for complex properties because `depth=reachable`.
Response:

```json
[ {
  "_id" : "0",
  "_type" : "com.braintribe.model.meta.data.display.PropertyDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 1.0
  },
  "description" : null,
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3627
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : {
    "_id" : "1",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2410
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : " Color  Code"
      } ]
    }
  },
  "?origin" : {
    "_id" : "2",
    "_type" : "com.braintribe.model.generic.pr.AbsenceInformation",
    "size" : null
  },
  "selector" : null,
  "_partial" : "true"
}, {
  "_id" : "3",
  "_type" : "com.braintribe.model.meta.data.display.PropertyDisplayInfo",
  "conflictPriority" : {
    "_type" : "double",
    "value" : 0.0
  },
  "description" : {
    "_id" : "4",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2573
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : "A second instance of this metadata"
      } ]
    }
  },
  "fontColor" : null,
  "id" : {
    "_type" : "long",
    "value" : 3873
  },
  "important" : false,
  "inheritance" : {
    "_type" : "com.braintribe.model.meta.data.MetaDataInheritance",
    "value" : "enabled"
  },
  "name" : {
    "_id" : "5",
    "_type" : "com.braintribe.model.generic.i18n.LocalizedString",
    "id" : {
      "_type" : "long",
      "value" : 2574
    },
    "localizedValues" : {
      "_type" : "map",
      "value" : [ {
        "key" : "default",
        "value" : "ClrCode"
      } ]
    }
  },
  "?origin" : {
    "_id" : "6",
    "_type" : "com.braintribe.model.generic.pr.AbsenceInformation",
    "size" : null
  },
  "selector" : null,
  "_partial" : "true"
} ]
```

### Enum Constant Metadata

This REST call allows you to resolve metadata placed on constants within an enum.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/enum-constant-md?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the enum metadata can be queried. | Yes
`metadata`  | The type signature for the metadata you wish to resolve. | Yes
`type` | 	The type signature of the enum you wish to query. | Yes
`constant` | The constant of the enum whose metadata you wish to resolve. | Yes
`useCase`  | Metadata can be restricted to resolve only when specific parameters are met. The use of the `useCase` selector is one. Here you can return all metadata (restricted to the type provided by the type signature) that should be resolved according to a use case by giving it as a value of this `useCase` parameter. | No
`exclusive` | Defines what metadata objects should be returned. If there are more than one metadata belonging to the model then, using the Conflict Priority, a decision is made. The default value for exclusive is `true`. This means only the winning metadata is displayed. However, if you use `false` as a parameter in the REST call, all metadata belonging to the model is displayed. | No

#### Projections

Name    | Description
------- | -----------
`simplify`  | In this case the simplify projection returns the same information as `payload`.
`payload`  | The metadata instances for this particular metadata. This is the default projection.

##### Example

See the example of [Enum Metadata](rest_metadata_resolving.md#enum-metadata).
