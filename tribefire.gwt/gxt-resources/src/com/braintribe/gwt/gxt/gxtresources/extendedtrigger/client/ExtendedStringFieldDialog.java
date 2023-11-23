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
package com.braintribe.gwt.gxt.gxtresources.extendedtrigger.client;

import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.form.TextArea;

/**
 * Dialog to be displayed when using the {@link ExtendedStringField}
 * @author michel.docouto
 *
 */
public class ExtendedStringFieldDialog extends ClosableWindow {
	
	private TextArea textArea;
	private boolean applyChanges = false;
	
	public ExtendedStringFieldDialog() {
		this.setSize("400px", "350px");
		this.setHeading(LocalizedText.INSTANCE.multiLineEditor());
		this.setModal(true);
		//this.setOnEsc(false);
		
		this.addShowHandler(new ShowHandler() {
			@Override
			public void onShow(ShowEvent event) {
				applyChanges = false;
			}
		});
		
		textArea = new TextArea();
		this.add(textArea);
		this.addButton(prepareCloseButton());
	}
	
	private TextButton prepareCloseButton() {
		TextButton closeButton = new TextButton(LocalizedText.INSTANCE.apply());
		closeButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
		closeButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				applyChanges = true;
				ExtendedStringFieldDialog.super.hide();
			}
		});
		
		return closeButton;
	}
	
	public String getString() {
		return textArea.getValue();
	}
	
	public void setString(String string) {
		textArea.setValue(string);
	}
	
	public boolean isApplyChanges() {
		return applyChanges;
	}
	
	@Override
	public void hide() {
		applyChanges = false;
		
		super.hide();
	}

}
