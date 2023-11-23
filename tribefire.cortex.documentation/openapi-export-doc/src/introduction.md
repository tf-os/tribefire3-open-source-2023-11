# OpenAPI Export

This document explains how to dynamically create OpenAPI documents reflecting REST requests possible in your tribefire installation. This may include operations on original tribefire data- and service-models as well as your custom ones.

## About OpenAPI
On the official OpenAPI webpage the technology is introduced as follows:

> The OpenAPI Specification (OAS) defines a standard, language-agnostic interface to RESTful APIs which allows both humans and computers to discover and understand the capabilities of the service without access to source code, documentation, or through network traffic inspection. When properly defined, a consumer can understand and interact with the remote service with a minimal amount of implementation logic.
>
> An OpenAPI definition can then be used by documentation generation tools to display the API, code generation tools to generate servers and clients in various programming languages, testing tools, and many other use cases.


If you want to learn more about OpenAPI itself, please visit the official webpage at [https://swagger.io/specification/](https://swagger.io/specification/)

## Prerequisites

* Make sure `tribefire.cortex:openapi-v3-module` is included in your tribefire installation. However as it is included there per default as part of the `tribefire-standard-aggregator`, chances are that it already is.

    >To check if the module is loaded you can try if the `openapi/ui` endpoint is available. Add the following URL path in your webbrowser after your tribefire-services URL: `openapi/ui/services/cortex`. This should display a human-readable representation of REST requests available on the cortex access.

* You must be logged in as any tribefire user.

## Scope

Documents in this section specifically explain the usage of the functionality that comes with the `tribefire.cortex:openapi-v3-module` which brings the following endpoints:

* `openapi/ui` - nicely displays the content of an OpenAPI document from a tribefire model in a human friendly way and lets you execute the contained REST requests conveniently from within the UI.

You will learn how to switch between different API modes:

* [CRUD on entity level](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_rest_v2_entities.md)
* [CRUD on property level](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_rest_v2_properties.md)
* tribefire [DDSA service requests](asset://tribefire.cortex.documentation:api-doc/REST-v2/rest_v2_api_v1.md)

as well as how to configure your models with metadata to influence description, grouping, order, visibility, etc. of your requests and their parameters in your OpenAPI documents.

## What's Next?

As you explore OpenAPI with tribefire, you should find the following information useful:

1. [OpenAPI endpoints](endpoints.md) - detailed explanation of the abovementioned endpoints.
2. [OpenAPI Export in Detail](how-it-works.md) - how exactly is the OpenAPI document generated.
3. [Metadata](metadata.md) - how to use metadata to influence OpenAPI requests and parameters.
