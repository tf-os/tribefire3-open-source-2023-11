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
package com.braintribe.transport.messaging.mq.test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.messaging.jms.JmsMqConnection;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.jms.JmsMqMessaging;
import com.braintribe.transport.messaging.jms.JmsMqSession;
import com.braintribe.transport.messaging.mq.test.config.Configurator;
import com.braintribe.transport.messaging.mq.test.config.TestConfiguration;
import com.braintribe.transport.messaging.mq.test.util.TestUtilities;
import com.braintribe.transport.messaging.mq.test.worker.MessageReceiver;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.ibm.mq.MQEnvironment;

@Category(SpecialEnvironment.class)
public class BasicJmsTests {

	protected TestConfiguration testConfiguration = null;

	protected JmsMqMessaging messaging = null;
	protected MessagingContext context = null;
	protected JmsMqConnection configuration = null;

	protected List<MessagingSession> sessions = new ArrayList<MessagingSession>();
	protected List<MessagingConnection> connections = new ArrayList<MessagingConnection>();

	@BeforeClass
	public static void initTests() throws Exception {
		ConfigurationHolder.configurator = new Configurator();
	}
	
	@Before
	public void initialize() throws Exception {

		this.testConfiguration = ConfigurationHolder.configurator.getContext().getBean("testConfiguration", TestConfiguration.class);
		if (this.testConfiguration == null) {
			throw new Exception("Could not find bean 'testConfiguration'");
		}
		if (this.testConfiguration.getTracing()) {
			MQEnvironment.enableTracing(10);
		}

		configuration = JmsMqConnection.T.create();
		configuration.setHost(this.testConfiguration.getHost());
		configuration.setUsername(this.testConfiguration.getUsername());
		configuration.setPassword(this.testConfiguration.getPassword());
		configuration.setQueueManager(this.testConfiguration.getQueueManager());
		configuration.setChannel(this.testConfiguration.getChannel());

		this.messaging = new JmsMqMessaging();
		this.context = TestUtilities.getMessagingContext();

	}
	
	@AfterClass
	public static void shutdown() {
		ConfigurationHolder.configurator.close();
	}

	@After
	public void destroy() throws Exception {

		for (MessagingSession session : this.sessions) {
			session.close();
		}
		for (MessagingConnection connection : this.connections) {
			connection.close();
		}
	}

	@Test
	public void connectTest() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession session = connection.createMessagingSession();
		session.open();

		session.close();
		connection.close();
	}

	@Test
	public void sendSingleMessageTest() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession session = connection.createMessagingSession();
		session.open();

		TestUtilities.emptyQueue((JmsMqSession) session, testConfiguration.getQueueName());
		
		Queue queue = session.createQueue(testConfiguration.getQueueName());

		Message sendMessage = Message.T.create();
		String sendBody = "Hello, world!";
		sendMessage.setBody(sendBody);
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("standardName", "shouldWork");
		properties.put("%someNonStandardName/;", "shouldAlsoWork");
		sendMessage.setProperties(properties);

		MessageProducer producer = session.createMessageProducer(queue);
		producer.sendMessage(sendMessage);

		MessageConsumer consumer = session.createMessageConsumer(queue);
		List<Message> receivedMessages = MessageReceiver.receiveMessagesSync(consumer, 1, 10000L);

		Message receivedMessage = receivedMessages.get(0);
		String receivedBody = (String) receivedMessage.getBody();

		Assert.assertEquals(sendBody, receivedBody);
		Assert.assertEquals(sendMessage.getMessageId(), receivedMessage.getMessageId());
		Map<String,Object> receivedProperties = receivedMessage.getProperties();
		TestUtilities.checkNeedleInHaystack(receivedProperties, properties);
		
		session.close();
		connection.close();
	}

	@Test
	public void sendMultipleMessagesTest() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession session = connection.createMessagingSession();
		session.open();

		Queue queue = session.createQueue(testConfiguration.getQueueName());

		int numMessages = 100;

		MessageConsumer consumer = session.createMessageConsumer(queue);
		MessageReceiver messageReceiver = MessageReceiver.receiveMessagesAsync(consumer, 100, 20000L);

		String sendBody = "Hello, world!";

		MessageProducer producer = session.createMessageProducer(queue);
		for (int i=0; i<numMessages; ++i) {
			Message sendMessage = Message.T.create();
			sendMessage.setBody(sendBody);

			producer.sendMessage(sendMessage);
		}

		messageReceiver.waitFor();
		List<Message> receivedMessages = messageReceiver.getReceivedMessages();
		Assert.assertEquals(numMessages, receivedMessages.size());

		Message receivedMessage = receivedMessages.get(0);
		String receivedBody = (String) receivedMessage.getBody();

		Assert.assertEquals(sendBody, receivedBody);

		session.close();
		connection.close();
	}
	
	@Test
	public void sendMultipleMessagesTestMultiSessions() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession receivingSession = connection.createMessagingSession();
		receivingSession.open();

		Queue queue = receivingSession.createQueue(testConfiguration.getQueueName());

		int numMessages = 100;

		MessageConsumer consumer = receivingSession.createMessageConsumer(queue);
		MessageReceiver messageReceiver = MessageReceiver.receiveMessagesAsync(consumer, 100, 60000L);

		String sendBody = "Hello, world!";

		for (int i=0; i<numMessages; ++i) {
			
			MessagingSession sendingSession = connection.createMessagingSession();
			sendingSession.open();
			this.sessions.add(sendingSession);
			
			MessageProducer producer = sendingSession.createMessageProducer(queue);
			
			Message sendMessage = Message.T.create();
			sendMessage.setBody(sendBody);

			producer.sendMessage(sendMessage);
		}

		messageReceiver.waitFor();
		List<Message> receivedMessages = messageReceiver.getReceivedMessages();
		Assert.assertEquals(numMessages, receivedMessages.size());

		Message receivedMessage = receivedMessages.get(0);
		String receivedBody = (String) receivedMessage.getBody();

		Assert.assertEquals(sendBody, receivedBody);

		receivingSession.close();
		connection.close();
	}	
	
	@Test
	public void sendMultipleMessagesTestMultiConnections() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession receivingSession = connection.createMessagingSession();
		receivingSession.open();

		Queue queue = receivingSession.createQueue(testConfiguration.getQueueName());

		int numMessages = 100;

		MessageConsumer consumer = receivingSession.createMessageConsumer(queue);
		MessageReceiver messageReceiver = MessageReceiver.receiveMessagesAsync(consumer, 100, 30000L);

		String sendBody = "Hello, world!";

		for (int i=0; i<numMessages; ++i) {
			
			MessagingConnection sendingConnection = connectionProvider.provideMessagingConnection();
			MessagingSession sendingSession = sendingConnection.createMessagingSession();
			sendingSession.open();
			this.connections.add(sendingConnection);
			this.sessions.add(sendingSession);
			
			MessageProducer producer = sendingSession.createMessageProducer(queue);
			
			Message sendMessage = Message.T.create();
			sendMessage.setBody(sendBody);

			producer.sendMessage(sendMessage);
		}

		messageReceiver.waitFor();
		List<Message> receivedMessages = messageReceiver.getReceivedMessages();
		Assert.assertEquals(numMessages, receivedMessages.size());

		Message receivedMessage = receivedMessages.get(0);
		String receivedBody = (String) receivedMessage.getBody();

		Assert.assertEquals(sendBody, receivedBody);

		receivingSession.close();
		connection.close();
	}		
	
	@Test
	public void sendMessagesToTopicTest() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession session = connection.createMessagingSession();
		session.open();

		Topic topic = session.createTopic(testConfiguration.getTopicName());

		int numMessages = 100;
		int numConsumer = 10;
		MessageReceiver[] messageReceivers = new MessageReceiver[numConsumer];
		
		for (int i=0; i<numConsumer; ++i) {
			
			MessagingConnection receivingConnection = connectionProvider.provideMessagingConnection();
			MessagingSession receivingSession = receivingConnection.createMessagingSession();
			receivingSession.open();
			this.connections.add(receivingConnection);
			this.sessions.add(receivingSession);
			
			MessageConsumer consumer = session.createMessageConsumer(topic);
			MessageReceiver messageReceiver = MessageReceiver.receiveMessagesAsync(consumer, 100, 120000L);
			messageReceivers[i] = messageReceiver;
		}

		
		
		MessageProducer producer = session.createMessageProducer(topic);
		for (int i=0; i<numMessages; ++i) {
			Message sendMessage = Message.T.create();
			sendMessage.setBody(""+i);

			producer.sendMessage(sendMessage);
		}

		for (MessageReceiver messageReceiver : messageReceivers) {
			messageReceiver.waitFor();
			List<Message> receivedMessages = messageReceiver.getReceivedMessages();
			Assert.assertEquals(numMessages, receivedMessages.size());

			int i=0;
			for (Message receivedMessage : receivedMessages) {
				String receivedBody = (String) receivedMessage.getBody();
				Assert.assertEquals(""+i, receivedBody);
				i++;
			}
			
		}

		session.close();
		connection.close();
	}
	

	//@Test
	public void sendAndReceiveMessageInteractivelyTest() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession session = connection.createMessagingSession();
		session.open();

		TestUtilities.emptyQueue((JmsMqSession) session, testConfiguration.getQueueName());
		
		Queue queue = session.createQueue(testConfiguration.getQueueName());

		Message sendMessage = Message.T.create();
		String sendBody = "Hello, world!";
		sendMessage.setBody(sendBody);

		MessageProducer producer = session.createMessageProducer(queue);
		producer.sendMessage(sendMessage);

		System.out.println("Please restart the JMS engine at this point an press any key when done.");
		System.in.read();
		
		MessageConsumer consumer = session.createMessageConsumer(queue);
		List<Message> receivedMessages = MessageReceiver.receiveMessagesSync(consumer, 1, 30000L);

		Message receivedMessage = receivedMessages.get(0);
		String receivedBody = (String) receivedMessage.getBody();

		Assert.assertEquals(sendBody, receivedBody);
		
		session.close();
		connection.close();
	}
	
	//@Test
	public void registerMessageListenerInteractiveTest() throws Exception {

		MessagingConnectionProvider<?> connectionProvider = messaging.createConnectionProvider(configuration, context);
		MessagingConnection connection = connectionProvider.provideMessagingConnection();
		MessagingSession receivingSession = connection.createMessagingSession();
		receivingSession.open();

		TestUtilities.emptyQueue((JmsMqSession) receivingSession, testConfiguration.getQueueName());
		
		Queue queue = receivingSession.createQueue(testConfiguration.getQueueName());

		MessageConsumer consumer = receivingSession.createMessageConsumer(queue);
		consumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) throws MessagingException {
				System.out.println("Received message: "+message.getBody());
			}
		});

		System.out.println("Please restart the JMS engine at this point an press any key when done.");
		System.in.read();

		MessagingSession sendingSession = connection.createMessagingSession();
		sendingSession.open();
		
		Message sendMessage = Message.T.create();
		String sendBody = "Hello, world!";
		sendMessage.setBody(sendBody);

		MessageProducer producer = sendingSession.createMessageProducer(queue);
		producer.sendMessage(sendMessage);

		System.out.println("You should have received a message by now. Press any key when done.");
		System.in.read();

		sendingSession.close();
		receivingSession.close();
		connection.close();
	}
	
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
	public void testTopicMultithreadedMassive() throws Exception {
		
		MessagingConnectionProvider<? extends MessagingConnection> messagingConnectionProvider = JmsMqMessagingConnectionProvider.instance.get();

		MessagingConnection connection = messagingConnectionProvider.provideMessagingConnection();

		connection.open();

		MessagingSession session = connection.createMessagingSession();

		session.open();

		Topic topic = session.createTopic(testConfiguration.getTopicName());
		
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
}

