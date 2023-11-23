# Developing web application with Tribefire Modules

> As we only support a WEB platform for now, **this example shows how to build a web application** with Tribefire. But **for a different application** we would only need to **chose a different platform**, the **rest of the steps would remain the same**. 

When building an web application with Tribefire, we typically want to take the following steps:

- [create one or more modules](#create-one-or-more-modules) for our custom code
- [create a project artifact](#create-a-project-artifact) which binds these modules (and any already existing modules we want to use) with our target `tribefire-web-platform` ([more info about project artifact...](application-structure.md#project))
- [install the created artifacts](#install-all-application-artifacts-in-your-local-maven-repository) in your local maven repository
- [create a local Tomcat environment](#create-a-local-tomcat-environment-for-debugging), ideally with a `Tomcat`-nature project based on our project artifact (so that we can run our web application from the IDE)
- import our new module (and the Tomcat project if relevant) into our IDE
- enjoy pure Java development without having to worry about the `Tribefire` architecture too much (see )
- [modify the project artifact](#modify-the-project) by adding or removing modules/libraries and re-setup the `Tomcat` project
- [prepare a package for server deployment](#prepare-a-package-for-server-deployment) (when the application is ready) (TODO ask Christina add link maybe)

Let's have a look at these steps more closely, using a concrete example.

## Conventions for the example
We'll illustrate the above steps with examples, which assume we are working on a `ApplicationX`, where our custom code is put into a group called `my.applicationx`, which is also the name of the repository where we put all the artifacts' sources, and thus it's also the name of the folder where we run our `Jinni` commands. This means we can omit the groupId parameter when creating new artifacts, as the name of the current folder is taken as default.

The default version for each artifact is `1.0`.

Note also that the Jinni create commands will create new artifacts which use ANT as their build system and are Eclipse projects, but this is just the default and can be changed using additional arguments.

## Create one or more modules
We start by creating a new module for our app, using Jinni:


```
jinni create-module application-x-module
```

So far we can leave this artifact empty, and only add more code later.


## Create a project artifact

A new project, which is an `AssetAggregator`, may be created with:

```
jinni create-asset-aggregator-asset application-x-project
```

When it's created, we reference our module (my.applicationx:application-x-module#1.0) in the project's `pom.xml` as an asset dependency, just like in the [web application structure](application-structure.md#project) example.


Thus, we should have the following artifacts in our repository:
```
xyz/git/my.applicationx/
    application-x-module/
        src/
        ...
        pom.xml
    application-x-project/
        ...
        pom.xml
```

## Install all application artifacts in your local maven repository

Using ANT (or whatever build system you chose), install the newly created artifacts (module,project) in your local repository. This is necessary, because the Jinni tasks that work upon our project artifact can only read artifact information from there.

## Create a local Tomcat environment (for debugging)

Next we want to create a local Tomcat environment so we can run our application, ideally with an additional Tomcat-nature project so can be run from IDE (currently only Eclipse is supported).

Since we don't want to store our local Tribefire in our repository, let's run Jinni in the following folder:

```
$ .../Tf-Installations/
```

### Local Tomcat with a Tomcat project for Eclipse

```
jinni setup-local-tomcat-platform --installationPath applicationx-tribefire --packageBaseDir applicationx-pckg --setupDependency my.applicationx:application-x-project#1.0 --deletePackageBaseDir true --debugProject my.applicationx:applicationx-tf-services
```

For more information on debugging, see [Debugging Tribefire Projects](development/debugging.md).

### Local Tomcat with our application packaged as a war file
Same as previous, but we omit the last argument (`--debugProject my.applicationx:applicationx-tf-services`).


## Modify the project
In case we decide to change the project artifact, we have to install the artifact locally again and then run Jinni to [create a local Tomcat environment](#create-a-local-tomcat-environment-for-debugging) again. We simply have to run it from the same location with exact same parameters as before, and this will update all relevant files.

Before doing so make sure our application (Tomcat) is not running.

## Prepare a package for server deployment
TODO ask MLA/CWI
