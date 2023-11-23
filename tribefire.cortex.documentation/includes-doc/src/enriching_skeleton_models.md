
We distinguish two basic model types: skeleton models and enriching models.

## Skeleton Model

A skeleton model defines custom types (`GmEntityType`, `GmEnumType`) and may depend on other models. If no other dependency is needed, it needs to at least depend on the `root-model`. A skeleton model must not declare type overrides.

> For more information about overrides, see [Metamodel](asset://tribefire.cortex.documentation:concepts-doc/features/platform-models/metamodel.md#overrides).

If other dependencies are needed, each dependency is a skeleton model as well.

Currently a skeleton model is represented via:

* Java interface class files
* Java enum class files
* Java annotations used in those class files

Metadata can be applied on entity types (and related properties), enum types (and related constants) and property overrides reachable by `GmMetaModel.types`. Essentially this means that a skeleton model should only contain annotatable essential metadata.

## Enriching Model

An enriching model on the other hand does not declare custom types (empty types collection). It may depend on other models which can be either a skeleton as well as another enriching model.

An enriching model may declare type overrides and those can declare any type of metadata on it. Unlike the skeleton model, an enriching model is not restricted to annotatable essential meta data.

Enriching models are represented by incremental changes to the cortex - `ManipulationPriming` in form of `.man` files (e.g. changes done via the metadata editor) or code primings in the form of initializers (e.g. Java).

An enriching model therefore enriches your skeleton model with those data that would get lost in the asset roundtrip (creating an asset, publishing it in an asset store, depending on it) on a skeleton model otherwise as, for example, custom meta data is not represented as a Java annotation and therefore would not be present in a deployed asset `.jar` file.

In a runtime environment as well as in current setups, this clear separation is not always into place. There, skeleton models hold non-essential metadata as well. This should be avoided as it may cause information loss if not handled properly.
