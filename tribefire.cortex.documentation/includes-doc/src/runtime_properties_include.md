// TODO re-think the whole strategy of non-modeled configuration

## Tribefire-services Properties

`tribefire-services` doesn't have to know about its URL at all (TRIBEFIRE-SERVICES-URL or TRIBEFIRE_PUBLIC_SERVICES_URL). All paths/endpoints shipped with `tribefire-services` (e.g.: login/logout, about, swagger, DDRA endpoints, etc.) should be referenced relatively when needed.

For distributed installations, `tribefire-services` does need to know about absolute URLs for GME installations (Control Center, Explorer), in addition to cartridges deployed externally. By default, these URLs should be treated relatively, assuming all components of an out-of-the-box installation are deployed on the same application server.



| Property                                            | Required | Local Setup Default                        | Description |
|-----------------------------------------------------|----------|--------------------------------------------|-------------|
| TRIBEFIRE_SERVICES_URL                              | YES      | `http://localhost:8080/tribefire-services`   | Explicit configuration of the absolute URL LoF of <b>tribefire-services</b> is required.                                         |
| TRIBEFIRE_CONTROL_CENTER_URL                        |          | `../tribefire-control-center`                | The external  URL available to the `tribefire-control-center` application. Used by the `HomeServlet` to prepare the individual links on the Landing Page.                                                                                                                                                                                                                                                                                                                      |
| TRIBEFIRE_EXPLORER_URL                              |          | `../tribefire-explorer`                     | The external URL available to the `tribefire-explorer` application. Used by the `HomeServlet` to prepare the individual links on the Landing Page.                                                                                                                                                                                                                                                                                                                             |
| TRIBEFIRE_JS_URL                                    |          | `../tribefire-js`                            | The external URL available to the `tribefire-js` library. Used by the `HomeServlet` to prepare the individual links on the Landing Page.                                                                                                                                                                                                                                                                                                                                       |
| TRIBEFIRE_EXTERNAL_PROPERTIES_LOCATION              |          | `conf/tribefire.properties`                  | If a properties file is found on specified location, these properties are taken into account when setting up TribefireRuntime properties during bootstrapping.                                                                                                                                                                                                                                                                                                                |
| TRIBEFIRE_USER_SESSIONS_MAX_IDLE_TIME               |          | 24h                                        | Defines the maximum idle time of standard user sessions as a single string containing the value and time unit, e.g.: `{@code 30m}` = thirty minutes, `{@code 2h}` = two hours, `{@code 1.5d}` = one day and a half, `{@code 1000s}` = one thousand seconds etc.                                                                                                                                                                                                       |
| TRIBEFIRE_USER_SESSIONS_STATISTICS_ENABLED          |          | TRUE                                       | Defines whether information about the user sessions created by a tribefire services instance should be persisted to the user statistics access.                                                                                                                                                                                                                                                                                                                         |
| TRIBEFIRE_COOKIE_DOMAIN                             |          | The domain of the current request context. | The `AuthServlet` is creating the `tfsessionId` cookie triggered by the /login page for successful authentications. If the cookie domain is explicitly configured, this value will be taken to bind the cookie to the domain. If none is configured, the domain of the current request context is used.                                                                                                                                                                                    |
| TRIBEFIRE_COOKIE_PATH                               |          | /                                          | The `AuthServlet` is creating the `tfsessionId` cookie triggered by the /login page for successful authentications. If the cookie path is explicitly configured, this value will be taken to bind the cookie to the path. If none is configured / (all) will be used.                                                                                                                                                                                                                     |
| TRIBEFIRE_COOKIE_HTTPONLY                           |          | FALSE                                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| TRIBEFIRE_COOKIE_ENABLED                            |          | TRUE                                       |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| TRIBEFIRE_ACCEPT_SSL_CERTIFICATES                   |          | TRUE                                       | Defines if the system accepts SSL certificates.                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| TRIBEFIRE_IS_CLUSTERED                              |          | FALSE                                      | Tells the system that it is currently running in a distributed environment (e.g.: multiple `tribefire-services` instances), and therefore various core accesses need to be switched to a centralized storage.                                                                                                                                                                                                                                                                                    |
| TRIBEFIRE_NODE_ID                                   |          |                                            |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |

## GMEs Properties

GME applications, if deployed externally (on a separate application server), need at least know about the absolute URL for `tribefire-services`. In that case (if there's a difference in the setup) the public URL needs to be used.

| Property                               | Required | Local Setup Default       | Description 
|----------------------------------------|----------|---------------------------|-------------------------------------------------------------------------|
| TRIBEFIRE_PUBLIC_SERVICES_URL          | YES      | `/tribefire-services`       | The external available RPC endpoint of `tribefire-services`. | 
|TRIBEFIRE_EXTERNAL_PROPERTIES_LOCATION   |          | `conf/tribefire.properties` | If a properties file is found on the specified location, these properties are taken into account when setting up `TribefireRuntime` properties while bootstrapping.|
| TRIBEFIRE_WEBSOCKET_URL                |          | No default                  | Can optionally be used to override the Base-URL the client uses to establish a websocket connection to the server. If not set, this URL is derived from the TRIBEFIRE_PUBLIC_SERVICES_URL by changing the protocol to either `"ws:"` or `"wss:"` (depending on http vs. https). and adding `"/websocket"`. |
| TRIBEFIRE_PLATFORM_SETUP_SUPPORT       |          | TRUE                      | Enables `platform-asset` UI features. |
| TRIBEFIRE_EXPLORER_URL                 |          |                           | Used in different use cases to switch to the Explorer application. |
| TRIBEFIRE_CONTROL_CENTER_URL           |          |                           | Used in different use cases to switch to the Control Center application.|
|TRIBEFIRE_MODELER_URL|||Used in different use cases to switch to the Modeler application.|

## Logging Properties
| Property                               | Required | Local Setup Default                      | Description|
|----------------------------------------|----------|-------------------------------------------|------------------------------------------------|
|TRIBEFIRE_LOG_LEVEL|||When changing this property at runtime, the Log Manager will try to set the log level to the specified value in all Log instances. It is not guaranteed that the value of this property correlates with the currently active log level. Possible values are: [TRACE, FINER, FINEST], [FINE, DEBUG], INFO, [WARN, WARNING], [ERROR, SEVERE] (similar values are put in square brackets).|

## Internal 

| Property                               | Required | Local Setup Default                      | Description|
|----------------------------------------|----------|------------------------------------------|-------------------------------------------------------------------|
|TRIBEFIRE_CONTAINER_ROOT_DIR| No |TRIBEFIRE_INSTALLATION_ROOT_DIR`/runtime/host`|The base path where the `host/` folder of Tomcat or other servlet container is located.|
|TRIBEFIRE_STORAGE_DIR| |TRIBEFIRE_INSTALLATION_ROOT_DIR`/storage`|The base path where the `storage/` folder of tribefire is located.|
|TRIBEFIRE_TMP_DIR| ||The folder where temporary files should be stored. If this is not set, the operating system’s default folder will be used.|
|TRIBEFIRE_CACHE_DIR| |TRIBEFIRE_INSTALLATION_ROOT_DIR`/storage/cash`|The base path where cashed files should be stored.|
|TRIBEFIRE_REPO_DIR| |TRIBEFIRE_INSTALLATION_ROOT_DIR`/storage/repo`|The base path where models that are deployed to repository from the Control Center should be stored.|
|TRIBEFIRE_DATA_DIR| |TRIBEFIRE_INSTALLATION_ROOT_DIR`/storage/databases`|The base path where data and resources that come from a storage that is not a Collaborative Smood Access (CSA) are. If you have a Wire `resourcesContract`, you can control where this base path should store resources using the `server()` method.|
|TRIBEFIRE_INSTALLATION_ROOT_DIR| ||The directory pointing to the installation root which is used for further resolving of directories like the  `conf` directory or the `storage`.
|TRIBEFIRE_CONFIGURATION_DIR| |TRIBEFIRE_INSTALLATION_ROOT_DIR`/conf`|The base path where configuration files should be stored.|
|TRIBEFIRE_CONFIGURATION_INJECTION_URL| ||Defines the URL pointing to an external configuration file which will be read at startup and fills the internal DeployableRegistry. e.g.: for external configuration of ConnectionPools.|
|TRIBEFIRE_CONFIGURATION_INJECTION_ENVVARIABLE| ||Defines the name of the environment variable that contains an external configuration JSON which will be read at startup and fills the internal DeployableRegistry.|
|TRIBEFIRE_EXECUTION_MODE||`mixed`| Defines if the deployables are deployed. Available options include: </br> - `design`: the deployables will not be deployed. The idea is that the user just uses the Control Center to modify the configuration and/or the model. </br> - `mixed`: deployables will be deployed and the runtime is fully functional. </br> Control Center is available regardless of this value |


## Platform Assets Properties

| Property                               | Required | Local Setup Default                      | Description|
|----------------------------------------|----------|------------------------------------------|-------------------------------------------------------------------|
|TRIBEFIRE_KEEP_TRANSFERRED_ASSET_DATA||True| Should be activated in case of troubleshooting in the domain of publishing (installing or deploying) Platform Assets. In case this property is set to **true**, created asset artifacts which are further used for publishing, are being kept in temp folder `storage/tmp`. The kept Java artifact contains the specific part files as well as hashes of the artifact. This may be useful in case the publishing failed for reasons like: wrong hash file generation.|
|TRIBEFIRE_PLATFORM_SETUP_SUPPORT||True| Activates Platform Asset support in all core-components of Tribefire: </br> - The GME gets enhanced with certain functionality around Platform Asset Management, e.g. the advanced commit button </br> - The backend tracks manipulations in trunk-stages of all collaborative SMOOD accesses which further keep access **Platform Setup** up-to-date (creating trunk-assets) |


## Platform Messaging Destination Names 

| Property                               | Required | Local Setup Default                      | Description|
|----------------------------------------|----------|------------------------------------------|-------------------------------------------------------------------|
|TRIBEFIRE_MESSAGING_TOPIC_MULTICAST_REQUEST|||Used to rename the topic of multicast requests. If you use a messaging system other than the one used in Tribefire and have a different naming convention for topics and queues, set this property to change these names.|
|TRIBEFIRE_MESSAGING_TOPIC_MULTICAST_RESPONSE|||Used to rename the topic of multicast responses in a messaging system.|
|TRIBEFIRE_MESSAGING_QUEUE_TRUSTED_REQUEST||| The queue that links TFS and Cartridges. It is assumed (hence the name) that this communication channel is trusted and that no other party has access to it.|
|TRIBEFIRE_MESSAGING_TOPIC_TRUSTED_RESPONSE||| Response channel for `TRIBEFIRE_MESSAGING_QUEUE_TRUSTED_REQUEST`.|
|TRIBEFIRE_MESSAGING_TOPIC_HEARTBEAT||| Used for sending heartbeat pings across all nodes to see which nodes are still alive|
|TRIBEFIRE_MESSAGING_TOPIC_UNLOCK||| Used by the lock manager to distribute a message to all interested parties that a lock has been released. This could shorten the time until the next in line gets the lock (who would do polling otherwise)|
|TRIBEFIRE_MESSAGING_TOPIC_DBL_BROADCAST|||Related to relationship management.|
|TRIBEFIRE_MESSAGING_TOPIC_DBL_REMOTE||| Related to relationship management.|
|TRIBEFIRE_MESSAGING_QUEUE_DBL_REMOTE||| Related to relationship management.|



## Security Related Properties

| Property                               | Required | Local Setup Default                      | Description|
|----------------------------------------|----------|------------------------------------------|-------------------------------------------------------------------|
|TRIBEFIRE_USERSESSION_IP_VERIFICATION||FALSE|If set to true, any request requiring a session ID is checked whether the originator of the request has the same IP address as the origin of the session creator. This prevents session IDs to be used by other hosts.|
|TRIBEFIRE_USERSESSION_IP_VERIFICATION_ALIASES|||If TRIBEFIRE_USERSESSION_IP_VERIFICATION is set to `true`, this comma-separated list of key-value pairs allows to specify address aliases. It is only intended for a limited number of known hosts.|

## Database Related General Settings

Using those properties, you can override the default `bindIds` and provide your own connection descriptors for them. On top of that, because you can override them, you can reuse the same connection for all the `bindIds` (something that is not available out of the box).

Default tribefire-services `bindIds`:

* tribefire-user-sessions-db
* tribefire-user-statistics-db
* tribefire-auth-db
* tribefire-client-challenges-db

> The above `bindIds` can be used to specify that the corresponding access should not be a SMOOD Access but a Hibernate Access. Hence, when one of these `bindIds` (which point to a DB connection) is defined, a Hibernate Access is deployed.

* tribefire-smoods-db - This defines the DB connection of a Distributed Smood Access. This should not be used anymore. The DCSA is the preferred method of sharing a SMOOD database across multiple instances.
* tribefire-resources-db - When this DB connector is set, Resources (ie. files) will be stored in the database instead of the filesystem. 
* tribefire-locking-db - If this `bindId` is used, `tribefire-services` will use the database locking manager instead of the one that operates only within the JVM.
* tribefire-leadership-db - If this `bindId` is used, `tribefire-services` will use the database leadership manager instead of the one that operates only within the JVM.
* tribefire-dcsa-shared-storage - If a Distributed Collaborative Smood Access is used, this would define the database connection.
* tribefire-leadership-manager - If this `bindId` is set, TFS will use the specified leadership manager instead of the default one. The corresponding implementation must be provided in a plugin.
* tribefire-lock-manager - If this `bindId` is set, TFS will use the specified lock manager instead of the default one. The corresponding implementation must be provided in a plugin.

Example of an etcd lock manager:
```json
{
    "_type":"com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
    "bindId":"tribefire-lock-manager",
    "denotation": {
            "_type":"com.braintribe.model.plugin.etcd.EtcdPlugableLockManager",
            "project":"documents-documents20-dev",
            "endpointUrls":["http://tf-etcd-cluster-client:2379"]
    }
}
```

| Property                               | Required | Local Setup Default                      | Description|
|----------------------------------------|----------|------------------------------------------|-------------------------------------------------------------------|
|TRIBEFIRE_DATABASE_USE_BLOB_BUFFER||`Derby, MySQL, MariaDB, and MSSQL`|A comma-separated list of regular expressions that identify JDBC driver names that do not support BLOB streaming.|
|TRIBEFIRE_JNDI_CONTEXT_PREFIX||`java:/comp/env/`|The prefix is a base path to where to find the JNDI connections pool. Defines the JEE container root JNDI context. |
|TRIBEFIRE_BINDING_AUTH_DS ||| Overrides the default `bindId` allowing you to provide your own connection descriptor. |
|TRIBEFIRE_BINDING_CLIENT_CHALLENGES_DS |||Overrides the default `bindId` allowing you to provide your own connection descriptor.|
|TRIBEFIRE_BINDING_SMOODS_DS |||Overrides the default `bindId` allowing you to provide your own connection descriptor.|
|TRIBEFIRE_BINDING_RESOURCES_DS |||Overrides the default `bindId` allowing you to provide your own connection descriptor.|
|TRIBEFIRE_BINDING_LEADERSHIP_DS |||Overrides the default `bindId` allowing you to provide your own connection descriptor.| 
|TRIBEFIRE_BINDING_LOCKING_DS |||Overrides the default `bindId` allowing you to provide your own connection descriptor.|
|TRIBEFIRE_BINDING_DCSA_SHARED_STORAGE |||Overrides the default `bindId` allowing you to provide your own connection descriptor.|
|TRIBEFIRE_BINDING_LEADERSHIP_MANAGER |||Overrides the default `bindId` allowing you to provide your own connection descriptor.|
|TRIBEFIRE_BINDING_LOCK_MANAGER |||Overrides the default `bindId` allowing you to provide your own connection descriptor.|


## CSA Priming

| Property                               | Required | Local Setup Default                      | Description|
|----------------------------------------|----------|------------------------------------------|-------------------------------------------------------------------|
|TRIBEFIRE_MANIPULATION_PRIMING|||Used for injecting data into specified accesses. See [Initializer Priming](asset://tribefire.cortex.documentation:concepts-doc/features/smood.md#initializer-priming)
|TRIBEFIRE_MANIPULATION_PRIMING_PREINIT|||Used for injecting data into specified accessed and is run before other initializers. See [Initializer Pre-priming](asset://tribefire.cortex.documentation:concepts-doc/features/smood.md#initializer-pre-priming)|

## Network Properties

|Property|Local Setup Default|Description|
|---------|------------------------|---------------------------------------------------------------------|
|TRIBEFIRE_HOSTNAME | This property is set by Tribefire on runtime. | The hostname assigned to the local address.|
|TRIBEFIRE_IP4_ADDRESS| This property is set by Tribefire on runtime. | The IPv4 address of the host. TF will try to ignore loopback adapters and network adapters that are deactivated. Furthermore, it will check whether the address is reachable. There is also the possibility to provide a blacklist of network interfaces. If there are multiple addresses, TF will take the first one available.|
|TRIBEFIRE_IP6_ADDRESS| This property is set by Tribefire on runtime. | The IPv6 of the host, obeying the same rules as for IPv4 addresses.|
|TRIBEFIRE_IP_ADDRESS| This property is set by Tribefire on runtime. | The same as the IPv4 address, unless `TRIBEFIRE_PREFER_IPV6` is true.|
|TRIBEFIRE_JVM_UUID| This property is set by Tribefire on runtime. | Unique ID used to construct the Node ID together with the hostname. |
|TRIBEFIRE_PREFER_IPV6||Indicates whether tribefire should prefer the usage of IPv6 addresses or IPv4 (default). This is, for example, needed when tribefire tries to determine its own IP address.|
|TRIBEFIRE_NETWORK_INTERFACE_BLACKLIST||A comma-separated list of network interface names that should be ignored by Tribefire.|
|TRIBEFIRE_NODE_ID | This property is set by Tribefire on runtime. | An ID constructed from the following components (explained above): `${TRIBEFIRE_PROJECT_NAME}@${TRIBEFIRE_HOSTNAME}#${TRIBEFIRE_JVM_UUID}`|

## Other Properties

|Property|Required|Local Setup Default|Description|
|---------|---------|------------------------|---------------------------------------------------------------------|
|TRIBEFIRE_FORCE_DEFAULT_SYSTEM_ACCESSES||False|If set to true, default system accesses (auth, user-sessions, ..) are forced and CortexConfiguration is ignored.|
|TRIBEFIRE_TENANT_ID|||A unique ID defining the tenant using tribefire. This information is necessary if, for example, a shared filespace or etcd service is used.|
|TRIBEFIRE_EXCEPTION_EXPOSITION||True|A boolean property that defines whether detailed information about exceptions should be returned to the client. Exception details include, for example, the stacktrace. For the sake of security, it might be advisable to set the exception exposition to false to prevent attackers to get information about the internal code structure and error mechanisms.|
|TRIBEFIRE_EXCEPTION_MESSAGE_EXPOSITION|||Same as TRIBEFIRE_EXCEPTION_EXPOSITION, but only referring to the actual error message of the exception. For even tighter security, this should also be set to false.|
|TRIBEFIRE_STATEPROCESSING_THREADS|||The number of workers that the processing engine (if it is in place) should employ to work on process contexts. Depending on the type of processes, the recommended number may vary. As a rule of thumb, it is recommended to use as many workers as CPU cores are available.|
|TRIBEFIRE_REQUESTTRACING_ENABLED||False|Enables Request Tracing in GmWebRpcServer. Can be (re-)set at runtime.|
|TRIBEFIRE_REQUESTTRACING_DIR||`../logs/requests`|Defines the base directory for the Request Tracing (default: logs/requests). Can be (re-)set at runtime.|
|TRIBEFIRE_REQUESTTRACING_TIMEOUT||`90000`|Defines how long the Request Tracing should be enabled in ms. (default: 15min). If set to 0 no timeout is defined. Can be (re-)set at runtime.|
|TRIBEFIRE_QUERYTRACING_EXECUTIONTHRESHOLD_INFO||`10000`|Defines the threshold of a query execution to enforce logging of the query on INFO level.  (default: 10sec)|
|TRIBEFIRE_QUERYTRACING_EXECUTIONTHRESHOLD_WARNING||`3000`|Defines the threshold of a query execution to enforce logging of the query on WARNING level. (default: 30sec)|
|TRIBEFIRE_PLUGINS_DIR||TRIBEFIRE_INSTALLATION_ROOT_DIR`/plugins`|Defines the plug-ins directory path.|
|TRIBEFIRE_THREAD_RENAMING||| Defines whether threads are renamed during runtime.|
|ENVIRONMENT_MESSAGING_DESTINATION_PREFIX|||Defines the prefix for all topics/queues used for messaging. If this is not set, the value of TRIBEFIRE_TENANT_ID will be used instead. If that is not set, no prefix will be used. If this value is "none", no prefix will be used.
|TRIBEFIRE_MULTICAST_PROCESSING_TIMEOUT|||Defines how long (in milliseconds) a multicast processor waits for responses from the known instances if no request timeout is given in the multicast request itself.|
|TRIBEFIRE_MULTICAST_PROCESSING_WARNINGTHRESHOLD|||Defines the threshold (in milliseconds) of a multicast request processing to log a WARNING.|
|TRIBEFIRE_MULTICAST_KEEP_ALIVE_INTERVAL|||Defines the interval (in milliseconds) of keep-alive signals for long running multicasted processes.|
|TRIBEFIRE_AUTH_ADMIN_USERNAME|||The username to be added to the `auth` access in case it doesn't exist yet. If this property is specified, `TRIBEFIRE_AUTH_ADMIN_PASSWORD` must also be specified.|
|TRIBEFIRE_AUTH_ADMIN_PASSWORD|||The encrypted password of the above user. This value **has to be encrypted** (use Jinni for encryption).|
|TRIBEFIRE_AUTH_ADMIN_ENFORCEPASSWORD || `false` | When set to `true`, admin password will be reset to the value set in `TRIBEFIRE_AUTH_ADMIN_PASSWORD` when a new session is started. This is useful is you forget the password of your admin user. |
|TRIBEFIRE_AUTH_ADMIN_ROLES|||A comma-separated list of roles that the user should get in case it has to be created.|
|TRIBEFIRE_AUTH_ADMIN_ENSURE||`true`|`true` or `false`. If true, a worker always ensures that `TRIBEFIRE_AUTH_ADMIN_USERNAME` exists in the `auth` access. If not defined, a `cortex` user is created with the cortex password and `tf-admin` role.|
| TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED || `false` | When set to `true`, the login dialog offers the option **Stay signed in**. If this option is selected, the session cookie will remain active, even if the user closes the browser session. If it's set to `false` (or, if the user disables the check box in the login dialog), the session cookie sent to the browser will have no expiry date, thus will be removed when the browser is closed (unless the default behaviour of your browser is different). |
| TRIBEFIRE_USER_SESSIONS_MAX_IDLE_TIME || `24h` | Specifies how long a user session should remain active when there is no activity on the session. After the specified inactive time has passed (i.e. no request with the corresponding session ID has been received by the server), the session is flagged as inactive and consequently removed by a periodic cleanup process. The time span can be specified as a human-readable string, using numbers and the time unit, as in `12h`, `30m`, `3600s`, etc. |
TRIBEFIRE_ELASTIC_FULLTEXT_ACCEPTLIST || All mime types are supported by default. | List of mime types getting indexed by elasticsearch 
TRIBEFIRE_ELASTIC_FULLTEXT_DENYLIST || None | List of mime types not indexed by elasticsearch
TRIBEFIRE_ELASTIC_FULLTEXT_MAXSIZE ||  `100 Mb` | File size limit for elasticsearch indexing.

