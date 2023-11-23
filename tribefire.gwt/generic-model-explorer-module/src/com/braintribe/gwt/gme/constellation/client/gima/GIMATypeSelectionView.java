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
import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.GmTypeOrAction;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.ObjectAssignmentActionDialog;
import com.braintribe.gwt.gmview.action.client.ObjectAssignmentConfig;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.SpotlightPanelListener;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.FixedTextButton;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.workbench.WorkbenchAction;
import com.google.gwt.core.client.Scheduler;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.form.TextField;

/**
 * GIMA Panel used for selecting the type for a list of possible types. The code was copied from
 * {@link ObjectAssignmentActionDialog}.
 * 
 * @author michel.docouto
 *
 */
public class GIMATypeSelectionView extends BorderLayoutContainer implements Function<ObjectAssignmentConfig, Future<GmTypeOrAction>>, GIMAView {

	private TextButton backButton;
	private TextButton filterButton;
	private List<GmType> previousTypes;
	private List<TypeCondition> previousTypeConditions;
	private List<ButtonConfiguration> additionalButtons;
	private SpotlightPanel spotlightPanel;
	private GmType type;
	private TypeCondition typeCondition;
	private TextButton selectButton;
	private Future<GmTypeOrAction> typeOrActionFuture;
	private String title;

	public GIMATypeSelectionView() {
		setBorders(false);
	}

	/**
	 * Configures the required {@link SpotlightPanel}.
	 */
	@Required
	public void setSpotlightPanel(SpotlightPanel spotlightPanel) {
		this.spotlightPanel = spotlightPanel;
		spotlightPanel.setUseApplyButton(false);
		spotlightPanel.setLoadExistingValues(false);
		spotlightPanel.setCheckEntityInstantiationDisabledMetaData(true);
		spotlightPanel.configureTextFieldMaxWidth(400);
		setCenterWidget(spotlightPanel, new MarginData(6, 6, 0, 6));

		spotlightPanel.addSpotlightPanelListener(new SpotlightPanelListener() {
			@Override
			public void onValueOrTypeSelected(ObjectAndType objectAndType) {
				handleObjectAndType(objectAndType);
			}

			@Override
			public void onTypeSelected(GmType modelType) {
				resetTypeCondition(modelType, spotlightPanel.prepareTypeCondition(modelType), null);
			}

			@Override
			public void onNotEnoughCharsTyped() {
				resetTypeCondition(type, typeCondition, null);
			}

			@Override
			public void onCancel() {
				// Nothing to do
			}
		});

		spotlightPanel.getGrid().getSelectionModel().addSelectionChangedHandler(event -> {
			if (event.getSelection() != null && !event.getSelection().isEmpty()) {
				GmType type = spotlightPanel.prepareObjectAndType().getType();
				if (type instanceof GmEntityType) {
					GmEntityType entityType = (GmEntityType) type;
					selectButton.setEnabled(!entityType.getIsAbstract());
					filterButton.setEnabled(isValidFilterType(entityType));
					return;
				} else {
					selectButton.setEnabled(true);
					filterButton.setEnabled(false);
					return;
				}
			}

			selectButton.setEnabled(false);
			filterButton.setEnabled(false);
		});
	}

	@Override
	public Future<GmTypeOrAction> apply(ObjectAssignmentConfig config) {
		typeOrActionFuture = new Future<>();
		//future = null;
		title = config.getTitle();
		GmType type = config.getGmType();

		resetTypeCondition(type, spotlightPanel.prepareTypeCondition(type), typeOrActionFuture);

		return typeOrActionFuture;
	}

	@Override
	public List<ButtonConfiguration> getAdditionalButtons() {
		if (additionalButtons != null)
			return additionalButtons;

		additionalButtons = new ArrayList<>();
		additionalButtons.add(new ButtonConfiguration(getBackButton()));
		additionalButtons.add(new ButtonConfiguration(getFilterButton()));

		return additionalButtons;
	}
	
	@Override
	public void handleCancel() {
		typeOrActionFuture.onSuccess(null);
	}
	
	@Override
	public boolean isApplyAllHandler() {
		return false;
	}

	@Override
	public TextButton getMainButton() {
		if (selectButton != null)
			return selectButton;

		selectButton = new TextButton(LocalizedText.INSTANCE.select());
		selectButton.setToolTip(LocalizedText.INSTANCE.selectTypeDescription());
		selectButton.addSelectHandler(event -> handleObjectAndType(spotlightPanel.prepareObjectAndType()));
		selectButton.setScale(ButtonScale.LARGE);
		selectButton.setIconAlign(IconAlign.TOP);
		selectButton.setIcon(ConstellationResources.INSTANCE.finish());
		selectButton.setEnabled(false);
		selectButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);

		return selectButton;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		spotlightPanel.setGmSession(gmSession);
	}

	private TextButton getBackButton() {
		if (backButton != null)
			return backButton;

		backButton = new FixedTextButton(LocalizedText.INSTANCE.back());
		backButton.setToolTip(LocalizedText.INSTANCE.backTypeDescription());
		backButton.addSelectHandler(event -> resetTypeCondition(previousTypes.remove(previousTypes.size() - 1),
				previousTypeConditions.remove(previousTypeConditions.size() - 1), null));
		backButton.setScale(ButtonScale.LARGE);
		backButton.setIcon(ConstellationResources.INSTANCE.back());
		backButton.setIconAlign(IconAlign.TOP);
		backButton.setEnabled(false);
		backButton.addStyleName(GIMADialog.GIMA_ADDITIONAL_BUTTON);

		return backButton;
	}

	private TextButton getFilterButton() {
		if (filterButton != null)
			return filterButton;

		filterButton = new TextButton(LocalizedText.INSTANCE.filterType());
		filterButton.setToolTip(LocalizedText.INSTANCE.filterTypeDescription());
		filterButton.addSelectHandler(event -> handleObjectAndType(spotlightPanel.prepareObjectAndType(), true));
		filterButton.setScale(ButtonScale.LARGE);
		filterButton.setIcon(ConstellationResources.INSTANCE.front());
		filterButton.setIconAlign(IconAlign.TOP);
		filterButton.setEnabled(false);
		filterButton.addStyleName(GIMADialog.GIMA_ADDITIONAL_BUTTON);

		return filterButton;
	}

	private void resetTypeCondition(GmType type, TypeCondition typeCondition, final Future<GmTypeOrAction> future) {
		this.type = type;
		this.typeCondition = typeCondition;
		if (future != null)
			GlobalState.showSuccess(LocalizedText.INSTANCE.loadingActions());
		spotlightPanel.configureUseTypeOnly(true/*this.future == null*/);
		spotlightPanel.configureTypeCondition(typeCondition) //
				.andThen((Void) -> handleQuickAccessLoadingFinished(future)) //
				.onError(e -> handleQuickAccessLoadingFinished(future));
		backButton.setEnabled(previousTypeConditions != null && !previousTypeConditions.isEmpty());
	}

	private void handleObjectAndType(ObjectAndType result) {
		handleObjectAndType(result, false);
	}

	private void handleObjectAndType(ObjectAndType result, boolean filter) {
		if (result.getObject() != null)
			returnResult(result);
		else if (result.getType() instanceof GmEnumType) {
			if (previousTypes == null)
				previousTypes = new ArrayList<GmType>();
			if (previousTypeConditions == null)
				previousTypeConditions = new ArrayList<TypeCondition>();
			previousTypes.add(type);
			previousTypeConditions.add(typeCondition);

			resetTypeCondition(result.getType(), spotlightPanel.prepareTypeCondition(result.getType()), null);
		} else {
			GmEntityType entityType = (GmEntityType) result.getType();
			if (!filter && Boolean.FALSE.equals(entityType.getIsAbstract()))
				returnResult(result);
			else if (isValidFilterType(entityType)) {
				if (previousTypes == null)
					previousTypes = new ArrayList<>();
				if (previousTypeConditions == null)
					previousTypeConditions = new ArrayList<>();
				previousTypes.add(type);
				previousTypeConditions.add(typeCondition);

				resetTypeCondition(result.getType(), spotlightPanel.prepareTypeCondition(result.getType()), null);
			}
		}
	}

	private void handleQuickAccessLoadingFinished(Future<GmTypeOrAction> future) {
		if (future == null)
			return;

		GlobalState.clearState();
		boolean isAbstractEntity = type instanceof GmEntityType && ((GmEntityType) type).getIsAbstract();
		if (spotlightPanel.getGrid().getStore().size() == 1 && !isAbstractEntity)
			future.onSuccess(spotlightPanel.prepareModelTypeOrAction());
		else {
			show();
			// resetTypeCondition(SpotlightPanel.prepareTypeCondition(type));
			Scheduler.get().scheduleDeferred(() -> {
				spotlightPanel.getGrid().getView().refresh(true);
				if (spotlightPanel.getTextField() instanceof TextField)
					((TextField) spotlightPanel.getTextField()).focus();
			});
		}
	}

	private boolean isValidFilterType(GmEntityType entityType) {
		if (type == entityType || (previousTypes != null && previousTypes.contains(entityType)))
			return false;

		return true;
	}

	private void returnResult(ObjectAndType objectAndType) {
		GmTypeOrAction result = null;
		if (objectAndType != null) {
			result = new GmTypeOrAction(objectAndType.getType(),
					objectAndType.getObject() instanceof WorkbenchAction ? (WorkbenchAction) objectAndType.getObject() : null);
		}

		typeOrActionFuture.onSuccess(result);
	}

	@Override
	public ModelPath getContentPath() {
		return null;
	}

	@Override
	public void setContent(ModelPath modelPath) {
		//NOP
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

}