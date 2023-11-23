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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.QuickImportPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.SvnPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.project.validator.ProjectSettingsValidator;
import com.braintribe.devrock.artifactcontainer.quickImport.QuickImportControl;
import com.braintribe.devrock.artifactcontainer.validator.ArtifactContainerPluginValidatorDialog;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.VersionModificationAction;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SourceRepositoryPairing;
import com.braintribe.model.malaclypse.cfg.preferences.svn.SvnPreferences;
import com.braintribe.plugin.commons.preferences.BooleanEditor;
import com.braintribe.plugin.commons.preferences.DirectoryEditor;
import com.braintribe.plugin.commons.preferences.listener.ModificationNotificationListener;
import com.braintribe.plugin.commons.preferences.validator.SettingsValidator;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;
import com.braintribe.plugin.commons.selection.PantherSelectionHelper;
import com.braintribe.plugin.commons.selection.SelectionException;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;


public class ProjectPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, VirtualEnvironmentNotificationListener, ModificationNotificationListener {	
	protected Font bigFont;	
	private BooleanEditor qiAlternativeUiEditor;
	private BooleanEditor qiLocalImportModeEditor;
//	private BooleanEditor qiAttachToCurrentProject;
	private BooleanEditor qiFilterOnWorkingSet;
	private BooleanEditor qiPrimeWithSelection;

	private SvnPreferences svnPreferences;
	private QuickImportPreferences quickImportPreferences;
	private List<SourceRepositoryPairing> pairings;
	private ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
	
	private DirectoryEditor currentWorkingCopyDirectoryEditor;
	
	private CommonTableViewer repositoryTableViewer;
	private Table repositoryTable;
	private Button addPairingButton;
	private Button editPairingButton;
	private Button removePairingButton;
	private Button buildIndexButton;
	
	private Button copyUntouched;
	private Button copyRangified;
	private Button copyReference;
	
	private Button pasteUntouched;
	private Button pasteRangified;
	private Button pasteReference;
	
	
	private Image addImage;
	private Image editImage;
	private Image removeImage;
	private Image rebuildIndexImage;
	
	
	public ProjectPreferencesPage() {
		super();
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( ProjectPreferencesPage.class, "add.gif");
		addImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ProjectPreferencesPage.class, "editconfig.gif");
		editImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ProjectPreferencesPage.class, "remove.gif");
		removeImage = imageDescriptor.createImage();
		
		imageDescriptor = ImageDescriptor.createFromFile( ProjectPreferencesPage.class, "rebuild_index.gif");
		rebuildIndexImage = imageDescriptor.createImage();
		
	}


	@Override
	public void init(IWorkbench arg0) {		
		try {
			PantherSelectionHelper.primeRepositoryInformation();
		} catch (SelectionException e) {
			;
		}
		svnPreferences = plugin.getArtifactContainerPreferences(false).getSvnPreferences();
		pairings = new ArrayList<SourceRepositoryPairing>(svnPreferences.getSourceRepositoryPairings());
		quickImportPreferences = plugin.getArtifactContainerPreferences(false).getQuickImportPreferences();
		VirtualEnvironmentPlugin.getInstance().addListener(this);
	}

	
	@Override
	public void dispose() {
		VirtualEnvironmentPlugin virtualEnvironmentPlugin = VirtualEnvironmentPlugin.getInstance();
		virtualEnvironmentPlugin.removeListener( currentWorkingCopyDirectoryEditor);
		virtualEnvironmentPlugin.removeListener(this);
		
		addImage.dispose();
		editImage.dispose();
		removeImage.dispose();
		rebuildIndexImage.dispose();
		
		super.dispose();
	}


	@Override
	protected Control createContents(Composite parent) {
		
		
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout( layout);				
		
		Composite svnGroup = new Composite( composite, SWT.NONE);
		svnGroup.setLayout( layout);
		svnGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
	
		Label svnLabel = new Label( svnGroup, SWT.NONE);
    	svnLabel.setText( "Source repository pairings");
    	svnLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	svnLabel.setFont(bigFont);
    	    	
    	
    	Composite repositoryComposite = new Composite( svnGroup, SWT.NONE);
    	GridLayout repoCompositelayout = new GridLayout();
    	repoCompositelayout.numColumns = 2;
    	repositoryComposite.setLayout( repoCompositelayout);
    	repositoryComposite.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, true, 4, 1));
    	
    	Composite tableComposite = new Composite(repositoryComposite, SWT.NONE);	
		tableComposite.setLayout( new FillLayout());
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1));
		
    	repositoryTableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columnData = new CommonTableColumnData[4];
		columnData[0] = new CommonTableColumnData("name", 100, 100, "Repository name", new NameColumnLabelProvider());
		columnData[1] = new CommonTableColumnData("local", 200, 200, "Working copy directory", new PathColumnLabelProvider(true));
		columnData[2] = new CommonTableColumnData("url", 200, 200, "Remote URL", new PathColumnLabelProvider(false));		
		columnData[3] = new CommonTableColumnData("kind", 50, 50, "Kind of source repository", new RepositoryKindColumnLabelProvider());
		
		repositoryTableViewer.setup(columnData);
		
		repositoryTable = repositoryTableViewer.getTable();
		repositoryTable.setHeaderVisible(true);
		repositoryTable.setLinesVisible(true);
		
		
	
		RepositoryContentProvider contentProvider = new RepositoryContentProvider();    
		contentProvider.setPairings( pairings);
		repositoryTableViewer.setContentProvider(contentProvider);
		repositoryTableViewer.setInput( pairings);
		
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true);
    	int ht = (repositoryTable.getItemHeight() * 15) + repositoryTable.getHeaderHeight();
    	layoutData.heightHint = repositoryTable.computeSize(SWT.DEFAULT, ht).y;
    	repositoryTable.setLayoutData( layoutData);

    	Composite buttonComposite = new Composite( repositoryComposite, SWT.NONE);

		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonComposite.setLayout( buttonLayout);
		buttonComposite.setLayoutData( new GridData( SWT.FILL, SWT.LEFT, true, false, 1,1));
		
		
		// add button
		addPairingButton = new Button( buttonComposite, SWT.NONE);
		addPairingButton.setImage(addImage);
		addPairingButton.setToolTipText( "add a new repository");
		addPairingButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		addPairingButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				RepositoryPairingDialog pairingDialog = new RepositoryPairingDialog( getShell());				
				pairingDialog.open();
				SourceRepositoryPairing pairing = pairingDialog.getSelection();
				if (pairing != null) {
					pairings.add( pairing);
					pairingDialog.setPairings(pairings);
					repositoryTableViewer.setInput(pairings);
					repositoryTableViewer.refresh();
				}
			}
			
		});
		
		
		// edit button
		editPairingButton = new Button( buttonComposite, SWT.NONE);
		editPairingButton.setImage(editImage);
		editPairingButton.setToolTipText( "edit the selected repository");
		editPairingButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		editPairingButton.addSelectionListener( new SelectionAdapter() {

			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				RepositoryPairingDialog pairingDialog = new RepositoryPairingDialog( getShell());
				// prime with selected pairing
				int index = repositoryTable.getSelectionIndex();
				if (index >= 0) {
					pairingDialog.setSelection( ((List<SourceRepositoryPairing>) repositoryTableViewer.getInput()).get(index));
					pairingDialog.setPairings(pairings);
					pairingDialog.open();					
					repositoryTableViewer.refresh();
					
				}
			}			
		});
		
		// edit button
		buildIndexButton = new Button( buttonComposite, SWT.NONE);
		buildIndexButton.setImage( rebuildIndexImage);
		buildIndexButton.setToolTipText( "scan the selected repository");
		buildIndexButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 1,1));
		buildIndexButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = repositoryTable.getSelectionIndex();
				if (index >= 0) {
					@SuppressWarnings("unchecked")
					SourceRepositoryPairing pairing = ((List<SourceRepositoryPairing>) repositoryTableViewer.getInput()).get(index);
					
					QuickImportControl quickImportScanController = plugin.getQuickImportScanController();					
					quickImportScanController.rescan(pairing);				
				}
			}			
		});

		
		// remove button 
		removePairingButton = new Button( buttonComposite, SWT.NONE);
		removePairingButton.setToolTipText( "remove the selected repository");
		removePairingButton.setImage(removeImage);
		removePairingButton.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		removePairingButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = repositoryTable.getSelectionIndex();
				if (index >= 0) {
					pairings.remove( index);
					repositoryTableViewer.setInput(pairings);
					repositoryTableViewer.refresh();
				}
			}
			
		});
		
		removePairingButton.setEnabled( false);
		editPairingButton.setEnabled( false);
		buildIndexButton.setEnabled( false);
		
		repositoryTable.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (repositoryTable.getSelectionIndex() >= 0) {
					removePairingButton.setEnabled( true);
					editPairingButton.setEnabled( true);
					buildIndexButton.setEnabled( true);
				}
				else {
					removePairingButton.setEnabled( false);
					editPairingButton.setEnabled( false);
					buildIndexButton.setEnabled( false);
				}
				super.widgetSelected(e);
			}
			
		});
		
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
    	
    	// activate
    	switch ( plugin.getArtifactContainerPreferences(false).getQuickImportPreferences().getLastDependencyCopyMode()) {
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
    	switch ( plugin.getArtifactContainerPreferences(false).getQuickImportPreferences().getLastDependencyPasteMode()) {
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
    	
  
    	qiAlternativeUiEditor = new BooleanEditor();
    	qiAlternativeUiEditor.setSelection( quickImportPreferences.getAlternativeUiNature());
    	Composite uiComposite = qiAlternativeUiEditor.createControl(qiGroup, "Alternative UI style for &Quick Import dialog: ");
    	uiComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
    	
    	qiLocalImportModeEditor = new BooleanEditor();
    	qiLocalImportModeEditor.setSelection( quickImportPreferences.getLocalOnlyNature());
    	Composite lmComposite = qiLocalImportModeEditor.createControl(qiGroup, "Use local scan for &Quick Import dialog: ");
    	lmComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
    	qiLocalImportModeEditor.setEnabled(false);
     
    	qiFilterOnWorkingSet = new BooleanEditor();
    	qiFilterOnWorkingSet.setSelection( quickImportPreferences.getFilterOnWorkingSet());
    	Composite filterComposite = qiFilterOnWorkingSet.createControl(qiGroup, "Allow selection of loaded project if a working set's selected: ");
    	filterComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));

     	
    	qiPrimeWithSelection = new BooleanEditor();
    	qiPrimeWithSelection.setSelection( quickImportPreferences.getPrimeWithSelection());
    	Composite acComposite = qiPrimeWithSelection.createControl(qiGroup, "Prime Quick Import dialog with current package explorer selection: ");
    	acComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
      	
   
		return composite;
	}

	
	@Override
	public void acknowledgeChange(Object sender, String value) {			
	}
	


	@Override
	public void acknowledgeOverrideChange() {		
	}


	@Override
	public boolean okToLeave() {
		
		SettingsValidator validator = new ProjectSettingsValidator( pairings);		
		ValidationResult result = validator.validate();
		if (result.getValidationState() == false) {
			
			ArtifactContainerPluginValidatorDialog dlg = new ArtifactContainerPluginValidatorDialog(getShell());
			dlg.setResultsToDisplay( Collections.singletonList( result));
			dlg.open();			
			return false;
		}
		else {
			return super.okToLeave();						
		}		
	}


	@Override
	protected void performApply() {
		// 		
		if (!okToLeave())
			return;
		// pairings are linked.
		svnPreferences.setSourceRepositoryPairings(pairings);
		
		quickImportPreferences.setLocalOnlyNature( qiLocalImportModeEditor.getSelection());
		quickImportPreferences.setAlternativeUiNature( qiAlternativeUiEditor.getSelection());
		
		if (copyRangified.getSelection()) {
			quickImportPreferences.setLastDependencyCopyMode( VersionModificationAction.rangified);
		}
		else if (copyReference.getSelection()) {
			quickImportPreferences.setLastDependencyCopyMode( VersionModificationAction.referenced);
		}
		else {
			quickImportPreferences.setLastDependencyCopyMode( VersionModificationAction.untouched);
		}
	
		if (pasteRangified.getSelection()) {
			quickImportPreferences.setLastDependencyPasteMode( VersionModificationAction.rangified);
		}
		else if (pasteReference.getSelection()) {
			quickImportPreferences.setLastDependencyPasteMode( VersionModificationAction.referenced);
		}
		else {
			quickImportPreferences.setLastDependencyPasteMode( VersionModificationAction.untouched);
		}
		
		
		quickImportPreferences.setPrimeWithSelection(qiPrimeWithSelection.getSelection());
		quickImportPreferences.setAttachToCurrentProject( false);
		quickImportPreferences.setFilterOnWorkingSet(qiFilterOnWorkingSet.getSelection());
		
		try {
			new QuickImportPreferencesCodec( plugin.getPreferenceStore()).decode(quickImportPreferences);
			new SvnPreferencesCodec( plugin.getPreferenceStore()).decode(svnPreferences);
		} catch (CodecException e) {
			String msg="cannot write preferences to IPreferencesStore";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
			
		}
	}

	
	

	@Override
	protected void performDefaults() {
		pairings = new ArrayList<SourceRepositoryPairing>(svnPreferences.getSourceRepositoryPairings());
		qiLocalImportModeEditor.setSelection( quickImportPreferences.getLocalOnlyNature());
		qiAlternativeUiEditor.setSelection( quickImportPreferences.getAlternativeUiNature());
		qiPrimeWithSelection.setSelection( quickImportPreferences.getPrimeWithSelection());
		qiFilterOnWorkingSet.setSelection(quickImportPreferences.getFilterOnWorkingSet());
		// 
		// 
		repositoryTableViewer.setInput(pairings);
		repositoryTableViewer.refresh();
		super.performDefaults();
	}


	@Override
	public boolean performCancel() {		
		performDefaults();
		return super.performCancel();
	}


	@Override
	public boolean performOk() {
		if (okToLeave() == false) {			
			return false;
		}
		performApply();
		plugin.getQuickImportScanController().setup();
		return super.performOk();
	}
	
	
	
	
}
