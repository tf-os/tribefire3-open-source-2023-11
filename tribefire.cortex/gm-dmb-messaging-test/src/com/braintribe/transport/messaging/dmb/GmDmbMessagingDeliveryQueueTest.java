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
package com.braintribe.transport.messaging.dmb;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.braintribe.model.messaging.Destination;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.test.GmMessagingDeliveryQueueTest;
import com.braintribe.transport.messaging.dbm.GmDmbMqMessageProducer;

public class GmDmbMessagingDeliveryQueueTest extends GmMessagingDeliveryQueueTest {

	@Override
	protected MessagingConnectionProvider<? extends MessagingConnection> getMessagingConnectionProvider() {
		return GmDmbMessagingConnectionProvider.instance.get();
	}

	@Override
	protected MessagingContext getMessagingContext() {
		return GmDmbMessagingConnectionProvider.instance.getMessagingContext();
	}

	/**
	 * <p>
	 * Produces an unmarshallable message payload to ensure the consumers behavior upon marshalling errors.
	 */
	@Override
	protected void sendUnmarshallableMessage(MessageProducer messageProducer) {

		GmDmbMqMessageProducer producer = (GmDmbMqMessageProducer) messageProducer;

		Destination destination = producer.getDestination();

		char destinationType = producer.getDestinationType(destination);

		byte[] messageBody = "This is just an example of unmarshallable body".getBytes();

		Map<String, Object> empty = Collections.emptyMap();

		producer.getSession().getConnection().getMessagingMBean().sendMessage(destinationType, destination.getName(), UUID.randomUUID().toString(),
				messageBody, 5, 0, empty, empty);

	}

}
