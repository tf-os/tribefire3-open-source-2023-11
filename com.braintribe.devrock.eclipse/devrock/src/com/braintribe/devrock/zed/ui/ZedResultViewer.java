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
package com.braintribe.devrock.zed.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.viewers.reason.transpose.TransposedReasonViewer;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.zarathud.model.classpath.ClasspathDuplicateNode;
import com.braintribe.devrock.zarathud.model.common.Node;
import com.braintribe.devrock.zarathud.model.module.PackageNode;
import com.braintribe.devrock.zed.ui.transposer.ClasspathAnalysisContentTransposer;
import com.braintribe.devrock.zed.ui.transposer.DependencyAnalysisContentTransposer;
import com.braintribe.devrock.zed.ui.transposer.ExtractionTransposer;
import com.braintribe.devrock.zed.ui.transposer.ModuleAnalysisContentTransposer;
import com.braintribe.devrock.zed.ui.transposer.ZedExtractionTransposingContext;
import com.braintribe.devrock.zed.ui.viewer.classpath.ClasspathAnalysisViewer;
import com.braintribe.devrock.zed.ui.viewer.dependencies.DependencyAnalysisViewer;
import com.braintribe.devrock.zed.ui.viewer.extraction.ExtractionViewer;
import com.braintribe.devrock.zed.ui.viewer.extraction.ZedExtractionViewerContext;
import com.braintribe.devrock.zed.ui.viewer.model.ModelAnalysisViewer;
import com.braintribe.devrock.zed.ui.viewer.module.ModuleAnalysisViewer;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.ClasspathForensicsResult;
import com.braintribe.zarathud.model.forensics.DependencyForensicsResult;
import com.braintribe.zarathud.model.forensics.ForensicsRating;
import com.braintribe.zarathud.model.forensics.ModelForensicsResult;
import com.braintribe.zarathud.model.forensics.ModuleForensicsResult;

public class ZedResultViewer extends DevrockDialog implements IDisposable {
	private static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private ZedViewingContext context;
	private CTabFolder tabFolder;
	private final UiSupport uiSupport = DevrockPlugin.instance().uiSupport();
	
	ViewingContextStorageHandler viewContextStorageHandler = new ViewingContextStorageHandler();
	
	private Image successImage;
	private Image warningImage;
	private Image errorImage;
	private Image ignoreImage;
	
	private Image saveImage;
	private Image saveRatingsImage;
	private Map<ForensicsRating, Image> ratingToImageMap;
	
	private YamlMarshaller marshaller;
	private ExtractionViewer extractionViewer;
	private ModuleAnalysisViewer moduleAnalysisViewer;
	private ModelAnalysisViewer modelAnalysisViewer;
	private ClasspathAnalysisViewer classpathAnalysisViewer;
	private DependencyAnalysisViewer dependencyAnalysisViewer;
	private TransposedReasonViewer reasonViewer; 
	{
		marshaller = new YamlMarshaller();
		marshaller.setWritePooled(true);
	}
	
	public ZedResultViewer(Shell parentShell) {
		super(parentShell);		
		setShellStyle( SHELL_STYLE);
		
		successImage = uiSupport.images().addImage("success", ZedResultViewer.class, "testok.png");						
		warningImage = uiSupport.images().addImage("warning", ZedResultViewer.class, "testwarn.png");						
		errorImage = uiSupport.images().addImage("error", ZedResultViewer.class, "testerr.png");
		ignoreImage = uiSupport.images().addImage("ignore", ZedResultViewer.class, "testignored.png");
		saveImage = uiSupport.images().addImage("save", ZedResultViewer.class, "save_file.transparent.png");
		saveRatingsImage = uiSupport.images().addImage("saveRatings", ZedResultViewer.class, "export_log.png");
		
		ratingToImageMap = new HashMap<>();
		ratingToImageMap.put( ForensicsRating.OK, successImage);
		ratingToImageMap.put( ForensicsRating.WARN, warningImage);
		ratingToImageMap.put( ForensicsRating.ERROR, errorImage);
		ratingToImageMap.put( ForensicsRating.FATAL, errorImage);
		ratingToImageMap.put( ForensicsRating.IGNORE, ignoreImage);
		ratingToImageMap.put( ForensicsRating.INFO, successImage);
		
		
		
	}
	
	@Configurable @Required
	public void setContext(ZedViewingContext context) {
		this.context = context;
	}

	@Override
	protected Point getDrInitialSize() {
		return new Point( 600, 600);
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText("zed's analysis results");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {		
		Composite composite = (Composite) super.createDialogArea(parent);							
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=2;        
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
                        
        StyledText label = new StyledText ( composite, SWT.NONE);
        String prefix = "Analysis data of ";
        
		Artifact analysisTerminal = context.getArtifact();
		String suffix = analysisTerminal.toVersionedStringRepresentation();	        
		label.setText( prefix + suffix + " " + context.getWorstRating().toString());        
        
        StyleRange styleRange = new StyleRange();
    	styleRange.start = prefix.length() + analysisTerminal.getGroupId().length() + 1;
    	styleRange.length = analysisTerminal.getArtifactId().length();
    	styleRange.fontStyle = SWT.BOLD;
    	
    	label.setStyleRange(styleRange);
    	label.setForeground( parent.getForeground());
    	label.setBackground( parent.getBackground());
        label.setEditable(false);
        //label.setFont(bigFont);        
        label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 2,1));
        
        Button saveRatingsButton = new Button(composite, SWT.PUSH);    	    
    	saveRatingsButton.setImage( saveRatingsImage);
    	saveRatingsButton.setToolTipText( "Save currently active fingerprint ratings to file");
    	
    	saveRatingsButton.setLayoutData(  new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    	saveRatingsButton.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) { 
				viewContextStorageHandler.storeRatings(context);				
				super.widgetSelected(e);
			}    			
		});
    	
        
        
    	Button saveButton = new Button(composite, SWT.PUSH);    	    
    	saveButton.setImage( saveImage);
    	saveButton.setToolTipText( "Save currently displayed analysis data to file");
    	
    	saveButton.setLayoutData(  new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    	saveButton.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) { 
				viewContextStorageHandler.store(context);				
				super.widgetSelected(e);
			}    			
		});
        
        
        
        tabFolder = new CTabFolder(composite, SWT.NONE);
        tabFolder.setLayout( layout);
        tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));

        //
        // dependencies
        //
        CTabItem dependencyAnalysisItem = new CTabItem(tabFolder, SWT.NONE);
        DependencyForensicsResult dependencyForensicsResult = context.getDependencyForensicsResult();
        
        // determine the overall rating of the finger prints and choose image from this         
        ForensicsRating dependencyRating = context.getRatingRegistry().getWorstRatingOfFingerPrints( dependencyForensicsResult.getFingerPrintsOfIssues());        
    	dependencyAnalysisItem.setImage(  getRatedImageForTabfolder(dependencyRating));
    	dependencyAnalysisItem.setText("dependency analysis");
    	
    	dependencyAnalysisViewer = new DependencyAnalysisViewer(context);
    	dependencyAnalysisViewer.setUiSupport(uiSupport);
    	dependencyAnalysisViewer.setNodes( DependencyAnalysisContentTransposer.transpose(context, dependencyForensicsResult));
    	    	     	
    	Composite dependencyViewerComposite = dependencyAnalysisViewer.createControl( tabFolder, "dependencies");
    	dependencyViewerComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
    	dependencyAnalysisItem.setControl( dependencyViewerComposite);
    	
    	//
    	// classpath
    	//
    	ClasspathForensicsResult classpathForensicsResult = context.getClasspathForensicsResult();
    	if (classpathForensicsResult != null && classpathForensicsResult.getFingerPrintsOfIssues().size() > 0) {
    		// show module information 
    		List<ClasspathDuplicateNode> shadowingNodes = new ClasspathAnalysisContentTransposer().transpose(context, classpathForensicsResult);
			  
	    	CTabItem classpathAnalysisItem = new CTabItem(tabFolder, SWT.NONE);
	    	// determine the overall rating of the finger prints and choose image from		  
	    	ForensicsRating classpathRating = context.getRatingRegistry().getWorstRatingOfFingerPrints(classpathForensicsResult.getFingerPrintsOfIssues());	 
	    	classpathAnalysisItem.setImage( getRatedImageForTabfolder(classpathRating));
	    	classpathAnalysisItem.setText("classpath analysis"); 
	    	
	    	classpathAnalysisViewer = new ClasspathAnalysisViewer(context);
	    	classpathAnalysisViewer.setUiSupport(uiSupport);
	    	List<Node> nodes = new ArrayList<>( shadowingNodes);
	    	classpathAnalysisViewer.setNodes( nodes);
		  
	    	Composite classpathViewerComposite = classpathAnalysisViewer.createControl(tabFolder, "classpath issues"); 
	    	classpathViewerComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true)); 
	    	classpathAnalysisItem.setControl( classpathViewerComposite);
    	}
    	
		// 
		// model 
    	// 
    	ModelForensicsResult modelForensicsResult = context.getModelForensicsResult(); 
    	if (modelForensicsResult != null && modelForensicsResult.getFingerPrintsOfIssues().size() > 0) {
    	
	    	//ModelAnalysisNode modelAnalysisNode = ModelAnalysisContentTransposer.transpose(context, modelForensicsResult);
	    	//ModelAnalysisNode modelAnalysisNode = ModelAnalysisContentTransposer.transposePerOwner(context, modelForensicsResult);
			  
	    	CTabItem modelAnalysisItem = new CTabItem(tabFolder, SWT.NONE);
	    	// determine the overall rating of the finger prints and choose image from		  
	    	ForensicsRating modelRating = context.getRatingRegistry().getWorstRatingOfFingerPrints(modelForensicsResult.getFingerPrintsOfIssues());	 
	    	modelAnalysisItem.setImage( getRatedImageForTabfolder(modelRating));
	    	modelAnalysisItem.setText("model analysis"); 
	    	
	    	modelAnalysisViewer = new ModelAnalysisViewer(context);
	    	modelAnalysisViewer.setUiSupport(uiSupport); 
	    	modelAnalysisViewer.setModelForensics(modelForensicsResult);
		  
	    	Composite modelViewerComposite = modelAnalysisViewer.createControl(tabFolder, "findings"); modelViewerComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true)); 
	    	modelAnalysisItem.setControl( modelViewerComposite);
    	}
	
			    	    	
    	//
    	// module
    	//
    	ModuleForensicsResult moduleForensicsResult = context.getModuleForensicsResult();
    	if (moduleForensicsResult!= null &&  moduleForensicsResult.getModuleImports().size() > 0) {
    		// show module information 
    		List<PackageNode> moduleReferenceNodes = new ModuleAnalysisContentTransposer().transpose(context, moduleForensicsResult);
			  
	    	CTabItem moduleAnalysisItem = new CTabItem(tabFolder, SWT.NONE);
	    	// determine the overall rating of the finger prints and choose image from		  
	    	ForensicsRating modelRating = context.getRatingRegistry().getWorstRatingOfFingerPrints(moduleForensicsResult.getFingerPrintsOfIssues());	 
	    	moduleAnalysisItem.setImage( getRatedImageForTabfolder(modelRating));
	    	moduleAnalysisItem.setText("module analysis"); 
	    	
	    	moduleAnalysisViewer = new ModuleAnalysisViewer(context);
	    	moduleAnalysisViewer.setUiSupport(uiSupport);
	    	List<Node> nodes = new ArrayList<>( moduleReferenceNodes);
	    	moduleAnalysisViewer.setNodes( nodes);
		  
	    	Composite moduleViewerComposite = moduleAnalysisViewer.createControl(tabFolder, "required modular imports"); moduleViewerComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true)); 
	    	moduleAnalysisItem.setControl( moduleViewerComposite);
    	}
    	
    	//
    	// extraction data 
    	//     	
    	Artifact artifact = context.getArtifact();
    	Node artifactNode = new ExtractionTransposer().transposeArtifact( new ZedExtractionTransposingContext(), artifact);
    	
    	CTabItem extractionItem = new CTabItem(tabFolder, SWT.NONE);
    	// determine the overall rating of the finger prints and choose image from		  
    	ForensicsRating modelRating = context.getRatingRegistry().getWorstRatingOfFingerPrints(moduleForensicsResult.getFingerPrintsOfIssues());	 
    	extractionItem.setImage( getRatedImageForTabfolder(modelRating));
    	extractionItem.setText("content analysis"); 
    	
    	extractionViewer = new ExtractionViewer( new ZedExtractionViewerContext());
    	extractionViewer.setUiSupport(uiSupport);    	
    	extractionViewer.setArtifactNode( artifactNode);
	  
    	Composite moduleViewerComposite = extractionViewer.createControl(tabFolder, "extraction data"); moduleViewerComposite.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true)); 
    	extractionItem.setControl( moduleViewerComposite);
    	
    
    	
    	//
    	// scan issues 
    	//
    	Reason analyzerReturnReason = context.getAnalyzerReturnReason();
    	if (analyzerReturnReason != null) {
    		CTabItem failureItem = new CTabItem(tabFolder, SWT.NONE);
	        failureItem.setImage( warningImage);
	        failureItem.setText("issues during analysis");
	        reasonViewer = new TransposedReasonViewer( analyzerReturnReason);
	        reasonViewer.setShowTypes(false);
	        reasonViewer.setUiSupport( uiSupport);
	        Composite reasonViewerComposite = reasonViewer.createControl( tabFolder, "failure");
	        reasonViewerComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
	        failureItem.setControl( reasonViewerComposite);
    	}
    	
    	tabFolder.setSelection(0);
    	
    	
        return composite;
	}

	private Image getRatedImageForTabfolder(ForensicsRating rating) {		
		return ratingToImageMap.get(rating);
	}

	@Override
	public void dispose() {
		if (dependencyAnalysisViewer != null) {
			dependencyAnalysisViewer.dispose();
		}
		if (modelAnalysisViewer != null) {
			modelAnalysisViewer.dispose();
		}
		if (moduleAnalysisViewer != null) {
			moduleAnalysisViewer.dispose();
		}
		if (extractionViewer != null) {
			extractionViewer.dispose();
		}
		if (reasonViewer != null) {
			reasonViewer.dispose();
		}		
	}

	

}
