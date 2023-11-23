# Modules Migration Guide

This guide helps you to migrate your project from having Initializers (PluginPriming) and Cartridges to the new world of ["Tribefire Modules"](introduction.md).

This guide assumes that you copied your cartridge and cartridge-initializer artifact and renamed the artifact id in the .pom and .project files correctly to match its new name. In many cases it also makes sense to create the new artifact via jinni templates (`jinni create-module` or `jinni create-initializer-module`) and then copy the cartridge's source code only. Even if this procedure would be similar, it is not described in this document.

In this tutorial the following components - based on the demo setup - are migrated:

* PluginPriming "tribefire-demo-cartridge-initializer"
* Cartridge "tribefire-demo-cartridge"

## Migration: PluginPriming "tribefire-demo-cartridge-initializer"

### 1. Dependencies
#### 1.1 Removals
Remove dependencies:
```xml
<dependency>
    <groupId>com.braintribe.gm</groupId>
    <artifactId>persistence-initializer-support</artifactId>
    <version>${V.com.braintribe.gm}</version>
</dependency>

<dependency>
    <groupId>com.braintribe.gm</groupId>
    <artifactId>persistence-initializer-provided-default-deps</artifactId>
    <version>${V.com.braintribe.gm}</version>
    <scope>provided</scope>
</dependency>
```


#### 1.2 Initializer Support Dependency
This initializer-support artifact replaces former-known persistence-initializer-support:
```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>initializer-support</artifactId>
    <version>${V.tribefire.cortex}</version>
</dependency>
```


#### 1.3 Optional: default-deployables

In case you refer to:
```xml
<dependency>
    <groupId>com.braintribe.gm</groupId>
    <artifactId>default-deployables-initializer-support</artifactId>
    <version>${V.com.braintribe.gm}</version>
</dependency>
```
Adapt to new group:
```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>default-deployables-initializer-support</artifactId>
    <version>${V.tribefire.cortex}</version>
</dependency>
```


#### 1.4 Optional: default-workbench-initializer
In case you refer to:
```xml
<dependency>
    <groupId>tribefire.cortex.assets</groupId>
    <artifactId>tribefire-default-workbench-initializer</artifactId>
    <version>${V.tribefire.cortex.assets}</version>
</dependency>
```
Adapt to:
```xml
<dependency>
    <groupId>tribefire.cortex.assets</groupId>
    <artifactId>default-wb-initializer</artifactId>
    <version>${V.tribefire.cortex.assets}</version>
</dependency>
```
Please adapt classes accordingly. The package structure has changed as well. Sorry for the inconveniences.

Old Name | New Name
--- | ---
WorkbenchContract|DefaultWbContract
WorkbenchMainContract|DefaultWbMainContract
DefaultWorkbenchWireModule|DefaultWbWireModule
DefaultWorkbenchInitializer|DefaultWbInitializer


#### 1.5 Optional: grayish-blue-style-initializer
In case you refer to:
```xml
<dependency>
    <groupId>tribefire.cortex.assets</groupId>
    <artifactId>grayish-blue-style-initializer</artifactId>
    <version>${V.tribefire.cortex.assets}</version>
</dependency>
```
Adapt to:
```xml
<dependency>
    <groupId>tribefire.cortex.assets</groupId>
    <artifactId>darktheme-wb-initializer</artifactId>
    <version>${V.tribefire.cortex.assets}</version>
</dependency>
```
Please adapt classes accordingly. The package structure has changed as well. Sorry for the inconveniences.

Old Name | New Name
--- | ---
GrayishBlueStyleMainContract | DarkthemeWbMainContract
IconContract | DarkthemeWbIconContract
ResourceContract | DarkthemeWbResourceContract
StyleContract | DarkthemeWbStyleContract
UiThemeContract | DarkthemeWbUiThemeContract
GrayishBlueStyleWireModule | DarkthemeWbWireModule
GrayishBlueStyleInitializer | DarkthemeWbInitializer

#### 1.6 scope <provided>
Remove `<scope>provided</scope>` from dependency declarations. As with Modules, all models and further required artifacts are part of the classpath, we can get rid off it.

### 2. model-pom.xml
File `model-pom.xml` is not required anymore. It can be deleted.

### 3. build.xml
File `build.xml` requires some adaption as well. Use `tf-module-ant-script` now (instead of `plugin-ant-script`):  

```xml
<bt:import artifact="com.braintribe.devrock.ant:tf-module-ant-script#1.0" useCase="DEVROCK" />
```
### 4. asset.man
File `asset.man` must be updated to define the Module nature:    
```xml
$nature = (PrimingModule=com.braintribe.model.asset.natures.PrimingModule)()
```

Property `.accessId` and `.additionalAccessIds` got replaced by a collection of accessIds.  
In case you have `.additionalAccessIds` set as well,  summarize it like this:  
```xml
.accessIds = ('cortex', 'additionalAccessId1', 'additionalAccessIdx')
```

### 5. PluginFactory  
`com.braintribe.model.processing.plugin.PluginFactory` can be deleted.

### 6. Initializer Inheritance
Navigate to your initializer class. It most probably inherits from `com.braintribe.gm.persistence.initializer.support.api.SimplifiedWireSupportedInitializer` now.  
Change this to inherit from `tribefire.cortex.initializer.support.impl.AbstractInitializer<DemoInitializerMainContract>`

> Due to clashes on the classpath the package structure of initializer-support classes has changed. Please fix the imports accordingly (e.g. CoreInstancesWireModule, CoreInstancesContract, WiredInitializerContext)

### 7. initialize() method
`AbstractInitializer` brings a new method `initialize(...)`. This method acts as the replacement for methods `initializeModels(...)` and `initializeData(...)`. This means, that there is no separate model/data phase anymore.  
Move the content of both methods into method `initialize(...)`.  
Delete `initializeModels(...)` and `initializeData(...)` afterwards.

### 8. Module reference in Wire
Managed instances where deployables are created do no longer set the cartridge instance via `bean.setCartridge(existingInstances.cartridge())` . Instead, the module needs to be assigned. Create a new instance lookup method in `ExistingInstancesContract` to refer to the module:
```java
@GlobalId("module://" + GROUP_ID + ":demo-module")
Module demoModule();
```
and assign it via `bean.setModule(existingInstances.demoModule())`

### 9. Reference to initializer's wire module
Naming of method `getWireModule()` inside the initializer class needs to be adapted to `getInitializerWireModule()`, its content stays the same.

### 10. packaging-info.yml
Create file `packaging-info.yml` on artifact `/src` root level and point to your initializer's class by specifying its qualified name:
```yaml
wireModule: "tribefire.extension.demo.DemoInitializer"
```
### 11. Package optimization
In order to avoid unnecessary packages you may want to remove subpackage `'persistence'` in your wire package structure (got created in case you are using artifact templates).

> The fully migrated artifact can be found [here](https://github.com/braintribehq/tribefire.extension.demo/tree/master/tribefire-demo-initializer).

## Migration: Cartridge

1. pom.xml
Remove the dependency
```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>tribefire-cartridge-default-deps</artifactId>
</dependency>
```
and replace it with:
```xml
<dependency>
    <groupId>tribefire.cortex</groupId>
    <artifactId>tribefire-web-module-api</artifactId>
	<version>${V.tribefire.cortex}</version>
</dependency>
```

2. adapt the build.xml in the same way as you did with the initializer above:
```xml
<bt:import artifact="com.braintribe.devrock.ant:tf-module-ant-script#1.0" useCase="DEVROCK" />
```

3. also the nature in the asset.man file is adapted in a similar fashion:
```xml
$nature = (TribefireModule=com.braintribe.model.asset.natures.TribefireModule)()
```
If there is a line in there that starts with `externalId` - remove it.

4. If there are any resources in the `context/WEB-INF` folder, create a folder named `resources` in the artifact root and move them there, after which you can delete the `context` folder. The resources that are meant to be public, i.e. also accesible by users and not just in the module context, should be placed in `resources/public` folder. So if you for example have some thumbnails in `context/WEB-INF/Resources/Thumbnails`, you would move them to `resources/public/Thumbnails` folder. You will also have to adapt references to these resources on source level. Read about that in step 7.

5. Time for adaptions on source level: In the package `com.braintribe.cartridge.extension.wire.space` you will find the class `CustomCartridgeSpace` which was the main entrypoint to configure your cartridge. Most of its content can be reused. Create a new class in a package of your choice. (Note that the last component of that package name must be `space`, like `my.product.feature.x.space`):
```
@Managed
public class TribefireDemoModuleSpace implements TribefireModuleContract {
```

  Copy the content of the `CustomCartridgeSpace` class body into that new class except the `onLoaded` and `customization` methods.

  find the following method
```
@Managed
public DenotationTypeBindingsConfig extensions() {
	DenotationTypeBindingsConfig bean = new DenotationTypeBindingsConfig();
```
and change it to
```
public void bindDeployables(DenotationBindingBuilder bindings) {
```

  As you see you don't need to create your binder instance yourself anymore but you get it via the method parameter. So you can also remove the final `return` statement. You can use the new `DenotationBindingBuilder` the same way you used the old `DenotationTypeBindingsConfig` - the API is the same.

6. The available wire contracts changed. Most of them kept the same name but changed their package. Unfortunately many of the old contracts are still available on the classpath so you need to be a bit careful to not accidentally use them (which will not work).

  The cartridge related contracts were in a subpackage of `com.braintribe.cartridge.extension`. If you imported one of those, see if you can find a contract with the same name in the  [`tribefire.module.wire.contract`](https://github.com/braintribehq/tribefire.cortex/tree/master/tribefire-web-module-api/src/tribefire/module/wire/contract) package of the `tribefire.cortex:tribefire-web-module-api`.

  If not, see if one of the new contracts in that package provides the desired functionality. A few commonly used methods follow:
  * `CartridgeClientContract.persistenceGmSessionFactory()` is now `SystemUserRelatedContract.sessionFactory()`
  * `CartridgeClientContract.serviceRequestEvaluator()` is now `RequestProcessing.evaluator()`

  There is also the new `tribefire.module.wire.contract.TribefireWebPlatformContract` which lets you conveniently browse to the available contracts via member methods.

7. If there were any resources migrated in step 4 you will need to adapt resource references on the source level as well. The resources that were previously accesible using the `webInf(String)` method from `ResourcesContract` are now available using methods from two different contracts. To access public resources we use the `publicResources(String)` method from `tribefire.module.wire.contract.PlatformResourcesContract` and for module(private) resources we use the `resource(String)` method from `tribefire.module.wire.contract.ModuleResourcesContract`. If we use the step 4 Thumbnails folder example, somewhere in your wire configuration you will find something like: `resources.webInf("Resources/Thumbnails/home-icon.png")`. Change it to `platformResources.publicResources("Thumbnails/home-icon.png")`.

8. Finally you need to create a wire module. Create a new class:
```
public enum TribefireDemoModuleWireModule implements StandardTribefireModuleWireModule {

	INSTANCE;

	@Override
	public Class<? extends TribefireModuleContract> moduleSpaceClass() {
		return TribefireDemoModuleSpace.class;
	}

}
```

  the `moduleSpaceClass()` method needs to return the class of your module's main wire space you created in step 5.

9. Possibly you will have references to the cartridge instance somewhere in your module. Make sure to remove them. It is not necessary to replace them with references to your new module.

10. Like for the initializer create file `packaging-info.yml` on artifact `/src` root level and point to your wire module's class (the one you created in step 8) by specifying its qualified name:
```yaml
wireModule: "tribefire.extension.demo.DemoModuleWireModule"
```

11. This should do the trick. If you want to try out your new module you will need to [create a new aggregator](application-structure.html#project) asset. Take care to remove the old cartridge from all dependencies and dependencies of dependencies so that it won't be pulled into your new setup. Other not (yet) migrated cartridges can remain part of your setup, this will not be an issue.
