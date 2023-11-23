# Garbage Collection

You can use this metadata to influence the behavior of the garbage collector.

Metadata Property Name  | Type Signature  
------- | -----------
`GarbageCollection` | `com.braintribe.model.meta.data.cleanupGarbageCollection.java`

## General

Using this metadata, you can configure how the garbage collector behaves for the instances of entity types this metadata is assigned to. You can attach the `GarbageCollection` metadata on the entity level.

> For information about using the garbage collector, see [Using the Garbage Collector](asset://tribefire.cortex.documentation:tutorials-doc/control-center/using_garbage_collector.md)

There is little configuration for this metadata - the most important is the `kind` property:

Value |  Description
------| -------
`anchor` | Considered root entities, it marks the point where garbage collector starts its reachability walks. Anchor entities will never be collected by the garbage collection.
`hold` | Defines entities that should never be collected by the garbage collector.
`collect` | Removes all marked entities, unless they are reachable (directly or indirectly) from one of the root entities.
