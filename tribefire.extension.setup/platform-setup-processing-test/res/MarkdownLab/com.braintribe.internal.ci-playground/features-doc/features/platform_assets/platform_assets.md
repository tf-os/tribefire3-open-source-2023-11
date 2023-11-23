# Platform Assets
>Platform assets are Maven artifacts classified by a nature element which serve the purpose to setup a tribefire instance in an incremental and reproducible way. Depending on the nature, the assets can range from data to code.

## General
You may think of platform assets as puzzle pieces that let you add and remove functionality using a central platform asset repository. This functionality doesn't only include tribefire-built apps and components, but also the tribefire platform itself. The assets that make up the platform are called tribefire assets.

Using platform assets allows you to easily create and enrich your tribefire instance. Moreover, you keep track of changes in tribefire elements as each asset is versioned. 

You can use platform assets to store:
* tribefire elements, such as <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entity types</a>, <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">models</a>, <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">accesses</a> and others as manipulation files
* environment configuration files
* custom code as <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.cartridge}}">cartridges</a>

With platform assets, you can package single tribefire elements as well as whole sets of models, experts, their configurations, cartridges with custom code, apps built on tribefire, and even the platform itself. 

{% include note.html content="You can store platform assets in any Maven-compatible repository."%}

### Setup
During setup, an asset is resolved along with its transitive dependencies. Downloading and setting up your tribefire to work with particular assets is done using a command line tool called Jinni. 
{% include tip.html content="For more information on setting up platform assets, see [Setting Up Environment for Platform Assets ](setting_up_platform_assets.html)."%}

## Platform Asset Natures
As a platform asset can be one of a number of different tribefire components, a nature defines how a particular asset is to be deployed in tribefire. You may think of an asset's nature as its type. The nature is used to identify the asset and map specific processing logic to the asset during a setup process.

Platform asset natures can be divided into the following categories:
* Computing assets, which extend the platform with additional functionality or data in a programmatic way:
    * `PluginPriming`
    * `ScriptPriming`
    * `Plugin`
    * `CustomCartridge`
    * `MasterCartridge`
* Data assets, which incrementally initialize persistence data. This can happen in a standardized way via recorded manipulations or computing assets:
    * `ManipulationPriming`
    * `ModelPriming`
    * `PluginPriming`
    * `ScriptPriming`
* Environment configuration assets, which do not introduce additional functionality but map assets to a specific environment configuration:
    * `RuntimeProperties`
    * `ContainerProjection`
    * `TomcatRuntime`
* Structural assets, which do not introduce additional functionality but enable you to structure dependencies within your project setup:
    * `AssetAggregator`
    * `ContainerProjection`
* Experience assets, which introduce frontend applications. Currently, experience assets are treated as web contexts:
    * `WebContext`

Because it is you who decides on the level of granularity of an asset (for example, you can have a single connection or a full-blown access packaged as an asset) you must define the nature for every platform asset. 

Nature | Description
------ | ----------
`AssetAggregator` | An aggregator of assets which brings no additional functionality with it. Its dependencies define the assets which are bundled together. An aggregator can depend on any type of asset. <br/> <br/> Example: `tribefire-standard-aggregator`
`ContainerProjection` | Environment configuration asset whose dependencies define the assets which should be mapped to a specific container. It does not bring additional functionality with it, but aggregates other assets like an `AssetAggregator` and further maps (projects) its direct asset dependencies to a specific container (a scalable runtime instance of tribefire). <br/> <br/> A `ContainerProjection` only makes sense for certain asset types and is otherwise ignored:<br/> `CustomCartridge` <br/> `RuntimeProperties` <br/> `WebContext` <br/> `Plugin` <br/><br/> This nature has a mandatory String property `containerName` where you define the folder in which its depending assets should be placed in the resulting setup package. <br/> <br/> Example: `demo-container`
`TomcatRuntime` | Preconfigured runtime configuration. Inside of the asset a `runtime.zip` file is found which contains standard tomcat base structure. While setting up a tomcat environment via Jinni's request `setup-local-tomcat-platform`, further custom configuration like port settings are injected in the respective configuration files, like `server.xml` or `web.xml`. Currently a `TomcatRuntime` is injected from the outside or a default one is used.<br/> <br/> Example: `tomcat-runtime`
`MarkdownDocumentation` | Documentation asset  which contains a `documentation.zip` file with a list of markdown files making up the documentation of the platform. Optionally, a separate file defines the menu structure of the provided documentation files. Documentation assets within a project setup result in an web context and can therefore be called offline and online. <br/> <br/> Example: `platform-setup-basics-doc`
`RuntimeProperties` | Environment configuration asset which brings environment configuration settings in form of environment variables. Environment variables are stored in the `runtime.properties` file in the same syntax as inside the `tribefire.properties` file. When resolving the dependencies, assets with the `RuntimeProperties` nature are collected and its environment variables are projected into one condensed `tribefire.properties`. <br/> <br/> Example: `services-url-runtime`
`CustomCartridge` | Computing asset which programmatically extends the platform with additional functionality or data. Such an asset has a `war` artifact part which contains a web application that extends the platform with additional functionality by binding denotation types from expert models to expert implementations to make them available for deployment and execution in tribefire. <br/> <br/> Example: `triebfire-simple-cartridge`
`MasterCartridge` | Core component nature which defines essential parts of tribefire services. This nature exists because core components, especially the master cartridge, often require special handling in specific environments, e.g. cloud. <br/> <br/> Example: `tribefire-services`
`ManipulationPriming` | Data asset which incrementally adds normalized manipulation information to a collaborative SMOOD access. Due to the normalization of the manipulation model it is possible to concatenate, compare and validate manipulation histories. Such assets can have the following optional artifact parts: <br/> * `data.man` containing general data events <br/>* `model.man` containing data events that concern model skeletons in the cortex database <br/> <br/> Both parts hold change event information expressed in GMML. The files can be edited manually or changed on a running tribefire by changing a collaborative SMOOD persistence programmatically or via the UI.<br/> <br/>The `ManipulationPriming` nature has the following properties: <br/>* `accessId` - Type `String` property which provides the `accessId` of the access that is to be primed by the given manipulations <br/> * `additionalAccessIds` - Type `set<String>` property which provides additional accesses where the data is shared <br/> * `roles` - Type `set<String>` property which configures the roles needed to update the asset  
`PluginPriming` | Data and computing asset that incrementally adds information in a programmatic way to a collaborative SMOOD access. Such an asset contains a `plugin.zip` artifact part which contains the relevant Java classes that implement the `PersistenceInitializer` interface. The contents of the `plugin.zip` file are the same as in an artifact of the `Plugin` nature. <br/> <br/> The `PluginPriming` nature has the following properties: <br/> * `accessId` - Type `String` property which provides the `accessId` of the access that is to be primed by the given manipulations <br/> * `additionalAccessIds` - Type `set<String>` property which provides additional accesses where the data is shared 
`ScriptPriming` | Data and computing asset that incrementally adds information to a collaborative SMOOD access in a scripted programmatic way. Groovy scripting language is for programmatic updates to the persistence. <br/> <br/> Such assets can have the following optional artifact parts: <br/> * `data.groovy` containing the script that concerns general data changes <br/> * `model.groovy` containing the script that concerns model skeletons in the cortex database <br/> <br/> Both parts hold a Groovy script which can access the prepared `$context` variable which complies to the `PersistenceInitializationContext` interface. <br/> <br/>The `ScriptPrimin` nature has the following properties: <br/> * `accessId` - Type `String` property which provides the `accessId` of the access that is to be primed by the given manipulations <br/> * `additionalAccessIds` - Type `set<String>` property which provides additional accesses where the data is shared
`ModelPriming` | A data asset that incrementally adds information for a model given by Java interfaces to the cortex collaborative SMOOD access. Priming models is one central aspect in tribefire as models are the concept which everything else is built around. Such an asset has a `.jar` artifact part containing the model interface classes. When processed by Jinni, the model classes from the `.jar` file are analyzed and converted into a `GmMetaModel` representation which is then converted into a GMML representation. In the end, the effective part during initialization of the database is the same as for the artifacts of the `ManipulationPriming` nature.
`Plugin` | A computing asset that injects basic functionality on which tribefire fundamentally builds, like database drivers or messaging implementations. Such an asset has a `plugin.zip` artifact part which contains the following file structure: <br/> * `lib` <br/>   * `abc.jaa` <br/>   * `xyz.jar` <br/> * `model` <br/>   * `a-model.jar` <br/>   * `b-model.jar` <br/> <br/> The jars in the `lib` folder are used to make up the class loader of the plugin which will have the tribefire application class loader as its parent. Those jars should not contain classes that are shared with tribefire's frameworking otherwise `ClassCastExceptions` will be thrown. <br/> <br/> The jars in the `model` folder are used to inject them into tribefire's application class loader to be shared between tribefire and the plugin. Normally, you name here models that are needed for the plugin denotations types. Plugins are only effective if denotation instances for the relevant plugin are announced to the system at other places.
`WebContext` | A web application archive which, from a server-side standpoint, can be either plain, static download content or server-side functionality such as servlets. The static content can be dynamic when interpreted in a web browser. That is the case when it represents a Javascript application. Such an asset has a `.war` artifact part which represents the web application archive.

### Assets and CollaborativeSmoodAccess
The collaborative SMOOD access is the central component of the tribefire platform. It is an object oriented database that supports the full capability of the `QueryModel` and is designed as an event-source database that allows to build data in an agile and collaborative way. In such an event-source database the history of the creation and manipulation of data is being used as actual persistence. Certain asset natures influence the states of a `CollaborativeSmoodAccess` referred to as **stages**. A stage is a named snapshot of the manipulation stack. The manipulations are stored using the Generic Model Manipulation Language. Those assets are the following:

* `ManipulationPriming` 
* `ModelPriming` 
* `PluginPriming`
* `ScriptPriming`
* `Plugin`
* `CustomCartridge` 

{% include image.html file="trunk-cortex.png"%}

{%include tip.html content="For more information, see [Smart Memory Object-oriented Database](smood.html)."%}

## Platform Setup Access
To support ease of management, we introduced the Platform Setup access, which allows you to manage your assets.

To open the Platform Setup access:

1. In <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>, click the cogwheel icon in the top-right corner and select **Switch to -> Platform Setup**.

Node | Description | Available Actions
-----| ------------|------------------
All Assets | Displays all assets in a list. |  - Deploy <br/> - Install <br/> - Add Dependencies <br/> - Merge <br/> - Close <br/> - Merge and Transfer <br/> - Close and Transfer
Modified Assets | Displays assets which have unsaved changes in comparison to its deployed state in a remote repository. The changes might be: <br/> - Manipulations which are not deployed (e.g. trunk assets or merged manipulations from a trunk asset) <br/> - Modified Dependencies <br/> - Nature changes | - Add Dependencies <br/> - Merge <br/> - Close <br/> - Merge and Transfer <br/> - Close and Transfer
Trunk Asset | Displays the trunk asset. |  - Add Dependencies <br/> - Merge <br/> - Close <br/> - Merge and Transfer <br/> - Close and Transfer
Assets by Nature | Displays asset grouped by their nature | - Deploy <br/> - Install <br/> - Add Dependencies <br/> - Rename Asset

## Trunk Asset
The trunk asset contains all the manipulations you make to a given access (so, for example everything you do in <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a>). 

Trunk assets have the `ManipulationPriming` nature and represent data that was recorded in `CollaborativeSmoodAccess` instances since your last setup, merge or close operation. 
{% include tip.html content="For more information on SMOOD accesses, see [Smart Memory Object-oriented Database](smood.html)."%}

A trunk asset is created when the first manipulation (like creating or changing a model, creating or changing an access, etc. ) is made in the respective access. Trunk assets contain transient data which you should deploy to a repository in order to persist them. 
Manipulations made in Control Center are stored in the trunk asset of the `cortex` access. 

Once a trunk asset is created, you can close the trunk asset effectively creating a new artifact.

By clicking the arrow next to the **Commit** button, you open the **Advanced Commit** dialog box where you can select what you want to do with the asset. 

{% include tip.html content="For more information on Control Center options, see the [Control Center Integration](platform_assets.html#control-center-integration) section of this document."%}

{% include note.html content="Any new manipulation creates a new trunk asset if none exists already."%}

When you close an asset, it is saved and appears in the **All Assets** section of the Platform Setup access.

## Closed Asset
You can update a closed asset with your latest modifications by merging the current manipulation state of trunk asset. 

## Asset Dependencies
Platform assets use maven POM files to manage asset dependencies. POM files are normally used to manage build dependencies for Java archives (`.jar`, `.war`, `.ear`, etc. ). Each dependency may address either one (manage/build) or both demands.
Assets with certain natures use the Maven POM to manage asset as well as build dependencies, for example:
* `Plugin`
* `PluginPriming`
* `ModelPriming`
* `CustomCartridge`

In the cases of the dependencies above, special care must be taken to address the different dependency purposes by qualifying the dependency declaration with:
* tag processing instructions
* Maven type
* classifier elements

Below is the description of the three possible cases.

### Build Only Dependencies
Such dependencies are **not** taken into account while resolving the platform setup but while resolving build dependencies.

```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>platform-model</artifactId>
    <version>${V.tribefire.cortex}</version>
</dependency>
```

### Asset Only Dependencies
Such dependencies are taken into account while resolving the platform setup but **not** while resolving build dependencies.

```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>platform-model</artifactId>
    <version>${V.tribefire.cortex}</version>
    <classifier>asset</classifier>
    <type>man</type>
    <?tag asset?>    
</dependency>
```

### Mixed Dependencies
Such dependencies are taken into account while resolving the platform setup **and** while resolving build dependencies.

```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>platform-model</artifactId>
    <version>${V.tribefire.cortex}</version>
    <?tag asset?>    
</dependency>
```

## Control Center Integration
<a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> provides the UI functionality you need to manage your platform assets.  

By clicking the arrow next to the **Commit** button, you open the **Advanced Commit** dialog box. 

Element | Description
------ | ------
Comment Box | Allows you to add a string comment which can help identify different stages. The comments are saved in manipulation files.
Platform Asset Management - None  | Allows you not to update the current asset with new data and only submit the comment.
Platform Asset Management - Merge | Allows you to update the current asset with your manipulations from last asset merge or close. You can select any valid stage from the current asset to merge into. 
Platform Asset Management - Close | Allows you to save your manipulations to a brand new asset. 

## Available Operations
We support the following operations:
* merge
* close
* install
* deploy
* merge and transfer
* close and transfer

### Merge
Merging allows you to append the manipulations that were recorded in a trunk asset to another asset with a `ManipulationPriming` nature or the trunk asset's predecessor. Note there must exist exactly one `ManipulationPriming` predecessor in the dependency chain. Only trunk assets can be merged.

The trunk asset itself will disappear after a successful merge. The asset to which it was merged will be marked as having unsaved changes which means that a transfer operation (install/deploy) is needed to finally persist the changes.

* Merge Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.MergeTrunkAsset` via one of the endpoints (e.g. webRPC or REST).
* Merge via UI

    You can find all current trunk assets by clicking on the **Trunk Assets** node in the Platform Setup access. Select the asset to be merged and execute the action **Merge**.

### Close
Closing a trunk asset means that the trunk asset is getting a fully qualified identification by specifying a `groupId`, a `name` and `version` for it. The trunk asset will disappear after a successful close operation and will reoccur on further manipulations being made.

* Close Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.CloseTrunkAsset` via one of the endpoints (e.g. webRPC or REST).

* Close via UI

    You can find all current trunk assets by clicking on the **Trunk Assets** node in the Platform Setup access. Select the asset to be merged and execute the action **Close**. A pop-up opens where the you must define the following values:
    * Group id: The group id which also defines the directory path in your repository (e.g. `my.group.id`)
    * Asset name: A lowercase dashed name (e.g. `my-asset-name`)
    * Version: The version of the asset (default is `1.0`)

### Add Dependencies
Adding and qualifying a dependency allows you to build up an ordered aggregation of assets and making it context sensitive. This also allows to incrementally extend the platform.

It's important to note that dependencies are transitively processed before the actual asset is being processed.

The action of adding an asset dependency enables you to further qualify the condition under which the dependency should be processed. You can either use a custom selector and/or a number of predefined and conjuncted selector shortcuts:
  * global setup candidate
  * design time only
  * runtime only
  * restricting to stages (e.g. dev, test, prod)
  * restricting to tags that support custom filtering

* Add Dependencies Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.AddAssetDependencies` via one of the endpoints (e.g. webRPC or REST).

* Add Dependencies via UI
    
    In the Platform Setup access, click the **All Assets** node. Select the asset want to add a dependency to and execute the action **Add Dependencies**. A pop-up opens where you select the assets to be added as dependencies and further qualify them with the qualification settings.

### Install
Installing an asset means to transfer it as an artifact to the configured local maven repository in order to locally persist it and make it available for further setups. The asset will be installed with its current `resolvedRevision` which won't be increased in this operation.

Only non-trunk assets can be installed.

* Install Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.TransferAsset` via one of the endpoints (e.g. webRPC or REST).

* Install via UI

    In the Platform Setup access, click the **All Assets** node. Select the asset to be installed and execute the action **Install**.

### Deploy
Deploying an asset means to transfer it as an artifact to a Maven-compatible repository to persist it and make it available for further setups. The asset will be deployed with its increased `resolvedRevision`.

You can deploy the assets of the following natures:
* `ManipulationPriming`
* `AssetAggregator`
* `ContainerProjection`
* `RuntimeProperties`

Only non-trunk assets can be deployed.

* Deploy Programmatically

    Execute the request `com.braintribe.model.platformsetup.api.request.TransferAsset` via one of the endpoints (e.g. webRPC or REST).

* Deploy via UI

    In the Platform Setup access, click the **All Assets** node. Select the asset to be installed and execute the action **Deploy**.

### Merge and Transfer

After an asset was successfully merged, it can be directly installed to the repository. This action is a combination of the Merge and Install actions.

* Merge and Transfer via UI

    In the Platform Setup access, click the **Trunk Assets** node. Select the asset to be merged and execute action **Merge and Transfer**.

### Close and Transfer
After an asset was successfully closed, it can be directly installed to the repository. This action is a combination of the Close and Install actions.

* Close and Transfer via UI
    
    In the Platform Setup access, click the **Trunk Assets** node. Select the asset to be merged and execute action **Close and Transfer**. A pop-up opens where the transfer operation can be specified.
