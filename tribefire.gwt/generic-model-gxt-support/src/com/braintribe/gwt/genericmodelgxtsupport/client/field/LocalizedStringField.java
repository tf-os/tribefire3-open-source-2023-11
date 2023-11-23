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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gmview.action.client.TrackableChangesAction;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.cell.core.client.form.TriggerFieldCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.form.Validator;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;
import com.sencha.gxt.widget.core.client.form.validator.EmptyValidator;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

/**
 * This editor consists of a normal TextField for strings.
 * It has a button that shows a dialog that shows a grid with 2 columns:
 * key and value. Key is the locale name, and value is the string itself, in that locale.
 * @author michel.docouto
 *
 */
public class LocalizedStringField extends TriggerField<LocalizedString> implements NoBlurWhileEditingField, TrackableChangesAction, TriggerFieldAction {
	private static final String DEFAULT_LOCALE = "default";
	private static final EntityType<LocalizedString> localizedStringEntityType = LocalizedString.T;
	
	private LocalizedValuesDialog localizedValuesDialog;
	private Supplier<? extends LocalizedValuesDialog> localizedValuesDialogSupplier;
	private Validator<LocalizedString> emptyValidator = null;
	private boolean editingField = false;
	private LocalizedString localizedString;
	private boolean hasChanges = false;
	private ValueBaseField<String> internalField = null;
	private PersistenceGmSession gmSession;
	private Action triggerAction;
	private AbstractGridEditing<?> gridEditing;
	private GridCell gridCell;
	private boolean useRawValue = true;
	private boolean gettingOldValue = false;
	private LocalizedString oldLocalizedString;
	private boolean editableHandlingPending;
	private boolean usedAsElement = false;
	private HideHandler hideHandler;
	private HandlerRegistration hideHandlerRegistration;
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the required {@link LocalizedValuesDialog}.
	 */
	@Required
	public void setLocalizedValuesDialog(Supplier<? extends LocalizedValuesDialog> localizedValuesDialogSupplier) {
		this.localizedValuesDialogSupplier = localizedValuesDialogSupplier;
	}
	
	/**
	 * Configures the internal field used within the LocalizedValuesDialog.
	 */
	@Configurable
	public void setInternalField(ValueBaseField<String> internalField) {
		this.internalField = internalField;
	}
	
	/**
	 * Configures whether this field is used as a DOM element via getElement, or if it is as an widget.
	 * Defaults to false.
	 */
	@Configurable
	public void setUsedAsElement(boolean usedAsElement) {
		this.usedAsElement = usedAsElement;
	}
	
	public LocalizedStringField() {
		super(new TriggerFieldCell<>());
		setPropertyEditor(new PropertyEditor<LocalizedString>() {
			@Override
			public LocalizedString parse(CharSequence text) throws ParseException {
				if (gettingOldValue) {
					gettingOldValue = false;
					return oldLocalizedString;
				}
				
				String value = text.toString();
				if (localizedString == null && value.isEmpty())
					return localizedString;
				
				if (localizedString == null || I18nTools.getDefault(localizedString, null) == null
						|| I18nTools.getDefault(localizedString, null).isEmpty() || !I18nTools.getDefault(localizedString, null).equals(value)) {
					if (usedAsElement)
						changeDefaultValue(value);
					
					return localizedString;
				}
				
				if (localizedString != null && value.isEmpty())
					localizedString = null;
				return localizedString;
			}
			
			@Override
			public String render(LocalizedString object) {
				return localizedString == null || I18nTools.getDefault(localizedString, null) == null ? "" : I18nTools.getDefault(localizedString, null).replace("\n", " ");
			}
		});
		
		this.addAttachHandler(event -> {
			if (editableHandlingPending && event.isAttached()) {
				editableHandlingPending = false;
				setEditable(isEditable(localizedString));
			}
		});
	}
	
	@Override
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
	
	@Override
	public Action getTriggerFieldAction() {
		if (triggerAction != null)
			return triggerAction;
		
		triggerAction = new Action() {
			@SuppressWarnings("rawtypes")
			@Override
			public void perform(TriggerInfo triggerInfo) {
				LocalizedString value = null;
				Grid<Object> grid = null;
				if (gridEditing != null)
					grid = (Grid) gridEditing.getEditableGrid();
				if (grid != null)
					value = (LocalizedString) grid.getColumnModel().getColumn(gridCell.getCol()).getValueProvider().getValue(grid.getStore().get(gridCell.getRow()));
				
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
	
	private Validator<LocalizedString> createEmptyValidator() {
		if (emptyValidator != null)
			return emptyValidator;
		
		this.emptyValidator = new EmptyValidator<LocalizedString>() {
			@Override
			public List<EditorError> validate(Editor<LocalizedString> editor, LocalizedString value) {
				if (isLocalizedStringEmpty(value)) {
					List<EditorError> errors = new ArrayList<>();
					errors.add(new DefaultEditorError(editor, getMessages().blankText(), ""));
					return errors;
				}
				
				return null;
			}

			private boolean isLocalizedStringEmpty(LocalizedString value) {
				Map<String, String> localizedStrings = (value != null ? value.getLocalizedValues() : null);

				if (localizedStrings != null) {
					for (Map.Entry<String, String> entry : localizedStrings.entrySet()) {
						if (!isStringEmpty(entry.getValue()))
							return false;
					}
				}

				return true;
			}

			private boolean isStringEmpty(String value) {
				if (value == null || value.isEmpty())
					return true;

				return false;
			}
		};

		return this.emptyValidator;
	}
	
	@Override
	public void setGridInfo(AbstractGridEditing<?> gridEditing, GridCell gridCell) {
		this.gridEditing = gridEditing;
		this.gridCell = gridCell;
	}
	
	private LocalizedValuesDialog getLocalizedValuesDialog() {
		if (localizedValuesDialog != null) {
			if (hideHandlerRegistration == null)
				hideHandlerRegistration = localizedValuesDialog.addHideHandler(getHideHandler());
			
			return localizedValuesDialog;
		}
		
		localizedValuesDialog = localizedValuesDialogSupplier.get();
		localizedValuesDialog.configureGridEditing(gridEditing);
		hideHandlerRegistration = localizedValuesDialog.addHideHandler(getHideHandler());
		
		return localizedValuesDialog;
	}
	
	private HideHandler getHideHandler() {
		if (hideHandler != null)
			return hideHandler;
		
		hideHandler = (HideHandler) event -> {
			boolean hasChanges = localizedValuesDialog.hasChanges();
			if (hasChanges && gridEditing != null)
				gridEditing.startEditing(gridCell);
			new Timer() {
				@Override
				public void run() {
					new Timer() {
						@Override
						public void run() {
							if (hasChanges) {
								populateField(localizedValuesDialog.getLocalizedValues());
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
	
	protected void handleTriggerClick() {
		if (isReadOnly())
			return;
		
		Map<String, String> valuesToEditInDialog = new FastMap<String>();
		String defaultValue = null;
		if (localizedString != null) {
			for (Map.Entry<String, String> entry : localizedString.getLocalizedValues().entrySet()) {
				if (!entry.getKey().equals(DEFAULT_LOCALE))
					valuesToEditInDialog.put(entry.getKey(), entry.getValue());
				else
					defaultValue = entry.getValue();
			}
		}
		if (isEditable() && useRawValue)
			defaultValue = getText();
		
		localizedValuesDialog = getLocalizedValuesDialog();
		localizedValuesDialog.setEditorField(internalField);
		localizedValuesDialog.setLocalizedValues(defaultValue, valuesToEditInDialog);
		editingField = true;
		localizedValuesDialog.show();
		if (isRendered() || usedAsElement)
			getInputEl().focus();
	}
	
	/**
	 * Populates the field with values.
	 */
	private void populateField(Map<String, String> localizedValues) {
		hasChanges = true;
		
		if (localizedString == null)
			localizedString = gmSession.create(LocalizedString.T);
		else if (localizedValuesDialog.itemsRemoved)
			localizedString.getLocalizedValues().clear();
		
		localizedString.getLocalizedValues().putAll(localizedValues);
		setText(localizedValues.get(DEFAULT_LOCALE));
		if (isAttached())
			setEditable(isEditable(localizedString));
		else
			editableHandlingPending = true;
	}
	
	@Override
	public LocalizedString getValue() {
		if ((!isRendered() || !isEditable()) && !usedAsElement)
			return super.getValue();
		
		String v = getText();
		if (getEmptyText() != null && v.equals(getEmptyText()))
			return null;
		
		try {
			return getPropertyEditor().parse(v);
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		setValue(null);
	}
	
	@Override
	public void setValue(LocalizedString localizedString) {
		if (localizedString == null)
			oldLocalizedString = null;
		else
			oldLocalizedString = (LocalizedString) localizedStringEntityType.clone(localizedString, null, null);
		
		hasChanges = false;
		if (isAttached())
			setEditable(isEditable(localizedString));
		else
			editableHandlingPending = true;
		this.localizedString = localizedString;
		super.setValue(localizedString);
	}
	
	@Override
	public void setValue(LocalizedString localizedString, boolean fireEvents, boolean redraw) {
		gettingOldValue = true;
		
		if (isRendered() || usedAsElement) {
			String text = getText();
			if (!text.isEmpty() && (localizedString == null || I18nTools.getDefault(localizedString, null) == null || I18nTools.getDefault(localizedString, null).isEmpty()
					|| !I18nTools.getDefault(localizedString, null).equals(text))) {
				changeDefaultValue(text);
				localizedString = this.localizedString;
			}
		}
		
		super.setValue(localizedString, fireEvents, redraw);
	}
	
	private boolean isEditable(LocalizedString localizedString) {
		if (localizedString != null && I18nTools.getDefault(localizedString, null) != null && I18nTools.getDefault(localizedString, null).contains("\n"))
			return false;
		
		return true;
	}
	
	/**
	 * Gets the Localized values as Map.
	 */
	public Map<String, String> getLocalizedValues() {
		return localizedString.getLocalizedValues();
	}
	
	@Override
	public boolean isEditingField() {
		return editingField;
	}

	protected void changeDefaultValue(String value) {
		if (localizedString == null) {
			localizedString = gmSession.create(LocalizedString.T);
			hasChanges = true;
		}
		
		String defaultValue = I18nTools.getDefault(localizedString, null); 
		if (defaultValue == null || !defaultValue.equals(value)) {
			hasChanges = true;
			localizedString.getLocalizedValues().put(DEFAULT_LOCALE, value);
		}
	}
	
	protected void changeValues(Map<String, String> localizedValues) {
		NestedTransaction nestedTransaction = gmSession.getTransaction().beginNestedTransaction();
		hasChanges = true;
		if (localizedString == null)
			localizedString = gmSession.create(LocalizedString.T);
		localizedString.getLocalizedValues().putAll(localizedValues);
		setText(localizedValues.get(DEFAULT_LOCALE));
		nestedTransaction.commit();
	}
	
	@Override
	public boolean hasChanges() {
		return hasChanges;
	}
	
	@Override
	public XElement getInputEl() {
		return super.getInputEl();
	}

}
