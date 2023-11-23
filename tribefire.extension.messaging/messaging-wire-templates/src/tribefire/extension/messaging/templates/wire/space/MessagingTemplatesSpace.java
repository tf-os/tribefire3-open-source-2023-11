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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.function.Predicate;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.messaging.model.InterceptionTarget;
import tribefire.extension.messaging.model.MessagingComponentDeploymentType;
import tribefire.extension.messaging.model.ResourceBinaryPersistence;
import tribefire.extension.messaging.model.conditions.types.TypeComparison;
import tribefire.extension.messaging.model.conditions.types.TypeOperator;
import tribefire.extension.messaging.model.deployment.event.EventConfiguration;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.model.deployment.event.KafkaEndpoint;
import tribefire.extension.messaging.model.deployment.event.ProducerEventConfiguration;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerStandardEventRule;
import tribefire.extension.messaging.model.deployment.service.MessagingAspect;
import tribefire.extension.messaging.model.deployment.service.MessagingProcessor;
import tribefire.extension.messaging.model.deployment.service.MessagingWorker;
import tribefire.extension.messaging.model.deployment.service.test.TestReceiveMessagingProcessor;
import tribefire.extension.messaging.model.service.demo.ProduceDemoMessage;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;
import tribefire.extension.messaging.templates.api.MessagingTemplateContext;
import tribefire.extension.messaging.templates.util.MessagingTemplateUtil;
import tribefire.extension.messaging.templates.wire.contract.BasicInstancesContract;
import tribefire.extension.messaging.templates.wire.contract.MessagingTemplatesContract;

@Managed
public class MessagingTemplatesSpace implements WireSpace, MessagingTemplatesContract {

	private static final Logger logger = Logger.getLogger(MessagingTemplatesSpace.class);

	private static final Predicate<MessagingTemplateContext> aspectPredicate = c -> ((ProducerEventConfiguration) c.getEventConfiguration())
			.getInterceptionType() != null && c.getServiceModelDependency() != null;

	@Import
	private BasicInstancesContract basicInstances;

	@Import
	private MessagingMetaDataSpace messagingMetaData;

	@Override
	public void setupMessaging(MessagingTemplateContext context) {
		checkContextPresent(context);
		logger.debug(() -> "Configuring MESSAGING based on:\n" + StringTools.asciiBoxMessage(context.toString(), -1));

		EventConfiguration configuration = context.getEventConfiguration();
		if (configuration.getDeploymentType() == MessagingComponentDeploymentType.PRODUCER) {
			setupProducerSet(context);
		} else if (configuration.getDeploymentType() == MessagingComponentDeploymentType.PRODUCER) {
			setupConsumerSet(context);
		} else {
			throw new IllegalStateException("Configuration type: '" + configuration.getDeploymentType() + "' not supported");
		}
	}

	@Override
	public void setupProducerSet(MessagingTemplateContext context) {
		checkContextPresent(context);
		sendMessageProcessor(context);
		if (aspectPredicate.test(context)) {
			messagingAspect(context);
		}
		logger.info("Messaging Producer Set has being deployed");
	}

	@Override
	public void setupConsumerSet(MessagingTemplateContext context) {
		checkContextPresent(context);
		messagingWorker(context);
		// Optional.ofNullable(((ConsumerEventConfiguration)context.getEventConfiguration()).getPostProcessorType()).ifPresent(t
		// -> postProcessor(context)); //TODO FIX ME when fixing CONSUMER EVENT
		logger.info("Messaging Consumer Set has being deployed");
	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public <T extends ServiceProcessor> T postProcessor(MessagingTemplateContext context) {
		T bean = context.create((EntityType<T>) TestReceiveMessagingProcessor.T/* context.getPostProcessorType() */,
				InstanceConfiguration.currentInstance());
		bean.setAutoDeploy(true);
		bean.setName(MessagingTemplateUtil.resolveContextBasedDeployableName("Messaging Service Post Processor", context));

		messagingMetaData.processWithPostProcessor(context);
		return bean;
	}

	@Override
	@Managed
	public MessagingProcessor sendMessageProcessor(MessagingTemplateContext context) {
		MessagingProcessor bean = context.create(MessagingProcessor.T, InstanceConfiguration.currentInstance());
		bean.setAutoDeploy(true);
		bean.setName(MessagingTemplateUtil.resolveContextBasedDeployableName("Messaging Service Processor", context));

		messagingMetaData.processSendMessageRequest(context);
		if (context.getContext().equals("$Default")) {
			// TODO: not 100% sure here how to handle the demo setup nicely...
			producerEventRule(context);
			producerEventRuleWithResource(context);
		}
		return bean;
	}

	// -----------------------------------------------------------------------
	// WORKER
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public MessagingWorker messagingWorker(MessagingTemplateContext context) {
		MessagingWorker bean = context.create(MessagingWorker.T, InstanceConfiguration.currentInstance());
		bean.setAutoDeploy(true);
		bean.setName(MessagingTemplateUtil.resolveContextBasedDeployableName("Messaging Worker", context));

		return bean;
	}

	// -----------------------------------------------------------------------
	// ASPECT
	// -----------------------------------------------------------------------

	@Override
	@Managed
	public MessagingAspect messagingAspect(MessagingTemplateContext context) {
		MessagingAspect bean = context.create(MessagingAspect.T, InstanceConfiguration.currentInstance());
		bean.setAutoDeploy(true);
		bean.setName(MessagingTemplateUtil.resolveContextBasedDeployableName("Messaging Aspect", context));
		bean.setMessagingProcessor(sendMessageProcessor(context));
		bean.setConfigurationName(context.getContext());
		messagingMetaData.aspectMetaData(context);
		return bean;
	}

	private void checkContextPresent(MessagingTemplateContext context) {
		if (context == null) {
			throw new UnsatisfiedMaybeTunneling(
					Reasons.build(ArgumentNotSatisfied.T).text("The MessagingTemplateContext must not be null").toMaybe());
		}
	}

	// -----------------------------------------------------------------------
	// DEMO SETUP
	// -----------------------------------------------------------------------

	@Managed
	private ProducerEventRule producerEventRuleWithResource(MessagingTemplateContext context) {
		ProducerStandardEventRule bean = context.create(ProducerStandardEventRule.T, InstanceConfiguration.currentInstance());

		bean.setGlobalId("YYY$Default");
		bean.setEndpointConfiguration(asList(eventEndpointConfiguration(context)));
		bean.setFilePersistenceStrategy(ResourceBinaryPersistence.ALL);
		bean.setInterceptionTarget(InterceptionTarget.REQUEST);
		bean.setName("Default Rule");
		bean.setRequestPropertyCondition(null);
		bean.setRequestTypeCondition(typeComparison(context));
		bean.setRuleEnabled(true);

		return bean;
	}

	@Managed
	private ProducerEventRule producerEventRule(MessagingTemplateContext context) {
		ProducerStandardEventRule bean = context.create(ProducerStandardEventRule.T, InstanceConfiguration.currentInstance());

		bean.setGlobalId("XXX$Default");
		bean.setEndpointConfiguration(asList(eventEndpointConfiguration(context)));
		bean.setFilePersistenceStrategy(ResourceBinaryPersistence.NONE);
		bean.setInterceptionTarget(InterceptionTarget.REQUEST);
		bean.setName("Default Rule");
		bean.setRequestPropertyCondition(null);
		bean.setRequestTypeCondition(typeComparison(context));
		bean.setRuleEnabled(true);

		return bean;
	}

	@Managed
	private TypeComparison typeComparison(MessagingTemplateContext context) {
		TypeComparison bean = context.create(TypeComparison.T, InstanceConfiguration.currentInstance());
		bean.setOperator(TypeOperator.equal);
		// TODO: this needs to be the fully qualified type name and not the short name
		// bean.setTypeName(ProduceDemoMessage.T.getTypeName());
		bean.setTypeName(ProduceDemoMessage.T.getShortName());
		return bean;
	}

	@Managed
	private EventEndpointConfiguration eventEndpointConfiguration(MessagingTemplateContext context) {
		EventEndpointConfiguration bean = context.create(EventEndpointConfiguration.T, InstanceConfiguration.currentInstance());
		bean.setTopics(asSet("test"));
		bean.setEventEndpoint(eventEndpoint(context));
		return bean;
	}

	@Managed
	private KafkaEndpoint eventEndpoint(MessagingTemplateContext context) {
		KafkaEndpoint bean = context.create(KafkaEndpoint.T, InstanceConfiguration.currentInstance());
		bean.setConnectionUrl("localhost:29092");
		bean.setName("Test Kafka Endpoint");
		return bean;
	}
}
