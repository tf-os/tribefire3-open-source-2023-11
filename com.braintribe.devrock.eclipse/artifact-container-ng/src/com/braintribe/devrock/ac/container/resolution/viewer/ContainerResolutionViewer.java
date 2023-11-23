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
package com.braintribe.devrock.ac.container.resolution.viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerPlugin;
import com.braintribe.devrock.ac.container.plugin.ArtifactContainerStatus;
import com.braintribe.devrock.ac.container.properties.ResolutionYamlMarshaller;
import com.braintribe.devrock.ac.container.repository.FileRepositoryPurger;
import com.braintribe.devrock.api.clipboard.ArtifactToClipboardExpert;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.editors.support.EditorSupport;
import com.braintribe.devrock.api.ui.viewers.artifacts.DetailRequestHandler;
import com.braintribe.devrock.api.ui.viewers.artifacts.TransposedAnalysisArtifactViewer;
import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer.Transposer;
import com.braintribe.devrock.api.ui.viewers.pom.PomViewer;
import com.braintribe.devrock.api.ui.viewers.reason.transpose.TransposedReasonViewer;
import com.braintribe.devrock.api.ui.viewers.yaml.YamlViewer;
import com.braintribe.devrock.bridge.eclipse.workspace.BasicWorkspaceProjectInfo;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DependerNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.mc.core.commons.ArtifactRemover;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.artifact.analysis.AnalysisTerminal;
import com.braintribe.model.artifact.analysis.DependencyClash;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.compiled.CompiledPartIdentification;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.wire.api.util.Lists;

/**
 * AC's resolution viewer
 * @author pit
 *
 */

public class ContainerResolutionViewer extends DevrockDialog implements DetailRequestHandler, DisposeListener, IDisposable {
		private static final String CONTAINER_RESOLUTION_VIEWER_STYLER_KEY = "container.resolution.viewer";
		private static Logger log = Logger.getLogger(ContainerResolutionViewer.class);
		public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
		
		private final Image successImage;
		private final Image errorImage;
		private final Image warningImage;
		private final Image saveImage;
		
		private final Map<TransposedAnalysisArtifactViewer, CTabItem> additionalDetailViewersToItem = new HashMap<>();
		private final Map<CTabItem, TransposedAnalysisArtifactViewer> additionalItemsToDetailViewers = new HashMap<>();
		
		private List<VersionedArtifactIdentification> purgedArtifacts = new ArrayList<>();

		private final Predicate<AnalysisTerminal> failedTerminalFilter = new Predicate<AnalysisTerminal>() {
			@Override
			public boolean test(AnalysisTerminal t) {
				if (t instanceof AnalysisArtifact) {
					return ((AnalysisArtifact)t).hasFailed();
				}
				if (t instanceof AnalysisDependency) {
					return ((AnalysisDependency)t).hasFailed();
				}
				return false;
			}			
		};
	
		
		private AnalysisArtifactResolution resolution;
		private Map<AnalysisArtifact, IProject> projectDependencies;
				
		private TransposedAnalysisArtifactViewer transposedIncompleteArtifactsViewer;
		private TransposedAnalysisArtifactViewer transposedUnresolvedDependenciesViewer;
		private TransposedAnalysisArtifactViewer transposedTerminalViewer;
		private TransposedAnalysisArtifactViewer transposedSolutionViewer;
		private TransposedAnalysisArtifactViewer transposedProjectsViewer;
		private TransposedAnalysisArtifactViewer transposedAllArtifactsViewer;		
		private TransposedAnalysisArtifactViewer transposedFilteredDependenciesViewer;
		private TransposedAnalysisArtifactViewer clashesViewer;
		private TransposedAnalysisArtifactViewer parentArtifactsViewer;
						
		private TransposedReasonViewer reasonViewer;
		private TransposedReasonViewer originationViewer;
		private YamlViewer yamlViewer;
		private final Transposer transposer;
				
		
		
		private final ContainerResolutionViewController viewController;
		private CTabFolder tabFolder;
		private final UiSupport uiSupport = ArtifactContainerPlugin.instance().uiSupport();
		private Future<RepositoryReflection> preemptiveRepositoryReflectionRetrievalFuture;	
		
		public ContainerResolutionViewer(Shell parentShell) {
			super(parentShell);
			
			setShellStyle(SHELL_STYLE);
			
			// 			
			successImage = uiSupport.images().addImage("success", ContainerResolutionViewer.class, "success.gif");						
			warningImage = uiSupport.images().addImage("warning", ContainerResolutionViewer.class, "warning.png");						
			errorImage = uiSupport.images().addImage("error", ContainerResolutionViewer.class, "error.gif");
			saveImage = uiSupport.images().addImage("save", ContainerResolutionViewer.class, "save_file.transparent.png");
			
			transposer = new Transposer();
			
			viewController = new ContainerResolutionViewController();
			viewController.setTransposer(transposer);
					
						
		}
		
							
		@Configurable @Required
		public void setResolution(AnalysisArtifactResolution resolution) {
			this.resolution = resolution;
			transposer.setResolution(resolution);
						
		}
		
		@Configurable
		public void setProjectDependencies(Map<AnalysisArtifact, IProject> projectDependencies) {
			this.projectDependencies = projectDependencies;
			transposer.setProjectDependencies(projectDependencies);
		}
		
		public void preemptiveDataRetrieval() {
			// parallel initial transposition of the different tab's data
			viewController.preemptiveDataRetrieval( Lists.list( 
					Transposer.CONTEXT_TERMINAL, // 
					Transposer.CONTEXT_SOLUTIONS, // 
					Transposer.CONTEXT_ALL,  //
					Transposer.CONTEXT_FILTERED, // 
					Transposer.CONTEXT_CLASHES,  // 
					Transposer.CONTEXT_INCOMPLETE, //
					Transposer.CONTEXT_UNRESOLVED, //  
					Transposer.CONTEXT_PARENTS,
					Transposer.CONTEXT_PROJECTS
					)
			); //	
			preemptiveRepositoryReflectionRetrievalFuture = viewController.preemptiveRepositoryReflectionRetrieval();
		}
				

		@Override
		protected Control createDialogArea(Composite parent) {	
			log.trace("creating dialog area ... ");
			long before = System.nanoTime();
			
			uiSupport.stylers(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY, parent.getFont());			
			
			initializeDialogUnits(parent);
			final Composite composite = new Composite(parent, SWT.NONE);

			int nColumns= 4;
	        GridLayout layout= new GridLayout();
	        layout.numColumns = nColumns;
	        composite.setLayout( layout);
	        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
	        
	        StyledText label = new StyledText ( composite, SWT.NONE);
	        String prefix = "Backing resolution of ";
	        AnalysisTerminal analysisTerminal = resolution.getTerminals().get(0);
			String suffix = analysisTerminal.asString();	        
	        label.setText( prefix + suffix);
	        
	        StyleRange styleRange = new StyleRange();
	    	styleRange.start = prefix.length() + analysisTerminal.getGroupId().length() + 1;
	    	styleRange.length = analysisTerminal.getArtifactId().length();
	    	styleRange.fontStyle = SWT.BOLD;
	    	label.setStyleRange(styleRange);
	    	label.setForeground( parent.getForeground());
	    	label.setBackground( parent.getBackground());
	        label.setEditable(false);
	        //label.setFont(bigFont);	        
	        label.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false, 3,1));
	        
	        // save button 
	        Button saveResolution = new Button( composite, SWT.PUSH);
	        saveResolution.setImage( saveImage);
	        saveResolution.setToolTipText("Store the current resolution to a file");
	        saveResolution.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false, 1,1));
	        saveResolution.addSelectionListener( new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					acknowledgeResolutionDumpRequest();
					super.widgetSelected(e);
				}
	        	
			});
	        
	        
	        tabFolder = new CTabFolder(composite, SWT.NONE);
	        tabFolder.setLayout( layout);
	        tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
	        
	        //
	        // origination
	        //
	        if (resolution.getOrigination() != null) {
	        	CTabItem originationItem = new CTabItem(tabFolder, SWT.NONE);
	        	originationItem.setImage( successImage);
	        	originationItem.setText("origination");
	        	originationViewer = new TransposedReasonViewer( resolution.getOrigination());
	        	originationViewer.setUiSupport( ArtifactContainerPlugin.instance().uiSupport());
	        	originationViewer.setShowTypes(false);
	        	Composite originationViewerComposite = originationViewer.createControl( tabFolder, "origination");
	        	originationViewerComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
	        	originationItem.setControl( originationViewerComposite);
	        }
	        
	        //
	        // failure
	        //
	        if (resolution.hasFailed()) {
		        CTabItem failureItem = new CTabItem(tabFolder, SWT.NONE);
		        failureItem.setImage( warningImage);
		        failureItem.setText("failure");
		        reasonViewer = new TransposedReasonViewer( resolution.getFailure());
		        reasonViewer.setUiSupport( ArtifactContainerPlugin.instance().uiSupport());
		        Composite reasonViewerComposite = reasonViewer.createControl( tabFolder, "failure");
		        reasonViewerComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
		        failureItem.setControl( reasonViewerComposite);
	        }
	        
	        
	        //
	        // terminal
	        //
	        CTabItem terminalItem = new CTabItem(tabFolder, SWT.NONE);
	        terminalItem.setText("terminal");	        
	        if (resolution.getTerminals().stream().filter( failedTerminalFilter).findFirst().orElse(null) != null) {
	        	terminalItem.setImage( errorImage);
	        }
	        else {
	        	terminalItem.setImage( successImage);
	        }
	   
	        transposedTerminalViewer = new TransposedAnalysisArtifactViewer( getShell(), Transposer.CONTEXT_TERMINAL);
	        ViewHandler terminalHandler = new ViewHandler(Transposer.CONTEXT_TERMINAL, viewController);
	        
	        transposedTerminalViewer.setInitialNodeSupplier( viewController::getInitialData);
	        transposedTerminalViewer.setNodeSupplier( terminalHandler::supplyNodes);
	        transposedTerminalViewer.setCapabilityActivationSupplier( terminalHandler::supplyCapability);	       
	        transposedTerminalViewer.setDetailRequestHandler(this);
	        transposedTerminalViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);

	        transposedTerminalViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
	        transposedTerminalViewer.setUiSupport(uiSupport);
	        	        	        
	        Composite terminalViewerComposite = transposedTerminalViewer.createControl( tabFolder, "terminal of the resolution");	      
	        terminalItem.setControl( terminalViewerComposite);
	   
	        //
	        // solutions
	        //
	        List<AnalysisArtifact> solutions = resolution.getSolutions();
	        CTabItem solutionsItem = null;
	        if (solutions.size() > 0) {	        
		        solutionsItem = new CTabItem(tabFolder, SWT.NONE);
		        solutionsItem.setText("solutions");	
				if (solutions.stream().filter( failedTerminalFilter).findFirst().orElse(null) != null) {
		        	solutionsItem.setImage( errorImage);
		        }
		        else {
		        	solutionsItem.setImage( successImage);
		        }
		        	        	
		        ViewHandler solutionsHandler = new ViewHandler(Transposer.CONTEXT_SOLUTIONS, viewController);
		        transposedSolutionViewer = new TransposedAnalysisArtifactViewer( getShell(), Transposer.CONTEXT_SOLUTIONS);
		   
		        transposedSolutionViewer.setInitialNodeSupplier( viewController::getInitialData);	        
		        transposedSolutionViewer.setNodeSupplier( solutionsHandler::supplyNodes);
		        transposedSolutionViewer.setCapabilityActivationSupplier( solutionsHandler::supplyCapability);	        
		        transposedSolutionViewer.setDetailRequestHandler(this);
		        	        
		        transposedSolutionViewer.setUiSupport(uiSupport);
		        transposedSolutionViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        transposedSolutionViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
		        
		        Composite solutionsViewerComposite = transposedSolutionViewer.createControl( tabFolder, "artifacts contributing to the classpath");	        
		        solutionsItem.setControl( solutionsViewerComposite);
	        }
	        
	        //
	        // projects
	        // 
	        if (projectDependencies != null && projectDependencies.size() > 0) {
	        	CTabItem projectDependenciesItem = new CTabItem(tabFolder, SWT.NONE);
		        projectDependenciesItem.setText("projects");	
				projectDependenciesItem.setImage( successImage);
		        
		        	        	
		        ViewHandler projectsHandler = new ViewHandler(Transposer.CONTEXT_PROJECTS, viewController);
		        transposedProjectsViewer = new TransposedAnalysisArtifactViewer( getShell(), Transposer.CONTEXT_PROJECTS);
		   
		        transposedProjectsViewer.setInitialNodeSupplier( viewController::getInitialData);	        
		        transposedProjectsViewer.setNodeSupplier( projectsHandler::supplyNodes);
		        transposedProjectsViewer.setCapabilityActivationSupplier( projectsHandler::supplyCapability);	        
		        transposedProjectsViewer.setDetailRequestHandler(this);
		        transposedProjectsViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
		        	        
		        transposedProjectsViewer.setUiSupport(uiSupport);
		        transposedProjectsViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        
		        Composite projectsViewerComposite = transposedProjectsViewer.createControl( tabFolder, "projects contributing to the classpath");	        
		        projectDependenciesItem.setControl( projectsViewerComposite);
	        }
	        
	        //
	        // all
	        //
	        if (solutions.size() > 0) {
		        CTabItem allArtifactItem = new CTabItem(tabFolder, SWT.NONE);
		        allArtifactItem.setText("all");	
		        if (solutions.stream().filter( failedTerminalFilter).findFirst().orElse(null) != null) {
		        	allArtifactItem.setImage( errorImage);
		        }
		        else {
		        	allArtifactItem.setImage( successImage);
		        }	 
		        ViewHandler allHandler = new ViewHandler(Transposer.CONTEXT_ALL, viewController);
		        transposedAllArtifactsViewer = new TransposedAnalysisArtifactViewer(getShell(), Transposer.CONTEXT_ALL);
		        
		   
		        transposedAllArtifactsViewer.setInitialNodeSupplier( viewController::getInitialData);	        
		        transposedAllArtifactsViewer.setNodeSupplier( allHandler::supplyNodes);
		        transposedAllArtifactsViewer.setCapabilityActivationSupplier( allHandler::supplyCapability);	        	        
		        transposedAllArtifactsViewer.setDetailRequestHandler(this);
		        transposedAllArtifactsViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
		        	      
		        transposedAllArtifactsViewer.setUiSupport(uiSupport);
		        transposedAllArtifactsViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        
		        Composite allArtifactsViewerComposite = transposedAllArtifactsViewer.createControl( tabFolder, "all relevant artifacts");	     
		        allArtifactItem.setControl( allArtifactsViewerComposite);
		    }
	        
	        
	        // 
	        // parent
	        //
	        if (solutions.size() > 0) {
		        CTabItem parentArtifactItem = new CTabItem(tabFolder, SWT.NONE);
		        parentArtifactItem.setText("parents");	
		        parentArtifactItem.setImage( successImage);
		        	 
		        ViewHandler parentHandler = new ViewHandler(Transposer.CONTEXT_PARENTS, viewController);
		        parentArtifactsViewer = new TransposedAnalysisArtifactViewer(getShell(), Transposer.CONTEXT_PARENTS);
		        
		   
		        parentArtifactsViewer.setInitialNodeSupplier( viewController::getInitialData);	        
		        parentArtifactsViewer.setNodeSupplier( parentHandler::supplyNodes);
		        parentArtifactsViewer.setCapabilityActivationSupplier( parentHandler::supplyCapability);	        	        
		        parentArtifactsViewer.setDetailRequestHandler(this);
		        parentArtifactsViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
		        	      
		        parentArtifactsViewer.setUiSupport(uiSupport);
		        parentArtifactsViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        
		        Composite parentArtifactsViewerComposite = parentArtifactsViewer.createControl( tabFolder, "parent artifacts");	     
		        parentArtifactItem.setControl( parentArtifactsViewerComposite);
		     }
	        
	        
	        // 
	        // filtered
	        //
	        Set<AnalysisDependency> filteredDependencies = resolution.getFilteredDependencies();
	        if (filteredDependencies.size() > 0) {
	        	CTabItem filteredItem = new CTabItem(tabFolder, SWT.NONE);
	        	filteredItem.setText("filtered dependencies");
	        	filteredItem.setImage( warningImage);
	        	
	        	// filtered viewer	        
	        	transposedFilteredDependenciesViewer = new TransposedAnalysisArtifactViewer(getShell(), Transposer.CONTEXT_FILTERED);
	        	
	        	ViewHandler filteredHandler = new ViewHandler(Transposer.CONTEXT_FILTERED, viewController);
		    
	        
	        	transposedFilteredDependenciesViewer.setInitialNodeSupplier( viewController::getInitialData);	        	
		        transposedFilteredDependenciesViewer.setNodeSupplier( filteredHandler::supplyNodes);
	        	transposedFilteredDependenciesViewer.setCapabilityActivationSupplier( filteredHandler::supplyCapability);
	        	transposedFilteredDependenciesViewer.setDetailRequestHandler(this);
	        	transposedFilteredDependenciesViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
	        	
	        	transposedFilteredDependenciesViewer.setUiSupport(uiSupport);
	        	transposedFilteredDependenciesViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        Composite filteredDependenciesViewerComposite = transposedFilteredDependenciesViewer.createControl( tabFolder, "filtered dependencies");		    
		        filteredItem.setControl( filteredDependenciesViewerComposite);
	        }
	        
	        //	     
	        // clashes
	        //
	        List<DependencyClash> clashes = resolution.getClashes();
	        if (clashes.size() > 0) {
	        	CTabItem clashesItem = new CTabItem(tabFolder, SWT.NONE);
	        	clashesItem.setText("clashing dependencies");
	        	clashesItem.setImage( warningImage);
	        	
	        	// clash viewer 
	        	clashesViewer = new TransposedAnalysisArtifactViewer(getShell(), Transposer.CONTEXT_CLASHES);
	        	
	        	ViewHandler clashesHandler = new ViewHandler(Transposer.CONTEXT_CLASHES, viewController);	
		    
	        	clashesViewer.setInitialNodeSupplier( viewController::getInitialData);	        	
		        clashesViewer.setNodeSupplier( clashesHandler::supplyNodes);
	        	clashesViewer.setCapabilityActivationSupplier( clashesHandler::supplyCapability);
	        	clashesViewer.setDetailRequestHandler(this);
	        	clashesViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
	        	
	        	clashesViewer.setUiSupport(uiSupport);
	        	clashesViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        Composite clashesViewerComposite = clashesViewer.createControl( tabFolder, "found clashes and their resolutions");		
		        clashesItem.setControl( clashesViewerComposite);
	        }
	        
	        //
	        // incomplete artifacts
	        //
	        Set<AnalysisArtifact> incompleteArtifacts = resolution.getIncompleteArtifacts();
	        if (incompleteArtifacts.size() > 0) {
	        	CTabItem incompleteArtifactsItem = new CTabItem(tabFolder, SWT.NONE);
	        	incompleteArtifactsItem.setText( "incomplete artifacts");
	        	incompleteArtifactsItem.setImage( warningImage);
	        		        	
	        	transposedIncompleteArtifactsViewer = new TransposedAnalysisArtifactViewer(getShell(), Transposer.CONTEXT_INCOMPLETE);
	        	
	        	ViewHandler incompleteHandler = new ViewHandler(Transposer.CONTEXT_INCOMPLETE, viewController);		       
	        
	        	transposedIncompleteArtifactsViewer.setInitialNodeSupplier( viewController::getInitialData);		       
	        	transposedIncompleteArtifactsViewer.setNodeSupplier( incompleteHandler::supplyNodes);
	        	transposedIncompleteArtifactsViewer.setCapabilityActivationSupplier( incompleteHandler::supplyCapability);
	        	transposedIncompleteArtifactsViewer.setDetailRequestHandler(this);
	        	transposedIncompleteArtifactsViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
	        	
	        	transposedIncompleteArtifactsViewer.setUiSupport(uiSupport);
	        	transposedIncompleteArtifactsViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        Composite incompleteArtifactsViewerComposite = transposedIncompleteArtifactsViewer.createControl( tabFolder, "incomplete artifacts");		     
		        incompleteArtifactsItem.setControl( incompleteArtifactsViewerComposite);
	        }
	        
	        //
	        // unresolved dependencies
	        //
	        Set<AnalysisDependency> unresolvedDependencies = resolution.getUnresolvedDependencies();
	        if (unresolvedDependencies.size() > 0) {
	        	CTabItem unresolveDependenciesItem = new CTabItem(tabFolder, SWT.NONE);
	        	unresolveDependenciesItem.setText( "unresolved dependencies");
	        	unresolveDependenciesItem.setImage( warningImage);
	        	
	        	transposedUnresolvedDependenciesViewer = new TransposedAnalysisArtifactViewer(getShell(), Transposer.CONTEXT_UNRESOLVED);
	        	ViewHandler incompleteHandler = new ViewHandler(Transposer.CONTEXT_UNRESOLVED, viewController);
	        	
	        	transposedUnresolvedDependenciesViewer.setInitialNodeSupplier( viewController::getInitialData);	        	
	        	transposedUnresolvedDependenciesViewer.setNodeSupplier( incompleteHandler::supplyNodes);
	        	transposedUnresolvedDependenciesViewer.setCapabilityActivationSupplier( incompleteHandler::supplyCapability);
	        	transposedUnresolvedDependenciesViewer.setDetailRequestHandler(this);
	        	transposedUnresolvedDependenciesViewer.setRepositoryReflection(preemptiveRepositoryReflectionRetrievalFuture);
	        	
	        	transposedUnresolvedDependenciesViewer.setUiSupport(uiSupport);
	        	transposedUnresolvedDependenciesViewer.setUiSupportStylersKey(CONTAINER_RESOLUTION_VIEWER_STYLER_KEY);
		        Composite unresolvedArtifactsViewerComposite = transposedUnresolvedDependenciesViewer.createControl( tabFolder, "unresolved dependencies");		  
		        unresolveDependenciesItem.setControl( unresolvedArtifactsViewerComposite);	        		        	
	        }
	        
	        boolean showYamlTab = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_YAML_ENABLED, false); 
	        // 
	        // yaml tab 
	        //
	        if (showYamlTab) {
		        CTabItem yamlItem = new CTabItem(tabFolder, SWT.NONE);
		        yamlItem.setText("yaml");	        
		        
		        yamlViewer = new YamlViewer();
		        yamlViewer.setResolution(resolution);
		        
		        Composite yamlViewerComposite = yamlViewer.createControl(tabFolder, "yaml");
		        yamlItem.setControl(yamlViewerComposite);	        	        	        
	        }
	        	        
	        boolean showTerminalFirst = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_INITIAL_TAG_TERMINAL, true);
	        if (showTerminalFirst || solutionsItem == null) {
	        	tabFolder.setSelection( terminalItem);	        	
	        }
	        else {
	        	tabFolder.setSelection( solutionsItem);
	        }
	        
	        long after = System.nanoTime();
	        log.trace("create dialog area took [" + ((after-before) / 1E6) + "] ms");
	        return composite;
		}

		
		@Override
		public void dispose() {
			if (reasonViewer != null) {
				reasonViewer.dispose();
			}
			if (originationViewer != null) {
				originationViewer.dispose();
			}
			
			if (transposedTerminalViewer != null) {
				transposedTerminalViewer.dispose();
			}
			
			if (transposedSolutionViewer != null) {
				transposedSolutionViewer.dispose();
			}
			if (transposedAllArtifactsViewer != null) {
				transposedAllArtifactsViewer.dispose();
			}
						
			if (clashesViewer != null) {
				clashesViewer.dispose();
			}
			
			if (transposedFilteredDependenciesViewer != null) {
				transposedFilteredDependenciesViewer.dispose();
			}			
			
			if (transposedIncompleteArtifactsViewer != null) {
				transposedIncompleteArtifactsViewer.dispose();
			}
			if (transposedUnresolvedDependenciesViewer != null) {
				transposedUnresolvedDependenciesViewer.dispose();
			}
			if (yamlViewer != null) {
				yamlViewer.dispose();
			}
			
			// dispose any additional viewers that are still open
			additionalItemsToDetailViewers.values().stream().forEach( v -> v.dispose());			
		}

		@Override
		public boolean close() {
			dispose();
			
			//bigFont.dispose();
			
			
			return super.close();
		}
		
		@Override
		protected Point getDrInitialSize() {		
			return new Point( 800, 600);
		}
		
		private String getIdentification(Node node) {
			if (node instanceof AnalysisNode) {
				AnalysisNode aNode = (AnalysisNode) node;
				VersionedArtifactIdentification vaiS = aNode.getSolutionIdentification();
				VersionedArtifactIdentification vaiD = aNode.getDependencyIdentification();
				if (vaiS != null) 
					return vaiS.asString();
				if (vaiD != null)
					return vaiD.asString();
			}
			else if (node instanceof DependerNode){ 
				DependerNode dNode = (DependerNode) node;
				VersionedArtifactIdentification vaiD = dNode.getDependerArtifact();
				return vaiD.asString();
			}		
			return "unknown";
		}

		@Override
		public void acknowledgeOpenDetailRequest(Node node) {			
			if (
					node instanceof AnalysisNode == false && 
					node instanceof DependerNode == false
				) {
				return;
			}			
			CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
	        tabItem.setText( getIdentification( node));	
	        tabItem.addDisposeListener( this);
		        
			ViewHandler viewHandler = new ViewHandler(Transposer.CONTEXT_DETAIL, viewController, node);
			TransposedAnalysisArtifactViewer transposedViewer = new TransposedAnalysisArtifactViewer( getShell(), Transposer.CONTEXT_DETAIL);	        		
	        transposedViewer.setNodeSupplier( viewHandler::supplyNodes);
	        transposedViewer.setCapabilityActivationSupplier( viewHandler::supplyCapability);	        
	        transposedViewer.setDetailRequestHandler(this);
	        	        
	        transposedViewer.setUiSupport(uiSupport);	        
	        Composite solutionsViewerComposite = transposedViewer.createControl( tabFolder, "detail of " + getIdentification(node));	        
	        tabItem.setControl( solutionsViewerComposite);
	        
	        additionalDetailViewersToItem.put(transposedViewer, tabItem);
	        additionalItemsToDetailViewers.put(tabItem, transposedViewer);
	        tabFolder.setSelection(tabItem);
		}

		@Override
		public void acknowledgeCloseDetailRequest(TransposedAnalysisArtifactViewer viewer) {		
			CTabItem item = additionalDetailViewersToItem.get(viewer);
			if (item == null)
				return;			
			item.dispose();
		}
		
		/**
		 * retrieves the pom from a {@link Node}
		 * @param node - either an {@link AnalysisNode} or a {@link DependerNode}
		 * @return - if artifact's in the workspace, an {@link IResource} or a {@link File} pointing to the pom (pom.xml if in workspace, *.pom otherwise) 
		 */
		private Pair<VersionedArtifactIdentification, Object> getPomFromNode( Node node) {
			// determine owning artifact
			AnalysisArtifact owner;
			if (node instanceof AnalysisNode) {
				AnalysisNode analysisNode = (AnalysisNode) node;
				owner = analysisNode.getBackingSolution();
			} else if (node instanceof DependerNode) {
				DependerNode dependerNode = (DependerNode) node;
				owner = dependerNode.getBackingArtifact();				
			}
			else {
				return null;
			}
			// determine whether it's in the workspace or not 
			BasicWorkspaceProjectInfo projectInfo = DevrockPlugin.instance().getWorkspaceProjectView().getProjectInfo(owner);
			if (projectInfo == null) {
				// not in the workspace, resolve the pom 
				CompiledPartIdentification cpi = CompiledPartIdentification.from( CompiledArtifactIdentification.from(owner), PartIdentifications.pom);
				Maybe<File> maybe = DevrockPlugin.mcBridge().resolve( cpi);
				if (maybe.isSatisfied()) {
					return Pair.of( owner, maybe.get());
				}
				else {
					ArtifactContainerStatus status = new ArtifactContainerStatus( "no pom found for repository artifact [" + owner.asString(), IStatus.ERROR);
					ArtifactContainerPlugin.instance().log(status);
				}
			} 
			else {
				// in the workspace, find the pom project member 
				IProject project = projectInfo.getProject();
				IResource resource = project.findMember("pom.xml");
				if (resource != null) {
					return Pair.of( owner, resource);
				}
				else {
					ArtifactContainerStatus status = new ArtifactContainerStatus( "no pom found for workspace artifact [" + owner.asString(), IStatus.ERROR);
					ArtifactContainerPlugin.instance().log(status);
				}
			}
			
			return null;
		}
		
	

		@Override
		public void acknowledgeOpenPomRequest(Node node) {
			Pair<VersionedArtifactIdentification, Object> pair = getPomFromNode(node);
			Object obj = pair.second;
			if (obj instanceof File) {
				EditorSupport.load( (File) obj);
			}
			else if (obj instanceof IResource) {
				EditorSupport.load( (IResource) obj);
			}
		}
		
		/**
		 * add a tab for the pom
		 * @param obj
		 * @param owner
		 */
		private void addPomTab(Object obj, VersionedArtifactIdentification owner) {
		    CTabItem pomItem = new CTabItem(tabFolder, SWT.CLOSE);
		    CompiledPartIdentification cpi = CompiledPartIdentification.from( CompiledArtifactIdentification.from(owner), PartIdentifications.pom);
	        pomItem.setText( cpi.asFilename());
	        pomItem.setToolTipText( cpi.asString());
	        
	        PomViewer pomViewer = new PomViewer();
	        if (obj instanceof File) {
	        	pomViewer.setPom( (File) obj);	        	
	        }
	        else if (obj instanceof IResource) {
	        	IResource iResource = (IResource) obj;
	        	File file = iResource.getLocation().toFile();
	        	pomViewer.setPom( file);	
	        }
	        	        
	        Composite pomViewerComposite = pomViewer.createControl(tabFolder, cpi.asString());
	        pomItem.setControl(pomViewerComposite);	    
	        
	        tabFolder.setSelection(pomItem);
		}
		
		@Override
		public void acknowledgeViewPomRequest(Node node) {									
			Pair<VersionedArtifactIdentification, Object> pomObject = getPomFromNode(node);
			if (pomObject == null) {
				// no backing pom (i.e. test data of a - for instance - repolet run)
				return;
			}
			VersionedArtifactIdentification owner = pomObject.first;
			Object pObj = pomObject.second;
			if (pObj != null) {
				addPomTab( pObj, owner);
			}						
		}
		
		

		@Override
		public void acknowledgeCopyDependencyToClipboardRequest(Node node) {
			CompiledDependencyIdentification cdi = getCompiledDependencyFromNode( node);
			VersionModificationAction action = VersionModificationAction.untouched;
			ArtifactToClipboardExpert.copyToClipboard(action, Collections.singletonList(cdi));			
		}
		
		/**
		 * build a {@link CompiledDependencyIdentification} to be imported from the selected node in the viewer
		 * @param node - the {@link Node}
		 * @return - a {@link CompiledDependencyIdentification} derived 
		 */
		private CompiledDependencyIdentification getCompiledDependencyFromNode( Node node) {
			CompiledDependencyIdentification cdi = null;
			if (node instanceof AnalysisNode) {
				AnalysisNode an = (AnalysisNode) node;
				AnalysisDependency backingDependency = an.getBackingDependency();
				if (backingDependency != null) {
					cdi = CompiledDependencyIdentification.from(backingDependency);
					return cdi;
				}
				AnalysisArtifact backingSolution = an.getBackingSolution();
				if (backingSolution != null) {
					cdi = CompiledDependencyIdentification.from(backingSolution);
					return cdi;
				}
			}
			else if (node instanceof DependerNode) {
				DependerNode dn = (DependerNode) node;
				cdi = CompiledDependencyIdentification.from(dn.getBackingDependency());
				return cdi;						
			}
			
			return null;
		}
		
		


		@Override
		public void acknowledgeResolutionDumpRequest() {		
			// store 
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());

			String preselected = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_LAST_FILE,null);
			if (preselected == null) {
				List<AnalysisTerminal> terminals = resolution.getTerminals();
				if (terminals.size() == 1) {
					AnalysisArtifact art = (AnalysisArtifact) terminals.get(0);
					preselected = art.asString().replace(':', '.').replace('#', '-');					
				}
			}

			FileDialog fd = new FileDialog(shell, SWT.SAVE);
			if (preselected != null) {
				File lastFile = new File( preselected);								
				fd.setFileName(lastFile.getName());
				fd.setFilterPath( lastFile.getParent());
			}
			fd.setFilterExtensions( new String[] {"*.yaml"});			
			String selectedFile = fd.open();
			
			if (selectedFile == null) {
				return;
			}			
			File file = new File( selectedFile);			
			ResolutionYamlMarshaller.toYamlFile(resolution, file);
			
		}
		
		
				

		@Override
		public boolean acknowledgeObsoleteCheckRequest(VersionedArtifactIdentification vai) {
			if (vai == null  || purgedArtifacts.isEmpty())
				return false;
			for (VersionedArtifactIdentification purgedVai : purgedArtifacts) {
				if (purgedVai.compareTo(vai) == 0) {				
					System.out.println("found it");
					return true;
				}
			}
			return false;
		}


		@Override
		public void acknowledgeRemovalFromPcRepositoryRequest(List<AnalysisNode> nodes) {
			purgedArtifacts.addAll( FileRepositoryPurger.purge(nodes, null));
		}
		
		

		@Override
		public void acknowledgeRemovalFromPcRepositoryRequest() {
			
			// run through the resolution, identify all pc's of 'install'		
			PcArtifactFilter filter = new PcArtifactFilter();
			
			/*
			 * // need to collect parents here (no imports for now) List<AnalysisArtifact>
			 * parents = extractParents(resolution.getSolutions());
			 * 
			 * // call filter List<AnalysisArtifact> pcs =
			 * Stream.concat(resolution.getSolutions().stream(), parents.stream()) .filter(
			 * aa -> filter.filter(aa)) .collect( Collectors.toList());
			 */
			// simply run any by the filter
			resolution.getSolutions().stream().filter( aa -> filter.filter(aa));
			// grab it's internal map of pc-artifacts
			Map<VersionedArtifactIdentification, File> result = filter.getPcIdentifications();
					
			
			// clear
			Pair<List<Reason>,List<File>> pair = ArtifactRemover.removeArtifactsFromFilesystemRepo(result);
			// process
			List<Reason> reasons = pair.first;
			List<File> nonDeletableFiles = pair.second;
			
			if (reasons != null) {
				DevrockPlugin devrockPlugin = DevrockPlugin.instance();
				for (Reason reason : reasons) {
					DevrockPluginStatus status = new DevrockPluginStatus("issues during purge:" + reason.stringify(), IStatus.ERROR);
					devrockPlugin.log(status);					
				}
			}			
			// store for now 
			if (nonDeletableFiles != null) {
				FileRepositoryPurger.storeNonDeleteables(nonDeletableFiles);
			}						
		}

		
		
		/*
		private List<AnalysisArtifact> extractParents(List<AnalysisArtifact> artifacts) {
			Set<AnalysisArtifact> parents = new HashSet<>();
			for (AnalysisArtifact artifact : artifacts) {
				AnalysisDependency parentDependency = artifact.getParent();
				if (parentDependency == null) {
					continue;
				}
				AnalysisArtifact parentSolution = parentDependency.getSolution();
				parents.add(parentSolution);
			}
			return new ArrayList<>( parents);
		}
		*/		

		@Override
		public void widgetDisposed(DisposeEvent event) {
			Widget widget = event.widget;
			TransposedAnalysisArtifactViewer viewer = additionalItemsToDetailViewers.get(widget);
			if (viewer != null) {
				additionalDetailViewersToItem.remove(viewer);
				additionalItemsToDetailViewers.remove(widget);
				viewer.dispose(); // dispose the viewer's resources
			}
			
		}
		
		@Override
		protected void configureShell(Shell newShell) {		
			super.configureShell(newShell);
			
			String title = "analysis of : " + resolution.getTerminals().stream().map( t -> t.asString()).collect(Collectors.joining(","));
			newShell.setText( title);
		}		

}
