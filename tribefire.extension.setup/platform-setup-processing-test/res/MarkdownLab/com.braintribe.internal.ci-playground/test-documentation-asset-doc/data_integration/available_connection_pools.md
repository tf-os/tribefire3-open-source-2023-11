## General
Pooling connections allows them to be reused. Because creating a new connection for each data request is costly and therefore degrades performance, after creation the connection is placed in the connection pool and can be reused at a later point. 

If all connections are being used a new connection is created, depending on the parameters set for the collection pool. For example, the maximum amount of connections allowed.

## Available Connection Pools

<div class="datatable-begin"></div>

Connection Pool Name  |  Description  
------- | -----------
`C3P0`  |  A `DatabaseConnectionPool` <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.denotation_type}}">denotation type</a> which carries information to create a C3P0 connection pool managed by tribefire. For more information, see [C3P0 documentation](https://www.mchange.com/projects/c3p0/index.html).
`HikariCpConnectionPool` |  A `DatabaseConnectionPool` denotation type which is our recommended choice for Hibernate connections. Even though it provides the best performance, make sure to specify the `minConnectionPoolSize` so that not the maximum number of connections are used every time. For more information, see [Hikari documentation](https://brettwooldridge.github.io/HikariCP/).
`JndiConnectionPool` | A `DatabaseConnectionPool` denotation type which doesn't hold connection details, but simply a JNDI `lookupName` from where the actual JDBC connections are to be retrieved from. </br> This is useful when you are using an application server capable of managing JDBC connection pools which are shared among the applications, so tribefire doesn't have to manage it on its own and simply uses whatever the server provides. 
`RegistryBoundConnectionPool` | A `DatabaseConnectionPool` denotation type which doesn't hold connection details, but simply a `bindId` which point to an entry in the registry of the environment deployables. </br> One way of creating an entry in such registry is to add your connection pool denotation instance as a json in the `configuration.json` file. </br> The advantage of doing so is that the connection details aren't stored in the `cortex` database, only the `bindId` is. This enables the same denotation instance in the `cortex` to point to different databases in different environments, where only the externally managed `configuration.json` has to differ. For more information, see [Creating Registry Bound Deployables](creating_registry_bound_deployables.html). 

<div class="datatable-end"></div>