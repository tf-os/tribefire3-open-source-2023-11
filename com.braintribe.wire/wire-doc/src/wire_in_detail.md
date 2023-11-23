# Wire in Detail

Wire is an Java extension based on annotations and byte code instrumentation which supports the IOC pattern.

## General

The idea of Inversion Of Control (IOC) is to decouple the code that sets up and binds software components (classes) from the implementation code of that components. Built using a small library, containing only one dependency, Wire uses Java as a flexible and direct way of using dependency injection.

## Prerequisites for Using Wire

* import the following artifacts: `com.braintribe.ioc.Wire#1.0`

## Components

Wire consists of the following structural elements:

* [`ManagedInstance`](#managedinstance)
* [`WireSpace`](#wirespace)
* [`Contract`](#contract)
* [`WireContext`](#wirecontext)
* [`WireScope`](#wirescope)
* [`WireModule`](#wiremodule)

### ManagedInstance

A managed instance is a Java class instance that is managed so that it has a lifecycle within a `WireScope`. Within that scope, instances can be shared and have cyclic references. Based on the scope, `LifecycleListeners` can react on the instantiation or disposal of an instance in a cross-cutting way.

A managed instance is an object created by a method within a `WireSpace`. The method (because a managed instance is effectively a Java method) needs to be annotated with the `@Managed` annotation:

```java
import com.braintribe.wire.api.annotation.Managed;

@Managed
public FancyThingy fancyOne() {...}
```

The return type of the method defines the official type of that instance. The concrete type of the instance that the method returns can be any subclass. You can choose to return that concrete type or the super type. You can also choose to make the method `public`, `protected` or `private`. If you choose the `private` modifier you restrict the access to that bean to only the `WireSpace` that it is defined in.

If you choose the `protected` modifier you can still extend the `WireSpace` but access is still limited to the concrete `WireSpace` instance. Only with the `public` modifier you allow other `WireSpaces` to have access.

The implementation of that method normally creates an instance and assigns it to a variable conventionally named `bean`. It then parametrizes that instance and finally returns it. The parameterization is often done by calling setters, adders, builders and other code intended to prepare the instance.

> The instance being assigned should not be a managed instance from another method.

```java
@Managed
public FancyThingy fancyOne() {
    // instantiation
    FancyThingy bean = new FancyThingy();

    // parameterization
    bean.setFast(true);
    bean.setName("foobar");

    // return the prepared instance
    return bean;
}
```

The code can be more complex than in this linear example, it can even have conditional parts and loops. Try to keep it simple though if you can and always think if a certain dynamic aspect is really part of wiring or of the implementation.

The code can also access the managed instance in order to add further information on that level. You can read about it in the [WireScope](wire_in_detail.md#wirescope) section.

#### Context-sensitive Parametrization

You can also parametrize an instance using context information:
```java
public interface MyContext implements ScopeContext {
String getName();
}

@Managed
public class ExampleWireSpace implements WireSpace {

    @Managed
    public FancyThingy fancyOne(MyContext context) {
        // instantiation
        FancyThingy bean= new FancyThingy();

        // parameterization
        bean.setFast(true);
        // parameterization using runtime information from a ScopeContext
        bean.setName(context.getName());

        // return the prepared instance
        return bean;
    }
}

```

#### `ManagedInstance` Configuration

With a static import of `com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance()`, you can access your managed instance configuration for further fine tuning, like in the following example:

```java
import static com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance;

@Managed
public class ExampleWireSpace implements WireSpace {

    @Managed
    public FancyThingy fancyOne() {
        // instantiation
        FancyThingy bean = new FancyThingy();

        // parameterization
        bean.setFast(true);
        bean.setName("foobar");

        // accessing the instance configuration to register an individual destroy method
        currentInstance().onDestroy(bean::onDispose);

        // return the prepared instance
        return bean;
    }
}
```

#### ManagedInstance Lifecycle

When a managed instance is being instantiated in a `WireScope`, it may be notified about its parameterization being completed. When the `WireScope` is closing, its managed instances may be notified about that as well. The actual notification pattern depends on `LifeycleListeners` configured to the `WireContext`. The most concise and performant approach is to work with well defined interfaces for lifecycle notification. You can configure the `com.braintribe.wire.impl.lifecycle.StandardLifecycleListener` class to support the following interfaces on managed instances in the right phase of the lifecycle:

Interface | Post Initialize | Pre Destroy
----- | ------- | -------
`com.braintribe.cfg.InitializationAware` | `postConstruct()` |
`com.braintribe.cfg.DestructionAware` | | `preDestroy()`
`java.lang.AutoCloseable` | | `close()`
`com.braintribe.cfg.LifecycleAware` | `postConstruct()` | `preDestroy()`

Alternatively, you can configure `com.braintribe.wire.impl.lifecycle.StandardLegacyLifecycleListener` to additionally support instance notification methods annotated with the JDK annotations:

* `@PostConstruct`
* `@PreDestroy`

You do this by overriding the `onLoaded()` method in a `WireSpace`, for example:

```java
@Managed
public class CustomCartridgeSpace extends CustomCartridgeSpaceFull {


    @Override
    public void onLoaded(WireContextConfiguration configuration) {

        configuration.addLifecycleListener(new LifecycleListener() {

            @Override
            public void onPreDestroy(InstanceHolder beanHolder, Object bean) {
                // nothing to do
            }

            @Override
            public void onPostConstruct(InstanceHolder beanHolder, Object bean) {
                if (beanHolder.space() instanceof EnsureGlobalIds && bean instanceof GenericEntity) {
                    GenericEntity entity = (GenericEntity) bean;
                    if (entity.getGlobalId() == null) {
                        String globalId = "bean:"+externalId+"/"+beanHolder.space().getClass().getSimpleName()+"/"+beanHolder.name();
                        entity.setGlobalId(globalId);

                    }
                }

            }
        });


        super.onLoaded(configuration);
    }
}
```

As you can do this on any space implementation you have to consider when a space has the `onLoaded()` method called.

Generally, the spaces are initialized as per the `@import` statments starting the traversing at the space that is associated to the top level contract. This means that in the following example the `MyMainSpace` space will be loaded last:

```java
MyMainSpace implements MyMainContract {
  @Import
  private MetaSpace meta;
  @Import
  private SomeOtherSpace someOther;
}
```

That means if the main space has child spaces it can never be the first one to load.

If you only want to support the methods without the interfaces, you can configure `com.braintribe.wire.impl.lifecycle.AnnotationBasedLifecycleListener `.

### WireSpace

A `WireSpace` is a bean namespace class where managed instance methods are placed.

Within a `WireSpace`, methods can reference other managed instance methods directly via `this` and can therefore also access `private` and `protected` managed instance methods. All managed instances within a `WireSpace` are lazily instantiated. So at least one access to such a method must have occurred that an instance is being created.

A `WireSpace` class implements the interface `com.braintribe.wire.api.space.WireSpace` and is annotated with `@Managed`.

A `WireSpace` can inherit from another `WireSpace`. But if you use a common super `WireSpace`, two deriving `WireSpaces` will only share the same code but won't share managed instances. If you want to share managed instances then use the `import` feature.

The following example shows a `WireSpace` with two bean defining methods that mutually reference each other conveniently with a direct method call in their parameterization part. That cyclic call structure would normally result in a stack overflow, but as Wire is an extension of Java, the logic works otherwise and ensures the cyclic referentiality within a `WireScope`.

```java
@Managed
public class ExampleWireSpace implements WireSpace {

    @Managed
    public FancyThingy fancyOne() {
        // instantiation
        FancyThingy bean= new FancyThingy();

        // parameterization
        bean.setFast(true);
        bean.setName("foobar");
        bean.setOther(fancyTwo());

        // return the prepared instance
        return bean;
    }

    @Managed
    public FancyThingy fancyTwo() {
        // instantiation
        FancyThingy bean = new FancyThingy();

        // parameterization
        bean.setFast(false);
        bean.setName("barfoo");
        bean.setOther(fancyOne());

        // return the prepared instance
        return bean;
    }
}
```
#### Loading WireSpaces

When a `WireSpace` is loaded, the `onLoaded(WireContextConfiguration)` method is called.

This method can be used to register meta functionality for the `WireContext` itself on the `WireContextConfiguration` but also to instantiate eager managed instances by simply calling their methods as shown in the example:

```java
@Managed
public class ExampleWireSpace implements WireSpace {

void onLoaded(WireContextConfiguration configuration) {
    // eager instantiations
    fancyOne();
    fancyTwo();
    }
}
// here the bean definitions would follow ….
```

#### Importing WireSpaces

If a managed instance method in a `WireSpace` wants to reference one from another `WireSpace`, it has to import the other `WireSpace` in order to do so. The referenced method must have been declared as `public`. The import is done with an uninitialized member variable that is annotated with the `@Import` annotation:

```java
import com.braintribe.wire.api.annotation.Import;

@Import
private OtherSpace otherSpace; {...}
```

Instances of `WireSpace` are managed by the `WireContext` which keeps them as singletons per class so that every import will bring the same `WireSpace` and therefore the same managed instances.

The following example shows the import of the other `WireSpace` and the access to one of its managed instances:

```java
@Managed
public class ExampleWireSpace implements WireSpace {

    @Import
    private OtherSpace otherSpace;

    @Managed
    public FancyThingy fancyOne() {
        // instantiation
        FancyThingy bean = new FancyThingy();

        // parameterization
        bean.setFast(true);
        bean.setName("foobar");
        bean.setOther(otherSpace.fancyOther());

        // return the prepared instance
        return bean;
    }
}
```

Imports can also be used to access `WireScopes`.

#### Methods Without `@Managed`

`WireSpace` classes can also contain unmanaged methods that are not annotated. These methods can be helpers, switches and other constructions, but you should be aware of the concern of `WireSpaces`. You can still access code from other classes (and even artifacts) that are not `WireSpace`-related.

### Contract

Contracts are interfaces that extend `com.braintribe.wire.api.space.WireSpace`.

In these interfaces, you can have abstract methods that must be implemented by an associated `WireSpace` class. The methods should not be annotated with `@Managed` as this is not inherited by implementation classes. The managed instance nature of a method is always only given by the implementation class and never by a contract.

Contract `WireSpaces` serve different purposes. One is the `Classloader` separation and the other is to support abstraction that could help in plugin systems and also for a more decoupled way to describe what is available for other `WireSpaces`.

### WireContext

A `WireContext` manages a number of `WireSpaces` which are loaded through a specific classloader that perform instrumentation of the managed instance methods in each `WireSpace` to add the cross cutting code of scoping.

`WireSpaces` are loaded top down the `@Import` structure in a depth-first order and initialized in the bottom up order. During the initialization, the `WireContext` is responsible for injecting values for the member fields annotated with `@Import`. The loading starts with the `WireSpace` implementation that is associated to the main contract.

The main contract is mandatory and defines what is accessible to the outside of a `WireContext`. As the main contract has to be like any other contract, the `WireContext` interface ensures that only instrumented classes are used as implementations for the contract.

#### Setting Up a WireContext

You can setup a `WireContext` with a `com.braintribe.wire.api.context.WireContextBuilder` that you obtain from the static entry point method `com.braintribe.wire.api.Wire.context(Class<S> wireSpace)`.

`WireContextBuilder` allows you to:

* bind contracts to implementation `WireSpace`s in various ways:
  * explicit mapping
  * pattern based mapping working on package and simple class name
* define a default `WireScope`
* configure the internal classloader if needed
* set a parent `WireContext` from which contracts could be resolved

Inspect the code below to see how a context is normally built:

```java
// build the WireContext for a given root contract
WireContext<MainContract> context = Wire
.context(MainContract.class)
.bindContracts("com.braintribe.wire.test.basic")
.build();

// from the built context access the root contract to get beans from it
MainContract mainContract = context.contract();
Something someThing = mainContract.someThing();
```

### WireScope

A `WireScope` is the central point of control managed instances are managed with. A `WireScope` controls the lifecycle of the instances, notifies `LifecycleListeners` within that lifecycle and also takes care about locking to support multithread-safety.

Scopes are enumeration constants defined in `com.braintribe.wire.api.annotation.Scope`. The supported scopes are:

Enum Constant | Description
---- | ----
`singleton` | The most commonly used scope that instantiates only once. This is the default scope of a `WireContext`.
`inherit` | Not an actual scope but inheriting the scope from a higher context (e.g. `WireSpace`, `WireContext`). This is the default scope on a `WireSpace`, and a managed instance method. The inheritance is as follows: <br/> - `WireContext` <br/> - `WireSpace` <br/> - Managed Instance Method
`prototype` | This scope will always create a new instance when a managed instance method is being called. The managed instances are only managed during construction and not hold afterwards. This means such instances are never destructed by Wire.
`referee` | Instances in that scope will be destroyed when the caller that called a managed instance method of that scope is destroyed.

#### Applying a Scope

You can apply the scope either when building a `WireContext`:

```java
// build the WireContext for a given root contract
WireContext<MainContract> context = Wire
.context(MainContract.class)
.bindContracts("com.braintribe.wire.test.basic")
.defaultScope(Scope.singleton)
.build();
```

or in the `@Managed` annotation on `WireSpace` or managed instance methods:

```java
@Managed(Scope.prototype)
public class ExampleWireSpace implements WireSpace {

@Managed(Scope.singleton)
public FancyThingy fancyOne() {
    // instantiation
    FancyThingy bean = new FancyThingy();

    // parameterization
    bean.setFast(true);
    bean.setName("foobar");

    // return the prepared instance
    return bean;
    }
}
```

### WireModule

A `WireModule` is used to group artifact `WireSpaces` so they can be bundled together and used in another artifact by allowing you to do their Wire context configuration internally.
Each `WireModule` can depend on a number of other `WireModules`, which in turn allows you to use their `WireSpaces` without facing any class loading related issues.

Each `WireModule` instance hosts:

* `WireModule` dependencies
* A `WireContextBuilder` configuration

If a `WireModule` also hosts a `WireContract` to be used as a main contract of `WireContext`, we call it a *`WireTerminalModule`*.

By default, `WireContextBuilder` is configured to load space classes based on package name convention i.e. from `WireModule` class package sub-packages named `contract` where `WireContracts` reside and `space` 
where their implementing `WireSpaces` reside. You can always override this default behavior.

##### Implementing a `WireModule`

A `WireModule` is meant to be singleton by nature and therefore is implemented as an `enum`.
Examples of a `WireModule` implementation (terminal and non terminal):

```java
// wire terminal module
public enum OurWireModule implements WireTerminalModule<MainWireContract> {

	INSTANCE;
	
	// this is where we specify our wire terminal module dependencies
	@Override
	public List<WireModule> dependencies() {
		return list(SomeWireModule1.INSTANCE, SomeWireModule2.INSTANCE);
	}
	
	// this is where we configure a wire context builder
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
	}
	
	// this is where we specify a main contract
	@Override
	public Class<MainWireContract> contract() {
		return MainWireContract.class;
	}
}

// wire module
public enum OurWireModule implements WireModule {

	INSTANCE;
	
	// this is where we specify our wire module dependencies
	@Override
	public List<WireModule> dependencies() {
		return list(SomeWireModule1.INSTANCE, SomeWireModule2.INSTANCE);
	}
	
	// this is where we configure a wire context builder
	@Override
	public void configureContext(WireContextBuilder<?> contextBuilder) {
		WireTerminalModule.super.configureContext(contextBuilder);
	}
}
```
