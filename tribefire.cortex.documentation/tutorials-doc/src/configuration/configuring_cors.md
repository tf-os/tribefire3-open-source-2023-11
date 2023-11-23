# Configuring CORS

Cross Origin Resource Sharing (CORS) is used to serve content from a tribefire instance to an application which has a different origin than the tribefire instance.

## General

Using CORS, you can filter requests based on their origin. If the origin of the request coming from the application is not allowed by the CORS configuration then tribefire blocks the request, not serving the resource.

By default, CORS configuration applies to the tribefire-services cartridge only. You can change the CORS configuration if you want to modify the default behavior.  

### CORS in tribefire-services

The CORS configuration persisted to the cortex only applies to the tribefire-services cartridge, which means other cartridges (including your custom ones) have no default CORS configuration.

### CORS in Other Cartridges

As CORS is not enabled for cartridges other than tribefire-services, you can implement any request filter solution you want.

### CORS Configuration in tribefire-services

CORS policies in the tribefire-services cartridge are enforced based on a configuration persisted in the Cortex. In cortex, there is an entity called `CortexConfiguration` which has a `corsConfiguration` property.

> You can have many instances of `CortexConfiguration`, but only the one with the id `singleton` is used at a time.

Property | Type | Description
----- | ------ | ------
`allowAnyOrigin` | Boolean | Flag that indicates whether requests are allowed for all origins.
`maxAge` | int | Amount of seconds the user agent is allowed to cache the result of the request.
`supportAnyHeader` | Boolean | Flag that indicates whether all headers are allowed.
`supportCredentials` | Boolean | Flag that indicates whether the resource supports credentials in the request.
`allowedOrigins` | `Set<String>` | Origins requests are allowed for.
`exposedHeaders` | | Headers the resource can use.
`supportedHeaders` | | Supported HTTP headers.
`supportedMethods` | | Supported HTTP methods.

> For more information on CORS properties, see the [W3 documentation](https://www.w3.org/TR/cors/#resource-processing-model).
