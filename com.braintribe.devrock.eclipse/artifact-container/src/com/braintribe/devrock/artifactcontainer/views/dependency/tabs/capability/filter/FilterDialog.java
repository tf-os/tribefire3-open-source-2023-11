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
package com.braintribe.devrock.artifactcontainer.views.dependency.tabs.capability.filter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.braintribe.devrock.artifactcontainer.ArtifactContainerPlugin;
import com.braintribe.model.malaclypse.cfg.preferences.ac.views.dependency.DependencyViewPreferences;
import com.braintribe.model.malaclypse.cfg.preferences.ac.views.dependency.FilterType;

public class FilterDialog extends Dialog {	
	private Text editBox;
	private Button notInterpret;
	private DependencyViewPreferences dvPreferences = ArtifactContainerPlugin.getInstance().getArtifactContainerPreferences(false).getDependencyViewPreferences();

	public FilterDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		// 

		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=2;        
        composite.setLayout( layout);
        composite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true));
        
        composite.setSize( 600, 400);
        
        // edit box for typing
        Composite labelComposite = new Composite( composite, SWT.NONE);
        labelComposite.setLayout( layout);
        labelComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
            
        Label label = new Label( labelComposite, SWT.NONE);
        label.setText( "Enter regular expression for filtering a condensed artifact name");
        label.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 4, 1));
        
        Composite textComposite = new Composite( composite, SWT.NONE);
        textComposite.setLayout( layout);
        textComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
        editBox = new Text( textComposite, SWT.BORDER);
        editBox.setLayoutData(new GridData( SWT.FILL, SWT.CENTER, true, true, 3, 1));
        String filter = dvPreferences.getFilterExpression();
        if (filter != null) {
        	editBox.setText(filter);
        }
        
        Composite switchComposite = new Composite( composite, SWT.NONE);
        switchComposite.setLayout( layout);
        switchComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4,1));
                
        notInterpret = new Button( switchComposite, SWT.CHECK);        
        notInterpret.setText(  "Interpret as direct regexp expression");
        notInterpret.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 4, 1));        
    	
        FilterType type = dvPreferences.getFilterType();
    	switch ( type ) {
    	case regexp:
    			notInterpret.setSelection(true);
    		break;
    		default:
    		case simple: 
    			notInterpret.setSelection( false);
    		break;
    	}
        
        
		 return composite;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point( 400, 200);
	}
	

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Dependency view display filter");						
	}

	@Override
	protected void okPressed() {
		String filter = editBox.getText();
		dvPreferences.setFilterExpression(filter);
		boolean filterType = notInterpret.getSelection();
		if (filterType) {
			dvPreferences.setFilterType( FilterType.regexp);			
		} else {
			dvPreferences.setFilterType(FilterType.simple);
		}
		super.okPressed();
	}	
	
}
