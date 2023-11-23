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
package tribefire.extension.messaging.templates.wire.space;

import java.util.Arrays;
import java.util.Optional;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Condensed;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.messaging.MessagingConstants;
import tribefire.extension.messaging.model.deployment.event.ProducerEventConfiguration;
import tribefire.extension.messaging.model.service.consume.ProcessConsumedMessage;
import tribefire.extension.messaging.model.service.demo.ProduceDemoMessage;
import tribefire.extension.messaging.model.service.produce.ProduceMessage;
import tribefire.extension.messaging.templates.api.MessagingTemplateContext;
import tribefire.extension.messaging.templates.util.MessagingTemplateUtil;
import tribefire.extension.messaging.templates.wire.contract.MessagingMetaDataContract;
import tribefire.extension.messaging.templates.wire.contract.MessagingTemplatesContract;

@Managed
public class MessagingMetaDataSpace implements WireSpace, MessagingMetaDataContract {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MessagingMetaDataSpace.class);

	@Import
	private MessagingTemplatesContract messagingTemplates;

	@Override
	@Managed
	public GmMetaModel serviceModel(MessagingTemplateContext context) {
		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		GmMetaModel rawServiceModel = context.lookup("model:" + MessagingConstants.SERVICE_MODEL_QUALIFIEDNAME);
		setModelDetails(model, MessagingTemplateUtil.resolveServiceModelName(context), rawServiceModel);
		return model;
	}

	// --------------------------–

	// -----------------------------------------------------------------------
	// META DATA - PROCESS WITH
	// -----------------------------------------------------------------------
	public void processSendMessageRequest(MessagingTemplateContext context) {
		GmMetaModel serviceModel = serviceModel(context);
		BasicModelMetaDataEditor serviceModelEditor = new BasicModelMetaDataEditor(serviceModel);

		serviceModelEditor.onEntityType(ProduceMessage.T).addMetaData(registerProcessWithSendMessageProcessor(context));

		if (context.getContext().equals("$Default")) {
			// TODO: not 100% sure here how to handle the demo setup nicely...
			serviceModelEditor.onEntityType(ProduceDemoMessage.T).addMetaData(registerProcessWithSendMessageProcessor(context));
			serviceModelEditor.onEntityType(ProduceDemoMessage.T).addMetaData(processWithMessagingAspect(context));
		}
	}

	public void processWithPostProcessor(MessagingTemplateContext context) {
		GmMetaModel serviceModel = serviceModel(context);
		BasicModelMetaDataEditor serviceModelEditor = new BasicModelMetaDataEditor(serviceModel);

		serviceModelEditor.onEntityType(ProcessConsumedMessage.T).addMetaData(registerProcessWithPostProcessor(context));
	}

	// TODO maybe should be revived after as it is not clear if it works as expected in TF env @dmiex task:
	// https://document-one.atlassian.net/browse/D1-3312
	/* @Managed public <T extends ServiceProcessor> ProcessWith registerProcessWith(MessagingTemplateContext context,
	 * Function<MessagingTemplateContext, T> processorFunction) { ProcessWith bean = context.create(ProcessWith.T,
	 * InstanceConfiguration.currentInstance()); bean.setProcessor(processorFunction.apply(context)); return bean; } */

	@Managed
	public ProcessWith registerProcessWithSendMessageProcessor(MessagingTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setProcessor(messagingTemplates.sendMessageProcessor(context));
		return bean;
	}

	@Managed
	public ProcessWith registerProcessWithPostProcessor(MessagingTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setProcessor(messagingTemplates.postProcessor(context));
		return bean;
	}

	public void aspectMetaData(MessagingTemplateContext context) {
		// GmMetaModel serviceModel = serviceModel(context);
		ProducerEventConfiguration configuration = (ProducerEventConfiguration) context.getEventConfiguration();

		// new
		// BasicModelMetaDataEditor(serviceModel).onEntityType(configuration.getAspectInterceptionEntityType()).addMetaData(condensed(context));
		// //TODO This should be talked over again -> how can this work?
		// https://document-one.atlassian.net/browse/D1-3312

		/* new BasicModelMetaDataEditor(context.getServiceModelDependency()).onEntityType(configuration.
		 * getAspectInterceptionEntityType()) .addMetaData(condensed(context)); */ // TODO FIX ME GERRY!!!
	}

	// ----------------------------------------------------

	@Managed
	public GmMetaModel externalServiceModel(MessagingTemplateContext context) {
		GmMetaModel externalServiceModel = context.lookup("model:tribefire.adx.phoenix:adx-content-service-asdf-model");
		return externalServiceModel;
	}

	// ----------------------------------------------------

	@Managed
	private Condensed condensed(MessagingTemplateContext context) {
		Condensed bean = context.create(Condensed.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	private AroundProcessWith processWithMessagingAspect(MessagingTemplateContext context) {
		AroundProcessWith bean = context.create(AroundProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setProcessor(messagingTemplates.messagingAspect(context));
		return bean;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static void setModelDetails(GmMetaModel targetModel, String modelName, GmMetaModel... dependencies) {
		targetModel.setName(modelName);
		targetModel.setVersion(MessagingConstants.MAJOR_VERSION + ".0");
		Optional.ofNullable(dependencies).ifPresent(d -> Arrays.stream(d).forEach(targetModel.getDependencies()::add));
	}
}
