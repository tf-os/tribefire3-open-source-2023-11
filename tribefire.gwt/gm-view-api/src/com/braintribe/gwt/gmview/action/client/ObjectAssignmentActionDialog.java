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
package com.braintribe.gwt.gmview.action.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ClosableWindow;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;
import com.braintribe.model.workbench.WorkbenchAction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ObjectAssignmentActionDialog extends ClosableWindow implements Function<ObjectAssignmentConfig, Future<GmTypeOrAction>> {
	
	private Future<ObjectAndType> future;
	private Future<Boolean> instantiationCheckFuture;
	private SpotlightPanel spotlightPanel;
	private TextButton backButton;
	private TextButton okButton;
	private TextButton filterButton;
	private List<GmType> previousTypes;
	private List<TypeCondition> previousTypeConditions;
	private GmType type;
	private TypeCondition typeCondition;
	private ModelEnvironmentDrivenGmSession gmSession;
	private Future<GmTypeOrAction> typeOrActionFuture;
	private BorderLayoutContainer borderLayoutContainer;
	
	public ObjectAssignmentActionDialog() {
		setSize("600px", "700px");
		setClosable(false);
		setModal(true);
		setMaximizable(false);
		setMinWidth(560);
		setMinHeight(200);
		setBodyBorder(false);
		setBorders(false);
		getHeader().setHeight(20);
		
		ToolBar toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.add(new FillToolItem());
		toolBar.add(getBackButton());
		toolBar.add(getFilterButton());
		toolBar.add(getCancelButton());
		toolBar.add(getOkButton());
		
		borderLayoutContainer = new BorderLayoutContainer();
		borderLayoutContainer.setBorders(false);
		borderLayoutContainer.setSouthWidget(toolBar, new BorderLayoutData(25));
		
		add(borderLayoutContainer);
	}
	
	/**
	 * Configures the required {@link SpotlightPanel}.
	 */
	@Required
	public void setSpotlightPanel(final SpotlightPanel spotlightPanel) {
		this.spotlightPanel = spotlightPanel;
		spotlightPanel.setUseApplyButton(false);
		spotlightPanel.setLoadExistingValues(false);
		spotlightPanel.setCheckEntityInstantiationDisabledMetaData(true);
		borderLayoutContainer.setCenterWidget(spotlightPanel);
		
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
				//Nothing to do
			}
		});
		
		spotlightPanel.getGrid().getSelectionModel().addSelectionChangedHandler(event -> {
			if (event.getSelection() != null && !event.getSelection().isEmpty()) {
				GmType type = ObjectAssignmentActionDialog.this.spotlightPanel.prepareObjectAndType().getType();
				if (type instanceof GmEntityType) {
					GmEntityType entityType = (GmEntityType) type;
					okButton.setEnabled(!entityType.getIsAbstract());
					filterButton.setEnabled(isValidFilterType(entityType));
					return;
				} else {
					okButton.setEnabled(true);
					filterButton.setEnabled(false);
					return;
				}
			}
			
			okButton.setEnabled(false);
			filterButton.setEnabled(false);
		});
	}
	
	/**
	 * Configures the {@link ModelEnvironmentDrivenGmSession}.
	 */
	@Configurable
	public void setGmSession(ModelEnvironmentDrivenGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	@Override
	public Future<GmTypeOrAction> apply(ObjectAssignmentConfig config) throws RuntimeException {
		typeOrActionFuture = new Future<>();
		future = null;
		GmType type = config.getGmType();
		resetTypeCondition(type, spotlightPanel.prepareTypeCondition(type), typeOrActionFuture);
		setHeading(config.getTitle() == null ? "" : config.getTitle());
		
		return typeOrActionFuture;
	}
	
	/**
	 * Returns a Future containing true if the type can be instantiated (either via type or via some instantiation action).
	 */
	public Future<Boolean> hasInstantiationPossibility(GmType type) {
		instantiationCheckFuture = new Future<>();
		future = null;
		this.type = type;
		this.typeCondition = spotlightPanel.prepareTypeCondition(type);
		spotlightPanel.configureUseTypeOnly(true);
		spotlightPanel.configureTypeCondition(typeCondition) //
				.andThen(result -> handleTypeConfiguration()) //
				.onError(e -> handleTypeConfiguration());
		return instantiationCheckFuture;
	}
	
	private void handleTypeConfiguration() {
		boolean isAbstractEntity = type instanceof GmEntityType && ((GmEntityType) type).getIsAbstract();
		instantiationCheckFuture.onSuccess(spotlightPanel.getGrid().getStore().size() > 0 && !isAbstractEntity);
	}
	
	/**
	 * Displays the dialog, and gets the object and type.
	 */
	public Future<ObjectAndType> getObjectAndType(GmType gmType, TypeCondition typeCondition) {
		future = new Future<>();
		
		show();
		resetTypeCondition(gmType, typeCondition, null);
		
		return future;
	}
	
	@Override
	protected void onKeyPress(Event event) {
		if (isOnEsc() && event.getKeyCode() == KeyCodes.KEY_ESCAPE) {
			hide();
			return;
		}
		
		super.onKeyPress(event);
	}
	
	@Override
	public void show() {
		super.show();
		
		int currentHeight = getOffsetHeight();
		int computedHeight = Math.min(Document.get().getClientHeight() - 30, currentHeight);
		if (computedHeight != currentHeight)
			setHeight(computedHeight);
	}
	
	@Override
	public void hide() {
		if (future != null)
			future.onSuccess(null);
		else
			typeOrActionFuture.onSuccess(null);
		
		super.hide();
	}
	
	private void resetTypeCondition(GmType type, TypeCondition typeCondition, Future<GmTypeOrAction> future) {
		this.type = type;
		this.typeCondition = typeCondition;
		if (future != null)
			GlobalState.showSuccess(LocalizedText.INSTANCE.loadingActions());
		spotlightPanel.configureUseTypeOnly(this.future == null);
		spotlightPanel.configureTypeCondition(typeCondition) //
				.andThen(result -> handleQuickAccessLoadingFinished(future)) //
				.onError(e -> handleQuickAccessLoadingFinished(future));
		backButton.setVisible(previousTypeConditions != null && !previousTypeConditions.isEmpty());
	}
	
	private void handleQuickAccessLoadingFinished(Future<GmTypeOrAction> future) {
		if (future == null)
			return;
		
		GlobalState.clearState();
		boolean isAbstractEntity = type instanceof GmEntityType && ((GmEntityType) type).getIsAbstract();
		if (spotlightPanel.getGrid().getStore().size() == 1 && !isAbstractEntity) {
			future.onSuccess(spotlightPanel.prepareModelTypeOrAction());
			return;
		}
		
		show();
		//resetTypeCondition(SpotlightPanel.prepareTypeCondition(type));
		Scheduler.get().scheduleDeferred(() -> {
			spotlightPanel.getGrid().getView().refresh(true);
			if (spotlightPanel.getTextField() instanceof TextField)
				((TextField) spotlightPanel.getTextField()).focus();
		});
	}
	
	private void hide(ObjectAndType objectAndType) {
		if (future != null)
			future.onSuccess(objectAndType);
		else {
			GmTypeOrAction result = null;
			if (objectAndType != null) {
				result = new GmTypeOrAction(objectAndType.getType(),
						objectAndType.getObject() instanceof WorkbenchAction ? (WorkbenchAction) objectAndType.getObject() : null);
			}
			
			typeOrActionFuture.onSuccess(result);
		}
		
		super.hide();
	}
	
	
	
	private TextButton getBackButton() {
		backButton = new TextButton();
		backButton.setText(LocalizedText.INSTANCE.back());
		backButton.setToolTip(LocalizedText.INSTANCE.backDescription());
		backButton.setIcon(GmViewActionResources.INSTANCE.back());
		backButton.addSelectHandler(event -> resetTypeCondition(previousTypes.remove(previousTypes.size() - 1),
				previousTypeConditions.remove(previousTypeConditions.size() - 1), null));
		backButton.setVisible(false);
		
		return backButton;
	}
	
	private TextButton getCancelButton() {
		TextButton cancelButton = new TextButton();
		cancelButton.setText(LocalizedText.INSTANCE.cancel());
		cancelButton.setIcon(GmViewActionResources.INSTANCE.cancel());
		cancelButton.addSelectHandler(event -> hide((ObjectAndType) null));
		
		return cancelButton;
	}
	
	private TextButton getOkButton() {
		okButton = new TextButton();
		okButton.setText(LocalizedText.INSTANCE.select());
		okButton.setToolTip(LocalizedText.INSTANCE.okDescription());
		okButton.setIcon(GmViewActionResources.INSTANCE.ok());
		okButton.addSelectHandler(event -> handleObjectAndType(spotlightPanel.prepareObjectAndType()));
		okButton.setEnabled(false);
		
		return okButton;
	}
	
	private TextButton getFilterButton() {
		filterButton = new TextButton();
		filterButton.setText(LocalizedText.INSTANCE.filter());
		filterButton.setToolTip(LocalizedText.INSTANCE.filterDescription());
		filterButton.setIcon(GmViewActionResources.INSTANCE.front());
		filterButton.addSelectHandler(event -> handleObjectAndType(spotlightPanel.prepareObjectAndType(), true));
		filterButton.setEnabled(false);
		
		return filterButton;
	}
	
	private void handleObjectAndType(ObjectAndType result) {
		handleObjectAndType(result, false);
	}
	
	private void handleObjectAndType(ObjectAndType result, boolean filter) {
		if (result.getObject() != null)
			hide(result);
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
			if (!filter && Boolean.FALSE.equals(entityType.getIsAbstract())) {
				if (future != null)
					result.setObject(gmSession.create(GMF.getTypeReflection().getEntityType(entityType.getTypeSignature())));
				else
					hide(result);
			} else if (isValidFilterType(entityType)) {
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
	
	private boolean isValidFilterType(GmEntityType entityType) {
		if (type == entityType || (previousTypes != null && previousTypes.contains(entityType)))
			return false;
		
		return true;
	}
	
}
