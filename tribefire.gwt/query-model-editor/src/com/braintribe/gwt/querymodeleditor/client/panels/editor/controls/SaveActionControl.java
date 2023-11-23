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
package com.braintribe.gwt.querymodeleditor.client.panels.editor.controls;

import com.braintribe.cfg.Configurable;
import com.braintribe.gwt.querymodeleditor.client.resources.LocalizedText;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class SaveActionControl extends ContentPanel implements ClickHandler, ResizeHandler {

	/********************************** Variables **********************************/

	private Menu saveActionValuesMenu = null;
	private AnchorElement saveActionAnchor = null;
	private DropDownControl saveActionDropDown = null;

	private boolean isSaveButtonEnabled = false;
	private boolean isControlEnabled = true;
	private boolean isDefaultSaveAs = true;

	private ClickHandler saveActionClickedHandler = null;
	private SelectionHandler<Item> saveActionSelectedHandler = null;
	private SelectionHandler<Item> settingsActionSelectedHandler = null;

	/****************************** SaveActionControl ******************************/

	@Configurable
	public void setSaveActionClicked(final ClickHandler saveActionClickedHandler) {
		this.saveActionClickedHandler = saveActionClickedHandler;
	}

	@Configurable
	public void setSaveActionSelected(final SelectionHandler<Item> saveActionSelectedHandler) {
		this.saveActionSelectedHandler = saveActionSelectedHandler;
	}

	@Configurable
	public void setSettingsActionSelected(final SelectionHandler<Item> settingsActionSelectedHandler) {
		this.settingsActionSelectedHandler = settingsActionSelectedHandler;
	}

	public SaveActionControl() {
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
		// Add drop down & draw layout
		this.add(getSaveActionDropDown());
		forceLayout();

		// Add Events to Element
		this.addDomHandler(this, ClickEvent.getType());
		this.addHandler(this, ResizeEvent.getType());

		enableSaveButton(this.isSaveButtonEnabled);
		enableDropDown(this.isDefaultSaveAs);
	}

	/************************** Create SubControl Methods **************************/

	private DropDownControl getSaveActionDropDown() {
		if (this.saveActionDropDown == null) {
			this.saveActionDropDown = new DropDownControl();
			this.saveActionDropDown.setMenu(getSaveActionValuesMenu());
			this.saveActionDropDown.setTextAreaChild(getSaveActionAnchor());
		}

		return this.saveActionDropDown;
	}

	private Menu getSaveActionValuesMenu() {
		if (this.saveActionValuesMenu == null) {
			this.saveActionValuesMenu = new Menu();

			final MenuItem saveAsMenuItem = new MenuItem(LocalizedText.INSTANCE.saveAs());
			saveAsMenuItem.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(final SelectionEvent<Item> event) {
					if (SaveActionControl.this.isDefaultSaveAs == false) {
						if (SaveActionControl.this.saveActionSelectedHandler != null) {
							SaveActionControl.this.saveActionSelectedHandler.onSelection(event);
						}
					}
				}
			});
			final MenuItem settingsMenuItem = new MenuItem(LocalizedText.INSTANCE.settings());
			settingsMenuItem.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(final SelectionEvent<Item> event) {
					if (SaveActionControl.this.isDefaultSaveAs == false) {
						if (SaveActionControl.this.settingsActionSelectedHandler != null) {
							SaveActionControl.this.settingsActionSelectedHandler.onSelection(event);
						}
					}
				}
			});

			this.saveActionValuesMenu.add(saveAsMenuItem);
			this.saveActionValuesMenu.add(settingsMenuItem);
		}

		return this.saveActionValuesMenu;
	}

	private AnchorElement getSaveActionAnchor() {
		if (this.saveActionAnchor == null) {
			this.saveActionAnchor = Document.get().createAnchorElement();
			this.saveActionAnchor.setClassName("gwt-Anchor queryModelEditorAnchorAction");
			this.saveActionAnchor.setHref("#");
		}

		return this.saveActionAnchor;
	}

	/******************************** Event Methods ********************************/

	@Override
	public void onClick(final ClickEvent event) {
		final NativeEvent nativeEvent = event.getNativeEvent();
		final Element targetElement = nativeEvent.getEventTarget().cast();

		if (getSaveActionAnchor().isOrHasChild(targetElement)) {
			if (this.isControlEnabled == true && this.isSaveButtonEnabled == true) {
				if (this.saveActionClickedHandler != null) {
					this.saveActionClickedHandler.onClick(event);
				}
			}
		}
	}

	@Override
	public void onResize(final ResizeEvent event) {
		// Draw layout
		forceLayout();
	}

	/******************************* Control Methods *******************************/

	public void setDefaultSaveAs(final boolean value) {
		// Enable/Disable edit mode
		this.isDefaultSaveAs = value;

		// Set default text of anchor element
		if (this.saveActionAnchor != null) {
			final String buttonText = (value ? LocalizedText.INSTANCE.saveAs() : LocalizedText.INSTANCE.save());

			this.saveActionAnchor.setInnerText(buttonText);
			this.saveActionAnchor.setName(buttonText);
		}

		// Set DropDown enabled
		enableDropDown(!value);
	}

	public boolean isDefaultSaveAs() {
		return this.isDefaultSaveAs;
	}

	public void enableSaveButton(final boolean value) {
		this.isSaveButtonEnabled = value;

		// Enable/Disable save button
		if (saveActionAnchor != null) {
			Style style = saveActionAnchor.getStyle();
			style.setOpacity(value ? 1d : 0.5d);
			style.setCursor(value ? Cursor.POINTER : Cursor.DEFAULT);
		}
	}

	public boolean isSaveButtonEnabled() {
		return this.isSaveButtonEnabled;
	}

	public void enableDropDown(final boolean value) {
		// Enable/Disable drop down element
		if (this.saveActionDropDown != null) {
			this.saveActionDropDown.enableDropDown(this.isDefaultSaveAs ? false : value);
		}
	}

	public boolean isDropDownEnabled() {
		if (this.saveActionDropDown != null) {
			return this.saveActionDropDown.isDropDownEnabled();
		}

		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.isControlEnabled = enabled;

		if (this.saveActionDropDown != null) {
			this.saveActionDropDown.setEnabled(enabled);
		}
	}

	@Override
	public boolean isEnabled() {
		return this.isControlEnabled;
	}
}
