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
package com.braintribe.commons.environment;


import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsExpertFactory;
import com.braintribe.build.artifact.representations.artifact.maven.settings.MavenSettingsReader;
import com.braintribe.build.artifact.representations.artifact.pom.PomExpertFactory;
import com.braintribe.build.artifact.retrieval.multi.cache.CacheFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.interrogation.RepositoryInterrogationClientFactoryImpl;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScope;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.scope.RavenhurstScopeImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.SolutionReflectionExpert;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.impl.RepositoryReflectionImpl;
import com.braintribe.build.artifact.retrieval.multi.repository.reflection.persistence.RepositoryPersistenceException;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifact.retrieval.multi.retrieval.access.RepositoryAccessClientFactoryImpl;
import com.braintribe.devrock.mc.core.filters.AllMatchingArtifactFilterExpert;
import com.braintribe.devrock.mc.core.filters.ArtifactFilterExpert;
import com.braintribe.devrock.mungojerry.plugin.Mungojerry;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.Part;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.artifact.processing.artifact.ArtifactProcessor;
import com.braintribe.model.ravenhurst.data.RepositoryRole;


public class MungojerryEnvironment {
	private static Logger log = Logger.getLogger(MungojerryEnvironment.class);
	private RepositoryReflectionImpl repositoryRegistry;
	private MavenSettingsReader mavenSettingsReader;
	private DependencyResolverFactory resolverFactory;
	private boolean contextIsOpen = false;

	public void openContext() {
		MavenSettingsExpertFactory mavenSettingsfactory = new MavenSettingsExpertFactory();
		
		RavenhurstScope scope = new RavenhurstScopeImpl();
		mavenSettingsReader = mavenSettingsfactory.getMavenSettingsReader();
		mavenSettingsReader.setExternalPropertyResolverOverride( Mungojerry.getInstance().getVirtualPropertyResolver());
		scope.setReader( mavenSettingsReader);
		
		Set<String> inhibitList = new HashSet<String>();		
		scope.setInhibitedRepositoryIds(inhibitList);
		repositoryRegistry = new RepositoryReflectionImpl();
		repositoryRegistry.setInterrogationClientFactory( new RepositoryInterrogationClientFactoryImpl());				
		repositoryRegistry.setAccessClientFactory( new RepositoryAccessClientFactoryImpl());
		repositoryRegistry.setRavenhurstScope(scope);
		repositoryRegistry.setMavenSettingsReader(mavenSettingsReader);
		repositoryRegistry.setLocalRepositoryLocationProvider(mavenSettingsReader);
		repositoryRegistry.setArtifactFilterExpertSupplier( this::passthrough);
		
		PomExpertFactory pomExpertFactory = new PomExpertFactory();		
		resolverFactory = new DependencyResolverFactory();
		resolverFactory.setLocalRepositoryLocationProvider(mavenSettingsReader);
		resolverFactory.setPomExpertFactory(pomExpertFactory);
		resolverFactory.setRepositoryRegistry( repositoryRegistry);
		
		pomExpertFactory.setDependencyResolverFactory(resolverFactory);
		
		pomExpertFactory.setCacheFactory( new CacheFactoryImpl());
		pomExpertFactory.setDependencyResolverFactory(resolverFactory);
		
		contextIsOpen = true;
	}
	
	private ArtifactFilterExpert passthrough(String repoId) {		
		return AllMatchingArtifactFilterExpert.instance;
	}
	 
	
	public void closeContext() {
		repositoryRegistry.closeContext();
	}
	
	public DependencyResolver getResolver() throws RuntimeException {
		if (!contextIsOpen) {
			openContext();
		}
		return resolverFactory.get();		
	}
	
	public MavenSettingsReader getMavenSettingsReader() {
		if (!contextIsOpen) {
			openContext();
		}
		return mavenSettingsReader;
	}
	
	public Part retrievePart( Part part) {
		if (!contextIsOpen) {
			openContext();
		}
		Solution solution = Solution.T.create();
		ArtifactProcessor.transferIdentification(solution, part);
		try {
			SolutionReflectionExpert solutionReflectionExpert = repositoryRegistry.acquireSolutionReflectionExpert( solution);
			String partName = NameParser.buildFileName(part);
			File file = solutionReflectionExpert.getPart( part, partName, RepositoryRole.release);
			if (file != null) {
				part.setLocation( file.getAbsolutePath());
				return part;
			}
					
		} catch (RepositoryPersistenceException e) {
			String msg="cannot access repository reflection";
			log.error( msg, e);
			Mungojerry.log( IStatus.ERROR, msg + " as " + e.getMessage());			
		} 
		return null;
	}


}
