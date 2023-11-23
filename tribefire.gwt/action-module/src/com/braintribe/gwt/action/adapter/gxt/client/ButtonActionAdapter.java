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
package com.braintribe.gwt.action.adapter.gxt.client;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.ActionOrGroup;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.widget.core.client.button.CellButtonBase;
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

/**
 * This class is an adapter from Action to GXT Button.
 * 
 * @author michel.docouto
 *
 */
public class ButtonActionAdapter implements DisposableBean {
	private Action action;
	private CellButtonBase<?> button;
	private ImageResource lastIcon;
	private boolean noText = false;
	private boolean ignoreIcon = false;
	private PropertyListenerImpl actionListener = new PropertyListenerImpl();
	private SelectHandler selectionHandler;
	HandlerRegistration handlerRegistration;

	private class PropertyListenerImpl implements Action.PropertyListener {
		@Override
		public void propertyChanged(ActionOrGroup source, String property) {
			updateButton();
		}
	}

	private class SelectionHandlerImpl implements SelectHandler {
		@Override
		public void onSelect(SelectEvent event) {
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.put(TriggerInfo.PROPERTY_COMPONENTEVENT, event);
			triggerInfo.setWidget(button);
			action.perform(triggerInfo);
		}
	}

	/**
	 * Use {@link ButtonActionAdapter#linkActionToButton(boolean, Action, CellButtonBase)} instead.
	 */
	private ButtonActionAdapter(boolean ignoreIcon, Action action, CellButtonBase<?> button) {
		this.button = button;
		this.action = action;
		this.ignoreIcon = ignoreIcon;

		action.addPropertyListener(actionListener);
		handlerRegistration = button.addSelectHandler(getSelectionHandler());
		updateButton();
	}

	/**
	 * Use {@link ButtonActionAdapter#linkActionToButton(Action, CellButtonBase, boolean)} instead.
	 */
	private ButtonActionAdapter(Action action, CellButtonBase<?> button, boolean noText) {
		this.button = button;
		this.action = action;
		this.noText = noText;

		action.addPropertyListener(actionListener);
		handlerRegistration = button.addSelectHandler(getSelectionHandler());
		updateButton();
	}

	/**
	 * Use {@link ButtonActionAdapter#linkActionToButton(Action, CellButtonBase)} instead.
	 */
	private ButtonActionAdapter(Action action, CellButtonBase<?> button) {
		this(action, button, false);
	}

	protected SelectHandler getSelectionHandler() {
		if (selectionHandler == null)
			selectionHandler = new SelectionHandlerImpl();
		return selectionHandler;
	}

	public CellButtonBase<?> getButton() {
		return button;
	}

	public void updateButton() {
		String id = action.getId();

		if (id != null)
			button.setId(id);

		String name = action.getName();
		if (name == null)
			name = "";

		if (!noText && !name.equals(button.getText()))
			button.setText(name);

		button.setEnabled(action.getEnabled());

		if (!ignoreIcon) {
			ImageResource icon = action.getIcon();
			if (icon != null && icon != lastIcon) {
				button.setIcon(icon);
				lastIcon = icon;
			}
		} else {
			ImageResource icon = action.getHoverIcon();
			if (icon != null && icon != lastIcon) {
				button.setIcon(icon);
				lastIcon = icon;
			}
		}

		if (action.getTooltip() != null)
			button.setToolTip(action.getTooltip());
		else if (noText)
			button.setToolTip(name);

		if (button instanceof ToggleButton) {
			ToggleButton toggleButton = (ToggleButton) button;
			toggleButton.setValue(action.getToggled());
		}

		button.setVisible(!action.getHidden());
	}

	@Override
	public void disposeBean() throws Exception {
		action.removePropertyListener(actionListener);
		if (handlerRegistration != null)
			handlerRegistration.removeHandler();
	}

	/**
	 * Links an {@link Action} to a {@link CellButtonBase}.
	 */
	public static ButtonActionAdapter linkActionToButton(Action action, CellButtonBase<?> button) {
		return new ButtonActionAdapter(action, button);
	}

	/**
	 * Links an {@link Action} to a {@link CellButtonBase}.
	 * 
	 * @param noText
	 *            - If true, use no text in the button (only use toolTip). Defaults to false.
	 */
	public static ButtonActionAdapter linkActionToButton(Action action, CellButtonBase<?> button, boolean noText) {
		return new ButtonActionAdapter(action, button, noText);
	}

	/**
	 * Links an {@link Action} to a {@link CellButtonBase}.
	 */
	public static ButtonActionAdapter linkActionToButton(boolean ignoreIcon, Action action, CellButtonBase<?> button) {
		return new ButtonActionAdapter(ignoreIcon, action, button);
	}
}
