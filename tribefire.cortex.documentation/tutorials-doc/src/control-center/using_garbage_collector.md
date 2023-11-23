# Using the Garbage Collector

A garbage collector is used to delete entities that are no longer needed in a specific access.

## General

The garbage collector functions by walking the entities paths and deletes entity instances not reachable from a root entity. A reachable entity is one that has a link or a relation to another entity.

> If you use garbage collection without setting the Garbage Collection metadata, the garbage collection is not going to work as it will lack the information what to do.

It is possible to configure the garbage collector through the addition of metadata to specific entities. Called Garbage Collection, it contains a property called `kind`, which can be defined with one of three values: `anchor`, `collect` and `hold`.

> For more information, see [Garbage Collection](asset://tribefire.cortex.documentation:concepts-doc/metadata/garbage_collection.md)

> If you wish to apply a general garbage collection behavior for all your instances, set the metadata on the `GenericEntity` type.

If you apply the metadata on the `GenericEntity` level, you must understand that this applies the metadata to all entity instances. However, in some cases you want to change the behavior, like perhaps using `hold` instead of `collect`. We recommend to use the [Use Case Selector](asset://tribefire.cortex.documentation:concepts-doc/metadata/selectors/use_case_selector.md) with a unique string identifying your use case. You can then use the same string to specify the use case when running the garbage collector on an access in the **Use Case** property.

## Test Mode

The garbage collector has a test mode that can be used to analyze the configuration of the garbage collector. The test mode functions in the same manner as normal with the exception that no entities are actually deleted. By default test mode is switched on, meaning that you will have to turn it off for actual entities to be deleted.

## Report

After the completion of the garbage collection a report is produced. It details the amount of root entities found, subset entities, reachable entities, and finally entities that have been deleted. The definition of these terms are as follows:

* subset – the instances belonging to a specific entity type.
* root – entities that are defined as root using the Garbage Collection with anchor is defined.
* reachable – entities discovered via a walk in a root entity's path.
* deleted – entities that have been deleted by Garbage Collection.

## Executing Garbage Collection

In this example, we edit the `GenericEntity` type and add the Garbage Collection metadata to it, so that every entity inherits the metadata.

1. In Control Center, navigate to **Entity Types Query** and search for `GenericEntity`.
2. Add the Garbage Collection metadata to `GenericEntity` and set the `kind` property to `collect`.
3. Assign a Use Case Selector to the Garbage Collection metadata and set the `useCase` property to `collectUseCase`. You might use a different name, if you want. Setting the Use Case Selector allows you to enable this particular metadata when running the garbage collection.
4. Commit your changes and navigate to **Custom Accesses** entry point. Create a new SMOOD access, create a model (without relations to `GenericEntity`) and, using Explorer, create several instances of the entities from that model.
5. In the **Custom Accesses** entry point, select your access and choose **More->Garbage Collection**. Make sure the **Test Mode** checkbox is marked.
6. In the **Use Cases** section, click the **Add** link and type `collectUseCase`. If you used a different name in the Use Case Selector, type in your use case name. Click **Execute**.
7. Download the report and open it. You can see that all instances you created would be deleted.

If you changed the use case name or did not assign any Garbage Collection metadata, the garbage collector would run, but would not produce any results and nothing would be deleted.
