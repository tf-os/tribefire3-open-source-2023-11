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
package com.braintribe.gwt.gmview.client.input;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.HasValue;

public abstract class RangeInputElement<T> extends FlowPanel implements HasValue<T>, HasName {
	
	List<ValueChangeHandler<T>> handlers = new ArrayList<>();
//	private boolean suppressFire = false;

	public RangeInputElement() {
		super("input");
		getElement().setAttribute("type", "range");
		
		addInputHandler(value -> fireValueChange());
		
		setMin(0);
		setStep(1);
	}
	
	public void setEnabled(boolean enabled) {
	    getElement().setPropertyBoolean("disabled", !enabled);
	}

	public void setMin(double min) {
		getElement().setAttribute("min", min + "");
	}
	
	public void setMax(double max) {
		getElement().setAttribute("max", max + "");
	}
	
	public void setStep(double step) {
		getElement().setAttribute("step", step + "");
	}
	
	@Override
	public void setName(String name) {
		getElement().setAttribute("name", name);
	}
	
	@Override
	public String getName() {
		return getElement().getAttribute("name");
	}
	
	public void addInputHandler(InputEventHandler<T> handler){
		_addInputHandler(getElement(), handler);
	}
	
	public native void _addInputHandler(Element el, InputEventHandler<T> handler) /*-{
		el.addEventListener("input", handler);
	}-*/;
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
		handlers.add(handler);
		return () -> handlers.remove(handler);		
	}

	@Override
	public T getValue() {
		return parse(_getValue(getElement()));
	}
	
	public abstract T parse(String v);
	
	private final native String _getValue(Element el) /*-{
		return el.value;
	}-*/;	

	@Override
	public void setValue(T value) {
		setValue(value, true);
	}	

	@Override
	public void setValue(T value, boolean fireEvents) {
//		this.suppressFire = !fireEvents;
		_setValue(getElement(), value);
	}
	
	private final native String _setValue(Element el, T v) /*-{
		el.value = v;				
	}-*/;
	
	protected void fireValueChange() {
		handlers.forEach(h -> {
			h.onValueChange(new RangeInputChangeEvent(getValue()));
		});	
	}
	
	class RangeInputChangeEvent extends ValueChangeEvent<T>{
		public RangeInputChangeEvent(T value) {
			super(value);
		}
	}	

}
