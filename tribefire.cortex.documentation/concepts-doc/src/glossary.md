# Tribefire Glossary

Here is a list of words used in `Tribefire` ecosystem that you won't find searching on the internet (or you will find only to be even more confused).

#### Artifact Reflection Builder
is an `Eclipse` plugin that adds addtional reflection information to an artifact when it is built by `Eclipse`.

#### Cascading MetaData Resolver
is a special API to resolve [meta-data](#metadata) for a given [model](#model).

#### Cortex
is an [access](#incremental-access) that contains the main configuration of a [Tribefire](#tribefire) server.

This is actually what makes `Tribefire` so special - internal components and their configuration are reflected as objects in a database, which opens the door for creating generic solutions.

For example, a generic REST API can be easily implemented - an [access](#incremental-access) or a [service processor](#service-processor), as well as data or a [service request](#service-request) entities can be decoded from URL parameters, and the desired functionality (CRUD or custom service) can be invoked. This is possible, because the components and their data/request types are fully reflected.

BTW: we're saying "main configuration", because anyone can create it's own custom access to store custom configuration.

#### Debug Module Builder
is an `Eclipse` plugin that dynamically configures the project to be able to be run in an `Eclipse` servlet debug session.

#### (D)CSA
(`(Distributed) Collaborative Smood Access`) is our own [access](#incremental-access) implementation, mostly used for storing configuration.

At runtime, all the data is stored internally in memory in a [Smood](#smood), and persistence is an ever-growing file of manipulations (i.e. changes to the data).

In a distributed environment a so called `shared storage` is used for storing these manipulations, thus keeping the individual nodes in sync and up to date.

#### DDRA
(`Denotation Driven Rest Architecture`) denotes CRUD and [DDSA](#ddsa) over REST. It is a web endpoint for CRUD operations and custom [services](#service-request) where the data and custom request entities are encoded using HTTP URL parameters.

Note that CRUD operations are available for every [access](#incremental-access) while custom services are available for service domains, including accesses.

#### DDSA
(`Denotation Driven Service Architecture`), i.e. an API/service architecture where individual requests are represented by entities, parameterized by their properties, and an API is represented by a model consisting of such entities.

For comparison, in Java an API might be represented by an interface and individual requests might be methods of that interface, parameterized by, well, parameters of these methods.

#### DevRock
is a term that covers tools we use internally for developing, building and packaging our applications (see [DevRock Ant tasks](#devrock-ant-tasks) and [Jinni](#jinni)) and managing dependencies (see [Malaclypse](#malaclypse) and [Greyface](#greyface)).

#### DevRock plugin
is the common and central `Eclipse` plugin that provides common features for working with Eclipse (project management) and services for the other plugins (see [Greyface](#greyface), [Model Builder](#model-builder), [Debug Module Builder](#debug-module-builder), [ArtifactReflectionBuilder](#artifact-reflection-builder), [Mungo Jerry](#mungo-jerry))

#### DevRock Ant tasks
is collection of jars (actually distributed as a zip file containing said jars) that contain custom ant tasks needed to build our artifacts. These jars needs to be added to the ant lib folder.

#### DOP
(`Deletion Oriented Programming`). There's only one way to stay agile, and it ain't carrying old stuff around forever.

#### GM
(`Generic Model`) is the base for all our technology. Core of GM is a new type system with 8 simple types (String, Date, Integer, Long...), enums, entities and collections (List, Set, Map). It has its own reflection (with `EntityType` and `Property` instead of `Class` and `Method`), which can be viewed as a language extension. In Java, this idea is "emulated" with interfaces where each property is manifested by a getter and setter pair.

#### GMML
(`Generic Model Manipulation Language`) is our custom language for [manipulations](#manipulations).

#### GMQL
(`Generic Model Querying Language`) is our custom language for querying entities, with a syntax very similar to `HQL`.

#### Greyface
is an `Eclipse` plugin for finding dependencies of a given artifact in selected public repository like `maven central`, and uploading them to our own `Maven`-compatible repository (`Artifactory`).

#### Hydrux
is a [tribefire.js](#tribefirejs)-based framework for building modular, server-side configurable client applications.

#### Incremental Access
sometimes shortened to just `Access`, is the term we use for a data storage, i.e. something where data can be store and queried from.

Note that every access is also a [Service Domain](#service-domain).

`HibernateAccess` is an implementation backed by an SQL DB (and Hibernate of course).

We also use our own object-oriented - the [(D)CSA](#dcsa), which is among other things necessary because not every model can be reasonably mapped to SQL tables.

#### Jinni
is a command line tool for everything other than building (which is done with Ant and [DevRock ant tasks](#devrock-ant-tasks)). It has a wide range of features like:
* creating new artifacts
* preparing dependencies for JavaScript development (similar to npm install)
* creating a "package" (for the lack of a better word) of tribefire assets  like platform, modules (functionality), initializers (configuration), ui client and so on, either for production or locally for debugging...

Jinni can also update itself with `jinni-update.sh`, can update [DevRock Ant tasks](#devrock-ant-tasks) with `jinni update-devrock-ant-tasks` and comes with an auto-generated bash completion script (which is easily possible due to normalization of service APIs via [DDSA](#ddsa)).

#### Malaclypse
is a dependency resolution tool for `Maven`-compatible artifacts. Simply said, it can read `pom.xml`s, resolve the dependencies based on given parameters (e.g. build vs compile vs test, and much more) and download the desired parts (binaries, sources, javadoc, anything else).

#### Manipulations
are entities from the `manipulation-model`. They describe incremental changes on data like a new entity was created, a property was changed or an element was added into a list on a given position.

Typical usages are:
* session commits - where a client sends changes to the data, rather than the latest version of the data.
* [DCSA](#dcsa) persistence - changes to the data in the DB is stored as manipulations, typically in a human-readable format called [GMML](#gmml).

#### Meta-Data
is an additional information attached to a model, on top of [model skeleton](#model-skeleton). There are basic `MetaData` for example to make a given property is mandatory, unique, invisible to certain roles, but custom meta-data can be configured too.

Since MD can be defined in multiple complicated ways with selectors and inheritance (similar to styles for HTML elements), there is a special API to resolve them called [Cascading Meta Data Resolver](#cascading-metadata-resolver).

NTOE that we probably use "metadata", "MetaData" and "meta-data" interchangeably (sorry).

#### Model
is a special type of artifact which consists of entities and enums (with the sole exception of `root-model`, which also contains the 8 simple types for the properties of our entities). Each entity/enum type is typically declared in some model.

Models can depend on other models (just like regular artifacts/libs), and every model must transitively depend on the `root-model`.

#### Model Builder
is an `Eclipse` plugin that adds additional reflection information to a model artifact when it gets built in `Eclipse'

#### Model Oracle
is an API that can resolve basic information about a model and it's types, e.g. what are all the sub/super types of given entity type, or what the model's transitive dependencies.

#### Model Skeleton
is the pure type-related information from a model. It includes the model name, entity/enum type names (called type signatures in [GM](#gm)), property names and types and enum constant names.

#### Mungo Jerry
is an `Eclipse` plugin that adds helper features for `GWT` development.

#### Ravenhurst
is a servlet that ties in into the database of an (our) artifact repository, and can deliver information about its content and about changes happened after a specific point in time. It is used to implement the 'dynamic repository update policy', i.e. it allows to keep the local indices in sync with the remote data in a more efficient way than the standard repositories and build systems can.

#### Shared Storage
see [DCSA](#dcsa).

#### Service Domain
is a logical name for a collection of [service processors](#service-processor), which implement the functionality for their corresponding [service requests](#service-request).

Every [access](#incremental-access) is also a service domain, which automatically supports requests from `access-api-model` like `QueryEntities` or `ManipulationRequest`. So just like we need to provide which access (i.e. database) we want to query, we need to specify the domain for other types of requests.

NOTE however that some requests are domain independent, like say `OpenUserSession`, which is pretty much a request to login from the outside and is a `PlatformRequest`.

#### Service Processor
is an actual implementation of some API (functionality). A `Service Processor` can handle one type of [service request](#service-request), typically some abstract request type and the processor internally decides what to do based on which concrete sub-type is passed to it.

#### Service Request
is a special type of evaluable entity, which represents an API call. Each `Service Request` is a subtype of `ServiceRequest` from `service-api-model`, and it's return type is specified (in Java) by it's `eval` method.

E.g.: `EvalContext<String> eval(Evaluator<ServiceRequest> evaluator);` for a request that returns a `String`.

#### Smood
(`Simple Memory Object-Oriented Database`) is a somewhat sophisticated data structure that stores entities in memory and can perform incremental updates (apply [manipulations](#manipulations)) and evaluate queries. It's mainly used for storing configuration, e.g. in [cortex](#cortex).

#### Tribefire
is a platform for building complex, modular applications and is based on the [GM](#gm) technology.

#### Tribefire Explorer
is a client web application for the Tribefire server. It always works with a single [access](#incremental-access), allowing the user to query and edit the data and evaluate [service requests](#service-request). The layout of the application can be configured per access, storing this configuration in a corresponding [workbench access](#workbench-access).

#### tribefire.js
is a JavaScript library, distributed as a single `.js` file, which brings the core [GM](#gm) technology to `JavaScript`. It allows a connection to a [Tribefire](#tribefire) server and offers APIs to manipulate persistent data (`PersistenceGmSession`) and to call services via [DDSA](#ddsa).

It is also `TypeScript`-friendly, i.e. `.d.ts` files are available for all APIs.

#### Workbench Access
is an [access](#incremental-access) that contains the [Tribefire Explorer](#tribefire-explorer) configuration of another access.
