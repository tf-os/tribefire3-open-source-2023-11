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

import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gmview.action.client.TrackableChangesAction;
import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ExtendedStringCell;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

/**
 * Editor which displays an {@link HtmlEditor} when displaying the string in a new window.
 *
 */
public class HtmlEditorField extends TriggerField<String> implements NoBlurWhileEditingField, TrackableChangesAction, TriggerFieldAction {
	
	private Action triggerAction;
	private AbstractGridEditing<?> gridEditing;
	private GridCell gridCell;
	private boolean usedAsElement = false;
	private boolean useRawValue = true;
	private boolean hasChanges = false;
	private String defaultValue;
	private HtmlEditorDialog htmlEditorDialog;
	private Supplier<? extends HtmlEditorDialog> htmlEditorDialogSupplier;
	private HideHandler hideHandler;
	private HandlerRegistration hideHandlerRegistration;
	private boolean editingField = false;
	private boolean useStrongTag;
	private boolean usePTag;
	private boolean useEmTag;
	
	/**
	 * Configures the required {@link HtmlEditorDialog}.
	 */
	@Required
	public void setHtmlEditorDialog(Supplier<? extends HtmlEditorDialog> htmlEditorDialogSupplier) {
		this.htmlEditorDialogSupplier = htmlEditorDialogSupplier;
	}
	
	/**
	 * Configures whether this field is used as a DOM element via getElement, or if it is as an widget.
	 * Defaults to false.
	 */
	@Configurable
	public void setUsedAsElement(boolean usedAsElement) {
		this.usedAsElement = usedAsElement;
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
	
	public HtmlEditorField() {
		super(new ExtendedStringCell());
		
		/*this.addAttachHandler(event -> {
			if (editableHandlingPending && event.isAttached()) {
				editableHandlingPending = false;
				setEditable(isEditable(localizedString));
			}
		});*/
	}
	
	/*
	 * @Override
	public void setAllowBlank(boolean allowBlank) {
		getCell().setAllowBlank(allowBlank);
		
		if (allowBlank == false) {
			if (emptyValidator == null)
				emptyValidator = createEmptyValidator();
			
			if (!getValidators().contains(emptyValidator))
				getValidators().add(0, emptyValidator);
		} else if (emptyValidator != null)
			getValidators().remove(this.emptyValidator);
	}
	 */
	
	@Override
	public void setGridInfo(AbstractGridEditing<?> gridEditing, GridCell gridCell) {
		this.gridEditing = gridEditing;
		this.gridCell = gridCell;
	}
	
	@Override
	public Action getTriggerFieldAction() {
		if (triggerAction != null)
			return triggerAction;
		
		triggerAction = new Action() {
			@SuppressWarnings("rawtypes")
			@Override
			public void perform(TriggerInfo triggerInfo) {
				String value = null;
				Grid<Object> grid = null;
				if (gridEditing != null)
					grid = (Grid) gridEditing.getEditableGrid();
				if (grid != null)
					value = (String) grid.getColumnModel().getColumn(gridCell.getCol()).getValueProvider().getValue(grid.getStore().get(gridCell.getRow()));
				
				if (value != null)
					setValue(value);
				useRawValue = false;
				handleTriggerClick();
				useRawValue = true;
			}
		};
		
		addTriggerClickHandler(event -> handleTriggerClick());
		
		triggerAction.setIcon(GMGxtSupportResources.INSTANCE.addLocale());
		triggerAction.setName(LocalizedText.INSTANCE.addLocalization());
		triggerAction.setTooltip(LocalizedText.INSTANCE.addLocaleDescription());
		
		return triggerAction;
	}
	
	@Override
	public boolean isEditingField() {
		return editingField;
	}
	
	protected void handleTriggerClick() {
		if (isReadOnly())
			return;
		
		if (isEditable() && useRawValue)
			defaultValue = getText();
		
		htmlEditorDialog = getHtmlEditorDialog();
		htmlEditorDialog.setValue(defaultValue);
		editingField = true;
		htmlEditorDialog.show();
		if (isRendered() || usedAsElement)
			getInputEl().focus();
	}
	
	@Override
	public boolean hasChanges() {
		return hasChanges;
	}
	
	@Override
	public XElement getInputEl() {
		return super.getInputEl();
	}
	
	private HtmlEditorDialog getHtmlEditorDialog() {
		if (htmlEditorDialog != null) {
			if (hideHandlerRegistration == null)
				hideHandlerRegistration = htmlEditorDialog.addHideHandler(getHideHandler());
			
			return htmlEditorDialog;
		}
		
		htmlEditorDialog = htmlEditorDialogSupplier.get();
		htmlEditorDialog.configureGridEditing(gridEditing);
		hideHandlerRegistration = htmlEditorDialog.addHideHandler(getHideHandler());
		htmlEditorDialog.setUseStrongTag(useStrongTag);
		htmlEditorDialog.setUsePTag(usePTag);
		htmlEditorDialog.setUseEmTag(useEmTag);
		
		return htmlEditorDialog;
	}
	
	private HideHandler getHideHandler() {
		if (hideHandler != null)
			return hideHandler;
		
		hideHandler = (HideHandler) event -> {
			boolean hasChanges = htmlEditorDialog.hasChanges();
			if (hasChanges && gridEditing != null)
				gridEditing.startEditing(gridCell);
			new Timer() {
				@Override
				public void run() {
					new Timer() {
						@Override
						public void run() {
							if (hasChanges) {
								populateField(htmlEditorDialog.getValue());
								if (gridEditing != null)
									gridEditing.completeEditing();
							}
							editingField = false;
							if (!hasChanges)
								blur();
							
							hideHandlerRegistration.removeHandler();
							hideHandlerRegistration = null;
						}
						
					}.schedule(50);
				}
			}.schedule(250);
		};
		
		return hideHandler;
	}
	
	/**
	 * Populates the field with values.
	 */
	private void populateField(String newValue) {
		hasChanges = true;
		
		
		setText(newValue);
		/*if (isAttached())
			setEditable(isEditable(localizedString));
		else
			editableHandlingPending = true;*/
	}

}
