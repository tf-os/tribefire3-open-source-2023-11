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

import com.braintribe.common.lcd.Pair;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.GIMADialog;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.action.client.EntitiesProviderResult;
import com.braintribe.gwt.gmview.action.client.ObjectAndType;
import com.braintribe.gwt.gmview.action.client.SpotlightPanel;
import com.braintribe.gwt.gmview.action.client.SpotlightPanelListenerAdapter;
import com.braintribe.gwt.gmview.client.GmContentView;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.Scheduler;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.MarginData;

/**
 * GIMA Panel used for selecting the one {@link WorkbenchAction} among a list of possible choices.
 * 
 * @author michel.docouto
 *
 */
public class GIMAActionSelectionView extends BorderLayoutContainer implements Function<List<? extends WorkbenchAction>, Future<? extends WorkbenchAction>>, GIMAView {
	
	private SpotlightPanel spotlightPanel;
	private TextButton selectButton;
	//private String title;
	private List<? extends WorkbenchAction> workbenchActions;
	private Future<WorkbenchAction> workbenchActionFuture;
	
	public GIMAActionSelectionView() {
		setBorders(false);
	}
	
	/**
	 * Configures the required {@link SpotlightPanel}.
	 */
	@Required
	public void setSpotlightPanel(SpotlightPanel spotlightPanel) {
		this.spotlightPanel = spotlightPanel;
		spotlightPanel.setUseApplyButton(false);
		spotlightPanel.configureEmptyText(LocalizedText.INSTANCE.typeForFilteringActions());
		spotlightPanel.setEnableGroups(false);
		spotlightPanel.setMinCharsForFilter(0);
		spotlightPanel.setEntitiesFutureProvider(parserArgument -> {
			List<Pair<GenericEntity, String>> entityAndDisplayList = new ArrayList<>();
			
			for (WorkbenchAction action : workbenchActions) {
				String filter = parserArgument.getValue();
				String displayName = I18nTools.getLocalized(action.getDisplayName());
				if (filter == null || filter.isEmpty() || displayName.toLowerCase().contains(filter.toLowerCase()))
					entityAndDisplayList.add(new Pair<>(action, displayName));
			}
			
			EntitiesProviderResult result = new EntitiesProviderResult(parserArgument.getOffset(), false, entityAndDisplayList);
			Future<EntitiesProviderResult> future = new Future<>();
			Scheduler.get().scheduleDeferred(() -> future.onSuccess(result));
			return future;
			
		});
		spotlightPanel.setLoadTypes(false);
		spotlightPanel.setIgnoreMetadata(true);
		spotlightPanel.configureTextFieldMaxWidth(400);
		setCenterWidget(spotlightPanel, new MarginData(6, 6, 0, 6));

		spotlightPanel.addSpotlightPanelListener(new SpotlightPanelListenerAdapter() {
			@Override
			public void onValueOrTypeSelected(ObjectAndType objectAndType) {
				handleObjectAndType(objectAndType);
			}
		});

		spotlightPanel.getGrid().getSelectionModel().addSelectionChangedHandler(event -> {
			if (event.getSelection() != null && !event.getSelection().isEmpty()) {
				selectButton.setEnabled(true);
				return;
			}

			selectButton.setEnabled(false);
		});
	}
	
	@Override
	public Future<? extends WorkbenchAction> apply(List<? extends WorkbenchAction> workbenchActions) throws RuntimeException {
		this.workbenchActions = workbenchActions;
		workbenchActionFuture = new Future<>();
		spotlightPanel.configureTypeCondition(spotlightPanel.prepareTypeCondition(WorkbenchAction.T));
		return workbenchActionFuture;
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
		selectButton.addSelectHandler(event -> handleObjectAndType(spotlightPanel.prepareObjectAndType()));
		selectButton.setScale(ButtonScale.LARGE);
		selectButton.setIconAlign(IconAlign.TOP);
		selectButton.setIcon(ConstellationResources.INSTANCE.finish());
		selectButton.setEnabled(false);
		selectButton.addStyleName(GIMADialog.GIMA_MAIN_BUTTON);

		return selectButton;
	}
	
	@Override
	public List<ButtonConfiguration> getAdditionalButtons() {
		return null;
	}

	/*@Override
	public String getTitle() {
		return title;
	}*/
	
	@Override
	public void configureGmSession(PersistenceGmSession gmSession) {
		spotlightPanel.setGmSession(gmSession);
	}
	
	private void handleObjectAndType(ObjectAndType result) {
		if (result.getObject() != null)
			returnResult(result);
	}
	
	private void returnResult(ObjectAndType objectAndType) {
		WorkbenchAction result = null;
		if (objectAndType != null)
			result = objectAndType.getObject() instanceof WorkbenchAction ? (WorkbenchAction) objectAndType.getObject() : null;
		
		workbenchActionFuture.onSuccess(result);
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
