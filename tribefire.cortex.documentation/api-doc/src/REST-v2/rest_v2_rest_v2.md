# REST API CRUD Services

Create, read, update, delete (CRUD) services are provides by the `/rest/v2/` endpoints. They allow you to perform operations on entities and properties inside accesses.

## Overview

Basic create/read/update/delete (CRUD) operations on entity instances and their properties stored in deployed accesses are provided by `/rest/v2/`. This API is divided into 2 endpoints: `/rest/v2/entities/` and `/rest/v2/properties/`, respectively grouping requests related to entities and entity properties.

Below is an overview of all the services that `/rest/v2/` API offers:

Endpoint and URL  | GET   | POST | PUT  | DELETE | PATCH 
---- | ---- | ---- | ---- | ---- | ---- 
[`/rest/v2/entities/`](rest_v2_rest_v2_entities.md)*{accessId}*`/`*{typeSignature}* | Entity search | Entity creation | Entity update or creation | Entity deletion | Entity update
[`/rest/v2/entities/`](rest_v2_rest_v2_entities.md)*{accessId}*`/`*{typeSignature}*`/`*{id}*`/`(*{partition}*) | Entity return | Entity update  | Entity update or creation  | Entity deletion | Entity update
[`/rest/v2/properties/`](rest_v2_rest_v2_properties.md)*{accessId}*`/`*{typeSignature}*`/`*{id}*`/`(*{partition}*)`/`*{property}* | Property value return   | Addition or removal collection elements <br/> <br/>  Only works on collection properties | Property value update | Property value reset to default <br/> <br/> int, long, double, float, BigDecimal = `0` <br/> Boolean = `false` <br/> enums, entities, objects = `null` <br/> maps, lists, sets = empty collection | Property value update

> For each REST API call to `/rest/v2`, the `entity.TypeSignature` part may be replaced by the simple name (for example `User` instead of `com.braintribe.model.user.User`), but only when this simple name is unique in a given access (there are no other entities with the same simple name).

> For more information and specific examples, see [REST v2 /rest/v2/entities](rest_v2_rest_v2_entities.md) and [REST v2 /rest/v2/properties](rest_v2_rest_v2_properties.md).

## Authentication

[](asset://tribefire.cortex.documentation:includes-doc/rest_authentication.md?INCLUDE)


## Try it Out

If you want to experiment with the API for yourself, use our integrated Swagger UI. 

> For more information, see [Using Swagger UI](using_swagger_ui.md).