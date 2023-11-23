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
package com.braintribe.gwt.gme.constellation.client.gima.field;

import java.util.List;

import com.braintribe.gwt.aceeditor.client.GmScriptEditorDialog;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.gima.ButtonConfiguration;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.google.gwt.core.client.Scheduler;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class GIMAScriptEditorView extends GIMAEntityFieldView<GmScriptEditorDialog>{

	private TextButton mainButton;
	private GmScriptEditorDialog dialog;

	public GIMAScriptEditorView(GmScriptEditorDialog dialog, GIMADialog gimaDialog) {
		super(dialog, gimaDialog);
		this.dialog = dialog;
	}

	@Override
	public void handleCancel() {
		dialog.cancelChanges();
		//Scheduler.get().scheduleDeferred(() -> gimaDialog.handleHideOrBack(false, false));
	}
	
	private void handleBack() {
		Scheduler.get().scheduleDeferred(() -> {		
			dialog.applyChanges();
			gimaDialog.handleHideOrBack(false, false);
		});
	}
	
	@Override
	public TextButton getMainButton() {		
		if (mainButton != null)
			return mainButton;
		
		if (!dialog.isReadOnly()) {		
			mainButton = new TextButton(LocalizedText.INSTANCE.apply());
			mainButton.setToolTip(LocalizedText.INSTANCE.applyAllDescription());
			mainButton.addSelectHandler(event -> handleBack());
			mainButton.setIcon(ConstellationResources.INSTANCE.finish());
			mainButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);
		} else {
			mainButton = new TextButton(LocalizedText.INSTANCE.back());
			mainButton.setToolTip(LocalizedText.INSTANCE.backTypeDescription());			
			mainButton.addSelectHandler(event -> handleCancel());
			mainButton.setIcon(ConstellationResources.INSTANCE.back());
			mainButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);			
		}
		mainButton.setScale(ButtonScale.LARGE);
		mainButton.setIconAlign(IconAlign.TOP);
		
		return mainButton;
	}

	@Override
	public List<ButtonConfiguration> getAdditionalButtons() {
		return null;
	}
	
}
