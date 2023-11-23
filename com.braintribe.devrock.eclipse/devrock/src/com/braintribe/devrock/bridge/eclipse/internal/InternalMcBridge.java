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
package com.braintribe.devrock.bridge.eclipse.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

import com.braintribe.devrock.api.concurrent.CustomThreadFactory;
import com.braintribe.devrock.bridge.eclipse.api.McBridge;
import com.braintribe.devrock.bridge.eclipse.internal.wire.InternalMcBridgeWireModule;
import com.braintribe.devrock.bridge.eclipse.internal.wire.contract.InternalMcBridgeContract;
import com.braintribe.devrock.bridge.eclipse.workspace.BasicWorkspaceProjectInfo;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.eclipse.model.reason.devrock.DelegatedResolutionFailure;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionContext;
import com.braintribe.devrock.mc.api.classpath.ClasspathResolutionScope;
import com.braintribe.devrock.mc.api.commons.VersionInfo;
import com.braintribe.devrock.mc.api.repository.configuration.ArtifactChangesSynchronization;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.marshaller.artifact.maven.metadata.DeclaredMavenMetaDataMarshaller;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifact;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.compiled.CompiledTerminal;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.artifact.maven.meta.MavenMetaData;
import com.braintribe.model.artifact.maven.meta.Versioning;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.version.Version;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * implementation of the functionality the plugins require from mc-core
 * 
 * @author pit / dirk 
 *
 */
public class InternalMcBridge implements McBridge {
	private static Logger log = Logger.getLogger(InternalMcBridge.class);
	private WireContext<InternalMcBridgeContract> context;
	private InternalMcBridgeContract contract;

	/**
	 * standard bridge to be scoped
	 */
	public InternalMcBridge() {
		Map<IProject, BasicWorkspaceProjectInfo> projectsInWorkspace = DevrockPlugin.instance().getWorkspaceProjectView().getProjectsInWorkspace();		
		context = Wire.context( new InternalMcBridgeWireModule( new ArrayList<>(projectsInWorkspace.values()), null));
		contract = context.contract();
	}
	
	/**
	 * hidden {@link McBridge} constructor to create a custom bridge with a different configuration
	 * @param repositoryConfiguration - the repository configuration to use 
	 */
	private InternalMcBridge(RepositoryConfiguration repositoryConfiguration) {
		Map<IProject, BasicWorkspaceProjectInfo> projectsInWorkspace = DevrockPlugin.instance().getWorkspaceProjectView().getProjectsInWorkspace();		
		context = Wire.context( new InternalMcBridgeWireModule( new ArrayList<>(projectsInWorkspace.values()), repositoryConfiguration));
		contract = context.contract();
	}
	

	@Override
	public McBridge customBridge(RepositoryConfiguration repositoryConfiguration) {	
		return new InternalMcBridge(repositoryConfiguration);
	}



	@Override
	public Maybe<AnalysisArtifactResolution> resolveClasspath(CompiledTerminal ct, ClasspathResolutionScope resolutionScope) {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build()
															.enrichJar(true)
															.enrichJavadoc(true)
															.enrichSources(true)														
															.scope(resolutionScope)
															.lenient(true)
														.done();
		
				
		try {
			AnalysisArtifactResolution resolution = contract.classpathResolver().resolve(resolutionContext, ct);
			// invalidate resolution if configuration probing has an issue
			RepositoryConfiguration activeConfiguration = contract.repositoryReflection().getRepositoryConfiguration();
			if (activeConfiguration.hasFailed()) {
				attachConfigurationFailureReason( resolution, activeConfiguration.getFailure());				
			}
			return Maybe.complete( resolution);
		} catch (Exception e) {						
			return Maybe.empty( Reasons.build( DelegatedResolutionFailure.T).text(e.getMessage()).toReason());
		}				
	}
	
	/**
	 * either attaches the failure reason of the configuration to the resolution's failure reason, or attaches them directly to the resolution
	 * @param resolution - the {@link AnalysisArtifactResolution} to flag
	 * @param configurationFailureReason - the {@link Reason} why the configuration failed
	 */
	private void attachConfigurationFailureReason(AnalysisArtifactResolution resolution, Reason configurationFailureReason) {
		Reason resolutionFailureReason = resolution.getFailure();
		if (resolutionFailureReason == null) {
			resolution.setFailure(configurationFailureReason);
		}
		else {
			resolutionFailureReason.getReasons().add(configurationFailureReason);
		}		
	}

	@Override
	public Maybe<AnalysisArtifactResolution> resolveClasspath(Collection<CompiledTerminal> cts, ClasspathResolutionScope resolutionScope) {
		ClasspathResolutionContext resolutionContext = ClasspathResolutionContext.build()
															.enrichJar(true)
															.enrichJavadoc(true)
															.enrichSources(true)														
															.scope(resolutionScope)
															.lenient(true)
														.done();
		
				
		try {
			AnalysisArtifactResolution resolution = contract.classpathResolver().resolve(resolutionContext, cts);
			// invalidate resolution if configuration probing has an issue
			RepositoryConfiguration activeConfiguration = contract.repositoryReflection().getRepositoryConfiguration();
			if (activeConfiguration.hasFailed()) {
				attachConfigurationFailureReason( resolution, activeConfiguration.getFailure());				
			}
			return Maybe.complete( resolution);
		} catch (Exception e) {						
			return Maybe.empty( Reasons.build( DelegatedResolutionFailure.T).text(e.getMessage()).toReason());
		}				
	}

	@Override
	public Maybe<CompiledArtifact> readPomFile(File pomFile) {				
		return contract.declaredArtifactCompiler().compileReasoned(pomFile);	
	}

	@Override
	public Maybe<CompiledArtifact> resolve(CompiledArtifactIdentification cai) {	
		Maybe<CompiledArtifact> resolve = contract.compiledArtifactResolver().resolve(cai);
		if (!resolve.isSatisfied()) {
			return resolve;			
		}
		return Maybe.complete( resolve.get());
	}

	@Override
	public Maybe<CompiledArtifactIdentification> resolve(CompiledDependencyIdentification cdi) {
		return contract.dependencyResolver().resolveDependency(cdi);		
	}

	@Override
	public Maybe<File> resolve(CompiledPartIdentification cpi) {
		Maybe<ArtifactDataResolution> optional = contract.artifactResolver().resolvePart(cpi, cpi);
		if (!optional.isSatisfied()) {
			return optional.emptyCast();
		}
		ArtifactDataResolution artifactDataResolution = optional.get();
		Resource resource = artifactDataResolution.getResource();
		if (resource instanceof FileResource) {
			FileResource fResource = (FileResource) resource;
			return Maybe.complete( new File( fResource.getPath()));
		}
		return Maybe.empty( Reasons.build( DelegatedResolutionFailure.T).text("can only return FileResource types here").toReason());
	}
	
	@Override
	public Maybe<RepositoryReflection> reflectRepositoryConfiguration() {
		RepositoryReflection repositoryReflection = contract.repositoryReflection();		
		return Maybe.complete(repositoryReflection); 
	}
	
	@Override
	public List<CompiledArtifactIdentification> matchesFor( CompiledDependencyIdentification cdi) {
		List<VersionInfo> versionInfos = contract.artifactResolver().getVersions(cdi);
		if (versionInfos == null || versionInfos.size() == 0)
			return Collections.emptyList();
		List<CompiledArtifactIdentification> result = new ArrayList<>( versionInfos.size());
		for (VersionInfo info : versionInfos) {
			if (cdi.getVersion().matches( info.version())) {
				CompiledArtifactIdentification cai = CompiledArtifactIdentification.from(cdi, info.version());
				result.add(cai);
			}			
		}
		return result;
	}
	
	
	@Override
	public List<RemoteCompiledDependencyIdentification> retrieveCurrentRemoteArtifactPopulation() {
		RepositoryReflection repositoryReflection = contract.repositoryReflection();
		RepositoryConfiguration repositoryConfiguration = repositoryReflection.getRepositoryConfiguration();
		ArtifactChangesSynchronization changesSynchronization = contract.changesSynchronization();
		List<RemoteCompiledDependencyIdentification> result = new LinkedList<>();
	
		// pre-filter to remove duplicates.. 
		Map<String, Repository> repositoriesToUseMap = new HashMap<>();
		for (Repository repository : repositoryConfiguration.getRepositories()) {
			String changesUrl = repository.getChangesUrl();
			// no changesUrl, no dice (only remotes with RH support remain
			if (changesUrl == null)
				continue;
			// no duplicates (maven based cfg may bring-in to mirrors for 'central' 
			if (!repositoriesToUseMap.containsKey( changesUrl)) {
				repositoriesToUseMap.put( changesUrl, repository);
			}
			else {
				/* nothing */; 
			}
		}
		
		
		for (Repository repository : repositoriesToUseMap.values()) {
			long before = System.nanoTime();
			Maybe<List<VersionedArtifactIdentification>> queryMaybe = changesSynchronization.queryContents( repository);
			long after = System.nanoTime();
			
			String msg = "scanning repository [" + repository.getName() + "] took [" + ((after-before) / 1E6) + "] ms";
			
			if (queryMaybe.isSatisfied()) {
				List<VersionedArtifactIdentification> queryResult = queryMaybe.get();
				List<RemoteCompiledDependencyIdentification> list = queryResult.stream().map( vai -> RemoteCompiledDependencyIdentification.create( vai.getGroupId(), vai.getArtifactId(), vai.getVersion(), repository, null)).collect(Collectors.toList());
				System.out.println( msg + " -> sucessfully returned " + list.size() + " artifacts");
				result.addAll(list);				
			}
			else {
				System.out.println( msg + " -> and failed");	
				DevrockPluginStatus status = new DevrockPluginStatus( queryMaybe.whyUnsatisfied());
				DevrockPlugin.instance().log(status);
			}			
		}
		return result;
	}
	
	

	@Override
	public List<RemoteCompiledDependencyIdentification> retrieveCurrentLocalArtifactPopulation() {
		RepositoryReflection repositoryReflection = contract.repositoryReflection();	
		RepositoryConfiguration repositoryConfiguration = repositoryReflection.getRepositoryConfiguration();
		
		// local repo 
		int numCidsFromLocalRepository = 0;
		List<RemoteCompiledDependencyIdentification> cidsFromLocalRepository = null;
		Repository localRepository = repositoryReflection.getRepository("local");		//	
		String localRepositoryPath = repositoryConfiguration.cachePath();
		if (new File(localRepositoryPath).exists()) {
			cidsFromLocalRepository = scanFilesystemRepository(localRepository, localRepositoryPath);
			numCidsFromLocalRepository = cidsFromLocalRepository.size();
		}
		else {
			DevrockPluginStatus status = new DevrockPluginStatus( "cannot scan sources as cache/local-repository doesn't exist :" + localRepositoryPath, IStatus.ERROR);
			DevrockPlugin.instance().log(status);
		}
		
		// install repo
		List<RemoteCompiledDependencyIdentification> cidsFromInstallRepository = null;
		int numCidsFromInstallRepository = 0;
		Repository installRepository = repositoryReflection.getRepository("install");
		if (installRepository != null && installRepository instanceof MavenFileSystemRepository) {
			MavenFileSystemRepository mfsr = (MavenFileSystemRepository) installRepository;			
			String installRepositoryPath = mfsr.getRootPath();
			File file = new File( installRepositoryPath);
			if (file.exists()) {
				cidsFromInstallRepository = scanFilesystemRepository(installRepository, installRepositoryPath);
				numCidsFromInstallRepository = cidsFromInstallRepository.size();
			}
			else {
				DevrockPluginStatus status = new DevrockPluginStatus( "cannot scan sources as install-repository doesn't exist :" + installRepositoryPath, IStatus.ERROR);
				DevrockPlugin.instance().log(status);
			}
		}
		
		// combine
		List<RemoteCompiledDependencyIdentification> result = new ArrayList<>( numCidsFromLocalRepository + numCidsFromInstallRepository);
		if (numCidsFromLocalRepository > 0) {
			result.addAll(cidsFromLocalRepository);
		}
		if (numCidsFromInstallRepository > 0) {
			result.addAll(cidsFromInstallRepository);
		}
		
		return result;
		
		
	}

	/**
	 * @param origin
	 * @param repositoryPath
	 * @return
	 */
	private List<RemoteCompiledDependencyIdentification> scanFilesystemRepository( Repository origin, String repositoryPath) {
		long before = System.nanoTime();
		List<EnhancedCompiledArtifactIdentification> scanned = new ScanJob().scan( new File(repositoryPath));
		long after = System.nanoTime();		
		double diff = (after - before) / 1E6; // in ms
		
		String msg = "scanning [" + repositoryPath + "] for [" + scanned.size() + "] artifacts took [" + diff + "] ms";
		log.debug(msg);
		System.out.println(msg);
		
		List<RemoteCompiledDependencyIdentification> rcdis = scanned.stream() //
																	.map( ecai -> rcdiFromEcai(ecai, origin)) //																	
																	.collect(Collectors.toList());		
	
		return rcdis;
	}
	
	private RemoteCompiledDependencyIdentification rcdiFromEcai( EnhancedCompiledArtifactIdentification ecai, Repository repository) {
		RemoteCompiledDependencyIdentification rcdi = RemoteCompiledDependencyIdentification.from(ecai);
		rcdi.setRepositoryOrigin(repository);
		return rcdi;
	}

	@Override
	public void close() {
		context.close();
		
	}

	/**
	 * modified to handle maven-metdata-files, actually a copy from the 'git source pom scanner'.
	 * @author petaG/pit
	 *
	 */
	public static class ScanJob {

		// Creating a new thread-pool has almost no overhead in comparison to scanning lots of folders
		// Our tasks are extremely lightweight, so Executors.newCachedThreadPool() would impair performance due to excessive number of threads
		private final ExecutorService executorService = Executors.newFixedThreadPool(//
				Runtime.getRuntime().availableProcessors(), //
				CustomThreadFactory.create().namePrefix("source-scanner"));
		
		private final DeclaredMavenMetaDataMarshaller marshaller = new DeclaredMavenMetaDataMarshaller();

		private final AtomicInteger submittedJobs = new AtomicInteger(0);
		private final CountDownLatch cdl = new CountDownLatch(1);
		private final Map<File, File> poms = new ConcurrentHashMap<>();

		public List<EnhancedCompiledArtifactIdentification> scan(File directory) {
			Set<File> mdFiles = findAllMetaDataIn(directory);

			// This doesn't get faster with parallel stream or even executor service. Arguably slower.
			
			return mdFiles.stream() //
					.map(f -> mdToIdentifications(f)) //
					.flatMap( m -> m.get().stream())
					.filter(id -> id != null) //					
					.collect(Collectors.toList());
		}

		private Set<File> findAllMetaDataIn(File dir) {
			findAllMetadatasInParallel(dir);

			if (submittedJobs.get() > 0)
				await();

			executorService.shutdown();

			return poms.keySet();
		}

		private void await() {
			try {
				cdl.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		private void findAllMetadatasInParallel(File dir) {
			File pom = new File(dir, "maven-metadata-local.xml");
			if (pom.exists()) {
				poms.put(pom, pom);
				return;
			}

			submittedJobs.incrementAndGet();
			executorService.submit(() -> {
				for (File subDir : dir.listFiles(File::isDirectory))
					findAllMetadatasInParallel(subDir);

				if (submittedJobs.decrementAndGet() == 0)
					cdl.countDown();
			});

		}

		private Maybe<List<EnhancedCompiledArtifactIdentification>> mdToIdentifications(File file) {
			try (InputStream in = new FileInputStream(file)) {
				MavenMetaData md = (MavenMetaData) marshaller.unmarshall( in);
				String grp = md.getGroupId();
				String art = md.getArtifactId();				
				Versioning versioning = md.getVersioning();
				if (versioning == null) {
					return Maybe.empty( Reasons.build(NotFound.T).text("No versioning found").toReason());					
				}
				List<EnhancedCompiledArtifactIdentification> ecais = new ArrayList<>();
				for (Version version : versioning.getVersions()) {
					EnhancedCompiledArtifactIdentification ecai = EnhancedCompiledArtifactIdentification.create(grp, art, version, null);
					ecais.add(ecai);
				}				
				
				return Maybe.complete( ecais);
			}
			catch (Exception e) {
				DevrockPluginStatus status = new DevrockPluginStatus( "cannot extract artifact identification from file : " + file.getAbsolutePath(), IStatus.ERROR);
				DevrockPlugin.instance().log(status);				
				return Maybe.empty(Reasons.build(InternalError.T).text( e.getMessage()).toReason());
			}
		}
	}	
}
