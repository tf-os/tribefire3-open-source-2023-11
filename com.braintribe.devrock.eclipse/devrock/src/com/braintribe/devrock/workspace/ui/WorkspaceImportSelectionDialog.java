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
package com.braintribe.devrock.workspace.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.api.ui.tree.TreeViewerColumnResizer;
import com.braintribe.devrock.eclipse.model.workspace.ExportPackage;
import com.braintribe.devrock.eclipse.model.workspace.Project;
import com.braintribe.devrock.eclipse.model.workspace.WorkingSet;
import com.braintribe.devrock.eclipse.model.workspace.Workspace;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.workspace.WorkspacePopulationMarshaller;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.wire.api.util.Lists;

/**
 * Dialog to selectively import projects/workingsets from a dump WS.  
 * 
 * 
 * @author pit
 *
 */
public class WorkspaceImportSelectionDialog extends DevrockDialog {
	private static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private static List<String> workingSetsToIgnore = Lists.list( "Java Main Sources", "Java Test Sources");
	
	
	private Workspace selectedWorkspace;
	private TreeViewer treeViewer;
	private List<WorkingSet> selectedWorkingSets;
	private List<Project> selectedProjects;
	private Shell shell;
	private ContentProvider contentProvider;
	
	private BooleanEditor toggleIntrinsicWorkingSets;
	private BooleanEditor toggleWorkingSets;
	private BooleanEditor toggleDuplicatesFromWorkingSets;
	private Button importAll;
	private Workspace workspace;
	private Pair<List<WorkingSet>, List<Project>> currentSelection;
		 	
	public WorkspaceImportSelectionDialog(Shell parentShell) {
		super(parentShell);
		this.shell = parentShell;
		setShellStyle( SHELL_STYLE);			
	}
	
	@Override
	protected Point getDrInitialSize() {
		return new Point( 600, 600);
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
        
        
        // components for file load
        
        Composite fileComposite = new Composite(composite, SWT.NONE);
        
        fileComposite.setLayout(layout);
        fileComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        Label fileSelectionLabel = new Label(fileComposite, SWT.NONE);
        fileSelectionLabel.setText("File: ");
        fileSelectionLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
        
        Text fileNameLabel = new Text( fileComposite, SWT.NONE);
        fileNameLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2,1));

		String lastFileName = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_LAST_FILE, "");
		if (lastFileName.length() != 0) {		
			fileNameLabel.setText(lastFileName);
		}
        
        Button scanButton = new Button( fileComposite, SWT.NONE);
        scanButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
        scanButton.setText( "..");
        scanButton.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(shell);
				fd.setFilterExtensions( new String[] {"*.yaml"});
				String lastFileName = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_LAST_FILE, "");
				if (lastFileName.length() != 0) {
					File lastFile = new File( lastFileName);								
					fd.setFileName(lastFile.getName());
					fd.setFilterPath( lastFile.getParent());
				}
				
				String selectedYaml = fd.open();
				if (selectedYaml != null) {
					fileNameLabel.setText(selectedYaml);
					getButton(IDialogConstants.OK_ID).setEnabled(true);					
					DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.SLOT_WS_IMPORT_LAST_FILE, selectedYaml);					
					// active Import_all button
					importAll.setEnabled(true);
					// load and populate
					update(new File( selectedYaml));
				}
				// at this point, nothing can be selected, hence the OK button (import selected) is not available
				getButton(IDialogConstants.OK_ID).setEnabled( false);
				
				super.widgetSelected(e);
			}
        	
		});
        
        // options
        Composite optionsComposite = new Composite( composite, SWT.NONE);    
        GridLayout layout2= new GridLayout();
        layout2.numColumns = 3;
        layout2.verticalSpacing=2;        
    
        optionsComposite.setLayout( layout2);
        optionsComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
        
        // a) hide 'intrinsic working sets
        toggleIntrinsicWorkingSets = new BooleanEditor();
        toggleIntrinsicWorkingSets.setLabelToolTip("Whether the intrinsic workingsets 'Java Main Sources' and 'Java Test Sources' are shown");
        toggleIntrinsicWorkingSets.setCheckToolTip("If active the intrinsic workingsets 'Java Main Sources' and 'Java Test Sources' are shown, otherwise they're filtered");
        toggleIntrinsicWorkingSets.setSelectionListener( new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		// store value
        		Boolean selection = toggleIntrinsicWorkingSets.getSelection();
        		DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.SLOT_WS_IMPORT_TOGGLE_INTRINSICS, selection);
        		if (workspace != null) {
        			update(workspace);
        		}        		        	
        	}        	
        });              
      
        Boolean value = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_TOGGLE_INTRINSICS, false);
        toggleIntrinsicWorkingSets.setSelection( value);        
        Composite toggleIntrinsicWorkingSetsComposite = toggleIntrinsicWorkingSets.createControl(optionsComposite, "Show intrinsic workingsets");
        toggleIntrinsicWorkingSetsComposite.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1, 1));        
        
        // b) show/hide working sets
        toggleWorkingSets = new BooleanEditor();
        toggleWorkingSets.setLabelToolTip("Whether existing workingsets are show at all");
        toggleWorkingSets.setCheckToolTip("If active, the workingsets are shown, otherwise only standard projects are shown");
        toggleWorkingSets.setSelectionListener( new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		// store value
        		Boolean selection = toggleWorkingSets.getSelection();
        		DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.SLOT_WS_IMPORT_WORKINGSETS, selection);
        		if (!selection) {
        			toggleDuplicatesFromWorkingSets.setEnabled(false);
        		}
        		else {
        			toggleDuplicatesFromWorkingSets.setEnabled( true);
        		}
        		if (workspace != null) {
        			update(workspace);
        		}
        	}        	
        });              
                       
        value = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_WORKINGSETS, true);
        toggleWorkingSets.setSelection( value);
        
        Composite toggleWorkingSetsComposite = toggleWorkingSets.createControl(optionsComposite, "Show workingsets");
        toggleWorkingSetsComposite.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        
        
        // c) show only projects outside working sets that do not belong to one (other than the intrinsic)
        toggleDuplicatesFromWorkingSets = new BooleanEditor();
        toggleDuplicatesFromWorkingSets.setLabelToolTip("Whether to show projects as standalone if they are already part of a workingset");
        toggleDuplicatesFromWorkingSets.setCheckToolTip("If active, duplicate projects are filtered");
        toggleDuplicatesFromWorkingSets.setSelectionListener( new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		boolean selection = toggleDuplicatesFromWorkingSets.getSelection();
        		DevrockPlugin.instance().storageLocker().setValue(StorageLockerSlots.SLOT_WS_IMPORT_TOGGLE_DUPLICATES, selection);        		        		
        		if (workspace != null) {
        			update(workspace);
        		}
        	}        	
        });              
        
        value = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_TOGGLE_DUPLICATES, false);
        toggleDuplicatesFromWorkingSets.setSelection( value);
        Composite toggleDuplicatesComposite = toggleDuplicatesFromWorkingSets.createControl(optionsComposite, "Show duplicates");
        toggleDuplicatesComposite.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        
        // tree for display
        Composite treeComposite = new Composite( composite, SWT.BORDER);              
		treeComposite.setLayout( new FillLayout());
		treeComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 3, 2));
		
		treeViewer = new TreeViewer( treeComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);				
		
		contentProvider = new ContentProvider();
		
		treeViewer.setContentProvider( contentProvider);
    	treeViewer.getTree().setHeaderVisible(true);
    
    	List<TreeViewerColumn> columns = new ArrayList<>();        	
    	
    	TreeViewerColumn nameColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        nameColumn.getColumn().setText("Workspace entry");
        nameColumn.getColumn().setToolTipText( "workspace content entity");
        nameColumn.getColumn().setWidth(100);
        
        ViewLabelProvider viewLabelProvider = new ViewLabelProvider();
        viewLabelProvider.setUiSupport( DevrockPlugin.instance().uiSupport());
        viewLabelProvider.setUiSupportStylersKey("workspace_import_dlg");
        
        nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider( viewLabelProvider));
        nameColumn.getColumn().setResizable(true);                   
        columns.add(nameColumn);
    
    	
    	ColumnViewerToolTipSupport.enableFor(treeViewer);
		
		Tree tree = treeViewer.getTree();   
		
		tree.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = treeViewer.getSelection();
				Button ok = getButton(IDialogConstants.OK_ID);
				if (!selection.isEmpty()) {
					ok.setEnabled(true);
				}
				else {
					ok.setEnabled(false);
				}
			}
			
		});
    	
		TreeViewerColumnResizer columnResizer = new TreeViewerColumnResizer();
    	columnResizer.setColumns( columns);		
    	columnResizer.setParent( treeComposite);
    	columnResizer.setTree( tree);    	
    	tree.addControlListener(columnResizer);
	    
    	treeViewer.expandAll();
    	
        return composite;
	}
	
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		
          parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
          
		  // Create a spacer label
		  Label spacer = new Label(parent, SWT.NONE);
		  spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		  // Update layout of the parent composite to count the spacer
		  GridLayout layout = (GridLayout)parent.getLayout();
		  layout.numColumns++;
		  layout.makeColumnsEqualWidth = false;

		  importAll = createButton(parent, IDialogConstants.NO_ID, "Import all", false);
		  importAll.setToolTipText("Import the full contents of the workspace-dump");
		  importAll.addSelectionListener(new SelectionAdapter() {
			  
			  @Override
			  public void widgetSelected(SelectionEvent e) {
				  currentSelection = null;
				  okPressed();
				  close();
			  }        	
		  });
		  
		  importAll.setEnabled(false);
		  
		  Button ok= createButton(parent, IDialogConstants.OK_ID,"Import selected", false);
		  ok.setToolTipText("Import the currently selected items");
		  ok.setEnabled(false);
		  
		  createButton(parent, IDialogConstants.CANCEL_ID,"Close", true);
								
	}
	
	
	

	private Pair<List<WorkingSet>, List<Project>> extractSelection() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection( );			
		selectedWorkingSets = new ArrayList<>();
		selectedProjects = new ArrayList<>();
		
		for (Object obj : selection.toList()) {
			if (obj instanceof WorkingSet) {
				selectedWorkingSets.add( (WorkingSet) obj);
			}
			else if (obj instanceof Project) {
				selectedProjects.add( (Project) obj);
			}
		}
		return Pair.of(selectedWorkingSets, selectedProjects);
	}
	
	
	/**
	 * loads a file and updates the dialog
	 * @param file - the file to load 
	 */
	private void update(File file) {
		if (file.exists()) {
			ExportPackage exportPackage = WorkspacePopulationMarshaller.load(file);
			workspace = exportPackage.getWorkspace();
			update(workspace);
		}
		else {
			; // honk
		}
	}
	
	private void update(Workspace workspace) {
		// filter
		Workspace clone = workspace.clone( new StandardCloningContext());
		Iterator<WorkingSet> workingsetIterator = clone.getWorkingSets().iterator();
		
		Map<String, WorkingSet> mapped = new HashMap<>();
		
		Boolean tIntWs = toggleIntrinsicWorkingSets.getSelection();
		Boolean tWs = toggleWorkingSets.getSelection();	
		Boolean tDuWs= toggleDuplicatesFromWorkingSets.getSelection();
		
		while (workingsetIterator.hasNext()) {
			WorkingSet ws = workingsetIterator.next();		
			// drop working sets
			boolean removedSet = false; 
			if (!tIntWs) { 
				if (workingSetsToIgnore.contains(ws.getWorkingSetName())){
					workingsetIterator.remove();
					removedSet = true;
				}
			}
			if (!tWs && !removedSet) {
				workingsetIterator.remove();
			}
			ws.getProjects().stream().forEach( p -> mapped.put(p.getProjectName(), ws));
		}
		
		// filter projects
		if (!tDuWs && tWs) {
			List<Project> filtered = clone.getProjects().stream().filter( p -> !mapped.containsKey(p.getProjectName())).collect(Collectors.toList());
			clone.setProjects(filtered);
		}								
		
		contentProvider.setWorkspace(clone);
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setInput(clone);
		treeViewer.refresh();
		treeViewer.expandAll();
		
	}
		
	/**
	 * @return - a workspace - either the original one or one with only the selected workingset/projects
	 */
	public Workspace getSelection() {
		if (currentSelection != null) {					
			List<WorkingSet> selectedWorkingSets = currentSelection.first;
			List<Project> selectedProjects = currentSelection.second;
			
			if (selectedWorkingSets == null && selectedProjects == null) {
				return selectedWorkspace;
			}
			
			Workspace workspace = Workspace.T.create();
			workspace.setWorkspaceName("synthetic");
			if (selectedWorkingSets != null) {
				workspace.getWorkingSets().addAll(selectedWorkingSets);
			}
			if (selectedProjects != null) {
				workspace.getProjects().addAll( selectedProjects);
			}
			return workspace;
		}
		else {
			return workspace;
		}
	}
	
	

	@Override
	protected void okPressed() {
		currentSelection = extractSelection();				
		super.okPressed();
	}

		
	
	
}
