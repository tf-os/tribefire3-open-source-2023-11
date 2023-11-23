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
package com.braintribe.transport.messaging.api.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.testing.category.Slow;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

/**
 * <p>
 * Tests the delivery of messages through queues.
 * 
 */
public abstract class GmMessagingDeliveryQueueTest extends GmMessagingDeliveryTest {

	@Override
	public Class<? extends Destination> getDestinationType() {
		return Queue.class;
	}

	@Test
	public void testMessagesDistribution() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Queue queue = session.createQueue("gm-test-queue-"+getMethodName());

		final MessageConsumer consumer1 = session.createMessageConsumer(queue);
		final MessageConsumer consumer2 = session.createMessageConsumer(queue);
		final MessageConsumer consumer3 = session.createMessageConsumer(queue);
		final MessageConsumer consumer4 = session.createMessageConsumer(queue);
		final MessageConsumer consumer5 = session.createMessageConsumer(queue);
		
		final Map<Message, MessageConsumer> recipients = new ConcurrentHashMap<Message, MessageConsumer>();
		
		consumer1.setMessageListener(new MessageListener() {
			@Override public void onMessage(Message message) throws MessagingException {
				consumer1.close();
				recipients.put(message, consumer1);
			}
		});

		consumer2.setMessageListener(new MessageListener() {
			@Override public void onMessage(Message message) throws MessagingException {
				consumer2.close();
				recipients.put(message, consumer2);
			}
		});

		consumer3.setMessageListener(new MessageListener() {
			@Override public void onMessage(Message message) throws MessagingException {
				consumer3.close();
				recipients.put(message, consumer3);
			}
		});

		consumer4.setMessageListener(new MessageListener() {
			@Override public void onMessage(Message message) throws MessagingException {
				consumer4.close();
				recipients.put(message, consumer4);
			}
		});

		consumer5.setMessageListener(new MessageListener() {
			@Override public void onMessage(Message message) throws MessagingException {
				consumer5.close();
				recipients.put(message, consumer5);
			}
		});
		
		MessageProducer producer = session.createMessageProducer(queue);
		List<Message> messages = createMessages(session, 9);
		
		for (Message message : messages) {
			producer.sendMessage(message);
		}

		long timeout = System.currentTimeMillis() + 10000;
		int rec = 0;
		while (rec < 5) {
			System.out.println("Waiting 5 messages. Current: "+rec);
			Thread.sleep(500);
			rec = 0;
			for (@SuppressWarnings("unused") Map.Entry<Message, MessageConsumer> entry : recipients.entrySet()) {
				rec++;
			}
			if (System.currentTimeMillis() > timeout) {
				break;
			}
		}
		
		Thread.sleep(2000);
		
		//final count
		rec = 0;
		for (Map.Entry<Message, MessageConsumer> entry : recipients.entrySet()) {
			System.out.println(entry.getKey().hashCode()+" was delivered to "+entry.getValue());
			rec++;
		}
		
		connection.close();
		
		Assert.assertEquals("It appears some closed producers also got messages", 5, rec);
		
	}

	@Test
	@Category(Slow.class)
	public void testReceiveTimeout() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		
		MessagingSession session = connection.createMessagingSession();

		Destination destination = session.createQueue("tf-test-queue-"+getMethodName());

		MessageProducer messageProducer = session.createMessageProducer(destination);
		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		//send persistent message that will expire in 2 seconds.
		Message sentMessage = createMessage(session, true, 2000);
		messageProducer.sendMessage(sentMessage);
		
		//receive with 0 timeout, one message
		Message message = messageConsumer.receive(0);
		Assert.assertNotNull(message);

		//send persistent message that will expire in 2 seconds.
		sentMessage = createMessage(session, true, 2000);
		messageProducer.sendMessage(sentMessage);

		//receive with 1000 timeout, one message
		message = messageConsumer.receive(1000);
		Assert.assertNotNull(message);

		//send persistent message that will expire in 2 seconds.
		sentMessage = createMessage(session, true, 2000);
		messageProducer.sendMessage(sentMessage);

		//receive with 5000 timeout, one message
		message = messageConsumer.receive(2000);
		Assert.assertNotNull(message);
		
		//receive with 5000 timeout, no messages
		message = messageConsumer.receive(5000);
		Assert.assertNull(message);
		
		connection.close();
	}
	
	@Test
	public void testReceiveRetained() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		
		MessagingSession session = connection.createMessagingSession();

		Destination destination = session.createQueue("tf-test-queue-"+getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		//persistent message that will expire in 2 seconds.
		Message sentMessage = createMessage(session, true, 2000);
		
		MessageProducer messageProducer = session.createMessageProducer(destination);
		
		messageProducer.sendMessage(sentMessage);
		
		//Creating consumer after sendMessage();

		ReceiverMessageListener messageListener = new ReceiverMessageListener();
		ReceiverJob receiverJob = new ReceiverJob(messageConsumer, messageListener);
		
		ExecutorService executorService = submitReceiver(messageListener, receiverJob);

		try {
			assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(sentMessage), 1, false);
		} finally {
			executorService.shutdownNow();
			connection.close();
		}
		
	}
	
	@Test
	public void testReceiveRetainedBatch() throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		
		MessagingSession session = connection.createMessagingSession();
		
		Destination destination = session.createQueue("tf-test-queue-"+getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		//10.000 persistent messages that will expire in 5 seconds.
		List<Message> messages = createMessages(session, multipleMessagesQty, true, 5000);
		
		MessageProducer messageProducer = session.createMessageProducer(destination);
		
		for (Message message : messages) {
			messageProducer.sendMessage(message);
		}

		ReceiverMessageListener messageListener = new ReceiverMessageListener();
		ReceiverJob receiverJob = new ReceiverJob(messageConsumer, messageListener);

		ExecutorService executorService = submitReceiver(messageListener, receiverJob);
		
		try {
			assertDeliveries(getMethodName(), destination.getClass(), messageListener, messages, 1, false);
		} finally {
			executorService.shutdownNow();
			connection.close();
		}
		
	}
	
	
}
