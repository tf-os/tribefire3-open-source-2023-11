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
package com.braintribe.transport.messaging.rabbitmq;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.test.GmMessagingDeliveryTopicTest;
import com.rabbitmq.client.AMQP;

@Category(SpecialEnvironment.class)
public class RabbitMqMessagingDeliveryTopicTest extends GmMessagingDeliveryTopicTest {

	@Override
	protected MessagingConnectionProvider<? extends MessagingConnection> getMessagingConnectionProvider() {
		return RabbitMqMessagingConnectionProvider.instance.get();
	}

	@Override
	protected MessagingContext getMessagingContext() {
		return RabbitMqMessagingConnectionProvider.instance.getMessagingContext();
	}

	@BeforeClass
	public static void configure() {
		// Overriding test parameters
		multipleMessagesQty = 200;
	}

	/**
	 * <p>
	 * Produces an unmarshallable message payload to ensure the consumers behavior upon marshalling errors.
	 */
	@Override
	protected void sendUnmarshallableMessage(MessageProducer messageProducer) {

		RabbitMqMessageProducer producer = (RabbitMqMessageProducer) messageProducer;

		RabbitMqDestination destination = producer.getRabbitMqDestination();

		AMQP.BasicProperties.Builder propBuilder = new AMQP.BasicProperties.Builder();

		String mimeType = "application/octet-stream"; 
		// TODO: remove line : producer.getSession().getConnection().getMessageMarshaller().getOutgoingMessagesType();

		// deliveryMode '2' means persistent
		propBuilder.contentType(mimeType).contentEncoding("UTF-8").deliveryMode(2);

		byte[] messageBody = "This is just an example of unmarshallable body".getBytes();

		try {
			producer.getChannel().basicPublish(destination.getExchangeName(), destination.getRoutingKey(), true, propBuilder.build(), messageBody);
		} catch (Exception e) {
			throw new IllegalStateException("Test failed to send an unmarshallable message", e);
		}

	}

}
