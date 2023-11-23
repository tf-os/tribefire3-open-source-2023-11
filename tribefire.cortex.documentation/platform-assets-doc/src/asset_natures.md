## Asset Natures

Each asset is defined by a specific type, a so called `PlatformAssetNature`. The nature is used to identify the asset and map specific processing logic to the asset during a setup process.
Natures can be grouped into several groups each, depending on their functional behavior.

### Computing Assets

Computing assets extend the platform with additional functionality or data in a programmatic way.

* [MasterCartridge](#mastercartridge)
* [CustomCartridge](#customcartridge)
* [PluginPriming](#pluginpriming)
* [ScriptPriming](#scriptpriming)
* [Plugin](#plugin)


### Data Assets

Data assets incrementally initialize persistence data. This can happen in a standardized way via recorded manipulations or [computing assets](#computing-assets).

* [ManipulationPriming](#manipulationpriming)
* [ModelPriming](#modelpriming)
* [PluginPriming](#pluginpriming)
* [ScriptPriming](#scriptpriming)
* [ResourcePriming](#resourcepriming)

### Environment Configuration Assets

Environment configuration assets do not bring additional functionality. They are used to define runtime environments, to configure those environments and and to map assets to them.

* [RuntimeProperties](#runtimeproperties)
* [ContainerProjection](#containerprojection)
* [TomcatRuntime](#tomcatruntime)
* [MarkdownDocumentationConfig](#markdowndocumentationconfig)

### Structural Assets

Structural assets do not bring additional functionality. They are used to enable the possibility to structure dependencies within your project setup.

* [AssetAggregator](#assetaggregator)
* [ContainerProjection](#containerprojection)

### Experience Assets

This type of nature is used to describe experience assets like frontend applications. Currently, experience assets are treated as web contexts.

* [WebContext](#webcontext)

### AssetAggregator

While bringing no actual functionality with it, the `AssetAggregator` aggregates a number of other assets of any type. With the help of an `AssetAggregator` those assets can be easily integrated with just one dependency to the aggregator. That makes especially sense when those assets are commonly reused in different projects and that very combination.

Example: [tribefire-standard-aggregator](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/cortex/assets/tribefire-standard-aggregator)

### ContainerProjection

While bringing no actual functionality with it, the `ContainerProjection` aggregates other assets like an [AssetAggregator](#assetaggregator) and further maps (projects) its direct assets dependencies to a specific container which stands for a separately scalable runtime instance of tribefire. This projection makes only sense for certain asset types and is otherwise ignored:

* [CustomCartridge](#customcartridge)
* [RuntimeProperties](#runtimeproperties)
* [WebContext](#webcontext)
* [Plugin](#plugin) 

The following nature properties can be customized:

|Property|Type|Description|  
|--------|----|-----------|
|containerName|`string`|Is mandatory and defines the name of the folder in which its depending assets should be placed in the resulting setup package.|

Example: [demo-container](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/extension/setup/test-container)

### TomcatRuntime

The nature `TomcatRuntime` defines the asset containing a preconfigured tomcat runtime. Such an asset has a `runtime.zip` artifact part that contains a templated standard tomcat base structure. While setting up a tomcat environment via Jinni's request `setup-local-tomcat-platform`, a template mechanism injects further custom configuration like port settings given by the command line options in the respective configuration files (e.g. `server.xml` or `web.xml`).

Currently the asset of this nature is injected from the outside or taken by default. In future it should also be possible to find it in the asset dependencies to persist its parameterization.

Example: [tomcat-runtime](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/cortex/assets/tomcat-runtime)

### MarkdownDocumentationConfig

An asset of type ```MarkdownDocumentationConfig``` determines the look and feel of Tribefire documentation portal. This asset contains a ```greeting.md``` where you can write a welcom message, and an ```mdoc-doc-metadata.yml``` file that determines the appearance of the documentaion portal, such as what entry points to show and what to include in the top navigation menus.

### MarkdownDocumentation

An asset of type `MarkdownDocumentation` identifies a documentation asset. Such an asset has a `documentation.zip` artifact part that contains list of markdown files which contribute to the documentation of the platform. Optionally, a separate file defines the menu structure of the provided documentation files.
Documentation assets within a project setup result in an web context and can therefore be called offline and online.

(Please note that the development for MarkdownDocumentation assets is currently ongoing and may therefore change in terms of structural aspects until its released in the first version. For now, the reference mentions above explains the basic structure where content markdown files are placed for now.)

Example: [tutorials-doc](https://github.com/braintribehq/tribefire.cortex.documentation/tree/master/tutorials-doc)

### RuntimeProperties

`RuntimeProperties` assets contribute to the setup by bringing environment configuration settings in form of environment variables. Such an asset has a `runtime.properties` artifact part that stores java properties. When resolving the dependencies, assets with the RuntimeProperties nature are collected and its environment variables are projected into one condensed `tribefire.properties`.

Example: [services-url-runtime](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/extension/setup/test-services-url-runtime)

### CustomCartridge

An asset of type `CustomCartridge` is a so called computing assets. Such an asset has a `war` artifact part which contains a web application that extends the platform with additional functionality by binding denotation types from expert models to expert implementations to make them available for deployment and execution in tribefire.

Example: [tribefire-simple-cartridge](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/extension/simple/simple-cartridge)

### MasterCartridge

The nature `MasterCartridge` defines the core component _tribefire-services_. There is an own nature for that as core components, especially the master cartridge, often requires special handling in specific environments (e.g. cloud).

Example: [tribefire-services](https://artifactory.server/artifactory/webapp/#/artifacts/browse/tree/General/core-dev/tribefire/cortex/services/tribefire-services)

### ManipulationPriming

An asset of type `ManipulationPriming` is a data asset that incrementally adds information in a normalized way to a [collaborative smood persistence](#collaborativesmoodpersistence). Due to the normalization of the manipulation model which is expressed in the GMML grammar it is possible to concatenate, compare and validate manipulation event histories which is very helpful when working concurrently on projects.

Such an asset can have the following optional artifact parts:

* `data.man` containing general data events
* `model.man` containing data events that concern model skeletons in the cortex database

Both parts hold manipulations (change events) expressed in the GMML grammar. The files can be maintained manually or can be recorded when running tribefire and doing changes to a collaborative smood persistence by programmatic or user experience ways.

The following nature properties can be customized:

|Property|Type|Description|  
|--------|----|-----------|
|accessId|`string`|Configures the accessId of the access that is to be primed by the given manipulations.|  
|additionalAccessIds|`set<string>`|Configures additional accesses when the data is shared|  
|roles|`set<string>`|Configures which roles are needed to update the asset|  

### PluginPriming

An asset of type `PluginPriming` is a data and a computing asset that incrementally adds information in a programmatic way to a [collaborative smood persistence](#collaborativesmoodpersistence). Such an asset contains a `plugin.zip` artifact part which contains the relevant Java classes that implement the [PersistenceInitializer](javadoc:com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer) interface. The content of the `plugin.zip` is identically organized as a [plugin](#plugin).

The following nature properties can be customized:

|Property|Type|Description|  
|--------|----|-----------|
|accessId|`string`|Configures the accessId of the access that is to be primed by the given manipulations.|  
|additionalAccessIds|`set<string>`|Configures additional accesses when the data is shared|  

### ResourcePriming
An asset of type ```ResourcePriming``` has a ```resources``` folder that contains resources (.png, .jpg, .pdf, or any binary representation of data).
Jinni processes such assets as follows:

1. checks the files inside this ```resources``` folder
2. calculates specification- and meta-data and persist this information in a respective data.man file for the access this asset contributes to
3. copies the resources as they are into the resources folder of the respective access. The resources folder is inside the storage folder.
    > For example, in the ```Demo catrridge```, the ```tribefire-demo-wb-resources``` is the ResourcePriming asset. It contains a group of images in its ```resources``` folder. After being processed by Jinni, the images are copied into the ```TribefireInstallationDirectory/storage/databases/access.demo.wb/resources/tribefire.extension.demo.tribefire-demo-wb-resources/```, and the calculated specifications and metadta are presisted in GMML format in ```TribefireInstallationDirectory/storage/databases/access.demo.wb/data/tribefire.extension.demo_tribefire-demo-wb-resources\#2.0/data.man ```.

Assets of type ```ResourcePriming``` should be referenced in Wire contracts.
> In the ```Demo catrridge``` example, you can see that the ```tribefire-demo-wb-resources``` is referenced in the ```DemoWorkbenchInitializerResourceContract``` interface.

### ScriptPriming

An asset of type `ScriptPriming` is a data and computing asset that incrementally adds information in a scripted programmatic way to a [collaborative smood persistence](#collaborativesmoodpersistence). The groovy scripting language is used to do the programmatic update on the persistence.

Such an asset can have the following optional artifact parts:

* `data.groovy` containing the script that concerns general data changes
* `model.groovy` containing the script that concerns model skeletons in the cortex database

Both parts hold script in the groovy language which can access the prepared `$context` variable which complies to the [PersistenceInitializationContext](javadoc:com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext) interface

The following nature properties can be customized:

|Property|Type|Description|  
|--------|----|-----------|
|accessId|`string`|Configures the accessId of the access that is to be primed by the given manipulations.|  
|additionalAccessIds|`set<string>`|Configures additional accesses when the data is shared|  

### ModelPriming

An asset of type `ModelPriming` is a data asset that incrementally adds information for a model given by java interfaces to the **cortex** [collaborative smood persistence](#collaborativesmoodpersistence). Priming models is one central aspect in tribefire as models are the concept which everything else is built around. Such an asset has a `jar` artifact part containing the model interface classes.

When being processed by `jinni` the model classes from the jar are analyzed and converted into a [GmMetaModel](javadoc:com.braintribe.model.meta.GmMetaModel) representation which is then converted into a GMML grammar representation. In the end the effective part during initialization of the database is the same like for [ManipulationPriming](#manipulationpriming).

### Plugin

An asset of type `Plugin` is a computing asset that can inject basic functionality on which tribefire fundamentally builds (eg. database driver, messaging implementations). Such an asset has a `plugin.zip` artifact part which contains the following file structure:

* `lib`
  * `abc.jaa`
  * `xyz.jar`
* `model`
  * `a-model.jar`
  * `b-model.jar`

The jars in the lib folder are used to makeup the classloader of the plugin. That classloader will have the tribefire application classloader as its parent. Those jars should not contain classes that are to be shared with tribefire's frameworking otherwise `ClassCastExceptions` will be the consequence.

The jars in the model folder are used to inject them into tribefire's application classloader to be shared between tribfire and the plugin. Normally you name here models that are needed for the plugins denotations types.

Plugins are only actually effective if at other places denotation instances for the relevant plugin are announced to the system.

### WebContext

An asset of type `WebContext` holds a web application archive which can be from a server-side standpoint plain static download content or server-side functionality such as servlets. The static content could be of course dynamic content when being interpreted in a web browser. That would be especially the case when it represent a javascript application. Such an asset has a `war` artifact part which represents the web application archive.

## Collaborative Smood Persistence

The `CollaborativeSmoodAccess` is a central component of the tribefire platform. It is an object oriented database that supports the full capability of the `QueryModel` and is designed as an event-source database that allows to build data in an agile and collaborative way. In such an event-source database the history of the creation and manipulation of data is being used as actual persistence. Based on this technique there are several possibilities to incrementally shape different end-states by building upon the reusable contributions from others. 

In tribefire this technique allows to build up central system access such as the reflective cortex access or workbenches.

You can contribute to a collaborative smood persistence with the following data assets:

* [ManipulationPriming](#manipulationpriming)
* [PluginPriming](#pluginpriming)
* [ScriptPriming](#scriptpriming)
* [ModelPriming](#modelpriming)

> For more information, see [SMOOD](asset://tribefire.cortex.documentation:concepts-doc/features/smood.md).