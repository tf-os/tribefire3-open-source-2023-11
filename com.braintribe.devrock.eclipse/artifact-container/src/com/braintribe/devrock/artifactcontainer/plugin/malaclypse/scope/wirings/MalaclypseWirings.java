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
package com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings;

import java.util.Arrays;

import com.braintribe.build.artifact.representations.artifact.maven.settings.OverrideableVirtualEnvironment;
import com.braintribe.build.artifact.retrieval.multi.ravenhurst.persistence.lock.FilesystemSemaphoreLockFactory;
import com.braintribe.build.artifact.retrieval.multi.resolving.DependencyResolver;
import com.braintribe.build.artifacts.mc.wire.classwalk.ClasspathResolvers;
import com.braintribe.build.artifacts.mc.wire.classwalk.context.WalkerContext;
import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ClasspathResolverExternalContract;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.ConfigurableClasspathResolverExternalSpace;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.PartTuples;
import com.braintribe.build.artifacts.mc.wire.classwalk.external.contract.Scopes;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.control.walk.resolver.WorkspaceDependencyResolver;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.overrides.OverrideLocalRepositoryRetrievalExpert;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.overrides.OverrideMavenSettingsPersistenceExpert;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.model.malaclypse.cfg.denotations.clash.ResolvingInstant;
import com.braintribe.wire.api.context.WireContext;

public class MalaclypseWirings {
	
	private static DependencyResolver attachWorkspaceResolver(DependencyResolver delegate) {
		WorkspaceDependencyResolver workspaceDependencyResolver = new WorkspaceDependencyResolver();
		workspaceDependencyResolver.setDelegate(delegate);
		return workspaceDependencyResolver;
	}

	/**
	 * returns a classpath resolver contract with full access - to Workspace Registry for instance
	 * @return - a {@link WireContext} of {@link ClasspathResolverContract} with a {@link WorkspaceDependencyResolver}
	 */
	public static WireContext<ClasspathResolverContract> fullClasspathResolverContract() {
		
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.runtimeScopes());
		
 
		//cfg.setRelevantPartTuples( tuples... );
		
		cfg.setDependencyResolverEnricher( MalaclypseWirings::attachWorkspaceResolver);
		//
		// lock
		//
		cfg.setLockFactory( new FilesystemSemaphoreLockFactory());
		
		//
		// virtual override
		//
		if (VirtualEnvironmentPlugin.getOverrideActivation()) {
			OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
			ove.setEnvironmentOverrides( VirtualEnvironmentPlugin.getEnvironmentOverrides());
			ove.setPropertyOverrides( VirtualEnvironmentPlugin.getPropertyOverrides());
			cfg.setVirtualEnvironment(ove);
		}
				
		
		//
		// listener
		//
		/*
		ArtifactProcessingWalkNotificationListener walkNotificationListener = new ArtifactProcessingWalkNotificationListener();
		// TODO: configure listener, flesh it out 
		cfg.setWalkNotificationListener( walkNotificationListener);
		cfg.setClashResolverNotificationListener( walkNotificationListener);
		cfg.setPomReaderNotificationListener(walkNotificationListener);
		*/
		
		//
		// settings persistence expert
		//
		OverrideMavenSettingsPersistenceExpert overrideSettingsPersistenceExpert = new OverrideMavenSettingsPersistenceExpert();
		// configure - get ScopeConfiguration from denotation type or access 		
		overrideSettingsPersistenceExpert.setPropertyResolver( ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver());
		cfg.setOverrideSettingsPersistenceExpert( overrideSettingsPersistenceExpert);
		
		//
		// local repository location expert overload
		//
		OverrideLocalRepositoryRetrievalExpert overrideLocalRepositoryExpert = new OverrideLocalRepositoryRetrievalExpert();
		overrideLocalRepositoryExpert.setPropertyResolver( ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver());
		cfg.setOverrideLocalRepositoryExpert( overrideLocalRepositoryExpert);

		ResolvingInstant resolvingInstant = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDynamicContainerPreferences().getClashResolvingInstant();
		cfg.setResolvingInstant( resolvingInstant);
		
		// get the context 
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		return context;		
	}
	
	/**
	 * returns a classpath resolver contract without full access - standard MC, no workspace connection
	 * @return - a {@link WireContext} of {@link ClasspathResolverContract} without the {@link WorkspaceDependencyResolver}
	 */
	public static WireContext<ClasspathResolverContract> basicClasspathResolverContract() {
		
		ConfigurableClasspathResolverExternalSpace cfg = new ConfigurableClasspathResolverExternalSpace();
		
		cfg.setScopes( Scopes.runtimeScopes());
						
		//
		// lock
		//
		cfg.setLockFactory( new FilesystemSemaphoreLockFactory());
		
		//
		// virtual override
		//
		if (VirtualEnvironmentPlugin.getOverrideActivation()) {
			OverrideableVirtualEnvironment ove = new OverrideableVirtualEnvironment();
			ove.setEnvironmentOverrides( VirtualEnvironmentPlugin.getEnvironmentOverrides());
			ove.setPropertyOverrides( VirtualEnvironmentPlugin.getPropertyOverrides());
			cfg.setVirtualEnvironment(ove);
		}
				
		
		//
		// listener
		//
		/*
		ArtifactProcessingWalkNotificationListener walkNotificationListener = new ArtifactProcessingWalkNotificationListener();
		// TODO: configure listener, flesh it out 
		cfg.setWalkNotificationListener( walkNotificationListener);
		cfg.setClashResolverNotificationListener( walkNotificationListener);
		cfg.setPomReaderNotificationListener(walkNotificationListener);
		*/
		
		//
		// settings persistence expert
		//
		OverrideMavenSettingsPersistenceExpert overrideSettingsPersistenceExpert = new OverrideMavenSettingsPersistenceExpert();
		// configure - get ScopeConfiguration from denotation type or access 		
		overrideSettingsPersistenceExpert.setPropertyResolver( ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver());
		cfg.setOverrideSettingsPersistenceExpert( overrideSettingsPersistenceExpert);
		
		//
		// local repository location expert overload
		//
		OverrideLocalRepositoryRetrievalExpert overrideLocalRepositoryExpert = new OverrideLocalRepositoryRetrievalExpert();
		overrideLocalRepositoryExpert.setPropertyResolver( ArtifactContainerPlugin.getInstance().getVirtualPropertyResolver());
		cfg.setOverrideLocalRepositoryExpert( overrideLocalRepositoryExpert);
		
		ResolvingInstant resolvingInstant = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDynamicContainerPreferences().getClashResolvingInstant();
		cfg.setResolvingInstant( resolvingInstant);

		
		// get the context 
		WireContext<ClasspathResolverContract> context = ClasspathResolvers.classpathResolverContext( b -> {  
			b.bindContract(ClasspathResolverExternalContract.class, cfg);	
		});
		
		return context;		
	}
	
	private static WalkerContext commonContext() {
		WalkerContext wc = new WalkerContext();
		wc.getRelevantPartTuples().addAll( Arrays.asList( PartTuples.standardPartTuples()));
		wc.setSkipOptionals(true);
		wc.setTypeRule("jar");		
		return wc;
	
	}
	
	public static WalkerContext compileWalkContext() {
		WalkerContext wc = commonContext();
		wc.setScopes( Scopes.compileScopes());
		 		
		return wc;
	}
	
	public static WalkerContext runtimeWalkContext() {
		WalkerContext wc = commonContext();
		wc.setScopes( Scopes.runtimeScopes());		 
		
		return wc;
	}
	
	
}
