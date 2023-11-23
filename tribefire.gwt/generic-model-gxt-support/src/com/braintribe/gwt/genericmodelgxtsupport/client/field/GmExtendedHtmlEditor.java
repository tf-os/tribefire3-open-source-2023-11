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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Configurable;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;
import com.sencha.gxt.widget.core.client.toolbar.SeparatorToolItem;

/**
 * {@link HtmlEditor} Extension prepared for GM's {@link TriggerFieldAction}.
 */
public class GmExtendedHtmlEditor extends HtmlEditor implements TriggerFieldAction, NoBlurWhileEditingField {

	private Action triggerAction;
	private ExtendedStringDialog extendedStringDialog;
	private boolean editingField = false;
	private AbstractGridEditing<?> gridEditing;
	private GridCell gridCell;
	private Supplier<? extends ExtendedStringDialog> extendedStringDialogProvider;
	private boolean useSeparators = false;
	private boolean useStrongTag;
	private boolean usePTag;
	private boolean useEmTag;

	public GmExtendedHtmlEditor(Supplier<? extends ExtendedStringDialog> dialogProvider) {
		this.extendedStringDialogProvider = dialogProvider;
		setEnableColors(false);
		setEnableFont(false);
		setEnableAlignments(false);
		setEnableFontSize(false);
		setEnableSourceEditMode(false);
		setUseSeparators(false);
	}
	
	protected void handleTriggerClick() {
		ExtendedStringDialog extendedStringFieldDialog = getExtendedStringDialog();
		extendedStringFieldDialog.configureGridEditing(gridEditing);
		String dialogName = this.getData("dialogName");
		if (dialogName != null)
			extendedStringFieldDialog.setCaption(dialogName);
		String codeFormat = this.getData("codeFormatting");
		if (codeFormat != null)
			extendedStringFieldDialog.setCodeFormat(codeFormat);
		extendedStringFieldDialog.setString(getValue());
		//extendedStringFieldDialog.setReadOnly(isReadOnly());
		extendedStringFieldDialog.show();		
		//if (!isReadOnly())
			//Scheduler.get().scheduleDeferred(() -> getInputEl().focus());
	}
	
	/*@Override
	public void focus() {
		if (!isReadOnly())
			super.focus();
	 }	*/
	
	/**
	 * Configures whether separators should be used in the toolbar.
	 * Defaults to false.
	 */
	@Configurable
	public void setUseSeparators(boolean useSeparators) {
		this.useSeparators = useSeparators;
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
	
	private ExtendedStringDialog getExtendedStringDialog() {
		if (extendedStringDialog != null)
			return extendedStringDialog;
		
		extendedStringDialog = extendedStringDialogProvider.get();
		
		extendedStringDialog.addHideHandler((HideHandler) event -> {
			//if (isReadOnly())
				//return;
			
			String newValue = extendedStringDialog.getString();				
			Scheduler.get().scheduleDeferred(() -> {
				if (extendedStringDialog.isApplyChanges())
					GmExtendedHtmlEditor.this.setValue(newValue);
			});
		});
		return extendedStringDialog;
	}

	@Override
	public Action getTriggerFieldAction() {
		if (triggerAction != null)
			return triggerAction;
		
		triggerAction = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				gridEditing.startEditing(gridCell);				
				editingField = true;
				Scheduler.get().scheduleDeferred(() -> {
					handleTriggerClick();
					/*if (isReadOnly()) {
						gridEditing.cancelEditing();
						editingField = true;
					}	*/					
				});
			}
		};
		
		triggerAction.setIcon(GMGxtSupportResources.INSTANCE.multiLine());
		triggerAction.setName(LocalizedText.INSTANCE.multiline());
		triggerAction.setTooltip(LocalizedText.INSTANCE.multilineDescription());
		
		return triggerAction;
	}

	@Override
	public void setGridInfo(AbstractGridEditing<?> gridEditing, GridCell gridCell) {
		this.gridEditing = gridEditing;
		this.gridCell = gridCell;
	}	
	
	@Override
	public String getValue() {
		String value = super.getValue();
		if (value == null)
			return value;
		
		if (value.toLowerCase().contains("<a href=") && !value.toLowerCase().contains("target=\"_blank\"")) 
			value = value.replaceAll("<a href=", "<a target=\"_blank\" href=");		
		
		if ((!useEmTag && !usePTag && !useStrongTag))
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
	public void setValue(String value) {
		if (value == null) {
			super.setValue(value);
			return;			
		}
		
		if (value.toLowerCase().contains("<a href=") && !value.toLowerCase().contains("target=\"_blank\"")) {
			value = value.replaceAll("<a href=", "<a target=\"_blank\" href=");
		}
		
		if ((!useEmTag && !usePTag && !useStrongTag)) {
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
	public boolean isEditingField() {
		return editingField;				
	}

	public void setDialog(Supplier<ExtendedStringDialog> extendedStringDialogProvider) {
		this.extendedStringDialogProvider = extendedStringDialogProvider; 		
	}
	
	@Override
	protected void initToolBar() {
		super.initToolBar();
		
		IFrameElement iFrame = IFrameElement.as(getTextArea().getElement());
		addTextPasteHandler(iFrame);
		
		if (useSeparators)
			return;
		
		Set<SeparatorToolItem> separatorsToRemove = new HashSet<>();
		for (int i = 0; i < toolBar.getWidgetCount(); i++) {
			Widget widget = toolBar.getWidget(i);
			if (widget instanceof SeparatorToolItem)
				separatorsToRemove.add((SeparatorToolItem) widget);
		}
		
		separatorsToRemove.forEach(separator -> toolBar.remove(separator));
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
