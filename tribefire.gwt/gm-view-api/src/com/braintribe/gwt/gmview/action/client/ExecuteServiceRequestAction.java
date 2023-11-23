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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.google.gwt.user.client.ui.Widget;

/**
 * Action used for executing (evaluating) a {@link ServiceRequest} for the session.
 * @author michel.docouto
 *
 */
public class ExecuteServiceRequestAction extends ModelAction {
	
	private ServiceRequest serviceRequest;
	private PersistenceGmSession dataSession;
	private TransientPersistenceGmSession transientSession;
	private Supplier<? extends TransientPersistenceGmSession> transientSessionProvider;
	private Supplier<? extends NotificationFactory> notificationFactorySupplier;
	
	public ExecuteServiceRequestAction() {
		setName(LocalizedText.INSTANCE.execute());
		setIcon(GmViewActionResources.INSTANCE.defaultActionIconSmall());
		setHoverIcon(GmViewActionResources.INSTANCE.defaultActionIconLarge());
		setHidden(true);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	/**
	 * Configures the required data session used for the {@link ServiceRequest} evaluation.
	 */
	@Required
	public void setDataSession(PersistenceGmSession dataSession) {
		this.dataSession = dataSession;
	}
	
	/**
	 * Configures the required transient session.
	 */
	@Required
	public void setTransientSession(TransientPersistenceGmSession transientSession) {
		this.transientSession = transientSession;
	}
	
	/**
	 * Configures the required provider for transient sessions used for the service execution.
	 */
	@Required
	public void setTransientSessionProvider(Supplier<? extends TransientPersistenceGmSession> transientSessionProvider) {
		this.transientSessionProvider = transientSessionProvider;
	}
	
	/**
	 * Configures the {@link NotificationFactory} used for broadcasting a notification.
	 */
	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		ModelPathNavigationListener listener = getModelPathNavigationListener(gmContentView);
		
		RequestExecutionData requestExecutionData = new RequestExecutionData(serviceRequest, dataSession, transientSession, listener,
				transientSessionProvider, notificationFactorySupplier);
		
		DdsaRequestExecution.executeRequest(requestExecutionData);
	}
	
	@Override
	protected void updateVisibility() {
		serviceRequest = null;
		
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true, true);
			return;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath modelPath : selection) {
			if (modelPath != null && !modelPath.isEmpty() && modelPath.last().getValue() instanceof ServiceRequest) {
				serviceRequest = modelPath.last().getValue();
				setHidden(false);
				return;
			}
		}
		
		setHidden(true, true);
	}
	
	private ModelPathNavigationListener getModelPathNavigationListener(Object view) {
		if (view instanceof ModelPathNavigationListener)
			return (ModelPathNavigationListener) view;
		else if (view instanceof Widget)
			return getModelPathNavigationListener(((Widget) view).getParent());
		
		return null;
	}

}
