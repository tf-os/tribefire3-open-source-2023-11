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
package com.braintribe.gwt.gme.templateevaluation.client.expert;

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.servicerequestpanel.client.ServiceRequestConstellation;
import com.braintribe.gwt.gme.templateevaluation.client.LocalizedText;
import com.braintribe.gwt.gmview.client.ModelPathNavigationListener;
import com.braintribe.gwt.gmview.ddsarequest.client.ConfirmationFailedException;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution;
import com.braintribe.gwt.gmview.ddsarequest.client.DdsaRequestExecution.RequestExecutionData;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.TransientPersistenceGmSession;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationContext;
import com.braintribe.model.processing.template.evaluation.TemplateEvaluationException;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionContext;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.TemplateServiceRequestAction;

/**
 * Handler for handling {@link TemplateServiceRequestAction} within the workbench.
 * @author michel.docouto
 *
 */
public class TemplateServiceRequestActionHandler extends TemplateBasedActionHandler<TemplateServiceRequestAction> {
	
	private PersistenceGmSession gmSession;
	private Supplier<? extends TransientPersistenceGmSession> currentTransientSessionProvider;
	private Supplier<? extends TransientPersistenceGmSession> transientSessionProvider;
	private Supplier<? extends NotificationFactory> notificationFactorySupplier;
	private ModelPathNavigationListener listener;
	private ExplorerConstellation explorerConstellation;
	
	/**
	 * Configures the required data session.
	 */
	@Required
	public void setGmSession(PersistenceGmSession gmSession) {
		this.gmSession = gmSession;
	}
	
	/**
	 * Configures the required {@link Supplier} for {@link TransientPersistenceGmSession} used for the service execution.
	 */
	@Required
	public void setTransientSessionProvider(Supplier<? extends TransientPersistenceGmSession> transientSessionProvider) {
		this.transientSessionProvider = transientSessionProvider;
	}
	
	/**
	 * Configures the required {@link TransientPersistenceGmSession}.
	 */
	@Required
	public void setCurrentTransientSessionProvider(Supplier<? extends TransientPersistenceGmSession> currentTransientSessionProvider) {
		this.currentTransientSessionProvider = currentTransientSessionProvider;
	}
	
	/**
	 * Configures the {@link NotificationFactory} used for broadcasting a notification.
	 */
	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}
	
	/**
	 * Configures the required {@link ExplorerConstellation}.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
	}
	
	/**
	 * Configures the optional {@link ModelPathNavigationListener}.
	 */
	@Configurable
	public void setListener(ModelPathNavigationListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Overriding for using the {@link ServiceRequestConstellation} for handling templates, instead of GIMA.
	 */
	@Override
	protected void handleEvaluateTemplate(WorkbenchActionContext<TemplateServiceRequestAction> workbenchActionContext,
			TemplateEvaluationContext templateEvaluationContext) {
		if (explorerConstellation == null) {
			super.handleEvaluateTemplate(workbenchActionContext, templateEvaluationContext);
			return;
		}
		
		Template template = templateEvaluationContext.getTemplate();
		TemplateServiceRequestAction templateServiceRequestAction = workbenchActionContext.getWorkbenchAction();
		boolean useNewNormalizedView = templateServiceRequestAction.getExecutionType() != null;
		if (!useNewNormalizedView) {
			super.handleEvaluateTemplate(workbenchActionContext, templateEvaluationContext);
			return;
		}
		
		explorerConstellation.handleServiceRequestPanel(templateEvaluationContext, templateServiceRequestAction, getHeading(template), null);
	}
	
	@Override
	public Future<Boolean> handleEvaluatedTemplate(Object evaluatedObject, WorkbenchActionContext<TemplateServiceRequestAction> workbenchActionContext)
			throws TemplateEvaluationException {
		Future<Boolean> future = new Future<>();
		
		RequestExecutionData requestExecutionData = new RequestExecutionData((ServiceRequest) evaluatedObject, gmSession,
				currentTransientSessionProvider.get(), listener, transientSessionProvider, notificationFactorySupplier);
		requestExecutionData.setTemplate(workbenchActionContext.getWorkbenchAction().getTemplate());
		
		DdsaRequestExecution.executeRequest(requestExecutionData) //
				.andThen(result -> future.onSuccess(true)) //
				.onError(e -> {
					if (e instanceof ConfirmationFailedException)
						future.onSuccess(false);
					else
						future.onFailure(e);
				});
		
		return future;
	}

	@Override
	public Future<Boolean> checkIfPerformPossible(WorkbenchActionContext<TemplateServiceRequestAction> workbenchActionContext) {
		return new Future<>(true);
	}

	@Override
	public boolean getCloneToPersistenceSession() {
		return false;
	}

	@Override
	public boolean getUseEvaluation() {
		return true;
	}
	
	@Override
	public String getHeading(Template template) {
		Object prototype = template.getPrototype();
		GenericModelType type = GMF.getTypeReflection().getType(prototype);
		
		if (!type.isEntity())
			return null;
		
		ModelMdResolver mdResolver;
		if (((GenericEntity) prototype).session() != null)
			mdResolver = GmSessions.getMetaData((GenericEntity) prototype);
		else {
			TransientPersistenceGmSession currentTrSession = currentTransientSessionProvider.get();
			if (currentTrSession != null && currentTrSession.getModelAccessory() != null) 
				mdResolver = currentTrSession.getModelAccessory().getMetaData().lenient(true);
			else	
			    mdResolver = gmSession.getModelAccessory().getMetaData().lenient(true);
		}
		
		String name = GMEMetadataUtil.getEntityNameMDOrShortName((EntityType<?>) type, mdResolver, null);
		String description = GMEMetadataUtil.getEntityDescriptionMDOrShortName((EntityType<?>) type, mdResolver, null);
		String heading = "";
		
		if (name != null)
			heading = name;
			if (description != null && !description.equals(heading))
				heading = heading + " - " + description;			
		else if (description != null)
			heading = description;
		
		return LocalizedText.INSTANCE.executeType(heading);
	}
	
	@Override
	public String getApplyText() {
		return LocalizedText.INSTANCE.ok();
	}
	
	@Override
	public String getApplyDescriptionText() {
		return LocalizedText.INSTANCE.executeDescription();
	}
	
	@Override
	public String getCancelDescriptionText() {
		return LocalizedText.INSTANCE.cancelExecuteDescription();
	}

}
