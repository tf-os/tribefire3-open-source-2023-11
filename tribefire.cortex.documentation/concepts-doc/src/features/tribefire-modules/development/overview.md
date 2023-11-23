# Tribefire module development

Tips and tutorials on various aspects of module development.

## IDE setup

**[Running Tribefire Application from Eclipse](debugging.md)** - how to set up and your Application as a Tomcat-based project and run it from Eclipse.

## General

**[Developing Tribefire modules - Basics](../module-development.md)** describes how to write a Tribefire module. It explains how to bind custom code and configuration data to our application and how to define module's own extension points.

**[Static resources (Files)](static-resources.md)** - how to attach files with your module, how to make them public, and how to access them via code and, if public, via URL.

**[Controlling Module Classpath](module-classpath.md)** - when and how to control which module dependencies can be "promoted" to the main classpath (as an optimization) via configuring `private` and `forbidden` dependencies.

## WebPlatform specific

**[Creating transient Resources](transient-resources.md)** How to create a `Resource` instance. Typically relevant when evaluating a `ServiceRequest` that evaluates to a `Resource`.

**[Configuring DCSA](dcsa-configuration.md)** How to configure a DCSA, i.e. how to ensure the collaborative accesses are deployed as distributed ones (relevant for clustered environment when only a local file system persistence is not enough).
