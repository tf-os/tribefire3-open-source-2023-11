# Hydrux Application Configuration

Now that we know [how to define a custom component's module](./hx-components.md), let's look at how we can configure a `Hydrux` application. We'll continue with the example using `DemoView` (see the previous link) , which will be the main (and only) view of our application.

## Context

### Components recap

So far we have these artifacts:
* `demo-hx-deployment-model` with `DemoView` component denotation type
* `demo-hx-module` with `TypeScript` implementation of `DemoView`, fulfilling the `IHxView` interface

### Service Domain

A `Hydrux Application` is always **configured on** the **service model of** some **Service Domain**. Let's simply assume such a domain exists, with externalId `demo-domain` a model called `tribefire.demo.hx:demo-service-model`.

## Tribefire Initializer

We need a **tribefire initializer** artifact, say `demo-hx-app-initializer`.

This initializer does the following:
* Adds `demo-hx-deployment-model` to the **cortex model**
* Creates an `HxApplication` meta-data, saying the main view is `DemoView`.
* Sets this `HxApplication` instance on the `tribefire.demo.hx:demo-service-model` denotation.

In fact, we'll create two different `HxApplication` meta-data. Our `DemoView` can be configured in **dark-mode**, so we'll make that the **light one default** and **dark-one optional**. We **achieve this by** specifying the meta-data for the **light one with no selector and lower priority** and the **dark one with a selector and higher (default) priority**.

### URL

Our application can be accessed via the following URLs:

Light (default):
```plain
https://${tribefire-services}/hydrux/demo-domain
```

Dark:
```plain
https://${tribefire-services}/hydrux/demo-domain/dark
```

### Individual Files

#### File system structure

```filesystem
src/tribefire/demo/hx/initializer/
    wire/
        contract/
            ExistingInstancesContract.java
            DemoHxAppInitializerContract.java
        space/
            DemoHxAppInitializerSpace
        DemoHxAppInitializerWireModule.java
    DemoHxAppInitializer.java
asset.man
pom.xml
```
> NOTE that this is quite simplified, compared to what `Jinni` creates with `jinni create-initializer demo-hx-app-initializer`.

#### ExistingInstancesContract

As for existing instances, we need (configured) `demo-service-model` and `demo-hx-module`.

```java
@InstanceLookup(lookupOnly = true)
public interface ExistingInstancesContract extends WireSpace {

	String GROUP_ID = "tribefire.demo.hx";

    // this is the configured model, which has a dependency of the raw "demo-service-model" from classpath
	@GlobalId("model:" + GROUP_ID + ":configured-demo-service-model")
	GmMetaModel configuredDemoServiceModel();

	@GlobalId("js-ux-module://" + GROUP_ID + ":demo-hx-module")
	UxModule demoHxModule();

}
```

> Note the configured model is a convention, when we have an `xyz-model` on a classpath, we do not configure meta-data directly on it. Instead, we create a wrapper model called `configured-xyz-model`. This model is then assigned to say a `Service Domain`, and it also contains meta-data specific to that `Service Domain` use-case.

#### DemoHxAppInitializerContract

```java
public interface DemoHxAppInitializerContract extends WireSpace {
	void configureDemoHxApp();
}
```

#### DemoHxAppInitializerSpace

```java
@Managed
public class DemoHxAppInitializerSpace extends AbstractInitializerSpace implements DemoHxAppInitializerContract {

	@Import
	private ExistingInstancesContract existingInstances;

	@Managed
	@Override
	public void configuredHxDemoApp() {
		GmMetaModel serviceModel =  existingInstances.configuredDemoServiceModel();

		BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(serviceModel).withSession(session()).done();
		mdEditor.addModelMetaData(
            lightApp(),
            darkApp()
        );
	}

	@Managed
	private HxApplication lightApp() {
		HxApplication bean = create(HxApplication.T);
		bean.setTitle("Light Demo Hx Application");
		bean.setApplicationId("light-demo-hx-app");
		bean.setDefaultScope(hxDefaultScope());
		bean.setView(lightDemoView());
		bean.setConflictPriority(-100d);

		return bean;
	}

	@Managed
	private DemoView lightDemoView() {
		DemoView bean = create(DemoView.T);
		bean.setDark(false);
		bean.setModule(existingInstances.demoHxModule());

		return bean;
	}

	@Managed
	private HxApplication darkApp() {
		HxApplication bean = create(HxApplication.T);
		bean.setTitle("Dark Demo Hx Application");
		bean.setApplicationId("dark-demo-hx-app");
		bean.setDefaultScope(hxDefaultScope());
		bean.setView(darkDemoView());
		bean.setSelector(darkUseCase());

		return bean;
	}

	@Managed
	private DemoView darkDemoView() {
		DemoView bean = create(DemoView.T);
		bean.setDark(true);
		bean.setModule(existingInstances.demoHxModule());

		return bean;
	}

	@Managed
	private UseCaseSelector darkUseCase() {
		UseCaseSelector bean = create(UseCaseSelector.T);
		bean.setUseCase("dark");

		return bean;
	}

	@Managed
	private HxScope hxDefaultScope() {
		HxScope bean = create(HxScope.T);
		return bean;
	}
```

#### DemoHxAppInitializerWireModule
        
```java
public enum DemoHxAppInitializerWireModule implements WireTerminalModule<DemoHxAppInitializerContract> {
	INSTANCE;
}
```

#### DemoHxAppInitializer

```java
public class DemoHxAppInitializer extends AbstractInitializer<DemoHxAppInitializerContract> {

	@Override
	public WireTerminalModule<DemoHxAppInitializerContract> getInitializerWireModule() {
		return DemoHxAppInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize( //
			PersistenceInitializationContext context, //
			WiredInitializerContext<DemoHxAppInitializerContract> initializerContext, //
			DemoHxAppInitializerContract demoInitializer) {

		CortexInitializerTools.addToCortexModel(context.getSession(), Model.modelGlobalId("tribefire.demo.hx:demo-hx-deployment-model"));

		demoInitializer.configuredHxDemoApp();
	}
}
```

#### pom.xml

For the record, this is what our dependencies should look like:

```xml
    <dependencies>
        <dependency>
            <groupId>tribefire.cortex</groupId>
            <artifactId>initializer-support</artifactId>
            <version>${V.tribefire.cortex}</version>
        </dependency>
        <dependency>
            <groupId>tribefire.demo.hx</groupId>
            <artifactId>demo-hx-deployment-model</artifactId>
            <version>${V.tribefire.demo.hx}</version>
            <?tag asset?>
        </dependency>
        <dependency>
            <groupId>tribefire.demo.hx</groupId>
            <artifactId>demo-service-model</artifactId>
            <version>${V.tribefire.demo.hx}</version>
            <?tag asset?>
        </dependency>

		<!-- Asset only deps -->
        <dependency>
            <groupId>tribefire.extension.hydrux</groupId>
            <artifactId>hydrux-module</artifactId>
            <version>${V.tribefire.extension.hydrux}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
        <dependency>
            <groupId>tribefire.demo.hx</groupId>
            <artifactId>demo-hx-module</artifactId>
            <version>${V.tribefire.demo.hx}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
    </dependencies>
```
