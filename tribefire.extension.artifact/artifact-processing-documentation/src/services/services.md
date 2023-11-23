services
========

All of the services of the extension require a [repository configuration](../configuration/configuration.md). Some of the services however also require a [resolution configuration](../configuration/resolution/configuration.md). Both configrations need to be uploaded/instantiated and then referenced via their id in the actual service request.

You can either check the [javadoc](javadoc:com.braintribe.model.artifact.processing.service.ArtifactProcessingRequest) directly, or continue reading about the requests below.

All services listed here treat the initial artifact you supply specially. Depending on the version you specify, it is either expanded into a 'hot fix' range, or taken as is, so abc.xyz#1.0 will be considered as abc.xyz#[1.0, 1.1), abc.xyz#1.0.5 will remain unchanged - see more about that in the section about how to specify the [artifact](./artifactidentification.md).

the extension provides features in three different areas:

- artifact versioning  
  The service type [GetArtifactVersions](javadoc:com.braintribe.model.artifact.processing.service.GetArtifactVersions) requires the two following parameters:  

   [hasArtifactIdentification](javadoc:com.braintribe.model.artifact.processing.HasArtifactIdentification) which contains the information about the [artifact](./artifactidentification.md) in question, and  
   [hasRepositoryConfiguration](javadoc:com.braintribe.model.artifact.processing.service.HasRepositoryConfigurationName) which contains the name of the respective [repository configuration](../configuration/repository/configuration.md).  

  The service returns a list of versions as strings.

- artifact & artifact-part information    
 The two service types of this feature require both the following parameters:  

 [hasArtifactIdentification](javadoc:com.braintribe.model.artifact.processing.HasArtifactIdentification)  which contains the information about the [artifact](./artifactidentification.md) in question, and   [hasRepositoryConfiguration](javadoc:com.braintribe.model.artifact.processing.service.HasRepositoryConfigurationName)  which contains the name of the respective [repository configuration](../configuration/repository/configuration.md).  

 The service returns a [ResolvedArtifact](javadoc:com.braintribe.model.artifact.processing.ResolvedArtifact).  

 The service type [GetArtifactInformation](javadoc:com.braintribe.model.artifact.processing.service.GetArtifactInformation) will download the minimal amount of data required to identify the artifact and return the index information (if available).

- artifact & dependency resolutions  
  The service of this feature requires the following three parameters:  
[hasArtifactIdentification](javadoc:com.braintribe.model.artifact.processing.HasArtifactIdentification)  which contains the information about the [artifact](./artifactidentification.md) in question,     [hasRepositoryConfiguration](javadoc:com.braintribe.model.artifact.processing.service.HasRepositoryConfigurationName)  which contains the name of the respective [repository configuration](../configuration/repository/configuration.md), and
[hasResolutionConfiguration](javadoc:com.braintribe.model.artifact.processing.service.HasResolutionConfigurationName)  which contains the name of the respective [resolution configuration](../configuration/resolution/configuration.md).

  This service is an analogue to a classpath resolution (for compile, launch and test use cases).

  The service type [ResolveArtifactDependencies](javadoc:com.braintribe.model.artifact.processing.service.ResolveArtifactDependencies) returns [ArtifactResolution](javadoc:com.braintribe.model.artifact.processing.ArtifactResolution), which contains both the  [ResolvedArtifact](javadoc:com.braintribe.model.artifact.processing.ResolvedArtifact) - the initial artifact - and a list of [ResolvedArtifact](javadoc:com.braintribe.model.artifact.processing.ResolvedArtifact) which is the full (flattened) list of the transitive dependencies.

- platform asset and dependency resolutions  
The service of this feature requires the following two parameters:  
   [hasArtifactIdentification](javadoc:com.braintribe.model.artifact.processing.HasArtifactIdentification)  which contains the information about the [artifact](./artifactidentification.md) in question, and   [hasRepositoryConfiguration](javadoc:com.braintribe.model.artifact.processing.service.HasRepositoryConfigurationName)  which contains the name of the respective [repository configuration](../configuration/repository/configuration.md).  

   The service type [ResolvePlatformAssetDependencies](javadoc:com.braintribe.model.artifact.processing.service.request.ResolvePlatformAssetDependencies) returns [PlatformAssetResolution](javadoc:com.braintribe.model.artifact.processing.PlatformAssetResolution) which contains both resolved [ResolvedPlatformAsset](javadoc:com.braintribe.model.artifact.processing.ResolvedPlatformAsset) and a list of [ResolvedPlatformAsset](javadoc:com.braintribe.model.artifact.processing.ResolvedPlatformAsset) which is the full (flattened) list of the transitive dependencies.
