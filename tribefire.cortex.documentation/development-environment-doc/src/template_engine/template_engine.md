# Template Engine

Template engine is a processor which generates empty artifacts based on artifact templates. The functionality of the template engine is embedded into Jinni.

> For Tribefire purposes the template engine is mostly used to create artifact templates, but you can create any file structure with it - it does not have to be a artifact.

## General

Creating directory and file structures for different platform asset natures manually is an extremely error-prone process. Template engine reduces the risk of an error by using preexisting artifact templates. Using the template engine, you are sure that the artifact you want to use for your custom implementation has consistent naming, contains all the necessary dependencies, and follows structural best practices.

As mentioned above, the functionality of the template engine is exposed via Jinni, which means you must set up Jinni on your machine to be able to generate empty artifacts.

## Jinni

[](asset://tribefire.cortex.documentation:includes-doc/jinni_setup.md?INCLUDE)

> For information about using Jinni to set up Tribefire, see [Quick Installation](asset://tribefire.cortex.documentation:development-environment-doc/quick_installation_devops.md).


## Using Template Engine

Generally, every artifact generation request adheres to the following syntax: `jinni request-name --groupId someGroupID --artifactId someArtifactID --version someVersion --installationPath somePath`.

Normally, only `--artifactId` is mandatory and other information is defaulted. Different requests may take different parameters, as some requests have mandatory parameters which you must append at the end of the Jinni call.

> For more information, see [Artifact Template Types](artifact_template_types.md) and [Creating Artifact Templates](creating_artifact_templates.md).