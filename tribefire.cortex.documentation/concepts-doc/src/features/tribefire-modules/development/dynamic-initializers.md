# Dynamic CSA Initializers

Dynamic CSA Initializer is effectively a custom initializer type registered from a module which processes one or more files as its input.

## What is a Dynamic Initializer?

Typically, you can bind an initializer in a module directly, and this initializer is executed exactly once to create/modify data in your CSA. However, sometimes you want to apply a certain initializer logic many times, with just slightly different parameters.

### Example:

Let's say you want to create a new Access and the parameters are its type, its identifiers (name, externalId) and the names of it's data and service model.

### Heavy solution with multiple modules:

One option would be to create a library which can create such an access and then for every concrete case create a module with an initializer that calls this library. This obviously feels cumbersome as you need an entire module pretty much just to provide a few strings as input to the actual initializer logic.

## Lightweight solution with a single module

It is much more elegant to register a custom initializer type and for every concrete case simply create a lightweight asset which contains a file with just the required information (access type, access name, ...), alongside the information which expert should process this file.

## How to use Dynamic Initializer?

So, as stated above, using this mechanism needs two parts:

* One module which binds the initializer expert.
* One or more input assets for this expert.

### Binding the initializer expert

The expert is simply bound from a module in the [bindInitializers](javadoc:tribefire.module.wire.contract.TribefireModuleContract.bindInitializers) method in the following way (using the model-creating example):

> I suggest to use a naming convention where the name of this module ends with `-configuring-module`, e.g. `access-configuring-module`


```java
public class ModelCreatorConfiguringModuleSpace implements TribefireModuleContract {

    @Override
    public void bindInitializers(InitializerBindingBuilder bindings) {
        bindings.bindDynamicInitializerFactory(this::newAccessCreatingInitializer);
    }

    private DataInitializer newAccessCreatingInitializer(File inputFolder){
        return ctx -> createAccess(ctx, inputFolder);
    }

    private void createAccess(PersistenceInitializationContext context, File inputFolder) {
        //  Resolve the parameters from the input foler
        ...

        // Lookup the data and service models
        ...

        // Cretea new access of given type with correct properties
        ...
    }

    ...

}
```

This might be slightly confusing, so let's figure it out.

We need a new DataInitializer instance for every input folder, which contains a single input for our expert.  Thus we simply register a function which for a given File (inputFolder) returns a DataInitializer, and the structure above is the most simple way how to implement this.

### Providing an input for our expert

Now for every case where we want to apply out expert we need to create an asset with nature [DynamicInitializerInput](javadoc:com.braintribe.model.asset.natures.DynamicInitializerInput).

> I suggest to use a naming convention where the name of this asset ends with `-configuration`, e.g. `my-access-configuration`

This asset has the following file structure:

```
my-accesss-creation-configuration/
    resources/
        access.yml
    asset.man
    build.xml
    pom.xml
```

This is what the files would look like:

`access.yml`
This is a custom format that can be read by the expert bound in the module. Typically this would be a YML serialization of an entity.

`asset.man`:

```
$nature = !com.braintribe.model.asset.natures.DynamicInitializerInput()
.accessIds=['cortex']
```

> Note the value is `cortex` just in this example, as creating an access is something one would typically do in `cortex`.

`build.xml`:
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>

<project xmlns:artifact="antlib:org.apache.maven.artifact.ant" xmlns:bt="antlib:com.braintribe.build.ant.tasks" basedir="." default="install">
	<bt:import artifact="com.braintribe.devrock.ant:resources-ant-script#1.0" useCase="DEVROCK"/>
</project>
```

`pom.xml`

```xml
	<dependencies>
        <dependency>
            <groupId>my.group</groupId>
            <artifactId>access-creating-configuring-module</artifactId>
            <version>1.0</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
        ...
	</dependencies>

```

**IMPORTANT:** `DynamicInitializerInput` must contain exactly one module dependency and it has to be the module which binds the corresponding expert. This information is needed to associated the input folder with the correct initializer expert. Other modules might be depended as well, if desired, but not as direct dependencies.

**NOTE:** Optionally it makes sense for a configuration asset like this one to have further dependencies, e.g. the models which are referenced as dependencies of the newly created model.


### Expected result in your projection

Every `Dynamic Initializer Input` asset is projected as a stage in every one of it's affected accesses. This is, for our access-creating example, the expected outcome in the data folder:

```
cortex/
    data/
        my.group_my-access-configuration#1.0/
            access.yml
        config.json
```

with the `config.json` containing an entry like this:

```json
{"_type": "com.braintribe.model.csa.DynamicInitializer", "_id": "5",
   "moduleName": "my.group:access-configuring-module",
   "name": "my.group:my-access-configuration#1.0"
  },
```

CSA uses this information to feed the correct input folder to the initializer factory from the correct module.