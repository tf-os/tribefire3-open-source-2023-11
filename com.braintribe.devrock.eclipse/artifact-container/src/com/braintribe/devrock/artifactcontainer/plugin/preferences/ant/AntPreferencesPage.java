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
package com.braintribe.devrock.artifactcontainer.plugin.preferences.ant;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.codec.CodecException;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.devrock.artifactcontainer.ArtifactContainerStatus;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.ArtifactContainerPreferenceInitializer;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.ant.validator.AntSettingsValidator;
import com.braintribe.devrock.artifactcontainer.plugin.preferences.codec.AntPreferencesCodec;
import com.braintribe.devrock.artifactcontainer.validator.ArtifactContainerPluginValidatorDialog;
import com.braintribe.model.malaclypse.cfg.AntTarget;
import com.braintribe.model.malaclypse.cfg.preferences.ac.AntRunnerPreferences;
import com.braintribe.plugin.commons.preferences.DirectoryEditor;
import com.braintribe.plugin.commons.preferences.FileEditor;
import com.braintribe.plugin.commons.preferences.listener.ModificationNotificationListener;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;
import com.braintribe.plugin.commons.tableviewer.CommonTableColumnData;
import com.braintribe.plugin.commons.tableviewer.CommonTableViewer;

public class AntPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, ModificationNotificationListener {
	protected Font bigFont;
	private DirectoryEditor antHomeEditor;
	private FileEditor antRunnerEditor;

	
	private Button addTargetButton;
	private Button removeTargetButton;
	
	private List<AntTarget> settings;
	private CommonTableViewer tableViewer;
	private Table targetTable;
	
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
		
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout( layout);
		
		ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
		
		Composite antSystemGroup = new Composite( composite, SWT.NONE);
		antSystemGroup.setLayout( layout);
		antSystemGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label antSystemLabel = new Label( antSystemGroup, SWT.NONE);
    	antSystemLabel.setText( "ANT system settings");
    	antSystemLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	antSystemLabel.setFont(bigFont);
    	
    	AntRunnerPreferences preferences = plugin.getArtifactContainerPreferences(false).getAntRunnerPreferences();

    	// ant home
		String anhomeAsString = preferences.getAntHome();
		antHomeEditor = new DirectoryEditor( getShell());
		antHomeEditor.setSelection(new File( anhomeAsString));
		Composite ahComposite = antHomeEditor.createControl( antSystemGroup, "Ant &home: ");
		ahComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		
		// ant runner
		String antRunnerAsString = preferences.getAntRunner();
		antRunnerEditor = new FileEditor(getShell());
		antRunnerEditor.setSelection( antRunnerAsString);
		antRunnerEditor.addListener( this);
		Composite arComposite = antRunnerEditor.createControl( antSystemGroup, "Ant &runner: ");
		arComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		// ant targets 
		Composite antTargetGroup = new Composite( composite, SWT.NONE);
		antTargetGroup.setLayout( layout);
		antTargetGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		Label antTargetLabel = new Label( antTargetGroup, SWT.NONE);
    	antTargetLabel.setText( "ANT targets");
    	antTargetLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	antTargetLabel.setFont(bigFont);
		
		
		Composite tableComposite = new Composite(antTargetGroup, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;	
		tableComposite.setLayout(gridLayout);
		tableComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));

		settings = preferences.getTargets();
		
		tableViewer = new CommonTableViewer(tableComposite, SWT.V_SCROLL | SWT.SINGLE);
		CommonTableColumnData [] columnData = new CommonTableColumnData[3];
		columnData[0] = new CommonTableColumnData("name", 100, 100, "Name of the ant target", new NameColumnLabelProvider(), new NameColumnEditingSupport(tableViewer));
		columnData[1] = new CommonTableColumnData("target", 100, 100, "the ant target", new TargetColumnLabelProvider(), new TargetColumnEditingSupport(tableViewer));
		columnData[2] = new CommonTableColumnData("transitivity", 100, 100, "whether target is a transitive target", new TransitivityColumnLabelProvider(), new TransitivityColumnEditingSupport(tableViewer));
		
		tableViewer.setup(columnData);
		
		targetTable = tableViewer.getTable();
		targetTable.setHeaderVisible(true);
		targetTable.setLinesVisible(true);
		
		GridData layoutData = new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4);
    	int ht = (targetTable.getItemHeight() * 10) + targetTable.getHeaderHeight();
    	layoutData.heightHint = targetTable.computeSize(SWT.DEFAULT, ht).y;
    	targetTable.setLayoutData( layoutData);
    	
    	AntTargetContentProvider contentProvider = new AntTargetContentProvider();
    	contentProvider.setSettings(settings);
    	tableViewer.setContentProvider(contentProvider);
    	tableViewer.setInput(settings);
		
    	//
    	// buttons
    	//
    
    	addTargetButton = new Button( antTargetGroup, SWT.NONE);
    	addTargetButton.setText( "Add target");
    	addTargetButton.setLayoutData( new GridData( SWT.LEFT,SWT.CENTER, true, true, 2, 1));
    	
    	removeTargetButton = new Button( antTargetGroup, SWT.NONE);
    	removeTargetButton.setText( "Remove target");
    	removeTargetButton.setLayoutData( new GridData( SWT.RIGHT,SWT.CENTER, true, true, 2, 1));
    	
    	SelectionListener listener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.widget == addTargetButton) {
					// add 
					AntTarget setting = AntTarget.T.create();
					setting.setName("name");
					setting.setTarget("target");
					setting.setTransitiveNature( true);
					settings.add( setting);
					//tableViewer.setInputSettings(settings);
					tableViewer.refresh();
				}
				
				if (event.widget == removeTargetButton) {
					// remove
					TableItem [] items = targetTable.getSelection();
 					for (TableItem item : items) {
 						AntTarget setting = (AntTarget) item.getData();
 						settings.remove( setting);
 					}
 					//tableViewer.setInputSettings(settings);
 					tableViewer.refresh();
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {							
			}
		};
		
		addTargetButton.addSelectionListener( listener);
		removeTargetButton.addSelectionListener(listener);
	
    	

		composite.pack();

		return composite;
	}
	
	

	@Override
	public boolean okToLeave() {
		AntSettingsValidator validator = new AntSettingsValidator( antHomeEditor.getSelection(), antRunnerEditor.getSelection());
		ValidationResult result = validator.validate();
		if (result.getValidationState()) {
			return super.okToLeave();		
		}
		else {				
			ArtifactContainerPluginValidatorDialog dlg = new ArtifactContainerPluginValidatorDialog(getShell());
			dlg.setResultsToDisplay( Collections.singletonList( result));
			dlg.open();	
			return false;
		}		
	}

	@Override
	protected void performApply() {	
		if (!okToLeave())
			return;
		ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
		AntRunnerPreferences preferences = plugin.getArtifactContainerPreferences(false).getAntRunnerPreferences();
		preferences.setAntHome( antHomeEditor.getSelection());
		preferences.setAntRunner( antRunnerEditor.getSelection());	
		try {
			new AntPreferencesCodec( plugin.getPreferenceStore()).decode(preferences);
		} catch (CodecException e) {
			String msg = "cannot write preferences to IPreferencesStore";
			ArtifactContainerStatus status = new ArtifactContainerStatus(msg, e);
			ArtifactContainerPlugin.getInstance().log(status);	
		}
	}

	@Override
	public boolean performOk() {
		if (!okToLeave())
			return false;
		performApply();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		ArtifactContainerPlugin plugin = ArtifactContainerPlugin.getInstance();
		plugin.getArtifactContainerPreferences(false).setAntRunnerPreferences( ArtifactContainerPreferenceInitializer.initializeAntRunnerPreferences());				
	}

	@Override
	public void acknowledgeChange(Object sender, String value) {
		if (sender == antRunnerEditor) {
			String prefix = antHomeEditor.getSelection();
			if (value.startsWith( prefix)) {
				value = value.substring( prefix.length()+1);
				antRunnerEditor.setSelection(value);
			}
		}
		
	}

	
}
