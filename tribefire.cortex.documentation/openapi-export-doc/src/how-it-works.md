# OpenAPI Export in Detail

This document explains in detail how your tribefire models are processed to generate the OpenAPI document.

The OpenAPI document is structured in operations which represent a certain REST URL path for a certain HTTP method. Each operation has its request parameters and possible responses.

## Services
A service request can be called via REST in 2 ways. Either via a explicitly created DdraMapping which is configured in the cortex access, which results in a nice and short URL and lets you specify supported HTTP methods, or in a generic way by which every service request on a service model may be addressed without any additional configuration necessary.

The exported OpenAPI document features both variations and thus iterates first through the DdraMappings and then through all ServiceRequests of a service model to create the operations. Then the properties of each request are analyzed to determine its type, default values, etc. The return type of a service request is analyzed in a similar fashion.

Descriptions of requests and request parameters are retrieved via metadata on the request entities and properties. Via metadata you can also hide certain requests or parameters as well as influence their order.

Also the display of generic endpoint parameters can be configured in the same way, because behind the scenes they are just properties of an entity in a model.

For more information on how to configure the exported requests via metadata please continue reading the document about [OpenAPI Document Configuration](metadata.md)

## CRUD
As the CRUD operations are very clearly defined, their export is straightfoward. For every entity-type in the access' data model there are 2 times 5 (=10) operations

Entity CRUD operations can optionally take the ID of the entity that should be operated on in its path. That's where the *2 times* comes from. Further there are 5 HTTP methods supported

* `DELETE`
* `GET`
* `PATCH`
* `POST`
* `PUT`

Every one of these operations has its own query parameters and thus varies a bit from the others. Property level CRUD operations work in a similar fashion.

For more information about entity- and property-level CRUD, please visit the respective documentation:

* [CRUD on entity level](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_rest_v2_entities.md)
* [CRUD on property level](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_rest_v2_properties.md)
