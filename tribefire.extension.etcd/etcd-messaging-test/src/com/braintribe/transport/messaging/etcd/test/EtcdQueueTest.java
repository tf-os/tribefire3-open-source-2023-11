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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.messaging.expert.Messaging;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;

@Category(SpecialEnvironment.class)
public class EtcdQueueTest {

	private static void emptyDestination(Destination destination, MessagingSession session) throws Exception {

		MessageConsumer consumer = session.createMessageConsumer(destination);
		Message message = null;
		try {
			do {
				message = consumer.receive(2000L);
			} while(message != null);
		} finally {
			consumer.close();
		}

	}

	@Test
	public void testTopicSimple() throws Exception {
		MessagingConnectionProvider<? extends Messaging> messagingConnectionProvider = EtcdMessagingConnectionProvider.instance.get();

		MessagingConnection connection = messagingConnectionProvider.provideMessagingConnection();

		connection.open();

		MessagingSession session = connection.createMessagingSession();

		session.open();

		Topic topic = session.createTopic("test-topic.6");

		emptyDestination(topic, session);

		MessageConsumer consumer = session.createMessageConsumer(topic);

		final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();
		consumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message message) throws MessagingException {
				messages.add(message);
			}
		});

		MessageProducer producer = session.createMessageProducer(topic);

		Message sendMessage = session.createMessage();
		sendMessage.setBody("Hello, world!");

		//Allow for listener to become alive
		Thread.sleep(1000L);
		
		producer.sendMessage(sendMessage);

		Message receivedMessage = messages.poll(10000L, TimeUnit.MILLISECONDS);

		assertThat(receivedMessage.getBody().toString()).isEqualTo("Hello, world!");

		session.close();

		connection.close();
	}

	@Test
	public void testQueueSimple() throws Exception {
		MessagingConnectionProvider<? extends Messaging> messagingConnectionProvider = EtcdMessagingConnectionProvider.instance.get();

		MessagingConnection connection = messagingConnectionProvider.provideMessagingConnection();

		connection.open();

		MessagingSession session = connection.createMessagingSession();

		session.open();

		Queue queue = session.createQueue("test-queue.2");

		emptyDestination(queue, session);

		MessageConsumer consumer = session.createMessageConsumer(queue);

		final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();
		consumer.setMessageListener(new MessageListener() {

			@Override
			public void onMessage(Message message) throws MessagingException {
				messages.add(message);
			}
		});

		MessageProducer producer = session.createMessageProducer(queue);

		Message sendMessage = session.createMessage();
		sendMessage.setBody("Hello, world!");

		//Allow for listener to become alive
		Thread.sleep(1000L);

		producer.sendMessage(sendMessage);

		Message receivedMessage = messages.poll(10000L, TimeUnit.MILLISECONDS);

		assertThat(receivedMessage.getBody().toString()).isEqualTo("Hello, world!");

		session.close();

		connection.close();
	}

	@Test
	public void testTopicMultithreaded() throws Exception {
		MessagingConnectionProvider<? extends Messaging> messagingConnectionProvider = EtcdMessagingConnectionProvider.instance.get();

		MessagingConnection connection = messagingConnectionProvider.provideMessagingConnection();

		connection.open();

		MessagingSession session = connection.createMessagingSession();

		session.open();

		Topic topic = session.createTopic("test-topic.3");

		emptyDestination(topic, session);

		int worker = 10;

		final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();

		for (int i=0; i<worker; ++i) {
			MessageConsumer consumer = session.createMessageConsumer(topic);
			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message message) throws MessagingException {
					System.out.println("Received a message...");
					messages.add(message);
				}
			});
		}

		MessageProducer producer = session.createMessageProducer(topic);

		Message sendMessage = session.createMessage();
		sendMessage.setBody("Hello, world!");

		//Allow for listener to become alive
		Thread.sleep(1000L);

		producer.sendMessage(sendMessage);
		System.out.println("Sent message.");

		int count = 0;
		while (count < worker) {
			Message receivedMessage = messages.poll(10000L, TimeUnit.MILLISECONDS);
			assertThat(receivedMessage.getBody().toString()).isEqualTo("Hello, world!");
			count++;
		}

		session.close();

		connection.close();
	}

	@Test
	public void testQueueMultithreaded() throws Exception {
		MessagingConnectionProvider<? extends Messaging> messagingConnectionProvider = EtcdMessagingConnectionProvider.instance.get();

		MessagingConnection connection = messagingConnectionProvider.provideMessagingConnection();

		connection.open();

		MessagingSession session = connection.createMessagingSession();

		session.open();

		Queue queue = session.createQueue("test-queue.1");

		emptyDestination(queue, session);


		int worker = 10;

		final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();

		for (int i=0; i<worker; ++i) {
			MessageConsumer consumer = session.createMessageConsumer(queue);
			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message message) throws MessagingException {
					System.out.println("Received a message...");
					messages.add(message);
				}
			});
		}

		MessageProducer producer = session.createMessageProducer(queue);

		Message sendMessage = session.createMessage();
		sendMessage.setBody("Hello, world!");

		//Allow for listener to become alive
		Thread.sleep(1000L);

		producer.sendMessage(sendMessage);
		System.out.println("Sent message.");

		Message receivedMessage = messages.poll(10000L, TimeUnit.MILLISECONDS);
		assertThat(receivedMessage.getBody().toString()).isEqualTo("Hello, world!");

		receivedMessage = messages.poll(10000L, TimeUnit.MILLISECONDS);
		assertThat(receivedMessage).isNull();

		session.close();

		connection.close();
	}

	@Test
	@Category(VerySlow.class)
	public void testTopicMultithreadedMassive() throws Exception {

		MessagingConnectionProvider<? extends Messaging> messagingConnectionProvider = EtcdMessagingConnectionProvider.instance.get();

		MessagingConnection connection = messagingConnectionProvider.provideMessagingConnection();

		connection.open();

		MessagingSession session = connection.createMessagingSession();

		session.open();

		Topic topic = session.createTopic("test-topic.4");

		emptyDestination(topic, session);

		int worker = 10;
		int messageCount = 1000;

		final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();

		for (int i=0; i<worker; ++i) {
			MessageConsumer consumer = session.createMessageConsumer(topic);
			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message message) throws MessagingException {
					//System.out.println("Received a message: "+message.getBody());
					messages.add(message);
				}
			});
		}

		MessageProducer producer = session.createMessageProducer(topic);

		//Allow for listener to become alive
		Thread.sleep(1000L);

		Instant start = NanoClock.INSTANCE.instant();

		for (int i=0; i<messageCount; ++i) {
			Message sendMessage = session.createMessage();
			sendMessage.setBody("Hello, world! "+i);
			producer.sendMessage(sendMessage);
		}

		System.out.println("Sent "+messageCount+" messages in: "+StringTools.prettyPrintDuration(start, NanoClock.INSTANCE.instant(), true, null));

		int count = 0;
		while (count < (worker*messageCount)) {
			Message receivedMessage = messages.poll(10000L, TimeUnit.MILLISECONDS);
			assertThat(receivedMessage.getBody().toString()).startsWith("Hello, world!");
			count++;
		}

		System.out.println("Received "+(messageCount*worker)+" messages in: "+StringTools.prettyPrintDuration(start, NanoClock.INSTANCE.instant(), true, null));

		session.close();

		connection.close();
	}

	@Test
	@Category(VerySlow.class)
	public void testTopicMultithreadedMassiveMultipleProducers() throws Exception {

		MessagingConnectionProvider<? extends Messaging> messagingConnectionProvider = EtcdMessagingConnectionProvider.instance.get();

		MessagingConnection connection = messagingConnectionProvider.provideMessagingConnection();

		connection.open();

		MessagingSession session = connection.createMessagingSession();

		session.open();

		Topic topic = session.createTopic("test-topic.5");

		emptyDestination(topic, session);

		int producers = 10;
		int worker = 10;
		int messageCount = 100;

		final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<>();

		for (int i=0; i<worker; ++i) {
			MessageConsumer consumer = session.createMessageConsumer(topic);
			consumer.setMessageListener(new MessageListener() {

				@Override
				public void onMessage(Message message) throws MessagingException {
					//System.out.println("Received a message: "+message.getBody()+" in thread "+Thread.currentThread().getName());
					messages.add(message);
				}
			});
		}

		System.out.println("Starting to send "+(messageCount*producers)+" messages.");

		//Allow for listener to become alive
		Thread.sleep(1000L);

		Instant start = NanoClock.INSTANCE.instant();

		ExecutorService service = Executors.newFixedThreadPool(producers);
		List<Future<?>> futures = new ArrayList<>();
		for (int j=0; j<producers; ++j) {

			futures.add(service.submit(() -> {

				MessageProducer producer = session.createMessageProducer(topic);

				for (int i=0; i<messageCount; ++i) {
					Message sendMessage = session.createMessage();
					sendMessage.setBody("Hello, world! "+i);
					producer.sendMessage(sendMessage);
				}

				producer.close();
			}));

		}

		for (Future<?> f : futures) {
			f.get();
		}

		service.shutdown();

		System.out.println("Sent "+(messageCount*producers)+" messages in: "+StringTools.prettyPrintDuration(start, NanoClock.INSTANCE.instant(), true, null));

		int count = 0;
		while (count < (worker*messageCount*producers)) {
			Message receivedMessage = messages.poll(10000L, TimeUnit.MILLISECONDS);
			assertThat(receivedMessage.getBody().toString()).startsWith("Hello, world!");
			count++;
		}

		System.out.println("Received "+(messageCount*worker*producers)+" messages in: "+StringTools.prettyPrintDuration(start, NanoClock.INSTANCE.instant(), true, null));

		session.close();

		connection.close();
	}

}
