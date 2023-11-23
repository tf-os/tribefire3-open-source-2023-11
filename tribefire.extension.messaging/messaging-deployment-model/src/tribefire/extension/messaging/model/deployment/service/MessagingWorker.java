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
package tribefire.extension.messaging.model.deployment.service;

import com.braintribe.model.extensiondeployment.Worker;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface MessagingWorker extends Worker {

	EntityType<MessagingWorker> T = EntityTypes.T(MessagingWorker.class);

	/*String messagingConsumer = "messagingConsumer";

	@Mandatory //TODO this can probably be revived when Consumer tasks would be of a priority @dmiex
	@Name("Messaging Consumer")
	@Description("Consumer for messaging backend")
	MessagingConsumer getMessagingConsumer();
	void setMessagingConsumer(MessagingConsumer messagingConsumer);*/
}
