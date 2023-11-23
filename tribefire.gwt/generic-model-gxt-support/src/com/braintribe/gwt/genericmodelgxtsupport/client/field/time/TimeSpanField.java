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
package com.braintribe.gwt.genericmodelgxtsupport.client.field.time;

import java.text.ParseException;

import com.braintribe.cfg.Configurable;
import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.gmview.action.client.TrackableChangesAction;
import com.braintribe.gwt.gmview.client.GmSessionHandler;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;

/**
 * Field for editing TimeSpan
 *
 */

public class TimeSpanField extends ComboBox<TimeSpan> implements NoBlurWhileEditingField, TrackableChangesAction, GmSessionHandler/*, TriggerFieldAction*/ {
	private static final String EMPTY_STRING = ""; 
	
	private boolean editing = false;
	private TimeSpan fieldTime;
	private boolean hasChanges = false;
	private PersistenceGmSession gmSession;
	//private Action triggerAction;
	//private AbstractGridEditing<?> gridEditing;
	//private GridCell gridCell;
	//private boolean useRawValue = true;
	private boolean gettingOldValue = false;
	private TimeSpan oldTime;
	private TimeUnit defaultTimeUnit = TimeUnit.milliSecond;
	private RegExp regExp = RegExp.compile("(([0-9]\\w+|[0-9])\\056([0-9]\\w+|[0-9])|([0-9]\\w+|[0-9]))");
	boolean editingField = false;
	
	static ListStore<TimeSpan> listStore = null;
	
	public TimeSpanField() {		
		super(new ComboBoxCell<> (getListStore(), getComboBoxLabelProvider()));
		//super(new TriggerFieldCell<TimeSpan>());
						
		setPropertyEditor(new PropertyEditor<TimeSpan>() {
			@Override
			public TimeSpan parse(CharSequence text) throws ParseException {												
				String value = text.toString();
								
				if (value.isEmpty()) {
					updateListStore(null, gmSession);
					if (fieldTime != null)
						hasChanges = true; 
					fieldTime = null;
					return fieldTime;
				} 
				
				if (!regExp.test(value))
					throw new ParseException(LocalizedText.INSTANCE.invalidTime(), 0);
					
				if (fieldTime == null && value.isEmpty()) {
					updateListStore(null, gmSession);
					return fieldTime;
				} else if (fieldTime == null || !getShortTimeString(fieldTime).equals(value)) {
					prepareTime(value);
					return fieldTime;
				} else if (fieldTime != null && "".equals(value)) { 
					updateListStore(null, gmSession);
					hasChanges = true;
					fieldTime = null;
					return fieldTime;
				} 
				return fieldTime;
			}
			
			@Override
			public String render(TimeSpan time) {
				return time == null ? "" : getShortTimeString(time); 
			}
		});
		
		addFocusHandler(event -> setText(getShortTimeString(fieldTime)));
						
		addSelectionHandler(event -> {
			if (event.getSelectedItem() != null) {
				if (fieldTime == null)
					fieldTime = gmSession.create(TimeSpan.T);
				fieldTime.setValue(event.getSelectedItem().getValue());				{
				fieldTime.setUnit(event.getSelectedItem().getUnit());
			}
		} 				
		});
		
		setReadOnly(false);
		setVisible(true);
		//setValidationDelay(0);
		//setAutoValidate(true);	
		//setTypeAheadDelay(0);
		//setTypeAhead(true);
		setSelectOnFocus(true);
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	
	/**
	 * Configures the default time Unit if not Unit is set. Default is TimeUnit.milliSecond
	 */	
	@Configurable
	public void setDefaultTimeUnit(TimeUnit timeUnit) {
		this.defaultTimeUnit = timeUnit;
	}
	
	@Override
	public PersistenceGmSession getGmSession() {
		return gmSession;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	private void prepareTime(String value) {
		if (fieldTime == null) {
			hasChanges = true;
			fieldTime = gmSession.create(TimeSpan.T);
		}
		
		Double doubleValue = fieldTime == null ? 0.0 : fieldTime.getValue();
		TimeUnit unit = fieldTime == null ? defaultTimeUnit : (fieldTime.getUnit() == null ? defaultTimeUnit : fieldTime.getUnit());
		
		for (TimeUnit timeUnit : TimeUnit.values()) {
			if (value.contains(timeUnit.toString()))
				unit = timeUnit;
		}
		
		String numberString = value.replace(unit.toString(), "");
		numberString = numberString.trim().toLowerCase();
		
		//can contains time shortcuts: ms (milliSeconds), us or �s (microSecond), ns (nanoSeconds),s (seconds),h (hour),m (minute),d (day), p (plancTime)			
		if (numberString.contains("ms") || numberString.contains("millis"))
			unit = TimeUnit.milliSecond;
		else if (numberString.contains("�s") || numberString.contains("us") || numberString.contains("micros"))
			unit = TimeUnit.microSecond;
		else if (numberString.contains("ns") || numberString.contains("nanos"))
			unit = TimeUnit.nanoSecond;
		else if (numberString.contains("s") || numberString.contains("sec"))
			unit = TimeUnit.second;
		else if (numberString.contains("h"))
			unit = TimeUnit.hour;
		else if (numberString.contains("m") || numberString.contains("min"))
			unit = TimeUnit.minute;
		else if (numberString.contains("d"))
			unit = TimeUnit.day;
		else if (numberString.contains("p"))
			unit = TimeUnit.planckTime;
		
		try {
			doubleValue = Double.parseDouble(numberString);
		} catch (Exception e) {
			//NOP
		}
		
		if (!getShortTimeString(fieldTime).equals(getShortTimeString(doubleValue, unit))) {
			hasChanges = true;
						
			fieldTime.setValue(doubleValue);
			fieldTime.setUnit(unit);
			
			updateListStore(doubleValue, gmSession);			
		}
	}
	
	@Override
	public boolean isEditing() {
		return editing;
	}
	
	@Override
	public boolean hasChanges() {
		return hasChanges;
	}
	
	
	/*@Override
	public Action getTriggerFieldAction() {
		if (this.triggerAction == null) {
			this.triggerAction = new Action() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public void perform(TriggerInfo triggerInfo) {
					TimeSpan value = null;
					Grid<Object> grid = null;
					if (gridEditing != null)
						grid = (Grid) gridEditing.getEditableGrid();
					if (grid != null)
						value = (TimeSpan) grid.getColumnModel().getColumn(gridCell.getCol()).getValueProvider().getValue(grid.getStore().get(gridCell.getRow()));
					if (value != null)
						setValue(value);
					//useRawValue = false;
					handleTriggerClick();
					//useRawValue = true;
				}
			};
			
			addTriggerClickHandler(new TriggerClickHandler() {
				@Override
				public void onTriggerClick(TriggerClickEvent event) {
					handleTriggerClick();
				}
			});
			
			this.triggerAction.setIcon(GMGxtSupportResources.INSTANCE.color());
			this.triggerAction.setName(LocalizedText.INSTANCE.changeTime());
			this.triggerAction.setTooltip(LocalizedText.INSTANCE.changeTimeDescription());
		}
		
		return this.triggerAction;
	}*/
	
	/*@Override
	public void setGridInfo(AbstractGridEditing<?> gridEditing, GridCell gridCell) {
		this.gridEditing = gridEditing;
		this.gridCell = gridCell;
	}*/
	
	/*protected void handleTriggerClick() {		
		if (isReadOnly())
			return;
		
		/*String value = null;
		if (fieldTime != null) {
			value = fieldTime.toString();
		}
		if (isEditable() && useRawValue)
			value = getText();*
		
		this.editingField = true;
		if (isRendered())
			getInputEl().focus();
	}*/
	
	@Override
	public boolean isEditingField() {
		return editingField;
	}
	
	@Override
	public TimeSpan getValue() {
		if (!isRendered() || !isEditable())
			return super.getValue();
		
		String v = getText();
		if (getEmptyText() != null && v.equals(getEmptyText()))
			return null;

		if (gettingOldValue) {
			gettingOldValue = false;
			return oldTime;
		}
		try {			
			//return getPropertyEditor().parse(v);
			
			getPropertyEditor().parse(v);
			if (fieldTime == null)
				return null;
			
			updateOriginalValue();		
			return super.getValue();
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void setValue(TimeSpan time) {
		oldTime = time;		
		hasChanges = false;
		updateLocalValue(time);
		super.setValue(time);
	}
	
	@Override
	public void setValue(TimeSpan value, boolean fireEvents, boolean redraw) {
		gettingOldValue = true;
		super.setValue(value, fireEvents, redraw);
	}
	
	private static void updateListStore(Double doubleValue, PersistenceGmSession session) {			
		if (listStore == null)
			return;		

		if (doubleValue == null) {
			listStore.clear();
			return;
		}
		
		if (listStore.size() == 0) {
			for (TimeUnit timeUnit : TimeUnit.values()) {
				TimeSpan timeSpan;
				if (session == null)
					timeSpan = TimeSpan.T.create();
				else
					timeSpan = session.create(TimeSpan.T);
				timeSpan.setUnit(timeUnit);
				timeSpan.setValue(doubleValue);
				listStore.add(timeSpan);
			}
		} else {		
			for (TimeSpan timeSpan : listStore.getAll()) {
				timeSpan.setValue(doubleValue);
				listStore.update(timeSpan);
			}
		}
		
		//add sorted from day, hour, minute...to nanoSecond
		/*
		TimeSpan item =  TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.day);
		listStore.add(item);
		
		item = TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.hour);
		listStore.add(item);

		item = TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.minute);
		listStore.add(item);

		item = TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.second);
		listStore.add(item);

		item = TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.milliSecond);
		listStore.add(item);

		item = TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.microSecond);
		listStore.add(item);
		
		item = TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.nanoSecond);
		listStore.add(item);
		
		item = TimeSpan.T.create();
		item.setValue(doubleValue);
		item.setUnit(TimeUnit.planckTime);
		listStore.add(item);
		*/

		/*
		for (TimeUnit unit : TimeUnit.values()) {			
			//TimeSpan item = gmSession.create(TimeSpan.T);			
			item.setValue(doubleValue);
			item.setUnit(unit);
			listStore.add(item);
		}
		*/
			
	} 				
	public static ListStore<TimeSpan> getListStore() {
		listStore = new ListStore<>(item -> item.toString());
		updateListStore(0.0, null);			
		return listStore;
	}		
	
	public static LabelProvider<TimeSpan> getComboBoxLabelProvider() {
		LabelProvider<TimeSpan> labelProvider = item -> getShortTimeString(item);
		return labelProvider;
	}
	
	/**
	 * Returns the time string representation of the TimeSpan with short name (s, h, m, ms, ns, ...)
	 */
	public static String getShortTimeString(TimeSpan time) {
		if (time == null)
			return null;
		
		StringBuilder builder = new StringBuilder();
		builder.append(time.getValue());
		if (time.getUnit() != null)
			builder.append(" ").append(getShortTimeUnit(time.getUnit()));
		return builder.toString();
	}
	
	/**
	 * Returns the time string representation of the TimeSpan with short name (s, h, m, ms, ns, ...)
	 */
	public static String getShortTimeString(Double doubleValue, TimeUnit timeUnit) {
		StringBuilder builder = new StringBuilder();
		builder.append(doubleValue);
		if (timeUnit != null)
			builder.append(" ").append(getShortTimeUnit(timeUnit));
		return builder.toString();
	}
	
	public static String getShortTimeUnit (TimeUnit timeUnit) {
		if (timeUnit == null)
			return EMPTY_STRING;
		
		SafeHtmlBuilder builder = new SafeHtmlBuilder();			
		if (timeUnit == TimeUnit.milliSecond)
			builder.appendEscaped("ms");
		else if (timeUnit == TimeUnit.microSecond)
			builder.appendEscaped("\u00B5").appendEscaped("s");
		else if (timeUnit == TimeUnit.nanoSecond)
			builder.appendEscaped("ns");
		else if (timeUnit == TimeUnit.second)
			builder.appendEscaped("s");
		else if (timeUnit == TimeUnit.hour)
			builder.appendEscaped("h");
		else if (timeUnit == TimeUnit.minute)
			builder.appendEscaped("m");
		else if (timeUnit == TimeUnit.day)
			builder.appendEscaped("d");
		else if (timeUnit == TimeUnit.planckTime)
			builder.appendEscaped("pt");
		
		return builder.toSafeHtml().asString();
	} 	
	
	  /**
	   * Return the parsed value, or null if the field is empty.
	   * Override because need parse call, also when value is empty - to refresh combo box items
	   * 
	   * @return the parsed value
	   * @throws ParseException if the value cannot be parsed
	   */
	@Override
	public TimeSpan getValueOrThrow() throws ParseException {
	    return getCell().getPropertyEditor().parse(getText());
	}
	
	/**
	 * Returns the time string representation of the TimeSpan
	 */
	public static String getTimeString(TimeSpan time) {
		if (time == null)
			return null;
		
		StringBuilder builder = new StringBuilder();
		builder.append(time.getValue());
		if (time.getUnit() != null)
			builder.append(" ").append(time.getUnit().toString());
		
		return builder.toString();
	}
	
	/**
	 * Returns the time string representation of the TimeSpan
	 */
	public static String getTimeString(Double doubleValue, TimeUnit timeUnit) {
		StringBuilder builder = new StringBuilder();
		builder.append(doubleValue);
		if (timeUnit != null)
			builder.append(" ").append(timeUnit.toString());
		
		return builder.toString();
	}
		
	private void updateOriginalValue() {
		if (fieldTime == null)
			return;
		
		TimeSpan time = super.getValue();
		/*
		if (time == null) {
			time = this.gmSession.create(TimeSpan.T);	
		}
		*/
		if (time != null) {
			time.setValue(fieldTime.getValue());
			time.setUnit(fieldTime.getUnit());
		}
		
		//time = this.fieldTime;
		//setValue(time);
	}
			
	private void updateLocalValue(TimeSpan time) {
		if (time == null) {
			fieldTime = null;
			return;
		}
		
		if (fieldTime == null || fieldTime == time)
			fieldTime = (TimeSpan) TimeSpan.T.clone(time, null, null);
		fieldTime.setValue(time.getValue());
		if (time.getUnit() == null)
			fieldTime.setUnit(defaultTimeUnit);
		else
			fieldTime.setUnit(time.getUnit());				
	}
	
	
	@Override
	protected void onCellParseError(ParseErrorEvent event) {
		super.onCellParseError(event);
	    parseError = LocalizedText.INSTANCE.invalidTime();
	    forceInvalid(parseError);
	}

}
