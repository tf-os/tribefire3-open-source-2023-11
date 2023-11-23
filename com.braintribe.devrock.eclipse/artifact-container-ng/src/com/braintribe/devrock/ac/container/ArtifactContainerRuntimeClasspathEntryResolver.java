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
package com.braintribe.devrock.ac.container;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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

/**
 * interceptor to support the Tomcat plugins - i.e the special CP file
 * @author pit
 *
 */
@SuppressWarnings("restriction")
public class ArtifactContainerRuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver {
	

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
		// 
		// 
		
		if (container instanceof ArtifactContainer) {
			ArtifactContainer artifactContainer = (ArtifactContainer) container;			
			cpes = artifactContainer.getRuntimeClasspathEntries();							
		}
		else {
			cpes = container.getClasspathEntries();		
		}
		
		// not sure what this is about, but at least no NPE happens in AC
		if (cpes == null) {
			return new IRuntimeClasspathEntry[0];
		}
				
		List<IRuntimeClasspathEntry> entries = new ArrayList<>( cpes.length);
		
		for (int i = 0; i < cpes.length; i++) {
			IClasspathEntry cpe = cpes[i];
			IRuntimeClasspathEntry e = new RuntimeClasspathEntry(cpe);
			IRuntimeClasspathEntry[] processed = postProcess(e, project);
			for (IRuntimeClasspathEntry suspect : processed) {
				suspect.setClasspathProperty(property);
				entries.add(suspect);
			}
		}
		
		return entries.toArray(new IRuntimeClasspathEntry[0]);
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
	
	private IRuntimeClasspathEntry[] postProcess(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		// if the project has multiple output locations, they must be returned
		IResource resource = entry.getResource();
		if (resource instanceof IProject) {
			IProject p = (IProject)resource;
			IJavaProject jp = JavaCore.create(p);
			if (jp != null && p.isOpen() && jp.exists()) {
				IRuntimeClasspathEntry[] entries = resolveOutputLocations(jp, entry.getClasspathProperty());
				if (entries != null) {
					return entries;
				}
			} else {
				return new IRuntimeClasspathEntry[0];
			}
		}
		IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[1];
		result[0] = entry;
		return result;
	}
	
	/**
	 * from eclipse : 
	 * 
	 * Returns runtime classpath entries corresponding to the output locations
	 * of the given project, or null if the project only uses the default
	 * output location.
	 * 
	 * @param project
	 * @param classpathProperty the type of classpath entries to create
	 * @return IRuntimeClasspathEntry[] or <code>null
	 * @throws CoreException
	 */
	private IRuntimeClasspathEntry[] resolveOutputLocations(IJavaProject project, int classpathProperty) throws CoreException {
		List<Object> nonDefault = new ArrayList<>();
		if (project.exists() && project.getProject().isOpen()) {
			IClasspathEntry entries[] = project.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry classpathEntry = entries[i];
				// check sources 
				if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = classpathEntry.getOutputLocation();
					if (path != null) {
						nonDefault.add(path);
					}
				}
				// check exported library folders
				else if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY && classpathEntry.isExported()) {
					IPath path = classpathEntry.getPath();
					if (path != null) {
						nonDefault.add(path);
					}
				}
			}
		}
		if (nonDefault.isEmpty()) {
			return null; 
		} 
		// add the default location if not already included
		IPath def = project.getOutputLocation();
		if (!nonDefault.contains(def)) {
			nonDefault.add(def);						
		}
		IRuntimeClasspathEntry[] locations = new IRuntimeClasspathEntry[nonDefault.size()];
		for (int i = 0; i < locations.length; i++) {
			IClasspathEntry newEntry = JavaCore.newLibraryEntry((IPath)nonDefault.get(i), null, null);
			locations[i] = new RuntimeClasspathEntry(newEntry);
			locations[i].setClasspathProperty(classpathProperty);
		}
		return locations;						
	}
}
