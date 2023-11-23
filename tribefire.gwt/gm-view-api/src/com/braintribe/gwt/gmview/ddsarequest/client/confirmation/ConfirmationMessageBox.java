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
package com.braintribe.gwt.gmview.ddsarequest.client.confirmation;

import java.util.List;

import com.braintribe.gwt.gmview.ddsarequest.client.LocalizedText;
import com.braintribe.model.extensiondeployment.meta.ConfirmationMouseClick;
import com.braintribe.model.meta.data.constraint.Confirmation;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.TextButtonCell;
import com.sencha.gxt.core.client.dom.XElement;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

/**
 * Extension of the MessageBox for handling {@link Confirmation}.
 * @author michel.docouto
 *
 */
public class ConfirmationMessageBox extends MessageBox {

	private boolean altKeyPressed;
	private boolean shiftKeyPressed;
	private boolean ctrlKeyPressed;
	private TextButton buttonPressed;
	private String okDisplay;
	private String cancelDisplay;
	private ConfirmationMouseClick mouseClick;
	private SelectHandler handler = event -> onButtonPressed((TextButton) event.getSource());
	
	public ConfirmationMessageBox(String messageText, String okDisplay, String cancelDisplay, ConfirmationMouseClick mouseClick) {
		super(LocalizedText.INSTANCE.confirmation(), messageText);
		this.okDisplay = okDisplay;
		this.cancelDisplay = cancelDisplay;
		this.mouseClick = mouseClick;
	}
	
	@Override
	protected String getText(PredefinedButton button) {
		if (PredefinedButton.OK.equals(button))
			return okDisplay == null ? LocalizedText.INSTANCE.ok() : okDisplay;
			
		return cancelDisplay == null ? LocalizedText.INSTANCE.cancel() : cancelDisplay;
	}
	
	//Overriding to check click event on the button cell, this is the last place where the NativeEvent is seen, and I can check the keys
	@Override
	protected void createButtons() {
		if (mouseClick == null || mouseClick.equals(ConfirmationMouseClick.none)) {
			super.createButtons();
			return;
		}
		
		Widget focusWidget = getFocusWidget();
		boolean focus = focusWidget == null || (getButtonBar().getWidgetIndex(focusWidget) != -1);
		getButtonBar().clear();

		List<PredefinedButton> buttons = getPredefinedButtons();
		for (int i = 0; i < buttons.size(); i++) {
			PredefinedButton b = buttons.get(i);
			TextButton tb = new TextButton(new TextButtonCell() {
				@Override
				protected void onClick(Context context, XElement p, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
					shiftKeyPressed = event.getShiftKey();
					ctrlKeyPressed = event.getCtrlKey();
					altKeyPressed = event.getAltKey();
					super.onClick(context, p, value, event, valueUpdater);
				}
			}, getText(b));
			tb.setItemId(b.name());
			tb.addSelectHandler(handler);
			if (i == 0 && focus)
				setFocusWidget(tb);
			addButton(tb);
		}
	}
	
	@Override
	protected void onButtonPressed(TextButton textButton) {
		buttonPressed = textButton;
		super.onButtonPressed(textButton);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void hide() {
		if (mouseClick == null || mouseClick.equals(ConfirmationMouseClick.none)) {
			super.hide();
			return;
		}
		
		PredefinedButton predefinedButton = buttonPressed == null ? null : getPredefinedButton(buttonPressed);
		if (predefinedButton == null)
			return;
		
		if (predefinedButton.equals(PredefinedButton.OK)) {
			switch (mouseClick) {
				case alt:
					if (!altKeyPressed)
						return;
					break;
				case ctrl:
					if (!ctrlKeyPressed)
						return;
					break;
				case shift:
					if (!shiftKeyPressed)
						return;
					break;
			}
		}
		
		super.hide();
	}

}
