
Follow the links below to download latest tribefire core components, selected cartridges and related tools.

## Jinni

Jinni is a CLI tool provided by Braintribe, enabling you to install Tribefire on a local Tomcat host. You can download the Jinni Package [here](https://artifactory.server/artifactory/core-stable/tribefire/extension/setup/jinni/) (authentication required).

The list below provides Maven setup dependencies for each asset. You can directly copy these into the `--setupDependency` part of your Jinni command, as in `--setupDependency groupId:artifactId`.

## Available Assets

Asset                |               Download Link                   |         Jinni `--setupDependency`
-------------------------- | -------------------------------------|------------------------------
Tribefire client dependencies | [tribefire-client-default-deps](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/cortex/tribefire-client-default-deps) | Dependencies  required to communicate with `tribefire-services` through the [Java API](asset://tribefire.cortex.documentation:tutorials-doc/cartridge/quick_start_java.md).
tribefire Modeler  | [tribefire-modeler](https://artifactory.server/artifactory/core-stable/tribefire/cortex/modeler/tribefire-modeler)|`tribefire.cortex.modeler:tribefire-modeler#2.0`
Enablement Cartridges<br>Setups      | [tribefire-demo-cartridge](https://artifactory.server/artifactory/core-stable/tribefire/extension/demo/tribefire-demo-cartridge/) <br/> [tribefire-simple-cartridge](https://artifactory.server/artifactory/core-stable/tribefire/extension/simple/simple-cartridge) <br/> |`tribefire.extension.demo:tribefire-demo-setup#2.0`<br/>`tribefire.extension.simple:simple-cartridge-setup#2.0`
Enablement Cartridges<br>Source code | [Enablement Maven Artifacts](https://artifactory.server/artifactory/core-stable/tribefire/extension/enablement-maven/artifacts/) | Download the zip-file with the latest version
Product Cartridges         | [tribefire-ldap-cartridge](https://artifactory.server/artifactory/core-stable/tribefire/extension/ldap/ldap-cartridge/) <br/> [tribefire-shiro-cartridge](https://artifactory.server/artifactory/core-stable/tribefire/extension/shiro/shiro-cartridge/)<br/>[swagger-model-import-cartridge](https://artifactory.server/artifactory/core-stable/tribefire/extension/swagger/swagger-model-import-cartridge)<br/>|`tribefire.extension.ldap:ldap-setup#2.0` <br/>`tribefire.extension.shiro:shiro-setup#2.0`<br/>`tribefire.extension.swagger:swagger-model-import-setup#2.0`
Aggregators                | [tribefire-standard-aggregator](https://artifactory.server/artifactory/core-stable/tribefire/cortex/assets/tribefire-standard-aggregator)<br/>|`tribefire.cortex.assets:tribefire-standard-aggregator#2.0`
Plugins                    | [jdbcplugins](https://artifactory.server/artifactory/core-stable/tribefire/extension/jdbcplugins)<br/>|Check on individual plugins

