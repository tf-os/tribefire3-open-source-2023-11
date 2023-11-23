# Access

## General
You can make changes to entity instances in a model using an access (sometimes also referred to as **integration access**). There are two basic types of accesses:

Access    | Description  
------- | -----------
System Access | Used to manipulate entity instances in base models, like Cortex, Authentication and Authorization, and Workbench.   
Custom Access | Used to manipulate entity instances in custom models.

>Every custom access extends a `HibernateAccess`, which, in turn, extends an `IncrementalAccess`. Even though the two are the same from a technical point of view, we use this naming convention for clarity.

## Incremental Access
`IncrementalAccess` is an interface which can read and update individual entities. This is the main interface which all custom accesses **must** implement.

### IncrementalAccess Properties

Property    | Description  | Type
------- | ----------- | ------
aspectConfiguration | Custom aspect for this access. By default, there is no aspect assigned to an access. Navigate to **More -> Setup Aspect** if you want to create a default aspect. |  AspectConfiguration
resourceFolderPath | Path to where the resources are uploaded to. |  `String`
autodeploy | Automatic deployment indicator. If you assign true to this property, your access is deployed automatically. |  `Boolean`
cartridge | Cartridge which contains the access. | `Cartridge`
globalID | String id used to globally identify entity instances. | `String`
partition | String describing which access this entity comes from.  Useful when dealing with the same entities in different accesses in a smart access. | `String`
metaData | Available metadata. For information on available metadata, see [metadata](asset://com.braintribe.internal.ci-playground:metadata-doc/general_metadata_properties.md) | `Set<MetaData>`
serviceModel | A service model associated with this access. For more information, see [denotation-driven service architecture](asset://com.braintribe.internal.ci-playground:fundamentals-doc/ddsa.md) | `GmMetaModel`
id | Automatically generated Id assigned after the first time the access is committed. | `long`
name | The name of your access. | `LocalizedString`
externalId | Id used to refer to the access, for example when displaying the access in the cog icon next to the log in screen or when building an app that this access must be connected to. | `String`
deploymentStatus | Id used to refer to the access, for example when displaying the access in the cog icon next to the log in screen or when building an app that this access must be connected to. | `DeploymentStatus(enum)`
metaModel | The model this access is associated with. | `GmMetaModel`
simulated | Simulation mode status indicator. | `Boolean`
workbenchAccess | The workbench access associated with this access. | `SmoodAccess`

## Database Structure and Accesses

Accesses interact with databases and can influence their structure. When you configure a database as a source of your data, you see the exact same data represented in the database (in the schema, table, rows, and columns, for example) and in tribefire (as 'bubbles' in tribefire Modeler, for example).

tribefire allows you to specifically influence how a data structure is represented in a database. This is done on an entity level by using proper Hibernate mapping metadata.
