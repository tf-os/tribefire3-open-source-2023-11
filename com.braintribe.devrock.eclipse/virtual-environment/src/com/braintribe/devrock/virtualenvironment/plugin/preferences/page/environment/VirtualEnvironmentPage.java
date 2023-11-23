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
package com.braintribe.devrock.virtualenvironment.plugin.preferences.page.environment;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import com.braintribe.codec.CodecException;
import com.braintribe.devrock.preferences.commons.BooleanEditor;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentPlugin;
import com.braintribe.devrock.virtualenvironment.VirtualEnvironmentStatus;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationBroadcaster;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;
import com.braintribe.devrock.virtualenvironment.plugin.preferences.VirtualEnvironmentPreferencesCodec;
import com.braintribe.model.malaclypse.cfg.preferences.ve.EnvironmentOverride;
import com.braintribe.model.malaclypse.cfg.preferences.ve.OverrideType;
import com.braintribe.model.malaclypse.cfg.preferences.ve.VirtualEnvironmentPreferences;

public class VirtualEnvironmentPage extends PreferencePage implements IWorkbenchPreferencePage, VirtualEnvironmentNotificationBroadcaster {	
	private Font bigFont;
	private VirtualEnvironmentPlugin plugin = VirtualEnvironmentPlugin.getInstance();
	private VirtualEnvironmentPreferences preferences = plugin.getPreferences(false);
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
		addListener( VirtualEnvironmentPlugin.getInstance());
	}
	
	

	@Override
	public void dispose() {
		removeListener(VirtualEnvironmentPlugin.getInstance());
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
    	activationEditor.setSelection( preferences.getActivation());
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
		variableOverrideTab.setOverrides( new HashSet<EnvironmentOverride>( preferences.getEnvironmentOverrides().values()));
		propertyOverrideTab.setOverrides( new HashSet<EnvironmentOverride>(preferences.getPropertyOverrides().values()));
	}
	
	private void setToPreferences() {
		
		preferences.setActivation(activationEditor.getSelection());
		
		Map<String, EnvironmentOverride> environmentOverrideMap = preferences.getEnvironmentOverrides();
		environmentOverrideMap.clear();
		for (EnvironmentOverride override : variableOverrideTab.getOverrides()) {
			environmentOverrideMap.put( override.getName(), override);
		}
		
		Map<String, EnvironmentOverride> propertyOverrideMap = preferences.getPropertyOverrides();
		propertyOverrideMap.clear();
		for (EnvironmentOverride override : propertyOverrideTab.getOverrides()) {
			propertyOverrideMap.put( override.getName(), override);
		}
		try {
			new VirtualEnvironmentPreferencesCodec( VirtualEnvironmentPlugin.getInstance().getPreferenceStore()).decode(preferences);
		} catch (CodecException e) {
			String msg = "cannot encode preferences to IPreferenceStore";
			VirtualEnvironmentStatus status = new VirtualEnvironmentStatus(msg, e);
			VirtualEnvironmentPlugin.getInstance().getLog().log(status);	
		}
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
		activationEditor.setSelection( preferences.getActivation());
		primeOverrideTabs();
		broadcastChanges();
		return super.performCancel();
	}

	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
	
	
	

}
