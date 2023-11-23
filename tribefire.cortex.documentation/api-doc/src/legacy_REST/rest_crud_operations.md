# Legacy REST CRUD Operations

Action calls allow you to execute various actions.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

## Available Calls

Name    | Syntax | Methods | Parameters
------- | -----------
[Assembly Manipulate](rest_crud_operations.md#assembly-manipulate) | `.../rest/assembly-manipulate` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `body`
[Assembly-Stream Manipulate](rest_crud_operations.md#assembly-stream-manipulate) | `.../rest/assembly-stream-manipulate` | `POST, PUT, OPTIONS` | `sessionId`, `accessId`, `streamBody`
[Create](rest_crud_operations.md#create) | `.../rest/create` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `type`
[Delete](rest_crud_operations.md#delete) | `.../rest/delete` | `GET, POST, PUT, DELETE, OPTIONS` | `sessionId`, `accessId`, `type`, `id`
[Entity Identification Resolution](rest_crud_operations.md#entity-identification-resolution) | `../rest/entity` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `type`, `entityId`
[Entity Fetch](rest_crud_operations.md#entity-fetch) | `../rest/fetch` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `type`
[List Insert](rest_crud_operations.md#list-insert) | `.../list-insert/` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`, `id`, `property`, `value`
[List Remove](rest_crud_operations.md#list-remove) | `.../list-remove/` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`, `property`, `value`
[Map Put](rest_crud_operations.md#map-put) | `.../map-put/` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`, `id`, `property`, `key`, `value`
[Map Remove](rest_crud_operations.md#map-remove) | `.../map-remove/` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`, `id`, `property`, `key`
[Property Fetch](rest_crud_operations.md#property-fetch) | `.../rest/property-fetch` | `GET, POST, OPTIONS` | `sessionId`, `accessId`, `type`, `id`, `property`
[Set Insert](rest_crud_operations.md#set-insert) | `...rest/set-insert` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`, `id`, `property`, `value`
[Set Remove](rest_crud_operations.md#set-remove) | `...rest/set-remove` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`, `id`, `property`, `value`
[Update](rest_crud_operations.md#update) | `...rest/update` | `GET, POST, PUT, OPTIONS` | `sessionId`, `accessId`, `type`, `id`, `property`, `value`

### Assembly Manipulate

Assembly Manipulate allows you to update or create new entity types or entity instances using a JSON object.

There is one property whose inclusion to the instance definition changes the functionality of this operation, namely the `id` property. If you do not include an `id` in the instance definition, then the property values passed are used to create a new instance of the defined type. If the `id` is included in the JSON object, then the instance matching the value of the `id` is updated with the property values passed. An exception is thrown if a JSON object is passed with an id that does not exist.

During the creation of a new entity instance through the use of the Assembly Manipulate REST call, you can also assign an id that should be used during instantiation. The functionality remains the same when assigning an ID; however, the JSON object that is passed using the body parameter is different. Whereas the JSON object defined in a normal Assembly Manipulate call is of the entity type to be manipulated, the JSON object required to assign an idea should be of the type `com.braintribe.model.rest.crud.ManipulationEnvelope`

The JSON object wraps the type that is to be created and contains two properties:

* `assembly`, which defines the entity or entities that should be created. It should contain the JSON ID, the entity type and the properties that should be defined for the new instance.
* `instantiationsWithId`, which is used to determine the entities, referenced in the assembly property, that are being instantiated, since it is possible to use the `ManipulationEnvelope` also to manipulate existing entities.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/assembly-manipulate?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The access through which the REST can gain access to the needed workbench access. | Yes
`body`  | 	A serialized representation of the entity to be created.| Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response object responsible.
`payload`  | The induced manipulation object of the manipulation response.
`id` | `id` of the root value if it is an entity.
`ids` | IDs of the entities in a list if the root value is a list of entities.
`entity` | Root value if it is an entity.
`entities` | Entities contained in a list if the root value is a list of entities.

#### Examples

The assembly manipulate operation allows you to manipulate instances of an entity through the use of a JSON object provided using the REST parameter body.

You can also create new entities by omitting the `id` property in the entity definition.

The JSON object is opened and closed using a set of square brackets `[]`, while each instance definition is defined within a set of curly brackets `{}`. Each property belonging to the entity instance is defined with the following syntax: `"PROPERTY_NAME" : "PROPERTY_VALUE"` and is presented as a comma separated list.

The JSON object does not have to contain references to all properties of the particular entity – the only mandatory property definition is entity type, configured using the syntax: `_type: "TYPE_SIGNATURE_OF_ENTITY"` Otherwise, no other properties are mandatory.

##### Manipulate

You can manipulate an entity instance by referencing the entity type and the `id` of the particular instance you wish to edit. In this example, we focus on the John Doe entity.

Below is its corresponding JSON object:

```json
[
   {
  "_type": "tribefire.demo.model.data.Person",
  "_id": "0",
  "anything": 1,
  "firstName": "John",
  "gender": {
    "value": "male",
    "_type": "tribefire.demo.model.data.Gender"
  },
  "id": {
    "value": "1",
    "_type": "long"
  },
  "lastName": "Dawson",
  "partition": "access.demo",
  "ssn": "111"
}
]
```

We will use this object to change the last name of this instance to Dawson. You do not have to pass all properties using the JSON object, only those that you want to edit as well as the `type` and `id`.

```json
localhost:8080/tribefire-services/rest/assembly-manipulate?sessionId=yourSessionId&accessId=access.demo&body=
[
{
  "_type": "tribefire.demo.model.data.Person",
  "_id": "0",
  "anything": 1,
  "firstName": "John",
  "gender": {
    "value": "male",
    "_type": "tribefire.demo.model.data.Gender"
  },
  "id": {
    "value": "1",
    "_type": "long"
  },
  "lastName": "Dawson",
  "partition": "access.demo",
  "ssn": "111"
}
]
```

Depending on the projection used, the returned object differs. However, the default value, returned by the example above, is `null`.

Running this call results in updating this entity's `lastName` to Dawson.

##### Create

You can create a new entity instance by referencing the entity type while omitting the `id` property in the JSON object. This automatically creates a new instance with the properties values passed:

```json
[
{
  "_type": "tribefire.demo.model.data.Person",
  "_id": "0",
  "anything": 1,
  "firstName": "Joan",
  "gender": {
    "value": "male",
    "_type": "tribefire.demo.model.data.Gender"
  },
  "lastName": "Dawson",
  "partition": "access.demo",
  "ssn": "121"
}
]
```

As noted above, we do not need to define every property within the entity, only the type signature and any other properties required. From the example above: `firstName`, `lastName`, and `ssn`. The value of `ssn` must be unique, because the parameter is specified as unique in the metadata.

##### Assigned IDs

The Assembly Manipulate REST call also allows you to create new instances of entities with assigned IDs. The functionality of the REST call remains the same, in that you still pass a JSON object to tribefire using the `body` property; however, the actual JSON object is slightly different that shown above. To assign ids the created type is wrapped inside another type, called `com.braintribe.model.rest.crud.ManipulationError`. This type has two properties: `assembly` and `InstantiationsWithId`. The first property assembly contains the type and properties that should be instantiated, while the second `InstantiationsWithId` contains a reference to entities in the assembly property that identify which entities are to be instantiated, since it is also possible to use the `ManipulationError` envelope to manipulate existing properties.

```json
http://localhost:8080/tribefire-services/rest/assembly-manipulate?accessId=auth&sessionId=be1e4f59-7df9-47a2-9428-8898e2d170dd&body=
{
    "_type": "com.braintribe.model.rest.crud.ManipulationEnvelope",
    "assembly": {
  "_type": "tribefire.demo.model.data.Person",
  "_id": "1211",
  "anything": 1,
  "firstName": "Joan",
  "gender": {
    "value": "male",
    "_type": "tribefire.demo.model.data.Gender"
  },
  "lastName": "Dawson",
  "partition": "access.demo",
  "ssn": "1211"
},
    "instantiationsWithId": {
        "_type": "set",
        "value": [
            { "_ref": "0" }
        ]
    }
}
```

As mentioned above, the `ManipulationEnvelope` contains two properties, after the type definition `"_type": "com.braintribe.model.rest.crud.ManipulationEnvelope".`

The first property contains the information for the instance that is to be created. In this case, it contains the JSON ID – this is the reference that is used in the second property `InstantiationsWithId` – the type definition for the `User` entity, along with the definition of two properties (`id` and `name`) – it is possible to define further properties belonging to the `User` entity.

```json
{
    "_type": "com.braintribe.model.rest.crud.ManipulationEnvelope",
    "assembly": {
        "_id": "0",
        "_type": "com.braintribe.model.user.User",
        "id": "cortex2",
        "name": "Cortex2"
    }
}
```

The second part is required because without it tribefire would search for the existing entity or entities defined in the `assembly` property, and return an error message, since this instance doesn't yet exist. It should contain a set with the JSON IDs that reference the specific entity description from the `assembly` property.

```json
"instantiationsWithId": {
       "_type": "set",
       "value": [
           { "_ref": "0" }
       ]
   }
```

By default the successful execution of this call returns a `null` value. You can use the different projections available for Assembly Manipulate to change the return value.

### Assembly-Stream Manipulate

The assembly stream manipulate operation provides you with the same functionality as the assembly manipulate operation, the only difference being that rather than defining the JSON object in the URL, using the parameter `body`, as with assembly manipulate, the JSON object should be provided in the body of the REST call itself.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/assembly-stream-manipulate?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes

> The actual JSON object that defines the entity or entities that should be created or edited should be defined in the body of the REST call send.

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response object responsible.
`payload`  | The induced manipulation object of the manipulation response.
`id` | ID of the root value if it is an entity.
`ids` | IDs of the entities in a list if the root value is a list of entities.
`entity` | Root value if it is an entity.
`entities` | Entities contained in a list if the root value is a list of entities.

#### Example

Call:

```
POST
localhost:8080/tribefire-services/rest/assembly-stream-manipulate?sessionId=c11ec1bd-6117-47b3-bb63-dcecd64640ee&accessId=access.demo&depth=1&type=tribefire.demo.model.data.Person

[ { "_type": "com.braintribe.model.user.User", "email": "Second.User@braintribe.com", "firstName": "Second", "lastName": "User", "password": "password", "ssn": "999" } ]
```

### Logout

The `logout` call ends the user session being used to validate the REST calls. Any calls made after a logout call require a new session ID.

#### URL Syntax

```
GET
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/logout?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | The ID of the session that should be ended. | Yes

### Create

The create REST call generates a new entity instance in tribefire.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/create?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of type to be instantiated. | Yes
`*PropertyName`| You add values to specific properties to the entity during creation by adding them to the REST call. To do so, you add an asterisk ( * ) in front of the property name, and then add a value: `&*PropertyName=PropertyValue` | No

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response object responsible.
`payload`  | The `ChangeValueManipulation` entity instance created during the creation of the entity.
`id` | `id` and `type` of the newly created entity.
`entity` | The newly created entity.

#### Example

The create operation allows you to create a new entity instance by using the type signature of the entity to be created. Additionally, you can also pass property values to create new instances with specific data.

##### Simple Create

Call:

```
http://localhost:8080/tribefire-services/rest/create?sessionId=yourSessionID&accessId=access.demo&type=tribefire.demo.model.data.Person
```

This returns an empty instance.

> If an entity type has mandatory properties, this call does not work. Use the **Create with Property Values** call instead.

##### Create Entity with Property Values

You can also use the pass property names and values when creating a new entity. For example, two properties belonging to the opportunity instance above are name and description. To pass the property you use the following syntax: `*PropertyName=PropertyValue`.
Call:

```
http://localhost:8080/tribefire-services/rest/create?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&*firstName=John&*lastName=Ponton&*ssn=987
```

This creates a new entity instance with the property values passed via the REST called defined:

```json
{
  "_type": "tribefire.demo.model.data.Person",
  "_id": "0",
  "firstName": "John",
  "id": {
    "value": "51",
    "_type": "long"
  },
  "lastName": "Ponton",
  "partition": "access.demo",
  "ssn": "987"
}
```

### Delete 

The delete call removes the requested entity instance.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/delete?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be manipulated | Yes
`type` | The type signature of type to be deleted. | Yes
`id` | `id` of the entity to be deleted. | Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response.
`payload`  | The induced manipulation response.
`none` | Returns nothing. This is the default projection.

#### Example

Call:

```
POST
http://localhost:8080/tribefire-services/rest/delete?sessionId=yourSessionId&accessId=access.demo&type=com.braintribe.model.sales.Opportunity&id=47&projection=payload
```

Response:
`null`

### Entity Identification Resolution 

The entity identification resolution call returns the specific entity instance that matches a given ID.


#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/entity?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be created. | Yes
`type` | The type signature of type to be instantiated. | Yes
`id` | ID of the entity to be returned. | Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The `EntityQueryResult` object responsible.
`payload`  | (Default) The returned entity.

#### Example

Call:
```
http://localhost:8080/tribefire-services/rest/entity?sessionId=170612122942762f485da217df46bea8&accessId=access.demo&type=tribefire.demo.model.data.Person&id=56
```

Response:

```json
{
  "_type": "tribefire.demo.model.data.Person",
  "_id": "0",
  "firstName": "John",
  "id": {
    "value": "56",
    "_type": "long"
  },
  "lastName": "Ponton",
  "partition": "access.demo",
  "ssn": "123"
}
```

### Entity Fetch 

The entity fetch call returns instances belonging to a particular entity, as well as allows the use of ordering parameters to determine in which order and how many entities are returned.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/fetch?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of type to be returned. | Yes
`orderBy` | The name of the property which the entity instances should be ordered by. This value of this parameter should match one of the properties contained in the given entity. | No
`orderDir` | The direction of the ordering. This can be one of two values ascending (lists results from lowest to highest. For example, alphabetical order.) and (descending – list results from highest to lowest). | No

#### Projections

Name    | Description
------- | -----------
`envelope`  | The `EntityQueryResult` object responsible for providing the entity queried.
`payload`  | The entity objects of the type requested. This is the default projection.
`first-of-payload` | The first entity object of the payload of the type requested.

#### Examples

##### Simple Entity Fetch

Call:

```
http://localhost:8080/tribefire-services/rest/fetch?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person
```

This returns all instances of this entity.

##### Ordered Entity Fetch

In addition to the mandatory parameters, you can also use `orderBy` and `orderDir` to create an ordering for your results. The `orderBy` parameter is used to define on which property the search should take place, while `orderDir` defines the direction (that is, lowest to highest or highest to lowest).

`orderDir` accepts either `ascending` or `descending`, while `orderBy` is defined with the value of the property that should be used as the base for the ordering. If no value for `orderBy` is given, that is, it is not defined, then the entity's `id` property is used by default. Likewise, if no value is given for `orderDir`, `ascending` is used by default.
Calls:

```
//Order by property description. The default ordering will be ascending
http://localhost:8080/tribefire-services/rest/fetch?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&orderBy=description

//Order by property description and order direction descending
http://localhost:8080/tribefire-services/rest/fetch?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&orderBy=description&orderDir=descending

//No order by parameter, this means the default property will be used, id. Order dir ascending
http://localhost:8080/tribefire-services/rest/fetch?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&orderDir=ascending
```

### List Insert

The list insert call allows you to add new elements to a list. The call is used to access the particular instance of an entity and its `list` property. You must ensure the element you wish to add is of the same type as the list contains. For example, if you try to add a `String` value to a list containing `Integers`, you receive an error.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/list-insert?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of the entity which contains the list property. | Yes
`id` | 	The `id` of the entity instance which the value should be added to. | Yes
`property` | The name of the property which is of the type list and where the new value should be added to. | Yes
`value` | The value to be added to the list. | Yes
`index` | The index position where the value should be added to. The default value is 0, meaning that each new value added will be placed at the top, in position 0, of the list. The value of the index parameter must not exceed the current size of the list. Otherwise you receive an out of bound exception. | No

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response, the instance of `ManipulationResponse` which is created during the call.
`payload`  | The induced manipulation, this is part of the manipulation response. It describes the instance that is used to create the change.
`none` | Returns nothing. This is the default projection.

#### Examples

The list insert operation allows you to add elements into a specific list. By default, the element added is placed at the beginning of the list, that is position 1. However, you can also use the optional property `index` to define where in the list the element should be added..

Using the default projection, no information will be returned.

##### Simple Types

If the list contains a collection of simple types (Strings, Dates, integers, and so on), you simply use the `value` parameter to insert the new item to the list:
Call:

```
http://localhost:8080/tribefire-services/rest/list-insert?sessionId=yourSessionId
&accessId=access.demo
&type=tribefire.demo.model.data.Person
&id=3
&property=stringListExample
&value=newString
```

The value of `value` is then added to the list.

##### Complex Types

If the list contains a collection of complex types (that is, other entities), you use `value` to provide the `id` of the instance belonging to the list type.
Call:

```
http://localhost:8080/tribefire-services/rest/list-insert?sessionId=yourSessionId
&accessId=access.demo
&type=tribefire.demo.model.data.Person
&id=3
&property=children
&value=1
```

The example shows a list property `children`, of the type `Person`. The value is defined as `1`, and means that the instance of `Person` with the `id` value of `1` is added to the entity `Children`.

##### Specific Position in a List

In addition to the mandatory parameters, there is also `index`, which allows you to specify where in the list the element should be added.
Call:

```
http://localhost:8080/tribefire-services/rest/list-insert?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=children&value=14&index=2
```

This adds the instance of `Person` with the `id` of `14` into the list at position `2`.
> If the value of index is larger than the list itself, you receive an exception error and the element is not added to the list.

### List Remove

The list remove call is used to remove items from a list property of a particular entity instance.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/list-remove?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of the entity which contains the list property. | Yes
`property`| The name of the property which is of the type list and where the value should be removed from. | Yes
`value` | The value to be removed. | Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response, the instance of `ManipulationResponse` which is created during the call.
`payload`  | The induced manipulation, this is part of the manipulation response. It describes the instance that is used to create the change
`none` | Returns nothing. This is the default projection.

#### Examples

##### Simple Types

If the list contains a collection of simple types (Strings, Dates, integers, and so on), you use the parameter `value` to determine the object to be removed.
Call:

```
http://localhost:8080/tribefire-services/rest/list-remove?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=stringListExample&value=newString
```

The above example removes the String with the value `newString` from the list.

##### Complex Types

If the list contains a collection of complex types(that is, an instance of another entity), you use the parameter `value` to specify the `id` of the instance to be removed.
Call:

```
http://localhost:8080/tribefire-services/rest/list-remove?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=children&value=1
```

The example above removes the instance of `Person` with the `id` of `1` from the list `children`, which belongs to the entity `Person`.

### Map Put

The map put call allows you to enter a new value into a map property belonging to entity instance also provided in the call.


#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/map-put?PARAMETERS
```

#### Parameters

`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be created. | Yes
`type` | The type signature of the entity which contains the map property. | Yes
`id` | 	The `id` of the entity instance which the value should be added to. | Yes
`property` | The name of the property which is of the type map and where the new value should be added to. | Yes
`key` | The key for the new addition.| Yes
`value` | The value for the new addition. | Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response object responsible.
`payload`  | The induced manipulation, part of the manipulation response, describes the instance that is used to create the change.
`none` | Returns nothing. This is the default projection.

#### Examples

##### Simple Types

If the key or the value for a map is of a simple type (String, Date, Integer, and so on), you enter the simple value using the parameter `value`. For example, if you have a have a `Map<String,String>`:
Call:

```
http://localhost:8080/tribefire-services/rest/map-put?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=mapTest&key=newKeyString&value=newValueString
```

This example adds a string `newKeyString` as the key and `newValueString` as a new entry into a `map` property called `mapTest`.

##### Complex Types

If the key or the value for a map is of a complex type(that is, an instance of another entity), you use the `value` parameter to refer to the instance of the entity to be added. This is done by using the `id` of the specific instance. For example, if you have a `Map<Person,Opportunity>`, meaning that the key is of an instance of the entity `Person` and the value is an instance of the entity `Opportunity`, you add these like so:
Call:

```
//mapTest<Person,Opportunity>
http://localhost:8080/tribefire-services/rest/map-put?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=mapTest&key=1&value=3

//addressBook<Person, String>
http://localhost:8080/tribefire-services/rest/map-put?sessionId=f798ec0e-a077-42b3-9ea5-556da75782ad&accessId=access.demo&type=tribefire.demo.model.data.Person&id=1&property=fakeMap&key=4&value=069882924356
```

The first example adds an instance of `Person` with the `id` `1` to the `key` and an instance of `Person` with `id` `3` to the `value`. The second adds a `Person` with the `id` of `4` and the string `069882924356` to the map `fakeMap`, which is a map belonging to the `Person` entity.

### Map Remove

The map remove call is used to remove objects from a map collection belonging to an entity instance also defined in the call.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/map-remove?PARAMETERS
```

#### Parameters

`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of the entity which contains the map property. | Yes
`id` | 	The `id` of the entity instance which the value should be deleted from. | Yes
`property` | The name of the property which is of the type map and where the new value should be deleted from. | Yes
`key` | The key of the object you want to delete | Yes
`value` | The value of the object you want to delete | Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response object responsible.
`payload`  | The induced manipulation, part of the manipulation response, describes the instance that is used to create the change.
`none` | Returns nothing. This is the default projection.

#### Examples

The parameter `key` is used to refer to the specific element in the map that should be removed, and is used differently depending on the type of object the key is.

##### Simple Types

If the `key` parameter is a simple type (String, Date, integer, and so on), you use the `key` to specify the value of the key that should be removed.
Call:

```
http://localhost:8080/tribefire-services/rest/map-remove?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=mapTest&key=newKeyString
```

This example removes the entry with the key `newKeyString` belonging to the map `mapTest`, belonging to the `Person` instance with the `id` `3`.

##### Complex Types

If the `key` parameter is complex (that is, an instance of another entity), you use the `key` to refer to the `id` of the instance in the map that should be removed.
Call:

```
//addressBook<Person, String>
http://localhost:8080/tribefire-services/rest/map-remove?sessionId=f798ec0e-a077-42b3-9ea5-556da75782ad&accessId=access.demo&type=tribefire.demo.model.data.Person&id=1&property=fakeMap&key=4
```

This example removes the entry whose `key` is the instance of `Person` with the `id` of `4` from the map `fakeMap`, belonging to the `Person` instance with the `id` `1`.

### Property Fetch

The property fetch call is used to return a property belonging to an instance of an entity also defined in the call.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/property-fetch?PARAMETERS
```

#### Parameters

`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the property can be found. | Yes
`type` | The type signature of the entity that contains the property required. | Yes
`id` | The id of the specific entity instance from which the property will be retrieved from. | Yes
`property` | The name of the property belonging to the entity that should be returned. | Yes
`orderBy` | The name of the property by which the results should be ordered. | No
`orderDir` | The direction of the ordering. This can be one of two values ascending (lists results from lowest to highest. For example, alphabetical order.) and (descending – list results from highest to lowest). | No

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response object responsible.
`payload`  | Returns the value of the property queried. This is the default projection.
`first-of-payload` | first instance of information listed in property query result.

#### Examples

##### Simple Properties

If the type of property queried is simple, then that value is returned.
Call:

```
http://localhost:8080/tribefire-services/rest/property-fetch?sessionId=yourSessionId&accessId=access.demo&type=com.braintribe.model.sales.Person&id=1&property=lastName
```

This example returns the value of the property `lastName` for the `Person` entity instance with the `id` of `1`.

##### Complex Properties

If the type of property queried is complex, the entity representation for that particular instance is returned.
Call:

```
http://localhost:8080/tribefire-services/rest/property-fetch?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=1&property=image
```

This example returns the value of the complex property image, which is an instance of the entity `ImageResource`.

##### Collection Properties

If the type of the property queried is a collection type, then all elements in the collection are returned.
Call:

```
http://localhost:8080/tribefire-services/rest/property-fetch?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=2&property=children
```

This example returns instances belonging to the property `children` – a set of `Person` entity instances – of the `Person` instance with `id` `2`.

##### Ordering a Collection

If the property type is a collection, you can also use the parameters `orderBy` and `orderDir` to set an ordering on returned results. `orderBy` defines the property that should be ordered, whereas `orderDir` sets the direction. There are only two possible values for the `orderDir` parameter: `ascending` and `descending`.
> The property defined by `orderBy` should belong to the entity contained in the collection.
Call:

```
http://localhost:8080/tribefire-services/rest/property-fetch?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=2&property=children&orderBy=lastName&orderDir=descending
```

This example orders results based on the `lastName` property, which is part of the `Person` entity, and with a `descending` direction (that is highest to lowest).

##### Projections

By default, the property fetch REST call uses the projection `payload`, which returns all results for the property queried. However, it is possible to use `first-of-payload` to return the first result for the properties queried, and `envelope` to return the `PropertyQueryResult`, which is the entity responsible for handling property query results.

`first-of-payload` is only really relevant when querying property that are of a collection type. Since only a collection can return more than once instance for a property.

### Set Insert

The set insert call allows you to add a new element into a `set` property of a particular entity instance, also defined in the call.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/set-insert?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of the entity that contains the set property required. | Yes
`id` | The id of the specific entity instance that the new set property value will be added to. | Yes
`property` | The name of the set property belonging to the entity where the new value should be added to.	 | Yes
`value` | The value that should be added to the set. | Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response.
`payload`  | The induced manipulation response.
`none` | Returns nothing. This is the default projection.

#### Examples

The set insert operation allows you to add elements into a specific set. Depending on the type to be inserted, there are two specific uses for the `parameter` value.

##### Simple Types

If the set contains a collection of simple types (Strings, Dates, integers, and so on), you simple use the `value` parameter to insert the value to the set.
Call:

```
http://localhost:8080/tribefire-services/rest/set-insert?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=stringSetFake&value=newString
```

The entity `Person` has a `set`, called `stringSetFake`, and the value `newString` is added to `stringSet`, belonging to the `Person` instance with the `id` `3`.

##### Complex Types

If the set contains a collection of complex types (that is, other entities), you use the `value` parameter to refer to the specific instance of the entity that should be added to the set.
Call:

```
http://localhost:8080/tribefire-services/rest/set-insert?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=1&property=fakeSet&value=10
```

The example above adds the instance of `Customer` with the `id` of `10` to the set `fakeSet`, belonging to the entity `Person`.

### Set Remove

The set remove call is used to remove elements from a set belonging to a particular entity instance also defined in the call.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/set-remove?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of the entity that contains the set property required. | Yes
`id` | The id of the specific entity instance that the set property value will be deleted from. | Yes
`property` | The name of the set property belonging to the entity where the value should be deleted from. | Yes
`value` | The value that should be removed from the set. | Yes

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response.
`payload`  | The induced manipulation response.
`none` | Returns nothing. This is the default projection.

#### Examples

The set remove REST call is used to remove specific items from a set. `property` is used to define the set, while `value` defines the element that should be removed from the set. Depending on the type to be removed, there are two specific uses for the parameter value.

##### Simple Types

If the set contains a collection of simple types (Strings, Dates, integers, and so on), you use the parameter `value` to determine the object to be removed.
Call:

```
http://localhost:8080/tribefire-services/rest/set-remove?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=3&property=fakeSet&value=newString
```

The entity `Person` has a `set`, called `fakeSet`, and the value `newString` is removed from `fakeSet`, belonging to the `Person` instance with the `id` `3`.

##### Complex Types

If the set contains a collection of complex types (that is, an instance of another entity), you use the parameter `value` to specify the `id` of the instance to be removed.
Call:

```
http://localhost:8080/tribefire-services/rest/set-remove?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=1&property=children&value=10
```

This example removes the instance of `Person` with the `id` of `10` from the set `children`, which belongs to the entity `Person`.

### Update

The update call can be used to update specific entities.

#### URL Syntax

```
https://TRIBEFIRE_SERVER:PORT_NUMBER/tribefire-services/rest/update?PARAMETERS
```

#### Parameters

Name    | Description | Required
------- | -----------
`sessionId`  | 	The valid session that grants the call access to tribefire. | Yes
`accessId`  | The `accessId` through which the entity can be found. | Yes
`type` | The type signature of type to be instantiated. | Yes
`id` | The id of the entity instance that should be updated. | Yes
`*PropertyName`| You add values to specific properties to the entity during creation by adding them to the REST call. To do so, you add an asterisk ( * ) in front of the property name, and then add a value: `&*PropertyName=PropertyValue` | No

#### Projections

Name    | Description
------- | -----------
`envelope`  | The manipulation response object responsible.
`payload`  | The `ChangeValueManipulation` entity instance created during the creation of the entity.
`none` | (Default) Returns nothing.
`entity` | Returns the entity object that has been updated.

#### Examples

The update REST call is used, at the name suggests, to update properties of specific entities, allowing you to edit property values. The properties that should be updated should also be given with an asterisk: `*PROPERTY_NAME=PROPERTY_VALUE`

##### Simple Update

This simple update REST call only updates one property `lastName`, which belongs to the `Person` entity. The new value for this property is `Smith`.
Call:

```
http://localhost:8080/tribefire-services/rest/update?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=4&*lastName=Smith
```

When referring to a property in the update REST call, or indeed any REST call that accepts property names, the property name must be preceded with an asterisk **/***.

##### Update More than One Property

You can use the update REST call to update more than one property at a time. You simply add the properties that you need to the end of the REST statement.
Call:

```
http://localhost:8080/tribefire-services/rest/update?sessionId=yourSessionId&accessId=access.demo&type=tribefire.demo.model.data.Person&id=4&*lastName=Smith&*firstName=John
```

This updates two properties, `firstName` and `lastName`, at the same time.

##### Updating Enum Properties

You update an enum property in the same way as you would update any other property, although the value given must be part of the enum associated with the respective property.
Call:

```
http://localhost:8080/tribefire-services/rest/update?sessionId=yourSessionId&accessId=access.demo&type=com.braintribe.model.sales.Keyfact&id=1&*keyfactType=NEWS
```

##### Updating a Complex Property

A complex property is one which represents an instance of another entity type. You can update the property with a new complex type instance by referring to the `id` of the complex type instance that should be added to the property.

For example, a hypothetical `Invoice` entity has a property called `salesDocument`, which is of the type `SalesDocument`. You, therefore, use the `value` to refer to the `id` of the instance, in the example below the `id` is `9`, that should be assigned to this property.
Call:

```
http://localhost:8080/tribefire-services/rest/update?sessionId=yourSessionId&accessId=access.demo&type=com.braintribe.model.sales.Invoice&id=3&*salesDocument=9
```

This example assigned the instance of `SalesDocument` with the `id` of `9` to the `salesDocument` property, which belongs to the entity instance of `Invoice` which has the `id` `3`.
> If you wish to remove a instance from a complex property, you use `null` as the property value.
