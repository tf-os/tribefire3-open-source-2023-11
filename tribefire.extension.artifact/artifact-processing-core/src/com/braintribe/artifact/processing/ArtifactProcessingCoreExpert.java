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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import com.braintribe.artifact.processing.backend.ArtifactProcessingEnvironmentExpert;
import com.braintribe.artifact.processing.backend.ArtifactProcessingLocalRepositoryLocationExpert;
import com.braintribe.artifact.processing.backend.ArtifactProcessingSettingsPersistenceExpert;
import com.braintribe.artifact.processing.backend.ArtifactProcessingWalkConfigurationExpert;
import com.braintribe.artifact.processing.backend.ArtifactProcessingWalkNotificationListener;
import com.braintribe.artifact.processing.backend.transpose.ArtifactProcessingModelTransposer;
import com.braintribe.artifact.processing.backend.transpose.PlatformAssetTransposer;
import com.braintribe.artifact.processing.wire.ArtifactProcessingPlatformAssetResolvingConfigurationSpace;
import com.braintribe.artifact.processing.wire.PlatformAssetResolvingWireModule;
import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.retrieval.multi.enriching.MultiRepositorySolutionEnricher;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.ReentrantReadWriteLockFactory;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.ArtifactReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RepositoryReflection;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.RetrievalMode;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.walk.multi.Walker;
import com.braintribe.build.artifacts.mc.wire.classwalk.ClasspathResolvers;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ConfigurableClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.Dependency;
import com.braintribe.model.artifact.Identification;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.PartTuple;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.info.ArtifactInformation;
import com.braintribe.model.artifact.info.VersionInfo;
import com.braintribe.model.artifact.processing.ArtifactIdentification;
import com.braintribe.model.artifact.processing.ArtifactResolution;
import com.braintribe.model.artifact.processing.AssetFilterContext;
import com.braintribe.model.artifact.processing.HasArtifactIdentification;
import com.braintribe.model.artifact.processing.PlatformAssetResolution;
import com.braintribe.model.artifact.processing.QualifiedPartIdentification;
import com.braintribe.model.artifact.processing.ResolvedArtifact;
import com.braintribe.model.artifact.processing.cfg.repository.RepositoryConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionConfiguration;
import com.braintribe.model.artifact.processing.cfg.resolution.ResolutionSortOrder;
import com.braintribe.model.artifact.processing.part.PartTupleProcessor;
import com.braintribe.model.artifact.processing.service.data.ArtifactPartData;
import com.braintribe.model.artifact.processing.service.data.ResolvedPlatformAssets;
import com.braintribe.model.artifact.processing.service.request.GetArtifactPartData;
import com.braintribe.model.artifact.processing.service.request.ResolvePlatformAssets;
import com.braintribe.model.artifact.processing.version.VersionMetricTuple;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.artifact.processing.version.VersionRangeProcessor;
import com.braintribe.model.artifact.version.Version;
import com.braintribe.model.artifact.version.VersionRange;
import com.braintribe.model.maven.settings.Settings;
import com.braintribe.model.ravenhurst.data.RepositoryRole;
import com.braintribe.model.resource.Resource;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.module.WireModule;

import tribefire.cortex.asset.resolving.api.PlatformAssetResolvingContext;
import tribefire.cortex.asset.resolving.impl.PlatformAssetResolver;
import tribefire.cortex.asset.resolving.impl.PlatformAssetSolution;

/**
 * transposing from MC's model to the asset's model
 * 
 * @author pit
 *
 */
public class ArtifactProcessingCoreExpert {
	private static ArtifactProcessingEnvironmentExpert environmentExpert = new ArtifactProcessingEnvironmentExpert();
	
	/**
	 * returns a list of versions (as strings) for the given {@link ArtifactIdentification}
	 * @param localRepositoryLocation - the local repository for the extension
	 * @param denotation - the {@link ArtifactIdentification} (at least groupid & artifactId must be present, version may be null)
	 * @param scopeConfiguration 
	 * @return - a {@link List} of versions as {@link String}
	 */
	public static List<String> getArtifactVersions( File localRepositoryLocation, HasArtifactIdentification denotation, RepositoryConfiguration scopeConfiguration, boolean withoutRevision) {
		
		WireContext<ClasspathResolverContract> context = classpathResolverContract( scopeConfiguration, localRepositoryLocation);
		
		// transfer to MC model	
		Identification artifactIdentification = ArtifactProcessingCoreCommons.ensureIdentificationOrDependency( denotation.getArtifact());
		
		List<Version> versions;
		// check if a version has been supplied
		if (artifactIdentification instanceof Dependency) { // fully qualified dependency -> only versions that match the range
			String walkScopeId = UUID.randomUUID().toString();			
			DependencyResolver dependencyResolver = context.contract().dependencyResolver();
			Set<Solution> solutions = dependencyResolver.resolveMatchingDependency(walkScopeId, (Dependency) artifactIdentification);									
			versions = solutions.stream().map( s ->  s.getVersion()).collect( Collectors.toList());

			// filter revisions if required
			if (withoutRevision) {
				versions = VersionProcessor.filterMajorMinorWinners(versions);
			}			
		}
		else { // no version -> all possible versions
			RepositoryReflection repositoryReflection = context.contract().repositoryReflection();
			ArtifactReflectionExpert artifactReflectionExpert = repositoryReflection.acquireArtifactReflectionExpert( artifactIdentification);
			List<Version> reflectedVersions = artifactReflectionExpert.getVersions( RepositoryRole.release);

			// filter revisions if required
			if (withoutRevision) {
				versions = VersionProcessor.filterMajorMinorWinners(reflectedVersions);
			}
			else {
				versions = reflectedVersions;
			}
		}
		
		return versions.stream().map( v -> {
			// filter revisions if required
			if (withoutRevision) {
				VersionMetricTuple t = VersionProcessor.getVersionMetric(v);
				return "" + t.major + "." + t.minor;
			}
			else {
				return VersionProcessor.toString(v);
			}
		}).collect( Collectors.toList());
	}
	
	/**
	 * returns what the system nows about the artifact passed. It contains the local files, and their potential sources 
	 * @param localRepositoryLocation  - the local repository for the extension
	 * @param denotation - the {@link ArtifactIdentification} (this time a dependency) to get the info 
	 * @param scopeConfiguration - the configuration of the repositories, a {@link RepositoryConfiguration}
	 * @return - a {@link List} of {@link VersionInfo}
	 */
	public static List<VersionInfo> getArtifactVersionInfo( File localRepositoryLocation, HasArtifactIdentification denotation, RepositoryConfiguration scopeConfiguration, boolean withoutRevision) {
		
		WireContext<ClasspathResolverContract> context = classpathResolverContract( scopeConfiguration, localRepositoryLocation);
		RepositoryReflection repositoryReflection = context.contract().repositoryReflection();
		
		// transfer to MC model	
		Identification artifactIdentification = ArtifactProcessingCoreCommons.ensureIdentificationOrDependency( denotation.getArtifact());
		ArtifactReflectionExpert artifactReflectionExpert = repositoryReflection.acquireArtifactReflectionExpert( artifactIdentification);			

		// retrieve the version data
		Map<Version,VersionInfo> retrievedMap = artifactReflectionExpert.getVersionData(RepositoryRole.release);
		
		//filter map 
		if (withoutRevision) {
			List<Version> winners = VersionProcessor.filterMajorMinorWinners( retrievedMap.keySet());
			Map<Version,VersionInfo> replacementMap = new HashMap<>();
			for (Version winner : winners) {
				VersionInfo info = retrievedMap.get(winner);
				VersionMetricTuple metric = VersionProcessor.getVersionMetric(winner);
				info.setVersion( String.format("%d.%d", metric.major, metric.minor)); 
				replacementMap.put(winner, info);
			}
			retrievedMap = replacementMap;
			
		}

		final Map<Version,VersionInfo> map = retrievedMap;
		
		// check if a version has been supplied,
		if (artifactIdentification instanceof Dependency) { // fully qualified dependency -> only versions that match the range
			VersionRange range = ((Dependency) artifactIdentification).getVersionRange();			
			List<Version> versions = new ArrayList<>();
			map.keySet().stream().forEach( v -> {
				if (VersionRangeProcessor.matches(range, v)) {
					versions.add(v);
				}
			});
			
			List<VersionInfo> result = versions.stream().map( v -> {
				return map.get(v);
			}).collect( Collectors.toList());
			
			return result;														
		}
		else { // no version -> all possible versions
			return new ArrayList<>(map.values());			
		}
				
	}

	/**
	 * returns the information that the {@link RepositoryReflection} can gather from the stuff stored in the
	 * local repository, most derived from the index files.
	 * @param localRepositoryLocation 
	 * @param denotation - the {@link ArtifactIdentification} (this time a dependency) to get the info
	 * @param mode - if set to {@link RetrievalMode#passive} it will only passively give the information found, 
	 * if set to {@link RetrievalMode#eager} it will actively scan and update the indexes.
	 * @param scopeConfiguration 
	 * @return - the information found as {@link ArtifactInformation}
	 */
	public static ArtifactInformation getArtifactInformation(File localRepositoryLocation, HasArtifactIdentification denotation, RepositoryConfiguration scopeConfiguration) {
		WireContext<ClasspathResolverContract> context = classpathResolverContract( scopeConfiguration, localRepositoryLocation);							
		Solution solution = ArtifactProcessingCoreCommons.determineSolution( denotation.getArtifact(), context.contract().dependencyResolver());
		return context.contract().repositoryReflection().retrieveInformation(solution, RetrievalMode.eager);		
	}
	
	
	
	/**
	 * resolves the passed dependency and retrieves its dependencies - just like Maven/AC
	 * @param localRepositoryLocation 
	 * @param denotation - the {@link ArtifactIdentification}  
	 * @param scopeConfiguration - the configuration of the repositories, a {@link RepositoryConfiguration}
	 * @param walkConfiguration  - the configuration for the resolution, a {@link ResolutionConfiguration}
	 * @return - a {@link List} pf {@link ResolvedArtifact}
	 */
	public static ArtifactResolution getArtifactResolution( File localRepositoryLocation, HasArtifactIdentification denotation, RepositoryConfiguration scopeConfiguration, ResolutionConfiguration walkConfiguration) {
		WireContext<ClasspathResolverContract> context = classpathResolverContract( scopeConfiguration, localRepositoryLocation);

		Solution solution = ArtifactProcessingCoreCommons.determineSolution( denotation.getArtifact(), context.contract().dependencyResolver());
		WalkerContext walkerContext = ArtifactProcessingWalkConfigurationExpert.acquireWalkerContext(walkConfiguration);
		
		Walker walker = context.contract().walker( walkerContext);
				
		String walkScopeId = UUID.randomUUID().toString();
		Collection<Solution> collectedSolutions = walker.walk( walkScopeId, solution);
		context.contract().contextualizedEnricher(walkerContext).enrich( walkScopeId, Collections.singleton( solution));
		
		ResolutionSortOrder sortOrder = null;
		if (walkConfiguration != null) {
			sortOrder = walkConfiguration.getSortOrder();
		}
		
		return ArtifactProcessingModelTransposer.transpose( context.contract().repositoryReflection(), solution, collectedSolutions, sortOrder);			
	}
	
	/**
	 * enriches 
	 * @param localRepositoryLocation
	 * @param denotation
	 * @param scopeConfiguration
	 * @return
	 */
	public static ArtifactPartData getArtifactPartData(File localRepositoryLocation, GetArtifactPartData denotation, RepositoryConfiguration scopeConfiguration) {
		
		String walkScopeId = UUID.randomUUID().toString();
		
		WireContext<ClasspathResolverContract> context = classpathResolverContract( scopeConfiguration, localRepositoryLocation);
		MultiRepositorySolutionEnricher enricher = context.contract().enricher();
		
		ArtifactPartData apd = ArtifactPartData.T.create();
		
		List<QualifiedPartIdentification> parts = new ArrayList<>(); 
				
		parts.addAll(denotation.getParts());
		parts.addAll(denotation.getOptionalParts());
		
		Runnable r = () -> parts.stream().forEach( qpi -> {
			
			Solution solution = Solution.T.create();
			solution.setGroupId( qpi.getGroupId());
			solution.setArtifactId( qpi.getArtifactId());
			solution.setVersion( VersionProcessor.createFromString( qpi.getVersion()));
			
			if (qpi.getType() == null) {			
				throw new IllegalStateException("a type must be passed to the enricher");
			}
			
			PartTuple tuple = PartTupleProcessor.fromString( qpi.getClassifier(), qpi.getType());
			
			Part part = enricher.enrich(walkScopeId, solution, tuple);
			if (part == null) {
				if (denotation.getParts().contains( qpi)) {
					throw new IllegalStateException("no part belonging to [" + NameParser.buildName(solution) + "] found matching [" + PartTupleProcessor.toString(tuple) + "]");
				}
				apd.getData().put( qpi, null);
			}
			
			File file = new File( part.getLocation());		
			Resource resource = Resource.createTransient(() -> new FileInputStream(file));
			
			apd.getData().put( qpi, resource);
		});
		
		ForkJoinPool pool = new ForkJoinPool(4);
		try {
			pool.submit( r).get();
		} catch (Exception e1) {
			;//throw new IllegalStateException("failure during fork pool shutdown", e1);
		}
		finally {
			pool.shutdown();			
		}
	
			
		return apd;
	}
	

	
	
	/**
	 * resolves the passed dependency and retrieves its ALL dependencies - like bt-ant-task's 'repository extract' 
	 * @param localRepositoryLocation 
	 * @param denotation - the {@link ArtifactIdentification}  
	 * @param scopeConfiguration - the configuration of the repositories, a {@link RepositoryConfiguration}
	 * @param exclusions - a list of artifacts that should be excluded
	 * @return - a {@link List} pf {@link ResolvedArtifact}
	 */
	/*
	public static ArtifactResolution getArtifactStructure( File localRepositoryLocation, HasArtifactIdentification denotation, RepositoryConfiguration scopeConfiguration, List<String> exclusions) {
		
		Solution terminal = ArtifactProcessingCoreCommons.determineSolution( denotation.getArtifact(), context.contract().dependencyResolver());
		
		Collection<Solution> collectedSolutions = null;
		ResolutionSortOrder sortOrder = ResolutionSortOrder.alphabetically;
		
		
		
		List<Pattern> parseExclusionPatterns = parseExclusionPatterns(exclusions);
		
		RepositoryExtractFilterConfiguration filterConfiguration = new RepositoryExtractFilterConfiguration();
		
		WireContext<BuildDependencyResolutionContract> wireContext = BuildDependencyResolvers.standard(b -> {  
			b.bindContract(FilterConfigurationContract.class, filterConfiguration);
		});
		
		String walkScopeId = UUID.randomUUID().toString();
		
		try {
			BuildDependencyResolutionContract buildDependencyResolutionContract = wireContext.contract();
			BuildRangeDependencyResolver dependencyResolver = buildDependencyResolutionContract.buildDependencyResolver();
			ArtifactPomReader pomReader = wireContext.contract().pomReader();
						
			
			List<Dependency> dependencies = terminal.getDependencies();
			
			BuildRange buildRange = new BuildRange(dependencies, null, null);
			BuildRangeDependencySolution solutionSet = dependencyResolver.resolve(buildRange);
			
			// output dependency tree if required
			//writeDependencyRelations(solutionSet);
			
			// create fileset
			//exposeAsFileset(buildDependencyResolutionContract, solutionSet);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "error while extracting dependencies of [" + NameParser.buildName(terminal) + "]");
		} finally {
			wireContext.shutdown();
		}
		
		return ArtifactProcessingModelTransposer.transpose( context.contract().repositoryReflection(), terminal, collectedSolutions, sortOrder);			
	}
	*/
	
	
	/**
	 * resolves the passed dependency pointing to a Platform Asset and retrieves its dependencies - just like jinni does
	 * @param localRepositoryLocation 
	 * @param denotation - - the {@link ArtifactIdentification}
	 * @param scopeConfiguration - the configuration of the repositories, a {@link RepositoryConfiguration}
	 * @param assetContext - the {@link AssetFilterContext} which may be null 
	 * @return
	 */
	public static PlatformAssetResolution getAssetResolution(  File localRepositoryLocation, HasArtifactIdentification denotation, RepositoryConfiguration scopeConfiguration, AssetFilterContext assetContext) {
		//

		ArtifactProcessingPlatformAssetResolvingConfigurationSpace cfg = new ArtifactProcessingPlatformAssetResolvingConfigurationSpace();
		VirtualEnvironment ove = environmentExpert.acquireVirtualEnvironment( scopeConfiguration);
		if (ove != null) {
			cfg.setVirtualEnvironment( ove);
		}
		
		ArtifactProcessingWalkNotificationListener walkNotificationListener = new ArtifactProcessingWalkNotificationListener();
		cfg.setPomNotificationListener(walkNotificationListener);
		
		//
		// settings persistence expert
		//
		ArtifactProcessingSettingsPersistenceExpert overrideSettingsPersistenceExpert = new ArtifactProcessingSettingsPersistenceExpert();
		// configure - get ScopeConfiguration from denotation type or access 
		overrideSettingsPersistenceExpert.setScopeConfiguration( scopeConfiguration);
		cfg.setSettingsPersistenceExpert( overrideSettingsPersistenceExpert);
		
		//
		// local repository location expert overload
		//
		ArtifactProcessingLocalRepositoryLocationExpert overrideLocalRepositoryExpert = new ArtifactProcessingLocalRepositoryLocationExpert();		
		// get the settings and the current configured local repository path  
		Settings settings = overrideSettingsPersistenceExpert.loadSettings();
		String repositoryExpression = settings.getLocalRepository();
		overrideLocalRepositoryExpert.setConfiguredExpression( repositoryExpression);
		// get file system root from TF
		if (localRepositoryLocation != null) {
			overrideLocalRepositoryExpert.setLocalRepositoryFilesystemRoot( localRepositoryLocation.getAbsolutePath());
		}
		
		cfg.setLocalRepositoryProvider(overrideLocalRepositoryExpert);
		
		
		WireModule integrationModule = new PlatformAssetResolvingWireModule(cfg);		
		
		PlatformAssetResolvingContext context = new ArtifactProcessingPlatformAssetResolvingContext( assetContext);
		Dependency dependency = ArtifactProcessingCoreCommons.ensureDependency( denotation.getArtifact());
		try (PlatformAssetResolver assetResolver = new PlatformAssetResolver(integrationModule, context, dependency)) {
			if (assetContext == null) {
				assetResolver.setSelectorFiltering(false);
			}
			assetResolver.resolve();
			SortedSet<PlatformAssetSolution> platformAssetSolutions = assetResolver.getSolutions();
		
		String key = dependency.getGroupId() + ":" + dependency.getArtifactId();

		// find terminal 
		PlatformAssetSolution terminal = platformAssetSolutions.stream().filter( p -> {
			String comp = p.solution.getGroupId() + ":" + p.solution.getArtifactId();
			if (key.equalsIgnoreCase( comp)) 
				return true;
			return false;
		}).findFirst().orElse(null);
		
		if (terminal == null) {
			throw new IllegalStateException( "expected terminal [" + key + "] not found");
		}
						
		//return PlatformAssetTransposer.transpose( assetResolver.getWireContext().contract().repositoryReflection(), terminal, platformAssetSolutions);
		return PlatformAssetTransposer.transpose( assetResolver.getWireContext().contract().repositoryReflection(), terminal);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "error while resolving [" + NameParser.buildName(dependency) + "]");
		}
	}
	
	public static ResolvedPlatformAssets resolvePlatformAssets(File localRepositoryLocation, ResolvePlatformAssets denotation, RepositoryConfiguration scopeConfiguration, AssetFilterContext assetContext) {
		ArtifactProcessingPlatformAssetResolvingConfigurationSpace cfg = new ArtifactProcessingPlatformAssetResolvingConfigurationSpace();
		VirtualEnvironment ove = environmentExpert.acquireVirtualEnvironment( scopeConfiguration);
		if (ove != null) {
			cfg.setVirtualEnvironment( ove);
		}
		
		ArtifactProcessingWalkNotificationListener walkNotificationListener = new ArtifactProcessingWalkNotificationListener();
		cfg.setPomNotificationListener(walkNotificationListener);
		
		//
		// settings persistence expert
		//
		ArtifactProcessingSettingsPersistenceExpert overrideSettingsPersistenceExpert = new ArtifactProcessingSettingsPersistenceExpert();
		// configure - get ScopeConfiguration from denotation type or access 
		overrideSettingsPersistenceExpert.setScopeConfiguration( scopeConfiguration);
		cfg.setSettingsPersistenceExpert( overrideSettingsPersistenceExpert);
		
		//
		// local repository location expert overload
		//
		ArtifactProcessingLocalRepositoryLocationExpert overrideLocalRepositoryExpert = new ArtifactProcessingLocalRepositoryLocationExpert();		
		// get the settings and the current configured local repository path  
		Settings settings = overrideSettingsPersistenceExpert.loadSettings();
		String repositoryExpression = settings.getLocalRepository();
		overrideLocalRepositoryExpert.setConfiguredExpression( repositoryExpression);
		// get file system root from TF
		if (localRepositoryLocation != null) {
			overrideLocalRepositoryExpert.setLocalRepositoryFilesystemRoot( localRepositoryLocation.getAbsolutePath());
		}
		
		cfg.setLocalRepositoryProvider(overrideLocalRepositoryExpert);
		
		
		WireModule integrationModule = new PlatformAssetResolvingWireModule(cfg);		
		
		PlatformAssetResolvingContext context = new ArtifactProcessingPlatformAssetResolvingContext( assetContext);
		List<Dependency> dependencies = new ArrayList<>();
		List<String> terminalKeys = new ArrayList<>();
		for (ArtifactIdentification ai : denotation.getAssets()) {
			Dependency dependency = ArtifactProcessingCoreCommons.ensureDependency( ai);
			dependencies.add(dependency);
			terminalKeys.add(dependency.getGroupId() + ":" + dependency.getArtifactId());
		}
		
		try (PlatformAssetResolver assetResolver = new PlatformAssetResolver(integrationModule, context, dependencies)) {
				if (assetContext == null) {
					assetResolver.setSelectorFiltering(false);
				}
				assetResolver.resolve();
				SortedSet<PlatformAssetSolution> platformAssetSolutions = assetResolver.getSolutions();
			
		
	
			// find terminal 
			List<PlatformAssetSolution> terminals = platformAssetSolutions.stream().filter( p -> {
				String comp = p.solution.getGroupId() + ":" + p.solution.getArtifactId();
				if (terminalKeys.contains( comp)) 
					return true;
				return false;
			}).collect(Collectors.toList());
			
			if (terminals.size() != terminalKeys.size()) {
				String msg = "expected [" + toString(dependencies) + "], yet only found [" + pasToString(terminals) + "] in resolution";
				throw new IllegalStateException(msg); 
			}										
			return PlatformAssetTransposer.transpose( assetResolver.getWireContext().contract().repositoryReflection(), terminals);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "error while resolving [" + toString( dependencies) + "]");
		}
	}
	
	private static String toString( List<Dependency> ds) {		
		return ds.stream().map( d -> NameParser.buildName(d)).collect( Collectors.joining(","));				
	}
	private static String pasToString( List<PlatformAssetSolution> pas) {		
		return pas.stream().map( p -> NameParser.buildName( p.solution)).collect( Collectors.joining(","));				
	}
	

	/**
	 * prob'ly requires some configurin' 
	 * @param localRepositoryLocation 
	 * @return
	 */
	public static WireContext<ClasspathResolverContract> classpathResolverContract(RepositoryConfiguration scopeConfiguration, File localRepositoryLocation) {
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		// run time scopes or dynamic? if latter, needs to be wired via walk config rather
		cfg.setScopes( Scopes.runtimeScopes());
		
		// TODO: configure? 
		//cfg.setRelevantPartTuples( tuples... );
		
		//
		// lock
		//
		cfg.setLockFactory( new ReentrantReadWriteLockFactory());
		
		//
		// virtual override
		//
		VirtualEnvironment ove = environmentExpert.acquireVirtualEnvironment( scopeConfiguration);
		if (ove != null) {
			cfg.setVirtualEnvironment( ove);
		}
		
		//
		// listener
		//
		ArtifactProcessingWalkNotificationListener walkNotificationListener = new ArtifactProcessingWalkNotificationListener();
		// TODO: configure listener, flesh it out 
		cfg.setWalkNotificationListener( walkNotificationListener);
		cfg.setClashResolverNotificationListener( walkNotificationListener);
		cfg.setPomReaderNotificationListener(walkNotificationListener);
		
		//
		// settings persistence expert
		//
		ArtifactProcessingSettingsPersistenceExpert overrideSettingsPersistenceExpert = new ArtifactProcessingSettingsPersistenceExpert();
		// configure - get ScopeConfiguration from denotation type or access 
		overrideSettingsPersistenceExpert.setScopeConfiguration( scopeConfiguration);
		cfg.setOverrideSettingsPersistenceExpert( overrideSettingsPersistenceExpert);
		
		//
		// local repository location expert overload
		//
		ArtifactProcessingLocalRepositoryLocationExpert overrideLocalRepositoryExpert = new ArtifactProcessingLocalRepositoryLocationExpert();		
		// get the settings and the current configured local repository path  
		Settings settings = overrideSettingsPersistenceExpert.loadSettings();
		String repositoryExpression = settings.getLocalRepository();
		overrideLocalRepositoryExpert.setConfiguredExpression( repositoryExpression);
		
		// get file system root from TF
		if (localRepositoryLocation != null) {
			overrideLocalRepositoryExpert.setLocalRepositoryFilesystemRoot( localRepositoryLocation.getAbsolutePath());
		}

		//  
		cfg.setOverrideLocalRepositoryExpert( overrideLocalRepositoryExpert);

		
		// get the context 
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		return context;		
	}


	
	
	
}
