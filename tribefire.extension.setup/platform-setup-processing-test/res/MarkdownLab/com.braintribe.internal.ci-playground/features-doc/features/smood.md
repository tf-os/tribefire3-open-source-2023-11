# Smart Memory Object-oriented Database
> SMOOD is a system configuration data and model persistence implementation.

## General

This document describes SMOOD itself, and then various `IncrementalAccess`  implementations based on SMOOD - Distributed Collaborative Smood Access (DCSA) and Collaborative Smood Access (CSA).

This means all these implementations use SMOOD as its query/apply manipulation engine/handler, and they only differ in terms of how they persist the actual data.

{%include apidoc_url.html link="interfacecom_1_1braintribe_1_1model_1_1access_1_1_incremental_access.html" className="IncrementalAccess" %}

Let's now have a look at SMOOD itself, and then at the different access implementations.

## SMOOD
SMOOD (Smart in-Memory Object Oriented Database) is a data structure holding a collection of <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.entity_type}}">entities</a> (called `population`), supporting queries and updates over this population. Indices are also supported, with `id` and `globalId` properties being indexed automatically, and indexing of other properties being configurable via <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.metadata}}">metadata</a>.

The main advantages of SMOOD are:
* no special infrastructure needed, it's all in-memory
* support for any <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.model}}">model</a>, regardless of its complexity (unlike e.g. `HibernateAccess`, where complex hierarchies with multiple inheritance or properties of type `GenericEntity` cannot be mapped).

This modeling flexibility is the main reason why we use SMOOD-based <a href="#" data-toggle="tooltip" data-original-title="{{site.data.glossary.access}}">accesses</a> for our core accesses, containing the configuration of our system.

### SmoodAccess

This is the most simple and straightforward SMOOD-based access. Its persistence consists of a `NonIncrementalAccess`, which is a persistence that stores and loads all the data at once. You cannot target an exact property for updating, nor can you evaluate a query against it.

{%include apidoc_url.html link="interfacecom_1_1braintribe_1_1model_1_1access_1_1_non_incremental_access.html" className="NonIncrementalAccess" %}

So the lifecycle of the SMOOD access is that on startup, it loads the population from the `NonIncrementalAccess` into SMOOD, and then uses it to evaluate queries. When it comes to write operations, `SmoodAccess` uses an extra layer for storing manipulations, called a manipulation buffer, which appends applied manipulations, one after another. 

When the threshold size of the buffer is reached, the entire population is persisted with the `NonIncrementalAccess`, and the buffer is purged.

{%include apidoc_url.html link="interfacecom_1_1braintribe_1_1model_1_1access_1_1smood_1_1api_1_1_manipulation_storage.html" className="ManipulationStorage" %}

The standard `NonIncrementalAccess` implementation is the `XmlAccess`, which simply stores its data in an XML file on the file system.

{%include apidoc_url.html link="classcom_1_1braintribe_1_1model_1_1access_1_1impl_1_1_xml_access.html" className="XmlAccess" %}

### DistributedSmoodAccess

The name distributed suggests these accesses can be used in a distributed environment with multiple nodes acting as the exact copies of each other. So the main point is to make sure that if one node applies some manipulations, other nodes also reflect these changes when a query is evaluated on them.

This implementation uses Hibernate to store manipulations in an SQL database common for all nodes. Before every operation, the state of the node is updated if needed, and when manipulations are being applied, the manipulation is also persisted. The whole transaction is guarded by a distributed lock, ensuring consistency for the entire cluster.

{%include apidoc_url.html link="classcom_1_1braintribe_1_1model_1_1access_1_1smood_1_1distributed_1_1_distributed_smood_access.html" className="DistributedSmoodAccess" %}

### CollaborativeSmoodAccess

In case of a collaborative SMOOD access, the persistence consists of stages, where each stage represents certain manipulations. 

From this perspective it is similar to the distributed SMOOD access in that it doesn't consist of the data, but of incremental changes on how to build the data. It is, however, even more complex, because some stages might be read-only, not backed by persisted manipulations, but e.g. by Java code (or some script) performing the changes.

For example, a simple collaborative SMOOD access can consist of the following stages:

1. Classpath model: `some.org:my-access-model`
    Read-only stage which simply finds a given model on a classpath and clones it into the collaborative SMOOD access's SMOOD.

2. Script: `groovy-stage`
    Read-only stage which executes a groovy script found with the `groovy-stage` folder. This script uses a variable called `$context` to access the SMOOD's session (`$context.getSession()`) and thus be able to create/access data.

3. GMML: `trunk`
    Stage backed by GMML file(s), with newly applied manipulation being appended to the correct manipulation file. For most accesses there is only one file - `data.man`, but specifically for cortex there are two of them - `model.man` and `data.man`. The `model` file stores manipulations where model elements are being modified (i.e. where they own the manipulations).

Every setup must end with a GMML stage, otherwise and that is the one where all new manipulations are written to.

When it comes to actual implementation, collaborative SMOOD access is configured with a list of `PersistenceInitializers` and the last stage must be a `PersistenceAppender`.

{%include apidoc_url.html link="namespacecom_1_1braintribe_1_1model_1_1processing_1_1session_1_1api_1_1collaboration.html" className="com.braintribe.model.processing.session.api.collaboration" %}

Additionally, there are two ways to configure these stages. The dynamic part is the content of a `config.json` file, containing a list of stages of the following types:

* `ManInitializer` - a GMML-based stage (`model.man`/`data.man` files)
* `ScriptInitializer` - Groovy script (`model.groovy`/`data.groovy` files)
* `PluginInitializer` - Initializer resolved via the plugin mechanism

The last entry in the `config.json` must always be a `ManInitializer`, called `trunk` in tribefire. New manipulations are always appended to this stage.

In addition to `config.json`, we have statically configured initializers via `Wire`, which can be any custom implementations of the `PersistenceInitializer` interface. We have two options here:
* use the initializers that run before the dynamic ones  
* use those running afterwards (post-initializers)

On top of the above, we have a mechanism to configure the initializer using an environment variable called `TRIBEFIRE_MANIPULATION_PRIMING`. The format of the value is a comma-separated list of entries with the following syntax: `${manipulationFile path}[>${accessId}]`. If the optional part (in square brackets) is omitted, `cortex` is used as a default `accessId`.

Other than the standard `IncrementalAccess` functionality, collaborative SMOOD access also supports service requests for managing stages such as renaming, merging, or retrieving a date from a given stage.

{%include apidoc_url.html link="classcom_1_1braintribe_1_1model_1_1access_1_1collaboration_1_1_collaborative_smood_access.html" className="CollaborativeSmoodAccess" %}

### DistributedCollaborativeSmoodAccess 

Distributed collaborative SMOOD access, just like the distributed SMOOD access, is an implementation designed for a clustered environment, but this one is an extension of a regular collaborative SMOOD access - it stores its data and resources on a file system. 

The reason we use this implementation, rather than a distributed SMOOD access, is that we also want to offer the stage management functionality found in collaborative SMOOD access.

When a distributed collaborative SMOOD access implementation is used in a cluster, each node will have an identical copy of a collaborative SMOOD access (aside from temporarily going out of sync), and a so called shared storage accessible by each of the nodes to coordinate the synchronization of the state.

Similar to the distributed SMOOD access, before every read operation we synchronize the local state with the shared state, so apply any operations that have been stored in the shared storage since the last check. In case of write operations, we use a distributed lock, and all local changes are also stored in the shared storage for all the other nodes to apply the next time they are syncing.

{%include apidoc_url.html link="classcom_1_1braintribe_1_1model_1_1access_1_1collaboration_1_1distributed_1_1_distributed_collaborative_smood_access.html" className="DistributedCollaborativeSmoodAccess" %}

The first two implementations of the shared storage are <a href="https://en.wikipedia.org/wiki/Java_Database_Connectivity">`JDBC`</a>- and <a href="https://coreos.com/etcd">`etcd`</a>-based.