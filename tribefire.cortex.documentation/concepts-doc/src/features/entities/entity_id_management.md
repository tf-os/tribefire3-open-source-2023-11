# Entity ID Management

To persist entities and reference them beyond runtime, persistence identification is needed. This identification is based on ID properties.

## General

When your entity extends `GenericEntity`, the ID property is inherited from the supertype and you do not need to explicitly mark a property if you want to use it as an ID.  

## Accessing ID Property

Although the `ID` property can be type-individual, you can access it in a normalized way using one of the following methods:

* `<T> T getId();`
* `void setId(Object id);`

## Partition Identification

Data supply chain can combine separate data storages into a virtual data storage, in which certain types can come from either one of the separate data storages. The type `com.braintribe.model.generic.GenericEntity` introduces the `partition` property of type string to distinguish between the separate storages if needed.

## Global Identification

Persistence IDs are only guaranteed to be unique within a specific storage. Also, migrating data from one storage to another leads to the necessity of id translations. For stable identification throughout those migrations, the type `com.braintribe.model.generic.GenericEntity` introduces the `globalId` property of type string to globally identify entity instances over time and across storages.

The `globalId` property is especially interesting for the cortex storage which frequently exchanges data for collaboration purposes (e.g. model exchange, release package updates). In case of the cortex storage, a **random** UUID is automatically assigned to any instance being persisted not yet having an assigned `globalId`. That ensures a UUID is present for all instances. Other storages may ignore the `globalId` property.

The cortex storage builds its persistence with incremental manipulation stacks based on `globalId` references in order to stabilize it for collaboration. The `globalId` property is generally either a random UUID or a URI with a scheme so it may come from manual assignment with semantic information (e.g. `GmProperty.globalId =  “property:com.braintribe.model.generic.GenericEntity/globalId”`).
