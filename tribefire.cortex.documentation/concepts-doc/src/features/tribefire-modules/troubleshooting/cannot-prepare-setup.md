# Incompatible setup components

## Situation:

You have an incompatible setup, e.g. with modules using different versions of [GM APIs](../application-structure.md#gm-apis) or using incompatible [platform libraries](../application-structure.md#platform-libraries).

## Observation:

When doing a setup with `Jinni`, you get an error that starts with:

```
Houston, we have a problem. Cannot prepare this setup...
```

followed by details about the incompatibility.

## Solution:

Well, it depends...

### Incompatible GM APIs

Assuming your GM APIs are both from the same TF version (major.minor), it's probably some third party jar that is the problem, because these can be resolved to different versions for different modules.

If the API that brings this jar is under your control, consider getting rid of the jar if it's not critical part of your API - see the note in [GM APIs doc](../application-structure.md#gm-apis).

If you are simply using somebody else's APIs and he left such a jar there, you have the problem because other dependencies (some-jar) of your component (module/library) bring a higher version of the conflicting jar. But maybe the module would work with the lower version, so you can try to exclude this jar from other sources, like this:

```xml
<dependency>
    <groupId>com.company</groupId>
    <artifactId>some-jar</artifactId>
    <version>${V.com.company}</version>
    <exclusions>
        <exclusion>
            <groupId>third.party</groupId>
            <artifactId>conflicting-artifact</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

**But why can't this exclusion be done automatically?**
The setup uses a strategy to select the highest version of every jar. Explicitly choosing a lower version might lead to problems, if the user of that highest version really needed that high version. For this reason we leave the responsibility on the user.

### Incompatible Platform library

In case you have a platform library which causes the problem, well... you can also try some exclusion magic as for GM APIs, but it's getting weird, when the whole point of the library is to bring all the jars to the classpath. This is somewhat a fundamental limitation of your application, e.g. you cannot used conflicting JDBC drivers in a single JVM, or you cannot bring conflicting artifacts whose presence on the classpath has some side-effects, which are the main use-cases for platform libraries.

----

## Extra details


### Why can't we promote an API (or anything else) without also promoting all it's dependencies.

This could in some cases be technically OK, but it entirely depends on which classes from say GM API you want to share among multiple modules.

Let's say we have the following situation:
```
platform

module-a
    api-a
        lib-1#v1.0

module-b
    module-a
        api-a
            lib-1#v1.0
    lib-2
        lib-1#v2.0
```

We have `module-a`, which offers `api-a` as an extension point and `module-b` uses that API.

We also see that `module-b` requires a higher version of `lib-1` than `module-a` does.

What can we do? Should we simply use `lib-1` in the higher version on all modules? Well, then we cannot guarantee that module-a works, because there might be compatibility breaking changes.

Can we simply promote the `api-a` and ignore the dependencies, thus ending up with a setup like this:

```
main classpath
    platform
    module-a
    api-a
    lib-1#v1.0

module-b classpath
    module-b
    lib-2
    lib-1#v2.0
```

Well, this might or might not work, but that depends on what we are actually sharing between the modules. Imagine this is the only part of `api-a` that we are sharing:

```java
public interface MyProcessor {
    int run(String param1, String param2);
}
```

This would be safe, as `MyProcessor` would only be loaded by the main class-loader and there is no problem.

But what if it looks like this:

```java
public interface MyProcessor {
    int run(String param1, ClassFromLib_1 param2);
}
```
All of a sudden we have a problem in `module-b`. The reason is that `module-b` would get the `MyProcessor` loaded by the main class-loader (which is still the only class-loader who sees it) but it would also have a `ClassFromLib_1` loaded by the main class-loader.

If, however, somewhere inside `module-b` we had code like this:

```java
myProcessor.run("value", new ClassFromLib_1())
```

We would have a problem, because we are passing a `ClassFromLib_1` loaded by `module-b`'s class-loader.

In practice, the situations can be very tricky to analyze, and the errors throw at you by Java might give no information of what is going on, typically `LinkageErrors` with messages like `loader constraint violation`.

