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
package com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client;

import com.sencha.gxt.cell.core.client.form.DateCell;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.DateTimePropertyEditor;

/**
 * Extends the {@link DateField} by adding a time field to its date picker.
 * @author michel.docouto
 *
 */
public class DateTimeField extends DateField {
	
	protected DateTimeMenu menu;
	
	public DateTimeField(DateCell cell) {
		super(cell, new DateTimePropertyEditor());
		
		menu = new DateTimeMenu();
		getCell().setMenu(menu);
		menu.addHideHandler(event -> focus());
	}
	
	public DateTimeField() {
		this(new DateCell());
	}
	
	public void setTimeRegex(String timeRegex, String pattern) {
		menu.setTimeRegex(timeRegex, pattern);
	}
	
	/**
	 * Configures whether to use seconds. Defaults to false.
	 */
	public void setUseSeconds(boolean useSeconds) {
		menu.setUseSeconds(useSeconds);
	}
	
	/**
	 * Configures whether to use milliseconds. Defaults to false.
	 */
	public void setUseMilliseconds(boolean useMilliseconds) {
		menu.setUseMilliseconds(useMilliseconds);
	}

}
