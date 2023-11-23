# Wire

Wire is an extension to Java based on annotations and byte code instrumentation used to support integration of concern.

The _wire_ library was created with the intend to serve the purpose of instantiation, wiring and lifecycle management of Java implementation instances. It helps you to integrate your software and give each of its parts a dedicated and continuous place of maintenance. That allows also others to understand the construction of your software and make contributions to it. Wire uses Java and therefore helps you with all the tooling that comes with it. That makes it a much better integrated IOC library than spring as it supports: namespaces, contracts, parametric instances, modularization, debugging.

Don't think _wire_ is only related to Tribefire extensions. It is completely independent and suited in almost all application to serve as central IOC hub. You can use _wire_ also in your unit tests and see them also consequently as IOC based software.

Going even further you have to understand that actually each programing language should have a dedicated syntax and support for IOC. It is a major lack of an essential feature in all the languages. Try to look at _wire_ as it would be the closest way to achieve such a feature in Java without actually changing the language. Look at it as if it would be native language feature and see its central purpose and you won't ever go without it in any of your applications.

## Separation of Concerns

_Separation of Concern_ (SOC) means to split the design of a software solution into well defined parts (e.g. artifacts, classes, methods). Each of those parts should have a dedicated purpose that is sufficiently easy to understand, implement, test, maintain and use. Separating well by dedicated purpose generates stability and raises the probability of reuse of the parts in other software solutions. In the best case your separation uncovers hidden domain agnostic functionality with a high potential of reuse.

When separated parts build on precursors try to couple/decouple this usage with contracts (e.g. _interfaces_) and avoid directly binding to a specific implementation and therefore avoid the acquisition of such precursors - stay passive in that sense. Simply offer a way to receive such contracted precursors (e.g. via setter or parameter).

You can see the central role of interfaces in SOC. Design your interfaces as contracts from the perspective of its usage instead of its implementation. For example there are rare cases where a setter makes sense on an interface. Think of the privileges a consumer of your interface has and don't offer aspects of your implementation there that other implementations could not serve with. You can clearly see how much that depends on the abstraction of the concern you separated.

## Integration of Concern

_Integration of Concern_ (IOC) is actually by itself the separated concern of the integration of the primarily separated software parts. It is an essential consequence of doing correct SOC that you need IOC to actually create a network of implementations that work together towards a higher purpose. The same implementations could be used in other integrations to achieve another purpose. You may know the acronym IOC as _Inversion of Control_ which doesn't fully reflect what it does. But _Inversion of Control_ is actually exactly _Integration of Concern_.

In the IOC part of your software you make things concrete. You choose and instantiate the dedicated parts and connect them based on their contract fulfillment.

_Dependency Management_ in that sense is a part of IOC as it chooses parts to be available for integration.

## Wire and IoC

The idea of inversion of control is to decouple the code that sets up and binds software components (classes) from the implementation code of that components. Based on that, the separation of concerns (SOC) pattern can be much better realized. Software components can therefore be developed to be independent of the concrete environment that they are working within.

As a consequence, dependency management of artifacts can be done in late binding way by mainly depending on API artifacts in implementation artifacts and depending on implementation artifacts in customization artifacts which will contain the code that sets up the concrete components and binds them. The purpose of the customization artifact is therefore scoped instantiation, parameterization and wiring. Hence the name *Wire*. 

### Why Not Other Solutions?

Wire has been developed to address all the issues other solutions don't. While developing Wire, we focused on the following drawbacks of available solutions:

* Configuration can only be done by constructors, bean property setters, bean factories, unsafe SPEL expressions.
* Separation of the wiring automatically means that this code needs to be linear and simplistic.
* Using either XML or a weak and incomplete Java based way of defining the wiring.
* Breaking the initial rules of SOC and brings heavy dependencies and does not clearly extract its meta functionality in APIs.
* Lack of support for namespaces, contracts or access limitations.

Wire is there to solve all these issues. Built using a small library, containing only one dependency, Wire uses Java as a flexible and direct way of coding dependency injection. Since Wire is Java based, it allows a level of complexity enabling the use of loops and if conditions using normal Java code.

Wire is also used to configure the different components in a cartridge. This includes the configuring of the deployment model, the experts, the binding of denotation types to their experts, and configuration of a data model, if required.

Tribefire's collaborative and asset based Cortex database allows to you to do IOC on an even higher level of _GenericModels_. This way your deployable based wiring is then reflected at runtime and can be collaboratively extended by completely separately built component. Cortex based IOC uses _wire_ as its IOC hub to bind deployables to real implementations

For more information, see [Wire in Detail](asset://com.braintribe.wire:wire-doc/wire_in_detail.md)

## Instant Type Weaving

The most efficient way in Java or JavaScript to store named and typed data is by means of compiled classes. Therefore, tribefire generates (weaves) code in the form of containers for model types if they do not exist as compiled classes. This code weaving is called Instant Type Weaving (ITW).

The classes, representing the model types, are simple beans, i.e. very simple classes that only store data as members and only implement the functionality to access the member data.

The Deployer Service is responsible to deploy the model together with the configured Expert. The configuration for that is read from the Cortex SMOOD Access and the Wire Deployable Expert Bindings. This way, ITW classes are instantiated at runtime.

In the case of the JVM the code weaving is achieved by byte-code generation. In the case of GWT it is done by Java source code generation at compile time and by JavaScript code generation at runtime. This way the highest performance in weaving the types and in their runtime behavior is achieved. Property storage and instance creation is implemented without using hash maps and reflection calls (e.g. Constructor.newInstance, Method.invoke). Instead direct field access and new statements are used to achieve the best possible performance.

## General

The idea of Inversion Of Control (IOC) is to decouple the code that sets up and binds software components (classes) from the implementation code of that components. Built using a small library, containing only one dependency, Wire uses Java as a flexible and direct way of using dependency injection.

## Prerequisites for Using Wire

Import the following artifacts: `com.braintribe.ioc.Wire#1.0`

## Components

For information on how Wire works, see [Wire in Detail](asset://tribefire.cortex.documentation:concepts-doc/features/wire/wire_in_detail.md)