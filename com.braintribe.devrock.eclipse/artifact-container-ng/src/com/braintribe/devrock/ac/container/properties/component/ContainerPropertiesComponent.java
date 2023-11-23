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
package com.braintribe.devrock.ac.container.properties.component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.properties.ResolutionYamlMarshaller;
import com.braintribe.devrock.ac.container.resolution.viewer.ContainerResolutionViewer;
import com.braintribe.devrock.ac.container.resolution.yaml.YamlResolutionViewer;
import com.braintribe.devrock.api.nature.NatureHelper;
import com.braintribe.devrock.api.ui.commons.ResolutionValidator;
import com.braintribe.devrock.api.ui.editors.support.EditorSupport;
import com.braintribe.devrock.api.ui.fonts.FontHandler;
import com.braintribe.devrock.api.ui.viewers.reason.ReasonViewer;
import com.braintribe.devrock.mc.core.declared.DeclaredArtifactIdentificationExtractor;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;

/**
 * component for container properties - i.e. number of dependencies, time it took etc
 * gives access to multiple viewing choices
 * 
 * @author pit
 *
 */
public class ContainerPropertiesComponent implements SelectionListener {

	private Font bigFont;
	
	private Image detailResolutionImage;
	private Image successImage;
	private Image warnImage;
	private Image detailReasonsImage;
	private Image failedImage;
	private Image viewResolutionImage;
	private Image openResolutionImage;
	private Image saveResolutionImage;

	private Button detailedReasonsButton;
	private Button detailedResolutionButton;	
	private Button viewResolutionAsYaml;	
	private Button openResolutionAsYaml;
	private Button saveResolutionAsYaml;

	private Shell shell;

	private ArtifactContainer container;
	private IProject iProject;
	
	private boolean initialMode = false; 
	
	public ContainerPropertiesComponent(Shell shell, ArtifactContainer container, IJavaProject javaProject) {
			
		this.shell = shell;
		this.container = container;		
		
		if (container == null) {
			initialMode = true;
			if (javaProject != null) {
				this.iProject = javaProject.getProject();
			} 
		}
		else {		
			IJavaProject iJavaProject = container.getProject();
			if (iJavaProject != null) {
				this.iProject = iJavaProject.getProject();
			}
		}
				
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "read_obj.png");
		detailResolutionImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "detail-resolution.gif");
		viewResolutionImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "open_file.transparent.png");
		openResolutionImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "save_file.transparent.png");
		saveResolutionImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "detail-reasons.png");
		detailReasonsImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "success.gif");
		successImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "warning.png");
		warnImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ContainerPropertiesComponent.class, "error.gif");
		failedImage = imageDescriptor.createImage();
	}
		
	/**
	 * to be called if owning component goes out of scope 
	 */
	public void dispose() {
		if (bigFont != null)
			bigFont.dispose();
		
		detailResolutionImage.dispose();
		viewResolutionImage.dispose();
		openResolutionImage.dispose();
		saveResolutionImage.dispose();
		successImage.dispose();
		detailReasonsImage.dispose();
		failedImage.dispose();
		warnImage.dispose();				
	}
		
	/**
	 * creates a functional composite 
	 * @param parent - the parent {@link Composite}
	 * @return - the packed {@link Composite} created 
	 */
	public Composite createControl(Composite parent) {
	
	
		Font initialFont = parent.getFont();

		bigFont = FontHandler.buildBigFont(shell.getDisplay(), initialFont);
		
	
		final Composite composite = new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        Composite projectInfoGroup = new Composite( composite, SWT.NONE);
        projectInfoGroup.setLayout( layout);
        projectInfoGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
        
        Label projectInfoLabel = new Label( projectInfoGroup, SWT.NONE);
        projectInfoLabel.setText( "Project info");
        projectInfoLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        projectInfoLabel.setFont(bigFont);
        
        // coordinates
        
        // Eclipse project
        Label projectNameLabel = new Label( projectInfoGroup, SWT.NONE);
        projectNameLabel.setText( "Project :");
        projectNameLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));					
        
        Text projectName = new Text(projectInfoGroup, SWT.NONE);
        projectName.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
        projectName.setText( iProject.getName());
        projectName.setEnabled(false);
        
        
        // Folder
        Label artifactFolderLabel = new Label( projectInfoGroup, SWT.NONE);
        artifactFolderLabel.setText( "Folder :");
        artifactFolderLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        
        Text artifactFolder = new Text(projectInfoGroup, SWT.NONE);
        artifactFolder.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
        artifactFolder.setText( iProject.getLocation().toFile().getAbsolutePath());
        artifactFolder.setEnabled(false);
        
        // natures 		
        Maybe<List<String>> natures = NatureHelper.getNatures(iProject);
        if (natures.isSatisfied()) {
        	Composite natureComposite = new Composite( composite, SWT.NONE);
        	natureComposite.setLayout( layout);
        	natureComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
        	
        	Label natureInfoLabel = new Label( natureComposite, SWT.NONE);
        	natureInfoLabel.setText( "Natures");
        	natureInfoLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        	natureInfoLabel.setFont(bigFont);
        	
        	for (String nature : natures.get()) {
        		Button button = new Button( natureComposite, SWT.CHECK);
        		button.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        		button.setText( nature);
        		button.setSelection(true);
        		button.setEnabled(false);
        	}
        }
        
        // Versioned Artifact
        Label artifactNameLabel = new Label( projectInfoGroup, SWT.NONE);
        artifactNameLabel.setText( "Artifact :");
        artifactNameLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        
        VersionedArtifactIdentification vai = container != null ? container.getVersionedArtifactIdentification() : getArtifactIdentificationFromProject(iProject);
        
        String toShow = vai != null ? vai.asString() : "unidentifiable artifact";
                        
    	Text artifactName = new Text(projectInfoGroup, SWT.NONE);
    	artifactName.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 3, 1));
    	artifactName.setText( toShow);
    	artifactName.setEnabled(false);        	
    
        
        if (!initialMode) {
			// resolution
			Composite resolutionInfoGroup = new Composite( composite, SWT.NONE);		 
			resolutionInfoGroup.setLayout(layout);
			resolutionInfoGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 6));
			
			Label resolutionInfoLabel = new Label( resolutionInfoGroup, SWT.NONE);
			resolutionInfoLabel.setText( "Resolution info");
			resolutionInfoLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
			resolutionInfoLabel.setFont(bigFont);
			
			double lastProcessingTime = container.getLastProcessingTime();
			Label processingTimeLabel = new Label( resolutionInfoGroup, SWT.NONE);
			processingTimeLabel.setText( "Processing time required : " + lastProcessingTime + " ms");
			processingTimeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
	
	    	// show resolution
			AnalysisArtifactResolution resolution = container.getCompileResolution();
			
			//String pathname = "f:/works/COREDR-10/com.braintribe.devrock.eclipse/artifact-container-ng/dumps/unresolved-dependency.dump.tdr.yaml";
			//AnalysisArtifactResolution resolution = ResolutionLoader.load( new File(pathname));
				
			if (resolution != null) {
				int size = resolution.getSolutions().size();
				Label numSolutionsLabel = new Label( resolutionInfoGroup, SWT.NONE);
				numSolutionsLabel.setText( "Resolution has the following number of resolved dependencies: " + size);
				numSolutionsLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
			}
			
				// create error display
			Composite statusComposite = null;
			if (resolution == null) {
				statusComposite = combineImageAndLabel(resolutionInfoGroup, warnImage, "No resolution attached at all");
			}		
			else if (ResolutionValidator.isResolutionInvalid(resolution)) {
	    		statusComposite = combineImageAndLabel(resolutionInfoGroup, failedImage, "Resolution failed");    	
	    		ReasonViewer reasonViewer = new ReasonViewer( ResolutionValidator.getReasonForFailure(resolution));
	    		Composite reasonViewerComposite = reasonViewer.createControl(resolutionInfoGroup, "reasons for failure");
	    		reasonViewerComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 6));    		
	    	}
			else {
				statusComposite = combineImageAndLabel(resolutionInfoGroup, successImage, "Resolution succeeded");					
			}
	    	
	    	statusComposite.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, true, 4, 1));
	    	
		
	   
	    	Composite yamlComposite = new Composite(resolutionInfoGroup, SWT.NONE);
	    	yamlComposite.setLayout(layout);
			yamlComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
			
			Label yamlCompositeLabel = new Label( yamlComposite, SWT.NONE);
			yamlCompositeLabel.setFont(bigFont);
			yamlCompositeLabel.setText("Viewing options");
			yamlCompositeLabel.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
	    
			detailedResolutionButton = new Button( yamlComposite, SWT.PUSH);
			detailedResolutionButton.setText("detailed resolution view");
			detailedResolutionButton.setToolTipText("opens the resolution viewer and shows what it makes of the resolution");
			detailedResolutionButton.setImage( detailResolutionImage);
			detailedResolutionButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1));
			detailedResolutionButton.addSelectionListener( this);
				
	    	viewResolutionAsYaml = new Button( yamlComposite, SWT.PUSH);
	    	viewResolutionAsYaml.setText("view YAML");
	    	viewResolutionAsYaml.setToolTipText( "displays the YAML formatted resolution in a searchable dialog");
	    	viewResolutionAsYaml.setImage( viewResolutionImage);
	    	viewResolutionAsYaml.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1, 1));
	    	viewResolutionAsYaml.addSelectionListener( this);
			
	    	openResolutionAsYaml = new Button( yamlComposite, SWT.PUSH);
	    	openResolutionAsYaml.setText("open YAML");
	    	openResolutionAsYaml.setImage( openResolutionImage);
	    	openResolutionAsYaml.setToolTipText( "opens the YAML formatted resolution in the YAML editor");
	    	openResolutionAsYaml.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1, 1));
	    	openResolutionAsYaml.addSelectionListener( this); 
	    	
	    	saveResolutionAsYaml = new Button( yamlComposite, SWT.PUSH);
	    	saveResolutionAsYaml.setText("save YAML");
	    	saveResolutionAsYaml.setToolTipText( "save the YAML formatted resolution to file");
	    	saveResolutionAsYaml.setImage( saveResolutionImage);
	    	saveResolutionAsYaml.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 1, 1));
	    	saveResolutionAsYaml.addSelectionListener( this);    	    	
        }
        
		composite.pack();
		
		return composite;	                        	
	}

	private VersionedArtifactIdentification getArtifactIdentificationFromProject(IProject project) {	
		File projectDir = project.getLocation().toFile();
		File pomFile = new File(projectDir, "pom.xml");
		if (!pomFile.exists()) {
			String msg = "project [" + project.getName() + "] has no associated pom in [" + projectDir.getAbsolutePath() + "] and is not a candidate for a container";			
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, IStatus.WARNING);
			ArtifactContainerPlugin.instance().log(status);    		
			return null;
		}
		Maybe<CompiledArtifactIdentification> extractedIdentificationPotential = DeclaredArtifactIdentificationExtractor.extractIdentification(pomFile);
		 
		if (extractedIdentificationPotential.isUnsatisfied()) {
			String msg = " project [" + project.getName() + "] isn't a suitable candidate for a container : cannot read pom [" + pomFile.getAbsolutePath() + "] associated with project [" + project.getName() + "] as [" + extractedIdentificationPotential.whyUnsatisfied().stringify() + "]"; 
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.WARNING);
			ArtifactContainerPlugin.instance().log(status);
			return null;
		}
		else {
			CompiledArtifactIdentification cai = extractedIdentificationPotential.get();							
			return VersionedArtifactIdentification.create( cai.getGroupId(), cai.getArtifactId(), cai.getVersion().asString());
		}
	}
	
	/**
	 * @param parent - the parent {@link Composite}
	 * @param image - the image to use 
	 * @param text - the text
	 * @return - the {@link Composite} created
	 */
	private Composite combineImageAndLabel(Composite parent, Image image, String text) {
		Composite composite = new Composite( parent, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		Label imageLabel = new Label(composite, SWT.NONE);
		imageLabel.setImage( image);
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText( text);
		textLabel.setFont( bigFont);		
		return composite;
	}
	
	
	
	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		widgetSelected(event);
	}
	
	@Override
	public void widgetSelected(SelectionEvent event) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (event.widget == detailedReasonsButton) {
			// popup reason window
		}
		else if (event.widget == detailedResolutionButton) {						
			ContainerResolutionViewer resolutionViewer = new ContainerResolutionViewer(shell);
			resolutionViewer.setResolution( container.getCompileResolution());
			resolutionViewer.setProjectDependencies( container.getProjectDependencies());
			resolutionViewer.preemptiveDataRetrieval();
			resolutionViewer.open();
		}
		else if (event.widget == viewResolutionAsYaml) {
			YamlResolutionViewer yamlResolutionViewer = new YamlResolutionViewer(shell);
			yamlResolutionViewer.setResolution( container.getCompileResolution());			
			yamlResolutionViewer.open();
		}
		else if (event.widget == openResolutionAsYaml) {
			// create temp file, blow it out, and then open the file 
			try {				
				File file = Files.createTempFile(null, ".yaml").toFile();
				if (file != null) {
					ResolutionYamlMarshaller.toYamlFile(container.getCompileResolution(), file);
					EditorSupport.load(file);
				}
				
			} catch (Exception e) {
				ArtifactContainerStatus status = new ArtifactContainerStatus( "Unable to unmarshall the resolution to a temporary file", e);
				ArtifactContainerPlugin.instance().log(status);				
			}
			
		}
		else if (event.widget == saveResolutionAsYaml) {
			FileDialog fd = new FileDialog( shell, SWT.SAVE);
			fd.setFilterExtensions( new String[] {"*.yaml"});
			fd.setOverwrite(true);		
			String selectedFile = fd.open();
			
			if (selectedFile != null) {
				if (!selectedFile.endsWith( ".yaml")) {
					selectedFile += ".yaml";
				}
				ResolutionYamlMarshaller.toYamlFile(container.getCompileResolution(), new File(selectedFile));
			}			
		}
	}	
	
}
