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

import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.EntityFieldDialog;
import com.braintribe.gwt.gxt.gxtresources.css.GxtResources;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ExtendedStringDialog extends ClosableWindow implements HasValueChangeHandlers<String>, EntityFieldDialog<GmStringType> {
	
	protected boolean applyChanges = false;
	public TextButton closeButton;
	protected boolean cancelChanges;
	protected BorderLayoutContainer container;
	private boolean readOnly = false;
	private AbstractGridEditing<?> gridEditing;
	private String caption = LocalizedText.INSTANCE.multiLineEditor();
	
	public ExtendedStringDialog() {
		setDefaultSize();
		setBodyBorder(false);
		setBorders(false);
		
		sinkEvents(Event.ONCHANGE);
		
		getHeader().setHeight(20);
		setHeading(caption);
		setModal(true);
		setClosable(false);
		setOnEsc(true);
		addStyleName("extendedStringDialog");
		addStyleName("gmeDialog");
		setMinWidth(450);
		setMinHeight(200);

		/*
		this.addShowHandler(new ShowHandler() {
			@Override
			public void onShow(ShowEvent event) {
				applyChanges = false;
				cancelChanges = false;
			}
		});
		*/
		
		container = new BorderLayoutContainer();
		container.setSouthWidget(prepareToolBar(), new BorderLayoutData(61));
        container.setStyleName("ExtendedStringDialogContainer");
		
		this.setWidget(container);		
	}

	@Override
	public void show() {		
		initSettings();
		super.show();
	}	
	
	/**
	 * Applies the changes
	 */
	public void applyChanges() {
		applyChanges = true;		
	}
	
	/**
	 * Cancels the changes
	 */
	public void cancelChanges() {
		cancelChanges = true;
		ExtendedStringDialog.super.hide();
	}
	
	@Override
	public void hide() {
		cancelChanges = true;
		super.hide();
	}
	
	private TextButton prepareCloseButton() {
		closeButton = new TextButton(LocalizedText.INSTANCE.apply(), GxtResources.INSTANCE.apply());
		closeButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
		closeButton.setIconAlign(IconAlign.TOP);
		closeButton.setScale(ButtonScale.LARGE);
		
		closeButton.addSelectHandler(event -> {
			applyChanges();
			ExtendedStringDialog.super.hide();			
		});		
		return closeButton;
	}
	
	protected ToolBar prepareToolBar() {
		ToolBar toolBar = new ToolBar();
		
		toolBar.setEnableOverflow(false);
		toolBar.add(new FillToolItem());
		toolBar.add(prepareCloseButton());
		return toolBar;
	}	
	
	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public Boolean isReadOnly() {
		return this.readOnly;
	}
	
	public boolean isApplyChanges() {
		return applyChanges;
	}
	
	public void setDefaultSize() {
		int maxheight = Document.get().getClientHeight();
		int maxwidth = Document.get().getClientWidth();
		
		this.setWidth(Math.min(maxwidth, 1000));
		this.setHeight(Math.min(maxheight, 500));		
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return this.addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setIsFreeInstantiation(Boolean isFreeInstantiation) {
		// NOP
	}

	@Override
	public void performManipulations() {
		//NOP
	}

	@Override
	public boolean hasChanges() {
		return cancelChanges;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		// NOP
		
	}

	@Override
	public void setEntityValue(GmStringType entityValue) {
		// NOP
	}

	@SuppressWarnings("unused")
	public void setString(String string) {
		//NOP
	}

	public String getString() {
		return null;
	}

	@SuppressWarnings("unused")
	public void setCodeFormat(String codeFormat) {
		//NOP		
	}

	public String getCodeFormat() {
		return null;
	}
	
	/**
	 * Configures the parent {@link AbstractGridEditing}.
	 */
	public void configureGridEditing(AbstractGridEditing<?> gridEditing) {
		this.gridEditing = gridEditing;
	}
	
	@Override
	public AbstractGridEditing<?> getParentGridEditing() {
		return gridEditing;
	}

	@Override
	public Widget getView() {
		return container;
	}	
	
	public void setCaption(String caption) {
		this.caption = caption;
		setHeading(caption);
	}
	
	public String getCaption() {
		return this.caption;
	}
	
	public void initSettings() {
		if (isReadOnly()) {
			closeButton.setText(LocalizedText.INSTANCE.close());
			closeButton.removeToolTip();
			closeButton.setIcon(GmViewActionResources.INSTANCE.close());			
		}
		
		applyChanges = false;
		cancelChanges = false;		
	}
}

