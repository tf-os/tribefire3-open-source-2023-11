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
package com.braintribe.devrock.ui.cfg.repository;

import java.io.File;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;
import com.braintribe.devrock.api.ui.editors.BooleanEditor;
import com.braintribe.devrock.api.ui.fonts.FontHandler;
import com.braintribe.devrock.api.ui.viewers.reason.transpose.TransposedReasonViewer;
import com.braintribe.devrock.api.ui.viewers.repository.RepositoryViewer;
import com.braintribe.devrock.model.repository.RepositoryConfiguration;
import com.braintribe.devrock.plugin.DevrockPlugin;
import com.braintribe.gm.model.reason.Reason;

public class RepositoryConfigurationInfoDialog extends DevrockDialog  {
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	private final Shell parentShell;

	private Date timestamp;
	private double lastProcessingTime;
	private RepositoryConfiguration repositoryConfiguration;
	private File origin;
	private TransposedReasonViewer reasonViewer;
	private TransposedReasonViewer failureViewer;
	private RepositoryViewer repositoryViewer;
	private Font bigFont;
	
		
	public RepositoryConfigurationInfoDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;				
		setShellStyle(SHELL_STYLE);
			
		
	}
	
	@Configurable @Required
	public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
		this.repositoryConfiguration = repositoryConfiguration;
	}
	
	@Configurable @Required
	public void setLastProcessingTime(double lastProcessingTime) {
		this.lastProcessingTime = lastProcessingTime;
	}
	
	@Configurable
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	@Configurable
	public void setOrigin(File origin) {
		this.origin = origin;
	}
		
	@Override
	protected Control createDialogArea(Composite parent) {	
		initializeDialogUnits(parent);
		
		parentShell.layout(true);
		
		bigFont = FontHandler.buildBigFont(parent.getDisplay(), parent.getFont());
	
		final Composite composite = new Composite(parent, SWT.NONE);
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
    			
        
        //
		Label imageLabel = new Label(composite, SWT.NONE);
		imageLabel.setLayoutData(new GridData( SWT.CENTER, SWT.TOP, false, false, 4, 1));
		imageLabel.setText("Currently active repository configuration");
		imageLabel.setFont(bigFont);

			 	
			
		Composite dataComposite = new Composite(composite, SWT.NONE);
		dataComposite.setLayout(layout);
		dataComposite.setLayoutData(  new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
				
	
		
		if (repositoryConfiguration.hasFailed()) {
			Reason failureReason = repositoryConfiguration.getFailure();						
			failureViewer = new TransposedReasonViewer( failureReason);
			failureViewer.setUiSupport( DevrockPlugin.instance().uiSupport());
    		Composite failureViewerComposite = failureViewer.createControl(dataComposite, "configuration has been flagged as failed");
    		failureViewerComposite.setLayoutData(  new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));				
		}
					

		// offline global
		BooleanEditor globallyOffline = new BooleanEditor();
		globallyOffline.setLabelToolTip("Whether the configuration is globally set to be offline");
		globallyOffline.setCheckToolTip("If checked, the configuration is offline. Otherwise, the state of the repositories is dynamically checked");
		Composite offlineComposite = globallyOffline.createControl( dataComposite, "globally offline");
		globallyOffline.setEnabled(false);
		globallyOffline.setSelection( repositoryConfiguration.getOffline());
		offlineComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
					
		
		// origination (use text to show reason)
		Reason origination = repositoryConfiguration.getOrigination();						
		reasonViewer = new TransposedReasonViewer( origination);
		reasonViewer.setShowTypes(false);
		reasonViewer.setUiSupport(DevrockPlugin.instance().uiSupport());
		Composite reasonViewerComposite = reasonViewer.createControl(dataComposite, "origin of configuration");
		reasonViewerComposite.setLayoutData(  new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 4));
		
		
		repositoryViewer = new RepositoryViewer(repositoryConfiguration);
		Composite repositoryViewerComposite = repositoryViewer.createControl(dataComposite, "active compiled configuration");
		repositoryViewerComposite.setLayoutData(  new GridData( SWT.FILL, SWT.FILL, true, true, 4, 10));
				
		
		composite.pack();
		return composite;
	}

		
	public void dispose() {
		if (reasonViewer != null) {
			reasonViewer.dispose();				
		}
		if (failureViewer != null) {
			failureViewer.dispose();
		}
		if (repositoryViewer != null) {
			repositoryViewer.dispose();
		}
		if (bigFont != null) {
			bigFont.dispose();
		}
	}
	

	@Override
	protected Point getDrInitialSize() {
		return new Point( 800, 800);
	}
	

	@Override
	protected Control createButtonBar(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayoutData(  new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
		GridLayout layout= new GridLayout();
	    layout.numColumns = 3;
		buttonComposite.setLayout( layout);
		
		Button closeButton = new Button( buttonComposite, SWT.NONE);
		closeButton.setText("OK");
		closeButton.addSelectionListener( new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				if (event.widget == closeButton) 
					close();				
			}
			
		});
		buttonComposite.setLayoutData(  new GridData( SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		return closeButton;
	}

	
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		if (timestamp != null) {
			newShell.setText("The repository configuration (compiled at " + timestamp.toString() + " in " + lastProcessingTime + " ms)");			
		}
		else {
			newShell.setText("The repository configuration loaded (in " + lastProcessingTime + " ms) from: " + origin.getName());
		}		
	}
	
	@Override
	public boolean close() {
		dispose();
		return super.close();
	}
	
	

}
