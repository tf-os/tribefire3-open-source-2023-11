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
package tribefire.extension.messaging.templates.wire.contract;

import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.messaging.model.deployment.service.MessagingAspect;
import tribefire.extension.messaging.model.deployment.service.MessagingProcessor;
import tribefire.extension.messaging.model.deployment.service.MessagingWorker;
import tribefire.extension.messaging.templates.api.MessagingTemplateContext;

public interface MessagingTemplatesContract extends WireSpace {

	/**
	 * Setup MESSAGING with a specified {@link MessagingTemplateContext}
	 */
	void setupMessaging(MessagingTemplateContext context);

	/**
	 * Set up a full Consumer/Producer set of services
	 * @param context contextTemplate
	 */

	void setupProducerSet(MessagingTemplateContext context);

	void setupConsumerSet(MessagingTemplateContext context);

	/* Consumer Related */

	MessagingWorker messagingWorker(MessagingTemplateContext context);

	<T extends ServiceProcessor> T postProcessor(MessagingTemplateContext context);

	/* Producer Related */

	MessagingAspect messagingAspect(MessagingTemplateContext context);

	MessagingProcessor sendMessageProcessor(MessagingTemplateContext context);

}
