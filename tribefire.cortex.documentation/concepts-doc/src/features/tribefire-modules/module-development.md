# Developing Tribefire modules

Let's now have a look at how to integrate our code and data into our `Tribefire` application in a `Tribefire module`.

Let's start with examining the structure of an empty module first, generated for our example by *`jinni create-module my-applicationx:application-x-module`* command:

```
application-x-module/
    src/
        my/
            application_x/
                wire/
                    space/
                        ApplicationXModuleSpace.java
                ApplicationXModuleWireModule.java
        packaging-info.yml
    asset.man
    pom.xml
```

Only 3 of these files are interesting for us now (for more information about the whole structure [click here](module-structure.md)):

***ApplicationXModuleSpace.java*** is an implementation of [TribefireModuleContract](#TribefireModuleContract), which is the interface for binding our code and data to our application.

***asset.man*** describes the [nature of our asset](../platform_assets.md#asset-natures) ([TribefireModule](javadoc:com.braintribe.model.asset.natures.TribefireModule)), which most importantly declares [which accesses our module initializes](#bindInitializers).

***pom.xml***, as always, is where we declare our dependencies. An empty module depends only on `com.braintribe.gm:tribefire-module-api`.

## TribefireModuleContract

In our example, we'll see that the `ApplicationXModuleSpace` class is an empty implementation of [TribefireModuleContract](javadoc:tribefire.module.wire.contract.TribefireModuleContract). It looks something like this:

```java
public class ApplicationXModuleSpace implements TribefireModuleContract {
    @Import
    private TribefirePlatformContract tfContainer;

    @Override
    public void bindHardwired() {
    
    }

    public void bindWireContracts(WireContractBindingBuilder bindings) {
    
    }

    @Override
    public void bindInitializers(InitializerBindingBuilder bindings) {
    
    }

    @Override
    public void bindDeployables(DenotationBindingBuilder bindings) {
    
    }
}
```

As we can see, it offers 4 different methods for binding our code and data. Use:

[bindHardwired()](#bindHardwired) to configure hardwired components (deployables).

[bindContracts()](#bindContracts) to define an extension for other modules to use.

[bindInitializers()](#bindInitializers) to add configuration data to our application, which is done by registering [initializers](javadoc:com.braintribe.model.processing.session.api.collaboration.DataInitializer) for one or more [CollaborativeSmoodAccesses](../smood.md#collaborativesmoodaccess). You can also add [custom initializer experts](development/dynamic-initializers.md).

[bindDeployables()](#bindDeployables) to register experts responsible for dynamic deployment of given components (deployables)


### TribefirePlatformContract
`TribefirePlatformContract` offers access to platform components relevant for configuring our custom components, like e.g. session related suppliers, component binders (deployment) or a request evaluator.

Note that the contract itself is really just a "container" that binds various contracts together, and all of these contracts can be imported directly. This contract is simply a starting point from which we can navigate to all the basic (there are [additional contracts](#additional-contracts)) components the platform offers us.

> For more information, see [TribefirePlatformContract javadoc](javadoc:tribefire.module.wire.contract.TribefirePlatformContract)

### bindHardwired()
This method allows us to directly configure [hardwired deployables](javadoc:com.braintribe.model.deployment.HardwiredDeployable), i.e. Tribefire components of known types where  the platform itself knows how to use them.

For example, this is how we would bind a very simple [service processor](../service-model/service_processor.md) which for a `SayHello` request simply returns the string "*Hello!*":

```java
@Import
private HardwiredDeployablesContract hardwiredDeployables;

@Override
public void bindHardwired() {
    hardwiredDeployables.bindOnServiceDomain("talk", "Talking Domain")
            .serviceProcessor("talk.hello", "Hello Processor", SayHello.T, (ctx, request) -> "Hello!");
}
```

`SayHello` might look like this:

```java
public interface SayHello extends DomainRequest {
	final EntityType<SayHello> T = EntityTypes.T(SayHello.class);

	@Override
	EvalContext<String> eval(Evaluator<ServiceRequest> evaluator);
}
```

and is declared in a separate model (say `application-x-service-model`), which we have to add as a dependency of our module of course.

### bindContracts()
This method **allows a module to offer its own "component"s for other modules** to build upon, allowing direct extension of a module by another one, bypassing anything Tribefire-specific.

Technically, the extension mechanism uses Wire Contracts, where a contract serves as a handle of the exposed components. One module would typically bind its own implementation of this contract, while other modules would import this contract (just like they import the TribefirePlatformContract), thus gaining access to the implementation provided by the first module.

As an example, let's consider the following contract, defined in a separate [GM API](application-structure.md#gm-apis) artifact (see below why):

```java
public interface EncryptionContract extends WireSpace {

	BiFunction<String, String, String> encryptor();
	
	BiFunction<String, String, String> decryptor();
}
```

Let's say we have a `custom-cipher-module` which implements this contract in the following way (the actual CustomCipher implementation not being important):

```java
@Managed
public class EncryptionSpace extends EncryptionContract {

    @Managed
    public BiFunction<String, String, String> encryptor() {
        return CustomCipher::encrypt;
    }
	
    @Managed
    public BiFunction<String, String, String> decryptor() {
        return CustomCipher::decrypt;
    }
}
```

This module also binds the contract, e.g. like this:
```java
@Override
public void bindWireContracts(WireContractBindingBuilder bindings) {
    // TODO improve example, this isn't proper Wire code
    bindings.bind(EncryptionContract.class, new EncryptionSpace());
}
```

And now any subsequently loaded module which imports `EncryptionContract` gets the `EncryptionSpace` instance bound here.

Couple of notes:
* For modules to be loaded in the correct order, **the contract importing module must declare the contract implementer as its asset dependency**.

* **There can be only one module which binds a space for given contract**. If an attempt is made to bind an already bound contract, exception is thrown at runtime (on bootstrap) and the application would not start.

* **The contract must be located on the [main classpath](module-compatibility.md#multiple-classpaths)**, so that the interface is the exact same runtime class for all the modules. This can only be guaranteed if the interface is declared in a [GM API](application-structure.md#gm-apis) artifact or its declaring artifact is placed into a [Platform library](application-structure.md#platform-libraries).

### bindInitializers()

This method is used to add configuration data to our application, via **binding [initializers](javadoc:com.braintribe.model.processing.session.api.collaboration.DataInitializer) for `cortex` and other collaborative accesses**. This is achieved simply by calling one of the bind methods of given [InitializerBindingBuilder](javadoc:tribefire.module.api.InitializerBindingBuilder) with our initializer as argument , e.g.:

```java
@Override
public void bindInitializers(InitializerBindingBuilder bindings) {
	bindings.bind("workbench", ctx -> adjustCortexWorkbench(ctx.getSession()));
}
```

**NOTE that when binding initializers to an access other than `cortex`, the name of the access also has to be stated in the `asset.man` file of our module.**

> This is necessary because this information is needed when setting up our application, where for each access we write the names of the modules that initialize it. So if we forget to put the entry in the "asset.man" file, our initializer(s) will not be seen from the access and thus will never be executed.

**Example:** when initializing "workbench" access, the `asset.man` file would look like this:

```
$natureType = com.braintribe.model.asset.natures.TribefireModule
.accessId='workbench'
```

In case our module is initializing other access too, it would be:

```
$natureType = com.braintribe.model.asset.natures.TribefireModule
.accessId='workbench'
.additionalAccessIds=('cortex.wb', 'setup.wb')
```

> It doesn't matter which access is referenced by which property, we could even ignore the `accessId` property and simply put all three values in the `additionalAccessIds` set.

You can also add [custom initializer experts](development/dynamic-initializers.md) at this stage.

### bindDeployables()

With this method we **register a deployment expert for a custom deployable**, i.e. to provide an information how to deploy a custom deployable type.. 

If you are not familiar with these terms, please [read the deployment documentation](../deployment.md).

Following is a comprehensive example of a module which binds an [AccessRequestProcessor](javadoc:com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor) and a custom [access](javadoc:com.braintribe.model.access.IncrementalAccess).

> This example is taken from the current version of `demo module`, just with slightly modified names of the actual implementations for readability.

```java
@Managed
public class MyModuleSpace implements TribefireModuleContract {
	@Import
	private TribefirePlatformContract tfContainer;

	@Import
	private CommonBindersContract binders;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(GetEmployeesByGenderProcessor.T) //
				.component(binders.accessRequestProcessor()) //
				.expertSupplier(this::employeesByGenderProcessor);

		bindings.bind(DemoAccess.T) //
				.component(binders.incrementalAccess()) //
				.expertFactory(this::demoAccess);
	}

	@Managed
	private GetEmployeesByGenderProcessorImpl employeesByGenderProcessor() {
		return new GetEmployeesByGenderProcessorImpl();
	}

	@Managed
	public DemoAccessImpl demoAccess(ExpertContext<DemoAccess> context) {
		DemoAccessImpl bean = new DemoAccessImpl();

		DemoAccess deployable = context.getDeployable();

		bean.setMetaModelProvider(deployable::getMetaModel);
		bean.setAccessId(deployable.getExternalId());
		return bean;
	}
}
```

NOTE we have imported `CommonBindersContract`, which is also reachable via `tfContainer.deployment().binders()`. This contract contains the component binders for the most common tribefire components. For more component binders [see ClusterBindersContract](#ClusterBindersContract).


## Additional contracts
NOTE: TODO UNCLEAR YET

Beyond the [Wire Contracts](../wire/wire_in_detail.md#contracts) available via `TribefirePlatformContract` and those from other modules, there are also additional contracts available from the platform itself, but they require an extra dependency in your module. 


> It is not clear to me at this point how this will work. Maybe these contracts will be platform specific, und thus also a module will only be compatible with certain platforms? Otherwise I see no point in having the contracts outside of `module-api` when they have to be part of every platform anyway.

### ClusterBindersContract
This contract offers additional component binders, for components relevant in a clustered environment, which at this point are:

* messaging
* lockingManager
* leadershipManager
* dcsaSharedStorage

> For more information, see [ClusterBindersContract javadoc](javadoc:tribefire.module.wire.contract.ClusterBindersContract)


Dependency:
```xml
<dependency>
	<groupId>tribefire.cortex</groupId>
	<artifactId>cluster-module-api</artifactId>
	<version>${V.tribefire.cortex}</version>
</dependency>
```
