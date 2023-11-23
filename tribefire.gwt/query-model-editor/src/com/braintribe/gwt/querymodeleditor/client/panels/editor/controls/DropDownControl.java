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
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorResources;
import com.braintribe.gwt.querymodeleditor.client.resources.QueryModelEditorTemplates;
import com.braintribe.gwt.querymodeleditor.client.resources.TemplateConfigurationBean;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.dom.DomQuery;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.menu.Menu;

public class DropDownControl extends ContentPanel implements ClickHandler, ResizeHandler {

	/********************************** Constants **********************************/

	private final String dropDownTextAreaId = this.hashCode() + "_dropDownTextArea";
	private final String dropDownIconId = this.hashCode() + "_dropDownIcon";

	private final String selectDropDownTextArea = "div[@id='" + this.dropDownTextAreaId + "']";
	private final String selectDropDownIcon = "img[@id='" + this.dropDownIconId + "']";

	/********************************** Variables **********************************/

	private DivElement dropDownTextArea = null;
	private ImageElement dropDownIcon = null;

	private ParagraphElement dropDownText = null;
	private Menu dropDownMenu = null;

	private boolean isDropDownEnabled = true;
	private boolean isControlEnabled = true;

	/******************************* DropDownControl *******************************/

	@Configurable
	public void setMenu(Menu dropDownMenu) {
		this.dropDownMenu = dropDownMenu;
		enableDropDown(isDropDownEnabled);
	}

	public DropDownControl() {
		this.setHeaderVisible(false);
		this.setDeferHeight(false);
		this.setBodyBorder(false);
		this.setBorders(false);

		// Needed to enable events
		addWidgetToRootPanel(this);
		initializeControl();
	}

	private static void addWidgetToRootPanel(Widget widget) {
		// Add to RootPanel
		RootPanel.get().add(widget);
	}

	private void initializeControl() {
		// Get and initialize template Elements
		TemplateConfigurationBean bean = new TemplateConfigurationBean();
		bean.setDropDownTextAreaId(dropDownTextAreaId);
		bean.setDropDownIconId(dropDownIconId);
		add(new HTML(QueryModelEditorTemplates.INSTANCE.dropDownControl(bean)));
		dropDownTextArea = DomQuery.selectNode(this.selectDropDownTextArea, getElement()).cast();

		dropDownIcon = DomQuery.selectNode(this.selectDropDownIcon, getElement()).cast();
		if (dropDownIcon != null) {
			dropDownIcon.setSrc(QueryModelEditorResources.INSTANCE.dropDown().getSafeUri().asString());
		}

		// Draw layout
		forceLayout();

		// Add Events to Element
		addDomHandler(this, ClickEvent.getType());
		addHandler(this, ResizeEvent.getType());

		enableDropDown(isDropDownEnabled);
	}

	/******************************** Event Methods ********************************/

	@Override
	public void onClick(final ClickEvent event) {
		NativeEvent nativeEvent = event.getNativeEvent();
		Element targetElement = nativeEvent.getEventTarget().cast();

		if (isControlEnabled && isDropDownEnabled && dropDownMenu != null && dropDownMenu.isEnabled() && dropDownIcon != null && dropDownIcon.isOrHasChild(targetElement))
			dropDownMenu.showAt(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
	}	

	@Override
	public void onResize(final ResizeEvent event) {
		// Draw layout
		forceLayout();
	}

	/******************************* Control Methods *******************************/

	public void setText(String text) {
		if (dropDownText == null)
			dropDownText = Document.get().createPElement();

		dropDownText.setInnerText(text);
		setHTML(dropDownText.toString());
	}

	public void setHTML(String html) {
		dropDownTextArea.setInnerHTML(html);
	}

	public void setTextAreaChild(Node textAreaChild) {
		dropDownTextArea.removeAllChildren();
		dropDownTextArea.appendChild(textAreaChild);
	}

	public void enableDropDown(boolean value) {
		isDropDownEnabled = value;

		if (dropDownText != null)
			dropDownText.getStyle().setOpacity(value == true ? 1d : 0.5d);
		
		if (dropDownIcon != null) {
			XElement xDropDownIcon = XElement.as(dropDownIcon);
			xDropDownIcon.setVisible(value);
		}
		
		if (dropDownMenu != null)
			dropDownMenu.setEnabled(value);
	}

	public boolean isDropDownEnabled() {
		return this.isDropDownEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		isControlEnabled = enabled;
		if (dropDownIcon != null)
			dropDownIcon.getStyle().setVisibility(enabled ? Visibility.VISIBLE : Visibility.HIDDEN);
	}

	@Override
	public boolean isEnabled() {
		return isControlEnabled;
	}
}
