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
package com.braintribe.gwt.gme.verticaltabpanel.client;

import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class RenameDialog extends ClosableWindow implements Function<String, Future<String>> {
	
	private Future<String> future;
	private MaxLengthTextField textField;
	private TextButton cancelButton;
	private TextButton applyButton;
	private ToolBar toolBar;
	private int maxTextLength = 255;
	
	public RenameDialog() {
		addStyleName("gmeVerticalTabRenameDialog");
		setSize("400px", "150px");
		setClosable(false);
		setModal(true);
		setBodyBorder(false);
		setBorders(false);
		setBodyStyle("backgroundColor:white");
		setHeading(LocalizedText.INSTANCE.renameTab());
		setHeaderVisible(true);
		
		add(prepareLayout());
		checkButtons();
	}

	@Override
	public Future<String> apply(String name) {
		show();
		checkButtons();
		textField.setEmptyText(name);
		textField.setValue("");
		
		Scheduler.get().scheduleDeferred(RenameDialog.this.textField::focus);			
		
		this.future = new Future<>();
		return this.future;		
	}
	
	private Widget prepareLayout() {
		ContentPanel panel = new ContentPanel();
		panel.addStyleName("gmeVerticalTabRenameDialogPanel");
		panel.setBodyBorder(false);
		panel.setBorders(false);
		panel.setHeaderVisible(false);
		
		BorderLayoutContainer borderLayoutContainer = new BorderLayoutContainer();
		borderLayoutContainer.setBorders(false);
		add(borderLayoutContainer); 
		borderLayoutContainer.add(prepareTextArea());
		borderLayoutContainer.setSouthWidget(prepareToolBar());
		
		panel.add(borderLayoutContainer);
		return panel;
	}
	
	private ToolBar prepareToolBar() {
		toolBar = new ToolBar();
		toolBar.setBorders(false);
		
		toolBar.add(new FillToolItem());
		toolBar.add(prepareApplyButton());
		toolBar.add(prepareCancelButton());
		
		return toolBar;		
	}
	
	protected TextButton prepareCancelButton() {
		cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		cancelButton.setToolTip(LocalizedText.INSTANCE.cancel());
		cancelButton.setIconAlign(IconAlign.TOP);
		cancelButton.setScale(ButtonScale.LARGE);
		cancelButton.setIcon(ConstellationResources.INSTANCE.cancel());
		cancelButton.addSelectHandler(event -> hide());

		return cancelButton;
	}
		
	protected TextButton prepareApplyButton() {
		applyButton = new TextButton(LocalizedText.INSTANCE.rename());
		applyButton.setToolTip(LocalizedText.INSTANCE.rename());
		applyButton.setIconAlign(IconAlign.TOP);
		applyButton.setScale(ButtonScale.LARGE);
		applyButton.setIcon(ConstellationResources.INSTANCE.finish());
		applyButton.setEnabled(true);
		applyButton.addSelectHandler(event -> returnName());

		return applyButton;
	}

	private void returnName() {
		hide();
		String name = textField.getValue();
		if (name != null && !name.isEmpty()) {
			name = SafeHtmlUtils.htmlEscape(name);
			future.onSuccess(name);
		}
	}
			
	private Widget prepareTextArea() {
		textField = new MaxLengthTextField(this.maxTextLength);
		textField.addStyleName("renameInputTextField");
		textField.setHeight(25);
		textField.setWidth(380);
		textField.setAutoValidate(true);
				
		textField.addHandler(event -> {				
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && applyButton.isEnabled())
				returnName();
			event.stopPropagation();
		}, KeyUpEvent.getType());
		
		textField.addHandler(event -> checkButtons(), ValueChangeEvent.getType());		
		
		return textField;
	}
	
	private void checkButtons() {
		//boolean enabled = !((this.textField.getValue() == null) || (this.textField.getValue().isEmpty()));
		String value = textField.getValue();
		boolean enabled = value != null && !value.isEmpty() && textField.validate();
		this.applyButton.setEnabled(enabled);
	}
	
}
