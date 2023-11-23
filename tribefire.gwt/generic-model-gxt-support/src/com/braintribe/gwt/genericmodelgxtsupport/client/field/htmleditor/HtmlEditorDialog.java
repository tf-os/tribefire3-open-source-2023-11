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
package com.braintribe.gwt.genericmodelgxtsupport.client.field.htmleditor;

import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.ExtendedStringDialog;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

public class HtmlEditorDialog extends ExtendedStringDialog {
	
	private boolean isFreeInstantiation;
	private String caption = LocalizedText.INSTANCE.htmlEditor();
	private boolean readOnly = false;
	private AbstractGridEditing<?> gridEditing;
	private ContentPanel editorContainer;
	private HtmlEditor htmlEditor;
	private boolean useStrongTag;
	private boolean usePTag;
	private boolean useEmTag;
		
	public HtmlEditorDialog() {
		editorContainer = new ContentPanel();
		editorContainer.setHeaderVisible(false);
		editorContainer.setBorders(false);
		editorContainer.setBodyBorder(false);
		setHeading(caption);
		setSize("800px", "400px");
		setResizable(true);
		setClosable(false);
		setOnEsc(false);
		setModal(true);
		
		htmlEditor = new HtmlEditor() {
			@Override
			public void setValue(String value) {
				if (!useEmTag && !usePTag && !useStrongTag) {
					super.setValue(value);
					return;
				}
				
				if (useStrongTag) {
					value = value.replaceAll("<strong>", "<b>");
					value = value.replaceAll("</strong>", "</b>");
				}
				
				if (usePTag) {
					value = value.replaceAll("<p>", "<div>");
					value = value.replaceAll("</p>", "</div>");	
				}
				
				if (useEmTag) {
					value = value.replaceAll("<em>", "<i>");
					value = value.replaceAll("</em>", "</i>");	
				}	
				
				super.setValue(value);
			}
			
			@Override
			public String getValue() {
				String value = super.getValue();
				if (!useEmTag && !usePTag && !useStrongTag)
					return value;
				
				if (useStrongTag) {
					value = value.replaceAll("<b>", "<strong>");
					value = value.replaceAll("</b>", "</strong>");
				}
				
				if (usePTag) {
					value = value.replaceAll("<div>", "<p>");
					value = value.replaceAll("</div>", "</p>");	
				}
				
				if (useEmTag) {
					value = value.replaceAll("<i>", "<em>");
					value = value.replaceAll("</i>", "</em>");	
				}
				
				return value;
			}
			
			@Override
			protected void initToolBar() {
				super.initToolBar();
				
				IFrameElement iFrame = IFrameElement.as(getTextArea().getElement());
				addTextPasteHandler(iFrame);
			}
		};
		htmlEditor.setEnableColors(true);
		htmlEditor.setEnableFont(false);
		htmlEditor.setEnableAlignments(false);
		htmlEditor.setEnableFontSize(false);
		htmlEditor.setEnableSourceEditMode(true);
		
		editorContainer.add(htmlEditor);
		setWidget(editorContainer);
		
		final TextButton cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		final TextButton okButton = new TextButton(LocalizedText.INSTANCE.ok());
		
		SelectHandler selectHandler = event -> {
			if (event.getSource() == cancelButton)
				cancelChanges();
			
			if (event.getSource() == okButton)
				applyChanges();
			
			HtmlEditorDialog.this.hide();
		};
					
		okButton.addSelectHandler(selectHandler);
		cancelButton.addSelectHandler(selectHandler);
		addButton(okButton);
		addButton(cancelButton);
	}
	
	/**
	 * When configured to true, then the strong tag is used instead of b (default) for bold text.
	 */
	@Configurable
	public void setUseStrongTag(boolean useStrongTag) {
		this.useStrongTag = useStrongTag;
	}
	
	/**
	 * When configured to true, then the p tag is used instead of div (default) for new paragraphs.
	 */
	@Configurable
	public void setUsePTag(boolean usePTag) {
		this.usePTag = usePTag;
	}
	
	/**
	 * When configured to true, then the em tag is used instead of i (default) for italic text. 
	 */
	@Configurable
	public void setUseEmTag(boolean useEmTag) {
		this.useEmTag = useEmTag;
	}

	/**
	 * Applies the changes
	 */
	@Override
	public void applyChanges() {
		applyChanges = true;		
	}
	
	/**
	 * Cancels the changes
	 */
	@Override
	public void cancelChanges() {
		cancelChanges = true;
	}
	
	public String getValue() {
		return htmlEditor.getValue();
	}
	
	@Override
	public void setString(String string) {
		setValue(string);
	}
	
	public void setValue(String value) {
		htmlEditor.setValue(value);
	}
	
	@Override
	public String getString() {
		return getValue();
	}

	@Override
	public boolean hasChanges() {
		return applyChanges;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//NOP
	}
	
	@Override
	public void show() {
		//RVE - for New Instance show Editor only as a GmView, not need show both together with Dialog
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
	@Override
	public void setCaption(String caption) {
		this.caption = caption;
		setHeading(caption);
	}
	
	@Override
	public String getCaption() {
		return this.caption;	
	}
	
	@Override
	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	@Override
	public Boolean isReadOnly() {
		return this.readOnly;
	}	
	
	/**
	 * Configures the parent {@link AbstractGridEditing}.
	 */
	@Override
	public void configureGridEditing(AbstractGridEditing<?> gridEditing) {
		this.gridEditing = gridEditing;
	}
	
	@Override
	public AbstractGridEditing<?> getParentGridEditing() {
		return gridEditing;
	}
	
	@Override
	public Widget getView() {
		return editorContainer;
	}
	
	public void updateContainer() {
		setWidget(editorContainer);
	}

	@Override
	public void hide() {
		cancelChanges = true;
		super.hide();
	}

	public boolean canFireDialog() {
		return true;
	}
	
	private native void addTextPasteHandler(IFrameElement iframe) /*-{
		if (iframe == null)
			return;
	
		var _this = this;
	
		var pasteFunction = function(e) {
			var text = '';
			if (e.clipboardData || e.originalEvent.clipboardData)
				text = (e.originalEvent || e).clipboardData.getData('text/plain');
			else if (iframe.contentWindow.clipboardData)
				text = iframe.contentWindow.clipboardData.getData('Text');
		    
			if (iframe.contentDocument.queryCommandSupported('insertText')) {
				e.preventDefault();
				iframe.contentDocument.execCommand('insertText', false, text);
			}
	    }
	
	    iframe.contentWindow.addEventListener('paste', pasteFunction, true);
	}-*/;
}
