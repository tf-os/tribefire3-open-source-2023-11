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
package com.braintribe.devrock.artifactcontainer.validator;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.plugin.commons.preferences.validator.ValidationResult;

public class ArtifactContainerPluginValidatorDialog extends Dialog {
	private static final int SHELL_STYLE = SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL | SWT.OK;
	private static final String DLG_TITLE = "Devrock's Artifact Container's validation result";
	private static final String MSG_INTRO = "The internal validation has found the following problems with your workspace's setup :";	
	private static final String MSG_FIX_SETTINGS = "You won't be able to use AC's features before the issues are fixed in the appropriate preference pages.";
	private CTabFolder tabFolder = null;
	private List<ValidationResult> resultsToDisplay;
	private Color background;
	private Font bigFont;
	
	@Configurable @Required
	public void setResultsToDisplay(List<ValidationResult> resultsToDisplay) {
		this.resultsToDisplay = resultsToDisplay;
	}
	
	public ArtifactContainerPluginValidatorDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle( SHELL_STYLE);
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {		
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout( new FillLayout());		
								           				
		int nColumns= 4;
        GridLayout layout= new GridLayout();
        layout.numColumns = nColumns;
        layout.verticalSpacing=5;
        
        Composite tabComposite = new Composite(composite, SWT.NONE);
   
        tabComposite.setLayout( layout);
        //tabComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL));
        
    	
	
		Font initialFont = parent.getFont();
		FontData [] fontDataBig = initialFont.getFontData();
		for (FontData data : fontDataBig) {
			data.setHeight( data.getHeight() + (data.getHeight() / 5));				
		}
		bigFont = new Font( getShell().getDisplay(), fontDataBig);
		

		Label introLabel = new Label( tabComposite, SWT.NONE);
		introLabel.setText( MSG_INTRO);
		introLabel.setFont(bigFont);
		introLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
        
		tabFolder = new CTabFolder( tabComposite, SWT.NONE);
		tabFolder.setFont(bigFont);
		background = parent.getBackground();
		tabFolder.setBackground( background);
		tabFolder.setSimple( false);		
		tabFolder.setLayout( new FillLayout());		
		tabFolder.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 4, 4));
		
		for (ValidationResult result : resultsToDisplay) {
			addValidationResult(result);
		}
		
		Label hintLabel = new Label( tabComposite, SWT.NONE);
		hintLabel.setText( MSG_FIX_SETTINGS);
		hintLabel.setFont(bigFont);
		hintLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		parent.layout();
		parent.setFocus();
		
		// 
		// 
		//
		tabFolder.setSelection( 0);
		
		return composite;
	}
	
	private void addValidationResult(ValidationResult result) {
		CTabItem item = new CTabItem( tabFolder, SWT.NONE);
		ArtifactContainerPluginValidatorTab tab = new ArtifactContainerPluginValidatorTab( result);
		
		Composite pageComposite = tab.createControl( tabFolder);
		pageComposite.setBackground( background);
		item.setControl( pageComposite);
		item.setText( result.getName());
		item.setToolTipText( result.getTooltip());
		
	}
	

	@Override
	protected Point getInitialSize() {		
		return new Point( 700, 400);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText( DLG_TITLE);
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.CANCEL_ID) {
			return null;
		}	
		return super.createButton(parent, id, label, defaultButton);
	}
	
	

	

}
