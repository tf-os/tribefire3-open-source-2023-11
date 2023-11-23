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
package com.braintribe.devrock.ac.container.dialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.ac.container.ArtifactContainer;
import com.braintribe.devrock.ac.container.properties.component.ContainerPropertiesComponent;

public class ContainerInfoDialog extends Dialog  {
	public static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;


	private ArtifactContainer container;	
	private IProject iProject;
	private Shell parentShell;
	private ContainerPropertiesComponent cpc;

	
	public ContainerInfoDialog(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;		
		
		setShellStyle(SHELL_STYLE);
		
		
	}
	
	@Configurable @Required
	public void setContainer(ArtifactContainer container) {
		this.container = container;		
	}
	
	@Configurable @Required
	public void setProject(IProject selectedProject) {
		iProject = selectedProject;			
	}

	@Override
	protected Control createDialogArea(Composite parent) {	
		initializeDialogUnits(parent);
		
		parentShell.layout(true);
	
		final Composite composite = new Composite(parent, SWT.NONE);
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
    
		
		cpc = new ContainerPropertiesComponent(parentShell, container, null);
		cpc.createControl(parent);
			
			
		return composite;
	}
	

	@Override
	protected Point getInitialSize() {		
		return new Point( 500, 450);
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
		newShell.setText("Container properties of project " + iProject.getName());
	}
	
	@Override
	public boolean close() {
		return super.close();
	}
	
	

}
