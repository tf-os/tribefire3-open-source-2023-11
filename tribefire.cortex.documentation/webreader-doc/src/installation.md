# Installation and Configuration of Webreader

## General

To install the Tribefire environment with the WebReader functionality, simply run the following command: `jinni setup-local-tomcat-platform --setupDependency tribefire.extension.documents:documents-setup#2.0 --installationPath Your/Path`.

What you should get is a Tribefire platform with the following extensions:

* Documents Cartridge, providing document models and a Web Terminal where you can upload documents.
* Conversion Cartridge, which is a dependency of the above. It provides document conversion models, conversion services transforming uploaded resources into tribefire documents, and another Web Terminal where you can browse conversion job statistics.

## Tribefire Properties Configuration

Both cartridges can be configured via the `tribefire.properties` file, found in `TF_INSTALLATION_DIR/conf` - you can simply add the required properties to this file in a new line. Information on each property is provided in a dedicated document for each cartridge.

For more information about properties in general, see [Runtime Properties](asset://tribefire.cortex.documentation:concepts-doc/features/runtime_properties.md).

## Database Configuration

The Conversion Cartridge includes a PostgreSQL driver by default, with no need for additional setup. However, if you want to connect to a different database, see [Connecting a PostgreSQL Database](asset://tribefire.cortex.documentation:tutorials-doc/database-connections/postgres_db_connection.md).

Instructions on how to connect to different database types are available in the same documentation asset.

## Browsing Conversion Statistics
To browse statistics for all conversion jobs, simply use the Conversion Statistics web terminal, available from tribefire landing page.

## What's Next?

For information on how to use the WebReader UI, see [Using Webreader UI](using_webreader_ui.md).