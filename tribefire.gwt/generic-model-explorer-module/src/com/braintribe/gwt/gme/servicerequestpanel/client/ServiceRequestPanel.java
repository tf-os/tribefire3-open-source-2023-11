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
package com.braintribe.gwt.gme.servicerequestpanel.client;

import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.servicerequestpanel.client.resources.LocalizedText;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.ioc.client.DisposableBean;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.validation.Validation;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ServiceRequestPanel extends BorderLayoutContainer implements InitializableBean, DisposableBean {
	
	private PropertyPanel propertyPanel;
	private ServiceRequest serviceRequest;
	private PersistenceGmSession dataSession;
	private TransientPersistenceGmSession transientSession;
	private Supplier<? extends TransientPersistenceGmSession> transientSessionProvider;
	private Supplier<? extends NotificationFactory> notificationFactorySupplier;
	private String useCase;
	private Validation validation;
	private ExplorerConstellation explorerConstellation;
	private VerticalTabElement verticalTabElementToRemove;
	
	@Required
	public void setPropertyPanel(final PropertyPanel propertyPanel) {
		this.propertyPanel = propertyPanel;
	}
	
	@Required
	public void setDataSession(PersistenceGmSession dataSession) {
		this.dataSession = dataSession;
	}
	
	@Required
	public void setTransientSession(TransientPersistenceGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	@Required
	public void setTransientSessionProvider(Supplier<? extends TransientPersistenceGmSession> transientSessionProvider) {
		this.transientSessionProvider = transientSessionProvider;
	}
	
	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	@Required
	public void setUseCase(String useCase) {
		this.useCase = useCase;
	}
	
	/**
	 * Configures the expert used for validation purposes.
	 */
	@Required
	public void setValidation(Validation validation) {
		this.validation = validation;
	}
	
	@Override
	public void intializeBean() throws Exception {
		final ToolBar toolBar = new ToolBar();
		toolBar.setBorders(false);
		toolBar.add(getEvaluateButton());
		setEastWidget(toolBar, new BorderLayoutData(500));

		addDomHandler(event -> {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
				evaluateServiceRequest();
		}, KeyDownEvent.getType());

		//propertyPanel.configureGmSession(transientSession);
		setCenterWidget(propertyPanel);
	}
	
	protected void configureExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	public void configureRequestData(final ServiceRequest serviceRequest) {
		this.serviceRequest = serviceRequest;
		
		ModelPath modelPath = new ModelPath();
		modelPath.add(new RootPathElement(serviceRequest));
		propertyPanel.configureGmSession((PersistenceGmSession) serviceRequest.session());
		propertyPanel.configureUseCase(useCase);
		propertyPanel.setContent(modelPath);
	}
	
	public PropertyPanel getPropertyPanel() {
		return propertyPanel;
	}
	
	protected void evaluateServiceRequest() {
		validation.validateManipulations() //
				.andThen(result -> {
					if (result.isEmpty()) {
						executeServiceRequest();
						return;
					}

					Action showValidationLogAction = new Action() {
						@Override
						public void perform(TriggerInfo triggerInfo) {
							explorerConstellation.prepareValidationLog(result, transientSession).onError(Throwable::printStackTrace)
									.andThen(result -> verticalTabElementToRemove = result);
						}
					};
					showValidationLogAction.setName(LocalizedText.INSTANCE.details());
					GlobalState.showWarning(LocalizedText.INSTANCE.validationError(), showValidationLogAction);
					showValidationLogAction.perform(null);
				}).onError(e -> {
					ErrorDialog.show("Error while validating manipulations.", e);
					e.printStackTrace();
				});
	}
	
	private void executeServiceRequest() {
		ModelPathNavigationListener listener = getModelPathNavigationListener(ServiceRequestPanel.this.getParent());
		
		RequestExecutionData requestExecutionData = new RequestExecutionData(serviceRequest, dataSession,
				transientSession, listener, transientSessionProvider, notificationFactorySupplier);
		
		DdsaRequestExecution.executeRequest(requestExecutionData) //
				.andThen(result -> {
					if (result == null) {
						listener.onOpenModelPath(null);
						return;
					}

					if (!(result instanceof Boolean)) {
						ModelPath modelPath = new ModelPath();
						RootPathElement pathElement = new RootPathElement(GMF.getTypeReflection().getType(result), result);
						modelPath.add(pathElement);
						listener.onOpenModelPath(modelPath);
					}
				});
		
		if (verticalTabElementToRemove != null) {
			explorerConstellation.removeVerticalTabElement(verticalTabElementToRemove);
			verticalTabElementToRemove = null;
		}
	}
	
	private TextButton getEvaluateButton() {
		TextButton evaluateButton = new TextButton(LocalizedText.INSTANCE.execute());
		evaluateButton.setIcon(GmViewActionResources.INSTANCE.okBig());
		evaluateButton.setWidth(100);
		evaluateButton.setIconAlign(IconAlign.TOP);
		evaluateButton.setScale(ButtonScale.LARGE);
		evaluateButton.setToolTip(LocalizedText.INSTANCE.execute());
		evaluateButton.getElement().getStyle().setPaddingLeft(50, Unit.PX);
		evaluateButton.addSelectHandler(event -> evaluateServiceRequest());

		return evaluateButton;
	}
	
	private ModelPathNavigationListener getModelPathNavigationListener(Widget widget) {
		if (widget instanceof ModelPathNavigationListener)
			return (ModelPathNavigationListener) widget;
		
		if (widget.getParent() != null)
			return getModelPathNavigationListener(widget.getParent());
		
		return null;
	}
	
	@Override
	public void disposeBean() throws Exception {
		propertyPanel.disposeBean();
	}

}
