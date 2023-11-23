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
package com.braintribe.devrock.api.ui.viewers.artifacts;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.ui.commons.UiSupport;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.api.ui.viewers.artifacts.filter.NodeFilter;
import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.context.BasicViewContext;
import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.context.ViewContextBuilder;
import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer.BasicTranspositionContext;
import com.braintribe.devrock.api.ui.viewers.artifacts.transpose.transposer.TranspositionContextBuilder;
import com.braintribe.devrock.eclipse.model.resolution.CapabilityKeys;
import com.braintribe.devrock.eclipse.model.resolution.nodes.AnalysisNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.DependerNode;
import com.braintribe.devrock.eclipse.model.resolution.nodes.Node;
import com.braintribe.devrock.eclipse.model.resolution.nodes.PartNode;
import com.braintribe.devrock.eclipse.model.storage.TranspositionContext;
import com.braintribe.devrock.eclipse.model.storage.ViewContext;
import com.braintribe.devrock.eclipse.model.storage.ViewerContext;
import com.braintribe.devrock.mc.api.repository.configuration.RepositoryReflection;
import com.braintribe.devrock.model.repository.MavenFileSystemRepository;
import com.braintribe.devrock.model.repository.Repository;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.version.Version;
import com.braintribe.utils.lcd.LazyInitialized;


/**
 * a custom Widget that can be inserted in any {@link Composite} and then can show the 
 * transposed data of a resolution (actually, it can show any {@link List} of {@link Node}  
 * @author pit
 *
 */
public class TransposedAnalysisArtifactViewer implements SelectionListener, IDoubleClickListener, IMenuListener, IDisposable {
	private static final Object REPOSITORY_PC_NAME = "install"; // install repo (pc-repository) name

	private static Logger log = Logger.getLogger(TransposedAnalysisArtifactViewer.class);
	
	private ContentProvider contentProvider;
	private TreeViewer treeViewer;

	private Tree tree;
	private Function<TranspositionContext,List<Node>> nodeSupplier;
	private Function<CapabilityKeys, Boolean> capabilityActivationSupplier;
	private DetailRequestHandler detailRequestHandler;
	
	private NodeViewLabelProvider viewLabelProvider;
	
	private NodeFilter nodeFilter = new NodeFilter();
	private Worker worker = new Worker();
	private Executor executor = Executors.newCachedThreadPool();
	private LinkedBlockingQueue<Pair<TranspositionContext, String>> queue = new LinkedBlockingQueue<>();


	// capabilities, i.e. their UI representatives
	private Text scanField;
	
	private Button showCollapsedFuzzies;
	private Button showGroupIds;
	private Button showStructuralDependencies;
	private Button showArtifactNatures;
	
	
	private Button showParents;	
	private Button showDependers;
	private Button showDependencies;
	private Button showParts;
	
	private Text filterExpression;
	
	
	private String key;		
		
	private Image searchImage;
	
	private Shell shell;
	private Button coalesce;
	
	private Button openPomInView;
	private Button openPomInEditor;
	
	private MenuManager menuManager;

	private ImageDescriptor openPomInViewImageDescriptor;
	private ImageDescriptor openPomInEditorImageDescriptor;
	private ImageDescriptor openNodeInViewImageDescriptor;
	private ImageDescriptor copyDependencyToClipboardImageDescriptor;
	private ImageDescriptor removePcImageDescriptor;
	private Function<String, List<Node>> initialNodeSupplier;
	
	private boolean contextMenuOnly = false;

	private UiSupport uiSupport;
	private String uiSupportStylersKey = "standard";

	private Button showParentDependers;

	private Button showImports;

	private Button showImportDependers;
	
	private Button saveResolution;
	
	private Button purgeResolution;
		
	private Future<RepositoryReflection> repositoryReflectionFuture;
	private LazyInitialized<RepositoryReflection> repositoryReflection = new LazyInitialized<>(this::supplyRepositoryReflection);
	
	
	@Configurable @Required
	public void setRepositoryReflection(Future<RepositoryReflection> repositoryReflectionFuture) {
		this.repositoryReflectionFuture = repositoryReflectionFuture;
	}

	@Configurable @Required
	public void setUiSupport(UiSupport uiSupport) {
		this.uiSupport = uiSupport;
		buildImages();
	}
	
	@Configurable
	public void setUiSupportStylersKey(String uiSupportStylersKey) {
		this.uiSupportStylersKey = uiSupportStylersKey;
	}

	@Configurable @Required
	public void setNodeSupplier( Function<TranspositionContext, List<Node>> supplier) {
		this.nodeSupplier = supplier;
	}
	
	public void setInitialNodeSupplier(Function<String, List<Node>> initialNodeSupplier) {
		this.initialNodeSupplier = initialNodeSupplier;
		
	}
	@Configurable @Required
	public void setCapabilityActivationSupplier(Function<CapabilityKeys, Boolean> capabilityActivationSupplier) {
		this.capabilityActivationSupplier = capabilityActivationSupplier;
	}
	
	@Configurable
	public void setDetailRequestHandler(DetailRequestHandler detailRequestHandler) {
		this.detailRequestHandler = detailRequestHandler;
	}
					
	private void buildImages() {
		
		searchImage = uiSupport.images().addImage("search", TransposedAnalysisArtifactViewer.class, "insp_sbook.gif");				
				
		openNodeInViewImageDescriptor= ImageDescriptor.createFromFile(TransposedAnalysisArtifactViewer.class, "read_obj.transparent.png"); 		
		openPomInViewImageDescriptor = ImageDescriptor.createFromFile(TransposedAnalysisArtifactViewer.class, "cpyqual_menu.png");
		openPomInEditorImageDescriptor = ImageDescriptor.createFromFile(TransposedAnalysisArtifactViewer.class, "open_file.transparent.png");
		copyDependencyToClipboardImageDescriptor = ImageDescriptor.createFromFile(TransposedAnalysisArtifactViewer.class, "copyToClipboard.png");
		removePcImageDescriptor = ImageDescriptor.createFromFile(TransposedAnalysisArtifactViewer.class, "remove.gif");
		
		
	}
	
	public TransposedAnalysisArtifactViewer(Shell shell, String key) {
		this.shell = shell;
		this.key = key;
		
	}
	
	/**
	 * integrates the viewer into the {@link Composite} passed, returns its {@link Composite}
	 * @param parent - the {@link Composite} to used as parent (to integrate itself in)
	 * @param tag - the title of the viewer
	 * @return - the {@link Composite} created by the viewer, to be layouted by the caller
	 */
	public Composite createControl( Composite parent, String tag) {
		
		long before = System.nanoTime();					
		
		ViewerContext viewerContext = ResolutionViewerContextStorage.loadViewContextFromStorage( key);

		viewLabelProvider = new AnalysisViewLabelProvider();
		viewLabelProvider.setUiSupport(uiSupport);
		viewLabelProvider.setUiSupportStylersKey(uiSupportStylersKey);
		viewLabelProvider.setViewerContext( viewerContext);
		viewLabelProvider.setRequestHander(detailRequestHandler);
		
			
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
		
		Composite treeLabelComposite = new Composite( composite, SWT.NONE);
        treeLabelComposite.setLayout( layout);
        treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
        
        Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
        treeLabel.setText( tag);                       
	    treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
                        
        // basic field for scanning  
        if (capabilityActivationSupplier.apply( CapabilityKeys.filter)) {
        
	        //Composite filterComposite = new Composite(composite, SWT.NONE);
	        //filterComposite.setLayout( layout);
	        //filterComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
	        
	        //Label filterLabel = new Label( filterComposite, SWT.NONE);
	        Label filterLabel = new Label( composite, SWT.NONE);
	        filterLabel.setText("filter");
	        filterLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
	        
	        //filterExpression = new Text( filterComposite, SWT.NONE);
	        filterExpression = new Text( composite, SWT.NONE);
	        filterExpression.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
	        filterExpression.setToolTipText("Use camel-case expression to filter top-level elements");
	        filterExpression.addModifyListener( new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					TranspositionContext context = loadTranspositionContextFromSwitches();
					queue.offer( Pair.of(context, filterExpression.getText()));			
				}
			});
        
        }
        
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);              
		treeComposite.setLayout( new FillLayout());
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 3, 2));
		
		if (capabilityActivationSupplier.apply( CapabilityKeys.detail)) {
			treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			treeViewer.addDoubleClickListener(this);
		}
		else if (capabilityActivationSupplier.apply( CapabilityKeys.purge)){
			treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		}
		else {
			treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL);			
		}
		
		
		
		contentProvider = new ContentProvider();
		
		treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
    	    	    	
    	ColumnViewerToolTipSupport.enableFor(treeViewer);
    	
    	// columns 
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Artifact");
        nameColumn.getColumn().setToolTipText( "id of the artifact");
        nameColumn.getColumn().setWidth(100);
        
        nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( viewLabelProvider));
        nameColumn.getColumn().setResizable(true);                   
        columns.add(nameColumn);
    
		        
		ColumnViewerToolTipSupport.enableFor(treeViewer);
		
		tree = treeViewer.getTree();
	   
    	
		TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
    	columnResizer.setColumns( columns);		
    	columnResizer.setParent( treeComposite);
    	columnResizer.setTree( tree);    	
    	tree.addControlListener(columnResizer);
	
    
    	treeViewer.expandAll();    	
    	
    	TranspositionContext transpositionContext = ResolutionViewerContextStorage.loadTranspositionContextFromStorage( key);
    	boolean identifyProjects = capabilityActivationSupplier.apply(CapabilityKeys.identifyProjects);		
		if (identifyProjects) {
			transpositionContext.setDetectProjects(identifyProjects);
		}
    	    	   
		// defer to display thread 
		shell.getDisplay().asyncExec( () -> {    	 
			try {
				List<Node> initialNodes;
				if (initialNodeSupplier != null) {
					initialNodes = initialNodeSupplier.apply( key);				
				}
				else {
					initialNodes = nodeSupplier.apply(transpositionContext);
				}
				contentProvider.setNodes( initialNodes);
				treeViewer.setInput( initialNodes);
			} catch (Exception e) {
				//
				;
			}

		});		

	    
    	//
    	// Button composite 
    	//
    	Composite buttonComposite = new Composite( composite, SWT.BORDER);              
		buttonComposite.setLayout( layout);
		buttonComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, true, 1, 2));

		if (!contextMenuOnly && capabilityActivationSupplier.apply(CapabilityKeys.search)) {
			// search / highlight
			Composite searchComposite = new Composite( buttonComposite, SWT.NONE);
			searchComposite.setLayout( layout);
			searchComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
			
			Label searchLabels = new Label( searchComposite, SWT.NONE);
		    searchLabels.setText("search");
		    searchLabels.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		    searchLabels.setFont( uiSupport.stylers(uiSupportStylersKey).getInitialFont());
			
		    scanField = new Text( searchComposite, SWT.NONE);
		    scanField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));
		    
		    Button scanButton = new Button( searchComposite, SWT.NONE);
		    scanButton.setImage( searchImage);
		    scanButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		}				
		
		//
		// view modes
		//
		boolean capShowShortNotation = capabilityActivationSupplier.apply(CapabilityKeys.shortNotation);
		boolean capVisibleGroups = capabilityActivationSupplier.apply( CapabilityKeys.visibleGroups);
		boolean capVisibleDependencies = capabilityActivationSupplier.apply( CapabilityKeys.visibleDependencies);
		boolean capVisibleNatures = capabilityActivationSupplier.apply( CapabilityKeys.visibleArtifactNature);
		if (!contextMenuOnly && (capShowShortNotation || capVisibleGroups || capVisibleNatures)) {
			Composite viewModes = new Composite( buttonComposite, SWT.NONE);
			viewModes.setLayout( layout);
			viewModes.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
			
			Label viewModeLabels = new Label( viewModes, SWT.NONE);
		    viewModeLabels.setText("view modes");
		    viewModeLabels.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		    viewModeLabels.setFont(  uiSupport.stylers(uiSupportStylersKey).getInitialFont());
		    
		    if (capShowShortNotation) {
				// short notation on ranges
				showCollapsedFuzzies = new Button( viewModes, SWT.CHECK);
				showCollapsedFuzzies.setText("short ranges");
				showCollapsedFuzzies.setToolTipText("display version ranges in javascript-style short notation");
				showCollapsedFuzzies.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showCollapsedFuzzies.addSelectionListener( this);
		    }		    
		    if (capVisibleGroups) {
				// show group ids
				showGroupIds = new Button( viewModes, SWT.CHECK);
				showGroupIds.setText("show groupIds");
				showGroupIds.setToolTipText("display groupIds of artifacts and dependencies");
				showGroupIds.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showGroupIds.addSelectionListener(this);
		    }
		    
		    if (capVisibleDependencies) {
		    	showStructuralDependencies = new Button( viewModes, SWT.CHECK);
		    	showStructuralDependencies.setText("dependency details");
		    	showStructuralDependencies.setToolTipText( "display dependencies as structural element or only aritfacts");
		    	showStructuralDependencies.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		    	showStructuralDependencies.addSelectionListener(this);
		    }
		    if (capVisibleNatures) {
		    	showArtifactNatures = new Button( viewModes, SWT.CHECK);
		    	showArtifactNatures.setText("show natures");
		    	showArtifactNatures.setToolTipText( "displays the nature of artifacts (jar/project/parent/import)");
		    	showArtifactNatures.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		    	showArtifactNatures.addSelectionListener(this);
		    }
		    
		    reflectViewContextInSwitches(viewerContext);
		}
		
		//
		// structural modes
		//
		boolean capShowParents = capabilityActivationSupplier.apply(CapabilityKeys.parents);		
		boolean capShowParentDependers = capabilityActivationSupplier.apply(CapabilityKeys.parentDependers);
		boolean capShowImports = capabilityActivationSupplier.apply(CapabilityKeys.imports);		
		boolean capShowImportDependers = capabilityActivationSupplier.apply(CapabilityKeys.importDependers);
		
		boolean capShowDependencies = capabilityActivationSupplier.apply( CapabilityKeys.dependencies);
		boolean capShowDependers = capabilityActivationSupplier.apply( CapabilityKeys.dependers);
		boolean capShowParts = capabilityActivationSupplier.apply( CapabilityKeys.parts);
		boolean capCoalesce = capabilityActivationSupplier.apply( CapabilityKeys.coalesce);
		
		if (
				!contextMenuOnly && 
				(
					capShowParents ||
					capShowParentDependers ||
					capShowImports ||
					capShowImportDependers ||
					capShowParts || 
					capShowDependencies || 
					capShowDependers || 
					capCoalesce
				)
			) { 
			Composite structualModes = new Composite( buttonComposite, SWT.NONE);
			structualModes.setLayout( layout);
			structualModes.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 2));
			
			Label structuralModelLabel = new Label( structualModes, SWT.NONE);
		    structuralModelLabel.setText("structural modes");
		    structuralModelLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		    structuralModelLabel.setFont( uiSupport.stylers(uiSupportStylersKey).getInitialFont());
		    		    
		    
			if (capShowDependencies) {
				showDependencies = new Button( structualModes, SWT.CHECK);
				showDependencies.setText("show dependencies");
				showDependencies.setToolTipText( "display dependencies of artifacts as tree elements (top to bottom)");
				showDependencies.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showDependencies.addSelectionListener(this);
			}
			
			if (capShowDependers) {
				showDependers = new Button( structualModes, SWT.CHECK);
				showDependers.setText("show dependers");
				showDependers.setToolTipText( "display dependers of artifacts and dependencies (bottom up)");
				showDependers.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showDependers.addSelectionListener( this);
			}
			
			if (capShowParents) {
				showParents = new Button( structualModes, SWT.CHECK);
				showParents.setText("show parents");
				showParents.setToolTipText( "display parent usage of artifacts");
				showParents.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showParents.addSelectionListener(this);
			}
			
			if (capShowParentDependers) {
				showParentDependers = new Button( structualModes, SWT.CHECK);
				showParentDependers.setText("show parent dependers");
				showParentDependers.setToolTipText( "display dependers of parents");
				showParentDependers.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showParentDependers.addSelectionListener(this);
			}
			if (capShowImports) {
				showImports = new Button( structualModes, SWT.CHECK);
				showImports.setText("show imports");
				showImports.setToolTipText( "display import usage of parent artifacts");
				showImports.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showImports.addSelectionListener(this);
			}
			
			if (capShowImportDependers) {
				showImportDependers = new Button( structualModes, SWT.CHECK);
				showImportDependers.setText("show import dependers");
				showImportDependers.setToolTipText( "display dependers of import");
				showImportDependers.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showImportDependers.addSelectionListener(this);
			}
			
			
			if (capShowParts) {
				showParts = new Button( structualModes, SWT.CHECK);
				showParts.setText("show parts");
				showParts.setToolTipText( "show parts of an artifact");
				showParts.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				showParts.addSelectionListener( this);
			}
			
			if (capCoalesce) {
				coalesce = new Button( structualModes, SWT.CHECK);
				coalesce.setText("coalesce dependencies");
				coalesce.setToolTipText( "show only unique dependencies, coalesce duplicates");
				coalesce.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				coalesce.addSelectionListener( this);
			}
		
			reflectTranspositionContextInSwitches(transpositionContext);
		}
		
		// store feature
		/*
		boolean capShowSaveButton = capabilityActivationSupplier.apply( CapabilityKeys.saveResolution);
		if (capShowSaveButton) {
			Composite saveResolutionComposite = new Composite( buttonComposite, SWT.NONE);
			saveResolutionComposite.setLayout( layout);
			saveResolutionComposite.setLayoutData( new GridData( SWT.FILL, SWT.BOTTOM, true, true, 4, 2));
			
			saveResolution = new Button( saveResolutionComposite, SWT.None);
			saveResolution.setText("save displayed resolution");
			saveResolution.setToolTipText( "saves the currently displayed resolution to a specified YAML file");
			saveResolution.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false, 4, 1));
			saveResolution.addSelectionListener(this);
		}
		*/
		// publishing candidate purge		
		boolean capPurgeButton = capabilityActivationSupplier.apply( CapabilityKeys.purge);
		if (capPurgeButton) {
			Composite purgeComposite = new Composite( buttonComposite, SWT.NONE);
			purgeComposite.setLayout( layout);
			purgeComposite.setLayoutData( new GridData( SWT.FILL, SWT.BOTTOM, true, false, 4, 1));
			
			purgeResolution = new Button( purgeComposite, SWT.None);
			purgeResolution.setText("purge pc-candidates");
			purgeResolution.setToolTipText( "removes all pc-candidates from a configured 'install' repository");
			purgeResolution.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
			purgeResolution.addSelectionListener(this);
		}

		
		//
		// context menu
		//
		
		// a) menu manager
		menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown( true);
		menuManager.addMenuListener( this);
		
		// b) attach
		Control control = treeViewer.getControl();
		Menu menu = menuManager.createContextMenu(control);
		control.setMenu( menu);
    	
		// start worker
		worker.start();
		
		long after = System.nanoTime();
		
		log.trace("viewer [" + key + "]'s createControl took [" + ((after - before) / 1E6) + "] ms");
		
		return composite;
	}
	
	private enum ActionType {NODE,PART, UNKNOWN};
	
	@Override
	public void menuAboutToShow(IMenuManager manager) {
			
		Node node = getSelectedNode( treeViewer.getSelection());
		if (node == null) {
			return;
		}
		ActionType nodeCase = ActionType.UNKNOWN;
		if (node instanceof AnalysisNode) {
			nodeCase = ActionType.NODE;
		}
		else if (node instanceof DependerNode) {
			nodeCase = ActionType.NODE;	
		}
		else if (node instanceof PartNode) {
			nodeCase = ActionType.PART;	
		}
		switch (nodeCase) {
			case NODE:
				// opening nodes 
				if (capabilityActivationSupplier.apply( CapabilityKeys.open)) {
								
					// detail view
					Action openNodeInView = new Action("open node in view", openNodeInViewImageDescriptor) {
						@Override
						public void run() {
							detailRequestHandler.acknowledgeOpenDetailRequest(node);
						}					
						@Override
						public String getToolTipText() {
							return "Opens the selected node in a new tab";
						}		
					}; 
					menuManager.add(openNodeInView);
					
					// pom in view
					Action openPomInView = new Action("open pom in view", openPomInViewImageDescriptor) {
						@Override
						public void run() {
							detailRequestHandler.acknowledgeViewPomRequest( node);
						}
	
						@Override
						public String getToolTipText() {
							return "Opens the associated pom as a new tab (if any)";
						}			
						
					}; 
					menuManager.add(openPomInView);
					
					// pom in editor
					Action openPomInEditor = new Action("open pom in editor", openPomInEditorImageDescriptor) {
						@Override
						public void run() {					
							detailRequestHandler.acknowledgeOpenPomRequest( node);
						}					
						@Override
						public String getToolTipText() {
							return "Opens the associated pom in a new editor pane";
						}	
					};
					menuManager.add(openPomInEditor);
				}
				
				// AnalysisNode only
				if (node instanceof AnalysisNode) {					
					AnalysisNode analysisNode = (AnalysisNode) node;
					
					// copy dependency -  
					if (capabilityActivationSupplier.apply( CapabilityKeys.copy)) {
						Action copyDependencyToClipboard = new Action("copy dependency to clipboard", copyDependencyToClipboardImageDescriptor) {
							@Override
							public void run() {					
								detailRequestHandler.acknowledgeCopyDependencyToClipboardRequest( analysisNode);
							}
							@Override
							public String getToolTipText() {
								return "Copies the backing dependency to the clipboard (if any)";
							}
						};
						
						menuManager.add(copyDependencyToClipboard);
					}
				
					// remove - if artifact comes from the install repository 
					// 
					List<Node> nodes = getSelectedNodes( treeViewer.getSelection());
					List<AnalysisNode> purgeableNodes = nodes.stream().filter( n -> n instanceof AnalysisNode).map( n -> (AnalysisNode) n).filter( n -> eligibleForPurge(n, repositoryReflection.get())).collect( Collectors.toList());
					
					if (purgeableNodes.size() > 0) {
						Action removeFromInstall = new Action( "purge : " +  purgeableNodes.size() + " locally installed artifacts", removePcImageDescriptor) {
							@Override
							public void run() {					
								detailRequestHandler.acknowledgeRemovalFromPcRepositoryRequest( purgeableNodes);							
								treeViewer.refresh();
							}
							@Override
							public String getToolTipText() {
								String names = purgeableNodes.stream().map( an -> an.getBackingSolution().asString()).collect( Collectors.joining(",\n"));
								return "removes the selected pc-artifacts (" + names + ") from the repository " + REPOSITORY_PC_NAME + " which holds these artifacts";
							}
						};
						menuManager.add(removeFromInstall);
					}
					
					// remove all pc-candidates 
					Action removeAllFromInstall = new Action( "purge all pc-artifacts from " + REPOSITORY_PC_NAME, removePcImageDescriptor) {
						@Override
						public void run() {					
							List<Node> currentNodes = contentProvider.getCurrentNodes();
							List<AnalysisNode> analysisNodes = currentNodes.stream().filter( n -> n instanceof AnalysisNode).map( n -> (AnalysisNode) n).collect(Collectors.toList());
							detailRequestHandler.acknowledgeRemovalFromPcRepositoryRequest( analysisNodes);
							treeViewer.refresh();
						}
						@Override
						public String getToolTipText() {
							return "identifies and removes all pc-artifacts from the repository " + REPOSITORY_PC_NAME + " which holds these artifacts";
						}
					};
					
					menuManager.add(removeAllFromInstall);
				}
				break;
				
				
			case PART:
				// part node 
				break;
			default:
				break;
		}
		
	}
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		if (
				event.widget == showCollapsedFuzzies || 
				event.widget == showGroupIds || 
				event.widget == showStructuralDependencies ||
				event.widget == showArtifactNatures
			) {			
			ViewerContext viewerContext = loadViewContextFromSwitches();
			viewLabelProvider.setViewerContext(viewerContext);
			treeViewer.refresh();
			// store current view context
			ResolutionViewerContextStorage.storeViewContextInStorage(key, viewerContext);
		}
		else if (
				event.widget == showDependencies || 
				event.widget == showDependers || 
				event.widget == showParents ||  
				event.widget == showParentDependers ||
				event.widget == showImports ||  
				event.widget == showImportDependers ||
				event.widget == showParts ||
				
				event.widget == coalesce
			) {			
			TranspositionContext context = loadTranspositionContextFromSwitches();
			if (capabilityActivationSupplier.apply( CapabilityKeys.identifyProjects)) {
				context.setDetectProjects( true);				
			}
			else {
				context.setDetectProjects( false);
			}
			// apply current filter.. 
			 if (capabilityActivationSupplier.apply( CapabilityKeys.filter)) {
				String expression = filterExpression.getText();			
				queue.offer( Pair.of(context, expression));			
			 }
			 else {
				processQueryAndUpdateContentProvider(context); 
			 }
			
			ResolutionViewerContextStorage.storeTranspositionContextToStorage( key, context);
		}	
		else if (openPomInEditor != null && openPomInView != null) {
			if (event.widget == openPomInEditor) {
				ISelection selection = treeViewer.getSelection();
				Node node = getSelectedNode(selection);
				detailRequestHandler.acknowledgeOpenPomRequest( node);
			}
			else if (event.widget == openPomInView) {
				ISelection selection = treeViewer.getSelection();
				Node node = getSelectedNode(selection);
				detailRequestHandler.acknowledgeViewPomRequest( node);	
			}
		}
		else if (event.widget == saveResolution) {
			detailRequestHandler.acknowledgeResolutionDumpRequest();
		}
		else if (event.widget == purgeResolution) {
			List<Node> currentNodes = contentProvider.getCurrentNodes();
			List<AnalysisNode> analysisNodes = currentNodes.stream().filter( n -> n instanceof AnalysisNode).map( n -> (AnalysisNode) n).collect(Collectors.toList());
			detailRequestHandler.acknowledgeRemovalFromPcRepositoryRequest( analysisNodes);
			treeViewer.refresh();
		}
	}
	
	private Node getSelectedNode(ISelection selection) {	
        if(!selection.isEmpty()){
        	if (selection instanceof IStructuredSelection) {
                Object item = ((IStructuredSelection) selection).getFirstElement();
                if (item instanceof Node) {
                	Node selectedNode = (Node) item;
                	return selectedNode;
                }
        	}        		
        }
        return null;    	
	}
	
	private List<Node> getSelectedNodes(ISelection selection) {		
        if(!selection.isEmpty()){
        	if (selection instanceof IStructuredSelection) {
        		IStructuredSelection iSelection = (IStructuredSelection) selection;
        		List<Node> result = new ArrayList<Node>(iSelection.size());
        		Iterator<?> iter = iSelection.iterator();
        		while (iter.hasNext()) {
        			Object item = iter.next();
        			if (item instanceof Node) {
        				Node selectedNode = (Node) item;
        				result.add(selectedNode);
        			}        			
        		}
                return result;
        	}                	
        }
        return Collections.emptyList();    	
	}

	
	@Override
	public void doubleClick(DoubleClickEvent event) {
        ISelection selection = event.getSelection();
        Node node = getSelectedNode(selection);
        if (node != null) {
        	if (detailRequestHandler != null) {
        		detailRequestHandler.acknowledgeOpenDetailRequest( node);
        	}
        }                       	
    }
	
	
	/**
	 * @param context - the {@link TranspositionContext} to show in the dialog's switches
	 */
	private void reflectTranspositionContextInSwitches(TranspositionContext context) {
		if (showDependencies != null) {
			showDependencies.setSelection( context.getShowDependencies());
		}
		if (showDependers != null) {
			showDependers.setSelection( context.getShowDependers());		
		}
		if (showParents != null) {
			showParents.setSelection( context.getShowParents());
		}		
		if (showParentDependers != null) {
			showParentDependers.setSelection( context.getShowParentDependers());
		}		
		if (showImports != null) {
			showImports.setSelection( context.getShowImports());
		}		
		if (showImportDependers != null) {
			showImportDependers.setSelection( context.getShowImportDependers());
		}		
		
		
		if (showParts != null) {
			showParts.setSelection( context.getShowParts());
		}
		if (coalesce != null) {
			coalesce.setSelection( context.getCoalesce());
		}
	}

	/**
	 * @return - the {@link TranspositionContext} as defined by the dialog's switches
	 */
	private TranspositionContext loadTranspositionContextFromSwitches() {		
		TranspositionContextBuilder builder = BasicTranspositionContext.build();
		
		boolean identifyProjects = capabilityActivationSupplier.apply(CapabilityKeys.identifyProjects);
		
		if (identifyProjects) {
			builder.detectProjects(true);
		}
		
		if (showDependencies != null) {
			builder.includeDependencies( showDependencies.getSelection());
		}
		if (showDependers != null) {
			builder.includeDependers( showDependers.getSelection());
		}
		if (showParents != null) {
			builder.includeParents( showParents.getSelection());
		}
		if (showParentDependers != null) {
			builder.includeParentDependers( showParentDependers.getSelection());
		}
		if (showImports != null) {
			builder.includeImports( showImports.getSelection());
		}
		if (showImportDependers != null) {
			builder.includeImportDependers( showImportDependers.getSelection());
		}
		
		if (showParts != null) {
			builder.includeParts( showParts.getSelection());
		}
		if (coalesce != null) {
			builder.coalesceFilteredDependencies( coalesce.getSelection());
		}
		return builder.done();
	}
	
	/**
	 * @param vc - the {@link ViewContext} to reflect in the current switches
	 */
	private void reflectViewContextInSwitches( ViewerContext vc) {
		if (showCollapsedFuzzies != null) {
			showCollapsedFuzzies.setSelection( vc.getShowShortNotation());
		}
		if (showGroupIds != null) {
			showGroupIds.setSelection(vc.getShowGroupIds());
		}
		if (showStructuralDependencies != null) {
			showStructuralDependencies.setSelection(vc.getShowDependencies());
		}
		
		if (showArtifactNatures != null) {
			showArtifactNatures.setSelection( vc.getShowNature());
		}
	}
	
	/**
	 * @return - a {@link ViewContext} reflecting the switches in the side bar
	 */
	private ViewerContext loadViewContextFromSwitches() {
		ViewContextBuilder builder = BasicViewContext.build();
		if (showCollapsedFuzzies != null)
				builder.shortRanges( showCollapsedFuzzies.getSelection());
		if (showGroupIds != null) {
				builder.showGroups( showGroupIds.getSelection());
		}
		if (showStructuralDependencies != null) {
			builder.showDependencies( showStructuralDependencies.getSelection());
		}
		if (showArtifactNatures != null) {
			builder.showNatures( showArtifactNatures.getSelection());
		}
	
		return builder.done();		
	}
	
	
	

	/**
	 * cleanup any resources
	 */
	@Override
	public void dispose() {
		// shutdown 
		try {
			worker.interrupt();			
			worker.join();			
		} catch (InterruptedException e) {
			String msg = "Exception on worker join";
			DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
			DevrockPlugin.instance().log(status);	
		}
				
		// dispose label provider
		viewLabelProvider.dispose();		 		
	}
	
	// 
	// thread that processes the input
	//
	private class Caller implements Runnable {
		private String expression;
		private TranspositionContext context;
		
		public Caller(TranspositionContext context, String expression){
			this.context = context;
			this.expression = expression;
		}		
		@Override
		public void run() {
			processQueryAndUpdateContentProvider(context, expression);			
		}	
	}
		
	//
	// thread that handles queue.. 
	//
	private class Worker extends Thread {		
		@Override
		public void run() {		
			for (;;) {
				try {
					// grab invocation from queue
					// test of more than one entry is in the queue 
					int len = queue.size(); 
					//System.out.println("Queue length: " + len);
					if ( len > 1) {
						// if so, drain the queue and leave only one in the queue. 
						List<Pair<TranspositionContext, String>> expressions = new ArrayList<>();
						queue.drainTo( expressions, len - 1);						
					}
					Pair<TranspositionContext, String> pair = queue.take();					
					// process  					
					Caller caller = new Caller( pair.first, pair.second);
					executor.execute(caller);										
				} catch (InterruptedException e) {
					// shutdown requested, expected situation
					return;
				}
			}
		}		
	}

	/**
	 * run the filter query and update the treeviewer in the display thread
	 * @param context - the {@link TranspositionContext}
	 * @param expression - the user's expression
	 */
	public void processQueryAndUpdateContentProvider(TranspositionContext context, String expression) {
		// run
		List<Node> filterNodes;
		List<Node> nodes = nodeSupplier.apply( context);
		if (expression == null || expression.length() == 0) {
			filterNodes = nodes;
		}
		else {		
			if (Character.isUpperCase(expression.charAt(0)) || // 
					expression.contains("?") || //
					expression.contains( "*")
				) {
				filterNodes = nodeFilter.filterNodes(expression, nodes);
			}
			else {
				filterNodes = nodeFilter.filterNodesViaContains(expression, nodes);	
			}			
		}
		shell.getDisplay().asyncExec( new Runnable() {	
			@Override
			public void run() {
				contentProvider.setNodes(filterNodes);
				treeViewer.setInput(filterNodes);
				treeViewer.refresh();
			}
		});							
	}
	
	public void processQueryAndUpdateContentProvider(TranspositionContext context) {
		// run	
		List<Node> nodes = nodeSupplier.apply( context);
		shell.getDisplay().asyncExec( new Runnable() {	
			@Override
			public void run() {
				contentProvider.setNodes(nodes);
				treeViewer.setInput(nodes);
				treeViewer.refresh();
			}
		});
	}

	
	
	private boolean eligibleForPurge( AnalysisNode node, RepositoryReflection repositoryReflection) {
		AnalysisArtifact artifact = node.getBackingSolution();
		if (detailRequestHandler.acknowledgeObsoleteCheckRequest(artifact)) {
			return false;
		}
		
		// check whether that's something to delete  
		Version version = Version.parse( artifact.getVersion());
		if (!version.isPreliminary()) {
			return false;
		}
				
		
		String origin = node.getRelevantResourceOrigin();
		Repository repository = repositoryReflection.getRepository(origin);
		if (repository == null || repository instanceof MavenFileSystemRepository == false) {
			return false;
		}			
		return true;
	}

	private RepositoryReflection supplyRepositoryReflection() {
		if (repositoryReflectionFuture != null) {
			RepositoryReflection ref = null;
			try {
				ref = repositoryReflectionFuture.get();
				if (ref != null) {
					return ref;
				}
			} catch (InterruptedException | ExecutionException e) {
				String msg = "Exception while trying access future of repository-reflection";
				DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
				DevrockPlugin.instance().log(status);				
			}
		}
		String msg = "No future defined for repository-reflection, accessing";
		DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.WARNING);
		DevrockPlugin.instance().log(status);
		
		Maybe<RepositoryReflection> maybe = DevrockPlugin.instance().reflectRepositoryConfiguration();
		if (!maybe.isSatisfied()) {
			msg = "Failed to retrieve repository-reflection:" + maybe.whyUnsatisfied().stringify();
			status = new DevrockPluginStatus( msg, IStatus.WARNING);
			DevrockPlugin.instance().log(status);
			return null;
		}
		RepositoryReflection repositoryReflection = maybe.get();
		return repositoryReflection;
	}
	
}
