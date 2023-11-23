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
package com.braintribe.gwt.gxt.gxtresources.multieditor.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client.ClickableTriggerField;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.GXTLogConfiguration;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.data.shared.Converter;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.ComponentHelper;
import com.sencha.gxt.widget.core.client.event.CancelEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.StartEditEvent;
import com.sencha.gxt.widget.core.client.form.CheckBox;
import com.sencha.gxt.widget.core.client.form.DateField;
import com.sencha.gxt.widget.core.client.form.HtmlEditor;
import com.sencha.gxt.widget.core.client.form.IsField;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.form.error.HasErrorHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.RowExpander;
import com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing;

/**
 * Extension of GXT's {@link GridInlineEditing} with support for multiple editors in the same column.
 *
 * @author michel.docouto
 */
public class MultiEditorGridInlineEditing<M> extends GridInlineEditing<M> {
	private static Logger logger = Logger.getLogger(MultiEditorGridInlineEditing.class.getName());
	private static final int EXPANDER_LEFT_PADDING = 32;
	private static final int EDIT_FIELD_RIGHT_PADDING_AT_DIALOG = 30;
	private static final int EDIT_FIELD_RIGHT_PADDING_AT_STANDARD = 2;
	private static final int LINE_HEIGHT = 6;
	
	private Map<RowAndColumn<M>, IsField<?>> rowAndColumnEditorMap = new HashMap<>();
	private Map<RowAndColumn<M>, Converter<?, ?>> rowAndColumnConverterMap = new HashMap<>();
	private Map<RowAndColumn<M>, Boolean> editOnExpanderMap = new HashMap<>();
	protected int currentRow;
	protected GridCell multiEditorActiveCell;
	private boolean useMultiEditor = true;
	private boolean isEnterPressed = false;
	private boolean isEscPressed = false;
	private boolean useDialogSettings = false;
	private boolean ignoreClickableTriggerFieldTrigger;

	public MultiEditorGridInlineEditing(Grid<M> editableGrid) {
		super(editableGrid);
	}
	
	/**
	 * Configures the current row. Must be used prior to calling {@link #addEditor(ColumnConfig, IsField)} and {@link #addEditor(ColumnConfig, Converter, IsField)}.
	 */
	public void setCurrentRow(int currentRow) {
		this.currentRow = currentRow;
	}
	
	/**
	 * Configures whether to use multiple fields for different rows.
	 * Defaults to true.
	 */
	public void setUseMultiEditor(boolean useMultiEditor) {
		this.useMultiEditor = useMultiEditor;
	}
	
	public void setUseDialogSettings(boolean useDialogSettings) {
		this.useDialogSettings = useDialogSettings;
	}
	
	@Override
	public void clearEditors() {
		super.clearEditors();
		rowAndColumnEditorMap.clear();
		rowAndColumnConverterMap.clear();
	}
	
	@Override
	public <N, O> void addEditor(ColumnConfig<M, N> columnConfig, Converter<N, O> converter, IsField<O> field) {
		addEditor(columnConfig, converter, field, false);
	}
	
	public <N, O> void addEditor(ColumnConfig<M, N> columnConfig, Converter<N, O> converter, IsField<O> field, boolean editOnExpander) {
		super.addEditor(columnConfig, converter, field);
		if (useMultiEditor) {
			RowAndColumn<M> rowAndColumn = new RowAndColumn<M>(currentRow, columnConfig);
			rowAndColumnEditorMap.put(rowAndColumn, field);
			rowAndColumnConverterMap.put(rowAndColumn, converter);
			editOnExpanderMap.put(rowAndColumn, editOnExpander);
		}
	}
	
	@Override
	public <N> void addEditor(ColumnConfig<M, N> columnConfig, IsField<N> field) {
		addEditor(columnConfig, field, false);
	}
	
	public <N> void addEditor(ColumnConfig<M, N> columnConfig, IsField<N> field, boolean editOnExpander) {
		super.addEditor(columnConfig, field);
		if (useMultiEditor) {
			RowAndColumn<M> rowAndColumn = new RowAndColumn<M>(currentRow, columnConfig);
			rowAndColumnEditorMap.put(rowAndColumn, field);
			editOnExpanderMap.put(rowAndColumn, editOnExpander);
		}
	}
	
	@Override
	public <O> IsField<O> getEditor(ColumnConfig<M, ?> columnConfig) {
		if (multiEditorActiveCell == null)
			return super.getEditor(columnConfig);
		
		return getEditor(columnConfig, multiEditorActiveCell.getRow());
	}
	
	public <O> IsField<O> getEditor(ColumnConfig<M, ?> columnConfig, int row) {
		if (useMultiEditor)
			return (IsField<O>) rowAndColumnEditorMap.get(new RowAndColumn<M>(row, columnConfig));
		
		return super.getEditor(columnConfig);
	}
	
	@Override
	public <N, O> Converter<N, O> getConverter(ColumnConfig<M, N> columnConfig) {
		if (multiEditorActiveCell == null)
			return super.getConverter(columnConfig);
		
		return getConverter(columnConfig, multiEditorActiveCell.getRow());
	}
	
	public <N, O> Converter<N, O> getConverter(ColumnConfig<M, N> columnConfig, int row) {
		if (useMultiEditor)
			return (Converter<N, O>) rowAndColumnConverterMap.get(new RowAndColumn<M>(row, columnConfig));
		
		return super.getConverter(columnConfig);
	}
	
	/*
	 * Overriding so we do not clear the field when canceling. This was bringing issues.
	 */
	@Override
	public void cancelEditing() {
		ignoreScroll = false;
		if (GXTLogConfiguration.loggingIsEnabled())
			logger.finest("cancelEditing active is " + (activeCell == null ? "null" : "no null"));
		
		if (activeCell == null) {
			stopMonitoring();
			return;
		}
		
		Element elem = getEditableGrid().getView().getCell(activeCell.getRow(), activeCell.getCol());
		elem.getFirstChildElement().getStyle().setVisibility(Style.Visibility.VISIBLE);

		ColumnConfig<M, ?> c = columnModel.getColumn(activeCell.getCol());
		IsField<?> field = getEditor(c, activeCell.getRow());
		//field.clear();

		removeEditor(field);

		final GridCell gc = activeCell;
		activeCell = null;
		resetActiveCell();

		fireEvent(new CancelEditEvent<>(gc));

		if (getFocusOnComplete()) {
			setFocusOnComplete(false);
			focusGrid();
			// EXTGWT-2856 focus of grid not working after canceling an edit in IE.
			// something is stealing focus and the only fix so far is to run focus call in a timer. deferred does not fix.
			// need to find why focus is not staying on first call.
			if (GXT.isIE()) {
				Timer t = new Timer() {
					@Override
					public void run() {
						focusGrid();
					}
				};
				t.schedule(100);
			}
		}
		
		stopMonitoring();
	}
	
	/**
	 * Overriding for adding a delay before starting editing (due to a delay in the selection handler).
	 * Also, do not complete the edition if the cell is the same.
	 */
	@Override
	protected void handleSingleEdit(NativeEvent event) {
		Element el = event.getEventTarget().<Element> cast();
		GridCell cell = findCell(el);
		if (cell == null) {
			if (activeCell != null && isHtmlEditorAndNotClicked(activeCell, el))
				completeEditing();
			
			return;
		}

		if (activeCell != null && ((activeCell.getRow() == cell.getRow() && activeCell.getCol() != cell.getCol()) || isHtmlEditorAndNotClicked(activeCell, null)))
			completeEditing();

		//This is the actual change: using a timer instead of the scheduleDeferred
		new Timer() {
			@Override
			public void run() {
				startEditing(cell);
			}
		}.schedule(50);
	}
	
	private boolean isHtmlEditorAndNotClicked(GridCell activeCell, Element clickedElement) {
		ColumnConfig<M, ?> c = columnModel.getColumn(activeCell.getCol());
		IsField<?> field = getEditor(c, activeCell.getRow());
		
		if (!(field instanceof HtmlEditor))
			return false;
		
		if (clickedElement != null)
			return !((HtmlEditor) field).getElement().isOrHasChild(clickedElement);
		
		return true;
	}
	
	@Override
	public void startEditing(GridCell cell) {
		multiEditorActiveCell = cell;
		super.startEditing(cell);
	}
	
	@Override
	public void completeEditing() {
		super.completeEditing();
		resetActiveCell();
	}
	
	private void resetActiveCell() {
		multiEditorActiveCell = null;
	}
	
	/**
	 * Returns true for a while when the edition was canceled by an enter.
	 */
	public boolean wasEditionFinishedByEnter() {
		return isEnterPressed;
	}
	
	@Override
	public boolean isEditing() {
		return multiEditorActiveCell != null;
	}
	
	/**
	 * Returns true for a while when the edition was canceled by an esc.
	 */
	public boolean wasEditionFinishedByEsc() {
		return isEscPressed;
	}
	
	@Override
	protected <N, O> void doStartEditing(final GridCell cell) {
		multiEditorActiveCell = null;
		RowExpander<M> rowExpander = getRowExpander();
		multiEditorActiveCell = cell;
		Boolean editOnExpander = false;
		ColumnConfig<M, N> c = null;
		if (cell != null) {
			c = columnModel.getColumn(cell.getCol());
			editOnExpander = editOnExpanderMap.get(new RowAndColumn<M>(cell.getRow(), c));
			if (editOnExpander == null)
				editOnExpander = false;
		}
		
		if (GXTLogConfiguration.loggingIsEnabled())
			logger.finest("doStartEditing");

		if (getEditableGrid() == null || !getEditableGrid().isAttached() || cell == null)
			return;
		
		M value = getEditableGrid().getStore().get(cell.getRow());

		if (c == null || value == null)
			return;
		
		final IsField<O> field = getEditor(c, cell.getRow());
		if (field == null)
			return;
		
		Converter<N, O> converter = getConverter(c, cell.getRow());

		ValueProvider<? super M, N> v = c.getValueProvider();
		N colValue = getEditableGrid().getStore().hasRecord(value) ? getEditableGrid().getStore().getRecord(value).getValue(v) : v.getValue(value);
		O convertedValue = converter != null ? converter.convertModelValue(colValue) : (O) colValue;

		if (field instanceof HasErrorHandler)
			((HasErrorHandler) field).setErrorSupport(null);

		activeCell = cell;

		if (GXTLogConfiguration.loggingIsEnabled())
			logger.finest("doStartEditing convertedValue: " + convertedValue);

		field.setValue(convertedValue);

		if (field instanceof TriggerField<?>)
			((TriggerField<?>) field).setMonitorTab(false);

		Widget w = field.asWidget();
		getEditableGrid().getView().getEditorParent().appendChild(w.getElement());
		ComponentHelper.setParent(getEditableGrid(), w);
		ComponentHelper.doAttach(w);

		//This is the actual difference from the default: we are getting the the size of the view, and the position of the expander.
		//Blur stuff bellow also changes.		
		int fieldRightPadding = (useDialogSettings) ? EDIT_FIELD_RIGHT_PADDING_AT_DIALOG : EDIT_FIELD_RIGHT_PADDING_AT_STANDARD;
		
		if (!editOnExpander || rowExpander == null) {
			int padding = 1;
			
			if ((cell.getRow() == 0) && (!w.getStyleName().contains("metaDataField"))) //TODO: this should be done based on some interface
				padding = 4;
			
			w.setPixelSize(c.getWidth() - fieldRightPadding, Integer.MIN_VALUE);
			w.getElement().<XElement>cast().makePositionable(true);
			Element row = getEditableGrid().getView().getRow(cell.getRow());
			int left = 1;
			for (int i = 0; i < cell.getCol(); i++) {
				if (!columnModel.isHidden(i))
					left += columnModel.getColumnWidth(i);
			}
			
			w.getElement().<XElement> cast().setLeftTop(left,
					row.getAbsoluteTop() - getEditableGrid().getView().getBody().getAbsoluteTop() + padding);
		} else {
			Element row = getEditableGrid().getView().getRow(cell.getRow());
			rowExpander.expandRow(cell.getRow());
			int pixelSize = c.getWidth();
			pixelSize = editableGrid.getView().getBody().getWidth(true) - EXPANDER_LEFT_PADDING - fieldRightPadding;
			w.setPixelSize(pixelSize, Integer.MIN_VALUE);
			w.getElement().<XElement>cast().makePositionable(true);
			
			int bodyTop = getEditableGrid().getView().getBody().getAbsoluteTop();
			int rowBodyTop = getEditableGrid().getView().getRowBody(row).getAbsoluteTop();
			
			w.getElement().<XElement> cast().setLeftTop(EXPANDER_LEFT_PADDING, rowBodyTop - bodyTop + LINE_HEIGHT);
		}
		
		Boolean isReadOnly = (field instanceof ValueBaseField<?>) ? ((ValueBaseField<?>) field).isReadOnly() : false;

		if (!isReadOnly)
			field.asWidget().setVisible(true);

		startMonitoring();

		Scheduler.get().scheduleDeferred(() -> {
			if (GXTLogConfiguration.loggingIsEnabled())
				logger.finest("doStartEditing scheduleDeferred doFocus ");

			// browsers select all when tabbing into a input and put cursor at location when clicking into an input
			// with inline editing, the field is not visible at time of click so we select all. we ignore
			// field.isSelectOnFocus as this only applies when clicking into a field
			if (field instanceof ValueBaseField<?>) {
				ValueBaseField<?> vf = (ValueBaseField<?>) field;
				vf.selectAll();
			}
			
			if (field instanceof DateField) {
				//Somehow, this attribute is deleted when preparing it directly when building the field.
				//It is needed here.
				DateField dateField = (DateField) field;
				XElement inputEl = XElement.as(dateField.getCell().getInputElement(dateField.getElement()));
				if (inputEl != null)
					inputEl.setAttribute("autocomplete", "off");
			}

			// EXTGWT-2856 calling doFocus before selectAll is causing blur to fire which ends the edit immediately
			// after it starts
			if (!isReadOnly) 
				doFocus(field);

			ignoreScroll = false;

			fieldRegistration.removeHandler();

			fieldRegistration.add(field.addValueChangeHandler(event -> {
				if (GXTLogConfiguration.loggingIsEnabled())
					logger.finest("doStartEditing onValueChanged");

				// if enter key cause value change we want to ignore the next
				// enter key otherwise
				// new edit will start by onEnter
				setIgnoreNextEnter(true);
				isEnterPressed = true; //Here is another change. We want to know when enter was pressed, for a while.

				Timer t = new Timer() {
					@Override
					public void run() {
						setIgnoreNextEnter(false);
						isEnterPressed = false; //Here is another change. We want to know when enter was pressed, for a while.
					}
				};

				completeEditing();

				t.schedule(100);
			}));

			fieldRegistration.add(field.addBlurHandler(event -> {
				//Here is another change. We check if the field is a NoBlurWhileEditingField.
				if (event.getSource() instanceof NoBlurWhileEditingField && ((NoBlurWhileEditingField) event.getSource()).isEditingField())
					return;
				
				if (GXTLogConfiguration.loggingIsEnabled())
					logger.finest("doStartEditing onBlur");

				setIgnoreNextEnter(true);
				isEnterPressed = true; //Here is another change. We want to know when enter was pressed, for a while.

				Timer t = new Timer() {
					@Override
					public void run() {
						setIgnoreNextEnter(false);
						isEnterPressed = false; //Here is another change. We want to know when enter was pressed, for a while.
					}
				};

				if (GXTLogConfiguration.loggingIsEnabled())
					logger.finest("doStartEditing onBlur call cancelEditing");

				cancelEditing();

				t.schedule(100);
			}));

			fireEvent(new StartEditEvent<M>(cell));
		});
		
		if (field instanceof ClickableTriggerField) {
			((ClickableTriggerField) field).setHideTrigger(!ignoreClickableTriggerFieldTrigger);
			if (!ignoreClickableTriggerFieldTrigger)
				Scheduler.get().scheduleDeferred(() -> ((ClickableTriggerField) field).fireTriggerClick(null));
		}
		
		ignoreClickableTriggerFieldTrigger = false;
	}
	
	/**
	 * Ignores firing the triggerClick when starting the edition for the next time.
	 */
	public void ignoreClickableTriggerFieldTrigger() {
		ignoreClickableTriggerFieldTrigger = true;
	}
	
	public void markAsFinishedByEnter() {
		isEnterPressed = true;
		new Timer() {
			@Override
			public void run() {
				isEnterPressed = false;
			}
		}.schedule(100);
	}
	
	@Override
	protected void onScroll(ScrollEvent event) {
		//Removing feature that cancelled an edition when scrolling.
	}
	
	@Override
	protected void onEsc(NativeEvent evt) {
		super.onEsc(evt);
		isEscPressed = true;
		//RVE - evt. calls disabled - wasn;t work in case when component is inside component -> parent dialog wans't been allowed to close on ESC key
		//evt.stopPropagation();
		//evt.preventDefault();
		new Timer() {
			@Override
			public void run() {
				//For a while, we want to know when the esc was pressed
				isEscPressed = false;
			}
		}.schedule(100);
	}
	
	private RowExpander<M> getRowExpander() {
		return (RowExpander<M>) editableGrid.getColumnModel().getColumns().stream().filter(c -> c instanceof RowExpander).findFirst().orElse(null);
	}
	
	/**
	 * Overwritten because we are having issues when the next element is not editable.
	 */
	@Override
	protected void onTab(NativeEvent evt) {
		if (GXTLogConfiguration.loggingIsEnabled())
			logger.finest("onTab");

		// keep active cell since we manually fire blur (finishEditing) which will
		// call cancel edit clearing active cell
		final GridCell active = activeCell;

		if (GXTLogConfiguration.loggingIsEnabled())
			logger.finest("onTab activeCell is " + (activeCell == null ? "null" : "not null"));

		if (activeCell != null) {
			ColumnConfig<M, ?> c = columnModel.getColumn(activeCell.getCol());

			IsField<?> field = getEditor(c);

			// we handle navigation programmatically
			evt.preventDefault();

			// since we are preventingDefault on tab key, the field will not blur on
			// its own, which means the value change event will not fire so we manually
			// blur the field, so we call finishEditing
			if (GXTLogConfiguration.loggingIsEnabled())
				logger.finest("onTab calling field.finishEditing()");
			field.finishEditing();
		}

		if (active == null)
			return;
		
		GridCell newCell = null;

		if (evt.getShiftKey())
			newCell = getEditableGrid().walkCells(active.getRow(), active.getCol() - 1, -1, callback);
		else
			newCell = getEditableGrid().walkCells(active.getRow(), active.getCol() + 1, 1, callback);
		
		if (newCell != null) {
			final GridCell c = newCell;

			Scheduler.get().scheduleFinally(() -> {
				if (GXTLogConfiguration.loggingIsEnabled())
					logger.finest("onTab scheduleFinally startEditing");
				startEditing(c);
				
				// The difference from GXT base is here. If the cell we are has no editor, then we start editing
				// the previous and immediately cancel the edition in order to remove the editor from the DOM.
				if (nextCellHasNoEditor(c)) {
					startEditing(active);
					cancelEditing();
					//completeEditing();
					getEditableGrid().getView().focusCell(active.getRow(), active.getCol(), true);
				}
			});
		} else {
			// when tabbing and no next cell to start edit, current edit is not ending
			// the focusCell call is not causing field to blur and finish editing
			if (isEditing())
				completeEditing();
			getEditableGrid().getView().focusCell(active.getRow(), active.getCol(), true);
		}
	}
	
	/**
	 * Overriding because removing the editor may clear the activeCell under some circumstances.
	 */
	@Override
	protected <N, O> void doCompleteEditing() {
		if (GXTLogConfiguration.loggingIsEnabled())
			logger.finest("doCompleteEditing activeCell is " + (activeCell != null ? " is not null" : "is null"));

		if (activeCell == null)
			return;
		
		final ColumnConfig<M, N> c = columnModel.getColumn(activeCell.getCol());

		IsField<O> field = getEditor(c);
		if (field == null) {
			activeCell = null;
			return;
		}

		Converter<N, O> converter = getConverter(c);

		if (!field.isValid(false) && isRevertInvalid()) {
			cancelEditing();
			return;
		}

		O fieldValue = null;

		if (field instanceof ValueBaseField)
			fieldValue = ((ValueBaseField<O>) field).getCurrentValue();
		else
			fieldValue = field.getValue();

		final N convertedValue;
		if (converter != null)
			convertedValue = converter.convertFieldValue(fieldValue);
		else
			convertedValue = (N) fieldValue;

		if (GXTLogConfiguration.loggingIsEnabled())
			logger.finest("Converted value: " + convertedValue);

		removeEditor(field);
		if (activeCell == null)
			return;

		ListStore<M> store = getEditableGrid().getStore();
		ListStore<M>.Record r = store.getRecord(store.get(activeCell.getRow()));

		rowUpdated = true;

		r.addChange(c.getValueProvider(), convertedValue);
		fireEvent(new CompleteEditEvent<M>(activeCell));

		if (getFocusOnComplete()) {
			setFocusOnComplete(false);
			focusGrid();
		}

		activeCell = null;
	}
	
	protected boolean nextCellHasNoEditor(GridCell cell) {
		ColumnConfig<M, ?> c = columnModel.getColumn(cell.getCol());

		IsField<?> editor = getEditor(c);
		return editor == null || editor instanceof CheckBox; //Difference from GXT: if the editor is a CheckBox, we do not inline edit.
	}

	protected native void setIgnoreNextEnter(boolean nextEnter) /*-{
		this.@com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing::ignoreNextEnter = nextEnter;
	}-*/;
	
	protected native boolean getIgnoreNextEnter() /*-{
		return this.@com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing::ignoreNextEnter;
	}-*/;
	
	protected native void setFocusOnComplete(boolean focus) /*-{
		this.@com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing::focusOnComplete = focus;
	}-*/;
	
	protected native boolean getFocusOnComplete() /*-{
		return this.@com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing::focusOnComplete;
	}-*/;
	
	/*
	protected native void removeEditor(final GridCell cell, final IsField<?> field) /*-{
		return this.@com.sencha.gxt.widget.core.client.grid.editing.GridInlineEditing::removeEditor(Lcom/sencha/gxt/widget/core/client/grid/Grid$GridCell;Lcom/sencha/gxt/widget/core/client/form/IsField;)(cell, field);
	}-*/ //;
	
	private void removeEditor(final IsField<?> field) {
	    assert field != null;
	    removeFieldBlurHandler();

	    if (GXT.isIE() && field instanceof ValueBaseField<?>) {
	      ValueBaseField<?> f = (ValueBaseField<?>) field;
	      f.getCell().getInputElement(f.getElement()).blur();
	    }

	    Widget w = field.asWidget();
	    if (w.isAttached()) {
	    	//RVE - this makes problem when is inside the SearchQueryDialog, and is inside the ComboBox-Change value with Enter loose dialog focus
	    	field.asWidget().setVisible(false);
	    	
	    	ComponentHelper.setParent(null, w);
	    	ComponentHelper.doDetach(w);
	    }
	}
	
	private void removeFieldBlurHandler() {
	    fieldRegistration.removeHandler();
	}	
	
	private static class RowAndColumn<M> {
		private int row;
		private ColumnConfig<M,?> columnConfig;
		
		public RowAndColumn(int row, ColumnConfig<M, ?> columnConfig) {
			this.row = row;
			this.columnConfig = columnConfig;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((columnConfig == null) ? 0 : columnConfig.hashCode());
			result = prime * result + row;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RowAndColumn<M> other = (RowAndColumn<M>) obj;
			if (columnConfig != other.columnConfig)
				return false;
			if (row != other.row)
				return false;
			return true;
		}
	}

}
