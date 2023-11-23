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
package com.braintribe.devrock.preferences.pages.analysis;

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
import com.braintribe.devrock.api.ui.editors.ColorEditor;
import com.braintribe.devrock.api.ui.editors.FileEditor;
import com.braintribe.devrock.bridge.eclipse.environment.BasicStorageLocker;
import com.braintribe.devrock.eclipse.model.storage.Color;
import com.braintribe.devrock.plugin.DevrockPlugin;

public class AnalysisPage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DEVROCK_PREFERENCES = "Devrock's analysis features";

	private Font bigFont;
	
	private BooleanEditor activateResolutionViewerYaml;
	private BooleanEditor storeSettingsOfResolutionViewer;
	private BooleanEditor terminalTabInitial;
	
	private ColorEditor artifactAxisColorEditor;	
	private ColorEditor parentAxisColorEditor;
	private ColorEditor importAxisColorEditor;
	private ColorEditor standardVersionColorEditor;
	private ColorEditor pcVersionColorEditor;
	
	private FileEditor zedFingerprintOverridesFileEditor;

	private BooleanEditor zedOverrideFingerPrintsEditor;
	private BooleanEditor zedInitialModelViewModeEditor;
	
	public AnalysisPage() {
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
		
		// viewer
		Composite viewerChoicesComposite = new Composite( composite, SWT.NONE);
		viewerChoicesComposite.setLayout(layout);
		viewerChoicesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		Label viewerChoicesLabel = new Label( viewerChoicesComposite, SWT.NONE);
		viewerChoicesLabel.setText("Choices for the resolution viewer");
		viewerChoicesLabel.setFont(bigFont);
		viewerChoicesLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		
		
		// resolution viewer 
		activateResolutionViewerYaml = new BooleanEditor();
		activateResolutionViewerYaml.setLabelToolTip( "Add a YAML display tab to the resolution viewer - careful : big files put a heavy tax on Eclipse!");
		activateResolutionViewerYaml.setCheckToolTip( "If checked, the resolution viewer shows the YAML of the resolution as a tab (you were warned)");
		Composite control = activateResolutionViewerYaml.createControl(viewerChoicesComposite, "Add a YAML display tab to the resolution viewer (for powerful computers only)");
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean enableViewerButton = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_YAML_ENABLED, false);
		activateResolutionViewerYaml.setSelection(enableViewerButton);
		
		storeSettingsOfResolutionViewer = new BooleanEditor();
		storeSettingsOfResolutionViewer.setLabelToolTip( "Whether remember your last choices of view possiblities in the resolution viewer");
		storeSettingsOfResolutionViewer.setCheckToolTip( "If checked, the resolution viewer will remember your view choices for each tab (plus one for all detail tabs)");
		control = storeSettingsOfResolutionViewer.createControl(viewerChoicesComposite, "Store view settings on resolution viewer tabs");
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean storeSettings = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_STORE_VIEW_SETTINGS, false);
		storeSettingsOfResolutionViewer.setSelection(storeSettings);
		
		terminalTabInitial = new BooleanEditor();
		terminalTabInitial.setLabelToolTip( "Whether to initially open the terminal-tab or the solutions-tab when the resolution-viewer is openend");
		terminalTabInitial.setCheckToolTip( "If checked, the resolution viewer will show the 'terminal-tab' when opened. Otherwise will activate the 'solutions-tab'");
		control = terminalTabInitial.createControl(viewerChoicesComposite, "Open the 'terminal-tab' at resolution-viewer start-up");
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		boolean terminalInitials = DevrockPlugin.instance().storageLocker().getValue( StorageLockerSlots.SLOT_ARTIFACT_VIEWER_INITIAL_TAG_TERMINAL, true);
		terminalTabInitial.setSelection( terminalInitials);
				
		Composite colorChoicesComposite = new Composite( composite, SWT.NONE);
		colorChoicesComposite.setLayout(layout);
		colorChoicesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		Label colorChoicesLabel = new Label( colorChoicesComposite, SWT.NONE);
		colorChoicesLabel.setText("Color choices for the resolution viewer");
		colorChoicesLabel.setFont(bigFont);
		colorChoicesLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));
				
		

		artifactAxisColorEditor = new ColorEditor(getShell());		
		artifactAxisColorEditor.setLabelToolTip("Color for the 'dependency axis' symbols");
		artifactAxisColorEditor.setCheckToolTip("The color you select will be used for all symbols shown related to the relationship of artifacts and their dependencies and dependers");
		control = artifactAxisColorEditor.createControl(colorChoicesComposite, "Color for the 'dependency axis' symbols");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		Color defaultColor = Color.create(0, 128, 0);
		Color color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_DEPENDENCY, defaultColor);
		artifactAxisColorEditor.setSelection( new org.eclipse.swt.graphics.Color( color.getRed(), color.getGreen(), color.getBlue()));
		
		parentAxisColorEditor = new ColorEditor(getShell());		
		parentAxisColorEditor.setLabelToolTip("Color for the 'parent axis' symbols");
		parentAxisColorEditor.setCheckToolTip("The color you select will be used for all symbols shown related to the relationship of artifacts and their parents and parent-dependers");
		control = parentAxisColorEditor.createControl(colorChoicesComposite, "Color for the 'parent axis' symbols");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		defaultColor = Color.create(128, 128, 128);
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_PARENT, defaultColor);
		parentAxisColorEditor.setSelection( new org.eclipse.swt.graphics.Color( color.getRed(), color.getGreen(), color.getBlue()));

		
		importAxisColorEditor = new ColorEditor(getShell());		
		importAxisColorEditor.setLabelToolTip("Color for the 'import axis' symbols");
		importAxisColorEditor.setCheckToolTip("The color you select will be used for all symbols shown related to the relationship of artifacts and their imports and importers");
		control = importAxisColorEditor.createControl(colorChoicesComposite, "Color for the 'import axis' symbols");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		defaultColor = Color.create(0, 0, 0);
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_IMPORT, defaultColor);
		importAxisColorEditor.setSelection( new org.eclipse.swt.graphics.Color( color.getRed(), color.getGreen(), color.getBlue()));

		
		standardVersionColorEditor = new ColorEditor(getShell());		
		standardVersionColorEditor.setLabelToolTip("Color for standard versions");
		standardVersionColorEditor.setCheckToolTip("The color you select will be used for all standard versions of artifacts");
		control = standardVersionColorEditor.createControl(colorChoicesComposite, "Color for standard versions");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		defaultColor = Color.create(0, 128, 0);
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_VERSION_STANDARD, defaultColor);
		standardVersionColorEditor.setSelection( new org.eclipse.swt.graphics.Color( color.getRed(), color.getGreen(), color.getBlue()));
		
		pcVersionColorEditor = new ColorEditor(getShell());		
		pcVersionColorEditor.setLabelToolTip("Color for preliminary versions");
		pcVersionColorEditor.setCheckToolTip("The color you select will be used for all pc/rc/cr/SNAPSHOT versions of artifacts");
		control = pcVersionColorEditor.createControl(colorChoicesComposite, "Color for pc versions");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		defaultColor = Color.create(134, 0, 0);
		color = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_VERSION_PC, defaultColor);
		pcVersionColorEditor.setSelection( new org.eclipse.swt.graphics.Color( color.getRed(), color.getGreen(), color.getBlue()));
		
		
		
					
			
		// zed
		Composite zedChoicesComposite = new Composite( composite, SWT.NONE);
		zedChoicesComposite.setLayout(layout);
		zedChoicesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		Label zedChoicesLabel = new Label( zedChoicesComposite, SWT.NONE);
		zedChoicesLabel.setText("choices for Zed's analysis");
		zedChoicesLabel.setFont(bigFont);
		zedChoicesLabel.setLayoutData(new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));


		zedInitialModelViewModeEditor = new BooleanEditor();
		zedInitialModelViewModeEditor.setLabelToolTip( "Choose how model analysis data should be presented initially");
		zedInitialModelViewModeEditor.setCheckToolTip("If activated, the findings are grouped by GenericEntity, otherwise they are groupd by issue-type");
		control = zedInitialModelViewModeEditor.createControl(zedChoicesComposite, "Initially group model-analysis findings by GenericEntity");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));		
		boolean groupingByEntity = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ZED_MODEL_INITIAL_VIEWMODE, false);
		zedInitialModelViewModeEditor.setSelection(groupingByEntity);

		
		Composite zedOverrideChoicesComposite = new Composite( composite, SWT.BORDER);
		zedOverrideChoicesComposite.setLayout(layout);
		zedOverrideChoicesComposite.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 4, 1));
		
		// override on
		zedOverrideFingerPrintsEditor = new BooleanEditor();
		zedOverrideFingerPrintsEditor.setLabelToolTip( "Choose whether to override zed's default ratings");
		zedOverrideFingerPrintsEditor.setCheckToolTip("If activated, the file specified containing the override values is injected into zed's rating.\nOtherwise the default ratings are used");
		control = zedOverrideFingerPrintsEditor.createControl(zedOverrideChoicesComposite, "Override zed's default ratings with file");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));		
		boolean overrideFingerPrintsValue = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ZED_FP_OVERRIDE_RATINGS, false);
		zedOverrideFingerPrintsEditor.setSelection(overrideFingerPrintsValue);

		// override file
		zedFingerprintOverridesFileEditor = new FileEditor(getShell());
		control = zedFingerprintOverridesFileEditor.createControl(zedOverrideChoicesComposite, "Fingerprint file:");
		control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		zedFingerprintOverridesFileEditor.setLabelToolTip("Select a file with overrides for zed's rating");
		zedFingerprintOverridesFileEditor.setCheckToolTip("Select a YAML formatted map of FingerPrint and their rating to override the default");
	
		String zedFile = DevrockPlugin.instance().storageLocker().getValue(StorageLockerSlots.SLOT_ZED_FP_CUSTOM_FILE, null);
		if (zedFile != null) {
			zedFingerprintOverridesFileEditor.setSelection(zedFile);			
		}

		composite.pack();
		return composite;
	}


	@Override
	public void dispose() {
		bigFont.dispose();	
	}
	
	private void saveToLocker() {
		BasicStorageLocker storageLocker = DevrockPlugin.envBridge().storageLocker();
		
		// viewer choices
		boolean enableResolutionViewerYaml = activateResolutionViewerYaml.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_YAML_ENABLED, enableResolutionViewerYaml);
		
		boolean storeResolutionViewerSettings = storeSettingsOfResolutionViewer.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_STORE_VIEW_SETTINGS, storeResolutionViewerSettings);
		
		boolean openTerminalTabSettings = terminalTabInitial.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_INITIAL_TAG_TERMINAL, openTerminalTabSettings);
		
		// viewer color choices
		org.eclipse.swt.graphics.Color selection = artifactAxisColorEditor.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_DEPENDENCY, Color.create( selection.getRed(), selection.getGreen(), selection.getBlue()));
						
		selection = parentAxisColorEditor.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_PARENT, Color.create( selection.getRed(), selection.getGreen(), selection.getBlue()));
		
		selection = importAxisColorEditor.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_AXIS_IMPORT, Color.create( selection.getRed(), selection.getGreen(), selection.getBlue()));
		
		selection = standardVersionColorEditor.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_VERSION_STANDARD, Color.create( selection.getRed(), selection.getGreen(), selection.getBlue()));
		
		selection = pcVersionColorEditor.getSelection();		
		storageLocker.setValue(StorageLockerSlots.SLOT_ARTIFACT_VIEWER_COLOR_VERSION_PC, Color.create( selection.getRed(), selection.getGreen(), selection.getBlue()));
					
		// zed 
		storageLocker.setValue(StorageLockerSlots.SLOT_ZED_FP_CUSTOM_FILE, zedFingerprintOverridesFileEditor.getSelection());
		storageLocker.setValue(StorageLockerSlots.SLOT_ZED_FP_OVERRIDE_RATINGS, zedOverrideFingerPrintsEditor.getSelection());
		storageLocker.setValue(StorageLockerSlots.SLOT_ZED_MODEL_INITIAL_VIEWMODE, zedInitialModelViewModeEditor.getSelection());
		
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
