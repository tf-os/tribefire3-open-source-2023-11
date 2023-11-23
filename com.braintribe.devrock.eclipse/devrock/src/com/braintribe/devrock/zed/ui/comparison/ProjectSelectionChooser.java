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
package com.braintribe.devrock.zed.ui.comparison;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.services.IDisposable;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.api.ui.commons.DevrockDialog;

/**
 * 
 * @author pit
 */
public class ProjectSelectionChooser extends DevrockDialog implements IDisposable, SelectionListener {
	private static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL;
	
	
	private Map<String, IProject> nameToProjectMap;
	private List<String> names;
	
	private IProject selectedProject;

	private org.eclipse.swt.widgets.List combo;

	public ProjectSelectionChooser(Shell parentShell) {
		super(parentShell);
		setShellStyle(SHELL_STYLE);
	}
	
	@Configurable @Required
	public void setAvailableProjects(List<IProject> availableProjects) {	
		nameToProjectMap = new HashMap<>( availableProjects.size());		
		availableProjects.stream().forEach( p -> nameToProjectMap.put( p.getName(), p));
		names = availableProjects.stream().map( p -> p.getName()).collect(Collectors.toList());
		names.sort( new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {				
				return o1.compareTo(o2);
			}			
		});					
		
	}

	@Override
	protected Control createDialogArea(Composite parent) {	
		initializeDialogUnits(parent);		
		
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NONE);

		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout( layout);       
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
                
        combo = new org.eclipse.swt.widgets.List(composite, SWT.READ_ONLY);
        combo.setItems( names.toArray(new String[0]));
        
        combo.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        combo.addSelectionListener(this);
        
        composite.pack();                                
        
        return composite;
	}
	
	private void checkValid(Boolean valid) {
		Button ok = getButton(IDialogConstants.OK_ID);
		
		if (ok == null)
			return;
		
		if (valid) {
			ok.setEnabled(true);
		}
		else {
			ok.setEnabled(false);
		}
	}
		

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {}

	@Override
	public void widgetSelected(SelectionEvent event) {
		Widget widget = event.widget;
		
		if (widget == combo) {
			int selectionIndex = combo.getSelectionIndex();
			if (selectionIndex >= 0) {
				String name = names.get(selectionIndex);
				selectedProject = nameToProjectMap.get(name);
				checkValid(true);
			}
			else {
				checkValid(false);
			}
		}		
	}

	@Override
	public void dispose() {
		
	}

	@Override
	protected Point getDrInitialSize() {
		checkValid(false);
		return new Point( 400, 200);
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText("Select project from current selection");		
	}
	
	
	public IProject getSelectedProject() {
		return selectedProject;
	}
	
	

}
