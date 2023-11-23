# Configuring tribefire with configuration.json

You can use the `configuration.json` file to configure certain aspects of Tribefire.

## Available Configuration Options

Using the `configuration.json` file, you provide the configuration information for SMOOD-based system accesses:

* external messaging system
* external data source for user sessions

## File Location

tribefire always looks for the existence of the actual file and only then it looks for the existence of the environment variable (either defined on OS level or in `catalina.properties` / `tribefire.properties`).

### Files

The value of the variable `TRIBEFIRE_CONFIGURATION_INJECTION_URL` is considered the filename of the JSON file. It such variable is not present, the default name `configuration.json` is used. The content of this file is read as a JSON structure. This is the case for the tribefire-services cartridge.

Custom cartridge configuration files are either defined by the `TRIBEFIRE_CONFIGURATION_INJECTION_URL_<context>` variable or the default file name `configuration.<context>.json` is used.

After that, it the system searches for `TRIBEFIRE_CONFIGURATION_INJECTION_URL_SHARED` variable. If there is no such variable, the default value `configuration.shared.json` is used. If this file exists, it is also read as a JSON file. If both a specific and a shared variable exist, the JSON entries are merged. The shared entries overwrite specific entries.

### Environment Variables

First, the system reads the variable `TRIBEFIRE_CONFIGURATION_INJECTION_JSON`. This is the case for the tribefire-services cartridge. Custom cartridge environment variables have `TRIBEFIRE_CONFIGURATION_INJECTION_JSON_<context>` added. The content of the environment variable with this name is parsed as a JSON structure. It is not interpreted as a file path.

There is also the shared `TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED` environment variable that is used after the `TRIBEFIRE_CONFIGURATION_INJECTION_JSON`. If both a specific AND a shared variable exists, the JSON entries are merged. The shared entries overwrite specific entries.

Approach | Configuration File Name | Description  
------- | ----------- | ------
a single configuration file for all cartridges  | `configuration.shared.json` | - You either define a `TRIBEFIRE_CONFIGURATION_INJECTION_URL_SHARED` variable and reference the path to the file  <br/> - You either define a TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED variable and place the JSON as the variable value: `TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED=[json content]`
configuration applied to the tribefire-services cartridge only | `configuration.json`  | You must define one of the following: <br/> - You define a TRIBEFIRE_CONFIGURATION_DIR variable in `tribefire.properties` and point it to: `<TRIBEFIRE_INSTALLATION_DIRECTORY>/tribefire/conf` <br/> - You define `TRIBEFIRE_CONFIGURATION_INJECTION_URL` variable and reference the path to the file <br/> - You define a `TRIBEFIRE_CONFIGURATION_INJECTION_JSON` variable place the JSON as the variable value
one configuration file for each cartridge | `configuration-CARTRIDGE_CONTEXT_NAME_IN_UPPERCASE.json` | - You either define a `TRIBEFIRE_CONFIGURATION_INJECTION_URL_uppercaseCartridgeName` variable and reference the path to the file <br/> - You either define a `TRIBEFIRE_CONFIGURATION_INJECTION_JSON_uppercaseCartridgeName` variable place the JSON as the variable value

> Note that multiple sources can be taken into consideration. If `configuration.json` and `configuration.shared.json` exist, both are read. The `RegistryEntry` in those files is collected and entries with the same ID are overwritten with their values from the `configuration.shared.json` file.
