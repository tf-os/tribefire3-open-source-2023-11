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
package com.braintribe.devrock.preferences.pages.main;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.api.ui.editors.IntegerEditor;
import com.braintribe.devrock.bridge.eclipse.environment.BasicStorageLocker;
import com.braintribe.devrock.commands.ImportWorkspacePopulation;
import com.braintribe.devrock.commands.RepositoryConfigurationInfoCommand;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.devrock.plugin.DevrockPluginStatus;

public class DevrockTitlePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DEVROCK_PREFERENCES = "Devrock's Eclipse support";

	private final Image workspaceImportImage;
	private Font bigFont;
	private BooleanEditor activateAutoWorkspaceUpdate;
	private BooleanEditor selectiveWorkspaceUpdate;	
	private BooleanEditor requireHigherVersionInFlexibleAssignment;
	private IntegerEditor maxResultInRemoteDependencyImport;

	private BooleanEditor modelBuilderMessages;

	
	public DevrockTitlePage() {
		setDescription(DEVROCK_PREFERENCES);
		
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromFile( DevrockTitlePage.class, "push.png");
		workspaceImportImage = imageDescriptor.createImage();		
	}

	@Override
	public void init(IWorkbench arg0) {
		// NO OP
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
		

		// TODO: perhaps a view of the loaded Devrock plugin?
		Composite choicesComposite = new Composite( composite, SWT.NONE);
		choicesComposite.setLayout(layout);
		choicesComposite.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, true, 4, 1));
		
		Label choicesLabel = new Label( choicesComposite, SWT.NONE);
		choicesLabel.setText("Miscellaneous choices");
		choicesLabel.setFont(bigFont);
		choicesLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		
		// AC stuff 
		activateAutoWorkspaceUpdate = new BooleanEditor();
		activateAutoWorkspaceUpdate.setLabelToolTip( "Changes the behavior of the workspace resource change listener");
		activateAutoWorkspaceUpdate.setCheckToolTip( "If checked, the containers will react to changes of the workspace, otherwise manual synchronizing is required");
		Composite control = activateAutoWorkspaceUpdate.createControl(choicesComposite, "Automatically update containers on detected changes in workspace");
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean autoUpdate = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_AUTO_UPDATE_WS, true);
		activateAutoWorkspaceUpdate.setSelection( autoUpdate);
		
		selectiveWorkspaceUpdate = new BooleanEditor();
		selectiveWorkspaceUpdate.setLabelToolTip("Changes the behavior of the default workspace sync");
		selectiveWorkspaceUpdate.setCheckToolTip( "If checked and projects are selected, it will sync these. Otherwise it will sync the workspace");
		control = selectiveWorkspaceUpdate.createControl(choicesComposite, "Selective workspace synchronization");		
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean selectivity = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_SELECTIVE_WS_SYNCH, false);
		selectiveWorkspaceUpdate.setSelection( selectivity);
		
		//requireHigherVersionInFlexibleAssignment
		
		requireHigherVersionInFlexibleAssignment = new BooleanEditor();
		requireHigherVersionInFlexibleAssignment.setLabelToolTip("Changes how non-matching projects of dependencies in debug-module projects are handled");
		requireHigherVersionInFlexibleAssignment.setCheckToolTip( "If checked, the project must have at least the same version as requested. Otherwise it only has to match a derived standard range");
		control = requireHigherVersionInFlexibleAssignment.createControl(choicesComposite, "Strict version interpretation for debug-module dependency-projects");		
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean requireHigherVersion = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_AC_REQUIRE_HIGHER_VERSION, false);
		requireHigherVersionInFlexibleAssignment.setSelection( requireHigherVersion);
						
		// dependency importer
		maxResultInRemoteDependencyImport = new IntegerEditor();
		maxResultInRemoteDependencyImport.setLabelToolTip( "The maximum number of hits shown in the 'Quick Remote Dependency Import'");		
		control = maxResultInRemoteDependencyImport.createControl(choicesComposite, "Maximum artifacts in dependency selection dialog");
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		int maxResult = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_MAX_RESULT, 100);
		maxResultInRemoteDependencyImport.setSelection(maxResult);
		
		
		// model builder messages
		modelBuilderMessages = new BooleanEditor();
		modelBuilderMessages.setLabelToolTip("Changes whether the model-builder reports success");
		modelBuilderMessages.setCheckToolTip( "If checked, the model-builder will report success after buikd, otherwise it will report only errors");
		control = modelBuilderMessages.createControl(choicesComposite, "Show success messages of the model-builder plugin");		
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean successMessages = DevrockPlugin.envBridge().storageLocker().getValue( StorageLockerSlots.SLOT_MB_SUCCESS_MESSAGES, false);
		modelBuilderMessages.setSelection( successMessages);
		
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		if (projects == null || projects.length == 0) {
			Composite emptyWorkspaceFeaturesComposite = new Composite( composite, SWT.NONE);
			emptyWorkspaceFeaturesComposite.setLayout(layout);
			emptyWorkspaceFeaturesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
			
			// analysis features 
			Label emptyWorkspaceLabel = new Label( emptyWorkspaceFeaturesComposite, SWT.NONE);
			emptyWorkspaceLabel.setText("Features that are useful in an empty workspace");
			emptyWorkspaceLabel.setFont(bigFont);
			emptyWorkspaceLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
			
			Button showCurrentRepoCfg = new Button(emptyWorkspaceFeaturesComposite, SWT.NONE);
			showCurrentRepoCfg.setText("Compile and show current repository-configuration");
			showCurrentRepoCfg.setToolTipText( "Retrieves, compiles and shows the repository-configuration currently active in this workspace");
			showCurrentRepoCfg.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
			
			showCurrentRepoCfg.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					RepositoryConfigurationInfoCommand rcic = new RepositoryConfigurationInfoCommand();
					try {
						rcic.execute(null);
					} catch (ExecutionException e1) {					
						DevrockPluginStatus status = new DevrockPluginStatus("Cannot run the repository-configuration-view command", e1);
						DevrockPlugin.instance().log(status);
					}
				}
				
			});
						
			
			Button workspaceImporter = new Button(emptyWorkspaceFeaturesComposite, SWT.NONE);
			workspaceImporter.setImage(workspaceImportImage);
			workspaceImporter.setText("Workspace import");
			workspaceImporter.setToolTipText( "Allows to restore a previously extracted workspace");
			workspaceImporter.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
			workspaceImporter.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					ImportWorkspacePopulation iwp = new ImportWorkspacePopulation();
					try {
						iwp.execute(null);
					} catch (ExecutionException e1) {						
						DevrockPluginStatus status = new DevrockPluginStatus("Cannot run the workspace-import command", e1);
						DevrockPlugin.instance().log(status);						
					}
				}
				
			});

			
		}
		
		
		composite.pack();
		return composite;
	}


	@Override
	public void dispose() {
		bigFont.dispose();
		workspaceImportImage.dispose();
	}
	
	private void saveToLocker() {
		BasicStorageLocker storageLocker = DevrockPlugin.envBridge().storageLocker();

		boolean activateAutoUpdate = activateAutoWorkspaceUpdate.getSelection();
		storageLocker.setValue(StorageLockerSlots.SLOT_AUTO_UPDATE_WS, activateAutoUpdate);
		
		boolean selectiveUpdate = selectiveWorkspaceUpdate.getSelection();
		storageLocker.setValue(StorageLockerSlots.SLOT_SELECTIVE_WS_SYNCH, selectiveUpdate);
	
		boolean requireHigherVersion = requireHigherVersionInFlexibleAssignment.getSelection();
		storageLocker.setValue(StorageLockerSlots.SLOT_AC_REQUIRE_HIGHER_VERSION, requireHigherVersion);
		
		boolean successMessages = modelBuilderMessages.getSelection();
		storageLocker.setValue( StorageLockerSlots.SLOT_MB_SUCCESS_MESSAGES, successMessages);
						
		Integer maxResult = maxResultInRemoteDependencyImport.getSelection();
		if (maxResult != null) {
			storageLocker.setValue(StorageLockerSlots.SLOT_MAX_RESULT, maxResult);	
		}
	}

	@Override
	protected void performApply() {
		saveToLocker();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		saveToLocker();
		return super.performOk();
	}

	
	
}
