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
package com.braintribe.devrock.preferences.pages.scan;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.api.ui.table.CommonTableColumnData;
import com.braintribe.devrock.api.ui.table.CommonTableViewer;
import com.braintribe.devrock.bridge.eclipse.environment.BasicStorageLocker;
import com.braintribe.devrock.eclipse.model.actions.VersionModificationAction;
import com.braintribe.devrock.eclipse.model.scan.SourceRepositoryEntry;
import com.braintribe.devrock.importer.scanner.QuickImportControl;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.preferences.pages.scan.dialog.RepositoryPairingDialog;



public class ScanConfigurationPage extends PreferencePage implements IWorkbenchPreferencePage, SelectionListener {
	private static final String PREFERENCES_SCAN = "Devrock's source-scan configuration";
	protected Font bigFont;
	
	private Button addButton;
	private Button removeButton;
	private Button editButton;
	
	private Image addImage;
	private Image editImage;
	private Image removeImage;
	private Image rebuildIndexImage;
	
	private Button copyUntouched;
	private Button copyRangified;
	private Button copyReference;
	
	private Button pasteUntouched;
	private Button pasteRangified;
	private Button pasteReference;
	
	private BooleanEditor qiShowAllArtifacts;	
	private BooleanEditor qiFilterOnWorkingSet;
	
	private CommonTableViewer repositoryTableViewer;
	private Table repositoryTable;
	private Button buildIndexButton;
	private List<SourceRepositoryEntry> entries;
	private QuickImportControl qiController = DevrockPlugin.instance().quickImportController();
	
	public ScanConfigurationPage() {
		setDescription(PREFERENCES_SCAN);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( ScanConfigurationPage.class, "add.gif");
		addImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ScanConfigurationPage.class, "editconfig.gif");
		editImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ScanConfigurationPage.class, "remove.gif");
		removeImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ScanConfigurationPage.class, "rebuild_index.gif");
		rebuildIndexImage = imageDescriptor.createImage();	
	}

	@Override
	public void init(IWorkbench arg0) {		
	}

	@Override
	protected Control createContents(Composite parent) {
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		Composite composite = new Composite(parent, SWT.BORDER);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout( layout);
		
					
		entries = DevrockPlugin.envBridge().getScanRepositories();
				
		
		Label label = new Label( composite, SWT.NONE);
		label.setText( "Directories to scan");
		label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		label.setFont(bigFont);
		
		Composite cmp = new Composite(  composite, SWT.NONE);
		cmp.setLayout( layout);
		cmp.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
		
		
		// table or tree left, buttons right ... 
		
		
		Composite tableComposite = new Composite(cmp, SWT.NONE);	
		tableComposite.setLayout( new FillLayout());
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 3,1));

    	repositoryTableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columnData = new CommonTableColumnData[2];		
		columnData[0] = new CommonTableColumnData("path", 200, 200, "Working copy directory", new PathColumnLabelProvider(parent.getDisplay()));
		columnData[1] = new CommonTableColumnData("key", 50, 50, "Key", new NameColumnLabelProvider());
		
		repositoryTableViewer.setup(columnData);
		
		repositoryTable = repositoryTableViewer.getTable();
		repositoryTable.setHeaderVisible(true);
		repositoryTable.setLinesVisible(true);
		
		RepositoryContentProvider contentProvider = new RepositoryContentProvider();    
		contentProvider.setPairings( entries);
		repositoryTableViewer.setContentProvider(contentProvider);
		repositoryTableViewer.setInput( entries);
		// 
		
		
		Composite buttonComposite = new Composite(  cmp, SWT.NONE);
		buttonComposite.setLayoutData( new GridData( SWT.LEFT, SWT.TOP, false, false, 1, 4));
		buttonComposite.setLayout( layout);
		
		addButton = new Button( buttonComposite, SWT.PUSH);
		//addButton.setText( "add");
		addButton.setToolTipText( "add a new source directory");
		addButton.setImage(addImage);
		addButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		addButton.addSelectionListener( this);
		
		removeButton = new Button( buttonComposite, SWT.PUSH);
		//removeButton.setText( "remove");
		removeButton.setToolTipText( "remove the selected source directory");
		removeButton.setImage(removeImage);
		removeButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		removeButton.addSelectionListener( this);
		
		editButton = new Button( buttonComposite, SWT.PUSH);
		//editButton.setText( "edit");
		editButton.setToolTipText("edit the selected source directory");
		editButton.setImage(editImage);
		editButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		editButton.addSelectionListener( this);
		
		buildIndexButton = new Button( buttonComposite, SWT.NONE);
		buildIndexButton.setImage( rebuildIndexImage);
		buildIndexButton.setToolTipText( "rescan the directories");
		buildIndexButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 4,1));
		buildIndexButton.addSelectionListener(this);
		
		
		Composite versionModeGroup = new Composite( composite, SWT.NONE);
		versionModeGroup.setLayout( layout);
		versionModeGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label versionModeLabel = new Label( versionModeGroup, SWT.NONE);
		versionModeLabel.setText( "Version manipulation modes for dependency copy/paste features");
    	versionModeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
    	versionModeLabel.setFont(bigFont);
        
		
		Composite copyModeGroup = new Composite( composite, SWT.NONE);
		copyModeGroup.setLayout( layout);
		copyModeGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label copyModeLabel = new Label( copyModeGroup, SWT.NONE);
		copyModeLabel.setText( "copy");
    	copyModeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
    		
    	// 
    	copyUntouched = new Button( copyModeGroup, SWT.RADIO);
    	copyUntouched.setText( "keep version untouched");
    	copyUntouched.setToolTipText( "version expressions of a dependency remains as is");
    	copyUntouched.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1));
    	
    	copyRangified = new Button( copyModeGroup, SWT.RADIO);
    	copyRangified.setText( "rangify version");
    	copyRangified.setToolTipText( "version expressions of dependencies are turned into version ranges");
    	copyRangified.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1));
    	
    	copyReference = new Button( copyModeGroup, SWT.RADIO);
    	copyReference.setText( "use variable reference");
    	copyReference.setToolTipText( "version expressions of dependencies are replaced by variable references");
    	copyReference.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1));
    	
    	
    	
    	VersionModificationAction vmaCopy = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_CLIP_COPY_MODE, VersionModificationAction.referenced);
    	
    	// activate
    	switch ( vmaCopy) {
			case rangified:
				copyRangified.setSelection( true);
				break;
			case referenced:
				copyReference.setSelection( true);
				break;
			case untouched:		
			default:
				copyUntouched.setSelection( true);
				break;    	
    	}
    	
    	
    	Composite pasteModeGroup = new Composite( composite, SWT.NONE);
		pasteModeGroup.setLayout( layout);
		pasteModeGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label pasteModeLabel = new Label( pasteModeGroup, SWT.NONE);
		pasteModeLabel.setText( "paste");
    	pasteModeLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		
    	// 
    	pasteUntouched = new Button( pasteModeGroup, SWT.RADIO);
    	pasteUntouched.setText( "keep version untouched");
    	pasteUntouched.setToolTipText( "version expressions of a dependency remains as is");
    	pasteUntouched.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1));
    	
    	pasteRangified = new Button( pasteModeGroup, SWT.RADIO);
    	pasteRangified.setText( "rangify version");
    	pasteRangified.setToolTipText( "version expressions of dependencies are turned into version ranges");
    	pasteRangified.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1));
    	
    	pasteReference = new Button( pasteModeGroup, SWT.RADIO);
    	pasteReference.setText( "use variable reference");
    	pasteReference.setToolTipText( "version expressions of dependencies are replaced by variable references");
    	pasteReference.setLayoutData(new GridData( SWT.FILL, SWT.FILL, false, false, 1, 1));
    	
    	// activate
    	VersionModificationAction vmaPaste;
    	Optional<VersionModificationAction> vmaPasteOptional = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_CLIP_PASTE_MODE); 
    	if (!vmaPasteOptional.isPresent()) 
    		vmaPaste = VersionModificationAction.referenced;
    	else 
    		vmaPaste = vmaPasteOptional.get();
    	
    	switch ( vmaPaste) {
			case rangified:
				pasteRangified.setSelection( true);
				break;
			case referenced:
				pasteReference.setSelection( true);
				break;
			case untouched:		
			default:
				pasteUntouched.setSelection( true);
				break;    	
    	}
    	
    	Composite qiGroup = new Composite( composite, SWT.NONE);
		qiGroup.setLayout( layout);
		qiGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label qiLabel = new Label( qiGroup, SWT.NONE);
		qiLabel.setText( "Quick Import options");
    	qiLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	qiLabel.setFont(bigFont);
    	
  
    	// Either show all artifacts scanned or filter them according whether they exist in the workspace     	
    	boolean auin = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_QI_SHOW_ALL, false);
    	
    	qiShowAllArtifacts = new BooleanEditor();
    	qiShowAllArtifacts.setSelection( auin);  
    	qiShowAllArtifacts.setLabelToolTip("Changes the way the result of the query are pre-filtered");
    	qiShowAllArtifacts.setCheckToolTip( "If set, no filtering takes place and the dialog will show you what actually can be imported. Otherwise, what you see is what you can import");
    	Composite uiComposite = qiShowAllArtifacts.createControl(qiGroup, "Show all scanned artifacts, do not filter-out non-importable: ");
    	uiComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
    	
    	// allow projects that do exist in the workspace, but not in the currently active working set 
    	
    	boolean fws = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_FILTER_WSET, false);    	
    	qiFilterOnWorkingSet = new BooleanEditor();
    	qiFilterOnWorkingSet.setSelection( fws);
    	qiFilterOnWorkingSet.setLabelToolTip("Changes the way the result of the query is filtered");
    	qiFilterOnWorkingSet.setCheckToolTip("If set, projects that exist in the workspace, yet not in the current working-set are not filtered. Otherwise, they are filtered out and not shown");
    	
    	Composite filterComposite = qiFilterOnWorkingSet.createControl(qiGroup, "Allow selection of loaded project if a working set is selected: ");
    	filterComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		composite.pack();
		return composite;
	}


	@Override
	public void dispose() {		
		addImage.dispose();
		editImage.dispose();
		removeImage.dispose();
		rebuildIndexImage.dispose();
		bigFont.dispose();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event) {
		widgetSelected(event);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == addButton) {
			RepositoryPairingDialog pairingDialog = new RepositoryPairingDialog( getShell());				
			pairingDialog.open();
			SourceRepositoryEntry pairing = pairingDialog.getSelection();
			if (pairing != null) {
				entries.add( pairing);
				pairingDialog.setPairings( entries);
				repositoryTableViewer.setInput( entries);
				repositoryTableViewer.refresh();
			}
		}
		else if (event.widget == removeButton) {
			if (!isEditableSelection()) 
				return;
			
			// remove 
			int [] indices = repositoryTable.getSelectionIndices();
			for (int i = 0; i < indices.length; i++) {
				SourceRepositoryEntry entry = entries.get( indices[i]);
				if (!entry.getEditable()) {
					continue;
				}			
				entries.remove(entry);				
				repositoryTableViewer.setInput( entries);
				repositoryTableViewer.refresh();
			}
			
			
		}
		else if (event.widget == editButton) {
			if (!isEditableSelection()) 
				return;
			RepositoryPairingDialog pairingDialog = new RepositoryPairingDialog( getShell());
			// prime with selected pairing
			int index = repositoryTable.getSelectionIndex();
			if (index >= 0) {
				List<SourceRepositoryEntry> currentEntries = (List<SourceRepositoryEntry>) repositoryTableViewer.getInput();
				SourceRepositoryEntry entryToEdit = currentEntries.get(index);
				pairingDialog.setSelection( entryToEdit);
				pairingDialog.setPairings(currentEntries);
				pairingDialog.open();					
				SourceRepositoryEntry editedEntry = pairingDialog.getSelection();
				entries.remove( entryToEdit);
				entries.add( editedEntry);
				repositoryTableViewer.setInput( entries);
				repositoryTableViewer.refresh();				
			}
		}
		else if (event.widget == repositoryTable) {
			switchButtonsAccordingSelection();
		}
		else if (event.widget == buildIndexButton) {
			// rescan - 			
			DevrockPlugin.instance().quickImportController().rescan( entries);
		}
	}

	
	private void switchButtonsAccordingSelection() {
		if (!isEditableSelection()) {
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			buildIndexButton.setEnabled( false);
		}
		else {
			editButton.setEnabled(true);
			removeButton.setEnabled(true);
			buildIndexButton.setEnabled( true);			
		}				
	}

	private boolean isEditableSelection() {
		boolean isEditable = true;
		int [] indices = repositoryTable.getSelectionIndices();
		if (indices == null || indices.length == 0) {
			return false;
		}
		
		for (int i = 0; i < indices.length; i++) {
			SourceRepositoryEntry entry = entries.get( indices[i]);
			if (!entry.getEditable()) {
				isEditable = false;
			}				
		}
		return isEditable;
	}
	
	private void saveToLocker() {
		List<SourceRepositoryEntry> workspaceSourceEntries = entries.stream().filter( e -> e.getEditable()).collect(Collectors.toList());
		BasicStorageLocker storageLocker = DevrockPlugin.envBridge().storageLocker();
		storageLocker.setValue(StorageLockerSlots.SLOT_SCAN_DIRECTORIES, workspaceSourceEntries);
		
		// determine currently selected values for COPY_MODE
		VersionModificationAction copyAction = VersionModificationAction.referenced;
		if (copyRangified.getSelection()) {
			copyAction = VersionModificationAction.rangified;
		}
		else if (copyUntouched.getSelection()) {
			copyAction = VersionModificationAction.untouched;
		}
		else {
			copyAction = VersionModificationAction.referenced;
		}
		storageLocker.setValue(StorageLockerSlots.SLOT_CLIP_COPY_MODE, copyAction);
		
		// determine currently selected values for COPY_MODE
		VersionModificationAction pasteAction = VersionModificationAction.referenced;
		if (pasteRangified.getSelection()) {
			pasteAction = VersionModificationAction.rangified;
		}
		else if (pasteUntouched.getSelection()) {
			pasteAction = VersionModificationAction.untouched;
		}
		else {
			pasteAction = VersionModificationAction.referenced;
		}			
		storageLocker.setValue(StorageLockerSlots.SLOT_CLIP_PASTE_MODE, pasteAction);
		
		// do not filter artifacts that can't be imported
		storageLocker.setValue(StorageLockerSlots.SLOT_QI_SHOW_ALL, qiShowAllArtifacts.getSelection());
		// allow duplicate imports if target exists in a different working set
		storageLocker.setValue(StorageLockerSlots.SLOT_FILTER_WSET, qiFilterOnWorkingSet.getSelection());
				
	}

	@Override
	protected void performApply() {
		saveToLocker();
		// auto scan
		qiController.rescan(); 
		super.performApply();
	}

	@Override
	public boolean performOk() {
		saveToLocker();
		// auto scan
		qiController.rescan();
		return super.performOk();
	}
	
	
	
}
