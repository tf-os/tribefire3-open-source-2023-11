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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gmview.client.EntityFieldDialog;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedColumnHeader;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.Layer;
import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.form.error.DefaultEditorError;
import com.sencha.gxt.widget.core.client.form.validator.AbstractValidator;
import com.sencha.gxt.widget.core.client.form.validator.RegExValidator;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridView;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * Dialog to be displayed when using the {@link LocalizedStringField}
 * @author michel.docouto
 *
 */
public class LocalizedValuesDialog extends ClosableWindow implements EntityFieldDialog<LocalizedString> {
	private static final String DEFAULT_LOCALE = "default";
	private static Long lastId = 0l;
	
	interface LocaleDataProperties extends PropertyAccess<LocaleData> {
		ModelKeyProvider<LocaleData> id();
		ValueProvider<LocaleData, String> locale();
		ValueProvider<LocaleData, String> value();
	}
	private static LocaleDataProperties props = GWT.create(LocaleDataProperties.class);
	
	private Grid<LocaleData> localizedValuesGrid;
	private ListStore<LocaleData> gridStore;
	protected RefreshableView gridView;
	private TextButton addButton;
	private TextButton removeButton;
	private ValueBaseField<String> editorField;
	protected boolean itemsRemoved;
	private TextButton applyButton;
	protected boolean cancelChanges;
	private TextButton clearButton;
	private LocalizedString localizedString;
	private ToolBar toolBar;
	private BorderLayoutContainer borderLayoutContainer;
	private GridInlineEditing<LocaleData> gridInlineEditing;
	private boolean dialogInitialized;
	private AbstractGridEditing<?> gridEditing;

	enum Locale {
		DEFAULT(DEFAULT_LOCALE), ENGLISH("en"), GERMAN("de");
		
		private final String value;
		
		private Locale(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	public LocalizedValuesDialog() {
		setSize("400px", "410px");
		setHeading(LocalizedText.INSTANCE.localizedValues());
		setModal(true);
		setClosable(false);
		setMinWidth(400);
		setMinHeight(350);
		setHeaderVisible(true);
		setBodyBorder(false);
		setBorders(false);
		//setFrame(false);
		setResizable(true);
		addStyleName("localizedValuesDialog");
	}
	
	@Override
	public Widget getView() {
		if (borderLayoutContainer == null) {
			borderLayoutContainer = new BorderLayoutContainer();
			borderLayoutContainer.setBorders(false);
			borderLayoutContainer.setCenterWidget(prepareGrid());
		}
		
		return borderLayoutContainer;
	}
	
	@Override
	public void show() {
		getView();
		
		if (!dialogInitialized) {
			dialogInitialized = true;
			borderLayoutContainer.setSouthWidget(prepareToolBar(), new BorderLayoutData(61));
			add(borderLayoutContainer);
		}
		
		super.show();
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
	
	protected void removeToolBar() {
		if (toolBar != null)
			toolBar.removeFromParent();
	}
	
	protected void addToolBar() {
		if (!dialogInitialized)
			return;
		
		if (toolBar.getParent() != borderLayoutContainer)
			borderLayoutContainer.setSouthWidget(toolBar, new BorderLayoutData(61));
		
		if (borderLayoutContainer.getParent() != this)
			add(borderLayoutContainer);
		
		if (addButton.getParent() != toolBar)
			toolBar.insert(addButton, 1);
		if (removeButton.getParent() != toolBar)
			toolBar.insert(removeButton, 2);
		if (clearButton.getParent() != toolBar)
			toolBar.insert(clearButton, 3);
	}
	
	@Override
	protected Layer createGhost() {
		Layer ghost = super.createGhost();
		ghost.getElement().addClassName("localizedValuesDialog");
		return ghost;
	}
	
	private ToolBar prepareToolBar() {
		toolBar = new ToolBar();
		toolBar.add(new FillToolItem());
		toolBar.add(getAddButton());
		toolBar.add(getRemoveButton());
		toolBar.add(getClearButton());
		toolBar.add(prepareCancelButton());
		toolBar.add(getApplyButton());
		return toolBar;
	}
	
	private ListStore<LocaleData> getGridStore() {
		if (gridStore != null)
			return gridStore;
		
		gridStore = new ListStore<>(props.id());
		return gridStore;
	}

	public TextButton getAddButton() {
		if (addButton != null)
			return addButton;
		
		addButton = new TextButton(LocalizedText.INSTANCE.add(), GMGxtSupportResources.INSTANCE.add());
		addButton.setToolTip(LocalizedText.INSTANCE.addLocalizationDescription());
		addButton.setIconAlign(IconAlign.TOP);
		addButton.setScale(ButtonScale.LARGE);
		addButton.addSelectHandler(event -> {
			LocaleData model;
			List<StoreSortInfo<LocaleData>> sortInfo = localizedValuesGrid.getStore().getSortInfo();
			if (!sortInfo.isEmpty() && SortDir.ASC.equals(sortInfo.get(0).getDirection()))
				model = createModel(String.valueOf(Character.MAX_VALUE), String.valueOf(Character.MAX_VALUE));
			else
				model = createModel(null, null);
			
			localizedValuesGrid.getStore().add(model);
			gridInlineEditing.cancelEditing();
			model.setLocale(null);
			model.setValue(null);
			gridView.refreshModel(model);
			localizedValuesGrid.getSelectionModel().select(model, false);
			gridInlineEditing.startEditing(new GridCell(localizedValuesGrid.getStore().indexOf(model), 0));
			getClearButton().setEnabled(true);
		});
		return addButton;
	}
	
	public TextButton getRemoveButton() {
		if (removeButton != null)
			return removeButton;
		
		removeButton = new TextButton(LocalizedText.INSTANCE.remove(), GMGxtSupportResources.INSTANCE.remove());
		removeButton.setToolTip(LocalizedText.INSTANCE.removeLocalizationDescription());
		removeButton.setEnabled(false);
		removeButton.setIconAlign(IconAlign.TOP);
		removeButton.setScale(ButtonScale.LARGE);
		removeButton.addSelectHandler(event -> {
			List<LocaleData> selectedModels = localizedValuesGrid.getSelectionModel().getSelectedItems();
			if (selectedModels != null && !selectedModels.isEmpty()) {
				gridInlineEditing.cancelEditing();
				for (LocaleData model : selectedModels)
					localizedValuesGrid.getStore().remove(model);
				itemsRemoved = true;
			}
			getClearButton().setEnabled(localizedValuesGrid.getStore().size() > 1);
			validateChanges();
		});
		return removeButton;
	}
	
	public TextButton getClearButton() {
		if (clearButton != null)
			return clearButton;
		
		clearButton = new TextButton(LocalizedText.INSTANCE.clear(), GMGxtSupportResources.INSTANCE.remove());
		clearButton.setToolTip(LocalizedText.INSTANCE.clearLocalizationsDescription());
		clearButton.setEnabled(false);
		clearButton.setIconAlign(IconAlign.TOP);
		clearButton.setScale(ButtonScale.LARGE);
		clearButton.addSelectHandler(event -> {
			LocaleData defaultModel = null;
			gridInlineEditing.cancelEditing();
			for (LocaleData model : localizedValuesGrid.getStore().getAll()) {
				String locale = model.getLocale();
				if (locale != null && locale.equals(DEFAULT_LOCALE))
					defaultModel = model;
			}
			localizedValuesGrid.getStore().clear();
			if (defaultModel != null)
				localizedValuesGrid.getStore().add(defaultModel);
			itemsRemoved = true;
			clearButton.setEnabled(false);
			validateChanges();
		});
		return clearButton;
	}
	
	private Grid<LocaleData> prepareGrid() {
		ColumnConfig<LocaleData, String> localeColumn = new ColumnConfig<>(props.locale(), 80, LocalizedText.INSTANCE.locale());
		SimpleComboBox<String> localeField = new SimpleComboBox<>(new StringLabelProvider<>());
		configureLocaleField(localeField);
		/*localeEditor.setCancelOnEsc(true);
		localeEditor.setCompleteOnEnter(true);*/
		
		ColumnConfig<LocaleData, String> valueColumn = new ColumnConfig<>(props.value(), 300, LocalizedText.INSTANCE.value());
		ValueBaseField<String> field = editorField;
		if (field == null)
			field = getMandatoryTextField();
		else if (field instanceof TextField) {
			((TextField) field).setAllowBlank(false);
			((TextField) field).setAutoValidate(true);
		}
		/*CellEditor valueEditor = new MultiEditorCellEditor(field) {
			@Override
			protected void onSpecialKey(FieldEvent fe) {
				if (fe.getKeyCode() == KeyCodes.KEY_TAB && fe.hasModifier() == false)
					if (gridStore.getAt(gridStore.getCount() - 1 ) == localizedValuesGrid.getSelectionModel().getSelectedItem()) {
						localizedValuesGrid.stopEditing(false);
						if (localizedValuesGrid.isEditing() == false)
							addButton.fireEvent(Events.Select);
						return;
					}
				super.onSpecialKey(fe);
			}
			
		};
		valueEditor.setCancelOnEsc(true);
		valueEditor.setCompleteOnEnter(true);*/
		List<ColumnConfig<LocaleData, ?>> columns = new ArrayList<>();
		columns.add(localeColumn);
		columns.add(valueColumn);
		ColumnModel<LocaleData> cm = new ColumnModel<>(columns);
		
		localizedValuesGrid = new Grid<>(getGridStore(), cm);
		
		gridInlineEditing = new GridInlineEditing<>(localizedValuesGrid);
		gridInlineEditing.addEditor(localeColumn, localeField);
		gridInlineEditing.addEditor(valueColumn, field);
		gridInlineEditing.addCancelEditHandler(event -> {
			if (event.getEditCell().getCol() == 0)
				validateChanges();
		});
		gridInlineEditing.addBeforeStartEditHandler(event -> {
			int colIndex = event.getEditCell().getCol();
			int rowIndex = event.getEditCell().getRow();
			if (colIndex == 0) {
				localeField.getStore().clear();
				for (Locale locale : Locale.values())
					localeField.add(locale.toString());
				
				getGridStore().commitChanges();
				
				int i = 0;
				for (LocaleData model : getGridStore().getAll()) {
					if (i++ != rowIndex)
						localeField.remove(model.getLocale());
				}
			}
			
			LocaleData model = localizedValuesGrid.getStore().get(rowIndex);
			if ((DEFAULT_LOCALE.equals(model.getLocale()) && colIndex == 0) || gridInlineEditing.isEditing()
					&& gridInlineEditing.getEditor(localizedValuesGrid.getColumnModel().getColumn(colIndex)).isValid(true)) {
				event.setCancelled(true);
				return;
			}
			getApplyButton().setEnabled(false);
			getAddButton().setEnabled(false);
		});
		
		localizedValuesGrid.getSelectionModel().addSelectionChangedHandler(event -> getRemoveButton().setEnabled(!event.getSelection().isEmpty() && !DEFAULT_LOCALE.equals(event.getSelection().get(0).getLocale())));
		
		gridInlineEditing.addCompleteEditHandler(event -> Scheduler.get().scheduleDeferred(this::validateChanges));
		
		gridView = new RefreshableView();
		gridView.setColumnHeader(new ExtendedColumnHeader<>(localizedValuesGrid, localizedValuesGrid.getColumnModel()));
		gridView.setShowDirtyCells(false);
		
		gridView.setViewConfig(new GridViewConfig<LocaleData>() {
			@Override
			public String getRowStyle(LocaleData model, int rowIndex) {
				return "";
			}
			
			@Override
			public String getColStyle(LocaleData model, ValueProvider<? super LocaleData, ?> valueProvider, int rowIndex, int colIndex) {
				return "gmeGridColumn";
			}
		});
		
		localizedValuesGrid.setView(gridView);
		gridView.setAutoExpandMax(10000);
		gridView.setAutoExpandColumn(valueColumn);

		return localizedValuesGrid;
	}
	
	private void validateChanges() {
		boolean changesValid = true;
		Collection<Store<LocaleData>.Record> modifiedRecords = localizedValuesGrid.getStore().getModifiedRecords();
		for (LocaleData model : localizedValuesGrid.getStore().getAll()) {
			String locale = model.getLocale();
			String value = model.getValue();
			for (Store<LocaleData>.Record record : modifiedRecords) {
				if (model == record.getModel()) {
					String recordLocale = record.getValue(props.locale());
					if (recordLocale != null)
						locale = recordLocale;
					
					String recordValue = record.getValue(props.value());
					if (recordValue != null)
						value = recordValue;
					
					break;
				}
			}
			
			if (locale == null || locale.isEmpty()) {
				gridInlineEditing.startEditing(new GridCell(localizedValuesGrid.getStore().indexOf(model), 0));
				changesValid = false;
				break;
			} else if (value == null || value.isEmpty()) {
				gridInlineEditing.startEditing(new GridCell(localizedValuesGrid.getStore().indexOf(model), 1));
				changesValid = false;
				break;
			}
		}
		
		getApplyButton().setEnabled(changesValid);
		getAddButton().setEnabled(changesValid);
	}
	
	private TextButton prepareCancelButton() {
		TextButton cancelButton = new TextButton(LocalizedText.INSTANCE.cancel(), GMGxtSupportResources.INSTANCE.delete());
		cancelButton.setToolTip(LocalizedText.INSTANCE.cancelDescription());
		cancelButton.setIconAlign(IconAlign.TOP);
		cancelButton.setScale(ButtonScale.LARGE);
		cancelButton.addSelectHandler(event -> cancelChanges());
		
		return cancelButton;
	}
	
	private TextButton getApplyButton() {
		if (applyButton != null)
			return applyButton;
		
		applyButton = new TextButton(LocalizedText.INSTANCE.apply(), GMGxtSupportResources.INSTANCE.apply());
		applyButton.setToolTip(LocalizedText.INSTANCE.closeDescription());
		applyButton.setIconAlign(IconAlign.TOP);
		applyButton.setScale(ButtonScale.LARGE);
		applyButton.addSelectHandler(event -> applyChanges());
		return applyButton;
	}
	
	/**
	 * Applies the changes
	 */
	public void applyChanges() {
		LocalizedValuesDialog.super.hide();
	}
	
	/**
	 * Cancels the changes
	 */
	public void cancelChanges() {
		cancelChanges = true;
		LocalizedValuesDialog.super.hide();
	}
	
	@Override
	public void hide() {
		cancelChanges = true;
		super.hide();
	}
	
	/**
	 * Returns the current values
	 */
	public Map<String, String> getLocalizedValues() {
		getGridStore().commitChanges();
		Map<String, String> localizedValues = new FastMap<>();
		for (LocaleData model : getGridStore().getAll()) {
			String locale = model.getLocale();
			String value = model.getValue();
			localizedValues.put(locale, value);
		}
		
		return localizedValues;
	}
	
	/**
	 * Sets the initial values
	 */
	protected void setLocalizedValues(String defaultValue, Map<String, String> localizedValues) {
		getGridStore().clear();
		cancelChanges = false;
		itemsRemoved = false;
		//initialDefaultValue = defaultValue;
		
		getGridStore().add(createModel(DEFAULT_LOCALE, defaultValue));
		//defaultTextArea.setValue(defaultValue);
		if (localizedValues != null)
			localizedValues.entrySet().forEach(entry -> getGridStore().add(createModel(entry.getKey(), entry.getValue())));
		getAddButton().setEnabled(true);
		getClearButton().setEnabled(getGridStore().size() > 1);
		getApplyButton().setEnabled(defaultValue != null && !defaultValue.isEmpty());
	}
	
	@Override
	public void setEntityValue(LocalizedString localizedString) {
		this.localizedString = localizedString;
		Map<String, String> valuesToEditInDialog = new FastMap<>();
		String defaultValue = null;
		if (localizedString != null) {
			for (Map.Entry<String, String> entry : localizedString.getLocalizedValues().entrySet()) {
				if (!entry.getKey().equals(DEFAULT_LOCALE))
					valuesToEditInDialog.put(entry.getKey(), entry.getValue());
				else
					defaultValue = entry.getValue();
			}
		}
		
		setLocalizedValues(defaultValue, valuesToEditInDialog);
	}
	
	@Override
	public void performManipulations() {
		CollectionType mapType = GMF.getTypeReflection().getCollectionType(Map.class, new GenericModelType[] {GenericModelTypeReflection.TYPE_STRING,
			GenericModelTypeReflection.TYPE_STRING});
		
		Map<String, String> newLocalizedValues = (Map<String, String>) mapType.create();
		newLocalizedValues.putAll(getLocalizedValues());
		LocalizedString.T.getProperty("localizedValues").set(localizedString, newLocalizedValues);
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//NOP
	}
	
	/**
	 * Sets the field to be used as the editor.
	 */
	public void setEditorField(ValueBaseField<String> editorField) {
		this.editorField = editorField;
	}
	
	protected LocaleData createModel(String locale, String value) {
		return new LocaleData(locale, value);
	}
	
	@Override
	public boolean hasChanges() {
		return cancelChanges ? false : getGridStore().getModifiedRecords().size() > 0 || itemsRemoved /*|| !defaultTextArea.getValue().equals(initialDefaultValue)*/;
	}

	private TextField getMandatoryTextField() {
		TextField textField = new TextField();
		textField.setAllowBlank(false);
		textField.setAutoValidate(true);
		return textField;
	}
	
	private void configureLocaleField(SimpleComboBox<String> localeField) {
		localeField.setEditable(true);
		localeField.addValidator(new RegExValidator("(?!((name)?/.*)$).*", LocalizedText.INSTANCE.invalidFormat()));
		localeField.addValidator(new AbstractValidator<String>() {
			@Override
			public List<EditorError> validate(Editor<String> editor, String value) {
				for (int i = 0; i < getGridStore().size(); i++) {
					if (gridInlineEditing.getActiveCell() == null || gridInlineEditing.getActiveCell().getRow() != i && value != null
							&& (value.equals(DEFAULT_LOCALE) || value.equals(getGridStore().get(i).getLocale()))) {
						return createError(new DefaultEditorError(editor, LocalizedText.INSTANCE.localeExistsAlready(), value));
					}
				}
				
				return null;
			}
		});
		
		localeField.setPropertyEditor(new PropertyEditor<String>() {
			@Override
			public String parse(CharSequence text) throws ParseException {
				return text == null ? "" : text.toString();
			}
			
			@Override
			public String render(String object) {
				return object == null ? "" : object;
			}
		});
	}

	@Override
	protected void onShow() {
		super.onShow();
		gridView.refresh(true);
	}
	
	public class RefreshableView extends GridView<LocaleData> {
		
		public void refreshModel(LocaleData model) {
			refreshRow(getGridStore().indexOf(model));
		}
		
		@Override
		protected void resize() {
			localizedValuesGrid.getColumnModel().setUserResized(false);
			super.resize();
		}
		
		@Override
		protected void onRowSelect(int rowIndex) {
			super.onRowSelect(rowIndex);
			Element row = getRow(rowIndex);
		    if (row != null)
		    	row.addClassName("x-grid3-row-selected");
		}
		
		@Override
		protected void onRowDeselect(int rowIndex) {
			super.onRowDeselect(rowIndex);
			Element row = getRow(rowIndex);
		    if (row != null)
		    	row.removeClassName("x-grid3-row-selected");
		}
	}
	
	public class LocaleData {
		private Long id;
		private String locale;
		private String value;
		
		public LocaleData(String locale, String value) {
			setLocale(locale);
			setValue(value);
			id = lastId++;
		}
		
		public void setId(Long id) {
			this.id = id;
		}
		
		public Long getId() {
			return id;
		}

		public void setLocale(String locale) {
			this.locale = locale;
		}
		
		public String getLocale() {
			return locale;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}

	@Override
	public void setIsFreeInstantiation(Boolean isFreeInstantiation) {
		// NOP		
	}
}
