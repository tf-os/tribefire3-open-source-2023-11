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
package com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client;

import java.text.ParseException;

import com.google.gwt.core.client.GWT;
import com.sencha.gxt.cell.core.client.form.TriggerFieldCell;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;

/**
 * {@link TriggerFieldCell} implementation for the {@link ExtendedStringField}.
 * @author michel.docouto
 *
 */
public class ExtendedStringCell extends TriggerFieldCell<String> {
	
	public interface ExtendedStringCellAppearance extends TriggerFieldAppearance {
		//NOP
	}
	
	public ExtendedStringCell() {
		this(GWT.<ExtendedStringCellAppearance> create(ExtendedStringCellAppearance.class));
	}
	
	public ExtendedStringCell(ExtendedStringCellAppearance appearance) {
	    super(appearance);
	    setPropertyEditor(new PropertyEditor<String>() {
			@Override
			public String parse(CharSequence text) throws ParseException {
				return text.toString();
			}
			
			@Override
			public String render(String object) {
				return object;
			}
		});
	}
	
}
