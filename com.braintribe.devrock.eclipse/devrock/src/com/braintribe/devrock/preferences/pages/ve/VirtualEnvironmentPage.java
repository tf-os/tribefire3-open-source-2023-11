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
package com.braintribe.devrock.preferences.pages.ve;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.api.storagelocker.StorageLockerSlots;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.api.ve.listeners.VirtualEnvironmentNotificationBroadcaster;
import com.braintribe.devrock.api.ve.listeners.VirtualEnvironmentNotificationListener;
import com.braintribe.devrock.eclipse.model.ve.EnvironmentOverride;
import com.braintribe.devrock.eclipse.model.ve.OverrideType;
import com.braintribe.devrock.plugin.DevrockPlugin;

public class VirtualEnvironmentPage extends PreferencePage implements IWorkbenchPreferencePage, 
																		VirtualEnvironmentNotificationBroadcaster, 
																		StorageLockerSlots,
																		Consumer<Boolean>{	
	private Font bigFont;	
	private BooleanEditor activationEditor;	
	private Set<VirtualEnvironmentNotificationListener> listeners = new HashSet<VirtualEnvironmentNotificationListener>();
	
	private CTabFolder tabFolder;
	private EnvironmentOverrideTab variableOverrideTab;
	private EnvironmentOverrideTab propertyOverrideTab;
	
	@Override
	public void addListener(VirtualEnvironmentNotificationListener listener) {		
		listeners.add(listener);
	}

	@Override
	public void removeListener(VirtualEnvironmentNotificationListener listener) {
		listeners.remove(listener);
		
	}

	@Override
	public void init(IWorkbench arg0) {
		addListener( DevrockPlugin.instance());
	}
	
	

	@Override
	public void dispose() {
		if (bigFont != null) {
			bigFont.dispose();
		}
		removeListener(DevrockPlugin.instance());
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
		
		// 
		Composite activationGroup = new Composite( composite, SWT.NONE);
		activationGroup.setLayout( layout);
		activationGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
	
		Label activationLabel = new Label( activationGroup, SWT.NONE);
    	activationLabel.setText( "Virtual environment settings");
    	activationLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false));
    	activationLabel.setFont(bigFont);
    	
    	// on/off switch
    	activationEditor = new BooleanEditor();
    	
    	
    	boolean globalActivation = DevrockPlugin.envBridge().storageLocker().getValue( SLOT_VE_ACTIVATION, false);
    	activationEditor.setSelection( globalActivation);
     	
    	Composite uiComposite = activationEditor.createControl(activationGroup, "&Activate virtual environment: ");
     	uiComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
     	
     // 
     	Composite definitionGroup = new Composite( composite, SWT.NONE);
     	definitionGroup.setLayout( layout);
     	definitionGroup.setLayoutData(new GridData( SWT.FILL, SWT.FILL, true, false, 4, 1));
     	     	
     	// tabfolder
     	tabFolder = new CTabFolder( definitionGroup, SWT.NONE);
		tabFolder.setBackground( parent.getBackground());
		tabFolder.setSimple( false);		
		tabFolder.setLayout( new FillLayout());
		tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
		
		variableOverrideTab = new EnvironmentOverrideTab( OverrideType.environment);
		CTabItem item = new CTabItem( tabFolder, SWT.NONE);
		Composite variablePageComposite = variableOverrideTab.createControl( tabFolder);
		variablePageComposite.setBackground( composite.getBackground());
		item.setControl( variablePageComposite);
		item.setText( "Environment overrides");
		item.setToolTipText( "Override enviroment variables");				
		
		propertyOverrideTab = new EnvironmentOverrideTab( OverrideType.property);
		item = new CTabItem( tabFolder, SWT.NONE);
		Composite propertyPageComposite = propertyOverrideTab.createControl( tabFolder);
		propertyPageComposite.setBackground( composite.getBackground());
		item.setControl( propertyPageComposite);
		item.setText( "System property overrides");
		item.setToolTipText( "Override system properties");
				
		
		primeOverrideTabs();
		
		parent.layout();
		parent.setFocus();	
		
		tabFolder.setSelection(0);
		
		variableOverrideTab.refresh();
		propertyOverrideTab.refresh();
		
 	
		return composite;
	}

	private void primeOverrideTabs() {		
		Pair<List<EnvironmentOverride>,List<EnvironmentOverride>> overrides = loadStoredOverrides();
		
		variableOverrideTab.setOverrides( new HashSet<EnvironmentOverride>( overrides.first));
		variableOverrideTab.setToggleConsumer(this);
		
		propertyOverrideTab.setOverrides( new HashSet<EnvironmentOverride>( overrides.second));
		propertyOverrideTab.setToggleConsumer(this);
	}
	
	
	private Pair<List<EnvironmentOverride>, List<EnvironmentOverride>> loadStoredOverrides() {
		Optional<List<EnvironmentOverride>> overrideOptional = DevrockPlugin.envBridge().storageLocker().getValue(SLOT_VE_ENTRIES);
		
		
		if (overrideOptional.isPresent()) {
			List<EnvironmentOverride> overrides = overrideOptional.get();
			
			List<EnvironmentOverride> envOverrrides = new ArrayList<>( overrides.size()); // too big most probably .. 
			List<EnvironmentOverride> propertyOverrrides = new ArrayList<>( overrides.size()); // too big most probably ..
			
			for (EnvironmentOverride override : overrides) {
				switch ( override.getOverrideNature()) {
				case environment:
					envOverrrides.add( override);
					break;
				case property:
					propertyOverrrides.add(override);
					break;
				default:
					break;				
				}			
			}
			return Pair.of( envOverrrides, propertyOverrrides);
		}
		return Pair.of( Collections.emptyList(), Collections.emptyList());
		
	}
	private void setToPreferences() {
		
		DevrockPlugin.envBridge().storageLocker().setValue( SLOT_VE_ACTIVATION, activationEditor.getSelection());
		
		List<EnvironmentOverride> overrides = new ArrayList<>(variableOverrideTab.getOverrides().size() + propertyOverrideTab.getOverrides().size());
		
		overrides.addAll( variableOverrideTab.getOverrides());
		overrides.addAll( propertyOverrideTab.getOverrides());
		
		DevrockPlugin.envBridge().storageLocker().setValue( SLOT_VE_ENTRIES, overrides);		
	}

	@Override
	protected void performApply() {	
		setToPreferences();
		broadcastChanges();		
	}

	private void broadcastChanges() {
		for (VirtualEnvironmentNotificationListener listener : listeners) {
			listener.acknowledgeOverrideChange();
		}
	}

	@Override
	public boolean performCancel() {
				
    	boolean globalActivation = DevrockPlugin.envBridge().storageLocker().getValue( SLOT_VE_ACTIVATION, false);
    	activationEditor.setSelection( globalActivation);
     			
		primeOverrideTabs();
		broadcastChanges();
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}

	@Override
	public void accept(Boolean added) {
		if (added) {
			activationEditor.setSelection(true);
		}
		
	}
	
	
	

}
