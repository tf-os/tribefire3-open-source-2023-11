# Adding Metadata

You can add metadata to models in the following ways:

* via the graphical [metadata editor](../features/ui-clients/ui_elements.md#metadata-editor)
* via the `ModelMetaDataEditor` interface in an initializer
* via annotations (e.g. `@Mandatory`)

In general, annotations are more convenient for a coder, but they only provide a limited subset of the full modelling capabilities, while the metadata editor and the cartridge initializer both have access to the full potential of modelling.

Your choice of the preferred method of adding metadata is, however, not that simple. It is important to understand where the metadata is applied before to actually apply it.

## Metadata and Model Types

There are a lot of factors that influence decision of where to place certain metadata. The semantical differentiation in skeleton and enriching models is a good starting point for you to decide.

[](asset://tribefire.cortex.documentation:includes-doc/enriching_skeleton_models.md?INCLUDE)

Consider the following when adding metadata:

* What is the real skeleton of my model, so which metadata should be brought to anybody who depends on my model?
* Is this metadata annotatable essential metadata, can it stand the whole roundtrip (creating an asset, publishing it in an asset store, depending on it)?
* Which kind of metadata is use-case driven?  

## Adding Metadata via `ModelMetaDataEditor`

The `ModelMetaDataEditor`  is an interface used for adding metadata to `GmMetaModel` instances. It takes care of navigation through the underlying GmMetaModel hierarchies and creating the appropriate model element overrides.

> For more information, see [BasicModelMetaDataEditor](javadoc:com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor).

A common place of adding metadata programmatically are initializers, which add information to a collaborative SMOOD persistence. As models and their metadata reside in the cortex access, a respective session to it is required.

In the example below, we are adding an instance of the  `Confidential` metadata defined in the `initializer` Wire space.

> For more information about wire spaces, see [Wire in Detail](asset://tribefire.cortex.documentation:concepts-doc/features/wire/wire_in_detail.html#wirespace).

The declaration of the `Confidential` metadata in the Wire space may look as follows:

```java
@Managed
@Override
public MetaData confidential() {
	Confidential md = create(Confidential.T);
	return md;
}
```

Note that metadata might already exist and therefore just be referenced via a Wire Contract.

> For more information about the `Confidential` metadata, see [Confidential](asset://tribefire.cortex.documentation:concepts-doc/metadata/prompt/confidential.md).

To add the metadata:

1. Create a new instance of `BasicModelMetaDataEditor` and provide the model you want to add the metadata to as the argument:

```java
ModelMetaDataEditor editor = new BasicModelMetaDataEditor(modelYouWantToAddMetadataTo);
```

2. Use the proper API method to add the metadata to the entity or property. In this example, we're adding metadata to the `PlatformAssetExtensionRequest` entity type:

```java
   editor.onEntityType(PlatformAssetExtensionRequest.T).addMetaData(initializer.confidential());
```

> If you have the Demo cartridge in your setup, inspect the `DemoCartridgeInitializer` class to see how metadata is added in a real-world scenario.