# Legacy REST API Introduction

Each REST call contains different parameters, depending on the call. Some parameters, however, are common for some calls.

[](asset://tribefire.cortex.documentation:includes-doc/rest_old_tip.md?INCLUDE)

If you want, you can call the REST API v1 using the `/rest/v1` endpoint instead of `/rest/`. This may help you quickly figure out which version of the REST API you are using.

## Common REST Parameters

Parameter    | Description  
------- | -----------
`accessId`  | The name of the access through which a model and its data can be accessed. The value provided should match the value defined in **External Id** of the required access.
`sessionId` | The session ID required for all REST requests. All REST calls require the use of the `sessionId` parameter, apart from the authenticate call, where you receive a valid `sessionId` in return.  
`pseudoContentType`  | Used to force a browser to handle known MIME types (`text/plain`, and so on).  

## Dynamic Parameters

Dynamic parameters are used to set property values of a given entity (a hypothetical entity type `Person` has a property called `age` and you want to change its value), as opposed to configure the REST call itself (like the `accessId` parameter, for example).

If you need to pass an existing dynamic parameter during a CRUD call, you simply enter the name of the parameter preceded by an asterisk and assign it a value as part of the REST call: `*VariableName=VariableValue`

## Depth

Rather than returning all information at once and potentially causing costly queries, information is returned according to traversing criteria. When using REST, absent information is shown as null. However, this does not always mean that the value is actually null, rather that some information is absent.

> For more information, see [traversing criteria](asset://tribefire.cortex.documentation:concepts-doc/features/traversing_criteria.md).

This is the case, for example, when you do a default, shallow search on an entity. All properties are returned, but only with top level information. This means that any complex properties are returned as null.

The `depth` parameter is important when returning information because it determines how much information is returned. There are three accepted values for this parameter.

Parameter    | Description  
------- | -----------
`shallow` | Returns only information for simple properties. This is the default value.  
`reachable`  | Returns all reachable information, including complex entity and collection data
`0-n`  | Traverses to the *n* level that returned everything until *n*.  

### Shallow

By default the `depth` parameter automatically returns shallow information, meaning that only simple property data is returned. If the object which is returned has complex information, it is displayed as null. However, if you use the parameter `depth=reachable` then this complex information is also returned.

The example below uses the `PropertyFetch` call to return the value of property `picture` belonging to the `Person` entity instance with the `id` of 21:

```
GET
http://localhost:8080/tribefire-services/rest/property-fetch?sessionId=17060109115466337f4dab6b084143a8&accessId=access.demo&type=tribefire.demo.model.data.Person&property=picture&id=21
```

```json
{
  "_type": "com.braintribe.model.resource.SimpleIcon",
  "_id": "0",
  "id": {
    "value": "25",
    "_type": "long"
  },
  "name": "johndoe Icon",
  "partition": "access.demo"
}
```

Because no `depth` parameter is used, it automatically defaults to shallow.

### Reachable

However, when you run the same call with the `depth=reachable` parameter, the response changes:

```
GET
http://localhost:8080/tribefire-services/rest/property-fetch?sessionId=17060109115466337f4dab6b084143a8&accessId=access.demo&type=tribefire.demo.model.data.Person&property=picture&id=21&depth=reachable
```

```json
{
  "_type": "com.braintribe.model.resource.SimpleIcon",
  "_id": "0",
  "id": {
    "value": "25",
    "_type": "long"
  },
  "image": {
    "_type": "com.braintribe.model.resource.Resource",
    "_id": "1",
    "id": {
      "value": "23",
      "_type": "long"
    },
    "name": "johndoe.gif",
    "partition": "access.demo",
    "resourceSource": {
      "_type": "com.braintribe.model.resource.source.StaticSource",
      "_id": "2",
      "id": {
        "value": "24",
        "_type": "long"
      },
      "partition": "access.demo",
      "resolverURI": "images/johndoe.gif"
    }
  },
  "name": "johndoe Icon",
  "partition": "access.demo"
}
```

### 0-n

You can also use the `depth` parameter to control the specific level that tribefire should drill-down to and display complex information. The number entered, therefore, represents the amount of levels that are traversed. Of course, `depth=0` returns the same level as `shallow`.

```
GET
http://localhost:8080/tribefire-services/rest/entity?sessionId=17060109115466337f4dab6b084143a8&accessId=access.demo&type=tribefire.demo.model.data.Person&id=21&depth=1
```

```json
{
  "_type": "tribefire.demo.model.data.Person",
  "_id": "0",
  "address": {
    "_type": "tribefire.demo.model.data.Address",
    "_id": "1",
    "city": "Vienna",
    "country": "Austria",
    "id": {
      "value": "22",
      "_type": "long"
    },
    "partition": "access.demo",
    "postalCode": "1070",
    "street": "Kandlgasse",
    "streetNumber": 3
  },
  "anything": 1,
  "children": [
    {
      "_type": "tribefire.demo.model.data.Person",
      "_id": "2",
      "firstName": "J.J.",
      "gender": {
        "value": "male",
        "_type": "tribefire.demo.model.data.Gender"
      },
      "id": {
        "value": "41",
        "_type": "long"
      },
      "lastName": "Doe",
      "partition": "access.demo",
      "ssn": "555"
    },
    {
      "_type": "tribefire.demo.model.data.Person",
      "_id": "3",
      "firstName": "Mary",
      "gender": {
        "value": "female",
        "_type": "tribefire.demo.model.data.Gender"
      },
      "id": {
        "value": "46",
        "_type": "long"
      },
      "lastName": "Doe",
      "partition": "access.demo",
      "ssn": "666"
    }
  ],
  "father": {
    "_type": "tribefire.demo.model.data.Person",
    "_id": "4",
    "firstName": "James",
    "gender": {
      "value": "male",
      "_type": "tribefire.demo.model.data.Gender"
    {...}
  },
  "id": {
    "value": "21",
    "_type": "long"
  },
{...}
  "partition": "access.demo",
  "picture": {
    "_type": "com.braintribe.model.resource.SimpleIcon",
    "_id": "6",
    "id": {
      "value": "25",
      "_type": "long"
    },
    "name": "johndoe Icon",
    "partition": "access.demo"
  },
  "ssn": "111"
}
```

## Projection

A projection determines the information that is returned. You can use projection to determine whether you want the actual data to be returned (so the payload) or the metadata of returned data. For example, executing a query returns a dataset of results, but you can use projection to return only the first instance in the dataset, or the `QueryResult` object, the instance which is responsible for handing the returned results.

Parameter    | Description  
------- | -----------
`envelope` | Returns the metadata of the returned data.  
`payload`  | Returns actual data.

This example returns all instances of the entity `Person`: `http://localhost:8080/tribefire-services/rest/query?sessionId=17060109115466337f4dab6b084143a8&accessId=access.demo&statement=from%20tribefire.demo.model.data.Person`
But if we use the `envelope` projection:

```
GET
http://localhost:8080/tribefire-services/rest/query?sessionId=17060109115466337f4dab6b084143a8&accessId=access.demo&statement=from%20tribefire.demo.model.data.Person&projection=envelope
```

```json
{
  "_type": "com.braintribe.model.query.EntityQueryResult",
  "_id": "0",
  "hasMore": false
}
```

> You can use projections in conjunction with the `depth` parameter. For example, the `EntityQueryResult` above, returns only information for its simple properties.

## Codec

The general parameter `codec` is used to handle the object type of information returned. There are two main object return types:

* JSON (default)
* XML

You can add the `codec=gm/xml` parameter to your  REST call to return the results as an `.xml` file.
