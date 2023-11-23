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
package tribefire.extension.messaging.initializer.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.messaging.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.messaging.initializer.wire.contract.MessagingInitializerModuleDefaultsContract;
import tribefire.extension.messaging.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.messaging.model.InterceptionTarget;
import tribefire.extension.messaging.model.ResourceBinaryPersistence;
import tribefire.extension.messaging.model.conditions.types.TypeComparison;
import tribefire.extension.messaging.model.conditions.types.TypeOperator;
import tribefire.extension.messaging.model.deployment.event.ProducerEventConfiguration;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerStandardEventRule;
import tribefire.extension.messaging.model.service.demo.ProduceDemoMessage;
import tribefire.extension.messaging.templates.api.MessagingTemplateContext;
import tribefire.extension.messaging.templates.wire.contract.MessagingTemplatesContract;

@Managed
public class MessagingInitializerModuleDefaultsSpace extends AbstractInitializerSpace implements MessagingInitializerModuleDefaultsContract {

	private static final Logger logger = Logger.getLogger(MessagingInitializerModuleDefaultsSpace.class);

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private RuntimePropertiesContract properties;

	@Import
	private MessagingTemplatesContract messagingTemplates;

	@Override
	public void setupDefaultConfiguration() {
		MessagingTemplateContext defaultTemplateContext = defaultTemplateContext();
		logger.debug(() -> "Created messaging template context: \n" + StringTools.asciiBoxMessage(defaultTemplateContext.toString(), -1));

		messagingTemplates.setupMessaging(defaultTemplateContext);

	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	@Managed
	private MessagingTemplateContext defaultTemplateContext() {
		//@formatter:off
		MessagingTemplateContext context = MessagingTemplateContext.builder()
				.setContext("$Default")
				.setEntityFactory(super::create)
				.setMessagingModule(existingInstances.module())
				.setLookupFunction(super::lookup)
				.setLookupExternalIdFunction(super::lookupExternalId)
				.setEventConfiguration(producerEventConfiguration())
				.setServiceModelDependency(existingInstances.serviceModel())
			.build();
		//@formatter:on
		return context;
	}

	@Managed
	private ProducerEventConfiguration producerEventConfiguration() {
		ProducerEventConfiguration bean = ProducerEventConfiguration.T.create();
		bean.setInterceptionType(existingInstances.genericEntityType());
		bean.setEventRules(asList(producerEventRule()));

		return bean;
	}

	@Managed
	private ProducerEventRule producerEventRule() {
		ProducerStandardEventRule bean = ProducerStandardEventRule.T.create();

		bean.setEndpointConfiguration(null);
		bean.setFilePersistenceStrategy(ResourceBinaryPersistence.NONE);
		bean.setInterceptionTarget(InterceptionTarget.REQUEST);
		bean.setName("Default Rule");
		bean.setRequestPropertyCondition(null);
		bean.setRequestTypeCondition(typeComparison());
		bean.setRuleEnabled(true);

		return bean;
	}

	@Managed
	private TypeComparison typeComparison() {
		TypeComparison bean = TypeComparison.T.create();
		bean.setOperator(TypeOperator.equal);
		bean.setTypeName(ProduceDemoMessage.T.getTypeName());
		return bean;
	}

}
