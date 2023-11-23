# Version Management

As platform assets are Maven-based, they support versioning. This document presents the best practices we would like you to follow in terms of assigning and managing versions of your assets.

## General

The main idea behind platform assets version management is to use properties in your `pom.xml` to have a single place where artifact versions are managed. This avoids redundancies and makes sure you don't accidentally use different versions of the same artifact within an artifact group. An example `pom.xml` may look like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>tribefire.cortex.documentation</groupId>
        <artifactId>parent</artifactId>
        <version>[${major}.${minor},${major}.${nextMinor})</version>
    </parent>
    <artifactId>your-platform-asset</artifactId>
    <version>${major}.${minor}.${revision}</version>
    <packaging>pom</packaging>
    <dependencies>
        <dependency>
             <groupId>com.braintribe.gm</groupId>
             <artifactId>root-model</artifactId>
             <version>${V.com.braintribe.gm}</version>
        </dependency>
    </dependencies>
    <properties>
        <major>2</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <revision>1-pc</revision>
        <archetype>asset</archetype>
    </properties>
</project>
```

## Property Placeholders

Let's take a closer look at the individual version element properties:

Property Reference | Description
--------------- | -----------
`${major}`      | Major part of the version. For example, in the `1.90.5` version, `1` is the major part of the version.
`${minor}`     | Minor part of the version. For example, in the `1.90.5` version, `90` is the minor part of the version.
`${nextMinor}`  | Indicator of the minor version that will follow the current minor version. The value of `${nextMinor}` must always be the value of `${minor}` + 1, so if `${minor}` is `90`, `${nextMinor}` must be `91` (90 + 1).
`${revision}`   | Revision part of the version. For example, in the `1.90.5` version, `5` is the revision part of the version.
`${V.com.braintribe.gm}` | Version of the artifact taken from the parent POM. See the [Parent POM](#parent-pom) section for more information.

Using the `${propertyName}` syntax allows you to reference properties from the `<properties>` section of your POM in the rest of the POM. For more information, see the [Properties](#properties) section.

> There could be situations where you add a version directly (and not via a property), therefore using properties is not a rule, but a convention and a best practice for tribefire developers.

While the official Maven documentation states that variables may occur anywhere in a POM, the current Maven implementations (command line and Eclipse plugin) cannot process variables in version tags.
That is why we automatically resolve the variables when the artifact is installed or deployed and replace the dynamic value with a static version tag.

## <parent> Tag

The `<parent>` tag provides the information about the parent POM. Even though it is not mandatory to use parent POMs, we do support this feature.

Parent POMs are used to group artifacts together - you can the use the parent POM to define major and minor versions, versions of dependencies, etc. If you have common elements shared between a group of POMs, you can place them in the parent POM for clarity. Tribefire artifacts always have a parent element, because this is the convention we decided to adopt. See the [Parent POM](#parent-pom) section for more information.

It is not necessary to follow the parent POM approach. If you want, you can define your dependencies explicitly in every artifact.

> For more information about Maven POMs see [Maven POM documentation](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html).

### Syntax of the `<version>` tag

The `<version>` tag contains the version variables in the following syntax (spaces added for clarity): `[ ${major} . ${minor} , ${major} . ${nextMinor} )`. The interval syntax is used not only for the parent, but in general when specifying versions of other artifacts.

When specifying the version of other tribefire artifacts, we don't specify a concrete version but instead use Maven ranges. That way one automatically gets hotfixes and minor improvements without having to micromanage versions. The recommended best practice is to get the latest version within a fixed major/minor version. For example, we want the latest version `1.8.x`. (but not `1.9.y`).

The `[` means the version range (interval) includes the first element in the declaration, while `)` element means the range doesn't include last element. Let's consider the following example: `[1.8,1.9)`. This version declaration means that the version range includes all releases from `1.8` (so `1.8.1`, `1.8.32`, etc.) until (but not including) `1.9`.

As you can see above, the version declaration follows the standard Maven convention. For more information about Maven version conventions, see [Maven Version Ranges](https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html).

## Properties

The `${propertyName}` syntax you find in the POM is a way of referencing properties. You provide the actual value for the `${propertyName}` in the `<properties>` section of your POM, so `${major}` provides the value of what's inside the `<major>` tag.

```xml
<properties>
        <major>2</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <revision>1-pc</revision>
        <archetype>asset</archetype>
</properties>
```

You can, of course, provide each version explicitly, but referencing a property using a local variable is an easier way of providing values for versions.

Internal developers may see Artifacts which have a `-pc` suffix in their version. This stands for *publishing candidate*, which is an artifact version which has not been published yet, i.e. it's only available in the developer's local repository (and it has been built locally by the developer). 

This means that if Maven compares two versions of the same artifact, the one with pc is 'older' than the one without `-pc`. It is, however, more recent than the previous version. For example, given versions `1.2.3` and `1.2.3-pc`, the one with `-pc` is older (`1.2.3` is the latest one). At the same time, given versions `1.2.2` and `1.2.3-pc`, version `1.2.2` is the older one.

The `-pc` element normally signifies that an artifact has been installed to your local repository and has priority during building. Note that the `-pc` suffix is removed automatically by the CI before the artifact is published to Artifactory.

## Parent POM

As we follow the parent POM approach, certain elements used across a range of artifacts of a group are defined in its parent POM. Much like in the `<parent>` element of a normal artifact's POM, we use property placeholders to inject version ranges.

> Note that it is not a requirement to have a parent POM or use property placeholders. If you want, you can define each dependency explicitly.

Example parent POM can look like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>tribefire.cortex.documentation</groupId>
    <artifactId>parent</artifactId>
    <version>${major}.${minor}.${revision}</version>
    <packaging>pom</packaging>
    <properties>
        <major>2</major>
        <minor>0</minor>
        <nextMinor>1</nextMinor>
        <revision>1-pc</revision>
        <java.version>1.8</java.version>
        <V.tribefire.cortex.documentation>[${major}.${minor},${major}.${nextMinor})</V.tribefire.cortex.documentation>
        <V.tribefire.extension.demo>[2.0,2.1)</V.tribefire.extension.demo>
        <V.tribefire.extension.simple>[2.0,2.1)</V.tribefire.extension.simple>
    </properties>
</project>
```

As you can see, the `<properties>` section in the parent POM contains some of the elements you saw in a normal artifact's POM, namely:

* `<major>`
* `<minor>`
* `<nextMinor>`
* `<revision>`

> Those properties describe the same elements as mentioned above. For more information on them see the [Properties](#properties) section. 

You can also find other properties in the parent POM. Let's take the parent POM above as an example:

* `<java.version>`, which specifies which version of Java should be used for all artifacts in this group
* `<V.tribefire.cortex.documentation>[${major}.${minor},${major}.${nextMinor})</V.tribefire.cortex.documentation>`, which is this group's version range declaration
* `<V.tribefire.extension.demo>[2.0,2.1)</V.tribefire.extension.demo>`, which is other groups' version range declarations. Each group any of the artifacts in your group depends on must be explicitly placed here along with its intended version range. This version is then shared across all artifacts in this group which depend on that group to ensure all artifacts use the same version.


The syntax for providing a version range for a group is as follows (spaces added for clarity) `< V . groupId > ` + version range (see [`Syntax of the <version> tag](#Syntax-of-the-<version>-tag) + `</ V . groupId > `. 

> You must provide one line per group.

You can, of course, hardcode the versions in each artifact's POM, but using a parent POM and the `V.groupId` notation allows you to manage all the dependencies from one place.

If you decide not to use this approach, you will need to create a `<dependencyManagement>` section and add the dependencies there. This has some downsides though. For example, when using classifiers and/or types in dependency declarations, the dependency declaration in dependency management section must match exactly (i.e. have same classifier and type). It may even be necessary to declare the same dependency multiple times in dependency management section (e.g. when depending on multiple classifiers). We recommend using properties as we find it to be easier and clearer.