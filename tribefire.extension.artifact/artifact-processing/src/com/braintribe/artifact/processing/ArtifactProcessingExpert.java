// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.artifact.processing;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.artifact.info.VersionInfo;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.AssetFilterContext;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.PlatformAssetResolution;
import com.braintribe.model.artifact.processing.QualifiedPartIdentification;
import com.braintribe.model.artifact.processing.cfg.asset.AssetContextConfiguration;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration;
import com.braintribe.model.artifact.processing.service.data.ArtifactPartData;
import com.braintribe.model.artifact.processing.service.data.HasAssetContextName;
import com.braintribe.model.artifact.processing.service.data.HasRepositoryConfigurationName;
import com.braintribe.model.artifact.processing.service.data.HasResolutionConfigurationName;
import com.braintribe.model.artifact.processing.service.data.ResolvedPlatformAssets;
import com.braintribe.model.artifact.processing.service.request.ArtifactProcessingRequest;
import com.braintribe.model.artifact.processing.service.request.GetArtifactInformation;
import com.braintribe.model.artifact.processing.service.request.GetArtifactPartData;
import com.braintribe.model.artifact.processing.service.request.GetArtifactVersionInfos;
import com.braintribe.model.artifact.processing.service.request.GetArtifactVersions;
import com.braintribe.model.artifact.processing.service.request.ResolveArtifactDependencies;
import com.braintribe.model.artifact.processing.service.request.ResolvePlatformAssetDependencies;
import com.braintribe.model.artifact.processing.service.request.ResolvePlatformAssets;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.resource.Resource;

public class ArtifactProcessingExpert extends AbstractDispatchingServiceProcessor<ArtifactProcessingRequest, Object> {
	private Supplier<PersistenceGmSession> sessionSupplier;
	private File localRepositoryLocation;

	@Configurable @Required
	public void setSessionSupplier(Supplier<PersistenceGmSession> sessionSupplier) {
		this.sessionSupplier = sessionSupplier;
	}
	@Configurable @Required
	public void setLocalRepositoryLocation(File localRepositoryLocation) {
		this.localRepositoryLocation = localRepositoryLocation;
	}
	
	@Override
	protected void configureDispatching(DispatchConfiguration<ArtifactProcessingRequest, Object> dispatching) {
		dispatching.register(GetArtifactVersions.T, this::getArtifactVersions);
		dispatching.register(GetArtifactVersionInfos.T, this::getArtifactVersionInfos);
		dispatching.register(GetArtifactInformation.T, this::getArtifactInformation);		
				
		dispatching.register( ResolveArtifactDependencies.T, this::getArtifactResolution);
		
		dispatching.register( ResolvePlatformAssetDependencies.T, this::getAssetResolution);
		
		dispatching.register( GetArtifactPartData.T, this::getArtifactPartData);
		
		dispatching.register( ResolvePlatformAssets.T, this::resolvePlatformAssets);
		
	}
	
	/**
	 * feature around artifact versions
	 * @param requestContext - the {@link ServiceRequestContext} context
	 * @param denotation - the {@link GetArtifactVersions} denotation
	 * @return - {@link List} of found versions as {@link String}
	 */
	private List<String> getArtifactVersions( ServiceRequestContext requestContext, GetArtifactVersions denotation) {	
		// 
		RepositoryConfiguration scopeConfiguration = acquireScopeConfiguration( (HasRepositoryConfigurationName) denotation);
		return ArtifactProcessingCoreExpert.getArtifactVersions(localRepositoryLocation, (HasArtifactIdentification) denotation, scopeConfiguration, denotation.getWithoutRevision());
	}
	
	/**
	 * feature around artifact versions
	 * @param requestContext - the {@link ServiceRequestContext} context
	 * @param denotation - the {@link GetArtifactVersions} denotation
	 * @return - {@link List} of found versions as {@link String}
	 */
	private List<VersionInfo> getArtifactVersionInfos( ServiceRequestContext requestContext, GetArtifactVersionInfos denotation) {	
		// 
		RepositoryConfiguration scopeConfiguration = acquireScopeConfiguration( (HasRepositoryConfigurationName) denotation);
		return ArtifactProcessingCoreExpert.getArtifactVersionInfo( localRepositoryLocation, (HasArtifactIdentification) denotation, scopeConfiguration, denotation.getWithoutRevision());
	}
	
	/**
	 * feature for passive artifact information retrieval - does not request info it not present
	 * @param requestContext - the {@link ServiceRequestContext}
	 * @param denotation - the {@link GetArtifactInformation} denotation 
	 * @return - an {@link ArtifactInformation} 
	 */
	private ArtifactInformation getArtifactInformation( ServiceRequestContext requestContext, GetArtifactInformation denotation) {
		RepositoryConfiguration scopeConfiguration = acquireScopeConfiguration( (HasRepositoryConfigurationName) denotation);		
		return ArtifactProcessingCoreExpert.getArtifactInformation( localRepositoryLocation, (HasArtifactIdentification) denotation, scopeConfiguration);
	}	
	
	
	/**
	 * feature to retrieve the dependencies of an artifact  
	 * @param requestContext - the {@link ServiceRequestContext}
	 * @param denotation - the {@link GetTransitiveDependencies} denotation
	 * @return - a {@link ArtifactResolution} which contains both the project dependency (terminal solution) plus the classpath 
	 */
	private ArtifactResolution getArtifactResolution( ServiceRequestContext requestContext, ResolveArtifactDependencies denotation) {
		RepositoryConfiguration scopeConfiguration = acquireScopeConfiguration( (HasRepositoryConfigurationName) denotation);
		ResolutionConfiguration walkConfiguration = acquireWalkConfiguration( (HasResolutionConfigurationName) denotation);
		return ArtifactProcessingCoreExpert.getArtifactResolution(localRepositoryLocation, denotation, scopeConfiguration, walkConfiguration);
	}
	
	/**
	 * feature to run an asset retrieval - with a directly configured {@link AssetFilterContext} if any
	 * @param requestContext - the {@link ServiceRequestContext}
	 * @param denotation - the {@link ResolvePlatformAssetDependencies} denotation
	 * @return - a {@link ArtifactResolution} which contains both the project dependency (terminal solution) plus the classpath
	 */
	private PlatformAssetResolution getAssetResolution( ServiceRequestContext requestContext, ResolvePlatformAssetDependencies denotation) {
		RepositoryConfiguration scopeConfiguration = acquireScopeConfiguration( (HasRepositoryConfigurationName) denotation);		
		return ArtifactProcessingCoreExpert.getAssetResolution(localRepositoryLocation, denotation, scopeConfiguration, denotation.getAssetContext());
	}
	
	
	/**
	 * feature to enrich n artifact's parts
	 * @param context - the {@link ServiceRequestContext}
	 * @param denotation - the {@link GetArtifactPartData} denotation, containing {@link QualifiedPartIdentification}
	 * @return - a {@link ArtifactPartData} which contains the {@link QualifiedPartIdentification} plus their {@link Resource}
	 */
	private ArtifactPartData getArtifactPartData( ServiceRequestContext context, GetArtifactPartData denotation) {
		RepositoryConfiguration scopeConfiguration = acquireScopeConfiguration( (HasRepositoryConfigurationName) denotation);
		return ArtifactProcessingCoreExpert.getArtifactPartData( localRepositoryLocation, denotation, scopeConfiguration);
	}
	
	private ResolvedPlatformAssets resolvePlatformAssets( ServiceRequestContext context, ResolvePlatformAssets denotation) {
		RepositoryConfiguration scopeConfiguration = acquireScopeConfiguration( (HasRepositoryConfigurationName) denotation);
		return ArtifactProcessingCoreExpert.resolvePlatformAssets( localRepositoryLocation, denotation, scopeConfiguration, denotation.getAssetContext());
	}
	
	
	
	/**
	 * acquire a {@link RepositoryConfiguration} from the supplied session 
	 * @param hasRepositoryConfiguration - the denotation type part that contains the name of the {@link RepositoryConfiguration}
	 * @return - a valid {@link RepositoryConfiguration} or null if none found
	 */
	private RepositoryConfiguration acquireScopeConfiguration( HasRepositoryConfigurationName hasRepositoryConfiguration) {
		RepositoryConfiguration repositoryConfiguration = null;
		
		String configurationName = hasRepositoryConfiguration.getRepositoryConfigurationName();
		if (configurationName == null) {
			// we could create a default here (to overload the expert's default on null values
			return repositoryConfiguration;
		}
		else {			
			EntityQuery entityQuery = EntityQueryBuilder.from( RepositoryConfiguration.T).where().property("name").eq(configurationName).done();			
			repositoryConfiguration = sessionSupplier.get().query().entities(entityQuery).first();
			if (repositoryConfiguration == null) {
				// warn ? honk? fallback? 
				// notify via channel, then throw IllegalArgumentException
				// 
			}
		}
		
		return repositoryConfiguration;
	}
	
	/**
	 * acquire a {@link ResolutionConfiguration} from the supplied session 
	 * @param hasResolutionConfiguration - the denotation type part that contains the name of the {@link ResolutionConfiguration}
	 * @return - a valid {@link ResolutionConfiguration} or null if not found 
	 */
	private ResolutionConfiguration acquireWalkConfiguration( HasResolutionConfigurationName hasResolutionConfiguration) {
		ResolutionConfiguration resolutionConfiguration = null;
		
		String configurationName = hasResolutionConfiguration.getResolutionConfigurationName();
		if (configurationName == null) {
			// we could create a default here (to overload the expert's default on null values
			return resolutionConfiguration;
		}
		else {
			EntityQuery entityQuery = EntityQueryBuilder.from( ResolutionConfiguration.T).where().property("name").eq(configurationName).done();			
			resolutionConfiguration = sessionSupplier.get().query().entities(entityQuery).first();
			if (resolutionConfiguration == null) {
				// warn ? honk? fallback?
				// notify via channel, then throw IllegalArgumentException
			}
		}
		
		return resolutionConfiguration;
	}
	
	/**
	 * @param hasAssetContextName
	 * @return
	 */
	@SuppressWarnings("unused")
	private AssetFilterContext acquireAssetContext( HasAssetContextName hasAssetContextName) {
		AssetFilterContext assetContext = null;
		String assetContextName = hasAssetContextName.getAssetContextName();
		if (assetContextName == null) {
			// we could create a default here (to overload the expert's default on null values
			return assetContext;
		}
		else {
			EntityQuery entityQuery = EntityQueryBuilder.from( AssetContextConfiguration.T).where().property("name").eq( assetContextName).done();			
			AssetContextConfiguration assetContextConfiguration = sessionSupplier.get().query().entities(entityQuery).first();
			if (assetContextConfiguration == null) {
				// warn ? honk? fallback?
				// notify via channel, then throw IllegalArgumentException
			}
			else {
				assetContext = AssetFilterContext.T.create();
				assetContext.setRuntime( assetContextConfiguration.getIsRuntime());				
				assetContext.setTags( assetContextConfiguration.getTags());
				assetContext.setStage( assetContextConfiguration.getStage());
			}
		}
		return assetContext;
	}
}
