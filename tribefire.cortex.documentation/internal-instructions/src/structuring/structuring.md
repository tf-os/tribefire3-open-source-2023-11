# Defining Documentation Structure
This document provides information on how to manage and aggregate documentation assets.

## Creating New Assets with Template Engine
Template engine is a processor which generates empty artifacts - including documentation assets - based on artifact templates. The functionality of the template engine is embedded into Jinni.

### Using Template Engine

Generally, every artifact generation request adheres to the following syntax: `jinni request-name --groupId someGroupID --artifactId someArtifactID --version someVersion --installationPath somePath`.

Typically, only --artifactId is mandatory, other arguments take default values. Different requests may take different parameters, as some requests have mandatory parameters which you must append at the end of the Jinni call. A simple command generating a new documentation artifact could look as follows:

`jinni create-markdown-documentation-asset --aid someId`

> For more information about the available options and syntax, see [Artifact Template Types](asset://tribefire.cortex.documentation:development-environment-doc/template_engine/artifact_template_types.md) and [Creating Artifact Templates](asset://tribefire.cortex.documentation:development-environment-doc/template_engine/creating_artifact_templates.md).


## Creating Asset Aggregators
Asset aggregators define dependencies between assets. The examples in this document are based on Maven, however it's possible to manage $mdoc$ dependencies with other tools.

To create an aggregator, you need to do the following:

1. Create a new asset to host the aggregator. You can use the following jinni command:

    `jinni create-asset-aggregator-asset --aid someId`

2. You need to create the parent artifact as well. To do so, execute:

    `jinni create-parent-artifact --aid someId`


3. Add dependencies to your aggregator. You can use the example below as a template:

An aggregator for two assets, `documentation.group.one` and `documentation.group.two`, could look as follows:

```xml

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>documentation.group.one</groupId>
        <artifactId>parent</artifactId>
        <version>[${major}.${minor},${major}.${nextMinor})</version>
    </parent>
    <artifactId>my-documentation-aggregator</artifactId>
    <version>${major}.${minor}.${revision}</version>
    <packaging>pom</packaging>
    <dependencies>
        <dependency>
            <groupId>documentation.group.one</groupId>
            <artifactId>docu-asset-one</artifactId>
            <version>${V.documentation.group.one}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>
        <dependency>
            <groupId>documentation.group.two</groupId>
            <artifactId>docu-asset-two</artifactId>
            <version>${V.documentation.group.two}</version>
            <classifier>asset</classifier>
            <type>man</type>
            <?tag asset?>
        </dependency>        
    <properties>
        <major>2</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <revision>21-pc</revision>
        <archetype>asset</archetype>
    </properties>
</project>

```

### `parent`

The `<parent>` component is a reference to the `parent` artifact in your local repository, which is a `pom.xml` file hosting information relevant to all artifacts in the aggregator, such as versions, common dependencies, properties, and so on. This file is identified by its `groupId` and `artifactId`, as is the case with all assets. In the case of documentation assets, parent is used to host version ranges for artifacts, as in: 

```xml
<groupId>documentation.group.one</groupId>
<artifactId>parent</artifactId>
<version>${major}.${minor}.${revision}</version>
<packaging>pom</packaging>
<properties>
        <major>2</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <revision>9-pc</revision>
        <java.version>1.8</java.version>
        <V.documentation.group.one>[${major}.${minor},${major}.${nextMinor})</V.documentation.group.one>
        <V.documentation.group.two>[2.0,2.1)</V.documentation.group.two>
</properties>

```

In the above situation, the versions are set in the parent (`major`, `minor`, `nextMinor`, `revision`), along with the  version ranges for two assets (note that the range is set via variables for `documentation.group.one` - referring to the above properties - and directly with the [2.0,2.1) range for `documentation.group.two`. These properties are typically managed by a CI tool, which increases the version each time a new artifact is published.

Defining artifact versions in the parent allows us to refer to them in the artifact itself. This is done from the aggregator, inside the dependency itself:

```xml
<dependency>
    <groupId>documentation.group.one</groupId>
    <artifactId>docu-asset-one</artifactId>
    <version>${V.documentation.group.one}</version>
    <classifier>asset</classifier>
    <type>man</type>
    <?tag asset?>
</dependency>
```

`V.documentation.group.one` is defined as a property in the parent, thus we're able to refer to it.

### `artifactId`
This is the ID used to identify this aggregator.

### `version`
Version of this aggregator, in this case referring to `major`, `minor`, and `nextMinor` properties, defined later in the POM.

### `dependency`
Dependencies, i.e. assets pulled by this aggregator. Note the version variables - they are defined in the `parent` artifact.

### `properties`
We're defining the aggregator version via the properties. These are normally managed by a CI system.

## Building the Aggregator

When your documentation aggregator is ready, you can try it out. For the above aggregator, you would have to run the following command:

`jinni package-platform-setup --setupDependency documentation.group.one:my-documentation-aggregator#2.0 --noDocu false`

>Note that the version (2.0 in this case) must lie within the defined range. 