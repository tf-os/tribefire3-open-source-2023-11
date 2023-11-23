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
package com.braintribe.devrock.ac.container.properties;




import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.properties.component.ContainerPropertiesComponent;

/**
 * the property page for the containers 
 * @author pit
 *
 */
public class ArtifactContainerPropertiesPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension {
	private com.braintribe.devrock.ac.container.ArtifactContainer container;
	private IJavaProject javaProject;
	private IClasspathEntry classpathEntry;

	private ContainerPropertiesComponent cpc;
	
	
	public ArtifactContainerPropertiesPage() {
		super("Artifact Container Properties");
		setTitle("Artifact Container Properties");
		setDescription("Set the default properties for the Artifact Container");	
	}

	
	@Override
	public void dispose() {
		if (cpc != null) {
			cpc.dispose();
		}
		
		super.dispose();
	}


	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
	
		cpc = new ContainerPropertiesComponent(getShell(), container, javaProject);
		Composite composite = cpc.createControl(parent);		
		setControl(composite);                        	
	}
	
	@Override
	public void initialize(IJavaProject project, IClasspathEntry[] entries) {
		this.javaProject = project;
		
		IProject iProject = project.getProject();		
		if (iProject != null) {
			container = ArtifactContainerPlugin.instance().containerRegistry().getContainerOfProject(iProject);
			if (container == null) {
			
				ArtifactContainerStatus status = new ArtifactContainerStatus( "No container stored for [" + iProject.getName() + "]", IStatus.WARNING);
				ArtifactContainerPlugin.instance().log(status);				
			}		
			
		}
	}

	@Override
	public boolean finish() {					
		// ok, no container -> must create one
		if (container == null) {
			classpathEntry = JavaCore.newContainerEntry(ArtifactContainer.ID, false);
		}
		return true;
	}

	@Override
	public IClasspathEntry getSelection() {		
		return classpathEntry;
		
	}

	@Override
	public void setSelection(IClasspathEntry classpathEntry) {
		this.classpathEntry = classpathEntry;
	}
	
	
		
}
