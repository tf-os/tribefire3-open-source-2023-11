# Creating Registry-bound Deployables

Decoupling the configuration of a deployable from the Cortex database allows you to easily inject deployables from outside of Cortex. Registry-bound deployables are types of deployables which are not read from the Cortex database directly.

## General

The difference between regular deployables and registry-bound deployables is that the latter are not read from the Cortex database but either from:

* the `conf/configuration.json` file  
* an environment variable

Decoupling the configuration of a deployable from the Cortex database allows you to easily inject deployables from outside of Cortex. Imagine you have a Hibernate access connected to a database. If you wanted to test that access on a staging server before deploying it to a production instance, you'd need to create two separate connections:

* one with the staging server connection data
* one with the production server connection data

Using a registry-bound deployable allows you to create a placeholder value where normally your connection would be and use either the `configuration.json` or an environment variable to provide the value for the connection object. A property `bindId` is used to determine where to inject your deployable. You must specify the `bindId` where you want your registry-bound deployable to be injected as well as in your deployable definition, so the system knows which deployable you want to inject.

> For more information about the `configuration.json` file and environment variables in the context of this file, see [Configuring tribefire with configuration.json](configuring_with_json.html).

As of now, registry-bound deployables work for the following elements:

* Connection

## Creating a Registry-bound Connection

1. In Control Center, create a new connection. When asked about the type, select `RegistryBoundConnectionPool`.
2. Provide the necessary properties `name` and `externalId`, for example: `myConnection` and `REPO_ACCESS_CONNECTION`.
3. Provide a `bindId`, for example: `REPO_ACCESS_CONNECTION`.
    > Note the `bindId` and the `externalId` are the same. You will use them in your registry-bound deployable JSON definition.
4. In your file explorer, navigate to `tribefire/conf/configuration.json` (if the file doesn't exist, create it) and create your deployable JSON definition in there, for example:

    ```json
    [  
       {  
          "_type":"com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
          "bindId":"REPO_ACCESS_CONNECTION",
          "denotation":{  
             "_type":"com.braintribe.model.deployment.database.pool.HikariCpConnectionPool",
             "externalId":"REPO_ACCESS_CONNECTION",
             "name":"RepoAccessRegistryBoundConnection",
             "minPoolSize":1,
             "maxPoolSize":4,
             "connectionDescriptor":{  
                "_type":"com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor",
                "user":"cortex",
                "password":"cortex",
                "url":"URL_TO_YOUR_DATABASE",
                "driver":"YOUR_DATABASE_DRIVER"
             }
          }
       }
    ]

    ```
    
    > Every registry-bound deployable must be of the type `com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry`.
5. Restart the server. Your deployable is injected in place of the placeholder connection you specified with the `bindId` of `REPO_ACCESS_CONNECTION`.
6. Make sure it works by deploying and testing the connection using the action on the Action Bar.
