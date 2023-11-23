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
package com.braintribe.commons.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.braintribe.build.artifact.virtualenvironment.VirtualPropertyResolver;
import com.braintribe.cfg.Configurable;
import com.braintribe.devrock.virtualenvironment.listener.VirtualEnvironmentNotificationListener;


public class StringEditor extends AbstractEditor implements ModifyListener, VirtualEnvironmentNotificationListener {
	
	private String startValue;
	private boolean startEnabled = true;
	private ModifyListener startListener;
	private Text text;	
	private VirtualPropertyResolver resolver;
	
	@Configurable
	public void setResolver(VirtualPropertyResolver resolver) {
		this.resolver = resolver;
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
		text.setLayoutData( new GridData(SWT.FILL, SWT.CENTER, true, true, 3, 1));
		if (startValue != null) {
			text.setText( startValue);
		}		
		text.setEnabled(startEnabled);
		if (startListener != null)
			text.addModifyListener(startListener);
		text.addModifyListener( this);
		text.setToolTipText( resolver != null ? resolver.resolve( text.getText()) : text.getText());
		return composite;
	}
	
	public String getSelection() {
		return text.getText();
	}
	
	public void setSelection( String selection) {
		if (text == null) {
			startValue = selection;
		}
		else {
			text.setText(selection);
		}
		
	}

	public void setEnabled( boolean enabled) {
		if (text == null) {
			startEnabled = enabled;
		}
		else {
			text.setEnabled(enabled);
		}
	}
 
	public void addModifyListener( ModifyListener listener) {
		if (text == null) {
			startListener = listener;
		}
		else {			
			text.addModifyListener(listener);
		}
	}
	
	public Widget getWidget() {
		return text;
	}

	@Override
	public void modifyText(ModifyEvent event) {
		text.setToolTipText( resolver != null ? resolver.resolve( text.getText()) : text.getText());
		broadcast( text.getText());
	}

	@Override
	public void acknowledgeOverrideChange() {
		modifyText(null);
	}

	
	
}
