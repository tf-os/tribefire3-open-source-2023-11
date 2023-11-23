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
package com.braintribe.devrock.api.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.braintribe.cfg.Configurable;

public class IntegerEditor {
	private Integer start;
	private Text text;
	
	private String labelToolTip;
	private String valueToolTip;
	
	@Configurable
	public void setLabelToolTip(String labelToolTip) {
		this.labelToolTip = labelToolTip;
	}

	@Configurable
	public void setValueToolTip(String checkToolTip) {
		this.valueToolTip = checkToolTip;
	}


	public Composite createControl( Composite parent, String tag) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		composite.setLayout(layout);
		
		Label label = new Label( composite, SWT.NONE);
		label.setText( tag);
		label.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		if (labelToolTip != null) {
			label.setToolTipText(labelToolTip);
		}
		
		text = new Text( composite, SWT.NONE);
		text.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		if (start != null) {
			text.setText( start.toString());
		}
		if (valueToolTip != null) {
			text.setToolTipText(valueToolTip);
		}
		return composite;
	}
	
	public Integer getSelection() {
		return Integer.valueOf(text.getText());
	}
	
	public void setSelection( int selection){
		start = selection;
		if (text != null) {
			text.setText( "" + selection);
		}	
	}
	
	public Widget getWidget() {
		return text;
	}
}
