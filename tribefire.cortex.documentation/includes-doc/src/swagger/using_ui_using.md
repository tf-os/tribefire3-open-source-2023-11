## Using Swagger UI

When you decide on what to display, the Swagger UI displays the following:

* a [list of items](#list-of-items) of your interest based on the URL you used
* the [**Models**](#models-section) section
* the [Swagger JSON](#json-file-export) file link

### List of Items

The list contains REST API endpoints displayed in alphabetical order. The items may be entities, properties, service requests, or accesses, depending on what parameters you passed in the URL. You may notice that you see the HTTP method and a quick description of the call directly in the list. 

If you don't see all HTTP operations for a particular resource, it may be that there is metadata attached that prohibit certain operations:

Assigned Metadata | Disabled HTTP Method
-------- | -------
NonInstantiable | `POST`
UnModifiable | `PUT`
NonDeletable | `DELETE`

> For more information, see [Instantiable and NonInstantiable](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/instantiable.md) [Modifiable and Unmodifiable](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/modifiable.md), and [Deletable and NonDeletable](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/deletable.md)

Clicking an entry in the list expands its details. In the details view, you can see every parameter of the respective API call with a detailed description on what the parameter influences. 

The description of call parameters is taken from the resource's metamodel. The content you provide as the value of the `description` attribute of an entity, property, or a service is the one displayed in the corresponding row of the Swagger UI. If you want the descriptions to be more meaningful, the easiest way is to add/change the `description` parameter in Control Center.

#### Serialized Request
All properties should be described in Swagger, but `serialized-request` deserves a separate mention. You can use it to provide a JSON assembly directly to your POST request, instead of filling up all the fields in Swagger separately:

```JSON
{
    "firstName": "Grzegorz",
    "lastName": "GDPR_was_here"
}
```

It's important to know that if you need to provide these assemblies on a regular basis in a given request, it may be worth it to create a prototype instead, and map it to that request. For details, see `requestPrototyping` in [Mapping Customization](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_api_v1.md#mapping-customization).
