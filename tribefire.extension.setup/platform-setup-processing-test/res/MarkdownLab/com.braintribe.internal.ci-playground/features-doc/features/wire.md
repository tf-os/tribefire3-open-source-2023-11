# Wire
Wire is an extension to Java based on annotations and byte code instrumentation used to support inversion of control.

## Wire and IoC
The idea of inversion of control is to decouple the code that sets up and binds software components (classes) from the implementation code of that components. Based on that, the separation of concerns (SOC) pattern can be much better realized. Software components can therefore be developed to be independent of the concrete environment that they are working within.

As a consequence, dependency management of artifacts can be done in late binding way by mainly depending on API artifacts in implementation artifacts and depending on implementation artifacts in customization artifacts which will contain the code that sets up the concrete components and binds them. The purpose of the customization artifact is therefore scoped instantiation, parameterization and wiring. Hence the name „Wire“.

### Why Not Other Solutions?
Wire has been developed to address all the issues other solutions don't. While developing Wire, we focused on the following drawbacks of available solutions:
* Configuration can only be done by constructors, bean property setters, bean factories, unsafe SPEL expressions.
* Separation of the wiring automatically means that this code needs to be linear and simplistic.
* Using either XML or a weak and incomplete Java based way of defining the wiring.
* Breaking the initial rules of SOC and brings heavy dependencies and does not clearly extract its meta functionality in APIs.
* Lack of support for namespaces, contracts or access limitations.

Wire is there to solve all these issues. Built using a small library, containing only one dependency, Wire uses Java as a flexible and direct way of coding dependency injection. Since Wire is Java based, it allows a level of complexity enabling the use of loops and if conditions using normal Java code.

Wire is also used to configure the different components in a cartridge. This includes the configuring of the deployment model, the experts, the binding of denotation types to their experts, and configuration of a data model, if required.

{%include tip.html content="For more information, see [Understanding Wire](understanding_wire.html)."%}

## Instant Type Weaving

The most efficient way in Java or Javascript to store named and typed data is by means of compiled classes. Therefore, tribefire generates (weaves) code in the form of containers for model types if they do not exist as compiled classes. This code weaving is called Instant Type Weaving (ITW).

The classes, representing the model types, are simple beans, i.e. very simple classes that only store data as members and only implement the functionality to access the member data.

The Deployer Service is responsible to deploy the model together with the configured Expert. The configuration for that is read from the Cortex SMOOD Access and the Wire Deployable Expert Bindings. This way, ITW classes are instantiated at runtime.

In the case of the JVM the code weaving is achieved by byte-code generation. In the case of GWT it is done by Java source code generation at compile time and by Javascript code generation at runtime. This way the highest performance in weaving the types and in their runtime behavior is achieved. Property storage and instance creation is implemented without using hash maps and reflection calls (e.g. Constructor.newInstance, Method.invoke). Instead direct field access and new statements are used to achieve the best possible performance.
