# Cartridge
>Cartridges are the components used to extend tribefire, allowing you to develop custom implementations, such as accesses, service processors, and others.

## General
A Tribefire cartridge allows you to extend the platform by implementing custom logic.
Within a cartridge, you can develop your custom implementations of regular tribefire extension points, such as:

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

<!--Once developed, a cartridge is deployed to tribefire as a `.war` file, and can be detected and configured using  <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.control_center}}">Control Center</a> – this step is required before any implementation contained in the cartridge can be used, since they must be deployed to the deployment registry to be activated.-->

Your cartridge can also bring in models, sorted in the following categories:

* [Deployment models](#deployment-model)
* [Data models](#data-model)
* [Service models](#service-model)

{% include note.html content="Cartridges developed for tribefire **1.1** will not work with tribefire **2.0**."%}
<!--Cartridges are considered first-class citizens of the system and are treated exactly the same as components within in the core system. All communications between the components is done via HTTP webRPC.-->

### Deployment Model
Deployment model is a key component of any cartridge. It contains all the denotation types for the cartridge's corresponding experts, i.e. the actual implementation that carries out some functionality. The denotation types are interfaces - Java representations of models that describe the configuration data of the implementation, such as an access, streamer, service processor, etc.

Deployment model is added to the cartridge via <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.wire}}">Wire</a> configuration. Additionally, Wire configuration is also used to bind the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.denotation_type}}">denotation type</a> to the <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.expert_type}}">expert type</a>, since each cartridge implementation requires both to function correctly.

<!--After the cartridge is deployed to tribefire, it can be detected and synchronized using Control Center. This process loads the denotation types into Cortex database, allowing the cartridge to be used in this instance of tribefire.-->

{% include tip.html content="For more information, see [deployment](deployment.html)" %}

### Data Model
Data model is used to contain any extra entities needed as part of the functionality of the cartridge. This could be a simple business model, for example, that can be packaged with the cartridge, or an integration model for use with an access. While the deployment model contains the mandatory modeled interfaces for the configuration of the different <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.expert_type}}">expert types</a> in the cartridge, data model is an optional element.

The data model is added to the cartridge via Wire. This configuration inserts the entities to be considered part of the data model into a set of `dataModelProvider`. It is possible using the Wire configuration to configure more than one data model for the cartridge.

<!--After the cartridge is deployed to tribefire, it can be detected and synchronized using Control Center. The data model is then loaded into the Cortex database and can be used just like any other model in tribefire.-->

### Service Model

If you have any custom processors implemented, you need a service model, containing all the requests and response entities being processed.

## Cartridge Architecture

From the architectural point of view, a cartridge is a group of platform assets.
There are several types of platform assets hosting certain cartridge elements and making sure the cartridge can be integrated in a Tribefire instance. We recommend you read about the different natures of platform assets before reading further.

{%include tip.html content="For information about platform asset natures, see [Platform Assets](platform_assets.html)."%}

## Cartridge Platform Assets

* [*AssetAggregator*](#assetaggregator) - setup artifact
* [*CustomCartridge*](#customcartridge) - main cartridge artifact
* [*ModelPriming*](#modelpriming) - model artifacts
* [*PluginPriming*](#pluginpriming) - initializer artifact

### `AssetAggregator`

`AssetAggregator` doesn't contain any cartridge elements, but represents a starting point for traversing `pom.xml` dependencies,  with the goal of finding all platform assets we want our platform setup to consist of.

Traversing is done based on the asset tag: `<?tag asset?>`. All tagged dependencies in `pom.xml` are considered to be platform assets provided they contain the `asset.man` file with a specified nature. The dependencies are traversed with the goal of finding more platform assets.

Example of **AssetAggregator** `pom.xml` (extracted from Demo Cartridge):

{% include pomXML.md %}

Above, you can find two dependencies: 

* `tribefire-standard-aggregator` is a platform asset that provides all the assets that make up the Tribefire platform, i.e. Control Center, Explorer, `tribefire-js`, Modeler, etc.
It also brings the standard `PluginPriming` assets that initialize the database with needed setup data, creating the initial storage.

* `tribefire-demo-cartridge-initializer` represents the `PluginPriming` cartridge, which brings in the `CustomCartridge` and `ModelPriming` assets (and itself).

Both of these dependencies (tagged as assets) are needed in order to achieve desired cartridge setup.

Note that this asset is the one you specify when using the Jinni setup tool to create a platform setup in a desired environment. Example Jinni CLI command for installing demo cartridge in a Tomcat container could look as follows: `jinni setup-local-tomcat-platform setupDependency=tribefire.extension.demo:tribefire-demo-setup#2.0 installationPath=PATH_WHERE_TRIBEFIRE_WILL_BE_INSTALLED`.

### `CustomCartridge`

`CustomCartridge` hosts cartridge deployables (experts) configuration based on their denotation types, bindings to their denotation types, and their actual implementation. 
Note that there is no instantiation (storing in database) of deployables being done in this asset, which is the job of the `PluginPriming` asset. 

By making the main cartridge artifact an asset of this specific nature, we make sure its resulting `.war` file can be found by the setup tool, and deployed in the desired environment. 
Additionally, an instance of `com.braintribe.model.deployment.Cartridge` is being created in the cortex database, 
representing our respective cartridge.
 
### `ModelPriming`

`ModelPriming` assets host cartridge models i.e. deployment, data and service models.

By assigning the `ModelPriming` nature to a model artifact, we make sure all of its models get stored in the database. 

### `PluginPriming`

The purpose of `PluginPriming` is to initialize the database with our cartridge setup data when a Tribefire instance is run. 
This is a great advantage and one of the core motivations behind the idea of platform assets - since our database is populated dynamically, the need for cartridge synchronization is eliminated.
Note that this is being done after our models from `ModelPriming` assets and cartridge instance are stored in the database.

`PluginPriming` consists of following components:

* cartridge Wire space
* plugin factory
* cartridge initializer

A cartridge Wire space hosts the **metadata** and **deployables instance configuration**, which are going to be stored in the  database. Note that this configuration of deployables is different than in `CustomCartridge` - that's because it is based on additional information on deployables instances, such as **metadata**, **IDs**, etc.

A plugin factory class is needed so that our cartridge initializer is found and run during Tribefire startup.

Cartridge initializer is the center of all the action. It sets up our models (they are already available in the database at this point) so they can be accessed via `Control Center`, provides them with additional configuration (such as new metadata), and stores our deployables in database.

### Cartridge Development

For information on cartridge development, see [cartridge tutorials.](back_end.html#cartridge)
