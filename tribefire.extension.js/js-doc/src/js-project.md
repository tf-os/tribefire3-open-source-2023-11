## Jinni Supported Javascript Project

A Javascript project supported by Jinni has a defined directory structure and expected files.

- `project-root`
  - [`js-project.yml`](#javascript-project-descriptor) ?
  - [`pom.xml`](#javascript-project-dependency-management)
  - [`asset.man`](#javascript-development-relevant-artifacts) ?
  - [`src/`](#editing-sources)
  - `backup`
  - [`lib/`](#javascript-project-dependency-management)
    - `_src/` _symlink to_ `../src`
    - `foo.bar.example-2.0~/` _symlink to unpacked lib_
    - `fix.fox.commons-1.0~/` _symlink to unpacked lib_
    - `...`

### Javascript Project Descriptor

A Javascript project may have a project descriptor named `js-project.yml` containing yaml representation of [JsProjectDescriptor](javadoc:tribefire.extension.js.model.project.JsProjectDescriptor) found in model `tribefire.extension.js:js-project-model`.

Currently it supports to list source folders and include them in automatic updates of its resource references when dependency versions change. 

The following example of `js-project.yml` includes the standard `src` folder in this automatic update:

```yaml
!tribefire.extension.js.model.project.JsProjectDescriptor {
    sourceFolders: [
        { path: "src", updateResourceLinks: true }
    ]
}
```

The automatic update runs when library dependencies declared in `pom.xml` are resolved with `jinni` standing in the project root folder:

```
jinni assemble-js-deps
```
### Javascript Project Dependency Management

The dependencies to other Javascript libraries are managed in the `pom.xml` file. This file declares the artifact identification and version of the project itself as well as its dependencies:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
        
    <groupId>tribefire.extension.example</groupId>
    <artifactId>js-example</artifactId>
    <version>1.0.1</version>

    <dependencies>
        <dependency>
            <groupId>some.group</groupId>
            <artifactId>some-artifact</artifactId>
            <version>[1.0,1.1)</version>
        </dependency>
    </dependencies>
</project>
```
The dependencies you can use in the `pom.xml` can be of various types that are explained [here](#javascript-development-relevant-artifacts).

The transitive dependencies of a project can be resolved to the `lib` folder of the project with `jinni` standing in the project root folder:

```
jinni assemble-js-deps [--min]
```

The request `assemble-js-deps` resolves the transitive dependencies into the local maven repository along with the Javascript parts (`.js.zip`, `-min.js.zip`). If available the parts will be upacked to the `js-libraries` folder for reuse in the folloing way:

- `js-libraries`
  - `some.group.some-artifact-1.0.1`
    - `pretty`
       - `some.js`
       - `other.js`
    - `min`
       - `some.js`
       - `other.js`

The default location of `js-libraries` is:

|OS|Path|
|---|---|
|Unix|`~/.devrock/js-libraries`|
|Windows|`%HOMEDRIVE%%HOMEPATH%\.devrock\js-libraries`|

The default location can be overridden using the environment variable `DEVROCK_JS_LIBRARIES`.

One of the actual library content folders of any resolved and upacked library is symbolically linked in the project's `lib` folder. If `assemble-js-deps` is flagged with `--min` and the `min` folder is existing it will be choosen over the `pretty` folder. The name of the symbol link is build from the artifact identification and a short form of the version expression of the dependency that requested it. Using the version expression allows to have stable resource paths in your sources while the actual resolved revision of an artifact may change.

Given that the only dependency `some.group.some-artifact` had no transitive dependency, the result in the lib folder could like like this: 

* `project-root`
  * `src`
    * `example.js`
  * `lib`
    * `_src` _symlink to_ `../src`
    * `some.group.some-artifact-1.0~` _symlink to_ `~/.devrock/js-libraries/some.group.some-artifact-1.0.1/pretty`
    
Once you resolved and assembled your library dependencies you can refer to them like in this `example.js`:

```Javascript
import * as some from "../some.group.some-artifact-1.0~/some.js"
// you can now use the some namespace to access exported members of the some.js module 
```

### Editing Sources

When you edit sources in your IDE you should always load the file via `lib/_src` in order to satisfy its relative import paths as they are lying in the `lib` folder as siblings just like in the runtime situation. 

## Javascript Development Relevant Artifacts

When developing Javascript projects with Jinni Javascript Tool support, the following artifact types are relevant: 

|Artifact Type|JavaScript Part|Java Part|Asset Nature|
|---|---|---|---|
|[Model](#model-artifact)|Ambient Typescript Model Declaration<br/>Model Weaving|Model Interfaces|`ModelPriming`|
|[Tribefire JsInterop API](#tribefire-jsinterop-api-artifact)|Ambient Typescript API Declaration|API Interfaces and Implementations|_n/a_|
|[JavaScript Library](#javascript-library-artifact)|Custom JavaScript Code|_n/a_|_n/a_
|[Assetified JavaScript Library](#assetified-javascript-library-artifact)|Custom JavaScript Code|_n/a_|`JsLibrary`|
|[Assetified JavaScript Aggregator](#assetified-javascript-aggregator-artifact)|_n/a_|_n/a_|`JsLibrary`|
|[Tribefire UxModule](#tribefire-uxmodule-artifact)|Custom JavaScript implementation of a TribefireUxModuleContract|_n/a_|`JsUxModule`|

### Model Artifact
A model artifact is a declaration of a GenericModel to be used with the GenericModel and Tribefire Technology.

Models artifacts supply ambient Typescript declaration files in their Javascript part. If you have dependencies to models in your Javascript project, you should know how to profit from [ambient Typescript declarations](#ambient-typescript-declarations) in your IDE.

To create such an artifact use jinni:
```
jinni create-model <name-model>
```

### Tribefire JsInterop API Artifact

A tribefire JsInterop API artifact is an artifact sourced with the Java Programming language but written for the execution in a web browser. It contains APIs for the programming against GenericModels and Tribefire.

When buildig a GWT terminal that depends on such an artifact (e.g. tribefire.-js, control-center) Java sources are transpiled to Javascript by the GWT compiler and interfaces or classes annotated with JsInterop annotations are made accessible for other javascript libraries by determined name and namespace preservation.

TODO: link to overview of existing tf jsinterop APIs

When building the artifact itself it is basically treated as a normal Java artifact producing a jar for classes and a jar for sources. Additionally a javascript zip is generated that contains Typescript declarations for the API that are drawn from a Java Reflection analysis that scans for the JsInterop annotations and us that the build process automatically generates typesafe API declarations with the TypeScript language to make the API conveniently usable (type checking, code completion) in IDEs such as Visual Studio Code.

Such artifacts are published with parts for Java classes, Java sources and Javascript sources (API typescript declarations). Depending on the artifact resolution usecase (Java, JavaScript) the relevant parts are resolved and linked to the library management of a project.

Tribefire JsInterop API artifacts supply ambient Typescript declaration files in their Javascript part. If you have dependencies to models in your Javascript project, you should know how to profit from [ambient Typescript declarations](#ambient-typescript-declarations) in your IDE.

### Ambient Typescript Declarations

Typescript declarations are files with the extension `d.ts`. In case of the [Models](#model-artifact) and [Tribefire JsInterop APIs](#tribefire-jsinterop-api-artifact) the `d.ts` files contain no ES6 modules but only ambient namespace definitions to be directly used without module imports. In order to make the IDE aware of the declarations you should link then using a pseudo import comments such as:

```Javascript
/// <reference path="../com.braintribe.gm.resource-model-1.0~/resource-model.d.ts"/>
/// <reference path="../com.braintribe.gm.gm-core-api-1.0~/gm-core-api.d.ts"/>

// using a type declared in resource-model.d.ts
let resource = $T.com.braintribe.model.resource.Resource.create();

// using a function declared in gm-core-api.d.ts
let typeReflection = $tf.getTypeReflection();
```

Using the reference comment the IDE (e.g. Visual Studio Code) will help you with code completion and type safety information.

### Javascript Library Artifact

Javascript library artifacts can contain any arbitrary Javascript library. They do not neccessarily need to have any relationship to models and tribefire.

You can use [firex](linktofirex) to import third party libraries from NPM registries as Javascript library artifact.

Javascript library artifacts are supported with development tooling [by jinni].(#jinni-supported-javascript-project)

To create such an artifact use jinni:
```
jinni create-js-library <libary-name>
```
### Assetified Javascript Library Artifact

Assetified Javascript library artifacts are very much like normal [Javascript library artifacts](#javascript-library-artifacts) with the advantage to be directly dependable in another tribefire platform asset as they come with an `asset.man` part that declare a `JsLibrary` asset nature.

To create such an artifact use jinni:
```
jinni create-js-library <libary-name> -a
```
### Assetified Javascript Aggregator Artifact

An assetified Javascript aggregator artifact aggregates a number of dependencies to Javascript libraries or other aggregators. It has no Javascript parts itself.

As it is assetified with an `asset.man` it is directly dependable in another tribefire platform asset.

To create such an artifact use jinni:
```
jinni create-js-library-aggregator <aggregator-name>
```
### Tribefire UxModule Artifact

Tribefire UxModule artifacts are very much like normal [assetified Javascript library artifacts](#assetified-javascript-library-artifacts). In this case the `asset.man` declares the `JsUxModule` asset nature.

To create such an artifact use jinni:
```
jinni create-js-ux-module <module-name>
```

