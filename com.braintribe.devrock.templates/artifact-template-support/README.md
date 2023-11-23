# Artifact Template Support

Provides additional features related to artifact-template processing.


### Features

##### Artifact Composition

When you want to create a custom request that is basically a collection of various `CreateArtifact` requests, with dependencies between them.

For example, `CreateExtension` artifact could be used to create a structure like this:

```
xyz-api

xyz-deployment-model
    depends:
        deployment-model

xyz-module
    depends:
        xyz-api
        xyz-deyployment-model
```

This artifacts provides tools to implement such logic in a simple way.


