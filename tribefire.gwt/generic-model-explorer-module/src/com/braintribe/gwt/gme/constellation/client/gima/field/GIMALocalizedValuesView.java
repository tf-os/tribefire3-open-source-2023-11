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

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.genericmodelgxtsupport.client.field.LocalizedValuesDialog;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.gima.ButtonConfiguration;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;

/**
 * {@link GIMAEntityFieldView} implementation for handling {@link LocalizedString}.
 * @author michel.docouto
 *
 */
public class GIMALocalizedValuesView extends GIMAEntityFieldView<LocalizedValuesDialog> {
	
	private LocalizedValuesDialog localizedValuesDialog;
	private TextButton mainButton;
	private List<ButtonConfiguration> additionalButtons;

	public GIMALocalizedValuesView(LocalizedValuesDialog entityFieldDialog, GIMADialog gimaDialog) {
		super(entityFieldDialog, gimaDialog);
		localizedValuesDialog = entityFieldDialog;
	}
	
	@Override
	public TextButton getMainButton() {
		if (mainButton != null)
			return mainButton;
		
		mainButton = new TextButton(LocalizedText.INSTANCE.apply());
		mainButton.setToolTip(LocalizedText.INSTANCE.applyAllDescription());
		mainButton.addSelectHandler(event -> handleBack());
		mainButton.setScale(ButtonScale.LARGE);
		mainButton.setIconAlign(IconAlign.TOP);
		mainButton.setIcon(ConstellationResources.INSTANCE.finish());
		mainButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);
		
		return mainButton;
	}
	
	@Override
	public void handleCancel() {
		localizedValuesDialog.cancelChanges();
	}
	
	private void handleBack() {
		gimaDialog.handleHideOrBack(false, false);
		localizedValuesDialog.applyChanges();
	}
	
	@Override
	public List<ButtonConfiguration> getAdditionalButtons() {
		if (additionalButtons != null)
			return additionalButtons;

		additionalButtons = new ArrayList<>();
		additionalButtons.add(new ButtonConfiguration(localizedValuesDialog.getAddButton()));
		additionalButtons.add(new ButtonConfiguration(localizedValuesDialog.getRemoveButton()));
		additionalButtons.add(new ButtonConfiguration(localizedValuesDialog.getClearButton()));

		return additionalButtons;
	}

}
