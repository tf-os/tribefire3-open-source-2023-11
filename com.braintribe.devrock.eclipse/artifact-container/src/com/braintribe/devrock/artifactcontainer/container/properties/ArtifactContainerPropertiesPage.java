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
package com.braintribe.devrock.artifactcontainer.container.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.container.ArtifactContainer;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerConfigurationPersistenceExpert;
import com.braintribe.devrock.artifactcontainer.control.container.ArtifactContainerRegistry;
import com.braintribe.devrock.artifactcontainer.control.walk.ArtifactContainerUpdateRequestType;
import com.braintribe.devrock.artifactcontainer.control.walk.wired.WiredArtifactContainerWalkController;
import com.braintribe.model.malaclypse.cfg.container.ArtifactContainerConfiguration;
import com.braintribe.model.malaclypse.cfg.container.ArtifactKind;
import com.braintribe.model.malaclypse.cfg.container.ResolverKind;
import com.braintribe.plugin.commons.container.ContainerNatureExpert;

/**
 * the property page for the containers 
 * @author pit
 *
 */
public class ArtifactContainerPropertiesPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension {

	private Font bigFont;

	private ArtifactContainer container;
	private IProject iProject;
	
	private Button aggregatorNatureButton;
	private Button tomcatNatureButton;
	private Button modelNatureButton;
	private Button carrierNatureButton;
	
	private Button standardProjectButton;
	private Button modelProjectButton;
	private Button gwtLibraryProjectButton;
	private Button gwtTerminalProjectButton;
	
	private Button optimisticResolvingButton;
	private Button hierarchicalResolvingButton;
	private Button indexResolvingButton;
	
	private ArtifactContainerConfiguration configuration;
	
	private IClasspathEntry classpathEntry;
	
	private ArtifactContainerRegistry registry = ArtifactContainerPlugin.getArtifactContainerRegistry();
	
	public ArtifactContainerPropertiesPage() {
		super("Artifact Container Properties");
		setTitle("Artifact Container Properties");
		setDescription("Set the default properties for the Artifact Container");		
	}

	

	@Override
	public void dispose() {
		if (bigFont != null)
			bigFont.dispose();
		
		super.dispose();
	}



	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
	
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		final Composite composite = new Composite(parent, NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        
        //
        // derived project status
        //
		Composite projectInfoGroup = new Composite( composite, SWT.NONE);
		projectInfoGroup.setLayout( layout);
		projectInfoGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label projectInfoLabel = new Label( projectInfoGroup, SWT.NONE);
		projectInfoLabel.setText( "Project info");
		projectInfoLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		projectInfoLabel.setFont(bigFont);
		
		// aggregator
		aggregatorNatureButton = new Button( projectInfoGroup, SWT.CHECK);
    	aggregatorNatureButton.setText("Aggregator project");
    	aggregatorNatureButton.setEnabled( false);    
    	aggregatorNatureButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	aggregatorNatureButton.setSelection( ContainerNatureExpert.hasAggregateNature(iProject));
    	
		// tomcat
    	tomcatNatureButton = new Button( projectInfoGroup, SWT.CHECK);
    	tomcatNatureButton.setText("Tomcat project");
    	tomcatNatureButton.setEnabled( false);    	    
    	tomcatNatureButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	tomcatNatureButton.setSelection( ContainerNatureExpert.hasTomcatNature(iProject));

    	// model 
    	modelNatureButton = new Button( projectInfoGroup, SWT.CHECK);
    	modelNatureButton.setText("Model project");
    	modelNatureButton.setEnabled( false);    	    
    	modelNatureButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	modelNatureButton.setSelection( ContainerNatureExpert.hasModelNature(iProject));
    	
    	// carrier 
    	carrierNatureButton = new Button( projectInfoGroup, SWT.CHECK);
    	carrierNatureButton.setText("TribefireServices project");
    	carrierNatureButton.setEnabled( false);    	    
    	carrierNatureButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    	carrierNatureButton.setSelection( ContainerNatureExpert.hasTribefireServicesNature(iProject));
    	

    	//
    	// project kind 
    	//
    	Composite projectKindGroup = new Composite( composite, SWT.NONE);
		projectKindGroup.setLayout( layout);
		projectKindGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label projectKindLabel = new Label( projectKindGroup, SWT.NONE);
		projectKindLabel.setText( "Project choices");
		projectKindLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		projectKindLabel.setFont(bigFont);
		
		standardProjectButton = new Button( projectKindGroup, SWT.RADIO);
		standardProjectButton.setText("Standard artifact project");
		standardProjectButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
		modelProjectButton = new Button( projectKindGroup, SWT.RADIO);
		modelProjectButton.setText("Model artifact project");
		modelProjectButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
		gwtLibraryProjectButton = new Button( projectKindGroup, SWT.RADIO);
		gwtLibraryProjectButton.setText("GWT library artifact project");
		gwtLibraryProjectButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));

		gwtTerminalProjectButton = new Button( projectKindGroup, SWT.RADIO);
		gwtTerminalProjectButton.setText("GWT terminal artifact project");
		gwtTerminalProjectButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
		// initialize 
		switch ( configuration.getArtifactKind()) {
			case gwtLibrary:
				gwtLibraryProjectButton.setSelection(true);
				break;
			case gwtTerminal:
				gwtTerminalProjectButton.setSelection( true);
				break;
			case model:
				modelProjectButton.setSelection(true);
				break;
			case plugin:
			case standard:
			default:
				standardProjectButton.setSelection(true);
				break;		
		}
        
        // container options
		Composite clashOptionsGroup = new Composite( composite, SWT.NONE);
		clashOptionsGroup.setLayout( layout);
		clashOptionsGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label clashOptionsLabel = new Label( clashOptionsGroup, SWT.NONE);
		clashOptionsLabel.setText( "Clash resolving options");
		clashOptionsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
		clashOptionsLabel.setFont(bigFont);
		
		optimisticResolvingButton = new Button( clashOptionsGroup, SWT.RADIO);
		optimisticResolvingButton.setText("Optimistic resolving - the highest version wins (recommended)");
		optimisticResolvingButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
		hierarchicalResolvingButton = new Button( clashOptionsGroup, SWT.RADIO);
		hierarchicalResolvingButton.setText("depth-based resolving - top level declaration wins (Maven style)");
		hierarchicalResolvingButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
		indexResolvingButton = new Button( clashOptionsGroup, SWT.RADIO);
		indexResolvingButton.setText("index-based resolving - first declaration encountered wins");
		indexResolvingButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
		switch (configuration.getResolverKind()) {
		case hierarchy:
			hierarchicalResolvingButton.setSelection(true);
			break;
		case index:
			indexResolvingButton.setSelection(true);
			break;
		case optimistic:
		default:
			optimisticResolvingButton.setSelection(true);
			break; 		
		}
		composite.pack();
		setControl(composite);                        	
	}

	@Override
	public void initialize(IJavaProject project, IClasspathEntry[] entries) {
		container = registry.getContainerOfProject(project.getProject());
		iProject = project.getProject();
		if (container == null) {
			ArtifactContainerStatus status = new ArtifactContainerStatus( "No container stored for [" + project.getProject().getName() + "]", IStatus.WARNING);
			ArtifactContainerPlugin.getInstance().log(status);	
			configuration = ArtifactContainerConfigurationPersistenceExpert.generateDefaultContainerConfiguration();
			// tell registry that we have some configuration that has no container yet..
			registry.setPreConfiguredConfigurationOfProject(project, configuration);
		}
		else {
			configuration = container.getConfiguration();
		}

	}

	@Override
	public boolean finish() {
		// project kind
		ArtifactKind selectedArtifactKind = ArtifactKind.standard;
		if (modelProjectButton.getSelection()) {
			selectedArtifactKind = ArtifactKind.model;
		}
		else if (gwtLibraryProjectButton.getSelection()) {
			selectedArtifactKind = ArtifactKind.gwtLibrary;
		}
		else if (gwtTerminalProjectButton.getSelection()) {
			selectedArtifactKind = ArtifactKind.gwtTerminal;
		}
		else {
			selectedArtifactKind = ArtifactKind.standard;
		}		
		// resolver mode
		ResolverKind selectedResolverKind = ResolverKind.optimistic;
		if (indexResolvingButton.getSelection()) {
			selectedResolverKind = ResolverKind.index;
		}
		else if (hierarchicalResolvingButton.getSelection()) {
			selectedResolverKind = ResolverKind.hierarchy;
		}
		else {
			selectedResolverKind = ResolverKind.optimistic;
		}
		
		boolean changedConfiguration = false;		
		if (configuration.getArtifactKind() != selectedArtifactKind) {
			configuration.setArtifactKind(selectedArtifactKind);
			changedConfiguration = true;
		}
		boolean requiresWalk = false;
		if (configuration.getResolverKind() != selectedResolverKind) {
			configuration.setResolverKind(selectedResolverKind);
			requiresWalk = true;
			changedConfiguration = true;
		}
		
		if (changedConfiguration &&  container != null) {
			configuration.setModified(true);
			container.setConfiguration(configuration);
		}
		if (changedConfiguration || container == null) {			
			classpathEntry = JavaCore.newContainerEntry(ArtifactContainer.ID, false);
		}
		
		if (container != null) {
			 
			if (requiresWalk) {
				// trigger a re-sync as the changes are relevant to the solution list
				WiredArtifactContainerWalkController.getInstance().updateContainer(container, ArtifactContainerUpdateRequestType.combined);
			}
			else if (changedConfiguration) {
				// trigger a refresh, as the changes are relevant to the classpath, yet not for the solutions
				WiredArtifactContainerWalkController.getInstance().updateContainer(container, ArtifactContainerUpdateRequestType.refresh);
			}
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
