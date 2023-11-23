# Wire

> Wire is an Java extension based on annotations and byte code instrumentation which supports the IOC pattern.

* [General](#general)
* [Prerequisites for Using Wire](#prerequisites-for-using-wire)
* [Components](#components)
	- [`ManagedInstance`](#managedinstance)
	- [`WireSpace`](#wirespace)
	- [`WireContext`](#wirecontext)
	- [`WireScope`](#wirescope)
	- [`WireModule`](#wiremodule)
		+ [Implementing `WireModule`](#implementing-wiremodule)


## General

The idea of Inversion Of Control (IOC) is to decouple the code that sets up and binds software components (classes) from the implementation code of that components. Built using a small library, containing only one dependency, Wire uses Java as a flexible and direct way of using dependency injection.

## Prerequisites for Using Wire

Import the following artifacts: `com.braintribe.ioc.Wire#1.0`

## Components

Wire consists of the following structural elements:

* [`ManagedInstance`](#managedinstance)
* [`WireSpace`](#wirespace)
* [`WireContract`](#wirecontract)
* [`WireContext`](#wirecontext)
* [`WireScope`](#wirescope)
* [`WireModule`](#wiremodule)

### ManagedInstance

TODO: move documentation.tribefire.com docs here

### WireSpace

TODO: move documentation.tribefire.com docs here

### WireContract

TODO: move documentation.tribefire.com docs here

### WireContext

TODO: move documentation.tribefire.com docs here

### WireScope

TODO: move documentation.tribefire.com docs here

### WireModule

A `WireModule` is used to group artifact `WireSpaces` so they can be "shipped" together and used in another artifact by allowing you to do their wire context configuration internally. 
Each `WireModule` can depend on a number of other `WireModules`, which in turn allows you to use their `WireSpaces` without facing any class loading related issues.

Each `WireModule` instance hosts:

* `WireModule` dependencies
* A `WireContextBuilder` configuration

If a `WireModule` also hosts a `WireContract` to be used as a main contract of `WireContext`, we call it a *`WireTerminalModule`*.

By default, `WireContextBuilder` is configured to load space classes based on package name convention i.e. from `WireModule` class package sub-packages named 'contract' where `WireContracts` reside and 'space' 
where their implementing `WireSpaces` reside. You can always override this default behavior.

##### Implementing `WireModule`

A `WireModule` is meant to be singleton by nature and therefore is implemented as an `enum`.
Examples of a `WireModule` implementation (terminal and non terminal):

```
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





