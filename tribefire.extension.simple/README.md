# Simple Module - Getting Started with tribefire Module Development

## Outdated
This module works fine, but this `README` and probably also some comments in the sources are outdated. This is why this document speaks of *Simple Cartridge* (instead of *Simple Module*) everywhere. (Cartridges were the predecessors of modules.)

## Work in Progress

We are currently working on the Simple Cartridge to also add Module-based examples. When this is done, this README will be updated.

## Introduction

The *Simple Cartridge* is an (intentionally) simple example cartridge, which consists of just a few simple models and extensions. Purpose of this cartridge is to help developers quickly get started with tribefire (2.0) cartridge development and important features such as *Wire*, *DDSA* and *Platform Assets* based setup.

## Artifacts
Although the Simple Cartridge itself doesn't do that much, this example still consists of quite a few artifacts. They are used to demonstrate important concepts such as how to share code between artifacts, how to set up a cartridge (programmatically) or how to implement unit and integration tests.

Category                 | Artifact                                                       | Description
------------------------ | ---------------------------------------------------------------| -----------
Model Artifacts          |                                                                | The model artifacts provide the Simple Cartridge related models. In this example models are created from Java code (i.e. simple Java interfaces). Of course, one could instead also use the *tribefire Modeler*.
(see above)              | [simple-data-model](./simple-data-model)                       | The data model (or business model) is an intentionally small and simple example model which contains types such as `Company`, `Department`, `Person` and `Address`.
(see above)              | [simple-deployment-model](./simple-deployment-model)           | The deployment model contains the denotation types for the deployables of the Simple Cartridge (i.e. access, service and web terminal).
(see above)              | [simple-service-model](./simple-service-model)                 | The service model contains the modeled service request and response types.
Implementation Artifacts |                                                                | The implementation artifacts contain the actual code (i.e. business logic), e.g. a service implementation.
(see above)              | [simple-cartridge](./simple-cartridge)                         | The main cartridge artifact. It contains extension implementations and Wire configuration. Building this artifact creates the `simple-cartridge.war` file.
(see above)              | [simple-commons](./simple-commons)                             | This artifact provides common classes which are used not only implementation of deployables (see `simple-processing`), but also for initialization and/or integration.
(see above)              | [simple-processing](./simple-processing)                       | This artifact provides the implementation of the deployables, i.e. the actual business logic (service, access, web terminal). Module and cartridge both depend on this artifact.
Test Artifacts           |                                                                | The test artifacts are used to test the code of the implementation artifacts. For more info on tests see [Tests](#tests).
(see above)              | [simple-processing-test](./simple-processing-test)             | Provides unit tests for artifact `simple-processing`.
(see above)              | [simple-integration-test](./simple-integration-test)           | Provides integration tests. For more information see [Integration Tests](#integration-tests).
Setup Artifacts          |                                                                | These artifacts are used to set up the Simple Cartridge. For more information see [Setup](#setup).
(see above)              | [simple-cartridge-initializer](./simple-cartridge-initializer) | Initializes the Cortex database with Simple Cartridge related models and deployable configuration.
(see above)              | [simple-cartridge-setup](./simple-cartridge-setup)             | Can be used to set up a tribefire environment with the `simple-cartridge` and respective configuration.

### Asset.man Files
Some of the artifacts have an `asset.man` file in the respective artifact folders. These specify the asset nature (e.g. `CustomCartridge`) and can also provide additional data.

`simple-cartridge`

    $nature = (CustomCartridge=com.braintribe.model.asset.natures.CustomCartridge)()
    .externalId='simple.cartridge'

This specifies that the asset is a (custom) cartridge and it also sets the external id.

`simple-cartridge-initializer`

    $nature = (PluginPriming=com.braintribe.model.asset.natures.PluginPriming)()
    .accessId='cortex'

Specifies that the asset is a plugin priming, which sets data on the `cortex` access.

`simple-cartridge-setup`
`simple-processing`

    $natureType = com.braintribe.model.asset.natures.AssetAggregator

Specifies that the asset is an aggregator, which means that is used aggregate (or combine) other assets, which are specified as dependencies in the respective `pom.xml`.
Note that the `simple-processing` artifact is mainly an implementation artifact. It also serves as an aggregator though as it depends on all custom models.

For more information on `asset.man` files and *Platform Assets* in general, please refer to the Platform Assets documentation.


## Setup

Artifact `simple-cartridge-setup` specifies a demo environment consisting of tribefire core components (e.g. `tribefire-services`, `tribefire-explorer`), the `simple-cartridge` and a demo configuration for the Simple Cartridge deployables.

How does this work? Well, `simple-cartridge-setup` describes the setup via respective dependencies in its `pom.xml`. For example, the `tribefire-standard-aggregator` provides the tribefire core components. Depending on `simple-cartridge` adds our cartridge. And the dependency on `simple-cartridge-initializer` is used to initialize the Cortex database with Simple Cartridge related setup (see below). The setup can be adapted just by modifying the dependencies of `simple-cartridge-setup`.

### Jinni
One can use *Jinni* to fetch the setup assets and set up the environment. Example:

    jinni.sh setup-local-tomcat-platform setupDependency=tribefire.extension.simple:simple-cartridge-setup#2.0 installationPath=/path/to/simple-cartridge-demo deletePackageBaseDir=true

For further information, please read the Jinni documentation.

### Configuration

Before one can use a cartridge and its extensions, respective configuration settings need to be provided. This can be done via the *Control Center*. Obviously, that also works for the Simple Cartridge. However, this example shows an alternative approach where all configuration steps are performed programmatically. This works using an initializer (from artifact `simple-cartridge-initializer`) which is run during startup of `tribefire-services`. It initializes the Cortex database with Simple Cartridge related models and deployable configuration.

This demonstrates how to do *configuration-as-code* with tribefire cartridges.

## Tests
This example contains unit and integration tests. The tests use standard tribefire testing libraries, which e.g. include *JUnit*, *AssertJ* based fluent assertions and also some custom helpers such as `AbstractTest`.

#### Unit Tests

The unit tests work out-of-the-box, i.e. they don't require any special setup. All tests can can be run directly from the IDE. For example, in *Eclipse* open the context menu of `simple-cartridge-test` project and click on *Run As / JUnit Test*.

#### Integration Tests

The `simple-integration-test` artifact contains (backend) integration tests. Whereas the unit tests work stand-alone, the integration tests require a proper environment to be set up. For more information regarding this setup see [Setup](#setup) above.

When the setup is available, the tests be run the same way as unit tests. The tests assume `tribefire-services` is available at `http://localhost:8080/tribefire-services`. One can also specify a custom URL by setting the JVM system property `tribefire.services.url`.

## Building

Tribefire requires a *Maven* repository to manage dependencies. Developers can also choose other build tools such as *Gradle* though. To demonstrate we will soon provide multiple variations of the Simple Cartridge, i.e. same code, but different build tool. Please give us a few more weeks. :)

## Documentation

All sources are documented with Javadoc and inline comments, (hopefully) making it easy to understand what's going on. For more detailed information on cartridge development and tribefire in general please refer to the general tribefire documentation.

## Feedback

Please let us know, whether this example helped you getting started with cartridge development and tell us, if you think that further documentation is needed or if you have any other requests.

## Happy Cartridge Development

We hope this example is a good start for tribefire cartridge development.


Have fun! :)
