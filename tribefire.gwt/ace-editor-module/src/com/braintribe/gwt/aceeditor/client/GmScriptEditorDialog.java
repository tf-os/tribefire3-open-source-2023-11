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
package com.braintribe.gwt.aceeditor.client;

import com.braintribe.gwt.gmview.client.EntityFieldDialog;
import com.braintribe.gwt.gmview.util.client.TextResourceManager;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import tribefire.extension.scripting.model.deployment.Script;

public class GmScriptEditorDialog extends ClosableWindow implements HasValueChangeHandlers<Script>, EntityFieldDialog<Script> {
	
	private Script script;
	private AceEditor aceEditor;
	private boolean isFreeInstantiation;
	private String caption = LocalizedText.INSTANCE.source();
	protected boolean applyChanges = false;
	protected boolean cancelChanges;
	private boolean readOnly = false;
	private AbstractGridEditing<?> gridEditing;
	private BorderLayoutContainer aceEditorContainer;
	private String initialScriptContent;
		
	public GmScriptEditorDialog() {
		aceEditorContainer = new BorderLayoutContainer();
		setLayoutData(new BorderLayoutContainer());
		setHeading(caption);
		setSize("800px", "400px");
		setResizable(true);
		setClosable(false);
		setOnEsc(false);
		setModal(true);
		
		aceEditor = GmScriptEditorUtil.prepareAceEditor();
		
		aceEditorContainer.setCenterWidget(aceEditor);
		
		setWidget(aceEditorContainer);
		
		final TextButton cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		final TextButton okButton = new TextButton(LocalizedText.INSTANCE.ok());
		
		SelectHandler selectHandler = event -> {
			if (event.getSource() == cancelButton && script != null)
				cancelChanges();
			
			if (event.getSource() == okButton)
				applyChanges();
			
			GmScriptEditorDialog.this.hide();
		};
					
		okButton.addSelectHandler(selectHandler);
		cancelButton.addSelectHandler(selectHandler);
		addButton(okButton);
		addButton(cancelButton);
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
		if (script == null || script.getSource() == null)
			aceEditor.setText(null);
		else
			aceEditor.setText(initialScriptContent);
	}

	public String getScriptText() {
		return aceEditor.getText();
	}
	
	@Override
	public void setEntityValue(Script entityValue) {
		this.script = entityValue;
		GmScriptEditorUtil.setScriptValue(script, aceEditor) //
				.andThen(content -> initialScriptContent = content) //
				.onError(e -> {
					ErrorDialog.show(GmScriptEditorLocalizedText.INSTANCE.retrieveError(), e);
					hide();
				});
	}

	@Override
	public void performManipulations() {
		if (script == null || script.getSource() == null)
			return;
		
		Property sourceProperty = script.entityType().getProperty("source");
		TextResourceManager.saveResourceContent(aceEditor.getText(), script.getSource(), sourceProperty, script).onError(e -> {
			ErrorDialog.show(GmScriptEditorLocalizedText.INSTANCE.saveError(), e);
		});
		initialScriptContent = aceEditor.getText();
	}

	@Override
	public boolean hasChanges() {
		if (!applyChanges)
			return false;
		
		if (script == null || script.getSource() == null || initialScriptContent == null)
			return aceEditor.getText() != null;
		
		return !initialScriptContent.equals(aceEditor.getText());
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//NOP
	}
	
	@Override
	public void show() {
		//RVE - for New Instance show Script Editor only as a GmView, not need show both together with Dialog
		if (isFreeInstantiation)
			return;
		
		super.show();
		
		int currentHeight = getOffsetHeight();
		int computedHeight = Math.min(Document.get().getClientHeight(), currentHeight);
		if (computedHeight != currentHeight)
			setHeight(computedHeight);
	}

	@Override
	public void setIsFreeInstantiation(Boolean isFreeInstantiation) {
		this.isFreeInstantiation = isFreeInstantiation;		
	}
	public void setCaption(String caption) {
		this.caption = caption;
		setHeading(caption);
	}
	
	public String getCaption() {
		return this.caption;	
	}
	
	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
		aceEditor.setReadOnly(readOnly);
	}
	
	public Boolean isReadOnly() {
		return this.readOnly;
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
		return aceEditorContainer;
	}
	
	public void updateContainer() {
		setWidget(aceEditorContainer);
	}

	@Override
	public void hide() {
		cancelChanges = true;
		super.hide();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Script> handler) {
		return this.addHandler(handler, ValueChangeEvent.getType());
	}
	
	public boolean canFireDialog() {
		return true;
	}
}
