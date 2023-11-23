# Hydrux Introduction

`Hydrux` is a `tribefire.js`-based framework for building modular, server-side configurable client applications.

[Why would you call it 'Hydrux'?](hx-name.md)

## Main Goal

The **main goal** of `Hydrux` is to let the developer **create custom components** (both visual and non-visual) and then **configure an application** as a **collection of** these **components**.

In practice this means **each component** has **two facets**:
* **implementation** - the actual `JavaScript` (ideally `TypeScript`) code
* **denotation** - an `Entity` that represents this component, which can be used for configuration/parameterization of given component

## Browser perspective

From browser perspective, `Hydrux` application is served by the `Hydrux Servlet` which resolves the configured application based on URL parameters and renders the HTML page.

This page contains `JavaScript` code that works as a runtime environment for our components. It:
* ensures the user is authenticated; if not, it redirects to the `Tribefire Server` login page
* resolves the main configuration meta-data (`HxApplication`)
* loads relevant `JavaScript` modules with our components
* wires these components together
* provides API to resolve components created dynamically, i.e. while the app is running (e.g. new tab, new session ...)
* sets the HTML element of the top-level `View` (visual component) as the child of the HTML document body

## Code structure

### Core Hydrux

`Hydrux` itself consists of the following assets:

* `hydrux-module`(TribefireModule) - brings `Hydrux Servlet` (and a special `Hydrux Request Processor` used internally).
* `hydrux-api` (JsLibrary) - defines **APIs** to be **implemented by a developer**, as well as **APIs given to the developer** from the `hydrux-platform`, including **API to bind a component**, i.e. to tell how to **resolve an implementation for** given **denotation** instance.
* `hydrux-platform` (JsLibrary) - wrapper (runtime environment) for our application, which can **read the server configuration**, **load** the correct **`JavaScript` modules** containing our custom components and which provides implementation for the platform-side `Hydrux` APIs.

NOTE that your project setup only needs an asset dependency on the `hydrux-module`, which automatically includes the other two assets.

```xml
<dependency>
    <groupId>tribefire.extension.hydrux</groupId>
    <artifactId>hydrux-platform</artifactId>
    <version>${V.tribefire.extension.hydrux}</version>
    <classifier>asset</classifier>
    <type>man</type>
    <?tag asset?>
</dependency>
```

### Custom Components

Custom `Hydrux` components are bundled in modules. A `Hydrux` module consists of two artifacts, which are both `PlatformAssets`:

* **model** - (Java-based artifact) contains the `Entities` that denote our component(s); each such entity must extend some `HxComponent` from `hydrux-deployment-model` (e.g. `HxView`), which is said to be its component type
* **js-library** - contains the `JavaScript` implementation of our component(s), together with code that binds each denotation type to the corresponding implementation; each such component must implement the corresponding `IHxComponent` interface from `hydrux-api`, e.g. `IHxView`, which corresponds to `HxView` component type

NOTE: When it comes to **naming conventions**, for a custom domain `xyz` we'd call the **model** `xyz-hx-deployment-model` and the **js-library** `xyz-hx-module`.

[Details...](./hx-components.md)

### Application Configuration

`Hydrux` **application** is **configured as a model meta-data** on a model of some `Service Domain` on the server side. This is typically done using an `initializer`, i.e.`PrimingModule`.

[Details...](./hx-app-config.md)


## Integration in TF Server

In order to make a `Hydrux` app available on `Tribefire` server, we must include the `hydrux-module`, our custom `initializers` and the relevant `js-libraries`. A **setup with two** separate **servers** is recommended, where the **second server** contains **only** the `js-libraries`, so that we can **update the client** app **without restarting the server**.

[Details...](./hx-setup.md)

## tooling (jinni)

TODO

## Comparison with Tribefire Server

Note that this is exactly the **same pattern** as we use on **server** for our **functional components**, which are modeled as sub-types of `Deployable`. In case of `Hydrux`, all components are sub-types of `HxComponent`, which we will discuss later.








### The fame

Hydrux encourages (well, demands) a modular client application structure, i.e. one consisting of various components.

On **server side**, these components are configured as `meta-data` (**hydrux-deployment-model**) on an `Access` or a `Service Domain`. The top-level application is defined by model meta-data `HxApplication`, and components for viewing individual entity types are defined by `HxViewWith`. These meta-data contain an `HxView` instance, which denotes the actual view (i.e. visual component), whose`HxModule` denotes the actual JavaScript module.


