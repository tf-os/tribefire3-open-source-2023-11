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
package com.braintribe.devrock.importer.scanner.ui;

import static com.braintribe.utils.lcd.StringTools.isEmpty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkingSet;

import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.api.clipboard.ArtifactToClipboardExpert;
import com.braintribe.devrock.api.selection.SelectionExtracter;
import com.braintribe.devrock.api.selection.TargetProvider;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.tree.TreeItemTooltipProvider;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.bridge.eclipse.workspace.WorkspaceProjectView;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.eclipse.model.identification.RemoteCompiledDependencyIdentification;
import com.braintribe.devrock.eclipse.model.identification.SelectableEnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.importer.ProjectImporter;
import com.braintribe.devrock.importer.scanner.QuickImportControl;
import com.braintribe.devrock.importer.scanner.listener.QuickImportScanListener;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;
import com.braintribe.utils.lcd.LazyInitialized;


/**
 * a dialog that lets you import projects...  
 * 
 * @author pit
 *
 */
public class QuickImportDialog extends DevrockDialog implements QuickImportScanListener, HasQuickImportTokens, SelectionListener {
	
	
	private static final int TREE_HEIGHT = 20;
	private static final int TREE_WIDTH = 40;
	private static final int COMPOSITE_HEIGHT = 25;
	private static final int COMPOSITE_WIDTH = 80;
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private final Shell shell;
	private Cursor waitCursor;
	private Text editBox;
	private Label status;

	private Button scanButton;
	private Tree tree;
	
	private enum Direction {up, down}
	
	
	private final Worker worker = new Worker();
	private final Executor executor = Executors.newCachedThreadPool();
	private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

	private final boolean alternativeUiStyle = true;
			
	private List<SelectableEnhancedCompiledArtifactIdentification> currentResult;	
	private String initialQuery;
	private TargetProvider targetProvider;
		
	private Button addToSelectedPom;
	
	private QuickImportAction importAction = QuickImportAction.importProject;
	private final QuickImportControl qiController = DevrockPlugin.instance().quickImportController();

	private Font bigFont;
	
	private EnhancedCompiledArtifactIdentification selection;	
	private Button directMode;
	private Button rangeMode;
	private Button variableMode;
	private ContentProvider contentProvider;
	private TreeViewer treeViewer;
	
	
	private final LazyInitialized<WorkspaceProjectView> workspaceProjectView = new LazyInitialized<>( () -> DevrockPlugin.instance().getWorkspaceProjectView());
	
	
	public EnhancedCompiledArtifactIdentification getSelection() {
		return selection;
	}
	@Configurable
	public void setSelection(EnhancedCompiledArtifactIdentification selection) {
		this.selection = selection;	
		setInitialQuery( selection.getGroupId() + ":" + selection.getArtifactId() + "#" +selection.getVersion().asString());
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
        
        
        // modify listener -> trigger query 
        editBox.addModifyListener( e -> offerToQueue( editBox.getText()));
               
        // traverse listener -> enter key
        editBox.addTraverseListener(this::processTraverseEvent);
      
    	scanButton = new Button( textComposite, SWT.NONE);
    	scanButton.setText( "Refresh");
    	scanButton.setLayoutData(new GridData( SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    	if (qiController.isScanActive()) {
    		scanButton.setEnabled( false);
    	} else {
    		scanButton.setEnabled( true);
    	}
    	
    	scanButton.addSelectionListener( new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				runscan();
			}

			
		});
      
        
        
        
        Composite treeLabelComposite = new Composite( composite, SWT.NONE);
        treeLabelComposite.setLayout( layout);
        treeLabelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
        
        Label treeLabel = new Label( treeLabelComposite, SWT.NONE);
        treeLabel.setText( "Matching items:");
        treeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);
		//treeComposite.setLayout( new FillLayout());
        treeComposite.setLayout(layout);
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
		
		treeViewer = new TreeViewer( treeComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		
		contentProvider = new ContentProvider();
		treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
    	treeViewer.addPostSelectionChangedListener( event -> updateOkButton());
    	
    	ColumnViewerToolTipSupport.enableFor(treeViewer);
    	
    	// columns 
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        makeColumnResizableAndSticky(nameColumn.getColumn(), "Name", 200);
        nameColumn.getColumn().setToolTipText( "Identification");
        nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( new ViewLabelProvider( "identification", parent.getFont(), importAction)));
        columns.add(nameColumn);
        
        TreeViewerColumn pathColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        makeColumnResizableAndSticky(pathColumn.getColumn(), "Path", 200);
        pathColumn.getColumn().setToolTipText( "location");
        pathColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( new ViewLabelProvider( "path", parent.getFont(), importAction)));
        columns.add(pathColumn);
		
        
		treeViewer.setInput( currentResult);
		ColumnViewerToolTipSupport.enableFor(treeViewer);
		
		tree = treeViewer.getTree();
	    
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL);			
		int ht = (tree.getItemHeight() * 10) + tree.getHeaderHeight();
		
    	Point computedSize = tree.computeSize(SWT.DEFAULT, ht);
		layoutData.heightHint = computedSize.y;    					
		layoutData.widthHint = computedSize.x * 2;
    	tree.setLayoutData(layoutData);
		
    	
		TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
    	columnResizer.setColumns( columns);		
    	columnResizer.setParent( treeComposite);
    	columnResizer.setTree( tree);    	
    	tree.addControlListener(columnResizer);
    
		
		// add listener to check for the double click event 
		tree.addMouseListener( new MouseAdapter() {								
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			
				TreeItem item = tree.getItem( new Point( e.x, e.y));
				if (item != null) {
					SelectableEnhancedCompiledArtifactIdentification secai = (SelectableEnhancedCompiledArtifactIdentification) item.getData();
					if (secai.getCanBeImportedWithCurrentChoices() || importAction != QuickImportAction.importProject) {										
						treeViewer.setSelection(new StructuredSelection( secai));				
						okPressed();
						return;						
					}
				}
			}

		});
		

		// add listener to override selections (only allow valid items that can be imported)
		tree.addSelectionListener( this);
			
		TreeItemTooltipProvider.attach(tree, MARKER_TOOLTIP);
		
		
		// add listener to process key strokes 
		tree.addTraverseListener( this::processTraverseEvent);
	

		// dependency insertion mode radios
		if (importAction == QuickImportAction.importDependency) {
			Composite versionModeComposite = new Composite( composite, SWT.NONE);
			versionModeComposite.setLayout( layout);
			versionModeComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false));
			
			Label versionModeLabel = new Label( versionModeComposite, SWT.NONE);
			versionModeLabel.setFont(bigFont);
			versionModeLabel.setText( "version modification");
			versionModeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
			
			VersionModificationAction versionModificationAction = VersionModificationAction.referenced;
			
			
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
		
		tree.setSize( pc.convertWidthInCharsToPixels(TREE_WIDTH), pc.convertHeightInCharsToPixels(TREE_HEIGHT));
		
		treeViewer.expandAll();
	    
        composite.pack();	
        editBox.setFocus();       
        
        shell.layout(true); 
        
        // 
        String expression = editBox.getText();
        if (!isEmpty(expression)) {
	        shell.getDisplay().asyncExec(() -> offerToQueue(expression));	
        }
        
        // activate scan 
        Job scanJob = new Job("autoscan") {
			
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				runscan();
				return Status.OK_STATUS;
			}
		}; 
		scanJob.schedule();
		
        return composite;
	}
	

	private void updateOkButton() {
		
		IStructuredSelection structuredSelection = treeViewer.getStructuredSelection();
		if (structuredSelection.isEmpty()) {
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return;
		}
		if (importAction == QuickImportAction.importDependency) {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
			return;
		}
		boolean cannot = false; 
		for (Object obj : structuredSelection.toList()) {
			if (obj instanceof SelectableEnhancedCompiledArtifactIdentification) {
				SelectableEnhancedCompiledArtifactIdentification secai = (SelectableEnhancedCompiledArtifactIdentification) obj;
				if (!secai.getCanBeImportedWithCurrentChoices()) {
					cannot = true;
					break;
				}
			}
		}
		if (cannot) {
			getButton(IDialogConstants.OK_ID).setEnabled( false);						
		}
		else {
			getButton(IDialogConstants.OK_ID).setEnabled(true);
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
			if ( !alternativeUiStyle ) {							
				if (e.stateMask == 0) {
					e.doit = false;
					e.detail = SWT.TRAVERSE_NONE;
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
		List<SelectableEnhancedCompiledArtifactIdentification> tuples = extractSelectedArtifacts();
		
		IProject project = targetProvider != null ? targetProvider.getTargetProject() : null;
		VersionModificationAction mode = determineDependencyInsertionMode();
		
		List<RemoteCompiledDependencyIdentification> rcdis = tuples.stream().map( secai -> RemoteCompiledDependencyIdentification.from( secai)).collect( Collectors.toList());
		

		switch (importAction) {
			case importDependency:
				if (tuples.size() > 0) {					
					ArtifactToClipboardExpert.copyToClipboard(mode, rcdis);
				}					
				if (project != null) {					
					ArtifactToClipboardExpert.injectDependenciesIntoProject(project, mode, rcdis);
				}
				break;
			case importProject:
				if (addToSelectedPom != null && addToSelectedPom.getSelection() && project != null) {
					ArtifactToClipboardExpert.injectDependenciesIntoProject(project, mode, rcdis);
				}
				if (!tuples.isEmpty()) {
					IWorkingSet workingSet = SelectionExtracter.currentlySelectedWorkingSet();
					ProjectImporter.importProjects( workingSet, tuples, null);
				}
				break;
			default:
				selection = tuples.get(0);
				break;
		}
		super.okPressed();
	}

	@Override
	protected Point getDrInitialSize() {
		return new Point( 800, 500);
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
		StringJoiner builder = new StringJoiner("|");
		for (SelectableEnhancedCompiledArtifactIdentification secai : extractSelectedArtifacts()) {		
			builder.add( "'" + secai.getGroupId() + "':'" + secai.getArtifactId() + "'");					
		}
		editBox.setText( builder.toString());
		offerToQueue(editBox.getText());
	}
	
	/**
	 * uses the parameter to run a query against the session, and then
	 * let's the tree update itself in the UI thread 
	 * @param txt - the {@link String} with the expression 
	 */
	private void processQueryAndBuildTreeContents( String txt) {
		
		if (txt == null || txt.length() == 0)
			return;
		
		// filter here 
		IWorkingSet selectedWorkingSet = SelectionExtracter.currentlySelectedWorkingSet();
				
		boolean workingSetImport = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_FILTER_WSET, false);

		// check if a simple 
		List<EnhancedCompiledArtifactIdentification> result;
		if (Character.isUpperCase(txt.charAt(0)) || // 
				txt.contains("?") || //
				txt.contains( "*")
			) {
			result = qiController.runQuery(txt);
		}
		else {			
			result = qiController.runContainsQuery(txt);	
		}
		currentResult = result.stream() //
				.map(ecai -> toSelectableEcai(selectedWorkingSet, workingSetImport, ecai)) //
				.collect(Collectors.toList());
	
		Optional<Boolean> showAllOpt = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_QI_SHOW_ALL);
		boolean showAll = showAllOpt.isPresent() && showAllOpt.get();
		// only filter here if some projects should be hidden AND it's about importing projects
		if (!showAll && importAction == QuickImportAction.importProject) {
			currentResult = currentResult.stream() //
					.filter(secai -> secai.getCanBeImportedWithCurrentChoices()) //
					.collect(Collectors.toList());
		}
		
		// sort 
		currentResult.sort(this::compareSecais);
		
		setStatusLine("found [" + currentResult.size() + "] source artifacts");
		getShell().getDisplay().asyncExec( () -> updateTree( currentResult));
	}

	private int compareSecais(SelectableEnhancedCompiledArtifactIdentification o1, SelectableEnhancedCompiledArtifactIdentification o2) {
		int retval = o1.getGroupId().compareTo(o2.getGroupId());
		if (retval != 0)
			return retval;

		retval = o1.getArtifactId().compareTo(o2.getArtifactId());
		if (retval != 0)
			return retval;

		return o1.getVersion().compareTo(o2.getVersion());
	}

	private SelectableEnhancedCompiledArtifactIdentification toSelectableEcai(IWorkingSet selectedWs, boolean wsImport,
			EnhancedCompiledArtifactIdentification ecai) {

		SelectableEnhancedCompiledArtifactIdentification secai = SelectableEnhancedCompiledArtifactIdentification.from(ecai);
		// check if it exist in the workspace
		if (workspaceProjectView.get().getProjectInfo(secai) != null) {
			secai.setExistsInWorkspace(true);
		}
		// check if it exist in the current working set
		if (secai.getExistsInWorkspace()) {
			if (selectedWs != null) {
				for (IAdaptable adaptable : selectedWs.getElements()) {
					IJavaProject javaProject = adaptable.getAdapter(IJavaProject.class);
					if (javaProject != null) {
						String location = javaProject.getProject().getLocation().toOSString();
						String origin = secai.getOrigin();
						if (location.equals(origin)) {
							secai.setExistsInCurrentWorkingSet(true);
							break;
						}
					}
				}
			} else {
				// actually it's semantically wrong as no working set exists or is selected..
				// but as the target would be the 'implicit' working set, it can't be imported
				secai.setExistsInCurrentWorkingSet(true);
			}
		}

		// decide whether it could be imported
		secai.setCanBeImportedWithCurrentChoices( //
				!secai.getExistsInWorkspace() || (wsImport && !secai.getExistsInCurrentWorkingSet()));

		return secai;
	}
	
	private void setStatusLine( final String line) {
		 shell.getDisplay().asyncExec( new Runnable() {
				
				@Override
				public void run() {
					 status.setText( line);						
				}
			});

	}

	private void runscan() {
		 shell.getDisplay().asyncExec( new Runnable() {
				
				@Override
				public void run() {
					scanButton.setEnabled( false);
					qiController.scheduleRescan();						
				}
			});	
	}

	
	/**
	 * update the tree - this needs to be run with the UI thread
	 */
	private void updateTree( List<SelectableEnhancedCompiledArtifactIdentification> foundArtifacts) {
		// remove all items 
		//tree.removeAll();
		contentProvider.setupFrom(foundArtifacts);
		treeViewer.setInput(foundArtifacts);
		treeViewer.refresh();
		treeViewer.expandAll();
			
				
		status.setText( "Found [" + foundArtifacts.size() + "] candidates");
	}

		
	
	@Override
	public int open() {
		worker.start();

		String expression = initialQuery;
		if (!isEmpty(expression)) {
			shell.getDisplay().asyncExec(() -> offerToQueue(expression));			
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
			DevrockPluginStatus status = new DevrockPluginStatus( msg, e);
			DevrockPlugin.instance().log(status);	
		}
		// dispose cursors..
		if (waitCursor != null)
			waitCursor.dispose();
		
		bigFont.dispose();
	
		// dispose the tree registry (actually, it's fonts)
		
		// remove ourselves from the listener set of the plugin
		qiController.removeListener( this);
		return super.close();
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
						List<String> expressions = new ArrayList<>();
						queue.drainTo( expressions, len - 1);						
					}
					String expression = queue.take();					
					// process  					
					executor.execute(() -> processQueryAndBuildTreeContents(expression));					
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
			newShell.setText("Quick import local dependencies");
			break;
		case importProject:
			newShell.setText("Quick import projects");
			break;
		default:
			newShell.setText("Select artifact");
			break;		
		}
		 
		// add ourselves to the plugin's list of scan listeners 
		qiController.addListener( this);
		
	}
	

	/**
	 * automatically select the next item 
	 * @param direction - the {@link Direction} of the increment/decrement
	 */
	private void selectArtifact( Direction direction) {
		
		
		SelectableEnhancedCompiledArtifactIdentification secai;
		switch (direction) {
			case up:		
					secai = contentProvider.getPrevious();
				break;
			case down:		
					secai = contentProvider.getNext(); 
				break;
			default:
				throw new IllegalStateException("only [" + Direction.up.toString()  +"] or [" + Direction.down.toString() + "] are supported here");
		}		
		
		treeViewer.expandToLevel(secai, 0);
		treeViewer.setSelection(new StructuredSelection( secai));
			
		updateOkButton();
		
		
	}
		
	/**
	 * import the selected projects from the tree<br/>
	 * if the associated project file can't be found, it will 
	 * try to make a check out from svn to retrieve it. 
	 */
	private List<SelectableEnhancedCompiledArtifactIdentification> extractSelectedArtifacts() {
		
		List<SelectableEnhancedCompiledArtifactIdentification> tuples = new ArrayList<>();
		
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection( );
							
		for (Object obj : selection.toList()) {
			if (obj instanceof SelectableEnhancedCompiledArtifactIdentification == false) {			
				continue;
			}
			// 
			// import 
			//									
			SelectableEnhancedCompiledArtifactIdentification secai = (SelectableEnhancedCompiledArtifactIdentification) obj;
			File projectFile = new File(secai.getOrigin() + "/.project");
														
			// found project? 
			if (!projectFile.exists()) {				
				String msg="Cannot import [" + secai.asString() + "] as the project file [" + projectFile.getAbsolutePath() +"] doesn't exist in the working copy";
				DevrockPluginStatus status = new DevrockPluginStatus( msg, IStatus.ERROR);
				DevrockPlugin.instance().log(status);				
				continue;
			}
			// add to list		
			tuples.add( secai);				
		}
		tree.deselectAll();
		return tuples;
	}
	
	
	
	@Override
	public void acknowledgeScanResult(Collection<EnhancedCompiledArtifactIdentification> result) {		
		// reactivate the scan button
		shell.getDisplay().asyncExec( () -> {							
			scanButton.setEnabled(true);
			// rerun the query?
			String text = editBox.getText();
			if (!isEmpty(text))
				queue.offer(text);
		});				
	}
	
	
	private void offerToQueue(String s) {
		queue.offer(s);
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {	
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {	
	}
	
	
}
