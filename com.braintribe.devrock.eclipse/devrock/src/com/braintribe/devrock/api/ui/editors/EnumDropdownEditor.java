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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;

public class EnumDropdownEditor<T extends Enum<T>> {

		private Map<String, T> choicesMap = new HashMap<>();
		private Map<T, String> toolTipMap = new HashMap<>();
		private T startValue;
		private Combo combo;
		
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
		
		public void setSelection( T selection) {
			int i = 0;
			for (Map.Entry<String, T> entry : choicesMap.entrySet()) {
				if (entry.getValue() == startValue) {
					combo.select( i);
					break;
				}
				i++;				
			}
		}
		
		public T getSelection() {
			int selectedIndex = combo.getSelectionIndex();
			int i = 0;
			for (Map.Entry<String, T> entry : choicesMap.entrySet()) {
				if (i == selectedIndex) {
					return entry.getValue();
				}
				i++;
			}
			return null;			 
		}
		
		public Composite createControl( Composite parent, String tag, Font font) {
			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = choicesMap.size();
			composite.setLayout(layout);
			if (tag != null) {
				Label label = new Label( composite, SWT.NONE);
				label.setText( tag);
				label.setLayoutData( new GridData(SWT.LEFT, SWT.CENTER, true, false, choicesMap.size(), 1));
				if (font != null) {
					label.setFont(font);
				}			
			}
			combo = new Combo(composite, SWT.NONE);
			combo.setItems( choicesMap.keySet().toArray(new String[0]));
						
			return composite;
		}
		
		
}
