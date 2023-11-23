# Setting up Tribefire in a Distributed Environment

Follow these instructions to set up tribefire in a distributed environment.

## General

Setting up tribefire in a distributed environment results in **tribefire services** and **cartridges** being provided from separate servers, improving tribefire performance and scalability.

## Prerequisites

You need the following items to complete the setup:

* Messaging infrastructure server
  > You can set up your messaging server based on the `ActiveMQ` cartridge, for details see **ActiveMQ** cartridge (if you have it in your setup).
* `ActiveMQ` messaging plugins for service and cartridge servers (do NOT extract the files):
    [https://artifactory.server/artifactory/core-stable/com/braintribe/activemq/jms-active-mq-messaging-plugin/](https://artifactory.server/artifactory/core-stable/com/braintribe/activemq/jms-active-mq-messaging-plugin/)
* A `JSON` configuration file called `configuration.shared.json`, used by service and cartridge servers, pointing to the listening port on the messaging infrastructure server (`"hostAddress": "tcp://localhost:61616"` in the below example):

```json
{
  "_type" : "com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry",
  "bindId" : "tribefire-mq",
  "denotation" : {
    "_type" : "com.braintribe.model.messaging.jms.JmsActiveMqConnection",
    "name" : "ActiveMq Denotation",
    "hostAddress": "tcp://localhost:61616"
  }
}

```

## Setting up the Cartridge Server

This tribefire server stores the cartridges you're using, while the functionality is provided separately.

1. Install tribefire in your target destination using the following command: [](asset://tribefire.cortex.documentation:includes-doc/jinni_command.md?INCLUDE). Replace `yourInstallationPath` with the actual destination.  
2. Set up your hosting port in `/runtime/host/conf/server.xml` (change the associated ports too). For this procedure, we're going to assume the cartridge server port is `28080`.
3. Remove the `/storage` and `/runtime/host/webapps/tribefire-services` folders. We don't need these in the cartridge server.
4. In the file `/runtime/host/webapps/ROOT/index.html`, change the `URL` to `"/tribefire-manager"` (as we no longer have services on this server).
5. Add the `ActiveMQ` plugin to the `/plugins` folder (create it if necessary). Do NOT extract the plugin.
6. Add the `configuration.shared.json` file to the `/conf` folder.
7. Set up the runtime properties in the `/conf/tribefire.properties` file as follows:
    * TRIBEFIRE_SERVICES_URL=`http\://localhost\:18080/tribefire-services` (the services are provided from the other server)
    * TRIBEFIRE_NODE_ID=`cartridge-demo\#0`
    * TRIBEFIRE_LOCAL_BASE_URL=`http\://localhost\:28080`
    * TRIBEFIRE_CONFIGURATION_DIR=`${TRIBEFIRE_INSTALLATION_ROOT_DIR}/conf`
    * TRIBEFIRE_PLUGINS_DIR=`${TRIBEFIRE_INSTALLATION_ROOT_DIR}/plugins`
    * TRIBEFIRE_IS_EXTENSION_HOST=`true`
8. Navigate to `yourInstallationDirectory/runtime/host/bin` and start the server (run the `catalina start` command, or open the `tribefire-console-start` file).

## Setting up Tribefire Services Server

This server provides the actual tribefire functionality, while using cartridges from the cartridge server.

1. Install tribefire in your target destination using the following command: [](asset://tribefire.cortex.documentation:includes-doc/jinni_command.md?INCLUDE). Replace `yourInstallationPath` with the actual destination.
2. Set up your hosting port in `/runtime/host/conf/server.xml` (change all associated ports too). For this procedure, we're going to assume the cartridge server port is `18080`.
3. Add the `ActiveMQ` plugin to the `/plugins` folder (create it if necessary). Do NOT unzip the plugin.
4. Add the `configuration.shared.json` file to the `/conf` folder.
5. Set up the runtime properties in the `/conf/tribefire.properties` file as follows:
    * TRIBEFIRE_CARTRIDGE_URL_DEMO=`http\://localhost\:28080/tribefire-demo-cartridge` - points to the cartridge on the other server
    * TRIBEFIRE_IS_CLUSTERED=`true`
6. Navigate to `yourInstallationDirectory/runtime/host/bin` and start the server (run the `catalina run` command, or open the `tribefire-console-start` file). That's it! You should now be able to detect, synchronize, and deploy the cartridges from the cartridge server.


<!-- don't call it clients, it's separation of services and cartridges
skip cloud-based environment

prereq: you need SOME messaging infrastructure (not necessarily tf

infrastructure manual: a port is available as a result

Adapt hostAddress to your messaging system port)-->