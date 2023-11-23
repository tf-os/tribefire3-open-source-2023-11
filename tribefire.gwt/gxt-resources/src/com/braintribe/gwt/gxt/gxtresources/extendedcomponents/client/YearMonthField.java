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
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.form.DateField;

/**
 * Extends the {@link DateField} by showing only the month and year options.
 * @author michel.docouto
 *
 */
public class YearMonthField extends DateField {
	
	public YearMonthField(DateCell cell) {
		super(cell);
		
		YearMonthMenu menu = new YearMonthMenu();
		getCell().setMenu(menu);
		menu.addHideHandler(new HideHandler() {
			@Override
			public void onHide(HideEvent event) {
				focus();
			}
		});
	}
	
	public YearMonthField() {
		this(new DateCell());
	}

}
