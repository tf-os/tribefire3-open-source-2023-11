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
package com.braintribe.devrock.preferences.pages.experimental;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.bridge.eclipse.environment.BasicStorageLocker;
import com.braintribe.devrock.plugin.DevrockPlugin;

public class ExperimentalPage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DEVROCK_PREFERENCES = "Devrock's experimental features.\n\nThey change the behavior or the look and feel of some features of the plugins in some respect, but are not final yet as they are still in development.";

	private Font bigFont;
	private BooleanEditor useTfNatureProjectIcons;
	private BooleanEditor useTfNatureProjectBackground;
	private BooleanEditor useSelectiveWorkspaceImport;
	private BooleanEditor useSelectiveWorkspaceExport;
	private BooleanEditor allowZedToPurgeExcessDependencies;

	private BooleanEditor addStorageLockerDataToWorkspaceExport;
	
	public ExperimentalPage() {
		setDescription(DEVROCK_PREFERENCES);				
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
		
	
		// ui choices 
		Composite uiChoicesComposite = new Composite( composite, SWT.NONE);
		uiChoicesComposite.setLayout(layout);
		uiChoicesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		Label uiChoicesLabel = new Label( uiChoicesComposite, SWT.NONE);
		uiChoicesLabel.setText("Experimental UI choices");
		uiChoicesLabel.setFont(bigFont);
		uiChoicesLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		 
	
		// Project icon + background decoration
		useTfNatureProjectIcons = new BooleanEditor();
		useTfNatureProjectIcons.setLabelToolTip(
				"Some TF projects (models, modules, apis) can have a nature-specific icon.\n\nProject REFRESH NEEDED to see the effect.");
		Composite control = useTfNatureProjectIcons.createControl(uiChoicesComposite, "Use TF nature based project icons");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean useTfNatureIcons = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_TF_NATURE_PROJECT_ICONS, false);
		useTfNatureProjectIcons.setSelection(useTfNatureIcons);

		useTfNatureProjectBackground = new BooleanEditor();
		useTfNatureProjectBackground.setLabelToolTip("Some TF projects (models, modules, apis) can have their project name highlighted with a "
				+ "nature-specific background color.\n\nProject REFRESH NEEDED to see the effect.");
		control = useTfNatureProjectBackground.createControl(uiChoicesComposite, "Use TF nature based project name highlighting");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean useTfNatureBackground = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_TF_NATURE_PROJECT_BACKGROUND,
				false);
		useTfNatureProjectBackground.setSelection(useTfNatureBackground);
					
		
		// ws import choices 
		Composite wsChoicesComposite = new Composite( composite, SWT.NONE);
		wsChoicesComposite.setLayout(layout);
		wsChoicesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		Label wsChoicesLabel = new Label( wsChoicesComposite, SWT.NONE);
		wsChoicesLabel.setText("Experimental workspace export/import choices");
		wsChoicesLabel.setFont(bigFont);
		wsChoicesLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));		 
		
		useSelectiveWorkspaceExport = new BooleanEditor();
		useSelectiveWorkspaceExport.setLabelToolTip( "Choose whether to use the selective workspace exporter");
		useSelectiveWorkspaceExport.setCheckToolTip("If activated, the dump will export the selected workspace items.\nOtherwise, the full content of the workspace is exported");
		control = useSelectiveWorkspaceExport.createControl(wsChoicesComposite, "Activate selective workspace exporter");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean usSelectiveWsExportValue = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_USE_SELECTIVE_EXPORT, false);
		useSelectiveWorkspaceExport.setSelection(usSelectiveWsExportValue);
		
		
		
		addStorageLockerDataToWorkspaceExport = new BooleanEditor();
		addStorageLockerDataToWorkspaceExport.setLabelToolTip( "Choose whether to add the plugin preferences to the export");
		addStorageLockerDataToWorkspaceExport.setCheckToolTip("If activated, the dump will also contain the current preferences.\nOtherwise, only projects and working-sets are exported");
		control = addStorageLockerDataToWorkspaceExport.createControl(wsChoicesComposite, "Include plugin preferenes to export");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean addSldToExportValue = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_INCLUDE_STORAGE_LOCKER_DATA, false);
		addStorageLockerDataToWorkspaceExport.setSelection(addSldToExportValue);
		
		
	
		// selective import
		useSelectiveWorkspaceImport = new BooleanEditor();
		useSelectiveWorkspaceImport.setLabelToolTip( "Choose whether to use the selective workspace importer");
		useSelectiveWorkspaceImport.setCheckToolTip("If activated, the content of the dumped workspace is shown and content can be selected.\nOtherwise, the full content of the selected file is imported");
		control = useSelectiveWorkspaceImport.createControl(wsChoicesComposite, "Activate selective workspace importer");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean usSelectiveWsImportValue = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_WS_IMPORT_USE_SELECTIVE_IMPORT, false);
		useSelectiveWorkspaceImport.setSelection(usSelectiveWsImportValue);

		// zed choices 
		Composite zedChoicesComposite = new Composite( composite, SWT.NONE);
		zedChoicesComposite.setLayout(layout);
		zedChoicesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		Label zedChoicesLabel = new Label( zedChoicesComposite, SWT.NONE);
		zedChoicesLabel.setText("Experimental choices for zed");
		zedChoicesLabel.setFont(bigFont);
		zedChoicesLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		 		
		allowZedToPurgeExcessDependencies = new BooleanEditor();
		allowZedToPurgeExcessDependencies.setLabelToolTip( "Choose whether to allow zed's dependency analyis UI to delete excessive depedencies");
		allowZedToPurgeExcessDependencies.setCheckToolTip("If activated, zed will try to delete dependencies it deems to be excessive.\nAs Zed's still in construction and can make mistakes by omission, it may render the artifact non-buildable\nStill, in that case, you can revert to the .bak-file created");
		control = allowZedToPurgeExcessDependencies.createControl(zedChoicesComposite, "Allow zed to purge excessive dependencies from a project");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean zedAlloPurgeValue = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ZED_ALLOW_PURGE, false);
		allowZedToPurgeExcessDependencies.setSelection(zedAlloPurgeValue);
		
		composite.pack();
		return composite;
	}


	@Override
	public void dispose() {
		bigFont.dispose();	
	}
	
	private void saveToLocker() {
		BasicStorageLocker storageLocker = DevrockPlugin.envBridge().storageLocker();
		
		// ui choices
		storageLocker.setValue(StorageLockerSlots.SLOT_TF_NATURE_PROJECT_ICONS, useTfNatureProjectIcons.getSelection());
		storageLocker.setValue(StorageLockerSlots.SLOT_TF_NATURE_PROJECT_BACKGROUND, useTfNatureProjectBackground.getSelection());
						
		// ws import/export choices 
		storageLocker.setValue(StorageLockerSlots.SLOT_WS_IMPORT_USE_SELECTIVE_EXPORT, useSelectiveWorkspaceExport.getSelection());
		storageLocker.setValue(StorageLockerSlots.SLOT_WS_IMPORT_USE_SELECTIVE_IMPORT, useSelectiveWorkspaceImport.getSelection());
		storageLocker.setValue(StorageLockerSlots.SLOT_WS_IMPORT_INCLUDE_STORAGE_LOCKER_DATA, addStorageLockerDataToWorkspaceExport.getSelection());
		
		// zed 
		storageLocker.setValue(StorageLockerSlots.SLOT_ZED_ALLOW_PURGE, allowZedToPurgeExcessDependencies.getSelection());
		DevrockPlugin.instance().broadcastPreferencesChanged();
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
