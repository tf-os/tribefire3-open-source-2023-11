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

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.SelectionConstellation;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel.SpotlightData;
import com.braintribe.gwt.gmview.client.ExpertUI;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButton;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButtonCell;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;

public class GIMASelectionContentView extends ContentPanel implements GmContentView, GIMAView, InitializableBean {

	private SelectionConstellation selectionConstellation;
	private TextButton propertyPanelToggleButton;
	private TextButton addButton;
	private boolean showPropertyPanel;
	private List<ButtonConfiguration> additionalButtons;
	private TextButton assignButton;
	private GIMASelectionConstellation gimaSelectionConstellation;
	private boolean detailPanelButtonHidden;
	private boolean addUsed;
	private boolean updateButtonsDisabled;
	private boolean addResizePerformed;

	/**
	 * Configures the required {@link SelectionConstellation} view.
	 */
	@Required
	public void setSelectionConstellation(SelectionConstellation selectionConstellation) {
		this.selectionConstellation = selectionConstellation;
		selectionConstellation.hideHome();
		selectionConstellation.getVerticalTabPanel().addVerticalTabListener(new VerticalTabListener() {
			@Override
			public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
				if (verticalTabElement == null)
					return;
				
				boolean isExpertUI = verticalTabElement.getWidget() instanceof ExpertUI;
				propertyPanelToggleButton.setEnabled(!isExpertUI);
				if (isExpertUI && showPropertyPanel)
					handlePropertyPanelToggle(true);
			}
			
			@Override
			public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
				//NOP
			}
			
			@Override
			public void onHeightChanged(int newHeight) {
				//NOP
			}
		});
	}

	protected SelectionConstellation getSelectionConstellation() {
		return selectionConstellation;
	}

	protected void configureGIMASelectionConstellation(GIMASelectionConstellation gimaSelectionConstellation) {
		this.gimaSelectionConstellation = gimaSelectionConstellation;
	}

	@Override
	public void intializeBean() throws Exception {
		setHeaderVisible(false);
		setBorders(false);
		setBodyBorder(false);

		add(selectionConstellation);
	}

	@Override
	public TextButton getMainButton() {
		if (assignButton != null)
			return assignButton;

		assignButton = new FixedTextButton(new FixedTextButtonCell(), LocalizedText.INSTANCE.assign());
		assignButton.setToolTip(LocalizedText.INSTANCE.addFinishDescription());
		assignButton.setScale(ButtonScale.LARGE);
		assignButton.setIconAlign(IconAlign.TOP);
		assignButton.setIcon(ConstellationResources.INSTANCE.finish());
		assignButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);
		assignButton.addSelectHandler(event -> {
			if (addUsed)
				gimaSelectionConstellation.performFinish(false);
			else
				gimaSelectionConstellation.performAddAndFinish(false);
		});
		assignButton.setEnabled(false);

		return assignButton;
	}

	@Override
	public List<ButtonConfiguration> getAdditionalButtons() {
		if (additionalButtons != null)
			return additionalButtons;

		additionalButtons = new ArrayList<>();
		additionalButtons.add(new ButtonConfiguration(getPropertyPanelToggleButton()));
		additionalButtons.add(new ButtonConfiguration(getAddButton(), true));

		return additionalButtons;
	}
	
	@Override
	public void handleCancel() {
		gimaSelectionConstellation.handleCancel(true, false);
	}
	
	/**
	 * Returns true if we are currently adding to a map key.
	 */
	public boolean isAddingToMapKey() {
		return gimaSelectionConstellation.isAddingToMapKey();
	}

	protected boolean isShowPropertyPanel() {
		return showPropertyPanel;
	}
	
	protected void disableUpdateButtons() {
		updateButtonsDisabled = true;
	}
	
	protected void enableUpdateButtons() {
		updateButtonsDisabled = false;
	}
	
	protected void setDetailPanelButtonVisibility(boolean hidden) {
		detailPanelButtonHidden = hidden;
		if (propertyPanelToggleButton != null)
			propertyPanelToggleButton.setVisible(!detailPanelButtonHidden);
	}

	private TextButton getPropertyPanelToggleButton() {
		if (propertyPanelToggleButton != null)
			return propertyPanelToggleButton;

		propertyPanelToggleButton = new FixedTextButton(LocalizedText.INSTANCE.showPropertyPanel());
		propertyPanelToggleButton.setScale(ButtonScale.LARGE);
		propertyPanelToggleButton.setIconAlign(IconAlign.TOP);
		propertyPanelToggleButton.setIcon(ConstellationResources.INSTANCE.showProperties());
		propertyPanelToggleButton.addSelectHandler(event -> handlePropertyPanelToggle(true));
		propertyPanelToggleButton.setVisible(!detailPanelButtonHidden);
		propertyPanelToggleButton.addStyleName(GIMADialog.GIMA_ADDITIONAL_BUTTON);

		return propertyPanelToggleButton;
	}
	
	protected TextButton getAddButton() {
		if (addButton != null)
			return addButton;

 		addButton = new FixedTextButton(LocalizedText.INSTANCE.add());
		addButton.setToolTip(LocalizedText.INSTANCE.addDescription());
		addButton.setScale(ButtonScale.LARGE);
		addButton.setIconAlign(IconAlign.TOP);
		addButton.setIcon(ConstellationResources.INSTANCE.add());
		addButton.addSelectHandler(event -> {
			gimaSelectionConstellation.performAdd(false);
			if (!addResizePerformed) {
				GIMADialog gimaDialog = gimaSelectionConstellation.getGimaDialog();
				gimaDialog.setHeight(gimaDialog.getOffsetHeight() + 100);
				addResizePerformed = true;
			}
		});
		addButton.setEnabled(false);
		addButton.addStyleName(GIMADialog.GIMA_ADDITIONAL_BUTTON);

 		return addButton;
	}
	
	protected void renameButtonsAfterAdd(boolean added) {
		if (updateButtonsDisabled || addUsed == added)
			return;
		
		addUsed = added;
		assignButton.setText(added ? LocalizedText.INSTANCE.finish() : LocalizedText.INSTANCE.addFinish());
		assignButton.setToolTip(added ? LocalizedText.INSTANCE.finishDescription() : LocalizedText.INSTANCE.addFinishDescription());
		assignButton.setMinWidth(added ? 70 : 120);
		
		if (added)
			assignButton.setEnabled(true);

		gimaSelectionConstellation.getGimaDialog().updateMainButton(this);
	}

	protected void handlePropertyPanelToggle(boolean performAction) {
		if (performAction)
			handleDetailPanelVisibility(!showPropertyPanel);
		else {
			propertyPanelToggleButton.setText(showPropertyPanel ? LocalizedText.INSTANCE.hidePropertyPanel() : LocalizedText.INSTANCE.showPropertyPanel());
			propertyPanelToggleButton.setIcon(showPropertyPanel ? ConstellationResources.INSTANCE.hideProperties() : ConstellationResources.INSTANCE.showProperties());
		}
	}
	
	protected void handleAddButtonVisibility(List<SpotlightData> selectedList) {
		if (selectedList.isEmpty()) {
			addButton.setEnabled(false);
			return;
		}

 		addButton.setEnabled(!selectedList.stream().filter(data -> data.getValue() == null).findAny().isPresent());
	}

 	protected void prepareButtonsVisibilityAndNames(int maxEntriesToSelect) {
 		addResizePerformed = false;
 		addUsed = false;
		addButton.setVisible(maxEntriesToSelect != 1);
		//typeFilterButton.setVisible(false);

 		if (maxEntriesToSelect == 1) {
			assignButton.setText(LocalizedText.INSTANCE.assign());
			assignButton.setMinWidth(70);
			assignButton.setToolTip(LocalizedText.INSTANCE.finishDescription());
		} else {
			assignButton.setText(LocalizedText.INSTANCE.addFinish());
			assignButton.setMinWidth(120);
			assignButton.setToolTip(LocalizedText.INSTANCE.addFinishDescription());
		}
	}
	
	protected void handleDetailPanelVisibility(boolean showPropertyPanel) {
		this.showPropertyPanel = showPropertyPanel;

		selectionConstellation.handleDetailPanelVisibility(showPropertyPanel);
 		propertyPanelToggleButton.setText(showPropertyPanel ? LocalizedText.INSTANCE.hidePropertyPanel() : LocalizedText.INSTANCE.showPropertyPanel());
 		propertyPanelToggleButton.setIcon(showPropertyPanel ? ConstellationResources.INSTANCE.hideProperties() : ConstellationResources.INSTANCE.showProperties());
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		//NOP
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return null;
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return null;
	}

	@Override
	public boolean isSelected(Object element) {
		return false;
	}

	@Override
	public void select(int index, boolean keepExisting) {
		//NOP
	}

	@Override
	public GmContentView getView() {
		return this;
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		//NOP
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return null;
	}

	@Override
	public void configureUseCase(String useCase) {
		//NOP
	}

	@Override
	public String getUseCase() {
		return null;
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		//NOP
	}

}