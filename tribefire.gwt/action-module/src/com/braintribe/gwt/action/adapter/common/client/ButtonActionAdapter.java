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
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.ToggleButton;

/**
 * This class is an adapter from Action to GXT Button.
 * 
 * @author michel.docouto
 *
 */
public class ButtonActionAdapter implements DisposableBean {
	private Action action;
	private ButtonBase button;
	private boolean noText = false;
	private boolean ignoreIcon = false;
	private ClickHandler clickHandler = event -> {
		TriggerInfo triggerInfo = new TriggerInfo();
		triggerInfo.put(TriggerInfo.PROPERTY_EVENTOBJECT, event);
		triggerInfo.setWidget(button);
		action.perform(triggerInfo);
	};

	private class PropertyListenerImpl implements Action.PropertyListener {
		@Override
		public void propertyChanged(ActionOrGroup source, String property) {
			updateButton();
		}
	}

	private PropertyListenerImpl actionListener = new PropertyListenerImpl();
	private HandlerRegistration clickHandlerRegistration;

	/**
	 * Use {@link ButtonActionAdapter#linkActionToButton(boolean, Action, ButtonBase)} instead.
	 */
	private ButtonActionAdapter(boolean ignoreIcon, Action action, ButtonBase button) {
		this.button = button;
		this.action = action;
		this.ignoreIcon = ignoreIcon;

		action.addPropertyListener(actionListener);
		clickHandlerRegistration = button.addClickHandler(clickHandler);
		updateButton();
	}

	/**
	 * Use {@link ButtonActionAdapter#linkActionToButton(Action, ButtonBase, boolean)} instead.
	 */
	private ButtonActionAdapter(Action action, ButtonBase button, boolean noText) {
		this.button = button;
		this.action = action;
		this.noText = noText;

		action.addPropertyListener(actionListener);
		clickHandlerRegistration = button.addClickHandler(clickHandler);
		updateButton();
	}

	/**
	 * Use {@link ButtonActionAdapter#linkActionToButton(Action, ButtonBase)} instead.
	 */
	private ButtonActionAdapter(Action action, ButtonBase button) {
		this(action, button, false);
	}

	public ButtonBase getButton() {
		return button;
	}

	public Action getAction() {
		return action;
	}

	@Override
	public void disposeBean() throws Exception {
		action.removePropertyListener(actionListener);
		clickHandlerRegistration.removeHandler();
	}

	public void updateButton() {
		String id = action.getId();

		if (id != null)
			button.getElement().setId(id);

		String name = action.getName();
		if (name == null)
			name = "";

		if (!noText && !name.equals(button.getText())) {
			button.setText(name);
		}

		button.setEnabled(action.getEnabled());

		if (!ignoreIcon) {
			ImageResource icon = action.getIcon();

			if (button instanceof CustomButton) {
				CustomButton customButton = (CustomButton) button;
				customButton.getUpFace().setImage(AbstractImagePrototype.create(icon).createImage());
			}

		}

		if (action.getTooltip() != null) {
			button.setTitle(action.getTooltip());
		} else if (noText) {
			button.setTitle(action.getName());
		}

		if (button instanceof ToggleButton) {
			ToggleButton toggleButton = (ToggleButton) button;
			toggleButton.setDown(action.getToggled());
		}

		button.setVisible(!action.getHidden());
	}

	/**
	 * Links an {@link Action} to a {@link ButtonBase}.
	 */
	public static ButtonActionAdapter linkActionToButton(Action action, ButtonBase button) {
		return new ButtonActionAdapter(action, button);
	}

	/**
	 * Links an {@link Action} to a {@link ButtonBase}.
	 * 
	 * @param noText
	 *            - If true, use no text in the button (only use toolTip). Defaults to false.
	 */
	public static ButtonActionAdapter linkActionToButton(Action action, ButtonBase button, boolean noText) {
		return new ButtonActionAdapter(action, button, noText);
	}

	/**
	 * Links an {@link Action} to a {@link ButtonBase}.
	 * 
	 * @param ignoreIcon
	 *            - If true, this adapter won't handle icon changes. Defaults to false.
	 */
	public static ButtonActionAdapter linkActionToButton(boolean ignoreIcon, Action action, ButtonBase button) {
		return new ButtonActionAdapter(ignoreIcon, action, button);
	}
}
