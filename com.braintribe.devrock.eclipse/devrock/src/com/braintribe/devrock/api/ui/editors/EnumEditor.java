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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

/**
 * an editor for Enum values<br/>
 * supply a map of string vs enum value, and it will build a simple radion button row
 * @author pit
 *
 * @param <T> - the Enum you want to have edited 
 */
public class EnumEditor<T extends Enum<T>> {

	private Map<String, T> choicesMap = new HashMap<>();
	private Map<T, String> toolTipMap = new HashMap<>();
	private T startValue;
	
	private Map<T, Button> radios;
	private Label label;

	/**
	 * set the choices, a map of declarative texts to enum value 
	 * @param choices - a {@link Map} of {@link String} to Enum value
	 */
	@Required @Configurable
	public void setChoices( Map<String, T> choices) {
		choicesMap.putAll(choices);
	}

	@Configurable
	public void setToolTips( Map<T, String> tips) {
		toolTipMap = tips;
	}
	/**
	 * set the selection or the initial value 
	 * @param selection - the Enum value 
	 */
	public void setSelection( T selection) {
		if (radios == null) {
			startValue = selection;
		}
		else {
			Button button = radios.get(selection);
			button.setSelection(true);
		}
	}
	
	/**
	 * get the currently selection Enum value 
	 * @return - the Enum value selected 
	 */
	public T getSelection() {
		for (Entry<T,Button> entry : radios.entrySet()) {
			if (entry.getValue().getSelection()){
				return entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * create the UI part 
	 * @param parent - the parent {@link Composite}, i.e. in what it should integrate 
	 * @param tag - the title of the composite
	 * @return - the created {@link Composite}
	 */
	public Composite createControl( Composite parent, String tag, Font font) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = choicesMap.size();
		composite.setLayout(layout);
		
		if (tag != null) {		
			label = new Label( composite, SWT.NONE);
			label.setText( tag);
			label.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, choicesMap.size(), 1));
			if (font != null) {
				label.setFont(font);
			}
		}
		
		radios = new HashMap<T, Button>();
		for (Entry<String, T> entry : choicesMap.entrySet()) {
			Button button = new Button( composite, SWT.RADIO);
			button.setText( entry.getKey());
			button.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1,1));
			T value = entry.getValue();
			if (value == startValue) {
				button.setSelection(true);
			}
			if (toolTipMap != null) {
				String tooltip = toolTipMap.get(value);
				if (tooltip != null) {
					button.setToolTipText(tooltip);
				}
			}
			radios.put( value, button);			
		}
		
		return composite;
	}
	@Configurable
	public void setEnable( boolean enable){
		if (label != null) {
			label.setEnabled(enable);
		}
		radios.values().stream().forEach( r -> r.setEnabled(enable));
	}
}
