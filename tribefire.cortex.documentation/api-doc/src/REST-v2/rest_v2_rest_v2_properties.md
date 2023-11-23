# REST API - CRUD on Entity Properties

The /rest/v2/entities endpoint allows you to perform CRUD operations on entity properties.

## General

This endpoint contains the APIs used for working with entity properties.

The URL for working with properties is always: `/rest/v2/properties/<accessName>/<entity.TypeSignature>/<id>(/<partition>)/<property>` 


URL Part | Description
--- | ---
`accessName` | Name of the access that contains the entity.
`entity.TypeSignature` | Type signature (or simple name, if only one entity type with this name in the access) of the entity to query.
`id` | `id` of the entity
`partition` |  Partition of the entity, if there are more than one entities with the same `id` in the access. This parameter is optional.

### `GET`

The `GET` method returns the value of the property.

#### Endpoint Configuration

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `envelope` or `value`
  Default Value | `value`
  Description | `envelope`: returns the entire `PropertyQueryResult` <br/>  `value`: returns the value of the property, encoded in the required `mimeType`

* Write Empty Properties

  Item | Description
  ----- | -----
  URL Parameter | `write-empty-properties=<...>`
  Header Parameter | `gm-write-empty-properties=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller returns all properties that are set to `null` or are empty (for maps, sets and lists)

* Stabilize Order

  Item | Description
  ----- | -----
  URL Parameter | `stabilize-order=<...>`
  Header Parameter | `gm-stabilize-order=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `writeEmptyProperties` parameter.

* Prettiness

  Item | Description
  ----- | -----
  URL Parameter | `prettiness=<...>`
  Header Parameter | `gm-prettiness=<...>`
  Type | `none`, `low`, `med` or `high`
  Default Value | `mid`
  Description |  This property represents the level of prettiness used when writing the assembly back to the body of the response. <br/> Each implementation of the marshaller may use this value slightly differently, but as a rule of thumb, `none` contains no new lines or indentation information and should only be used to minimize the size of the body and `high` provides the best possible indentation for humans to read.

* Depth

  Item | Description
  ----- | -----
  URL Parameter | `depth=<...>`
  Header Parameter | `gm-depth=<...>`
  Type | `shallow` - returns only the first level <br/> `reachable` - returns the whole assembly <br/> `number >= 0` - returns the provided level, starting at `0`
  Default Value | `3`
  Description |  For complex assemblies, this property specifies how deep the returned assembly should be traversed before being returned. This property is a simplified TraversingCriterion.


* Entity Recurrence Depth

  Item | Description
  ----- | -----
  URL Parameter | `entity-recurrence-depth=<...>`
  Header Parameter | `gm-entity-recurrence-depth=<...>`
  Type | `number >= -1` - returns the provided level, starting at `0`
  Default Value | `0`
  Description |  For complex entities which have recurrent entities (entities that appear more than once in the returned JSON), this property specifies how deep the returned recurrent entity should be traversed before being returned. This property is used to avoid `_id` and `_ref` in the returned JSON.

> The `entity-recurrence-depth` parameter is applied after the `depth` parameter.
  
* Type Explicitness

  Item | Description
  ----- | -----
  URL Parameter | `type-explicitness=<...>`
  Header Parameter | `gm-type-explicitness=<...>`
  Type | `auto`, `always`, `entities` or `polymorphic`
  Default Value | `auto`
  Description | This property is used to decide whether `_type` is present in marshalled JSON. <br/> <br/> This property is only available in JSON marshaller. <br/> <br/> **auto** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **always** - `_type` is always returned for every element <br> **entities** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **polymorphic** - `_type` is returned for every element if the actual type cannot be established from the context.
  
* Identity Management Mode

  Item | Description
  ----- | -----  
  URL Parameter | `identity-management-mode=<...>`
  Header Parameter | `gm-identity-management-mode=<...>`
  Type | `off`, `auto`, `_id` or `id`
  Default Value | `auto`
  Description |  Represents how duplicates of objects should be unmarshalled. <br/> <br/> **auto** - Depending on the parsed assembly, the marshaller automatically detects the identification information and uses it for identity management.<br/> **off** - No identity management. <br/> **_id** - The internally generated `_id` information is used, if available.<br/> **id** - The `id` property is used, if available.

### `POST`

The `POST` method can be used to edit `collection` properties, i.e. `map`, `set` or `list` properties.

If you use this method on a non-collection property, a `400 Bad request` is thrown.

This method supports adding elements to collections (default behavior) or removing elements from collections (with URL parameter `remove=true` or Header `gm-remove=true`).

Below you can find the possibilities for the content of the body of the method and their effect depending on the `remove` property and the type of of the property (`list`, `set` or `map`):

| Body Content     | Remove           | Property Type - List                                                                                                                                                                                                               | Property Type - Set                                              | Property Type - Map                                   |
| ---------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------ | ------------------------------------- |
| single value     | empty or `false` | Add the value to the list                                                                                                                                                                                          | Adds the value to the set.                         | `400`: The body should contain a map    |
| single value     | `true`           | `400`: body must be a `map<int, value>`                                                                                                                                                                              | Removes the value from the set.                   | `400`: The body should contain a map    |
| `list<value>`    | empty or `false` | Add the values to the end of the list.                                                                                                                                                                              | Adds the values to the set.                        | `400`: The body should contain a map    |
| `list<value>`    | `true`           | `400`: Body must be a `map<int, value>`                                                                                                                                                                              | Removes the values from the set.                  | `400`: The body should contain a map    |
| `map<key,value>` | empty or `false` | Keys must be of type `integer`.<br/> <br/> Inserts the value at the given positions. <br/>If the key is bigger than the size of the list, the value is added at the end of the list.                                                  | `400`: The body should contain value or list of values | Puts the given values in the map.      |
| `map<key,value>` | `true`           | Keys must be of type `integer`.<br/> <br/>Removes the values at the given position. If the value in the list at provided position (key) is not the same as the value given in the body, the value is removed from the list. | `400`: The body should contain value or list of values | Removes the values for the given keys. |


* Changing a complex property


  In this example you will add Frank and another person with ID `28` as friends of John's. If you wanted to remove some of John's friends, you should provide the endpoint parameter `remove=true` (or header `gm-remove` value `true`) in your call and provide the values to be removed in the body.


  * Method: `POST`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/properties/demo.PersonAccess/Person/{id}/friends?sessionId=xxx`
  * Headers:
    * `Accept`: `application/json`
    * `Content-Type`: `application/json`
  * Body:

  ```json

  {
    "_type": "map",
    "value": [
      {
        "key": 0,
        "value": {
          "_type": "tribefire.demo.model.data.Person",
          "id": {
            "_type": "long",
            "value": 1
          }
        }
      },
      {
        "key": 1,
        "value": {
          "_type": "tribefire.demo.model.data.Person",
          "id": {
            "_type": "long",
            "value": 28
          }
        }
      }
    ]
  }

  ```

  This call returns:

  ```json
  true
  ```

#### Endpoint Configuration

* Remove

  Item | Description
  ----- | -----
  URL Parameter | `remove=<...>`
  Header Parameter | `gm-remove=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When set to `true`, removes the values in the body from the collection instead of adding them.

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `envelope` or `success`
  Default Value | `success`
  Description | Return type for the call. <br/> <br/> `envelope`: returns the entire `ManipulationResponse` <br/> `success`: returns the value `true`

* Write Empty Properties

  Item | Description
  ----- | -----
  URL Parameter | `write-empty-properties=<...>`
  Header Parameter | `gm-write-empty-properties=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller returns all properties that are set to `null` or are empty (for maps, sets and lists)

* Prettiness

  Item | Description
  ----- | -----
  URL Parameter | `prettiness=<...>`
  Header Parameter | `gm-prettiness=<...>`
  Type | `none`, `low`, `med` or `high`
  Default Value | `mid`
  Description |  This property represents the level of prettiness used when writing the assembly back to the body of the response. <br/> Each implementation of the marshaller may use this value slightly differently, but as a rule of thumb, `none` contains no new lines or indentation information and should only be used to minimize the size of the body and `high` provides the best possible indentation for humans to read.

* Stabilize Order

  Item | Description
  ----- | -----
  URL Parameter | `stabilize-order=<...>`
  Header Parameter | `gm-stabilize-order=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `writeEmptyProperties` parameter.

* Depth

  Item | Description
  ----- | -----
  URL Parameter | `depth=<...>`
  Header Parameter | `gm-depth=<...>`
  Type | `shallow` - returns only the first level <br/> `reachable` - returns the whole assembly <br/> `number >= 0` - returns the provided level, starting at `0`
  Default Value | `3`
  Description |  For complex assemblies, this property specifies how deep the returned assembly should be traversed before being returned. This property is a simplified TraversingCriterion.


* Entity Recurrence Depth

  Item | Description
  ----- | -----
  URL Parameter | `entity-recurrence-depth=<...>`
  Header Parameter | `gm-entity-recurrence-depth=<...>`
  Type | `number >= -1` - returns the provided level, starting at `0`
  Default Value | `0`
  Description |  For complex entities which have recurrent entities (entities that appear more than once in the returned JSON), this property specifies how deep the returned recurrent entity should be traversed before being returned. This property is used to avoid `_id` and `_ref` in the returned JSON.


> The `entity-recurrence-depth` parameter is applied after the `depth` parameter.
  

* Type Explicitness

  Item | Description
  ----- | -----
  URL Parameter | `type-explicitness=<...>`
  Header Parameter | `gm-type-explicitness=<...>`
  Type | `auto`, `always`, `entities` or `polymorphic`
  Default Value | `auto`
  Description | This property is used to decide whether `_type` is present in marshalled JSON. <br/> <br/> This property is only available in JSON marshaller. <br/> <br/> **auto** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **always** - `_type` is always returned for every element <br> **entities** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **polymorphic** - `_type` is returned for every element if the actual type cannot be established from the context.
  
* Identity Management Mode

  Item | Description
  ----- | -----
  URL Parameter | `identityManagementMode=<...>`
  Header Parameter | `gm-identityManagementMode=<...>`
  Type | `off`, `auto`, `_id` or `id`
  Default Value | `auto`
  Description |  Represents how duplicates of objects should be unmarshalled. <br/> <br/> **auto** - Depending on the parsed assembly, the marshaller automatically detects the identification information and uses it for identity management.<br/> **off** - No identity management. <br/> **_id** - The internally generated `_id` information is used, if available.<br/> **id** - The `id` property is used, if available.


### `PUT` and `PATCH`

The `PUT` and `PATCH` methods can be used to set the value of the property to the value provided in the body of the call.


You can call those methods on properties of types: 

* `list`
* `set`
* `map` 

Calling those methods on one of the above property type replaces the content of the collection by the content passed in the body.
> This API is idempotent, which means you can call it multiple times, but the result does not change after the initial call.


* Changing a simple property

  In this example we will add Frank as a friend of John's. We are posting the `{id}` parameter value `2` in the URL, because `2` is Frank's ID:

  * Method: `PUT or PATCH`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/properties/demo.PersonAccess/Person/{id}/name?sessionId=xxx`
  * Headers:
    * `Accept`: `application/json`
    * `Content-Type`: `application/json`
  * Body:

  ```json
  "Bar"
  ```

  This call returns:

  ```json
  true
  ```


#### Endpoint Configuration

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `envelope` or `success`
  Default Value | `success`
  Description | Return type for the call. <br/> <br/> `envelope`: returns the entire `ManipulationResponse` <br/> `success`: returns the value `true`

* Write Empty Properties

  Item | Description
  ----- | -----
  URL Parameter | `write-empty-properties=<...>`
  Header Parameter | `gm-write-empty-properties=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller returns all properties that are set to `null` or are empty (for maps, sets and lists)

* Prettiness

  Item | Description
  ----- | -----
  URL Parameter | `prettiness=<...>`
  Header Parameter | `gm-prettiness=<...>`
  Type | `none`, `low`, `med` or `high`
  Default Value | `mid`
  Description |  This property represents the level of prettiness used when writing the assembly back to the body of the response. <br/> Each implementation of the marshaller may use this value slightly differently, but as a rule of thumb, `none` contains no new lines or indentation information and should only be used to minimize the size of the body and `high` provides the best possible indentation for humans to read.

* Stabilize Order

  Item | Description
  ----- | -----
  URL Parameter | `stabilize-order=<...>`
  Header Parameter | `gm-stabilize-order=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `writeEmptyProperties` parameter.

* Depth

  Item | Description
  ----- | -----
  URL Parameter | `depth=<...>`
  Header Parameter | `gm-depth=<...>`
  Type | `shallow` - returns only the first level <br/> `reachable` - returns the whole assembly <br/> `number >= 0` - returns the provided level, starting at `0`
  Default Value | `3`
  Description |  For complex assemblies, this property specifies how deep the returned assembly should be traversed before being returned. This property is a simplified TraversingCriterion.


* Entity Recurrence Depth

  Item | Description
  ----- | -----
  URL Parameter | `entity-recurrence-depth=<...>`
  Header Parameter | `gm-entity-recurrence-depth=<...>`
  Type | `number >= -1` - returns the provided level, starting at `0`
  Default Value | `0`
  Description |  For complex entities which have recurrent entities (entities that appear more than once in the returned JSON), this property specifies how deep the returned recurrent entity should be traversed before being returned. This property is used to avoid `_id` and `_ref` in the returned JSON.


> The `entity-recurrence-depth` parameter is applied after the `depth` parameter.
  

* Type Explicitness

  Item | Description
  ----- | -----
  URL Parameter | `type-explicitness=<...>`
  Header Parameter | `gm-type-explicitness=<...>`
  Type | `auto`, `always`, `entities` or `polymorphic`
  Default Value | `auto`
  Description | This property is used to decide whether `_type` is present in marshalled JSON. <br/> <br/> This property is only available in JSON marshaller. <br/> <br/> **auto** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **always** - `_type` is always returned for every element <br> **entities** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **polymorphic** - `_type` is returned for every element if the actual type cannot be established from the context.
  
* Identity Management Mode

  Item | Description
  ----- | -----
  URL Parameter | `identityManagementMode=<...>`
  Header Parameter | `gm-identityManagementMode=<...>`
  Type | `off`, `auto`, `_id` or `id`
  Default Value | `auto`
  Description |  Represents how duplicates of objects should be unmarshalled. <br/> <br/> **auto** - Depending on the parsed assembly, the marshaller automatically detects the identification information and uses it for identity management.<br/> **off** - No identity management. <br/> **_id** - The internally generated `_id` information is used, if available.<br/> **id** - The `id` property is used, if available.


### `DELETE`

The `DELETE` method can be used to set the value of a property to `null` or the default empty value.

Property Type | Default Empty Value
`boolean` | `false`
`int` <br/> `long` <br/> `float` <br/> `double` <br/> `decimal` | `0`
`date`<br/> `entity`<br/> `enum`<br/> `object` <br/> `string` | `null`
`list` <br/> `set` <br/> `map` | empty collection

#### Endpoint Configuration

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `envelope` or `success`
  Default Value | `success`
  Description | Return type for the call. <br/> <br/> `envelope`: returns the entire `ManipulationResponse` <br/> `success`: returns the value `true`

* Write Empty Properties

  Item | Description
  ----- | -----
  URL Parameter | `write-empty-properties=<...>`
  Header Parameter | `gm-write-empty-properties=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller returns all properties that are set to `null` or are empty (for maps, sets and lists)

* Prettiness

  Item | Description
  ----- | -----
  URL Parameter | `prettiness=<...>`
  Header Parameter | `gm-prettiness=<...>`
  Type | `none`, `low`, `med` or `high`
  Default Value | `mid`
  Description |  This property represents the level of prettiness used when writing the assembly back to the body of the response. <br/> Each implementation of the marshaller may use this value slightly differently, but as a rule of thumb, `none` contains no new lines or indentation information and should only be used to minimize the size of the body and `high` provides the best possible indentation for humans to read.

* Stabilize Order

  Item | Description
  ----- | -----
  URL Parameter | `stabilize-order=<...>`
  Header Parameter | `gm-stabilize-order=<...>`
  Type | Boolean
  Default Value | `false`
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `writeEmptyProperties` parameter.

* Depth

  Item | Description
  ----- | -----
  URL Parameter | `depth=<...>`
  Header Parameter | `gm-depth=<...>`
  Type | `shallow` - returns only the first level <br/> `reachable` - returns the whole assembly <br/> `number >= 0` - returns the provided level, starting at `0`
  Default Value | `3`
  Description |  For complex assemblies, this property specifies how deep the returned assembly should be traversed before being returned. This property is a simplified TraversingCriterion.


* Entity Recurrence Depth

  Item | Description
  ----- | -----
  URL Parameter | `entity-recurrence-depth=<...>`
  Header Parameter | `gm-entity-recurrence-depth=<...>`
  Type | `number >= -1` - returns the provided level, starting at `0`
  Default Value | `0`
  Description |  For complex entities which have recurrent entities (entities that appear more than once in the returned JSON), this property specifies how deep the returned recurrent entity should be traversed before being returned. This property is used to avoid `_id` and `_ref` in the returned JSON.

> The `entity-recurrence-depth` parameter is applied after the `depth` parameter.
  

* Type Explicitness

  Item | Description
  ----- | -----
  URL Parameter | `type-explicitness=<...>`
  Header Parameter | `gm-type-explicitness=<...>`
  Type | `auto`, `always`, `entities` or `polymorphic`
  Default Value | `auto`
  Description | This property is used to decide whether `_type` is present in marshalled JSON. <br/> <br/> This property is only available in JSON marshaller. <br/> <br/> **auto** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **always** - `_type` is always returned for every element <br> **entities** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **polymorphic** - `_type` is returned for every element if the actual type cannot be established from the context.
  
* Identity Management Mode

  Item | Description
  ----- | -----
  URL Parameter | `identityManagementMode=<...>`
  Header Parameter | `gm-identityManagementMode=<...>`
  Type | `off`, `auto`, `_id` or `id`
  Default Value | `auto`
  Description |  Represents how duplicates of objects should be unmarshalled. <br/> <br/> **auto** - Depending on the parsed assembly, the marshaller automatically detects the identification information and uses it for identity management.<br/> **off** - No identity management. <br/> **_id** - The internally generated `_id` information is used, if available.<br/> **id** - The `id` property is used, if available.

## Try it Out

If you want to experiment with the API for yourself, use our integrated Swagger UI. 

> For more information, see [Using Swagger UI](using_swagger_ui.md).
