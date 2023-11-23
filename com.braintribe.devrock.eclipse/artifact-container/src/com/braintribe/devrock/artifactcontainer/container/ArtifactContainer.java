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
package com.braintribe.devrock.artifactcontainer.container;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerConfigurationPersistenceExpert;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkProcessor;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.artifact.Solution;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerConfiguration;
import com.braintribe.model.malaclypse.cfg.container.ContainerGenerationMode;
import com.braintribe.model.malaclypse.container.ContainerPersistence;

/**
 * an implementation of the {@link IClasspathContainer}, with a dynamic touch
 * 
 * @author pit
 *
 */
public class ArtifactContainer implements IClasspathContainer {	
	public final static IPath ID = new Path("Braintribe.ArtifactClasspathContainer");

	private String containerId;
	private IPath containerIPath;
	private IJavaProject iJavaProject;
	private ArtifactContainerConfiguration configuration;
	
	private ContainerPersistence containerPersistence;

	private IClasspathEntry [] classpathEntries;
	private IClasspathEntry [] runtimeEntries;
	
	private ArtifactContainerRegistry registry = ArtifactContainerPlugin.getArtifactContainerRegistry();
	
	private Set<IProject> dependencies = new HashSet<IProject>();
		
	
	@Override
	public String getDescription() {		
		return "Artifact Container";
	}
	@Override
	public int getKind() {
		return K_APPLICATION;
	}
	@Override
	public IPath getPath() {
		return containerIPath;
	}	
	public IJavaProject getProject() {
		return iJavaProject;
	}	
	public String getId() {
		return containerId;
	}
	public ArtifactContainerConfiguration getConfiguration() {
		if (configuration == null) {
			configuration = ArtifactContainerConfigurationPersistenceExpert.generateDefaultContainerConfiguration();
		}
		return configuration;
	}
	public void setConfiguration(ArtifactContainerConfiguration configuration) {
		this.configuration = configuration;
	}
	
	
		
	public Set<IProject> getDependencies() {
		return dependencies;
	}
	public void setDependencies(Set<IProject> dependencies) {
		this.dependencies = dependencies;
	}
	public void setCompileSolutions( List<Solution> solutions) {
		if (containerPersistence == null) {
			containerPersistence = ContainerPersistence.T.create();			
		}
		containerPersistence.setTimestamp( new Date());
		containerPersistence.setCompileSolutions(solutions);
	}
	public void setRuntimeSolutions( List<Solution> solutions) {
		if (containerPersistence == null) { 
			containerPersistence = ContainerPersistence.T.create();			
		}
		if (containerPersistence.getTimestamp() == null)
			containerPersistence.setTimestamp( new Date());
		containerPersistence.setRuntimeSolutions( solutions);	
	}
	
	public ContainerPersistence getContainerPersistence() {
		return containerPersistence;
	}
	public void setContainerPersistence( ContainerPersistence persistence){
		this.containerPersistence = persistence;
	}
	
	public void setClasspathEntries(IClasspathEntry[] classpathEntries) {
		this.classpathEntries = classpathEntries;
	}
	public void setRuntimeEntries(IClasspathEntry[] classpathEntries) {
		this.runtimeEntries = classpathEntries;
	}	
	public String getMd5() {
		if (containerPersistence != null)
			return containerPersistence.getMd5();
		return null;
	}
	
	/**
	 * standard constructor 
	 * @param iPath - the id of the container as {@link IPath}
	 * @param iJavaProject - the {@link IJavaProject} it's attached to
	 */
	public ArtifactContainer(IPath iPath, IJavaProject iJavaProject, String id) {
		this.containerIPath = iPath;
		this.iJavaProject = iJavaProject;
		this.containerId = id;
	}
	
	public static ArtifactContainer notifyEclipse( ArtifactContainer container) {		
		return new ArtifactContainer(container);
	}
	
	/**
	 * copy constructor to publish new artifact containers settings to Eclipse
	 */
	public ArtifactContainer( ArtifactContainer sibling) {		
		iJavaProject = sibling.iJavaProject;
		containerIPath = sibling.containerIPath;
		configuration = sibling.configuration;
		containerId = sibling.containerId;
		dependencies = sibling.dependencies;
		
		containerPersistence = sibling.containerPersistence;
		
		classpathEntries = sibling.classpathEntries;
		
		ArtifactContainerPlugin.getArtifactContainerRegistry().reassignContainerToProject( iJavaProject.getProject(), this);
		 		
	}
	
	public void clear() {
		containerPersistence = null;		
	}
	
	private Object classpathEntriesMonitor = new Object();

	@Override
	public IClasspathEntry[] getClasspathEntries() {
		// any entries ready? return these
		if (classpathEntries != null)
			return classpathEntries;
		
		synchronized (classpathEntriesMonitor) {
			
			if (classpathEntries != null)
				return classpathEntries;
		
			if (!WiredArtifactContainerWalkController.getInstance().getContainerInitializingInhibited()) {
			// directly call the processor to IMMEDIATELY get a sync on the COMPILE level 
				WiredArtifactContainerWalkProcessor artifactContainerWalkProcessor = new WiredArtifactContainerWalkProcessor();			
				artifactContainerWalkProcessor.setContainerClasspathDiagnosticsListener( registry.getContainerClasspathDiagnosticsRegistry());		
				artifactContainerWalkProcessor.initializeCompileContainer(this, MalaclypseWirings.fullClasspathResolverContract().contract());			
			
				if (classpathEntries == null) {
					classpathEntries = new IClasspathEntry[0];
				}
				return classpathEntries;
			}
		}
		return new IClasspathEntry[0];				 		
	}
	
	private Object runtimeEntriesMonitor = new Object();
	 
	public IClasspathEntry[] getRuntimeClasspathEntries() {
		if (runtimeEntries != null)
			return runtimeEntries;
		
		synchronized( runtimeEntriesMonitor) {
			
			if (runtimeEntries != null)
				return runtimeEntries;
		
			if (!WiredArtifactContainerWalkController.getInstance().getContainerInitializingInhibited()) {
				// directly call the processor to IMMEDIATELY get a sync on the COMPILE level 
				WiredArtifactContainerWalkProcessor artifactContainerWalkProcessor = new WiredArtifactContainerWalkProcessor();
				artifactContainerWalkProcessor.setContainerClasspathDiagnosticsListener( registry.getContainerClasspathDiagnosticsRegistry());
				//artifactContainerWalkProcessor.setMalaclypseRuntimeScope(ArtifactContainerPlugin.getDefaultRuntimeScope());
				artifactContainerWalkProcessor.initializeLaunchContainer(this, MalaclypseWirings.fullClasspathResolverContract().contract(), ContainerGenerationMode.standard);
				return runtimeEntries;
			}
		}
		return new IClasspathEntry[0];					
	}
	

}
