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
package com.braintribe.devrock.artifactcontainer.quickImport.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporter;
import com.braintribe.devrock.artifactcontainer.control.project.ProjectImporterTuple;
import com.braintribe.devrock.artifactcontainer.quickImport.QuickImportControl;
import com.braintribe.devrock.artifactcontainer.quickImport.notification.QuickImportScanResultListener;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessingException;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportAction;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.VersionModificationAction;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.panther.SourceArtifact;
import com.braintribe.plugin.commons.commands.ArtifactToClipboardExpert;
import com.braintribe.plugin.commons.selection.PantherSelectionHelper;
import com.braintribe.plugin.commons.selection.SelectionExtractor;
import com.braintribe.plugin.commons.selection.TargetProvider;
import com.braintribe.plugin.commons.ui.tree.TreeColumnResizer;
import com.braintribe.plugin.commons.ui.tree.TreeExpander;
import com.braintribe.plugin.commons.ui.tree.TreeItemTooltipProvider;

/**
 * a dialog that lets you import projects...  
 * 
 * @author pit
 *
 */
public class QuickImportDialog extends Dialog implements QuickImportScanResultListener, HasQuickImportTokens, SelectionListener {
	
	
	private static final int TREE_HEIGHT = 20;
	private static final int TREE_WIDTH = 40;
	private static final int COMPOSITE_HEIGHT = 25;
	private static final int COMPOSITE_WIDTH = 80;
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private Shell shell;
	private Cursor waitCursor;
	private Text editBox;
	private Label status;
	private Button modeButton;
	private Button scanButton;
	private Tree tree;
	private TreeExpander expander = new TreeExpander();
	
	private List<TreeColumn> columns = new ArrayList<TreeColumn>();
	private String [] columnNames = { "Artifact", "Version",};
	private int [] columnWeights = { 500, 100,};

	private enum Direction {up, down}
	
	private DisplayMode currentMode = DisplayMode.condensed;
	
	private Worker worker = new Worker();
	private Executor executor = Executors.newCachedThreadPool();
	private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

	private boolean alternativeUiStyle = true;
			
	private List<SourceArtifact> currentResult;
	
	private TreeRegistry registry;
		
	private int currentlySelectedIndex = 0;
	private String initialQuery;
	private TargetProvider targetProvider;
	
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	private QuickImportPreferences qiPreferences = plugin.getArtifactContainerPreferences(false).getQuickImportPreferences();
	private QuickImportControl qiController = plugin.getQuickImportScanController();
		
	private Button addToSelectedPom;
	
	private QuickImportAction importAction = QuickImportAction.importProject;

	private Font bigFont;
	
	private Artifact selection;
	private int lastSelectedIndex;
	private Button directMode;
	private Button rangeMode;
	private Button variableMode;
	
	private boolean preprocess;

	
	public Artifact getSelection() {
		return selection;
	}
	@Configurable
	public void setSelection(Artifact selection) {
		this.selection = selection;	
		setInitialQuery( selection.getGroupId() + ":" + selection.getArtifactId() + "#" + VersionProcessor.toString(selection.getVersion()));
	}

	@Configurable
	public void setTargetProvider(TargetProvider targetProvider) {
		this.targetProvider = targetProvider;
	}
	
	@Configurable
	public void setInitialQuery(String initialQuery) {
		this.initialQuery = initialQuery;
	}	
	
	public void setImportAction(QuickImportAction importAction) {
		this.importAction = importAction;
	}
	
	public QuickImportDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle( SHELL_STYLE);
		shell = parentShell;
	}
	
	public QuickImportDialog( Shell parentShell, String initialQuery) {
		super( parentShell);
		//setShellStyle(SHELL_STYLE);
		this.initialQuery = initialQuery;
		shell = parentShell;
	}
	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		Composite composite = (Composite) super.createDialogArea(parent);
		
		
		
		alternativeUiStyle = qiPreferences.getAlternativeUiNature();
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=2;        
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        PixelConverter pc = new PixelConverter(getParentShell());		
        composite.setSize( pc.convertWidthInCharsToPixels(COMPOSITE_WIDTH), pc.convertHeightInCharsToPixels(COMPOSITE_HEIGHT));
        
   
        // edit box for typing
        Composite labelComposite = new Composite( composite, SWT.NONE);
        labelComposite.setLayout( layout);
        labelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
            
        Label label = new Label( labelComposite, SWT.NONE);
        label.setText( "Enter artifact name (<group>:<artifact>#<version>, or patterns (?*) and/or camel case:");
        label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        Composite textComposite = new Composite( composite, SWT.NONE);
        textComposite.setLayout( layout);
        textComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        editBox = new Text( textComposite, SWT.BORDER);
        editBox.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 3, 1));
        if (initialQuery != null) {
        	editBox.setText(initialQuery);
        }
        else {
        	String primed = determineInitialQueryFromPackageExplorer();
        	if (primed != null) {
        		editBox.setText(primed);
        		initialQuery = primed;
        	}        	
        }
        
        
        // modify listener -> trigger query 
        editBox.addModifyListener( new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				queue.offer( editBox.getText());			
			}
		});
               
        // traverse listener -> enter key
        editBox.addTraverseListener( new TraverseListener() { 
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				processTraverseEvent(e);
			}
		});
              
        if (qiPreferences.getLocalOnlyNature()) {
        	scanButton = new Button( textComposite, SWT.NONE);
        	scanButton.setText( "Refresh");
        	scanButton.setLayoutData(new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        	if (qiController.isScanActive()) {
        		scanButton.setEnabled( false);
        	} else {
        		scanButton.setEnabled( true);
        	}
        	
        	scanButton.addSelectionListener( new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					// run rescan ..
					scanButton.setEnabled( false);					
					qiController.rescan();										 								
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {										
				}
			});
        }
        
        
        
        Composite treeLabelComposite = new Composite( composite, SWT.NONE);
        treeLabelComposite.setLayout( layout);
        treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
        
        Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
        treeLabel.setText( "Matching items:");
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);
		treeComposite.setLayout( new FillLayout());
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
		
		tree = new Tree ( treeComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible( true);
		
		// add listener to check for the double click event 
		tree.addMouseListener( new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {			
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TreeItem item = tree.getItem( new Point( e.x, e.y));
				if (
						item != null &&
						( 	
								Boolean.TRUE.equals(item.getData(MARKER_AVAILABLE)) || 
								importAction != QuickImportAction.importProject  // can only be selected in import project mode if available
						)  					
					) {
					tree.setSelection(item);				
					okPressed();
					return;						
				} 													
			}
		});

		// add listener to override selections (only allow valid items that can be imported)
		tree.addSelectionListener( this);
			
		TreeItemTooltipProvider.attach(tree, MARKER_TOOLTIP);
		
		
		// add listener to process key strokes 
		tree.addTraverseListener( new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
					processTraverseEvent(e);
			}
		});

		// dependency insertion mode radios
		if (importAction == QuickImportAction.importDependency) {
			Composite versionModeComposite = new Composite( composite, SWT.NONE);
			versionModeComposite.setLayout( layout);
			versionModeComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false));
			
			Label versionModeLabel = new Label( versionModeComposite, SWT.NONE);
			versionModeLabel.setFont(bigFont);
			versionModeLabel.setText( "version modification");
			versionModeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
			
			VersionModificationAction versionModificationAction = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences().getLastDependencyPasteMode();
			
			
			directMode = new Button(versionModeComposite, SWT.RADIO);
			directMode.setText("unchanged");
			directMode.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));			
			directMode.setToolTipText( "Keep version of the dependency as in the classpath");
						
			rangeMode = new Button(versionModeComposite, SWT.RADIO);
			rangeMode.setText("rangified");
			rangeMode.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			rangeMode.setToolTipText( "Modify the version of the dependency to a hotfix supporting range");
			
			variableMode = new Button(versionModeComposite, SWT.RADIO);
			variableMode.setText("variable");
			variableMode.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			variableMode.setToolTipText( "Deduce a variable from the groupId of the dependency and replace the version with a reference");
			
			if (versionModificationAction == VersionModificationAction.rangified) {
				rangeMode.setSelection(true);
			}
			else if (versionModificationAction == VersionModificationAction.referenced) {
				variableMode.setSelection(true);
			}
			else {			 
				directMode.setSelection(true);
			}
								
		}
    
		
		Composite statusComposite = new Composite( composite, SWT.NONE);
		statusComposite.setLayout( layout);
		statusComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false));
		
		status = new Label( statusComposite, SWT.NONE);	
		status.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1));
		        
	    
	    Button preprocessButton = new Button( statusComposite, SWT.CHECK);
	    preprocessButton.setText( "Preprocess");
	    preprocessButton.setToolTipText("transitively resolves the project first before importing into Eclipse.\nIntended for initial downloads into an empty repository.\nWhile Eclipse stays reactive during the process, the resolver doesn't see projects in the workspace.");
	    preprocessButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
	    
		preprocessButton.addSelectionListener( new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (preprocessButton.getSelection()) {
					preprocess = true;
				} else {
					preprocess = false;
				}			
			}			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
		});
	    
	    
	    // condensed button 
	    modeButton = new Button(statusComposite, SWT.CHECK);
		modeButton.setText( "Condensed");
		modeButton.setToolTipText("switches the view into condensed mode");
		modeButton.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
	    
		modeButton.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (modeButton.getSelection()) {
					currentMode = DisplayMode.condensed;
				} else {
					currentMode = DisplayMode.explicit;
				}
				// 
				queue.offer( editBox.getText());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
		});
		
		modeButton.addTraverseListener( new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				processTraverseEvent(e);				
			}
		});
		
				
		
		for (int i = 0; i < columnNames.length; i++) {
			TreeColumn treeColumn = new TreeColumn( tree, SWT.LEFT);
	
			treeColumn.setText( columnNames[i]);
			treeColumn.setWidth( columnWeights[i]);
			treeColumn.setResizable( true);
			//treeColumn.addSelectionListener(treeSortListener);
			columns.add( treeColumn);
		}
						
		TreeColumnResizer columnResizer = new TreeColumnResizer();
		columnResizer.setColumns( columns);
		columnResizer.setColumnWeights( columnWeights);
		columnResizer.setParent( treeComposite);
		columnResizer.setTree( tree);
		
		tree.addControlListener(columnResizer);
		
		tree.setSize( pc.convertWidthInCharsToPixels(TREE_WIDTH), pc.convertHeightInCharsToPixels(TREE_HEIGHT));
		
		
	    
        composite.pack();	
        editBox.setFocus();       
        
        shell.layout(true); 
        
        // 
        String expression = editBox.getText();
        if (expression != null && expression.length() > 0) {
	        shell.getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					queue.offer( expression);								
				}
			});	
        }
        
        return composite;
	}
	
	
	
	private void updateOkButton() {
		if (tree.getSelectionCount() > 0) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);			
		} else {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}
	
	/**
	 * process key strokes 
	 */
	private void processTraverseEvent( TraverseEvent e) {
	
		// ENTER key 
		if (
				e.keyCode == SWT.KEYPAD_CR ||
				e.keyCode == SWT.CR
			) {
			// 
			if (
					!alternativeUiStyle &&
					currentMode == DisplayMode.condensed
				) {							
				if (e.stateMask == 0) {
					e.doit = false;
					e.detail = SWT.TRAVERSE_NONE;
					// current mode 
					if (currentMode == DisplayMode.condensed) {
						currentMode = DisplayMode.explicit;
					} else {
						currentMode = DisplayMode.condensed;
					}
					updateTreeWithSelectedArtifacts();
				}
			}
			return;
		}
		
		// cursor down 
		if (e.keyCode == SWT.ARROW_DOWN) {
			e.doit = true;
			e.detail = SWT.TRAVERSE_NONE;
			selectArtifact( Direction.down);
			return;
		}
		// cursor up 
		if (e.keyCode == SWT.ARROW_UP) {
			e.doit = true;
			e.detail = SWT.TRAVERSE_NONE;
			selectArtifact( Direction.up);
			return;
		}
	
		// tabulator
		if (e.keyCode == SWT.TAB) {
			if (alternativeUiStyle) {
				e.doit = false;
				e.detail = SWT.TRAVERSE_NONE;
				
				// current mode 
				if (currentMode == DisplayMode.condensed) {
					currentMode = DisplayMode.explicit;
				} else {
					currentMode = DisplayMode.condensed;
				}
				updateTreeWithSelectedArtifacts();
			}
			return;
		}							
	}
	
	private VersionModificationAction determineDependencyInsertionMode() {	
		if (variableMode != null && variableMode.getSelection()) {
			return VersionModificationAction.referenced;
		}
		else if (rangeMode != null && rangeMode.getSelection()) {
			return VersionModificationAction.rangified;
		}
		return VersionModificationAction.untouched;
	}

	@Override
	protected void okPressed() {		
		List<ProjectImporterTuple> tuples = extractSelectedArtifacts();
		List<Artifact> artifactsToCopy = new ArrayList<Artifact>( tuples.size());
		tuples.stream().map( t -> t.getArtifact()).collect( Collectors.toCollection(() -> artifactsToCopy));
		IProject project = targetProvider != null ? targetProvider.getTargetProject() : null;
		VersionModificationAction mode = determineDependencyInsertionMode();

		switch (importAction) {
			case importDependency:
				if (artifactsToCopy.size() > 0) {
					ArtifactToClipboardExpert.copyToClipboard(mode, artifactsToCopy.toArray( new Artifact[0]));
				}					
				if (project != null) {					
					ArtifactToClipboardExpert.injectDependenciesIntoProject(project, mode, artifactsToCopy.toArray( new Artifact[0]));
				}
				break;
			case importProject:
				if (addToSelectedPom != null && addToSelectedPom.getSelection() && project != null) {
					ArtifactToClipboardExpert.injectDependenciesIntoProject(project, mode, artifactsToCopy.toArray( new Artifact[0]));
				}
				if (tuples.size() > 0) {
					ProjectImporter.importProjects( preprocess, targetProvider, ArtifactContainerPlugin.getWorkspaceProjectRegistry(), tuples.toArray( new ProjectImporterTuple[0]));
				}
				break;
			default:
				selection = artifactsToCopy.get(0);
				break;
		}
		super.okPressed();
	}

	@Override
	protected Point getInitialSize() {
		return new Point( 600, 500);
	}
	
	
	
	@Override
	protected Control createButtonBar(Composite parent) {		
		Control control = super.createButtonBar( parent);
		getButton( IDialogConstants.OK_ID).setEnabled( false);
		return control;
	}

	/**
	 * update tree via the selected artifacts: get selection and build a query statement from the selection,
	 * then run the query again. <br/>
	 * switches from like to eq query via the single quote 
	 */
	private void updateTreeWithSelectedArtifacts() {
		// switch mode..		
		StringBuilder builder = new StringBuilder();
		for (TreeItem item : tree.getSelection()) {
			if (builder.length() > 0)
				builder.append("|");
			SourceArtifact sourceArtifact = (SourceArtifact) item.getData(MARKER_ARTIFACT);
			builder.append( "'" + sourceArtifact.getGroupId() + "':'" + sourceArtifact.getArtifactId() + "'");					
		}
		editBox.setText( builder.toString());	
		queue.offer( editBox.getText());			
	}
	
	

	/**
	 * uses the parameter to run a query against the session, and then
	 * let's the tree update itself in the UI thread 
	 * @param txt - the {@link String} with the expression 
	 */
	private void handleArtifactSelection( String txt) {
		
		List<SourceArtifact> result = qiController.runCoarseSourceArtifactQuery(txt);
	
		currentResult = result != null ? new ArrayList<SourceArtifact>(result) : new ArrayList<SourceArtifact>();
		setStatusLine("found [" + currentResult.size() + "] source artifacts");
		getShell().getDisplay().asyncExec( new Runnable() {
			
			@Override
			public void run() {
				updateTree( currentResult);				
			}
		});							
	}
	
	private void setStatusLine( final String line) {
		 shell.getDisplay().asyncExec( new Runnable() {
				
				@Override
				public void run() {
					 status.setText( line);						
				}
			});

	}

	
	/**
	 * update the tree - this needs to be run with the UI thread
	 * @param foundArtifacts - a {@link List} of {@link SourceArtifact}
	 */
	private void updateTree( List<SourceArtifact> foundArtifacts) {
		// remove all items 
		tree.removeAll();
		registry = new TreeRegistry(tree);
		registry.setRelationFilter(ProjectWorkspaceRelation.notLoaded);		
		registry.setMode( currentMode);
		
		if (targetProvider != null) {
			IWorkingSet targetWorkingSet = targetProvider.getTargetWorkingSet();
			if (targetWorkingSet != null) {
				registry.setActiveWorkingSet( targetWorkingSet);
				if (qiPreferences.getFilterOnWorkingSet()) {
					registry.setRelationFilter( ProjectWorkspaceRelation.presentInWorkspace);
				}
			}
		}
		
		
		// sort the source artifacts 
		Collections.sort( foundArtifacts, new Comparator<SourceArtifact>() {

			@Override
			public int compare(SourceArtifact o1, SourceArtifact o2) {
				// ascending group compare
				int retval = o1.getGroupId().compareTo(o2.getGroupId());
				// ascending artifact compare 
				if (retval == 0) {
					retval = o1.getArtifactId().compareTo( o2.getArtifactId());
				}
				// descending version compare 
				if (retval == 0)
					retval = o1.getVersion().compareTo(o2.getVersion()) * -1;					
				return retval;
			}
			
		});	
				
		for (SourceArtifact artifact : foundArtifacts) {
			registry.acquireTreeItemForArtifact(artifact, importAction);			
		}
		// automatically expand the full tree 
		expander.expandTree(tree, true);
		// select the first entry
		currentlySelectedIndex = 0;
		TreeItem selectedItem = registry.getTreeItemForIndex( currentlySelectedIndex);
		if (selectedItem != null) {
			tree.setSelection(selectedItem);
		}
		
		switch (currentMode) {
			case condensed:
				modeButton.setSelection( true);
				break;
			case explicit:
				modeButton.setSelection( false);
				break;			
		}
		status.setText( "Found [" + foundArtifacts.size() + "] candidates");
	}

		
	
	@Override
	public int open() {
		worker.start();

		String expression = initialQuery;
		if (expression != null && expression.length() > 0) {
			shell.getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					queue.offer( expression);								
				}
			});			
		}
		
		// 
		int value = super.open();		
		return value;
	}

	@Override
	public boolean close() {
		try {
			worker.interrupt();			
			worker.join();
		} catch (InterruptedException e) {
			String msg = "Exception on worker join";
			ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
		// dispose cursors..
		if (waitCursor != null)
			waitCursor.dispose();
		
		bigFont.dispose();
	
		// dispose the tree registry (actually, it's fonts)
		if (registry != null)
			registry.dispose();
		// remove ourselves from the listener set of the plugin
		qiController.removeQuickImportScanResultListener( this);
		return super.close();
	}
	
	// 
	// thread that processes the input
	//
	private class Caller implements Runnable {
		private String expression; 
		
		public Caller( String expression){
			this.expression = expression;
		}		
		@Override
		public void run() {
			handleArtifactSelection(expression);			
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
						List<String> expressions = new ArrayList<String>();
						queue.drainTo( expressions, len - 1);						
					}
					String expression = queue.take();					
					// process  					
					Caller caller = new Caller( expression);
					executor.execute(caller);					
					//handleArtifactSelection(expression);
				} catch (InterruptedException e) {
					// shutdown requested, expected situation
					return;
				}
			}
		}		
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		switch (importAction) {
		case importDependency:
			newShell.setText("Quick import dependency");
			break;
		case importProject:
			newShell.setText("Quick import project");
			break;
		default:
			newShell.setText("Select artifact");
			break;		
		}
		 
		// add ourselves to the plugin's list of scan listeners 
		qiController.addQuickImportScanResultListener( this);
		
	}
	

	/**
	 * automatically select the next item 
	 * @param direction - the {@link Direction} of the increment/decrement
	 */
	private void selectArtifact( Direction direction) {
		
		// nothing listed or nothing is selectable, return
		if (registry.getArtifactCount()== 0 || !isAnySelectable()) {
			return;
		}
		switch (direction) {
			case up:		
					currentlySelectedIndex--;
				break;
			case down:		
					currentlySelectedIndex++;
				break;
		}		
		if (currentlySelectedIndex < 0) {
			currentlySelectedIndex = registry.getArtifactCount() - 1;
		}
		if (currentlySelectedIndex == registry.getArtifactCount()) {
			currentlySelectedIndex = 0;
		}
		TreeItem treeItemForIndex = registry.getTreeItemForIndex( currentlySelectedIndex);
		if (isSelectionValid(treeItemForIndex)) {
			lastSelectedIndex = currentlySelectedIndex;
			tree.setSelection( treeItemForIndex);
			updateOkButton();
		}
		else {
			if (lastSelectedIndex != currentlySelectedIndex) {
				lastSelectedIndex = currentlySelectedIndex;				
				selectArtifact(direction);
			}
			else {
				
			}
			
		}
		
	}
	
	/**
	 * check if any of the tree items listed are selectable
	 * @return - true if any are, false if none are
	 */
	private boolean isAnySelectable() {
		Collection<TreeItem> items = registry.getTreeItems();
		if (items == null || items.size() == 0)
			return false;
		for (TreeItem item : items) {
			if (isSelectionValid(item))
				return true;
		}
		return false;
	}
	/**
	 * import the selected projects from the tree<br/>
	 * if the associated project file can't be found, it will 
	 * try to make a check out from svn to retrieve it. 
	 */
	private List<ProjectImporterTuple> extractSelectedArtifacts() {
		
		List<ProjectImporterTuple> tuples = new ArrayList<ProjectImporterTuple>();
		
		TreeItem [] items = tree.getSelection();		
		for (TreeItem item : items) {
			SourceArtifact sourceArtifact = (SourceArtifact) item.getData( MARKER_ARTIFACT);
			if (sourceArtifact == null) {
				continue;
			}
			// 
			// import 
			//					
			try {
				// a) convert to artifact
				Artifact artifact = Artifact.T.create();
				artifact.setGroupId( sourceArtifact.getGroupId());
				artifact.setArtifactId( sourceArtifact.getArtifactId());
				artifact.setVersion( VersionProcessor.createFromString( sourceArtifact.getVersion()));
				
				// b) test what protocol
				File projectFile = PantherSelectionHelper.determineProjectFile(sourceArtifact);
															
				// found project? 
				if (projectFile == null || projectFile.exists() == false ) {				
					String msg="Cannot import [" + PantherSelectionHelper.sourceArtifactToString(sourceArtifact) + "] as the project file [" + projectFile.getAbsolutePath() +"] doesn't exist in the working copy";
					ArtifactContainerStatus status = new ArtifactContainerStatus( msg, IStatus.ERROR);
					plugin.log(status);				
					continue;
				}
				// d) add to list		
				ProjectImporterTuple tuple = new ProjectImporterTuple(projectFile.getAbsolutePath(), artifact);
				tuples.add(tuple);				
			} catch (VersionProcessingException e) {
				String msg ="cannot create a valid version from [" + sourceArtifact.getVersion() +"]";
				ArtifactContainerStatus status = new ArtifactContainerStatus( msg, e);
				plugin.log(status);				
				continue;
			} 						
		}
		tree.deselectAll();
		return tuples;
	}
	
	

	@Override
	public void acknowledgeScanResult(SourceRepositoryPairing pairing, List<SourceArtifact> result) {		
		// reactivate the scan button
		shell.getDisplay().asyncExec( new Runnable() {							
			@Override
			public void run() {
				scanButton.setEnabled( true);
				// rerun the query?
				String text =editBox.getText();
				if (
						text != null &&
						text.length() > 0
					) {
					queue.offer( text);
				}
			}
		});				
	}
	
	
	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {	
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		TreeItem item = (TreeItem) e.item;
		handleSelection(item);				
	}
	
	private void handleSelection(TreeItem item) {
		if  (!isSelectionValid(item)){ 
			tree.deselect(item);
		}
		updateOkButton();
	}						
	
	private boolean isSelectionValid( TreeItem item) {
		return !(
				!Boolean.TRUE.equals(item.getData(MARKER_AVAILABLE)) 
				&& importAction == QuickImportAction.importProject // can only be selected in import project mode if available
			);
	}
	
	private String determineInitialQueryFromPackageExplorer() { 
		if (!ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getQuickImportPreferences().getPrimeWithSelection())
			return null;
		List<Artifact> artifacts = SelectionExtractor.extractSelectedArtifacts();
		if (artifacts != null && !artifacts.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Artifact artifact : artifacts) {
				String name = NameParser.buildName(artifact);
				if (sb.length() > 0) {
					sb.append("|");
				}
				sb.append( name);
			}
			return sb.toString();					
		}
		return null;
	}
}
