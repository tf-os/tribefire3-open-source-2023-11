# Tribefire application structure

Our application consists of these components, which are all [platform assets](../platform_assets.md)

* [Project](#project)
* [Platform](#platform)
* [Modules](#modules)
* [Platform libraries](#platform-libraries)
* [Other assets](#other-assets)

and tribefire libraries, with one group of libraries that deserves a special attention:

* [GM APIs](#gm-apis)


## Project
An [AssetAggregator](javadoc:com.braintribe.model.asset.natures.AssetAggregator), which **defines which assets our entire setup consists of**.

It must at minimum contain a **platform** (`tribefire-web-platform` for web application), but would normally also contain the **modules** and **libraries** which extend this platform, and other desired assets.

As a simple example, its `pom.xml` dependencies might look like his:

```xml
<!-- tribefire-web-platform -->
<dependency>
    <groupId>tribefire.cortex.services</groupId>
    <artifactId>tribefire-web-platform</artifactId>
    <version>${V.tribefire.cortex.services}</version>
    <classifier>asset</classifier>
    <type>man</type>
    <?tag asset?>
</dependency>

<!-- standard cortex initializer - core module -->
<dependency>
    <groupId>tribefire.cortex.assets</groupId>
    <artifactId>cortex-initial-priming</artifactId>
    <version>${V.tribefire.cortex.assets}</version>
    <classifier>asset</classifier>
    <type>man</type>
    <?tag asset?>
</dependency>

<!-- custom module for our application -->
<dependency>
    <groupId>my.org</groupId>
    <artifactId>application-x-module</artifactId>
    <version>${V.my.org}</version>
    <classifier>asset</classifier>
    <type>man</type>
    <?tag asset?>
</dependency>
```

> Note that this kind of dependency declaration simply states the dependency to the other asset, without actual jar files being depended. This means it provides the structure information for the setup process.

## Platform
Each application specifies exactly one platform, which determines the "nature" of the application - WEB, CLI, something else. See [high level overview of Tribefire Architecture](introduction.md#high-level-overview) for more information.

## Modules
[Tribefire Modules](javadoc:com.braintribe.model.asset.natures.TribefireModule) are units **containing code and (configuration) data**.

Modules may of course have dependencies on other modules, as well as any other assets or libraries. Three types of dependencies - [models](../models/models.md), [GM APIs](#gm-apis) and [platform libraries](#platform-libraries) - need special attention, because they are always moved to the main classpath, and thus our project can only consist of modules which don't have version conflicts of these kinds of assets. See [module-compatibility](module-compatibility.md) article for more details.

## GM APIs

When developing a Tribefire application, we also use Tribefire libraries (jars). They generally fall into three categories, [models](../models/models.md), `APIs` (which we call `GM APIs`) and `"implementations"`.

`GM APIs`, as the name suggests, consist mainly of java interfaces, and they define what internal Tribefire components look like, and the `"implementations"` artifacts consist of implementations of these interfaces and related tools/utility classes.

Therefore, all `GM APIs` used in an application must be compatible, i.e. we cannot have two components in our application which specify a different version of the same `GM API` artifact. This is important when considering whether a given module` is compatible with a given platform, or if two modules are mutually compatible.

Note that for `GM APIs` there is a convention of having names with an "-api" suffix (e.g. `gm-session-api`), and they are also marked as a `GM API` in their *manifest* file.

**IMPORTANT:** This all means you should try to **keep your API artifacts as small as possible**, and ideally with no third party dependencies, just other APIs. This reduces the probability of incompatibility. If you want to share a component coming from a third party library between modules, and this component contains it's interface and implementation in a single jar, consider wrapping the functionality in your own interface or exposing it via say `ServiceRequest`. If not possible, consider simply not sharing the functionality and have each module construct it's own objects to do the job.

> We were facing this issue in TF internally, with `HttpClientBuilder` class from (org.apache:httpclient library). As it comes with tons of dependencies which could easily block other libraries, we have decided not to expose it as an API at all, i.e. the builder used by the platform itself is not exposed via contracts, but each module has to create it's own builder.

## Platform libraries
[Platform library](javadoc:com.braintribe.model.asset.natures.PlatformLibrary) is an asset that forces the placement of everything it contains (i.e. depends on) on the main classpath.

What this means is closely related to the question of [module compatibility](module-compatibility.md). In short, if different modules use different versions of the same library, we might end up with an application where each module has some of it's libraries on it's own classpath, thus avoiding the conflict with other modules. Sometimes, however, we want force the placement of certain libraries on the main classpath, because having those libraries visible only with a special class-loader would cause problems. Thus, simply creating a `Platform library`, with our libraries as it's dependencies, is the way to instruct the setup tool to place our libraries on the main classpath. Then, should there be conflicts between two different platform libraries, the setup process would fail with an exception.

> Technically, platform libraries are treated similarly to a [GM API](#gm-apis) artifacts, except the existence of a `jar` part of this artifact is not required. (meaning you only need the pom.xml in your artifact repository). Thus this can be used purely as an instruction to force certain libraries on the main classpath .

## Other assets
[All other assets](../platform_assets.md#asset-natures) are processed in a standard way.

