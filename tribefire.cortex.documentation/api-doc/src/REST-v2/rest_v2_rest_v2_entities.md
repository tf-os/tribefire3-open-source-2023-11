# REST API - CRUD on Entities

The /rest/v2/entities endpoint allows you to perform CRUD operations on entity instances.

## `GET`

This method allows to query a single entity by `id` (and optionally, by `partition`) or by match of certain criteria using a `where` statement.

### `GET` by `id`

You can query entities by `id` using the following URL `/rest/v2/entities/<accessName>/<entity.TypeSignature>/<id>(/<partition>)`

URL Part | Description
--- | ---
`accessName` | Name of the access that contains the entity.
`entity.TypeSignature` | Type signature (or simple name, if only one entity type with this name in the access) of the entity to query.
`id` | `id` of the entity
`partition` |  Partition of the entity, if there is more than one entity with the same `id` in the access. This parameter is optional.

Possible responses:

* Success

  Code: `200`

  Body: The entity, encoded in the `mimeType` requested in the `Accept` header

* If the access or entity type, or instance does not exist

  Code: `404`

* If there is more than one instance with the given `id` or `entityType`

  Code: `400`


In this example, we assume you have a `com.braintribe.model.custom.demo.PersonModel#1.0` model with the following entities and their properties:

* `com.braintribe.model.custom.demo.Person`
  * string `id`
  * string `name`
  * list<Person> `friends`

Let's assume a `demo.PersonAccess` access uses the model above and is deployed properly. Below, you can find an example call.

* Method: `GET`
* URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person/1?sessionId=xxx`
* Headers
  * `Accept`: `application/json`

That call returns the following:

```json
{
  "_type": "person.model.Person",
  "_id": "0",
  "friends": [
    {
      "_id": "1",
      "friends": [
        {
          "_id": "2",
          "globalId": "4fe20678-bf35-4400-ba0c-2c62aaa7fac2",
          "id": "3",
          "name": "Smith",
          "partition": "person.access"
        },
        {
          "_ref": "0"
        },
        {
          "_id": "3",
          "globalId": "60787b42-e08f-40c5-8fe8-ed61b50e8c81",
          "id": "1",
          "name": "Foo",
          "partition": "person.access"
        }
      ],
      "globalId": "f406d8b5-270f-4396-9f94-5c9d47e4dbf0",
      "id": "2",
      "name": "bar",
      "partition": "person.access"
    },
    {
      "_ref": "3"
    }
  ],
  "globalId": "d6d7fc5e-df55-443f-92a1-02acac73416d",
  "id": "4",
  "name": "John",
  "partition": "person.access"
}
```

### Endpoint Configuration

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `DdraGetEntitiesProjection` <br/> <br/> Possible values: `envelope`, `results` or `firstResult`
  Default Value | `firstResult`
  Description | `envelope`: returns the entire `EntityQueryResult` <br/> `results`: returns the list of entities (a list of length 1 in this case) <br/> `firstResult`: returns the first entity in the result list

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
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `write-empty-properties` parameter.

* Depth

  Item | Description
  ----- | -----
  URL Parameter | `depth=<...>`
  Header Parameter | `gm-depth=<...>`
  Type | `shallow` - returns only the first level <br/> `reachable` - returns the whole assembly <br/> `number >= 0` - returns the provided level, starting at `0`
  Default Value | `3`
  Description |  For complex assemblies, this property specifies how deep the returned assembly should be traversed before being returned. This property is a simplified TraversingCriterion.
  
* Prettiness

  Item | Description
  ----- | -----
  URL Parameter | `prettiness=<...>`
  Header Parameter | `gm-prettiness=<...>`
  Type | `none`, `low`, `med` or `high`
  Default Value | `mid`
  Description |  This property represents the level of prettiness used when writing the assembly back to the body of the response. <br/> Each implementation of the marshaller may use this value slightly differently, but as a rule of thumb, `none` contains no new lines or indentation information and should only be used to minimize the size of the body and `high` provides the best possible indentation for humans to read.


* Entity Recurrence Depth

  Item | Description
  ----- | -----
  URL Parameter | `entity-recurrence-depth=<...>`
  Header Parameter | `gm-entity-recurrence-depth=<...>`
  Type | `number >= -1` - returns the provided level, starting at `0`
  Default Value | `0`
  Description |  For complex entities which have recurrent entities (entities that appear more than once in the returned JSON), this property specifies how deep the returned recurrent entity should be traversed before being returned. This property is used to avoid `_id` and `_ref` in the returned JSON.


 The `entity-recurrence-depth` parameter is applied after the `depth` parameter.
  

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
  Description |  Represents how duplicates of objects should be unmarshalled.<br/><br/> **auto** - Depending on the parsed assembly the marshaller automatically detects the identification information and uses it for identity management.<br/> **off** - No identity management done at all.<br/> **_id** - The internally generated `_id` information is used if available.<br/> **id** - The id property is used if available.
  
> For more information, see [REST Parameters and Headers](rest_v2_introduction.md#rest-parameters-and-headers).


### `GET` using a `where` Statement

The `where` statement allows you to query for entities based on a provided value of a given property. You can query with the `where` clause using the following URL: `/rest/v2/entities/<accessName>/<entity.TypeSignature>(?where.<property1>=<value1>(&<property2>=<value2>(&...)))`. 

> Querying resources (entities) with `where.someObjectProperty` (e.g. `where.id`) will not work as `Object` for URL query parameters and headers is not allowed. For operations over a specific resource consider using the `/access/resource/{resourceId}` endpoint.
 
GMQL is not supported. Use the `QueryEntities` service request for more powerful query capabilities.

URL Part | Description
`accessName` | Name of the access that contains the entity.
`entity.TypeSignature` | Type signature (or simple name, if only one entity type with this name in the access) of the entity to query.
`propertyX=valueX` |  Query that checks whether the given property has the given value. The value is translated from `string` to the type of the property. <br/> <br/> Only simple properties (string, Boolean, int, long, float, double, BigDecimal, enum) are allowed. <br/> <br/> If multiple `where` clauses are specified, the resulting query does an `AND` between all the conditions.

Possible responses:

* Success

  Code: `200`

  Body: The list of entities, encoded in the `mimeType` requested in the `Accept` header

* If no entity with the given property value(s) is found
  
  Code: `200`

  Body: Empty list

* If the access or entity type does not exist

  Code: `404`

* If there is more than one entity with the given `entityType`

  Code: `400`


In this example, we assume you have a `com.braintribe.model.custom.demo.PersonModel#1.0` model with the following entities and their properties:

* `com.braintribe.model.custom.demo.Person`
  * string `id`
  * string `name`
  * list<Person> `friends`

Let's assume a `demo.PersonAccess` access uses the model above and is deployed properly. Below, you can find an example call.

* Method: `GET`
* URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person?where.name=Patrick&sessionId=xxx`
* Headers
  * `Accept`: `application/json`

Returns the following:

```json
[
  {
    "_type": "com.braintribe.model.custom.demo.Person",
    "id": 1,
    "name": "Patrick",
    "friends": [
      {
        "_type": "com.braintribe.model.custom.demo.Person",
        "id": 2,
        "name": "Bob",
        "friends": [
          {
            "_ref": 1
          }
        ]
      }
    ]
  }
]
```

### Endpoint Configuration

* Starting Index

  Item | Description
  ----- | -----
  URL Parameter | `start-index=<...>`
  Header Parameter | `gm-start-index=<...>`
  Type | `int` >= 0
  Mandatory | no
  Default Value | `0`
  Description | Starting index used for pagination.

* Maximum Number of Results

  Item | Description
  ----- | -----
  URL Parameter | `max-results=<...>`
  Header Parameter | `gm-max-results=<...>`
  Type | `int` >= 0
  Mandatory | no
  Default Value | none
  Description |  Maximum number of results to return. If not set, returns all results.


* Order By

  Item | Description
  ----- | -----
  URL Parameter | `order-by=<...>`
  Header Parameter | `gm-order-by=<...>`
  Type | `String` or `list<String>`
  Mandatory | no
  Default Value | none
  Description |  Properties to order by.
  Example | `/rest/v2/entities/myAccess/MyEntity?orderBy=property1&orderBy=property2` orders by `property1` and, if multiple entities have the same value, `property2`, both in ascending order.


* Order Direction

  Item | Description
  ----- | -----
  URL Parameter | `order-direction=<...>`
  Header Parameter | `order-direction=<...>`
  Type | `OrderingDirection` or `list<OrderingDirection>`
  Mandatory | no
  Possible Values | `ascending` or `descending`
  Default Value | `ascending`
  Description |  Ordering direction of the property you want to order by with the `orderBy` parameter.
  Example | `/rest/v2/entities/myAccess/MyEntity?orderBy=property1&orderingDirection=descending&orderBy=property2` orders by `property1` in descending order and, if multiple entities have the same value, `property2` in ascending order.

* Distinct

  Item | Description
  ----- | -----
  URL Parameter | `distinct=<...>`
  Header Parameter | `gm-distinct=<...>`
  Type | Boolean
  Mandatory | no
  Default Value | `false`
  Description |  Whether or not to include duplicate values in the query result.

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `DdraGetEntitiesProjection` <br/> <br/> Possible values: `envelope`, `results` or `firstResult`
  Default Value | `firstResult`
  Description | `envelope`: returns the entire `EntityQueryResult` <br/> `results`: returns the list of entities (a list of length 1 in this case) <br/> `firstResult`: returns the first entity in the result list

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
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `write-empty-properties` parameter.

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
  Description | This property is used to decide whether `_type` is present in marshalled JSON. <br/> <br/> This property is only available in JSON marshaller. <br/> <br/> **auto** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **always** - `_type` is always returned for every element <br> **entities** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **polymorphic** - `_type` is returned for every element if the actual type cannot be established from the context
  
* Identity Management Mode

  Item | Description
  ----- | -----
  URL Parameter | `identity-management-mode=<...>`
  Header Parameter | `gm-identity-management-mode=<...>`
  Type | `off`, `auto`, `_id` or `id`
  Default Value | `auto`
  Description |  Represents how duplicates of objects should be unmarshalled. <br/> <br/> **auto** - Depending on the parsed assembly, the marshaller automatically detects the identification information and uses it for identity management.<br/> **off** - No identity management. <br/> **_id** - The internally generated `_id` information is used, if available.<br/> **id** - The `id` property is used, if available.



## `POST`, `PUT`, and `PATCH`

This endpoint and the methods it supports comply as closely as possible with the general REST specifications. 

POST | PUT | PATCH
------ | ------
Works on collections | Works on single entities | Works on single entities
Allow creation with autogenerated ID | Create or update for a provided ID <br/> <br/> If you don't provide an ID, this call fails. | Update for a provided ID
Entity must exist if ID is specified | Entity doesn't need to exist, it is created with the provided ID if doesn't exist | Entity must exist if ID is specified
Updates/creates entities for every call | Idempotent (multiple calls with same parameter STRICTLY equivalent to a single call) | Idempotent (multiple calls with same parameter STRICTLY equivalent to a single call)

Nested create/update are allowed | Nested create/update are allowed | Nested updates are allowed

> For more information on REST specification, see the following [IETF document.](https://tools.ietf.org/html/rfc7231).

Below you can find an overview of available calls and their results for the `PUT` and `POST` methods.


| URL       | Body content              | Entity already exists |  PUT result                         | POST result                                                   |
| --------- | ------------------------- | --------------------- | ----------------------------------- | ------------------------------------------------------------- |
| /Person   | { ... }                   | N.A.                  | 400: no ID specified                | 200: created entity with generated id                         |
| /Person   | { id: 1, ... }            | yes                   | 200: updated entity                 | 200: updated entity                                           |
| /Person   | { id: 1, ... }            | no                    | 200: created entity with id 1       | 400: cannot find entity with id 1                             |
| /Person   | [{ ... }, { id: 1, ... }] | yes                   | 400: expected single entity in body | 200: created entities without id and updated the ones with id |
| /Person   | [{ ... }, { id: 1, ... }] | no                    | 400: expected single entity in body | 400: cannot find entity with id 1                             |
| /Person/1 | { ... }                   | yes                   | 200: updated entity                 | 200: updated entity                                           |
| /Person/1 | { ... }                   | no                    | 200: created entity with id 1       | 404: cannot find entity with id 1                             |
| /Person/1 | { id: 1, ... }            | yes                   | 200: updated entity                 | 200: updated entity                                           |
| /Person/1 | { id: 1, ... }            | no                    | 200: created entity with id 1       | 404: cannot find entity with id 1                             |
| /Person/1 | [{ ... }, { id: 1, ... }] | yes                   | 400: expected single entity in body | 400: expected single entity in body                           |
| /Person/1 | [{ ... }, { id: 1, ... }] | no                    | 400: expected single entity in body | 400: expected single entity in body                           |


In this example, we assume you have a `com.braintribe.model.custom.demo.PersonModel#1.0` model with the following entities and their properties:

* `com.braintribe.model.custom.demo.Person`
  * string `id`
  * string `name`
  * list `<Person>` `friends`

Let's assume a `demo.PersonAccess` access uses the model above, is deployed properly, and has no data. Below, you can find an example call.

* Creating an instance with an autogenerated `id`

  To create a `Person` entity instance, call:

  * Method: `POST`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person?sessionId=xxx`
  * Headers:
    * `Accept`: `application/json`
    * `Content-Type`: `application/json`
    * `gm-projection`: `idInfo`
  * Body:

  ```json
  {
    "name": "John"
  }
  ```

  This call returns a JSON with the `id` of the newly created entity instance:

  ```json
  {
  "value": "1",
  "_type": "long"
  }
  ```

* Creating an instance with a provided `id`

  Let's create another person, and specify the `id` this time:

  * Method: `PUT`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person?sessionId=xxx`
  * Headers:
    * `Accept`: `application/json`
    * `Content-Type`: `application/json`
  * Body:

  ```json
  {
    "id": 2,
    "name": "Frank"
  }
  ```

  This call returns:

  ```json
  {
  "value": "2",
  "_type": "long"
  }

  ```
  
* Creating multiple instances

  Let's create many instances of `person`:

  * Method: `POST`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person?sessionId=xxx`
  * Headers:
    * `Accept`: `application/json`
    * `Content-Type`: `application/json`
    * `gm-list-entities-request`: `true`
  * Body:

  ```json
  [
    {
      "name": "John"
    },
    {
      "name": "Smith"
    }
  ]
  ```

  This call returns:

  ```json
  [
    {
        "value": "2",
        "_type": "long"
    },
    {
        "value": "3",
        "_type": "long"
    }
  ]
  ```

### Endpoint Configuration

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `referenceInfo`, `idInfo`, `locationInfo`, `envelope`, `data`, `success`
  Default Value | `success`
  Description | Return type for the call. <br/> <br/> `referenceInfo`: the `EntityReference` (or list of references, if the body contains a list, in the same order as the body) <br/> `idInfo`: the `id` (or list of `id`s, if the body contains a list, in the same order as the body) <br/>`locationInfo`: the URL of the created/updated entity (or entities, if the body contains a list, in the same order as the body) <br/> `envelope`: the entire `ManipulationResponse` entity <br/> `data`: the content of the created/updated entity (or entities, if the body contains a list, in the same order as the body), note that a second query is made with this projection once the entities have been updated <br/> `success`: the Boolean `true` is returned in the body

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
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `write-empty-properties` parameter.

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

  The `entity-recurrence-depth` parameter is applied after the `depth` parameter.
  

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
  
* List Entities Request

  Item | Description
  ----- | -----
  URL Parameter | `list-entities-request=<...>`
  Header Parameter | `gm-list-entities-request=<...>`
  Type | `Boolean`
  Default Value | `false`
  Description |  When this flag is set to `true`, multiple entities can be sent in the request payload.


## `DELETE`

This method allows to delete a single entity, or a list of entities based on a where clause. The functionality/features of this endpoint are the same at the [GET](rest_v2_rest_v2_entities.md#get) endpoint.

As for the `GET`, if the `id` (possibly, `partition` as well) is specified in the URL and the entity does not exist, a `404` is thrown. 
> Querying resources (entities) with `where.someObjectProperty` (e.g. `where.id`) will not work as `Object` for URL query parameters and headers is not allowed. For operations over a specific resource consider using the `/access/resource/{resourceId}` endpoint.

In this example, we assume you have a `com.braintribe.model.custom.demo.PersonModel#1.0` model with the following entities and their properties:

* `com.braintribe.model.custom.demo.Person`
  * string `id`
  * string `name`
  * list<Person> `friends`

Let's assume a `demo.PersonAccess` access uses the model above, and is deployed properly. Below, you can find an example `DELETE` call.

* Deleting by `id` 

  * Method: `DELETE`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person/1?sessionId=xxx`
  * Headers
    * `Accept`: `application/json`

  The call above deletes the Person entity instance with `id=1` and returns the number of deleted entities:

  ```json
  1
  ```
* Deleting using `where` statement

  * Method: `DELETE`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person?where.name=bob&sessionId=xxx`
  * Headers
    * `Accept`: `application/json`

  Or to delete all the `Person`s:

  * Method: `DELETE`
  * URL: `http://localhost:8080/tribefire-services/rest/v2/entities/demo.PersonAccess/Person?sessionId=xxx`
  * Headers
    * `Accept`: `application/json`

### Endpoint Configuration

* Delete Mode

  Item | Description
  ----- | -----
  URL Parameter | `delete-mode=<...>`
  Header Parameter | `gm-delete-mode=<...>`
  Type | `dropReferences`, `dropReferencesIfPossible`, `failIfReferenced`, `ignoreReferences`
  Default Value | `dropReferencesIfPossible`
  Description | Way of handling the references to the deleted entities <br/> <br/>  `dropReferences`: if the deleted entities are referenced, drop the reference, even if not allowed (e.g. `EntityA.entityb = EntityB` and `EntityA.entityb` is a mandatory property, delete `EntityB` will set `EntityA.entityb` to `null`) <br/> `dropReferencesIfPossible`: if the deleted entities is referenced, drop the reference if allowed, if not allowed, throw a `500`  <br/> `failIfReferenced`: fails if the delete entities is referenced anywhere <br/> `ignoreReferences`: delete the entities and ignore the references. This may still result in errors depending on the access' implementation, for example, if an SQL database backs an access and there are foreign keys between entities.

* Projection

  Item | Description
  ----- | -----
  URL Parameter | `projection=<...>`
  Header Parameter | `gm-projection=<...>`
  Type | `envelope`, `count`, `success`
  Default Value | `count`
  Description | Return type for the call. <br/> <br/>  `envelope`: returns the `ManipulationResponse` <br/> `count`: returns the number of deleted entities (always 1 if the ID of the entity is in the URL)<br/> `success`: returns the Boolean `true`


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
  Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `write-empty-properties` parameter.

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
  
* Allow Multiple Delete

  Item | Description
  ----- | -----
  URL Parameter | `allow-multiple-delete=<...>`
  Header Parameter | `gm-allow-multiple-delete=<...>`
  Type | `Boolean`
  Default Value | `false`
  Description |  When this flag is set to `true`, multiple (group or all) entities are allowed to be deleted. This parameter is used to prevent unintentional deletion of groups of entities.

## Try it Out

If you want to experiment with the API for yourself, use our integrated Swagger UI.

> For more information, see [Using Swagger UI](using_swagger_ui.md).
