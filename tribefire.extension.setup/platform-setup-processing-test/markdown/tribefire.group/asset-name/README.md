# Getting Started

This documentation describes the basic steps to work with Platform Assets based on the _tribefire-standard-aggregator_ (which brings tribefire’s required core components and configuration).

## Prerequisites

[Install](asset://tribefire.group:asset-name/sub/markdown-intro.md#heading2) the tribefire jinni CLI.
[Javadoc](javadoc://com.braintribe.doc.DocumentLoaderTest)

## include
[](sub/markdown-intro.md?INCLUDE) the tribefire jinni CLI.

### User Management

## Prepare a local environment
[TOC]
Create a suitable base folder for tribefire setups (in this documentation called *tf-setups*). The following folder structure shows the suggested organization. This filesystem structure allows to identify and separate different setups.
```
Note: In the future the information in the filsystem will be used by the jinni CLI to deduce information from the current working directory instead of getting them explicitly from CLI parameters.
```

* tf-setups
  * some-group.project-x-asset#1.0  
    * package
    * installation  
      * conf
      * storage
      * plugins
      * runtime
  * some-group.project-y-asset#1.0  
    * package
    * installation  
      * conf
      * storage
      * plugins  
      * runtime

## Create a project

As an example we want to create a project called *my-project* with a major-minor version *1.0* which is in the group *tutorial*.

To achieve this open a command line and execute:

```
> jinni create-project qualifiedName=tutorial:my-project#1.0 dependencies=tribefire.cortex.assets:tribefire-standard-aggregator#2.0
```

The result of this operation will be a newly created asset artifact *tutorial:my-project#1.0.1-pc* with the [AssetAggregator](javadoc:com.braintribe.model.asset.natures.AssetAggregator) nature having the ranged dependency to the *tribefire.cortex.assets:tribefire-standard-aggregator#[2.0,2.1)*. This artifact is installed into the local maven repository which is configured via [maven settings](https://maven.apache.org/settings.html). The local maven repository is sufficient to resolve this new project during a setup, but note that the project is not yet deployed to a shared repository that should be considered as the actual persistence.

## Setup the newly created project

Navigate to folder **tf-setups** and execute:

```
> jinni setup-local-tomcat-platform project=tutorial:my-project#1.0
```

The result of this operation will be an executable tribefire installation for the localhost with port 8080. The _qualifiedName_ of the project is used to create an individual directory structure:

* tf-setups
  * tutorial.my-project#1.0  
    * package
    * installation  
      * conf
      * storage
      * plugins
      * runtime  
        * host
          * bin
            * **catalina.bat**
            * **catalina.sh**

## Start tribefire with the new project

To achieve this open a command line in folder *tf-setups/tutorial.my-project#1.0/installation/runtime/host/bin* and execute:

```
> catalina start
```

After you started tribefire it will be available via following URL:

[http://localhost:8080](http://localhost:8080)

## Customize the new project

In order to be able to deploy new assets and to introduce designtime users to work with the installation, you have to start the _tribefire-control-center_:

[http://localhost:8080/tribefire-control-center](http://localhost:8080/tribefire-control-center)

### Maven Repository Configuration

For the sake of simplicity we will configure a file based deployment repository.

Let us define the repository url as follows:  
```file://ABSOLUTE-PATH-TO/tf-setups/repository```

Configure this url as described in [Repository Configuration](asset://tribefire.cortex.assets:assets-doc/configuration.md#maven-repository-for-deploy-transfer).

### [User Management](#user-management)

In the right upper corner of the _tribefire-control-center_ click on the cogwheel and choose the item _Switch to > Authentication and Authorization_. This will open the access where you can manage user accounts.

Click on item Users and create a new user called "tutorial-user". Assign the password "tutorial" and role "tf-admin" to it.

### Create Assets

In the right upper corner of the _tribefire-control-center_ click on the cogwheel and choose the item _Switch to > Platform Setup_. This will open the access where you can manage assets.
In the workbench click on _Modified Assets_. This will show you two trunk-assets (cortex, auth). Close and transfer(deploy) both trunk-assets. [Add both new assets as dependencies](asset://tribefire.cortex.assets:assets-doc/management.md#add-dependencies) to the project aggregator and mark the dependencies as global setup candidates.  
Deploy the project aggregator as well.

For more information on how to manage assets, please refer to [Asset Management](asset://tribefire.cortex.assets:assets-doc/management.md).

## Conclusion

You now have a minimal project setup that you can recreate on other machines and that allows you to create and maintain other assets to shape your project.
