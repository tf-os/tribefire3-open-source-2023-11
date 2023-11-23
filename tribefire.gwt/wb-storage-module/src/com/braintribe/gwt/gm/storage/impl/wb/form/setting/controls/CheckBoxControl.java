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
package com.braintribe.gwt.gm.storage.impl.wb.form.setting.controls;

import com.braintribe.gwt.gm.storage.impl.wb.resources.WbStorageUiResources;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;

public class CheckBoxControl extends ContentPanel implements ClickHandler {

	/********************************** Variables **********************************/

	private ImageElement checkBoxIcon = null;
	private boolean isCheckBoxEnabled = true;
	private boolean checkBoxState = false;

	/******************************* CheckBoxControl *******************************/

	public CheckBoxControl() {
		super();

		this.setHeaderVisible(false);
		this.setDeferHeight(false);
		this.setBodyBorder(false);
		this.setBorders(false);

		// Needed to enable events
		addWidgetToRootPanel(this);
		initializeControl();
	}

	private static void addWidgetToRootPanel(final Widget widget) {
		// Add to RootPanel
		RootPanel.get().add(widget);
	}

	private void initializeControl() {
		this.checkBoxIcon = Document.get().createImageElement();
		this.getElement().appendChild(this.checkBoxIcon);
		setChecked(this.checkBoxState);

		// Draw layout
		forceLayout();

		// Add Event to Element
		this.addDomHandler(this, ClickEvent.getType());
	}

	/******************************** Event Methods ********************************/

	@Override
	public void onClick(final ClickEvent event) {
		final NativeEvent nativeEvent = event.getNativeEvent();
		final Element targetElement = nativeEvent.getEventTarget().cast();

		if (this.checkBoxIcon.isOrHasChild(targetElement)) {
			if (this.isCheckBoxEnabled == true) {
				setChecked(!this.checkBoxState);
			}
		}
	}

	/******************************* Control Methods *******************************/

	public void setCheckBoxClassName(final String className) {
		if (this.checkBoxIcon != null) {
			this.checkBoxIcon.setClassName(className);
		}
	}

	public String getCheckBoxClassName() {
		if (this.checkBoxIcon != null) {
			return this.checkBoxIcon.getClassName();
		}

		return null;
	}

	public void setChecked(final boolean value) {
		this.checkBoxState = value;

		if (this.checkBoxIcon != null) {
			final WbStorageUiResources uiResources = WbStorageUiResources.INSTANCE;
			this.checkBoxIcon.setSrc((value == true ? uiResources.checked() : uiResources.unchecked()).getSafeUri().asString());
		}
	}

	public boolean isChecked() {
		return this.checkBoxState;
	}

	@Override
	public void setEnabled(final boolean value) {
		this.isCheckBoxEnabled = value;

		if (this.checkBoxIcon != null) {
			this.checkBoxIcon.getStyle().setOpacity(value == true ? 1d : 0.5d);
		}
	}

	@Override
	public boolean isEnabled() {
		return this.isCheckBoxEnabled;
	}
}
