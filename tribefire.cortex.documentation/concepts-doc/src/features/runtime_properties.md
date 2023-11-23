# Runtime Properties

Properties influence the behavior and enabled features of your installation.

## General

The `tribefire.properties` file allows you to configure different components. Inside the file, you can find a number of properties as well as a description of what each property does. Changing the value of a property results in a change of behavior of an element of the system.

## Setting Properties

You can set the values for the properties in three ways by:

* setting the values in the `tribefire.properties` file
* passing the value directly to the JVM, for example using the `catalina.properties` files when using Tomcat
* passing the value via an environment variable on your OS

> For cloud deployments, it is better to pass properties as environment variables.

## Defining New Properties

You can define a new runtime property by doing one of the following:

* Creating a `RuntimeProperties` asset. See [RuntimeProperties Asset Nature](asset://tribefire.cortex.documentation:concepts-doc/features/platform_assets.md#runtimeproperties).
* Adding a new key-value pair in the `tribefire.properties` file.

> This option will not persist your configuration when you deploy a new setup. If you want your configuration to be persisted, use the option above.


[](asset://tribefire.cortex.documentation:includes-doc/runtime_properties_include.md?INCLUDE)

