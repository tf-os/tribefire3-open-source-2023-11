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
package com.braintribe.devrock.api.ui.selection;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.api.ui.editors.FileEditor;
import com.braintribe.devrock.api.ui.listeners.ModificationNotificationListener;
import com.braintribe.devrock.eclipse.model.identification.EnhancedCompiledArtifactIdentification;
import com.braintribe.devrock.plugin.DevrockPlugin;

public class CustomFileSelector {
	private BooleanEditor useStandardFile;
	private FileEditor customFile;		
	private Font bigFont;
	
	private String currentlySelectedCustom;
	private boolean currentlySelectedUsageOfStandard;
	private String title;
	private String tooltip;
	private String[] extensions;	
	private String standardLabel;
	private String standardLabelTip;
	private String standardCheckTip;
	
	private String customLabel;
	private String customLabelTip;
	private String customCheckTip;
	private String standardSlot;

	private String customAssociationSlot;
	private String customAssociatedOverrideSlot;
	private EnhancedCompiledArtifactIdentification customArtifactKey;
	
	private boolean initialStandardValue;
	
	
	
	/**
	 * creates a UI component that allows to either use a 'standard file' (whatever that is) or lets to specify a file.
	 * returns whether there is an active custom file or what the file is.
	 * uses the storage locker to initialize/store the data 
	 */ 
	
	@Required @Configurable
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Required @Configurable
	public void setToolTip(String tooltip) {
		this.tooltip = tooltip;
	}
	
	// standard boolean switch 
	@Required @Configurable
	public void setStandardLabel(String standardLabel) {
		this.standardLabel = standardLabel;
	}
	@Required @Configurable
	public void setStandardLabelTip(String standardLabelTip) {
		this.standardLabelTip = standardLabelTip;
	}
	
	@Required @Configurable
	public void setStandardCheckTip(String standardCheckTip) {
		this.standardCheckTip = standardCheckTip;
	}
	
	@Required @Configurable
	public void setStandardSlot(String standardSlot) {
		this.standardSlot = standardSlot;
	}
	
	@Required @Configurable
	public void setInitialStandardValue(boolean initialStandardValue) {
		this.initialStandardValue = initialStandardValue;
	}
	
	// custom file selection 

	@Required @Configurable
	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}
	
	@Required @Configurable
	public void setCustomLabel(String customLabel) {
		this.customLabel = customLabel;
	}
	
	@Required @Configurable
	public void setCustomLabelTip(String customLabelTip) {
		this.customLabelTip = customLabelTip;
	}
	@Required @Configurable
	public void setCustomCheckTip(String customCheckTip) {
		this.customCheckTip = customCheckTip;
	}
		
	@Configurable 
	public void setCustomArtifactKey(EnhancedCompiledArtifactIdentification customArtifactKey) {
		this.customArtifactKey = customArtifactKey;
	}
	@Configurable @Required
	public void setCustomAssociationSlot(String customAssociationSlot) {
		this.customAssociationSlot = customAssociationSlot;
	}
	@Configurable @Required
	public void setCustomAssociatedOverrideSlot(String customAssociatedOverrideSlot) {
		this.customAssociatedOverrideSlot = customAssociatedOverrideSlot;
	}
		

	// diverse 
	
	@Configurable @Required
	public void setBigFont(Font bigFont) {
		this.bigFont = bigFont;
	}
			
	public Composite createControl( Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		
		composite.setToolTipText(tooltip);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        
        layout.verticalSpacing=2;        
        
        Label respositorySelectionLabel = new Label( composite, SWT.NONE);
        
		respositorySelectionLabel.setText(title);
        respositorySelectionLabel.setFont(bigFont);
        
        respositorySelectionLabel.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        
        useStandardFile = new BooleanEditor();
        useStandardFile.setSelectionListener( new SelectionAdapter() {
        	
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		if (useStandardFile.getSelection()) {
        			customFile.setEnabled(false);
        			currentlySelectedUsageOfStandard = true;
        		}
        		else {
        			customFile.setEnabled(true);
        			currentlySelectedUsageOfStandard = false;
        		}
        		super.widgetSelected(e);
        	}        	
        });

		useStandardFile.setLabelToolTip(standardLabelTip);
		useStandardFile.setCheckToolTip( standardCheckTip);

		Composite control = useStandardFile.createControl(composite, standardLabel);		
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		boolean useStandard = DevrockPlugin.envBridge().storageLocker().getValue( standardSlot, initialStandardValue);		
		useStandardFile.setSelection( useStandard);
		currentlySelectedUsageOfStandard = useStandard;
		
		customFile = new FileEditor( parent.getShell());
		customFile.setExtensions( extensions);
		customFile.setLabelToolTip( customLabelTip);
		customFile.setCheckToolTip( customCheckTip);
		
		control = customFile.createControl(composite, customLabel);		
		control.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		
		if (customArtifactKey != null) {
			// override 			
			Map<String,Boolean> overrides = DevrockPlugin.envBridge().storageLocker().getValue( customAssociatedOverrideSlot, null);
			if (overrides != null) {
				Boolean override = overrides.get(customArtifactKey.asString());
				if (override != null && override == false) {
					useStandardFile.setSelection(false);
					useStandard = false;
					customFile.setEnabled(true);
					currentlySelectedUsageOfStandard = false;
				}
				else {
					useStandardFile.setSelection(true);
					customFile.setEnabled(false);
					currentlySelectedUsageOfStandard = true;
				}
			}
		
			// build file 
			String home = customArtifactKey.getOrigin() + "/build.xml";
			Map<String,String> assocs = DevrockPlugin.envBridge().storageLocker().getValue( customAssociationSlot, null);
			if (assocs != null) {
				String customValue = assocs.get( customArtifactKey.asString());
				if (customValue != null) {
					customFile.setSelection( customValue);
					currentlySelectedCustom = customValue;
					useStandard = false;
				}
				else {
					customFile.setSelection(home);
					currentlySelectedCustom = home;
					useStandardFile.setSelection(false);
				}
			}
			else {
				customFile.setSelection( home);		
				currentlySelectedCustom = home;
				useStandardFile.setSelection(false);
			}
			
		}
		else {
			if (useStandard) {
				customFile.setEnabled(false);
			}			
		}
		
		customFile.addListener( new ModificationNotificationListener() {
			
			@Override
			public void acknowledgeChange(Object sender, String value) {		
				if (sender == customFile) {
					currentlySelectedCustom = value;
				}
			}
		});
       
        return composite;
	}

	
	public String getCurrentlySelectedCustomFile() {
		return currentlySelectedCustom;
	}

	public boolean getCurrentlySelectedUsageOfStandardFile() {
		return currentlySelectedUsageOfStandard;
	}
	
	public void storeValues() {
		if (customArtifactKey != null) {
			if (currentlySelectedCustom != null) {
				Map<String,String> assocs = DevrockPlugin.envBridge().storageLocker().getValue( customAssociationSlot, new HashMap<>());
				assocs.put(customArtifactKey.asString(), currentlySelectedCustom);
				DevrockPlugin.envBridge().storageLocker().setValue( customAssociationSlot, assocs);
			}
			Map<String,Boolean> overrides = DevrockPlugin.envBridge().storageLocker().getValue( customAssociatedOverrideSlot, new HashMap<>());
			overrides.put(customArtifactKey.asString(), currentlySelectedUsageOfStandard);
			DevrockPlugin.envBridge().storageLocker().setValue( customAssociatedOverrideSlot, overrides);
			
		}
		DevrockPlugin.envBridge().storageLocker().setValue( standardSlot, currentlySelectedUsageOfStandard);
	}

	
	public void setEnabled( boolean enable) {
		useStandardFile.setEnabled(enable);
		customFile.setEnabled( !useStandardFile.getSelection());
		if (!enable) {
			useStandardFile.setSelection(true);
			customFile.setEnabled(false);
		}
	}
	
	public void dispose() {
	}
	
}
