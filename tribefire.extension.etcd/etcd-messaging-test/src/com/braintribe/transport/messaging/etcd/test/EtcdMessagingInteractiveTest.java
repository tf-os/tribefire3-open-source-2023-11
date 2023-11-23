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
package com.braintribe.transport.messaging.etcd.test;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.test.GmMessagingTest;

@Category(SpecialEnvironment.class)
public class EtcdMessagingInteractiveTest extends GmMessagingTest {
	
	@Override
	protected MessagingConnectionProvider<? extends MessagingConnection> getMessagingConnectionProvider() {
		return EtcdMessagingConnectionProvider.instance.get();
	}

	@Override
	protected MessagingContext getMessagingContext() {
		return EtcdMessagingConnectionProvider.instance.getMessagingContext();
	}
	
	@Category(VerySlow.class)
	@Test
	public void testConnectionAutoRecovery() throws Exception {
		
		MessagingConnectionProvider<? extends MessagingConnection> connectionProvider =  getMessagingConnectionProvider();
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession session = connection.createMessagingSession();
		session.open();

		Destination queue = createDestination(Queue.class, session, getMethodName());

		Message outgoingMessage = createMessage(session, true, 120000);
		
		MessageProducer producer = session.createMessageProducer(queue);
		producer.sendMessage(outgoingMessage);

		System.out.println("Please restart the messaging server/engine at this point an press any key when done.");
		//System.in.read();
		Thread.sleep(60000);
		System.out.println("Continuing...");
		
		MessageConsumer consumer = session.createMessageConsumer(queue);
		
		Message incomingMessage = consumer.receive();
		
		assertEquals(outgoingMessage, incomingMessage);
		
		session.close();
		connection.close();
		
	}
	

}
