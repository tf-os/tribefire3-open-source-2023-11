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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import com.braintribe.build.artifacts.mc.wire.classwalk.contract.ClasspathResolverContract;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkProcessor;
import com.braintribe.devrock.artifactcontainer.plugin.malaclypse.scope.wirings.MalaclypseWirings;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerConfiguration;
import com.braintribe.model.malaclypse.cfg.container.ArtifactKind;
import com.braintribe.model.malaclypse.cfg.container.ContainerGenerationMode;

@SuppressWarnings("restriction")
public class ArtifactContainerRuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver {
	private ArtifactContainerRegistry registry = ArtifactContainerPlugin.getArtifactContainerRegistry();

	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry( IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		IJavaProject project = entry.getJavaProject();
		if (project == null) {
			project = JavaRuntime.getJavaProject(configuration);
		}
		return resolveRuntimeClasspathEntry(entry, project);
	}

	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry( IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		if (project == null || entry == null) {
			// cannot resolve without entry or project context
			return new IRuntimeClasspathEntry[0];
		}
		IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
		
		if (container == null) {
			abort("could not resolve classpath container " + entry.getPath().toString(), null);
			// execution will not reach here - exception will be thrown
			return null;
		}
		IClasspathEntry[] cpes = null;
		
		
		int property = -1;
		switch (container.getKind()) {
			case IClasspathContainer.K_APPLICATION:
				property = IRuntimeClasspathEntry.USER_CLASSES;
				break;
			case IClasspathContainer.K_DEFAULT_SYSTEM:
				property = IRuntimeClasspathEntry.STANDARD_CLASSES;
				break;
			case IClasspathContainer.K_SYSTEM:
				property = IRuntimeClasspathEntry.BOOTSTRAP_CLASSES;
				break;
		}
		//
		// if we're dealing with the artifact container, we can check 
		// if the current's project a GWT terminal, and if so, ask
		// the container to get the path for gwt (with source included)
		//
		
		if (container instanceof ArtifactContainer) {
			ArtifactContainer artifactContainer = (ArtifactContainer) container;
			IProject iProject = project.getProject();
			ArtifactContainer containerOfProject = registry.getContainerOfProject( iProject);
			if (containerOfProject == null) {
				cpes = container.getClasspathEntries();			
			}
			else {
				ArtifactContainerConfiguration settings = containerOfProject.getConfiguration();
								
				ArtifactKind kind = settings.getArtifactKind();
				switch (kind) {
					case gwtTerminal : {
						// override classpath entries with the gwt flag set
						ClasspathResolverContract contract = MalaclypseWirings.fullClasspathResolverContract().contract();
						WiredArtifactContainerWalkProcessor artifactContainerWalkProcessor = new WiredArtifactContainerWalkProcessor();
						artifactContainerWalkProcessor.setClasspathResolverContract(contract);
						artifactContainerWalkProcessor.setContainerClasspathDiagnosticsListener( registry.getContainerClasspathDiagnosticsRegistry());
						artifactContainerWalkProcessor.initializeLaunchContainer(artifactContainer, contract, ContainerGenerationMode.combined);
						cpes = artifactContainer.getRuntimeClasspathEntries();						
						break;
					}
					default:											
						cpes = artifactContainer.getRuntimeClasspathEntries();						
						break;
				}
			}		
		}
		else {
			cpes = container.getClasspathEntries();		
		}
		
		// not sure what this is about, but at least no NPE happends in AC
		if (cpes == null) {
			return new IRuntimeClasspathEntry[0];
		}
		
		IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[cpes.length];
		
		for (int i = 0; i < cpes.length; i++) {
			IClasspathEntry cpe = cpes[i];
			IRuntimeClasspathEntry e = new RuntimeClasspathEntry(cpe);
			e.setClasspathProperty(property);
			result[i] = e;
		}
		
		return result;
	}

	@Override
	public IVMInstall resolveVMInstall(IClasspathEntry entry)
			throws CoreException {
		return null;
	}
	
	/**
	 * Throws a core exception with an internal error status.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @throws CoreException a {@link CoreException} wrapper
	 */
	private static void abort(String message, Throwable exception) throws CoreException {
		abort(message, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR, exception);
	}
		
		
	/**
	 * Throws a core exception with an internal error status.
	 * 
	 * @param message the status message
	 * @param code status code
	 * @param exception lower level exception associated with the
	 * 
	 *  error, or <code>null</code> if none
	 * @throws CoreException a {@link CoreException} wrapper
	 */
	private static void abort(String message, int code, Throwable exception) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, "Braintribe.ArtifactClasspathContainer", code, message, exception));
	}
}
