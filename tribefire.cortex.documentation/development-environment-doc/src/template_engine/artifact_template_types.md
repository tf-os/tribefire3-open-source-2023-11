# Artifact Template Types

The template engine can use three different of artifact templates:

* [add on template](#add-on-template)
* [standard template](#standard-template)
* [aggregator template](#aggregator-template)

## Artifact Template Dependencies

Using dependencies, you can extend the existing artifact templates and create data structures based on them. To allow for greater reuse, any artifact template can have a dependency to another template. When a template with dependencies is being resolved, it is its dependencies that are resolved and projected first. Dependencies are marked in the `pom.xml` file with a special tag: `<?tag template>`.

## Add On Template

An add on template is used to dynamically extend your artifact template. In other words, you can choose whether to have projected in conjunction with your template. The template itself does not have a request nor the `data.yml` file - it gets all of his data from the base template for which it is an add on to. Add ons normally have to do with non-Tribefire functionality (although they can also handle core Tribefire functionality) and their use cases include providing the following:

* configuration files for a build system (Ant or Maven, for example)
* configuration files for specific IDEs (Eclipse, VS Code)
* configuration files for a version control system (GIT, SVN)

By separating the Tribefire and non-Tribefire related functionality, it is much easier to provide artifacts tailored to specific needs.

## Basic Template

A artifact template is made from the following parts:

* dynamic
* static
* `data.yml` file

The template engine uses Apache FreeMarker as the basis for templating. Template files always have the `.ftl` extension and have access to the variables you pass as a part of the service request, for example: `${request.accessId}`, `${request.artifactId}`, etc.

> For more information, see [Apache FreeMarker](https://freemarker.apache.org/).

### Dynamic Part

The dynamic part of an artifact template is the part where conditional processing takes place.

It is in the dynamic part of an artifact template where you can find the `projection` folder. This directory hosts the `.ftl` files which are processed when a given service request is executed to create an artifact. Also in the dynamic part is the `import` folder which contains the `.ftl` templates which you import into your `.ftl` templates in the `projection` folder, for example global variables. In other words, imports are non projected `.ftl` files you only use to import into your projected `.ftl` files from the `projection` folder.

> Imported `.ftl`s do not have to be placed in the `imports` folder, but we recommend to put them there as a best practice.

What you might find a bit misleading is the `static-handler.ftl` file. This file is used to dynamically manipulate the output structure of the asset including creating folders or excluding files and folders from the processing by evaluating the service request parameters. For example, you might have the `asset.man` file in the static part of your artifact template. If you pass the `includeAssetMan=false` argument in the service request, it is in the `static-handler.ftl` where you would specify the logic to exclude the `asset.man` file from the output.
And then static handler would ignore that file from static structure

Examples of dynamic elements might include the following files:

* `static-handler.ftl`
* `.classpath`
* `pom.xml`
* `asset.man`
* `build.xml`
* `model.man`
* `data.man`
* `imports/root.ftl`
* Java classes

### Static Part

The static part of an artifact template consists of elements that you want to project as is, without any processing. Examples of static elements might include the following files:

* `asset.man`
* static resources including images and downloadables
* `runtime.properties`

### `data.yml`

Only standard and aggregator templates have the `data.yml` file.

The `data.yml` file provides static data which is hardcoded in the template and cannot be changed by the user. For the most part, the data present in the `data.yml` file is used by the add ons, and may include static dependencies, for example. Below, you can see the `data.yml` file of the `asset-aggregator-template` artifact:

```yml
!com.braintribe.devrock.templates.processing.model.artifact.ArtifactProcessing
artifactType: 'aggregator'
```

The first line of the file always refers to an entity type. In the following lines of the `data.yml` file you can populate the properties of the entity type referred to in the first line. Needless to say, you can only populate the properties of the entity type you referred to in the first line.

> As of now, only the `ArtifactProcessing` entity type can be populated.

## Aggregator Template

The aggregator template is used to create aggregator requests which can run several other requests in a sequence. By using an aggregator template you can group several requests and create a bundle of artifacts.

A vital part of an aggregator template is the `requests.groovy` file which is a Groovy script that prepares all the individual calls and passes a list of prepared requests to Jinni for resolution.

Inside the `requests.groovy` file, you can use the `$request` object to influence the calls. See the file below for reference:
[](asset://tribefire.cortex.documentation:includes-doc/requests_groovy.md?INCLUDE)

> For more information, see [Creating Artifact Templates](creating_artifact_templates.md#request).

The content in the `dynamic` folder is projected after the calls from the `requests.groovy` file.

### Artifact Template Requests

Available requests change quite dynamically and vary between different Jinni versions (which is one of the reasons why your Jinni should always be up-to-date). To see the latest available requests, simply run `Jinni help`. Template-based requests start with `create`:

```
available commands:

    alias
    analyze-xsd
    analyze-xsd-container
    build-docker-images
    create-aggregator
    create-cartridge
    create-common
    create-container-projection
    create-gm-api
    create-initializer
```

If you want more information about a given request, run `jinni help requestName`, for example: 

```
jinni help create-template-artifact
```
As a result, you will get the request **alias** (if available), **syntax** explanation, and a list of **arguments** the request takes:

```
PS C:\dev> jinni help create-template
qualified-type: com.braintribe.devrock.templates.model.artifact.CreateTemplate
aliases: create-template
syntax: jinni create-template [<artifactId> [<version> [<installationPath>]]] [--argument <value>]...

Creates an empty artifact template.

mandatory arguments:

--artifactId --aid ARG   : The artifact id of the projected artifact.
                           ARG type: string

optional arguments:

--buildSystem --bs ARG   : The build system that is going to be used by the
                           projected artifact (e.g. 'bt-ant', 'maven', 'gradle',
                           ...).
                           ARG type: string
                           default: bt-ant

--dependencies --deps ARG ... : The dependencies of the projected artifact. Note
                           that this is used to add the dynamic dependencies,
                           but the static ones can be configured from the
                           artifact template itself.
                           list ARG type:
                           com.braintribe.devrock.templates.processing.model.Dep
                           endency

--groupId --gid ARG      : The group id of the projected artifact.
                           ARG type: string
                           default:
                           ${support.getRealFileName(request.installationPath)}

--hasParent --hp [ARG]   : Specifies whether or not the projected artifact has
                           parent.
                           ARG type: boolean (true, false)
                           default: true

--ide -i ARG             : The IDE in which the projected artifact will be used
                           (e.g. 'eclipse', ...).
                           ARG type: string
                           default: eclipse

--installationPath --ip ARG : The installation path of the artifact template
                           projection.
                           ARG type: string
                           default:

--installationPathExtension --ipe ARG : The installation path extension that
                           will be appended to the original installation path.
                           ARG type: string
                           default: ${request.artifactId}

--properties --props ARG ... : The properties of the projected artifact. Note
                           that this is used to add the dynamic properties, but
                           the static ones can be configured from the artifact
                           template itself.
                           list ARG type:
                           com.braintribe.devrock.templates.processing.model.Pro
                           perty

--sourceControl --sc ARG : The source control in which the projected artifact
                           will be checked in (e.g. 'git', ...).
                           ARG type: string
                           default: git

--template -t ARG        : The fully qualified name of the (base) artifact
                           template this request corresponds to. Defaulted by
                           the concrete sub-types.
                           ARG type: string
                           default:
                           com.braintribe.devrock.templates:template-artifact-te
                           mplate#1.0

--templateAddons --addons ARG ... : The fully qualified names of the artifact
                           templates that will be projected in conjuction with
                           the base artifact template.
                           list ARG type: string
                           default:
                           [com.braintribe.devrock.templates:template-artifact-b
                           t-ant-build-system-addon#1.0,
                           com.braintribe.devrock.templates:eclipse-project-meta
                           data-addon#1.0,
                           com.braintribe.devrock.templates:git-config-addon#1.0
                           ]

--templateType --tt ARG  : The type of the artifact template that is going to be
                           projected.
                           ARG type:
                           com.braintribe.devrock.templates.processing.model.Tem
                           plateType (BASIC, ADDON)
                           default: BASIC

--version -v ARG         : The version (e.g. 1.1) of the projected artifact.
                           ARG type: string
                           default: 1.0


DONE
PS C:\artifacts>
```

#### Simplifying Template-based Requests
As you can see in the above example, Jinni requests can take a lot of arguments. Fortunately, Jinni also provides certain mechanisms to make them easier, specifically **default values**, **aliases**, and **positional arguments**. Let's examine how to simplify a complex request.

Creating a model artifact is a typical use-case for a template request. Let's create an API model. We want our model to have the following information:

* **--artifactId** - a mandatory property for any artifact, in our case `tutorial-api-model`
* **--groupId** - artifacts are typically part of a group, thus we need to set this property. Let's set it to `tribefire.extension.tutorial`.
* **--installationPath** - we want to define a specific installation directory **C:/Workspace/Artifacts/Models**
* **--version** - we want to set the version to **1.0**
* **--dependencies** - we want to set the following dependencies: `com.braintribe.gm.root-model` and `com.braintribe.gm.service-api-model`
* **--ide** - we want to set **Eclipse** as the artifact's IDE

From Jinni help, we know that we can write the above requirements into the following request:

```
jinni create-model --artifactId tutorial-api-model --groupId tribefire.extension.tutorial --installationPath C:/Workspace/Artifacts/Models --version 1.0 --dependencies [com.braintribe.gm.root-model, com.braintribe.gm.service-api-model] --ide Eclipse
```

The above request is legitimate and would work. However, we can make our life easier. First, let's take a look into the default values for our arguments. We can immediately see some important information:

1. `--version` default value is set to **1.0**, which matches our requirements:

    ```
    --version -v ARG         : The version (e.g. 1.1) of the projected artifact.
                            ARG type: string
                            default: 1.0
    ```

2. `--ide` default value is set to **Eclipse**, which also matches our requirements:

    ```
    --ide -i ARG             : The IDE in which the projected artifact will be used
                            (e.g. 'eclipse', ...).
                            ARG type: string
                            default: eclipse
    ```

Since we don't have to change the default values, we can remove those arguments from the request altogether. Our request is now shortened to:

```
jinni create-model --artifactId tutorial-api-model --groupId tribefire.extension.tutorial --installationPath C:/Workspace/Artifacts/Models --dependencies [com.braintribe.gm.root-model, com.braintribe.gm.service-api-model]
```

> If you run Jinni from a prepared folder which you named in advance as your Group ID, you can resign from `--groupId` as well - it defaults to the root folder name.

We can still do better. Note that all arguments have an **alias**, for example `--aid` for `--artifactID`. When we replace arguments' full names with their respective aliases, our request is further shortened to the following:

```
jinni create-model --aid tutorial-api-model --gid tribefire.extension.tutorial --ip C:/Workspace/Artifacts/Models --deps [com.braintribe.gm.root-model, com.braintribe.gm.service-api-model]
```

Finally, we can use **positional arguments**. This feature allows you to enter values of `--artifactId`, `--version`, and `--installationPath` **without naming the argument**, provided that values are entered in the correct order.

Consider the request below:

```
jinni create-model myModel 1.5 installation
```

In this case, **myModel** (position 1 in the request) is assumed to be the value of `--artifactId`, **1.5** is assumed to be the `--version`, and **installation** is assumed to be the `--installationPath`. Order is important when you resign from argument names and must be respected:

Position | Assumed argument
---|---
First | `--artifactId`
Second | `--version`
Third | `--installationPath`

In our case, we can resign from naming `--artifactId` (`--aid`) explicitly. We cannot resign from `--installationPath` (`--ip`), because there is no way to put it in position 2 (we resigned from `--version` entirely). As a result, our final request is:

```
jinni create-model tutorial-api-model --gid tribefire.extension.tutorial --ip C:/Workspace/Artifacts/Models --deps [com.braintribe.gm.root-model, com.braintribe.gm.service-api-model]
```

