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

import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.YearMonthDatePicker.YearhMonthDatePickerAppearance;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.DatePicker;
import com.sencha.gxt.widget.core.client.DatePicker.DatePickerAppearance;
import com.sencha.gxt.widget.core.client.menu.DateMenu;

/**
 * Extends GXT's {@link DateMenu}, by showing only the month and year in the date picker.
 * @author michel.docouto
 *
 */
public class YearMonthMenu extends DateMenu {

	protected YearMonthDatePicker yearMonthDatePicker;
	protected DatePicker mockDatePicker;
	protected boolean mockDatePickerInitialized = false;

	public YearMonthMenu() {
		remove(picker);

		yearMonthDatePicker = new YearMonthDatePicker((DatePickerAppearance) GWT.create(YearhMonthDatePickerAppearance.class));

		yearMonthDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				onPickerSelect(event);
			}
		});

		add(yearMonthDatePicker);
	}

	@Override
	public void focus() {
		super.focus();
		yearMonthDatePicker.getElement().focus();
	}

	/**
	 * Returns the selected date.
	 * 
	 * @return the date
	 */
	@Override
	public Date getDate() {
		return yearMonthDatePicker.getValue();
	}

	/**
	 * Sets the menu's date.
	 * 
	 * @param date the date
	 */
	@Override
	public void setDate(Date date) {
		yearMonthDatePicker.setValue(date);
	}
	
	@Override
	public DatePicker getDatePicker() {
		if (mockDatePicker == null) {
			mockDatePicker = new DatePicker() {
				@Override
				public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
					if (mockDatePickerInitialized)
						return yearMonthDatePicker.addValueChangeHandler(handler);
					
					return null;
				}
				
				@Override
				public void focus() {
					if (mockDatePickerInitialized)
						yearMonthDatePicker.focus();
				}
				
				@Override
				public XElement getElement() {
					return mockDatePickerInitialized ? yearMonthDatePicker.getElement() : super.getElement();
				}
				
				@Override
				public void setMaxDate(Date maxDate) {
					if (mockDatePickerInitialized)
						yearMonthDatePicker.setMaxDate(maxDate);
				}
				
				@Override
				public void setMinDate(Date minDate) {
					if (mockDatePickerInitialized)
						yearMonthDatePicker.setMinDate(minDate);
				}
				
				@Override
				public void setValue(Date date, boolean fireEvents) {
					if (mockDatePickerInitialized)
						yearMonthDatePicker.setValue(date, fireEvents);
				}
			};
				
			mockDatePickerInitialized = true;
		}
		
		return mockDatePicker;
	}

}
