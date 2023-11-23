# Cartridge

> Cartridges are components used to extend Tribefire, allowing you to develop custom implementations, such as accesses, service processors, and others.


* [General](#general)
	- [Deployment models](#deployment-models)
	- [Data models](#data-models)
	- [Service models](#service-models)
* [Cartridge Architecture](#cartridge-architecture)
	- [`AssetAggregator`](#assetaggregator)
	- [`CustomCartridge`](#customcartridge)
	- [`ModelPriming`](#modelpriming)
	- [`PluginPriming`](#pluginpriming)


## General

A *Tribefire cartridge* allows you to extend the platform by implementing custom logic.
Within a cartridge, you can develop your custom implementations of regular Tribefire extension points, such as:

* Access
* Connection
* Web terminal
* Authentication service
* Service processor
* State change processor
* Action processor
* Transition processor
* Condition processor
* App
* Worker
* Resource streamer

Your cartridge can also bring in models, sorted in the following categories:

1. [Deployment models](#deployment-models)
2. [Data models](#data-models)
3. [Service models](#service-models)

All models in cartridge are created programmatically, i.e. as Java interfaces.

Note: Cartridges developed for Tribefire 1.1 will not work with Tribefire 2.0.

### Deployment models

*Deployment model* is a key component of any cartridge.
It contains all denotation types for the cartridge’s corresponding experts, i.e. the actual implementation that carries out some functionality.  
The denotation types describe the configuration data of experts, such as an access, streamer, service processor, etc.

### Data models

*Data model* contains any extra entities needed as part of the functionality of the cartridge.
This could be a simple business model, for example, that can be packaged with the cartridge, or an integration model for use with an access.
While the deployment model contains the mandatory modeled interfaces for the configuration of the different expert types in the cartridge, data model is an optional element.

### Service models

If your cartridge is to have any custom processor implementation, you also need a *service model*, containing all the request and response entities being processed.

## Cartridge architecture

From the architectural point of view, a cartridge is a group of *platform assets*.
There are several types of platform assets, hosting certain cartridge elements and making sure the cartridge can be set-up in Tribefire instance.
We recommend you read about the different natures of [platform assets](https://documentation.tribefire.com/#!/2.0/docs/platform_assets.html) before going further.

Cartridge includes the following platform assets:

1. [`AssetAggregator`](#assetaggregator) - setup artifact
2. [`CustomCartridge`](#customcartridge) - main cartridge artifact
3. [`ModelPriming`](#modelpriming) - model artifacts
4. [`PluginPriming`](#pluginpriming) - initializer artifact

### AssetAggregator

*`AssetAggregator`* doesn't contain any cartridge elements, but represent a starting point for traversing `pom.xml` dependencies with goal of finding all platform assets
we want our platform setup to consist of.

Traversing is being done based on the asset tag: `<?tag asset?>`.
All tagged dependencies in `pom.xml` are considered to be platform assets provided they contain the `asset.man` file with a specified nature.
The platform asset dependencies are traversed with the goal of finding more platform assets.

Note that tagged dependencies that only serve the purpose of platform setup and are not used in any way inside the artifact are also marked with `<classifier>asset</classifier>` and `<type>man</type>`.

Example of `AssetAggregator` `pom.xml` (extracted from demo cartridge):

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>tribefire.extension.demo</groupId>
        <artifactId>parent</artifactId>
        <version>[${major}.${minor},${major}.${nextMinor})</version>
    </parent>
    <properties>
        <major>2</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <revision>1-pc</revision>
        <archetype>library</archetype>
    </properties>
    <artifactId>tribefire-demo-setup</artifactId>
    <version>${major}.${minor}.${revision}</version>
    <dependencies>
        <dependency>
            <groupId>tribefire.cortex.assets</groupId>
            <artifactId>tribefire-standard-aggregator</artifactId>
            <version>${V.tribefire.cortex.assets}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
        <dependency>
            <groupId>tribefire.extension.demo</groupId>
            <artifactId>tribefire-demo-cartridge-initializer</artifactId>
            <version>${V.tribefire.extension.demo}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
    </dependencies>
</project>
```

<div class='collapsed'>
  # Spoiler alert!

  This is a spoiler
</div>

[Deployment models](demo-cartridge.md?INCLUDE&collapsed&level=0)

Above, you can find two dependencies:

* `tribefire-standard-aggregator` is a platform asset that provides all the assets that make up the Tribefire platform, i.e. Control Center, Explorer, `tribefire-js`, Modeler, etc.
It also brings the standard `PluginPriming` assets that initialize the database with needed setup data, creating the initial storage.

* `tribefire-demo-cartridge-initializer` represents the cartridge `PluginPriming`, which beside itself brings in `CustomCartridge` and `ModelPriming` assets.

Both of these dependencies (tagged as assets) are needed in order to achieve desired cartridge setup.

Note that this asset is the one you specify when using the ***Jinni*** setup tool to create a platform setup in a desired environment.
Example of Jinni CLI command for installing demo cartridge in a Tomcat container:

`jinni setup-local-tomcat-platform setupDependency=tribefire.extension.demo:tribefire-demo-setup#2.0 installationPath=PATH_WHERE_TRIBEFIRE_WILL_BE_INSTALLED`

### CustomCartridge

*`CustomCartridge`* hosts cartridge deployables (experts) configuration based on their denotation types, bindings to their denotation types, and their actual implementation.
Note that there is no instantiation (storing in the database) of deployables being done in this asset, which is the job of `PluginPriming` asset.

By making main cartridge artifact an asset of this specific nature, we make sure its resulting `.war` can be found by setup tool and deployed in desired environment.
Additionally, an instance of `com.braintribe.model.deployment.Cartridge` is being created in the cortex database, representing our respective cartridge.

### ModelPriming

*`ModelPriming`* assets host cartridge models i.e. deployment, data and service models.

By assigning the `ModelPriming` nature to a model artifact, we make sure all of its models get stored in the database.

### PluginPriming

The purpose of *`PluginPriming`* asset is to initialize the database with our cartridge setup data when Tribefire instance is run.
That is a great advantage and one of core motivations behind the idea of platform assets - since our database is populated dynamically, the need for cartridge synchronization is eliminated.
Note that this is being done after our models from `ModelPriming` assets and our cartridge representation instance are stored in the database.

It consists of following components:

* Cartridge Wire space
* Plugin factory
* Cartridge initializer

A *Cartridge Wire space* hosts the metadata and deployables instance configuration, which are going to be stored in the database.
Note that this configuration of deployables is different than in `CustomCartridge` -
that’s because it is based on additional information on deployables instances, such as metadata, IDs, etc, which doesn't drive
their workflow.

A *Plugin factory* is needed so that our cartridge initializer to be found and run during Tribefire startup.

*Cartridge initializer* is the center of all the action.
It sets up our models (they are already available in the database at this point) so they can be accessed via Control Center,
provides them with additional configuration (such as new metadata), and stores our deployables in the database.
