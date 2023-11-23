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
package com.braintribe.model.processing.cortex.service.workbench;

import java.util.Collections;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.accessapi.ModelEnvironmentServices;
import com.braintribe.model.cortexapi.workbench.CreateServiceRequestTemplate;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notifications;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.workbench.WorkbenchInstructionProcessor;
import com.braintribe.model.workbench.TemplateInstantiationServiceRequestAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.model.workbench.instruction.CreateTemplateBasedAction;

public class TemplateCreator extends ServiceBase {
	
	private static Logger logger = Logger.getLogger(TemplateCreator.class);

	private AccessService accessService;
	private PersistenceGmSessionFactory sessionFactory;
	private CreateServiceRequestTemplate request;

	
	public TemplateCreator(PersistenceGmSessionFactory sessionFactory, CreateServiceRequestTemplate request, AccessService accessService) {
		this.sessionFactory = sessionFactory;
		this.request = request;
		this.accessService = accessService;
	}
	
	public Notifications run() throws Exception {
	
		String workbenchAccessId = getWorkbenchAccessId();
		if (workbenchAccessId == null) {
			return createConfirmationResponse("No workbench access found!", Level.ERROR, Notifications.T);
		}
		PersistenceGmSession workbenchSession = sessionFactory.newSession(workbenchAccessId);
		
		CreateTemplateBasedAction instruction = CreateTemplateBasedAction.T.create();
		instruction.setActionName(request.getActionName());
		instruction.setPrototype(request.getTemplateRequest());
		instruction.setActionType((request.getInstantiationAction() ? TemplateInstantiationServiceRequestAction.T.getTypeSignature() : TemplateServiceRequestAction.T.getTypeSignature()));
		instruction.setPath(request.getFolderPath());
		instruction.setMultiSelectionSupport(request.getMultiSelectionSupport());
		instruction.setIgnoreProperties(request.getIgnoreProperties());
		instruction.setIgnoreStandardProperties(request.getIgnoreStandardProperties());
		
		GmEntityType criterionType = request.getCriterionType();
		if (criterionType != null) {
			instruction.setCriterion(
					TC.create()
						.typeCondition(
								TypeConditions.isAssignableTo(criterionType.getTypeSignature())
								).done());
		}
		
		
		
		WorkbenchInstructionProcessor instructionProcessor = new WorkbenchInstructionProcessor();
		instructionProcessor.setSession(workbenchSession);
		instructionProcessor.setCommitAfterProcessing(true);
		
		instructionProcessor.processInstructions(Collections.singletonList(instruction));
		
		
		addNotifications(com.braintribe.model.processing.notification.api.builder.Notifications.build()
						.add()
							.message().confirmInfo("Please consider reloading the ControlCenter! \n Do you want to reload now?")
							.command().reload("Reload ControlCenter")
						.close()
					.list());
		
		return createResponse("Added action template for: "+request.getTemplateRequest().entityType().getShortName()+" to workbench.", Notifications.T);
	}
	
	private String getWorkbenchAccessId() {
		String wbAccessId = request.getWorkbenchAccessId();
		if (wbAccessId == null) {
			wbAccessId = getDefaultWorkbenchAccessId();
		}
		return wbAccessId;
	}

	private String getDefaultWorkbenchAccessId() {
		String domainId = request.getDomainId();
		try {
			ModelEnvironmentServices modelEnvironmentServices = accessService.getModelEnvironmentServices(domainId);
			return modelEnvironmentServices.getWorkbenchModelAccessId();
		} catch (Exception e) {
			logger.warn("Could not determine workbenchAccess for domain: "+domainId,e);
			return null;
		}
	}
}
