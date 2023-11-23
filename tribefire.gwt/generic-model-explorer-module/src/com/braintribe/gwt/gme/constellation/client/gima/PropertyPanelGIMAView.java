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

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gmview.action.client.ActionGroup;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.client.GmActionSupport;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmContentViewActionManager;
import com.braintribe.gwt.gmview.client.GmEntityView;
import com.braintribe.gwt.gmview.client.GmInteractionListener;
import com.braintribe.gwt.gmview.client.GmInteractionSupport;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButton;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButtonCell;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.validation.log.ValidationLogView;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.api.transaction.TransactionFrame;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;

/**
 * {@link PropertyPanel} implementation of the {@link GIMAView}.
 * 
 * @author michel.docouto
 *
 */
public class PropertyPanelGIMAView implements GIMAView, GmEntityView, GmInteractionSupport, GmActionSupport {

	private GmEntityView propertyPanel;
	private GIMADialog gimaDialog;
	private TextButton undoButton;
	private TextButton redoButton;
	private List<ButtonConfiguration> additionalButtons;
	private TextButton mainButton;
	private boolean isInstantiation;
	private boolean handlingAdd;

	public PropertyPanelGIMAView(GmEntityView propertyPanel, GIMADialog gimaDialog, boolean isInstantiation, boolean handlingAdd) {
		this.propertyPanel = propertyPanel;
		this.gimaDialog = gimaDialog;
		this.isInstantiation = isInstantiation;
		this.handlingAdd = handlingAdd;

		List<NestedTransaction> nestedTransactions = gimaDialog.getNestedTransactions();
		nestedTransactions.get(nestedTransactions.size() - 1)
				.addTransactionFrameListener(transactionFrame -> updateUndoAndRedoButtons(transactionFrame));
		
		if (propertyPanel instanceof ValidationLogView)
			((ValidationLogView) propertyPanel).setValidation(gimaDialog.getValidation(null));
	}
	
	@Override
	public TextButton getMainButton() {
		if (mainButton != null)
			return mainButton;

		mainButton = new TextButton(new FixedTextButtonCell(), isInstantiation ? (handlingAdd ? LocalizedText.INSTANCE.createAndAdd() : LocalizedText.INSTANCE.create())
				: LocalizedText.INSTANCE.apply());
		mainButton.setToolTip(LocalizedText.INSTANCE.applyDescription());
		mainButton.setScale(ButtonScale.LARGE);
		mainButton.addSelectHandler(event -> gimaDialog.performApply(false));
		mainButton.setIcon(ConstellationResources.INSTANCE.finish());
		if (handlingAdd)
			mainButton.setMinWidth(130);
		mainButton.setEnabled(true);
		mainButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);

		return mainButton;
	}

	@Override
	public List<ButtonConfiguration> getAdditionalButtons() {
		if (additionalButtons != null)
			return additionalButtons;

		additionalButtons = new ArrayList<>();
		additionalButtons.add(new ButtonConfiguration(getUndoButton()));
		additionalButtons.add(new ButtonConfiguration(getRedoButton()));

		return additionalButtons;
	}
	
	public void saveScrollState() {
		if (propertyPanel instanceof PropertyPanel)
			((PropertyPanel) propertyPanel).saveScrollState();
	}
	
	public void restoreScrollState() {
		if (propertyPanel instanceof PropertyPanel)
			((PropertyPanel) propertyPanel).restoreScrollState();
	}

	private TextButton getUndoButton() {
		if (undoButton != null)
			return undoButton;

		undoButton = new FixedTextButton(LocalizedText.INSTANCE.undo());
		undoButton.addSelectHandler(event -> gimaDialog.performUndo());
		undoButton.setToolTip(LocalizedText.INSTANCE.undoDescription());
		undoButton.setIcon(ConstellationResources.INSTANCE.undoBlack());
		undoButton.setIconAlign(IconAlign.TOP);
		undoButton.setScale(ButtonScale.LARGE);
		undoButton.setEnabled(false);
		undoButton.addStyleName(GIMADialog.GIMA_ADDITIONAL_BUTTON);

		return undoButton;
	}

	private TextButton getRedoButton() {
		if (redoButton != null)
			return redoButton;

		redoButton = new FixedTextButton(LocalizedText.INSTANCE.redo());
		redoButton.addSelectHandler(event -> gimaDialog.performRedo());
		redoButton.setToolTip(LocalizedText.INSTANCE.redoDescription());
		redoButton.setIcon(ConstellationResources.INSTANCE.redoBlack());
		redoButton.setIconAlign(IconAlign.TOP);
		redoButton.setScale(ButtonScale.LARGE);
		redoButton.setEnabled(false);
		redoButton.addStyleName(GIMADialog.GIMA_ADDITIONAL_BUTTON);

		return redoButton;
	}

	private void updateUndoAndRedoButtons(TransactionFrame transactionFrame) {
		if (undoButton != null)
			undoButton.setEnabled(transactionFrame.canUndo());

		if (redoButton != null)
			redoButton.setEnabled(transactionFrame.canRedo());
	}

	@Override
	public ModelPath getContentPath() {
		return propertyPanel.getContentPath();
	}

	@Override
	public void configureActionGroup(ActionGroup actionGroup) {
		if (propertyPanel instanceof GmActionSupport)
			((GmActionSupport) propertyPanel).configureActionGroup(actionGroup);
	}

	@Override
	public void configureExternalActions(List<Pair<ActionTypeAndName, ModelAction>> externalActions) {
		if (propertyPanel instanceof GmActionSupport)
			((GmActionSupport) propertyPanel).configureExternalActions(externalActions);
	}

	@Override
	public List<Pair<ActionTypeAndName, ModelAction>> getExternalActions() {
		if (propertyPanel instanceof GmActionSupport)
			return ((GmActionSupport) propertyPanel).getExternalActions();
		
		return null;
	}

	@Override
	public void setActionManager(GmContentViewActionManager actionManager) {
		if (propertyPanel instanceof GmActionSupport)
			((GmActionSupport) propertyPanel).setActionManager(actionManager);
	}

	@Override
	public void setContent(ModelPath modelPath) {
		propertyPanel.setContent(modelPath);
		getMainButton().setMinWidth(70);
		
		if (!isInstantiation) {
			getMainButton().setText(LocalizedText.INSTANCE.apply());
			return;
		}
		
		if (handlingAdd) {
			getMainButton().setText(LocalizedText.INSTANCE.createAndAdd());
			getMainButton().setMinWidth(130);
		} else if (modelPath.last() instanceof PropertyRelatedModelPathElement) {
			getMainButton().setText(LocalizedText.INSTANCE.createAndAssign());
			getMainButton().setMinWidth(140);
		} else
			getMainButton().setText(LocalizedText.INSTANCE.create());
	}

	@Override
	public void addSelectionListener(GmSelectionListener sl) {
		propertyPanel.addSelectionListener(sl);
	}

	@Override
	public void removeSelectionListener(GmSelectionListener sl) {
		propertyPanel.removeSelectionListener(sl);
	}

	@Override
	public ModelPath getFirstSelectedItem() {
		return propertyPanel.getFirstSelectedItem();
	}

	@Override
	public List<ModelPath> getCurrentSelection() {
		return propertyPanel.getCurrentSelection();
	}

	@Override
	public boolean isSelected(Object element) {
		return propertyPanel.isSelected(element);
	}

	@Override
	public void select(int index, boolean keepExisting) {
		propertyPanel.select(index, keepExisting);
	}

	@Override
	public GmContentView getView() {
		return propertyPanel.getView();
	}

	@Override
	public void addInteractionListener(GmInteractionListener il) {
		if (propertyPanel instanceof GmInteractionSupport)
			((GmInteractionSupport) propertyPanel).addInteractionListener(il);
	}

	@Override
	public void removeInteractionListener(GmInteractionListener il) {
		if (propertyPanel instanceof GmInteractionSupport)
			((GmInteractionSupport) propertyPanel).removeInteractionListener(il);
	}

	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		propertyPanel.configureGmSession(gmSession);
	}

	@Override
	public PersistenceGmSession getGmSession() {
		return propertyPanel.getGmSession();
	}

	@Override
	public void configureUseCase(String useCase) {
		propertyPanel.configureUseCase(useCase);
	}

	@Override
	public String getUseCase() {
		return propertyPanel.getUseCase();
	}
	
	public void startEditing() {
		if (propertyPanel instanceof PropertyPanel)
			((PropertyPanel) propertyPanel).startEditing();
	}

	public PropertyPanel getPropertyPanel() {
		if (propertyPanel instanceof PropertyPanel)
			return (PropertyPanel) propertyPanel;
		
		return null;
	}
	
	public GmEntityView getGmEntityView() {
		return propertyPanel;
	}
}