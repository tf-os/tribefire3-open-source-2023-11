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
package com.braintribe.gwt.gme.notification.client;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.IgnoreKeyConfigurationDialog;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.orangeflattab.client.OrangeFlatTabPanelAppearance;
import com.braintribe.model.extensiondeployment.meta.ConfirmationMouseClick;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.PlainTabPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ConfirmationDialog extends ClosableWindow implements IgnoreKeyConfigurationDialog {
	
	protected TextButton cancelButton;
	protected TextButton okButton;
	protected PlainTabPanel tabPanel;
	private Future<Boolean> future;
	protected VerticalPanel mainPanel;
	private ConfirmationMouseClick confirmationMouseClick;
	private boolean altKeyPressed;
	private boolean shiftKeyPressed;
	private boolean ctrlKeyPressed;
	
	public ConfirmationDialog() {
		setModal(true);
		setClosable(false);
		setBodyBorder(false);
		setBorders(false);
		setPixelSize(500, 400);
		addStyleName("tabbedDialog");
		getHeader().setHeight(20);
		
		BorderLayoutContainer container = new BorderLayoutContainer();
		//container.setCenterWidget(prepareTabPanel(), new MarginData());
		container.setCenterWidget(prepareMainPanel(), new MarginData());
		container.setSouthWidget(prepareToolBar(), new BorderLayoutData(61));
		add(container);
	}
	
	public Future<Boolean> getConfirmation() {
		future = new Future<>();
		
		show();
		
		return future;
	}
	
	/**
	 * Configures whether the Cancl button is visible. Defaults to true.
	 */
	public void setCancelButtonVisible(boolean visible) {
		cancelButton.setVisible(visible);
	}
	
	/**
	 * Configures whether the OK button is visible. Defaults to true.
	 */
	public void setOkButtonVisible(boolean visible) {
		okButton.setVisible(visible);
	}
	
	public void setConfirmationMouseClick(ConfirmationMouseClick confirmationMouseClick) {
		this.confirmationMouseClick = confirmationMouseClick;
	}
	
	protected VerticalPanel prepareMainPanel() {
		mainPanel = new VerticalPanel();
		mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		return mainPanel;
	}
	
	protected PlainTabPanel prepareTabPanel() {
		tabPanel = new PlainTabPanel(GWT.<OrangeFlatTabPanelAppearance>create(OrangeFlatTabPanelAppearance.class));
		tabPanel.setTabScroll(true);
		tabPanel.setBorders(false);
		tabPanel.setBodyBorder(false);
		return tabPanel;
	}
	
	protected ToolBar prepareToolBar() {
		ToolBar toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.addStyleName("gmeToolbar");
		toolBar.add(new FillToolItem());
		toolBar.add(prepareCancelButton());
		toolBar.add(prepareOKButton());
		return toolBar;
	}
	
	private TextButton prepareCancelButton() {
		cancelButton = new TextButton(LocalizedText.INSTANCE.cancel()) {
			@Override
			protected void onClick(Event event) {
				shiftKeyPressed = event.getShiftKey();
				ctrlKeyPressed = event.getCtrlKey();
				altKeyPressed = event.getAltKey();
				super.onClick(event);
			}
		};
		cancelButton.setIconAlign(IconAlign.TOP);
		cancelButton.setScale(ButtonScale.LARGE);
		cancelButton.setIcon(ConstellationResources.INSTANCE.cancel());
		cancelButton.addSelectHandler(event -> hide());
		cancelButton.addStyleName("gmeGimaCancelButton");
		return cancelButton;
	}
	
	private TextButton prepareOKButton() {
		okButton = new TextButton(LocalizedText.INSTANCE.ok()) {
			@Override
			protected void onClick(Event event) {
				shiftKeyPressed = event.getShiftKey();
				ctrlKeyPressed = event.getCtrlKey();
				altKeyPressed = event.getAltKey();
				super.onClick(event);
			}
		};
		okButton.setIconAlign(IconAlign.TOP);
		okButton.setScale(ButtonScale.LARGE);
		okButton.setIcon(ConstellationResources.INSTANCE.finish());
		okButton.addSelectHandler(event -> returnOK());
		okButton.addStyleName("gmeGimaMainButton");

		return okButton;
	}
	
	@SuppressWarnings("incomplete-switch")
	private void returnOK() {
		if (confirmationMouseClick != null && !confirmationMouseClick.equals(ConfirmationMouseClick.none)) {
			switch (confirmationMouseClick) {
				case alt:
					if (!altKeyPressed)
						return;
					break;
				case ctrl:
					if (!ctrlKeyPressed)
						return;
					break;
				case shift:
					if (!shiftKeyPressed)
						return;
					break;
			}
		}
		
		super.hide();
		if (future != null)
			future.onSuccess(true);
	}
	
	@Override
	public void hide() {
		super.hide();
		if (future != null)
			future.onSuccess(false);
	}
	
	@Override
	protected void onWindowResize(int width, int height) {
		try {
			super.onWindowResize(width, height);
		} catch(Exception ex) {
			//An exception occurs when resizing for some reason... (related to touch stuff)
		}
	}
	
	protected void configureTextAreaLayoutAndMessage(TextArea textArea, String message) {
		textArea.setReadOnly(true);
		textArea.setValue(message);
		textArea.setText(message);
		textArea.setBorders(false);
		textArea.setHeight("200px");
		textArea.setWidth("500px");
		
		InputElement inputElement = textArea.getCell().getInputElement(textArea.getElement());
		Style style = inputElement.getStyle();
		style.setFontSize(14, Unit.PX);
		style.setFontWeight(FontWeight.NORMAL);
		style.setTextAlign(TextAlign.CENTER);
		style.setPaddingTop(40, Unit.PX);
		style.setProperty("border", "none");
		style.setWidth(100d, Unit.PCT);
		style.setVerticalAlign(VerticalAlign.MIDDLE);
	}

}
