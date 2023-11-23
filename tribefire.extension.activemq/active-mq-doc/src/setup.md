# Setup and Configuration

## Jinni Setup

Use the following depdendency with jinni to set up an ActiveMQ server in the current directory:

`jinni setup-local-tomcat-platform --setupDependency tribefire.extension.activemq:active-mq-server-module-setup#2.0`


### Configuration Properties

For more information about the properties, see [Runtime Properties](asset://tribefire.cortex.documentation:concepts-doc/features/runtime_properties.md).

| Property           | Description      | Default      |
| :------------- | :----     | :----      |
| AMQ_SERVER_BINDADDRESS         | The local IP address that the ActiveMQ broker should listen on. Use `0.0.0.0` to listen on all IP interfaces. | `0.0.0.0` |
| AMQ_SERVER_PORT         | The server port of the ActiveMQ broker.      | `61616` |
| AMQ_SERVER_DATA_DIRECTORY         | The folder where ActiveMQ should put its data files. If the state of the ActiveMQ service should stay consistent between re-installations, this must be set.      | `WEB-INF/activemq-data` |
| AMQ_SERVER_BROKER_NAME         | The name that the local ActiveMQ broker should get. If this is not set, a default name that contains the local IP address and the listening port will be created.      | `null` |
| AMQ_SERVER_USE_JMX         | Boolean flag that indicates whether the ActiveMQ broker should support JMX access. This can be useful for debugging purposes or when using a monitoring/inspection tool like HawtIO.      | `false` |
| AMQ_SERVER_PERSISTENCE_DB_DIRECTORY         | Defines where the persistence database should be stored.      | `WEB-INF/activemq-db` |
| AMQ_SERVER_HEAP_USAGE_IN_PERCENT         | An Integer value that defines how much heap (in percent) the ActiveMQ broker should use.      | `70` |
| AMQ_SERVER_DISK_USAGE_LIMIT         | The maximum number of bytes that ActiveMQ is allowed to occupy on the disk in general.      | `100000000` (100 MB) |
| AMQ_SERVER_TEMP_USAGE_LIMIT         | The maximum number of bytes that ActiveMQ is allowed to occupy on the disk for temporary files.      | `10000000` (10 MB) |
| AMQ_SERVER_CREATE_VM_CONNECTOR         | Boolean flag for indicating whether ActiveMQ should also add a `vm://localhost` connector. See the ActiveMQ documentation for more details.      | `false` |
| AMQ_SERVER_PERSISTENT         | Indicates whether the messages should be persisted. This would increase the fault tolerance, but also decreases the performance. Set this to `true` when there a mission-critical messages that are not re-sent.    | `false` |
| AMQ_CLUSTER_NODES         | A comma-separated list of IP addresses (or hostnames) or ActiveMQ instances that should form a cluster. The address may contain a port (separated by a colon), if the port is not part of the hostname, it will be assumed to be the same port as `AMQ_SERVER_PORT`.   | `null` |

