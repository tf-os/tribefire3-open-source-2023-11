# OpenAPI Document Configuration

If required, you can influence how a request is displayed in the exported OpenAPI document. Especially you can

* **Hide** a request, a request parameter or a response parameter. You can even hide a whole model to indicate that it doesn't make sense to export an OpenAPI document for it.
* Influence the **order**, requests, request properties or response properties are displayed
* Set a **description** for a request, a request parameter, a response or a response parameter. You can also set a description for the whole model which will be used as description for the exported OpenAPI document.
* Set a **title** for a request
* Specify whether request properties are **mandatory**

## Metadata

### Supported types
<!--TODO test and add examples-->
#### Hide
Use the `com.braintribe.model.meta.data.prompt.Hidden` metadata, or its counterpart `com.braintribe.model.meta.data.prompt.Visible`.

For information on how to use this metadata, see documentation for [Visible and Hidden metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/visible.md).

* **On a model** this means that no OpenAPI document will be generated for that model but an Exception will be thrown instead. Also no `API` link will be visible for that access or service domain on the tribefire landing page.
* **On a request entity type** this means that the request won't be reflected in the OpenAPI document
* **On a property** this means that the property won't be reflected in schemas or parameters.

#### Order
Use the `com.braintribe.model.meta.data.prompt.Priority` metadata.

For information on how to use this metadata, see documentation for [Priority metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/priority.md).

* **On a request entity type** this influences the order the request or its mapping shows up in the OpenAPI document
* **On a property** this influences the order the property shows up in the OpenAPI document as parameter or in schemas.

#### Description
Use the `com.braintribe.model.meta.data.prompt.Description` metadata.

For information on how to use this metadata, see documentation for [Description metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/description.md).

* **On a model** this influences the description of the OpenAPI document which also shows up in the top of the OpenAPI UI.
* **On a request entity type** this influences the request description
* **On a property** this influences the description of this property as parameter or in schemas

#### Name
Use the `com.braintribe.model.meta.data.prompt.Name` metadata.

For information on how to use this metadata, see documentation for [Name metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/name.md).

* **On a model** this influences the title of the OpenAPI document which also shows up in the top of the OpenAPI UI.
* **On a request entity type** this influences the request summary string

#### Mandatory
Use the `com.braintribe.model.meta.data.constraint.Mandatory` metadata or its counterpart `com.braintribe.model.meta.data.constraint.Optional`.

For information on how to use this metadata, see documentation for [Mandatory metadata](asset://tribefire.cortex.documentation:concepts-doc/metadata/constraint/mandatory.md).

* **On a property** this makes this property mandatory from OpenAPI perspective as parameter or in schemas

### Use Cases

All this meta data is not OpenAPI specific. So if you for example add a `Hidden` metadata to a property it will also vanish everywhere else where the `Hidden` metadata is respected - like for example in the Control Center. So in most cases it makes sense to add a so called `UseCaseSelector` to the metadata instance when adding it to a property, entity type, enum type or model. This selector expects a string which acts as a key specifying its scope. So for example when you add a `UseCaseSelector` with the usecase `openapi` to your `Hidden` metadata it will only be respected when the resolver (in our case roughly the OpenAPI export module) specifically enables this use case. The Control Center would simply ignore this metadata because it does not support the `openapi` usecase and would still display the property.

Read more about that in the documentation about Metadata in general.

#### Generally Supported Use Cases

The following use cases are always respected when exporting openapi documents
* `openapi`
* `ddra`

#### Adding Support for Custom Use Cases

Additionally you can add further use cases as a query parameter in the URL of the respective OpenAPI endpoint which will then be respected upon metadata resolving like in the following example

`<tribefire-services-url>/openapi/ui/services/cortex?useCases=openapi:simple&useCases=my-custom:usecase`.

#### `openapi:simple`
The module initializer of the openapi export module already sets a lot of metadata with the `openapi:simple` use case. Mostly it is used for `Hidden` metadata for rarely used request properties. When creating plain OpenAPI documents this use case is not respected by default but can be enabled by adding the `?useCases=openapi:simple` parameter to the endpoint URL like any custom use case.

Also you will notice a small drop down menu in the upper left corner of the OpenAPI UI that switches on the simple mode. In fact of course this is just a link to the current endpoint with this url parameter appended. Finally if you arrive in the OpenAPI endpoint from the `API` link on tribefire landing page this will also have this URL parameter and thus the simple mode enabled.

#### Mapping-Specific Use Cases

Sometimes you would like to change description, visibility or order of a request parameter for a certain OpenAPI operation only. Imagine you have an request type `CreateDocument` that is mapped via DDRA mapping to two different paths `/document/create` and `document/plain`. While the `/document/create` endpoint should allow several configuration options like adding some tags, the `document/plain` endpoint should look very simple and allow you to just supply a name for the document. Behind the scenes in both cases you want to use the same service request which should be handled by the same service processor to avoid code duplication for these very similar operations but you want to provide two different views from OpenAPI side.

This is possible by adding a property metadata with a mapping-specific use case. In your case you would create a UseCaseSelector with the usecase `openapi:/document/plain` and assign it to a `Hidden` metadata which again you would assign to all properties that you would specifically want to hide for this certain mapping.

#### Mime-Type-Specific Use Cases

Our OpenAPI processors support three different mime types for request bodies:
* application/json
* multipart/form-data
* application/x-www-form-urlencoded

In some cases you might want to configure the generated openapi schema just for a specific mime type. If you for example would like to hide a request property only in the multipart case you could add a `Hidden` metadata with the usecase `openapi:multipart/form-data`. The mime type *application/x-www-form-urlencoded* can also be used to specifically configure query parameters.

## Generic Endpoint Properties
Apart from the specific properties of your request you will notice that there are also properties prefixed with `endpoint.`. These are generic properties available for every request of the OpenAPI document which usually influence the request evaluation and response representation in a generic way like for example the `endpoint.prettiness` parameter that influences if the response entity (for example a JSON) should be marshalled in a pretty, human-readable way or a more compact and lightweight way.

Here you can find a list of supported parameters
* [for DDRA](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_api_v1.html#endpoint-configuration)
* [for entity CRUD](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_rest_v2_entities.html)
* [for property CRUD](asset://tribefire.cortex.documentation:/api-doc/REST-v2/rest_v2_rest_v2_properties.html)

Because these properties are modeled as well as an entity type in a tribefire model (namely the `DdraEndpoint` entity in the `tribefire.cortex:ddra-endpoints-model` as well as its subtypes), you can influence the description, visibility and order of these properties in the same way as above by attaching metadata to the respective properties.

For example if you decided that the `endpoint.prettiness` is such a commonly used parameter for you that you would like to access it quickly from the OpenAPI UI, you can decide to display it always on top. To do so you would edit the metadata of the `prettiness` property of the `DdraEndpoint` entity in a programmatic way or manually from the control center.
In your case you would add a `Priority` metadata with a high value for its `priority` property. Because this behavior should be specific for OpenAPI you would add an `openapi` UseCaseSelector to the metadata.
