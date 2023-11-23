# REST API - DDSA Service Evaluation

You can execute DDSA Service Requests via the `/api/v1` endpoint.

## Overview

Supported Method | Description
----- | -------
[`GET`](rest_v2_api_v1.md#get) | Used for simple DDSA service requests. All properties are encoded in the URL parameters or as custom (so starting with `gm-`) headers. The properties must be of type `String`, `int`, `float`, `long`, `double`, `boolean`, `Date`, `decimal`, `enum` or a list or set of the mentioned types.
[`POST`](rest_v2_api_v1.md#post-and-patch) | Used for more complex DDSA service requests. All types are supported. The service request can either be provided as form data or encoded in the body of the request, with the `mimeType` provided in the `Content-Type` header.
[`PATCH`](rest_v2_api_v1.md#post-and-patch)  | Used in the same manner as `POST`.

Every service request call is unmarshalled into the following entities:

* the actual call
* the endpoint, which contains multiple properties that drive the behavior of the endpoint, independently of the actual service requests you want execute.

The properties of the `ServiceRequest` may be prefixed with `service` and properties of the `Endpoint` entity may be prefixed with `endpoint`.

If no prefix is provided, we'll try to find the property in the service request first, then in the endpoint. If the property doesn't exist anywhere, we'll send a `400: Bad request` error with an error message.

> For more information, see [REST Parameters and Headers](rest_v2_introduction.md#rest-parameters-and-headers).

### Service Domain

[](asset://tribefire.cortex.documentation:includes-doc/service_domain.md?INCLUDE)

The `serviceDomain` part of the URL is necessary for every API call, as it specifies where the service is located, which is usually an access. You can, however, have a service domain which is not an access. 

You can create a new service domain in Control Center by looking up `ServiceDomain` using the **Quick Access** bar and creating a new instance.

Service domain is a parameter which must be specified in the path of every service request REST call. If you don't specify it, you will send your call to the default domain. 

#### Overriding Service Domain

You can override the service domain by passing a `serviceDomain` parameter in the URL of the call or by providing a `domainId` in the body of the call. This is useful when you want to run a service request from one domain on another one. The best example for this is the GMQL REST call. 

The GMQL REST call is a part of the default `serviceDomain:default` service domain. If you want to run a GMQL query on another domain (access), you must override the service domain by adding `serviceDomain=yourServiceDomain` to the URL or by adding `domainId="yourServiceAccess"` to the body, for example, calling `host:port/tribefire-services/api/v1/gmql?statement=from%20com.braintribe.model.generic.GenericEntity&domainId=myCustomAccess` will send the GMQL query `from com.braintribe.model.generic.GenericEntity` to the access with the external ID `myCustomAccess`.

#### Simple Name Resolution

If your service request has a simple name that is unique within a given service domain, then instead of using the `/api/v1/serviceDomain/foo.bar.ExampleServiceRequest` notation you can just use `/api/v1/serviceDomain/ExampleServiceRequest`. If the simple name is not unique and you try to use the `/api/v1/serviceDomain/ExampleServiceRequest` notation, you will get an exception.

## Authentication

[](asset://tribefire.cortex.documentation:includes-doc/rest_authentication.md?INCLUDE)

## Endpoint Configuration

You can configure the behavior of the REST endpoint by assigning desired values to available parameters:

* [Write Empty Properties](#write-empty-properties)
* [Stabilize Order](#stabilize-order)
* [Prettiness](#prettiness)
* [Projection](#projection)
* [Depth](#depth)
* [Entity Recurrence Depth](#entity-recurrence-depth)
* [Type Explicitness](#type-explicitness)
* [Identity Management Mode](#identity-management-mode)
* [Download Resource](#download-resource)
* [Save Locally](#save-locally)
* [Response Filename](#response-filename)
* [Response Content Type](#response-content-type)


### Write Empty Properties
Parameter | Description
--- | ---
URL Parameter | `write-empty-properties=<...>`
Header Parameter | `gm-write-empty-properties=<...>`
Type | Boolean
Default Value | `false`
Description | When this flag is set to `true`, the marshaller returns all properties that are set to `null` or are empty (for maps, sets and lists)

### Stabilize Order
Parameter | Description
--- | ---
URL Parameter | `stabilize-order=<...>`
Header Parameter | `gm-stabilize-order=<...>`
Type | Boolean
Default Value | `false`
Description | When this flag is set to `true`, the marshaller ensures that properties in different instances of the same entity are always in the same order. This is especially useful when you return empty properties using the `writeEmptyProperties` parameter.

### Prettiness
Parameter | Description
--- | ---
URL Parameter | `prettiness=<...>`
Header Parameter | `gm-prettiness=<...>`
Type | `none`, `low`, `mid` or `high`
Default Value | `mid`
Description |  This property represents the level of prettiness used when writing the assembly back to the body of the response. <br/> Each implementation of the marshaller may use this value slightly differently, but as a rule of thumb, `none` contains no new lines or indentation information and should only be used to minimise the size of the body and `high` provides the best possible indentation for humans to read.


### Projection
Parameter | Description
--- | ---
URL Parameter | `projection=<...>`
Header Parameter | `gm-projection=<...>`
Type | String
Default Value | `null`
Description | When the response to a call is a `GenericEntity` (i.e. in most of the cases), this value represents the path of properties to be returned from the response.

In this example, the following REST call: `/api/v1/serviceDomain/foo.bar.ExampleServiceRequest`
returns the following answer (encoded as JSON) without projection:

```json
{
  "_type": "foo.bar.ExampleServiceResponse",
  "exampleProperty": {
    "_type": "foo.bar.ExampleEntity",
    "anotherProperty": {
      "_type": "foo.bar.AnotherEntity",
      "stringValue": "Welcome to tribefire documentation!"
    }
  },
  "intValue": 42,
  "someList": [0, 1, 5]
}
```

The following are different values returned for various projections:

* `projection=intValue`
```json
42
```

* `projection=exampleProperty`
```json
{
   "_type": "foo.bar.ExampleEntity",
   "anotherProperty": {
      "_type": "foo.bar.AnotherEntity",
      "stringValue": "Welcome to tribefire documentation!"
   }
}
```

* `projection=exampleProperty.anotherProperty.stringValue`
```json
"Welcome to tribefire documentation!"
```

The following projections are **not** supported:
* `projection=_type`: the projection should be a list of property names separated by `.` and `_type` is not a property, but an internal value added by tribefire used when unmarshalling a JSON
* `projection=someList.0`: it is not possible to reference values inside of collections


### Depth
Parameter | Description
--- | ---
URL Parameter | `depth=<...>`
Header Parameter | `gm-depth=<...>`
Type | `shallow` - returns only the first level <br/> `reachable` - returns the whole assembly <br/> `number >= 0` - returns the provided level, starting at `0`
Default Value | `3`
Description |  For complex assemblies, this property specifies how deep the returned assembly should be traversed before being returned. This property is a simplified TraversingCriterion.

The `depth` parameter is applied after the `projection` parameter.

In this example, the following REST call: `/api/v1/serviceDomain/foo.bar.ExampleServiceRequest`
returns the following answer (encoded as JSON, with `writeEmptyProperties=true`) without the `projection` parameter set:

```json
{
  "_type": "foo.bar.ExampleServiceResponse",
  "a": {
    "_type": "foo.bar.EntityA",
    "b": {
      "_type": "foo.bar.EntityB",
      "c": {
        "_type": "foo.bar.EntityC",
        "d": {
          "_type": "foo.bar.EntityD",
          "e": "We need to go deeper."
        }
      }
    }
  }
}
```

The following are different values returned for various depths:

* `depth=shallow`: only returns the first level
* `depth=0`: only returns the first level

```json
{
  "_type": "foo.bar.ExampleServiceResponse",
  "a": null
}
```

* `depth=2`: returns the 3 first levels (zero-indexing)

```json
{
  "_type": "foo.bar.ExampleServiceResponse",
  "a": {
    "_type": "foo.bar.EntityA",
    "b": {
      "_type": "foo.bar.EntityB",
      "c": null
    }
  }
}
```

* `depth=reachable`: returns the entire assembly
* `depth=327`: any number bigger than the maximum depth returns the entire assembly

```json
{
  "_type": "foo.bar.ExampleServiceResponse",
  "a": {
    "_type": "foo.bar.EntityA",
    "b": {
      "_type": "foo.bar.EntityB",
      "c": {
        "_type": "foo.bar.EntityC",
        "d": {
          "_type": "foo.bar.EntityD",
          "e": "We need to go deeper."
        }
      }
    }
  }
```

* `depth=1&projection=a.b`: starting from `foo.bar.EntityB`, returns 2 levels (starting at 0...)

```json
{
  "_type": "foo.bar.EntityB",
  "c": {
    "_type": "foo.bar.EntityC",
    "d": null
  }
}
```

### Entity Recurrence Depth
Parameter | Description
--- | ---
URL Parameter | `entity-recurrence-depth=<...>`
Header Parameter | `gm-entity-recurrence-depth=<...>`
Type | `number >= -1` - returns the provided level, starting at `0`
Default Value | `0`
Description |  For complex entities which have recurrent entities (entities that appear more than once in the returned JSON), this property specifies how deep the returned recurrent entity should be traversed before being returned. This property is used to avoid `_id` and `_ref` in the returned JSON.

The `entity-recurrence-depth` parameter is applied after the `depth` parameter.

In this example, the following REST call: `/api/v1/serviceDomain/foo.bar.ExampleServiceRequest` without the `projection` parameter set returns the following answer (encoded as JSON, with `write-empty-properties=true`) :

```json
{
  "_type": "foo.bar.ExampleServiceResponse", "_id": "0",
  "a": {
    "_type": "foo.bar.EntityA", "_id": "1",
    "b": {
      "_type": "foo.bar.EntityB", "_id": "2",
      "name": "name_of_entity_B",
      "c": {
        "_type": "foo.bar.EntityC", "_id": "3",
        "d": {
          "_ref": "2"
        }
      }
    }
  }
}
```


Note there are different values returned for various settings of entity-recurrence-depth:

* `entity-recurrence-depth=0`: returns marshalled JSON with `_id` and `_ref`


```json
{
  "_type": "foo.bar.ExampleServiceResponse", "_id": "0",
  "a": {
    "_type": "foo.bar.EntityA", "_id": "1",
    "b": {
      "_type": "foo.bar.EntityB", "_id": "2",
      "name": "name_of_entity_B",
      "c": {
        "_type": "foo.bar.EntityC", "_id": "3",
        "d": {
          "_ref": "2"
        }
      }
    }
  }
}
```


* `entity-recurrence-depth=1`: only returns first level (scalar values) for the recurrent entity

```json
{
  "_type": "foo.bar.ExampleServiceResponse",
  "a": {
    "_type": "foo.bar.EntityA", "_id": "1",
    "b": {
      "_type": "foo.bar.EntityB", "_id": "2",
      "name": "name_of_entity_B",
      "c": {
        "_type": "foo.bar.EntityC", "_id": "3",
        "d": {
          "_type": "foo.bar.EntityB",
          "name": "name_of_entity_B"
        }
      }
    }
  }
}
```

* `entity-recurrence-depth=-1`: returns the entire recurrent entity (an entity that appears more than once in the returned JSON), if cyclic dependency is detected it will only return scalar values for that entity.

### Write Absence Information
Parameter | Description
--- | ---
URL Parameter | `write-absence-information=<...>`
Header Parameter | `gm-write-absence-information=<...>`
Type | Boolean
Default Value | `false`
Description | When this Boolean flag is set to `true`, the marshaller writes information for all absent properties.


### Type Explicitness
Parameter | Description
--- | ---
URL Parameter | `type-explicitness=<...>`
Header Parameter | `gm-type-explicitness=<...>`
Type | `auto`, `always`, `entities` or `polymorphic`
Default Value | `auto`
Description | This property is used to decide whether `_type` is present in marshalled JSON. <br/> <br/> This property is only available in JSON marshaller. <br/> <br/> **auto** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **always** - `_type` is always returned for every element <br> **entities** - `_type` is always returned for entity types and properties that are not known simple types (String, integer, etc). <br> **polymorphic** - `_type` is returned for every element if the actual type cannot be established from the context <br>


### Identity Management Mode
Parameter | Description
--- | ---
URL Parameter | `identity-management-mode=<...>`
Header Parameter | `gm-identity-management-mode=<...>`
Type | `off`, `auto`, `_id` or `id`
Default Value | `auto`
Description |  Represents how duplicates of objects should be unmarshalled. <br/> <br/> **auto** - Depending on the parsed assembly, the marshaller automatically detects the identification information and uses it for identity management.<br/> **off** - No identity management. <br/> **_id** - The internally generated `_id` information is used, if available.<br/> **id** - The `id` property is used, if available.

### Download Resource
Parameter | Description
--- | ---
URL Parameter | `download-resource=<...>`
Header Parameter | `gm-download-resource=<...>`
Type | Boolean
Default Value | `false`
Description |  When set to `true`, the result of the request is not the JSON representation of the Resource but the binary data itself. 


### Save Locally
Parameter | Description
--- | ---
URL Parameter | `save-locally=<...>`
Header Parameter | `gm-save-locally=<...>`
Type | Boolean
Default Value | `false`
Description |  When set to `true`, the Resource being handled by the request is not displayed in the browser (if the Resource is of type understood by the browser) but is downloaded (the `contentDisposition` header changes from `inline` to `attachment`). For more information about this header, see [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition).


### Response Filename
Parameter | Description
--- | ---
URL Parameter | `response-filename=<...>`
Header Parameter | `gm-response-filename=<...>`
Type | String
Default Value | n/a
Description |  Represents the `filename` of the file downloaded (changes the value of the `filename` property of the `contentDisposition` header). For more information about this header, see [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition).


### Response Content Type
Parameter | Description
--- | ---
URL Parameter | `response-content-type=<...>`
Header Parameter | `gm-response-content=type=<...>`
Type | MIME Type String
Default Value | n/a
Description |  Sets the MIME type of the content.

## `GET`

The GET service request call URL is formed according to the following pattern: 
`/api/v1/`*serviceDomain*/*type.signature.of.ServiceRequest*

You can use the URL and the header parameters to populate the `ServiceRequest` and the `Endpoint` entities.

The GET method does **not** support passing in a body, therefore all the properties of the `ServiceRequest` as well as `Endpoint` entities must be fully set from the URL and header parameters. You can work with all property types, but some of them will require using an alias first - see [Complex Request Properties](#complex-request-properties).

Examples:

Method: GET

URL: `http://localhost:8080/tribefire-services/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser?sessionId=session-id`
Headers:
* `Accept`: `application/json`
* `Content-Type`: `application/json`

Response:
  ```json
  {
    "_type": "com.braintribe.model.user.User",
    "id": "some-id",
    "firstName": "cortex",
    "lastName": "cortex"
  }
  ```

Here are several examples of the same call which returns the current user session from a `sessionId` but written in different forms:

* All parameters in URL, without prefix

  URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser?sessionId=xxx&prettiness=high`  
  Headers:
  * `Accept`: `application/json`

* All parameters in URL, with prefix

  URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser?service.sessionId=xxx&endpoint.prettiness=high`  
  Headers:
  * `Accept`: `application/json`

* All parameters in headers, without prefix

  URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser`
  Headers:
* `Accept`: `application/json`
* `gm-session-id`: `xxx`
* `gm-prettiness`: `high`

* All parameters in headers, with prefix:

  URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser`  
  Headers:
  * `Accept`: `application/json`
  * `gm-service.session-id`: `xxx`
  * `gm-endpoint.prettiness`: `high`

* Parameters mixed between headers and URL parameters, without prefix:

URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser?sessionId=xxx`  
Headers:

* `Accept`: `application/json`
* `gm-prettiness`: `high`

### Complex Request Properties
Above you learned about how to set simple request properties. However, if you want set values on complex property types in your request (such as entities, maps, collections of entities, properties of the `object` type), you will need to use an alias.

Let's imagine a GET request to find movies:

`FindMoviesRequest` which returns a response of Type `List<Movie>`.

Our request has the following properties (indention indicates properties set on other properties (sub-properties); the type of a property follows behind its name):

```yaml
- name (String)
- director (Entity Director)
 - name (String)
 - country (String)
 - awardsWon (Entity AwardsWon)
   - institution (Entity Institution)
     - name (String)
   - name (String)
- category (List<CategoryEnum>) (CategoryEnum's constants: Horror, Romance, Thriller, Comedy)
- additional (Map<String,String>)
  [Example:
    key: wonOscar
    value: true]

```

#### Example 1 - Simple Entities

As first example, let's try to find all movies of Austrian directors.

Let `director` property have the alias `d`. This is expressed using the following syntax: `director=@d`, which we will use to set the sub-properties of the `director` request property. In our example it's the country:

`@d.country=Austria`

Note that you couldn't just say `director.country=Austria`, because that would mean that director is expected to be an alias which must have been introduced by you first - as demonstrated in the previous paragraph.

On the right side of the equal sign, the same rules apply as if it would be a "normal" request property. In this example: Because the Director's `country` property is of type `String`, the property can be set directly, as demonstrated.

The final URL then looks as follows:

`findMovies?director=@d&d.country=Austria`

#### Example 2 - Deeply Nested Entities
Now, we will try to set the name of the award won by the director to `oscar`. In this situation, `name` is a property of `awardsWon`, and `awardsWon` is a property of `director`:

```yaml
- director (Entity Director)
 - name (String)
 - country (String)
 - awardsWon (Entity AwardsWon)
   - institution (Entity Institution)
     - name (String)
   - name (String)
```

First, we need to set an alias for `director`:

`director=@d`

Then, we need another alias for `awardsWon`:

`@d.awardsWon=@a`

Only now can we reach `name`:

`a.name=oscar`

Combining the above into a single request, we get:

`findMovies?director=@d&d.awardsWon=@a&a.name=oscar`

#### Example 3 - Collections
Let's try to set the movie category to `romance` and `horror` (who doesn't enjoy some sparkling vampires every now and then). 

Now, we can assign enum values in our request:

`category=horror&category=romance`

Combining the above into a single request, we get:

`findMovies?category=horror&category=romance`


#### Example 4 - Maps
The property `additional` has the type `map`. We use an alias `a` to add another entry to the map. Note that the alias does not refer to the map but to a map entry: `additional=@a`. 

We can set the key and value of the entry as follows:

`a.key=wonOscar&a.value=true`

The final request would then be:

`findMovies?additional=@a&a.key=wonOscar&a.value=true`


#### Example 5 - Sub-types of Entities
Thanks to the alias feature, you can assign values having a different type than the original property, provided it's a sub-type.

Consider a `person` property having the `com.braintribe.Person` type. Let's assume we want to assign a value of a different type, for example `com.braintribe.ExtendedPerson`. 

First, we need to assign a type signature to the alias:

`@p=com.braintribe.ExtendedPerson`

Without the above, you could only assign values of the original type of the person property (`com.braintribe.Person`)

Now, we can assign the alias to the property having the supertype `com.braintribe.Person`.

`person=@p`

> Note that order is important here. If we did the above operation first, we would already assign the type of Person (`com.braintribe.Person`) to the alias `@p`, which is not our goal.

Finally, we can assign the property itself:

`p.extendedName=Thomas`

As a result, the request looks as follows:

`requestName?@p=com.braintribe.ExtendedPerson&person=@p&p.extendedName=Thomas`

#### Example 6 - Object-type Properties
If necessary, it's possible to assign values to properties of the `object` type in your GET request. The below examples show the basic principles on how to do it:

##### Simple assignment

To assign a value (an integer in this case), use the below syntax:

`objectProperty=11`

The whole request would then be `requestName?objectProperty=11`.

Naturally, you can assign other types as well:

Data type | Syntax
---|---
integer | `objectProperty=11`
string | `objectProperty='11'` (in quotes due to type ambiguity)
long | `objectProperty=11L`
double | `objectProperty=11.234D`
float | `objectProperty=11.234F`
string | `objectProperty=Grzegorz` (no type ambiguity hence no quotes)
boolean | `objectProperty=true`
enum | `objectProperty=com.braintribe.ManyValuesEnum.VALUE_007`
 

##### Advanced example - assigning an entity:
Let's assume we want to assign a value to the `name` property of the `com.braintribe.Person` entity, which, in turn, will be assigned to a property of the type `object`.

First, we need to assign an alias to the `com.braintribe.Person` entity.

`@p=com.braintribe.Person`

Now, we can assign the entity to the object property using the alias:

`objectProperty=@p`

Finally, we can assign the `name` property itself:

`p.name=Joe`

As a result, the request looks as follows:

`requestName?@p=com.braintribe.Person&objectProperty=@p&p.name=Joe`


## `POST`, `PATCH`, and `PUT`

There are two ways of sending `POST`, `PATCH`, and `PUT` service request calls:

* [as `form-data` or `form-urlencoded`](#as-form-data)
* [as simple body](#as-simple-body)

### As Form Data

Tribefire supports special handling for requests that have the `multipart/form-data` or `application/x-www-form-urlencoded` format which are typically sent from native HTML browser forms. The `multipart/form-data` format is also the only way to send binary data (i.e. files) together with the REST request. This solution makes it easy to create HTML pages that can trigger a given service request.

#### Examples

* Simple request: authentication

```html
<form action="/tribefire-services/api/v1/authenticate">
  User: 
  <input type="text" name="user">
  Password:
  <input type="password" name="password">
  <input type="submit" value="Submit">
</form>
```

This will perform an authentication call and return the `userId` as a String and open it in the same tab in the browser.

* Complex request: updating the image of a user

Imagine a request `UpdateAvatarRequest` that calls the `UpdateAvatarProcessor` which updates the avatar image of a user. The request has two properties:

* `userName` of type String
* `image` of type `Resource`

```html
<form action="/tribefire-services/api/v1/updateAvatar">
  User: 
  <input type="text" name="userName">
  Image:
  <input type="file" name="image">
  <input type="submit" value="Submit">
</form>
```

On server side this will result in an `UpdateAvatarRequest` where the `userName` is set to the value of the input field and the resource at the `image` property contains the binary data of the file uploaded via the form element with the same name.

> Note that you can still send endpoint parameters via the form action URL:

```html
<form action="/tribefire-services/api/v1/updateAvatar?prettyness=high&depth=1">
  User: 
  <input type="text" name="userName">
  Image:
  <input type="file" name="image">
  <input type="submit" value="Submit">
</form>
```

### As Simple Body

You can also choose to submit your service request in the body of the REST call. The `ServiceRequest` is then unmarshalled from the body, in the `mimeType` specified in the `Content-Type` header.

Since only the `Endpoint` entity is specified in the URL/header parameter, there is no need for a prefix here, however you can use the `endpoint.` prefix for clarity if you want.

Since the type of the `ServiceRequest` may be contained in the body, it is not necessary to pass it in the URL. However, the following URLs are still valid:

* `/api/v1/serviceDomain/type.signature.of.ServiceRequest`: If there is also a type signature in the body, it is used instead of the type signature from the URL
* `/api/v1/serviceDomain/`: If there is no type signature in the body, a `400: Bad request`is thrown.

Examples: 

Method: POST

URL: `http://localhost:8080/tribefire-services/api/v1/cortex/com.braintribe.model.securityservice.GetCurrentUser`
Headers:
* `Accept`: `application/json`
* `Content-Type`: `application/json`
* `gm-session-id`: `someId`

Body:

```json
{
  "domainId": "cortex" 
}
```

Response:

```json
{
  "_type": "com.braintribe.model.user.User",
  "id": "some-id",
  "firstName": "cortex",
  "lastName": "cortex"
}
```

Here are several examples of the same call which returns the current user session from a `sessionId` but written in different forms:

* `ServiceRequest` type in URL, endpoint parameters in URL

URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser?prettiness=high&sessionId=someId`  
Headers:

* `Accept`: `application/json`
* `Content-Type`: `application/json`

Body:

```json
{
  
}
```

* `ServiceRequest` type in URL, endpoint parameters in headers

URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser?sessionId=someId`  
Headers:

* `Accept`: `application/json`
* `Content-Type`: `application/json`
* `gm-prettiness`: `high`

Body:

```json
{
  
}
```

* `ServiceRequest` type in body, endpoint parameters in URL

URL: `/api/v1/serviceDomain?prettiness=high&sessionId=someId`  
Headers:

* `Accept`: `application/json`
* `Content-Type`: `application/json`

Body:

```json
{
  "_type": "com.braintribe.model.securityservice.GetCurrentUser"
}
```

* `ServiceRequest` type in body, endpoint parameters in headers

URL: `/api/v1/serviceDomain/com.braintribe.model.securityservice.GetCurrentUser`  
Headers:

* `Accept`: `application/json`
* `Content-Type`: `application/json`
* `gm-prettiness`: `high`
* `gm-session-id`: `someId`

Body:

```json
{
  "_type": "com.braintribe.model.securityservice.GetCurrentUser"

}
```

## Mapping Customization

By default, any `foo.bar.ServiceRequest` in tribefire may be invoked via REST with the method `GET` or `POST` and URL `/api/v1/serviceDomain/foo.bar.ServiceRequest`. 

However, it is possible to configure custom mappings for any request via the singleton `com.braintribe.model.ddra.DdraConfiguration` entity in the cortex access.

For every parameter, you can define its default value. For example, `entityRecurrenceDepth` is the parameter supported on the REST endpoint and with `defaultEntityRecurrenceDepth` you can define the default (as the name suggests) for that parameter.

Much like the facade design pattern, customizing your mapping allows you to decouple the REST endpoint where a functionality is exposed from the service request that carries out the functionality. Consider the `/gmql` request for example. The functionality is exposed under the `/tribefire-services/api/v1/gmql` endpoint, but the actual service request is `com.braintribe.model.accessapi.GmqlRequest`. This way, if the request ever changes, the exposed endpoint will remain the same.

`DdraConfiguration` has a set of `com.braintribe.model.ddra.DdraMapping` that influence the behavior of a single mapping. `DdraMapping` has the following properties:

* `method`

  This property defines which HTTP method the service request is accessible with.

  Type | `com.braintribe.model.ddra.DdraUrlMethod` Enumeration
  Possible Values | `GET`, `POST`, `PUT`, `DELETE`, `GET_POST` (exposed as both GET and POST)
  Default Value | `GET_POST`
  Mandatory | no


* `path`

  This property defines under which URL the service request is accessible.

  Type | String
  Possible Values | any, but must start with `/`
  Mandatory | yes

  Supported features:
  * you can map different service requests under the same URL and different method (you can use this to simulate CRUD APIs to a certain extend)
  * paths with multiple `/` are supported

  Not supported functionality:
  * dynamic or conditional paths are **not supported**, for example `/my-entity/<id>` where ID would be filled in from the URL
  * paths with variable length are **not supported**, for example `/my-path/*` where any path starting with `/my-path` would match


  Path Value | Accessible Under
  --- | ---
  `path=/my-request` | `tribefire-host/tribefire-services/api/v1/serviceDomain/my-request`
  `path=/my-services/service1` | `tribefire-host/tribefire-services/api/v1/serviceDomain/my-services/service1`
  `path=/my-services/service2` | `tribefire-host/tribefire-services/api/v1/serviceDomain/my-services/service2`
  `method=GET path=/my-crud-api`| `GET:tribefire-host/tribefire-services/api/v1/serviceDomain/my-crud-api`
  `method=POST path=/my-crud-api` | `POST:tribefire-host/tribefire-services/api/v1/serviceDomain/my-crud-api`
  `method=PUT path=/my-crud-api`| `PUT:tribefire-host/tribefire-services/api/v1/serviceDomain/my-crud-api`
  `method=DELETE path=/my-crud-api`| `DELETE:tribefire-host/tribefire-services/api/v1/serviceDomain/my-crud-api`

* `requestType`

  This value contains the `GmEntityType` of the request that should be created and executed.

  Parameter | Description
  --- | ---
  Type | `GmEntityType`, must correspond to a subtype of `ServiceRequest`
  Mandatory | No

* `transformRequest`

  This value contains an instance of `ServiceRequest` (as opposed to a type in `requestType`) that should be evaluated for this path. Note that this instance is **not** filled from the URL parameters, headers or the body, it is executed as is.

  If the `transformRequest` is an instance of `com.braintribe.model.service.api.HasServiceRequest` and `requestType` is defined, then an instance of type `requestType` is created from the parameters or body and assigned to the `serviceRequest` property.

  The idea here is to be able apply preprocessing or postprocessing to multiple REST endpoints and to decouple it from the endpoint implementation.

  Parameter | Description
  --- | ---
  Type | `ServiceRequest`
  Mandatory | no

* `defaultProjection`

  This value contains the default projection to use for this REST call. 

  Parameter | Description
  --- | ---
  Type | String
  Mandatory | no

 For more information on projection, see [Projection](rest_v2_api_v1.md#projection).
  
* `defaultDepth`

  A simplified TraversingCriterion. For complex assemblies, it dictates how deep the returned assembly should be.

  Parameter | Description
  --- | ---
  Type | `com.braintribe.model.processing.ddra.endpoints.model.DdraEndpointDepthKind` Enumeration
  Possible Values | `shallow`, `reachable`, `custom` (number >= 0).
  Default Value | `0`
  Mandatory | no
  
* `defaultEntityRecurrenceDepth`

  A integer value. For complex recurrence of entities, it dictates how deep returned recurrence assembly should be. In case of `0` it will use **_id** and **_ref** in result. If `-1` is used it will traverse recurrence tree indefinitely.

  Parameter | Description
  --- | ---
  Type | Integer
  Possible Values | `number >= -1`
  Default Value | `0`
  Mandatory | no
  
* `defaultMimeType`
  Identifies default content format used for operations against content transmitted.

  Parameter | Description
  --- | ---
  Type | String
  Possible Values | `application/xml`, `application/json`, `application/gm`, `text/xml`, `text/x-json`, `gm/xml`, `gm/bin`, `gm/json`, `gm/jseh`, `gm/jse`, `gm/man`, `gm/deprecated-json`
  Default Value | `application/json`
  Mandatory | no
  
* `defaultPrettiness`

  Represents the level of prettiness to be used when writing the assembly back to the body of the response.

  Parameter | Description
  --- | ---
  Type | `com.braintribe.codec.marshaller.api.OutputPrettiness` Enumeration
  Possible Values | `none`, `low`, `mid`, `high`
  Default Value | `mid`
  Mandatory | no
  
* `defaultServiceDomain`
  Identifies **service domain** to be used by default for service requests.

  Parameter | Description
  --- | ---
  Type | String
  Possible Values | `any`
  Mandatory | no
  
* `defaultStabilizeOrder`

  When this Boolean flag is set to *true*, the marshaller ensures that properties in different instances of the same entity are always in the same order. Especially useful when `writeEmptyProperties=true`.

  Parameter | Description
  --- | ---
  Type | Boolean
  Possible Values | `true`, `false`
  Default Value | `false`
  Mandatory | no
  
* `defaultTypeExplicitness`

  Represents how type of objects should be marshalled.

  Parameter | Description
  --- | ---
  Type | `com.braintribe.codec.marshaller.api.TypeExplicitness` Enumeration
  Possible Values | `auto` - The marshaller decides which of the other options it will choose. <br/> `always` - The types are made explicit to allow to preserve the correct type under all circumstances which means not to rely on any contextual information to auto convert it from another type. <br/> `entities` -  The types are made explicit for entities in all cases and the other values can get simpler types if appropriate and the context of the value can give the information to reestablish the correct type with an auto conversion. <br/> `polymorphic` - he types are made explicit if the actual type cannot be reestablished from the context of the value which is the case that value is a concretization of the type given by the context.
  Default Value | `auto`
  Mandatory | no
  
* `defaultWriteAbsenceInformation`

  When this Boolean flag is set to **true**, the marshaller writes information for all absent properties.

  Parameter | Description
  --- | ---
  Type | Boolean
  Possible Values | `true`, `false`
  Default Value | `false`
  
* `defaultWriteEmptyProperties`

  When this Boolean flag is set to **true**, the marshaller writes all properties that are set to **null** or are empty (for maps, sets and lists).

  Parameter | Description
  --- | ---
  Type | Boolean
  Possible Values | `true`, `false`
  Default Value | `false`
  
* `requestPrototyping`

  You can use this property as an alternative to a [serialized-request](using_swagger_ui.md#serialized-request). Instead of setting the serialized-request field manually (e.g. copy-pasting JSON directly into the request in Swagger), you can create a request prototype instead, and then map it to your request via `DdraMapping`. Note that this could be very useful to set default properties for the request. If found, the prototype will be used instead of the `serialized-request`.
  
  When prompted, use one of the below prototyping requests:

    * QueryPrototyping - queries for the prototype in an access. This prototype is then used when you execute your request. You need to identify the target prototype:

      Parameter | Description
      --- | ---
      accessId | `access_name` where the prototype is located (you can find it in the URL after opening the access after `?accessId=`, for example `access.demo` in `?accessId=access.demo#default`).
      prototypeGlobalId | `GlobalId` of the target prototype.

    * ResourcePrototyping - uses the attached resource as a prototype. In this case, you only need to attach the `JSON` file:

      Parameter | Description
      --- | ---
      prototypeResource | Attach your prototype here (a `JSON` file).

    * StaticPrototyping - similar to QueryPrototyping, but the prototype must be found in the **Cortex** model.

      Parameter | Description
      --- | ---
      prototype | Assign the prototype from Cortex here.

### Configuration in Control Center

The `com.braintribe.model.ddra.DdraConfiguration` may be queried and updated in Control Centre. You can find it using the **Quick Access** box.

The mappings are effective as soon as the changes are committed, therefore it is possible to update the configuration without restarting tribefire.

### Configuration via Java

Since the configuration for the API is an entity in the `cortex` access, it is possible to edit it the exact same way as for entities in other accesses. 

In this example, we are programmatically mapping the simple authenticate call to `/api/v1/serviceDomain/login?user=xxx&password=xxx`. Consult the `EditDdraConfigurationExample` class below:

```java
import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

public class EditDdraConfigurationExample {

	public static void main(String[] args) throws GmSessionFactoryBuilderException {

		// get the session factory for a local instance of Tribefire
		PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("http://localhost:8080/tribefire-services").authentication("cortex", "cortex").done();

		// open a session to the cortex access
		PersistenceGmSession session = sessionFactory.newSession("cortex");

		// get the GmEntityType for the simple authentication service request
		GmEntityType simpleAuth = session.query().entity(GmEntityType.T, "type:com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword").find();

		// create a DdraMapping
		DdraMapping mapping = session.create(DdraMapping.T);
		// no need to set this, as this is the default value
		// mapping.setMethod(DdraUrlMethod.GET_POST);
		mapping.setPath("/login");
		mapping.setRequestType(simpleAuth);

		// by default, the OpenUserSessionWithUserAndPassword returns an OpenUserSessionResponse
		// this entity contains a UserSession entity (under the property userSession), which itself contains a sessionId, let's just return the sessionId here
		mapping.setDefaultProjection("userSession.sessionId");

		// get the DdraConfiguration instance
		DdraConfiguration ddraConfiguration = session.query().entity(DdraConfiguration.T, "singleton").find();

		// add the mapping
		ddraConfiguration.getMappings().add(mapping);

		// commit
		session.commit();
	}

}
```

If you create a similar class and configure the `DdraMapping` object as specified above, you can authenticate in tribefire with a `GET` or `POST` call to `/api/v1/serviceDomain/login`.

## Embedded Properties

It's worth knowing that if the request has any embedded properties, they are also shown in Swagger. For details on how embedding works, see [Embedded metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/embedded.md).

If you want embedded properties to show up only in Swagger, add the `ddra` use-case selector to `embedded` metadata.

## Requesting Nested Properties
`/api/v1` endpoints support accessing nested REST properties via an alias, meaning that you can execute requests even on complex property structures. Aliases can be assigned to entities, maps, lists, and even properties of the `Object` type. Consider the below data that we want to query with a GET request to find a movie:

```yaml
FindMoviesRequest -> List<Movie>

- name
- director
 - name
 - country
 - awardsWon
   - institution
     - name
   - name
- category List<Enum> (Horror, Romance, Thriller, Comedy)
- additional
  (Map)
  Example:
    key: wonOscar
    value: true
```

Normally it wouldn't be possible to refer to nested data, such as `director` properties, and their properties. This problem was solved by adding the possibility to assign an alias to a property.

Let `director` have the alias `d`. This is expressed using the following syntax: `director=@d`, resulting in the possiblility to refer to properties of `director`:

`@d.country=Austria`

> Note that you couldn't just say `director.country=Austria`, because then `director` is expected to be an alias.

Similarly, let the property `additional` of the type **map** have the alias `a`. This is expressed as `additional=@a`, resulting in the possibility to refer to keys and values:

`a.key=wonOscar&a.value=true`

An advanced GET query using an alias could then look as follows:

`findMovies?director=@d&d.country=Austria&category=Horror&category=Romance&additional=@a&a.key=wonOscar&a.value=true`

A real-life use-case for this feature could be sending a POST request disguised as GET, when using a third-party tool that only supports GET. In that case, we can decide to offer a request that semantically should be a POST (i.e. it creates new entries in the database) as a GET request in tribefire. This is not the recommended solution but sometimes it could be necessary to use it.
For example, we could add an extra GET mapping for `model/create` because we need to create a model via a simple third-party tool that does not support POST.

## Try it Out

If you want to experiment with the API for yourself, use our integrated Swagger UI. 

> For more information, see [Using Swagger UI](using_swagger_ui.md).
