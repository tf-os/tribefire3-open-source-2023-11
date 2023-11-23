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
package com.braintribe.devrock.artifactcontainer.ui.ant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.devrock.artifactcontainer.quickImport.ui.QuickImportDialog;
import com.braintribe.model.artifact.Artifact;
import com.braintribe.model.artifact.processing.version.VersionProcessor;
import com.braintribe.model.malaclypse.cfg.preferences.ac.qi.QuickImportAction;
import com.braintribe.plugin.commons.preferences.StringEditor;

public class AntWizardArtifactTab extends SelectionAdapter implements ArtifactSelectionBroadcaster, ArtifactSelectionListener {
	
	private static final String paddingString = "                       ";
	private static final int padding = 12;

	private Artifact selectedTargetArtifact;
	private Button targetBrowseButton;
	private StringEditor targetGroupIdEditor;
	private StringEditor targetArtifactIdEditor;
	private StringEditor targetVersionEditor;
	private List<ArtifactSelectionListener> listeners = new ArrayList<ArtifactSelectionListener>();
	
	private Shell parentShell;
	
	public AntWizardArtifactTab(Shell parentShell) {
		this.parentShell = parentShell;
	}
	
	public AntWizardArtifactTab(Shell parentShell, Artifact artifact) {
		this.parentShell = parentShell;
		selectedTargetArtifact = artifact;
	}
		
	

	@Override
	public void addArtifactSelectionListener(ArtifactSelectionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeArtifactSelectionListener(ArtifactSelectionListener listener) {
		listeners.remove( listener);
		
	}

	public Composite createControl( Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        
    	targetGroupIdEditor = new StringEditor();
    	Composite targetGroupIdComposite = targetGroupIdEditor.createControl( composite, pad( "Group Id:"));
		targetGroupIdComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1));			
		if (selectedTargetArtifact != null) {
			targetGroupIdEditor.setSelection( selectedTargetArtifact.getGroupId());
		}

		targetBrowseButton = new Button( composite, SWT.NONE);
		targetBrowseButton.setText( "..");
		targetBrowseButton.setLayoutData( new GridData( SWT.RIGHT, SWT.FILL, false, false, 1, 1));
		targetBrowseButton.addSelectionListener( this);
    	
		targetArtifactIdEditor = new StringEditor();
    	Composite targetArtifactIdComposite = targetArtifactIdEditor.createControl( composite, pad( "Artifact Id:"));
		targetArtifactIdComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		if (selectedTargetArtifact != null) {
			targetArtifactIdEditor.setSelection( selectedTargetArtifact.getArtifactId());
		}
		    	
		targetVersionEditor = new StringEditor();
    	Composite targetVersionComposite = targetVersionEditor.createControl( composite, pad( "Version:"));
		targetVersionComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		if (selectedTargetArtifact != null) {		
			targetVersionEditor.setSelection( VersionProcessor.toString( selectedTargetArtifact.getVersion()));		
		}
                
        return composite;
	}
	
	private String pad( String tag) {
		int l = tag.length();
		if (l < padding) {
			return tag + paddingString.substring(0, padding - l);
		}
		return tag;
	}
	
	private Artifact getBrowseResult() {
		QuickImportDialog quickImportDialog = new QuickImportDialog( parentShell);
		quickImportDialog.setImportAction( QuickImportAction.selectOnly);
		if (selectedTargetArtifact != null) {
			quickImportDialog.setSelection(selectedTargetArtifact);
		}		
		quickImportDialog.open();
		return quickImportDialog.getSelection();
	}
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget == targetBrowseButton) {
			Artifact selected = getBrowseResult();
			if (selected != null) {
				targetGroupIdEditor.setSelection( selected.getGroupId());
				targetArtifactIdEditor.setSelection( selected.getArtifactId());
				targetVersionEditor.setSelection( VersionProcessor.toString( selected.getVersion()));
				selectedTargetArtifact = selected;
				acknowledgeArtifactSelection( this, NameParser.buildName( selected));
			}
			else {
				acknowledgeArtifactSelection( this, "no target");
			}
		}
	}

	public Artifact getSelectedArtifact() {		
		String groupId = targetGroupIdEditor.getSelection();
		String artifactId = targetArtifactIdEditor.getSelection();
		String version = targetVersionEditor.getSelection();
		if (
				groupId == null || groupId.length() == 0 ||
				artifactId == null || artifactId.length() == 0 ||
				version == null || version.length() == 0
		   )
			return null;
		
		Artifact artifact = Artifact.T.create();
		artifact.setGroupId( groupId);
		artifact.setArtifactId( artifactId);
		artifact.setVersion( VersionProcessor.createFromString( version));
		return artifact;		
	}

	@Override
	public void acknowledgeArtifactSelection(AntWizardArtifactTab tab, String name) {
		for (ArtifactSelectionListener listener : listeners) {
			listener.acknowledgeArtifactSelection(tab, name);
		}		
	}

	
	
}
