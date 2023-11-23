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
package com.braintribe.gwt.genericmodelgxtsupport.client.field;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;

public class ClickableComboBoxCell<T> extends ComboBoxCell<T> {
	
	private TriggerClickListener triggerClickListener;

	public ClickableComboBoxCell(ListStore<T> store, LabelProvider<? super T> labelProvider, SafeHtmlRenderer<T> renderer) {
		super(store, labelProvider, renderer);
	}
	
	public void setTriggerClickListener(TriggerClickListener triggerClickListener) {
		this.triggerClickListener = triggerClickListener;
	}

	@Override
	public void onTriggerClick(Context context, XElement parent, NativeEvent event, T value, ValueUpdater<T> updater) {
		if (triggerClickListener == null || !triggerClickListener.onTriggerClicked())
			super.onTriggerClick(context, parent, event, value, updater);
	}

	/*
	 * Overriding this so the edition is finished if the user selects something (of course, only if the selection is valid).
	 */
	@Override
	protected void onViewClick(XElement parent, NativeEvent event, boolean focus, boolean takeSelected) {
		super.onViewClick(parent, event, focus, takeSelected);
		
		Element elem = getListView().findElement((Element) event.getEventTarget().cast());
		if (elem != null || takeSelected)
			Scheduler.get().scheduleDeferred(() -> triggerBlur(lastContext, lastParent, lastValue, lastValueUpdater));
	}
	
	@FunctionalInterface
	public interface TriggerClickListener {
		/**
		 * Fired when the trigger is clicked. If true is returned by this, then the event is cancelled and the default trigger click is not handled.
		 */
		public boolean onTriggerClicked();
	}

}
