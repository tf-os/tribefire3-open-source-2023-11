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

import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.DatePicker;
import com.sencha.gxt.widget.core.client.menu.DateMenu;

/**
 * Extends GXT's {@link DateMenu}, by showing only the month and day in the date picker.
 * @author michel.docouto
 *
 */
public class MonthDayMenu extends DateMenu {
	
	protected MonthDayDatePicker monthDayDatePicker;
	protected DatePicker mockDatePicker;
	protected boolean mockDatePickerInitialized = false;

	public MonthDayMenu() {
		remove(picker);

		monthDayDatePicker = new MonthDayDatePicker();

		monthDayDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				onPickerSelect(event);
			}
		});

		add(monthDayDatePicker);
	}

	@Override
	public void focus() {
		super.focus();
		monthDayDatePicker.getElement().focus();
	}

	/**
	 * Returns the selected date.
	 * 
	 * @return the date
	 */
	@Override
	public Date getDate() {
		return monthDayDatePicker.getValue();
	}

	/**
	 * Sets the menu's date.
	 * 
	 * @param date the date
	 */
	@Override
	public void setDate(Date date) {
		monthDayDatePicker.setValue(date);
	}
	
	@Override
	public DatePicker getDatePicker() {
		if (mockDatePicker == null) {
			mockDatePicker = new DatePicker() {
				@Override
				public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
					if (mockDatePickerInitialized)
						return monthDayDatePicker.addValueChangeHandler(handler);
					
					return null;
				}
				
				@Override
				public void focus() {
					if (mockDatePickerInitialized)
						monthDayDatePicker.focus();
				}
				
				@Override
				public XElement getElement() {
					return mockDatePickerInitialized ? monthDayDatePicker.getElement() : super.getElement();
				}
				
				@Override
				public void setMaxDate(Date maxDate) {
					if (mockDatePickerInitialized)
						monthDayDatePicker.setMaxDate(maxDate);
				}
				
				@Override
				public void setMinDate(Date minDate) {
					if (mockDatePickerInitialized)
						monthDayDatePicker.setMinDate(minDate);
				}
				
				@Override
				public void setValue(Date date, boolean fireEvents) {
					if (mockDatePickerInitialized)
						monthDayDatePicker.setValue(date, fireEvents);
				}
			};
				
			mockDatePickerInitialized = true;
		}
		
		return mockDatePicker;
	}

}
