# DevRock

The DevRock Suite is a set of dependency, build, and environment management tools which help you develop with tribefire.

You can use any IDE you prefer as long as it supports a Maven/Ant build solution, but we strongly recommend to use Eclipse because of its integration with the DevRock suite.

As part of DevRock Suite, Braintribe provides a series of tools that integrate into the Eclipse IDE to help working with models, tribefire components, artifacts and dependencies.

## DevRock Components

Component    | Description  
------- | -----------
[Artifact Container](artifact_container.md) | Organizes and resolves dependencies when working with artifacts which have a Maven-compatible dependency declaration.
[Greyface](greyface.md) | Organizes dependencies by importing artifacts from several Maven repositories in a controlled manner.
[Virtual Environment](virtual_environment.md) | Maintains different development setups by overriding system properties and environment variables.
[Model Builder](#model-builder) | Allows you attach a model nature to an Eclipse project.
[Mungo Jerry](#mungo-jerry) | Manages GWT settings.


## Installing DevRock in Eclipse
To install DevRock in Eclipse, do the following:

1. Select **Help/Install New Software...**
2. Click **Add...** to open the Add Repository prompt window.
3. In **Name:** Call the DevRock repository any name you like.
4. In **Location:** Enter https://kwaqwagga.ch/devrock-site. This is the release site for DevRock tools.
5. Click **Add**, then select all available DevRock components.
6. Click **Finish**. The installation starts. Eclipse will restart in the process. When it does, check for **DevRock** under **Window/Preferences** to verify that it has been installed.
    > You may need to create a new workspace to run Eclipse after DevRock has been installed.

## Model Builder

The model builder plugin is an Eclipse plugin that can attach a specific nature to an Eclipse project. You can assign or remove a model nature by right-clicking the model project in package explorer and selecting the appropriate option.

Also, the plugin implements a builder which creates a small `.xml` file that declares the model dependencies of the model and its declared types by itself. The builder adds a build step to Eclipse Java build process which creates the model declaration file in its internal class repository. By default, the file is `<artifact>/classes/model-declaration.xml` and contains the following:

* a list of the model dependencies, i.e. all models that the current model should inherit types from.
* a list of types the model actually defines by itself
* a hash map to help tribefire's model management detect changes.

> It is only intended for model artifacts version `TF#2.0` or higher.

## Mungo Jerry

Mungo Jerry (MJ) is only intended to be used by developers working with GWT. MJ helps to manage GWT-specific settings and has an interceptor to make such projects debuggable in Eclipse.
