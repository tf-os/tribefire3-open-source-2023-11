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
package com.braintribe.plugin.commons.preferences;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;


/**
 * scan for a directory 
 * 
 * @author pit
 *
 */
public class DirectoryEditor extends AbstractEditor implements ModifyListener, VirtualEnvironmentNotificationListener {
	
	private Shell shell;
	private String start;
	private Text text;
	private Button scanButton;
	private boolean startEnabled = true;
	private ModifyListener listener;
	private VirtualPropertyResolver propertyResolver;

	public DirectoryEditor( Shell shell) {
		this.shell = shell;			
	}
	
	@Configurable
	public void setSelection( File selection) {
		start = selection.getAbsolutePath();
		if (text != null) {
			text.setText( start);
		}
	}

	@Configurable
	public void setSelection( String selection) {
		start = selection;
		if (text != null) {
			text.setText( start);
		}
	}

	@Configurable
	public void setEnable( boolean enable){
		startEnabled = enable;
		if (text != null)
			text.setEnabled(enable);
		if (scanButton != null)
			scanButton.setEnabled(enable);
	}
	@Configurable
	public void setListener( ModifyListener listener) {
		this.listener = listener;
		if (text != null) {
			text.addModifyListener( this.listener);
		}
	}
	
	@Configurable
	public void setResolver( VirtualPropertyResolver resolver) {
		propertyResolver = resolver;
	}
	
	public String scanForDirectory(Shell shell) {
		DirectoryDialog dialog = new DirectoryDialog(shell);
		String directory = propertyResolver != null ? propertyResolver.resolve(text.getText()) : text.getText();
		if (directory != null) {
			dialog.setFilterPath( directory);
		}
		String name = dialog.open();
		if (name == null)
			return null;
		return name;
	}
	
	public Composite createControl( Composite parent, String tag) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
		
		Label label = new Label( composite, SWT.NONE);
		label.setText( tag);
		label.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
				
		text = new Text( composite, SWT.NONE);
		text.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		if (start != null) {
			text.setText( start);
		}
		text.setToolTipText( propertyResolver != null ? propertyResolver.resolve(start) : start);
		text.setEnabled(startEnabled);
		if (listener != null)
			text.addModifyListener(listener);

		text.addModifyListener( this);
		
		scanButton = new Button(composite, SWT.NONE);
		scanButton.setText("..");
		scanButton.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
		scanButton.addSelectionListener( new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String value = scanForDirectory( shell);
				if (value != null) {
					text.setText( value);
					setSelection( new File( value));
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}
		});		
		scanButton.setEnabled(startEnabled);
		return composite;
	}
	
	
	public String getSelection() {
		if (text != null)			
			return text.getText();
		else
			return start;
	}

	@Override
	public void modifyText(ModifyEvent event) {
		text.setToolTipText( propertyResolver != null ? propertyResolver.resolve( text.getText()) : text.getText());
		broadcast(text.getText());
	}

	@Override
	public void acknowledgeOverrideChange() {
		modifyText(null);
	}
	

}
