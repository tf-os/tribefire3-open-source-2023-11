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
package com.braintribe.gwt.action.adapter.common.client;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.ActionOrGroup;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class is an adapter from Actions to GWT Labels
 * @author michel.docouto
 *
 */
public class LabelActionAdapter {
	
	private Action action;
	private Label label;
	
	private class PropertyListenerImpl implements Action.PropertyListener {
		@Override
		public void propertyChanged(ActionOrGroup source, String property) {
			updateLabel();
		}
	}
	
	private class LabelListenerImpl implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {
			Widget sender = (Widget)event.getSource();
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.setWidget(sender);
			action.perform(triggerInfo);
		}
	}
	
	/**
	 * Use {@link LabelActionAdapter#linkActionToLabel(Action, Label)} instead.
	 */
	private LabelActionAdapter(Action action, Label label) {
		this.label = label;
		this.action = action;

		action.addPropertyListener(new PropertyListenerImpl());
		label.addClickHandler(new LabelListenerImpl());
		updateLabel();
	}
	
	public void updateLabel() {
		String name = action.getName();
		if (name == null) name = "";
		if (!name.equals(label.getText())) {
			label.setText(name);
		}
		
		String tooltip = action.getTooltip();
		if (tooltip != null && !tooltip.equals(label.getTitle())) {
			label.setTitle(action.getTooltip());
		}
		
		label.setVisible(!action.getHidden());
		
		String styleName = action.getStyleName();
		if (styleName != null && !styleName.equals(label.getStyleName())) {
			label.setStyleName(action.getStyleName());
		}
	}
	
	/**
	 * Links an {@link Action} to a {@link Label}.
	 */
	public static LabelActionAdapter linkActionToLabel(Action action, Label label) {
		return new LabelActionAdapter(action, label);
	}

}
