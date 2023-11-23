# Smart Memory Object-Oriented Database
> SMOOD is a system configuration data and model persistence implementation.

## General

This document describes SMOOD itself, and then various `IncrementalAccess`  implementations based on SMOOD - Distributed Collaborative Smood Access (DCSA) and Collaborative Smood Access (CSA).

This means all these implementations use SMOOD as its query/apply manipulation engine/handler, and they only differ in terms of how they persist the actual data.

Let's now have a look at SMOOD itself, and then at the different access implementations.

## SMOOD
SMOOD (Smart in-Memory Object Oriented Database) is a data structure holding a collection of entities (called `population`), supporting queries and updates over this population. Indices are also supported, with `id` and `globalId` properties being indexed automatically, and indexing of other properties being configurable via metadata.

The main advantages of SMOOD are:
* no special infrastructure needed, it's all in-memory
* support for any model, regardless of its complexity (unlike e.g. `HibernateAccess`, where complex hierarchies with multiple inheritance or properties of type `GenericEntity` cannot be mapped).

This modeling flexibility is the main reason why we use SMOOD-based accesses for our core accesses, containing the configuration of our system.

### SmoodAccess

This is the most simple and straightforward SMOOD-based access. Its persistence consists of a `NonIncrementalAccess`, which is a persistence that stores and loads all the data at once. You cannot target an exact property for updating, nor can you evaluate a query against it.

> For more information, see [NonIncrementalAccess javadoc](javadoc:com.braintribe.model.access.NonIncrementalAccess)

So the lifecycle of the SMOOD access is that on startup, it loads the population from the `NonIncrementalAccess` into SMOOD, and then uses it to evaluate queries. When it comes to write operations, `SmoodAccess` uses an extra layer for storing manipulations, called a manipulation buffer, which appends applied manipulations, one after another.

When the threshold size of the buffer is reached, the entire population is persisted with the `NonIncrementalAccess`, and the buffer is purged.

> For more information, see [ManipulationStorage javadoc](javadoc:com.braintribe.model.access.smood.api.ManipulationStorage)

The standard `NonIncrementalAccess` implementation is the `XmlAccess`, which simply stores its data in an XML file on the file system.

> For more information, see [XmlAccess javadoc](javadoc:com.braintribe.model.access.impl.XmlAccess)

### CollaborativeSmoodAccess

In case of a collaborative SMOOD access, the persistence consists of stages, where each stage represents certain manipulations.

From this perspective it is similar to the distributed SMOOD access in that it doesn't consist of the data, but of incremental changes on how to build the data. It is, however, even more complex, because some stages might be read-only, not backed by persisted manipulations, but e.g. by Java code (or some script) performing the changes.

Other than the standard `IncrementalAccess` functionality, collaborative SMOOD access also supports service requests for managing stages such as renaming, merging, or retrieving a date from a given stage.

> For more information, see [CollaborativeSmoodAccess javadoc](javadoc:com.braintribe.model.access.collaboration.CollaborativeSmoodAccess)

For example, a simple collaborative SMOOD access can consist of the following stages:

1. Classpath model: `some.org:my-access-model`
    Read-only stage which simply finds a given model on a classpath and clones it into the collaborative SMOOD access's SMOOD.

2. Script: `groovy-stage`
    Read-only stage which executes a groovy script found with the `groovy-stage` folder. This script uses a variable called `$context` to access the SMOOD's session (`$context.getSession()`) and thus be able to create/access data.

3. GMML: `trunk`
    Stage backed by GMML file(s), with newly applied manipulation being appended to the correct manipulation file. For most accesses there is only one file - `data.man`, but specifically for cortex there are two of them - `model.man` and `data.man`. The `model` file stores manipulations where model elements are being modified (i.e. where they own the manipulations).

Every setup must end with a GMML stage as that is the one where all new manipulations are written to.

When it comes to actual implementation, collaborative SMOOD access is configured with a list of `PersistenceInitializers` and the last stage must be a `PersistenceAppender`.

Additionally, there are two ways to configure these stages. The dynamic part is the content of a `config.json` file, containing a list of stages of the following types:

* `ManInitializer` - a GMML-based stage (`model.man`/`data.man` files)
* `ScriptInitializer` - Groovy script (`model.groovy`/`data.groovy` files)
* `PluginInitializer` - Initializer resolved via the plugin mechanism

The last entry in the `config.json` must always be a `ManInitializer`, called `trunk` in Tribefire. New manipulations are always appended to this stage.

In addition to `config.json`, we have statically configured initializers via `Wire`, which can be any custom implementations of the `PersistenceInitializer` interface. We have two options here:

* use the initializers that run before the dynamic ones  
* use those running afterwards (post-initializers)

#### Initializer Priming

On top of the above, we have a mechanism to configure the initializer using a runtime property called `TRIBEFIRE_MANIPULATION_PRIMING`. The format of the value is a comma-separated list of entries with the following syntax: `${manipulationFile path}[>${accessId}]`. If the optional part (in square brackets) is omitted, `cortex` is used as a default `accessId`.

Note that you can refer to an environment variable from `TRIBEFIRE_MANIPULATION_PRIMING` using the `env:VAR_NAME` parameter. Consider the below example, where we change the user password by calling the `AUTH_INITIALIZER` variable:

```
TRIBEFIRE_MANIPULATION_PRIMING="cortex/data.man,workbench/data.man>workbench,env:AUTH_INITIALIZER>auth"

AUTH_INITIALIZER="$0=com.braintribe...User(`ENTER_USER_ID`).password='ENTER_NEW_PASSWORD'"
```

You can also address not only one particular access (via `accessId`) but multiple accesses matching a certain `accessId` pattern. You can configure this using the `pattern` prefix before the `accessId`. Whatever follows the `pattern:` is interpreted as a regular expression to be matched against the `accessId`. For example, the following expression addresses all accesses matching `access.*wb` (e.g.: `access.foo.wb`, `access.bar.wb`, ...):

```
TRIBEFIRE_MANIPULATION_PRIMING="common/common.wb.primings/data.man>pattern:access.*wb"
```
**Important Guidelines for Initializer Priming**

* Maniplation files can have only one of the following names: `data.man` or `model.man`. The nature of the file is determined by its name.
* You must store manipulation files inside the Tribefire's `config` folder. For example, if your configuration directory is under `user/tribefire/config`, you can store your cortex manipulations as follows:
  ```
  tribefire/
    config/
      csa-priming/
        cortex/
          data.man     
          model.man
        cortex-workbench/
          data.man          
  ```
  Then, you can configure the `TRIBEFIRE_MANIPULATION_PRIMING` as follows:

  `csa-priming/cortex/model.man,csa-priming/cortex/data.man,csa-priming/cortex-workbench/data.man>cortex.wb`

#### Initializer Pre-priming

While the `TRIBEFIRE_MANIPULATION_PRIMING` variable runs the priming after all other initializers, the `TRIBEFIRE_MANIPULATION_PRIMING_PREINIT` variable can force a priming to run before the other initializers. This only takes effect for dynamically deployed CSAs (e.g. custom workbench accesses) and does not apply to `HardwiredAccesses` (e.g.: `cortex`).


### DistributedCollaborativeSmoodAccess

Distributed collaborative SMOOD access, just like the distributed SMOOD access, is an implementation designed for a clustered environment, but this one is an extension of a regular collaborative SMOOD access - it stores its data and resources on a file system.

The reason we use this implementation, rather than a distributed SMOOD access, is that we also want to offer the stage management functionality found in collaborative SMOOD access.

When a distributed collaborative SMOOD access implementation is used in a cluster, each node will have an identical copy of a collaborative SMOOD access (aside from temporarily going out of sync), and a so called shared storage accessible by each of the nodes to coordinate the synchronization of the state.

Similar to the distributed SMOOD access, before every read operation we synchronize the local state with the shared state, so apply any operations that have been stored in the shared storage since the last check. In case of write operations, we use a distributed lock, and all local changes are also stored in the shared storage for all the other nodes to apply the next time they are syncing.

The first two implementations of the shared storage are [JDBC](https://en.wikipedia.org/wiki/Java_Database_Connectivity) - and [etcd](https://coreos.com/etcd) -based.

> For more information, see [DistributedCollaborativeSmoodAccess javadoc](javadoc:com.braintribe.model.access.collaboration.distributed.DistributedCollaborativeSmoodAccess)
