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

import org.junit.experimental.categories.Category;

//import org.junit.Test;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

@Category(SpecialEnvironment.class)
public class EtcdMessageConsumerTest extends EtcdDenotationTypeBasedTest {
	
	public static void main(String[] args) {
		EtcdMessageConsumerTest t = new EtcdMessageConsumerTest();
		
		try {
			t.testSynchronous();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//@Test
	public void testSynchronous() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		
		MessagingSession session = connection.createMessagingSession();
		
		Destination destination = session.createQueue("tf-test-queue-02");
		
		MessageConsumer messageConsumer = session.createMessageConsumer(destination);
		
		while(true) {
			Message message = messageConsumer.receive();
			System.out.println("GOT MESSAGE FROM RECEIVE(): "+message.getBody());
		}
	}
	
	//@Test
	public void testAsynchronous() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		
		MessagingSession session = connection.createMessagingSession();
		
		Destination destination = session.createTopic("tf-test-topic-02");
		
		MessageConsumer messageConsumer = session.createMessageConsumer(destination);
		
		messageConsumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message message) throws MessagingException {
				System.out.println("GOT MESSAGE FROM LISTENER: "+message.getBody());
			}
			
		});
		
		Thread.sleep(20000);
		
	}
	
}
