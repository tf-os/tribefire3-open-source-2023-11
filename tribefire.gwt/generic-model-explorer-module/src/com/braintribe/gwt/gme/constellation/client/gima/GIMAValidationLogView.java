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
package com.braintribe.gwt.gme.constellation.client.gima;

import java.util.List;

import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.EditEntityActionListener;
import com.braintribe.gwt.gmview.client.EditEntityContext;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.validationui.client.ValidationLogRepresentation;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * {@link GIMAView} implementation for the {@link ValidationLogRepresentation} panel.
 * @author michel.docouto
 *
 */
public class GIMAValidationLogView extends SimpleContainer implements GIMAView {
	
	private ValidationLogRepresentation validationLogRepresentation;
	private TextButton backButton;
	private GIMADialog gimaDialog;
	
	public GIMAValidationLogView(ValidationLogRepresentation validationLogRepresentation, GIMADialog gimaDialog) {
		this.validationLogRepresentation = validationLogRepresentation;
		this.gimaDialog = gimaDialog;
		add(validationLogRepresentation.asWidget());
		//validationLogRepresentation.setEditEntityActionListener(modelPath -> handleBack());
		validationLogRepresentation.setEditEntityActionListener(new EditEntityActionListener() {			
			@Override
			public void onEditEntity(ModelPath modelPath, EditEntityContext editEntityContext) {
				handleBack();				
			}
			
			@Override
			public void onEditEntity(ModelPath modelPath) {
				handleBack();				
			}
		});
	}
	
	@Override
	public TextButton getMainButton() {
		if (backButton != null)
			return backButton;
		
		backButton = new TextButton(LocalizedText.INSTANCE.back());
		backButton.setToolTip(LocalizedText.INSTANCE.backValidationDescription());
		backButton.addSelectHandler(event -> handleBack());
		backButton.setScale(ButtonScale.LARGE);
		backButton.setIconAlign(IconAlign.TOP);
		backButton.setIcon(ConstellationResources.INSTANCE.back());
		backButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);

		return backButton;
	}

	@Override
	public List<ButtonConfiguration> getAdditionalButtons() {
		return null;
	}
	
	@Override
	public boolean isApplyAllHandler() {
		return false;
	}
	
	private void handleBack() {
		gimaDialog.handleHideOrBack(false, false);
	}

	@Override
	public ModelPath getContentPath() {
		return validationLogRepresentation.getContentPath();
	}

	@Override
	public void setContent(ModelPath modelPath) {
		validationLogRepresentation.setContent(modelPath);
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		validationLogRepresentation.addSelectionListener(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		validationLogRepresentation.removeSelectionListener(sl);
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return validationLogRepresentation.getFirstSelectedItem();
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return validationLogRepresentation.getCurrentSelection();
	}

	@Override
	public boolean isSelected(Object element) {
		return validationLogRepresentation.isSelected(element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		validationLogRepresentation.select(index, keepExisting);
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		validationLogRepresentation.configureGmSession(gmSession);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return validationLogRepresentation.getGmSession();
	}

	@Override
	public void configureUseCase(String useCase) {
		validationLogRepresentation.configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		return validationLogRepresentation.getUseCase();
	}

}
