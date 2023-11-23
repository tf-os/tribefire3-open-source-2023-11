# Architectural Overview

## General
In terms of architecture, your tribefire installation is built from building blocks called assets. Depending on the assets you have chosen to create your installation from, your tribefire installation will have different components. Given that you can create your own platform assets, there are many possible configuration options.

The assets which make up the platform itself are called tribefire assets. Below is the description of the assets that come bundled with the `tribefire-standard-aggregator` asset. You can use this asset to create a tribefire installation with all the main components.
{%include tip.html content="For more information about platform assets themselves, see [Platform Assets](platform_assets.html)."%}

{%include tip.html content="For quick installation instructions, see [Quick Installation](quick_installation_devops.html). For instructions on how to set up your environment for platform asset development, see [Setting Up Environment for Platform Assets](setting_up_platform_assets.html)"%}

## `tribefire-standard-aggregator` Dependencies
The diagram below presents direct dependencies the standard aggregator has to other assets:

{% include image.html file="standard-aggregator_architecture.png" max-width="800"%}

### `tribefire-initial-aggregator`
The `tribefire-initial-aggregator` asset introduces the initial storage assets:
* `cortex-initial-priming`
* `cortex-wb-initial-priming`
* `auth-initial-priming`
* `auth-wb-initial-priming`
* `audit-wb-initial-priming`
* `workbench-initial-priming`
* `setup-wb-initial-priming`
* `user-sessions-wb-initial-priming`
* `user-statistics-wb-initial-priming`

This initial storage assets provide minimal required pre-configuration settings for the tribefire platform, like the license (`cortex-initial-priming`), Control Center workbench (`cortex-wb-initial-priming`) or the basic user configuration (`auth-initial-priming`). All storage assets have the `ManipulationPriming` nature. 
{%include tip.html content="For more information about platform asset natures, see [Platform Asset Natures](platform_assets.html#platform-asset-natures)."%} 

### `tribefire-services`
This asset provides the essential core tribefire functionality as well as dependencies required for successful compilation (`javax.servlet-api` and `javax.websocket-api`). This asset is mandatory, hence it has the `MasterCartridge` nature. 
{%include tip.html content="For information about what is core tribefire functionality, see [Fundamentals](fundamentals.html) and [Features](features.html)."%}

### `tribefire-explorer`
This asset provides the Explorer user interface, which allows you to visually access and manage your data. Is is not mandatory to have this asset in your installation if you only plan to work with tribefire programmatically. Instead of Explorer, you can use any data management client. 
{%include tip.html content="For more information about Explorer, see [Explorer](explorer.html)."%}

### `tribefire-control-center`
This asset provides the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> UI. Control Center provides a visual user interface for the entire functionality of the `tribefire-services` asset, which means it also contains a built-in Modeler. Instead of using Control Center, you can create your own admin user interface that communicated with the master cartridge and use it instead. Is is not mandatory to have this asset in your installation if you only plan to work with tribefire programmatically.  
{%include tip.html content="For more information, see [Control Center](control_center.html)."%}

### `tribefire-js`
This asset provides tribefire.js integration. Is is not mandatory to have this asset in a tribefire installation if you plan to work with Java or REST APIs only.
{%include tip.html content="For more information about tribefire.js, see [tribefire.js](tribefire_js.html)."%}

### `tribefire-modeler`
This asset provides the standalone Modeler user interface. As this is the standalone Modeler, you can remove it if you only want the Modeler to be available from Control Center. Is is not mandatory to have this asset in a tribefire installation as you can create models programmatically.
{%include tip.html content="For more information about Modeler, see [Modeler](using_modeler.html)."%}

## Configuration Possibilities
You can create your own aggregator asset and pass it to Jinni to build your custom environment. You are free to select the assets you want, but make sure you must always include the `tribefire-services` asset. Below you can find an example asset configuration which provides a tribefire environment ready for REST development with MySQL.

### REST + MySQL
This asset configuration assumes you need a local REST development environment. The data will come from a MySQL database.

Required assets:
* `tribefire-services` to provide the core functionality
* `tribefire-initial-aggregator` to configure core components, like the license or Control Center dependencies
* `tribefire-control-center` to provide Control Center where you will create your access
* a new asset of the `Plugin` nature used to introduce the MySQL database driver. To use the plugin, you must create a new platform asset and provide the correct configuration information. To create this asset:
    1. In the Platform Setup access, go to **All Assets** and click **New**.
    2. Provide the following information:
        * `groupId`: `tribefire.extension.jdbcplugins`
        * `name`: `mysql-plugin`
        * `version`: `2.0`
        * `nature`: `Plugin`
    3. Click **Commit**. 

You could also add you access asset if it has already been created. In this case, you will be able to create the access using Control Center because we added it as a dependency. If you create your access correctly, you can package it as an asset and simply add it as a dependency to your aggregator asset so that it is available the next time you build the environment using your custom aggregator.
{%include tip.html content="For information on how to create an example MySQL access, see [Connecting a MySQL Database](mysql_db_connection.html)."%}

{% include image.html file="REST_mysql.png" max-width="800"%}

