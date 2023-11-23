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
package com.braintribe.gwt.genericmodelgxtsupport.client.field.color;

import java.text.ParseException;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.genericmodelgxtsupport.client.LocalizedText;
import com.braintribe.gwt.genericmodelgxtsupport.client.field.TriggerFieldAction;
import com.braintribe.gwt.genericmodelgxtsupport.client.resources.GMGxtSupportResources;
import com.braintribe.gwt.gmview.action.client.TrackableChangesAction;
import com.braintribe.gwt.gxt.gxtresources.multieditor.client.NoBlurWhileEditingField;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Timer;
import com.sencha.gxt.cell.core.client.form.TriggerFieldCell;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.PropertyEditor;
import com.sencha.gxt.widget.core.client.form.TriggerField;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.editing.AbstractGridEditing;

public class ColorField extends TriggerField<com.braintribe.model.style.Color> implements NoBlurWhileEditingField, TrackableChangesAction, TriggerFieldAction {
	
	private boolean editing = false;
	private ColorPicker colorPicker;
	private Window colorPickerWindow;
	private com.braintribe.model.style.Color fieldColor;
	private boolean hasChanges = false;
	private PersistenceGmSession gmSession;
	private Action triggerAction;
	private AbstractGridEditing<?> gridEditing;
	private GridCell gridCell;
	private boolean useRawValue = true;
	private boolean gettingOldValue = false;
	private com.braintribe.model.style.Color oldColor;
	private RegExp regExp = RegExp.compile("\\(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]),([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5]),([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\)");
	boolean editingField = false;
	
	public ColorField() {
		super(new TriggerFieldCell<com.braintribe.model.style.Color>());
		//setTriggerStyle("colorEditorTrigger"); TODO: use appearance
		
		setPropertyEditor(new PropertyEditor<com.braintribe.model.style.Color>() {
			@Override
			public com.braintribe.model.style.Color parse(CharSequence text) throws ParseException {
				if (gettingOldValue) {
					gettingOldValue = false;
					return oldColor;
				}
				
				String value = text.toString();
				if (!regExp.test(value))
					throw new ParseException(LocalizedText.INSTANCE.invalidColor(), 0);
				
				if (fieldColor == null && value.isEmpty())
					return fieldColor;
				
				if (fieldColor == null || !getRgbColor(fieldColor).equals(value)) {
					prepareColor(value);
					return fieldColor;
				}
				
				if (fieldColor != null && "".equals(value))
					fieldColor = null;
				return fieldColor;
			}
			
			@Override
			public String render(com.braintribe.model.style.Color color) {
				return color == null ? "" : getRgbColor(color);
			}
		});
		
		colorPickerWindow = prepareColorPickerWindow();
		setAutoValidate(true);
	}
	
	/**
	 * Configures the required {@link PersistenceGmSession}.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	private void prepareColor(String value) {
		if (fieldColor == null) {
			hasChanges = true;
			fieldColor = gmSession.create(com.braintribe.model.style.Color.T);
		}
		
		if (!getRgbColor(fieldColor).equals(value)) {
			hasChanges = true;
			Color color = new Color();
			try {
				color.setRGB(value);
			} catch (Exception e) {
				ErrorDialog.show("Error while setting color value.", e);
				e.printStackTrace();
			}
			fieldColor.setRed(color.getRed());
			fieldColor.setGreen(color.getGreen());
			fieldColor.setBlue(color.getBlue());
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
	
	@Override
	public Action getTriggerFieldAction() {
		if (triggerAction != null)
			return triggerAction;
		
		triggerAction = new Action() {
			@Override
			public void perform(TriggerInfo triggerInfo) {
				com.braintribe.model.style.Color value = null;
				Grid<Object> grid = null;
				if (gridEditing != null)
					grid = (Grid<Object>) gridEditing.getEditableGrid();
				if (grid != null)
					value = (com.braintribe.model.style.Color) grid.getColumnModel().getColumn(gridCell.getCol()).getValueProvider().getValue(grid.getStore().get(gridCell.getRow()));
				if (value != null)
					setValue(value);
				useRawValue = false;
				handleTriggerClick();
				useRawValue = true;
			}
		};
		
		addTriggerClickHandler(event -> handleTriggerClick());
		
		triggerAction.setIcon(GMGxtSupportResources.INSTANCE.color());
		triggerAction.setName(LocalizedText.INSTANCE.changeColor());
		triggerAction.setTooltip(LocalizedText.INSTANCE.changeColorDescription());
		
		return triggerAction;
	}
	
	@Override
	public void setGridInfo(AbstractGridEditing<?> gridEditing, GridCell gridCell) {
		this.gridEditing = gridEditing;
		this.gridCell = gridCell;
	}
	
	protected void handleTriggerClick() {
		if (isReadOnly())
			return;
		
		String value = null;
		if (fieldColor != null) {
			value = getRgbColor(fieldColor);
		}
		if (isEditable() && useRawValue)
			value = getText();
		
		try {
			colorPicker.setRGB(value);
		} catch (Exception e) {
			ErrorDialog.show("Error while setting color value.", e);
			e.printStackTrace();
		}
		colorPickerWindow.show();
		editingField = true;
		if (isRendered())
			getInputEl().focus();
	}
	
	@Override
	public boolean isEditingField() {
		return editingField;
	}
	
	@Override
	public com.braintribe.model.style.Color getValue() {
		if (!isRendered() || !isEditable())
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
	public void setValue(com.braintribe.model.style.Color color) {
		if (color == null)
			oldColor = null;
		else
			oldColor = (com.braintribe.model.style.Color) com.braintribe.model.style.Color.T.clone(color, null, null);
		
		hasChanges = false;
		setEditable(true);
		this.fieldColor = color;
		super.setValue(color);
	}
	
	@Override
	public void setValue(com.braintribe.model.style.Color value, boolean fireEvents, boolean redraw) {
		gettingOldValue = true;
		super.setValue(value, fireEvents, redraw);
	}
	
	private Window prepareColorPickerWindow() {
		colorPickerWindow = new Window();
		colorPickerWindow.setHeading(LocalizedText.INSTANCE.chooseColor());
		colorPickerWindow.setSize("403px", "328px");
		colorPickerWindow.setResizable(false);
		colorPickerWindow.setClosable(false);
		colorPickerWindow.setOnEsc(false);
		colorPickerWindow.setModal(true);
		
		colorPicker = new ColorPicker();
		
		final TextButton cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		final TextButton okButton = new TextButton(LocalizedText.INSTANCE.ok());
		
		SelectHandler selectHandler = event -> {
			if (event.getSource() == cancelButton) {
				colorPickerWindow.hide();
				editingField = false;
				ColorField.this.blur();
				return;
			}
			
			colorPickerWindow.hide();
			final boolean hasChanges = fieldColor == null || !getRgbColor(fieldColor).equals(colorPicker.getRGBColor());
			if (hasChanges && gridEditing != null)
				gridEditing.startEditing(gridCell);
			new Timer() {
				@Override
				public void run() {
					new Timer() {
						@Override
						public void run() {
							if (hasChanges) {
								populateField(colorPicker.getRGBColor()/*, true*/);
								if (gridEditing != null)
									gridEditing.completeEditing();
							}
							editingField = false;
							if (!hasChanges)
								blur();
						}
						
					}.schedule(50);
				}
			}.schedule(250);
		};
		okButton.addSelectHandler(selectHandler);
		cancelButton.addSelectHandler(selectHandler);
		
		colorPickerWindow.add(colorPicker);
		colorPickerWindow.addButton(okButton);
		colorPickerWindow.addButton(cancelButton);
		
		return colorPickerWindow;
	}
	
	/**
	 * Populates the field with values.
	 */
	protected void populateField(String rgbColor/*, boolean focus*/) {
		hasChanges = true;
		//if (focus)
			//focus();
		
		if (fieldColor == null)
			fieldColor = gmSession.create(com.braintribe.model.style.Color.T);
		
		if (!getRgbColor(fieldColor).equals(colorPicker.getRGBColor())) {
			Color color = new Color();
			try {
				color.setRGB(rgbColor);
			} catch (Exception e) {
				ErrorDialog.show("Error while setting color value.", e);
				e.printStackTrace();
			}
			fieldColor.setRed(color.getRed());
			fieldColor.setGreen(color.getGreen());
			fieldColor.setBlue(color.getBlue());
		}
		
		setText(rgbColor);
		setEditable(true);
	}
	
	/**
	 * Returns the (r,g,b) representation of the color
	 */
	public static String getRgbColor(com.braintribe.model.style.Color color) {
		if (color != null && color.getRed() != null && color.getGreen() != null && color.getBlue() != null) {
			StringBuilder builder = new StringBuilder();
			builder.append("(").append(color.getRed().toString()).append(",").append(color.getGreen().toString()).append(",")
					.append(color.getBlue().toString()).append(")");
			return builder.toString();
		}
		
		return null;
	}
	
	@Override
	protected void onCellParseError(ParseErrorEvent event) {
		super.onCellParseError(event);
	    parseError = LocalizedText.INSTANCE.invalidColor();
	    forceInvalid(parseError);
	}

}
