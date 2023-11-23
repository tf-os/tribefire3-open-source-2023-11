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

import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.widget.core.client.ComponentHelper;
import com.sencha.gxt.widget.core.client.DatePicker;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.validator.RegExValidator;
import com.sencha.gxt.widget.core.client.form.validator.RegExValidator.RegExMessages;
import com.sencha.gxt.widget.core.client.menu.DateMenu;

/**
 * Extends GXT's {@link DateMenu}, by adding a time field in the date picker.
 * @author michel.docouto
 *
 */
public class DateTimeMenu extends DateMenu {
	
	protected TextField timeField;
	protected RegExValidator regExValidator;
	protected boolean useSeconds;
	protected boolean useMilliseconds;
	
	public DateTimeMenu() {
		timeField = new TextField();
		regExValidator = new RegExValidator("^(([0-9])|([0-1][0-9])|([2][0-3])):(([0-9])|([0-5][0-9]))$", LocalizedText.INSTANCE.invalidTime());
		timeField.addValidator(regExValidator);
		timeField.setAutoValidate(true);
		timeField.setAllowBlank(false);
		timeField.setWidth(65);
		
		Label label = new Label(LocalizedText.INSTANCE.time());
		label.getElement().getStyle().setMarginRight(5, Unit.PX);
		label.getElement().getStyle().setMarginTop(3, Unit.PX);
		
		HorizontalPanel timePanel = new HorizontalPanel();
		timePanel.add(label);
		timePanel.add(timeField);
		
		remove(picker);
		picker = new DatePicker() {
			//Removing the resetTime from the picker
			@Override
			public void setValue(Date date, boolean fireEvents) {
				if (date == null) {
					setPickerValue(this, null);
					updatePicker(this, new DateWrapper());
				} else {
					DateWrapper wrapper = new DateWrapper(date);
					setPickerValue(picker, wrapper);
					updatePicker(this, wrapper);
				}

				XElement overElement = getPickerOverElement(this);
				if (overElement != null) {
					getAppearance().onUpdateDateStyle(overElement, DateState.OVER, false);
				}

				if (fireEvents) {
					ValueChangeEvent.fire(this, date);
				}
			}
			
			@Override
			protected void onDayClick(XElement e) {
				if (timeField.isValid())
					super.onDayClick(e);
			}
			
			@Override
			protected void onKeyLeft(NativeEvent evt) {
				//NOP
			}
			
			@Override
			protected void onKeyRight(NativeEvent evt) {
				//NOP
			}
		};
	    picker.addValueChangeHandler(new ValueChangeHandler<Date>() {
	    	@Override
	    	public void onValueChange(ValueChangeEvent<Date> event) {
	    		onPickerSelect(event);
	    	}
	    });

	    add(picker);
		
		picker.getElement().selectNode(picker.getAppearance().todayButtonSelector()).appendChild(timePanel.getElement());
	}
	
	public void setTimeRegex(String timeRegex, final String pattern) {
		regExValidator.setRegex(timeRegex);
		regExValidator.setMessages(new RegExMessages() {
			@Override
			public String regExMessage() {
				return LocalizedText.INSTANCE.invalidTimeParam(pattern);
			}
		});
	}
	
	/**
	 * Configures whether to use seconds. Defaults to false.
	 */
	public void setUseSeconds(boolean useSeconds) {
		this.useSeconds = useSeconds;
		timeField.setWidth(useSeconds ? 75 : 65);
	}
	
	/**
	 * Configures whether to use milliseconds. defaults to false.
	 */
	public void setUseMilliseconds(boolean useMilliseconds) {
		this.useMilliseconds = useMilliseconds;
		timeField.setWidth(useMilliseconds ? 100 : (useSeconds ? 75 : 65));
	}
	
	@Override
	protected void onPickerSelect(ValueChangeEvent<Date> event) {
		if (timeField.isValid()) {
			prepareDateWithString(event.getValue(), timeField.getValue());
		    super.onPickerSelect(event);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void prepareDateWithString(Date date, String time) {
		String[] timeArray = time.split(":");
		
		date.setHours(Integer.parseInt(timeArray[0]));
		date.setMinutes(Integer.parseInt(timeArray[1]));
		if (useSeconds || useMilliseconds) {
			date.setSeconds(Integer.parseInt(timeArray[2]));
			if (useMilliseconds) {
				long timeLong = date.getTime();
				
				int milliseconds = (int) (date.getTime() % 1000l);
				milliseconds = milliseconds < 0 ? milliseconds + 1000 : milliseconds;
				
				timeLong = date.getTime() - milliseconds;
				timeLong += Integer.parseInt(timeArray[3]);
				date.setTime(timeLong);
			}
		}
	}
	
	@Override
	protected void doAttachChildren() {
		super.doAttachChildren();
		ComponentHelper.doAttach(timeField);
	}
	
	@Override
	protected void doDetachChildren() {
		super.doDetachChildren();
		ComponentHelper.doDetach(timeField);
	}
	
	@Override
	public void show(Element elem, AnchorAlignment alignment) {
		keyNav.bind(null);
		super.show(elem, alignment);
		timeField.setValue(prepareTimeFromDate(picker.getValue()));
	}
	
	@SuppressWarnings("deprecation")
	protected String prepareTimeFromDate(Date date) {
		String hours = String.valueOf(date.getHours());
		if (hours.length() == 1)
			hours = "0" + hours;
		String minutes = String.valueOf(date.getMinutes());
		if (minutes.length() == 1)
			minutes = "0" + minutes;
		
		if (!useSeconds && !useMilliseconds)
			return hours + ":" + minutes;
		else {
			String seconds = String.valueOf(date.getSeconds());
			if (seconds.length() == 1)
				seconds = "0" + seconds;
			
			if (!useMilliseconds)
				return hours + ":" + minutes + ":" + seconds;
			else {
				int milliseconds = (int) (date.getTime() % 1000l);
				milliseconds = milliseconds < 0 ? milliseconds + 1000 : milliseconds;
				String milli = String.valueOf(milliseconds);
				if (milli.length() == 1)
					milli = "00" + milli;
				else if (milli.length() == 2)
					milli = "0" + milli;
				
				return hours + ":" + minutes + ":" + seconds + ":" + milli;
			}
		}
	}
	
	@Override
	public void focus() {
		super.focus();
		picker.getElement().focus();
	}

	private native void setPickerValue(DatePicker datePicker, DateWrapper date) /*-{
		datePicker.@com.sencha.gxt.widget.core.client.DatePicker::value = date;
	}-*/;
	
	private native void updatePicker(DatePicker datePicker, DateWrapper wrapper) /*-{
		datePicker.@com.sencha.gxt.widget.core.client.DatePicker::update(Lcom/sencha/gxt/core/client/util/DateWrapper;)(wrapper);
	}-*/;
	
	private native XElement getPickerOverElement(DatePicker datePicker) /*-{
		return datePicker.@com.sencha.gxt.widget.core.client.DatePicker::overElement;
	}-*/;

}
