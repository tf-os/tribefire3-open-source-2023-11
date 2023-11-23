# SLF4J API Linkage Error

## Situation:

Your module depends on `slf4j-api`, most likely indirectly through some 3rd party library.


## Observation:

On startup you get some kind of `LinkageError` related to classes under `org.slf4j` package. For example:

```
loader constraint violation: when resolving method "org.slf4j.impl.StaticLoggerBinder.getLoggerFactory()Lorg/slf4j/ILoggerFactory;" the class loader (instance of tribefire/cortex/module/loading/ModuleClassLoader) of the current class, org/slf4j/LoggerFactory, and the class loader (instance of org/apache/catalina/loader/ParallelWebappClassLoader) for the method's defining class, org/slf4j/impl/StaticLoggerBinder, have different Class objects for the type org/slf4j/ILoggerFactory used in the signature
```

## Solution:
Also add a dependency to `slf4j-jdk14`, ideally one matching your `slf4j-api`.

E.g.:
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-jdk14</artifactId>
	<version>1.7.25</version>
</dependency>
```

## Explanation:
We use `slf4j-jdk14` in our platform to redirect `slf4j` logging to `jdk` logging. This artifact has a dependency on `slf4j-api`, however, through some magic in this library there is a de-facto opposite dependency, because the class `LoggerFactory` (in API) internally uses `StaticLoggerBinder` (in JDK). This is achieved by placing a dummy `StaticLoggerBinder` and removing it while building.

What can happen is that the `LoggerFactory` and `ILoggerFactory` are loaded by the `ModuleClassLoader`, as they are in the API (and API is also on module's classpath). `StaticLoggerBinder` gets loaded by the `PlatformClassLoader` and since it depends on `ILoggerFactory`, this one is also loaded by the `PlatformClassLoader`. Thus, the class `LoggerFactory` has a way to reach `ILoggerFactory` from both class-loaders.

The solution is to ensure `StaticLoggerBinder` is also loaded by the `ModuleClassLoader`, and this is achieved by putting the `slf4j-jdk14` on the module's classpath.