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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

/**
 * <p>
 * General base for message delivery tests.
 * 
 */
@Category(VerySlow.class)
public abstract class GmMessagingDeliveryTest extends GmMessagingTest {

	protected static int multipleMessagesQty = 1000;
	protected static int multipleConsumersQty = 10;
	protected static int multipleDestinationsQty = 10;
	protected static int multipleMessagesPerDestinationQty = 4;
	protected static int multipleConsumersPerDestinationQty = 4;

	public abstract Class<? extends Destination> getDestinationType();

	/**
	 * <p>
	 * This method can be overwritten by {@link GmMessagingDeliveryTest} concrete tests to ensure the consumers behavior
	 * upon marshalling errors.
	 */
	protected void sendUnmarshallableMessage(@SuppressWarnings("unused") MessageProducer messageProducer) {
		System.out.println(getClass().getSimpleName() + " did not implement sendUnmarshallableMessage()");
	}

	/**
	 * Tests the delivery of 1 persistent message to 1 destination.
	 */
	@Test
	public void testPersistent() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		Message sentMessage = createMessage(session, true, 0);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		messageProducer.sendMessage(sentMessage);

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(sentMessage), 1, false);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} persistent messages to 1 destination.
	 */
	@Test
	public void testPersistentBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		List<Message> messages = createMessages(session, multipleMessagesQty, true, 0);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		for (Message message : messages) {
			messageProducer.sendMessage(message);
		}

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, messages, 1, false);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 persistent and expired message to 1 destination.
	 */
	@Test
	public void testPersistentExpired() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		Message sentMessage = createMessage(session, true, 100);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		messageProducer.sendMessage(sentMessage);

		Thread.sleep(200);

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(sentMessage), 1, true);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} persistent and expired messages to 1 destination.
	 */
	@Test
	public void testPersistentExpiredBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		List<Message> messages = createMessages(session, multipleMessagesQty, true, 100);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		for (Message message : messages) {
			messageProducer.sendMessage(message);
		}

		Thread.sleep(200);

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, messages, 1, true);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 non-persistent message to 1 destination.
	 */
	@Test
	public void testNonPersistent() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		Message sentMessage = createMessage(session, false, 0);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		messageProducer.sendMessage(sentMessage);

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(sentMessage), 1, false);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination.
	 */
	@Test
	public void testNonPersistentBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		List<Message> messages = createMessages(session, multipleMessagesQty, false, 0);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		for (Message message : messages) {
			messageProducer.sendMessage(message);
		}

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, messages, 1, false);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 non-persistent and expired message to 1 destination.
	 */
	@Test
	public void testNonPersistentExpired() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		Message sentMessage = createMessage(session, false, 100);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		messageProducer.sendMessage(sentMessage);

		Thread.sleep(200);

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(sentMessage), 1, true);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent and expired messages to 1 destination.
	 */
	@Test
	public void testNonPersistentExpiredBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		List<Message> messages = createMessages(session, multipleMessagesQty, false, 100);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		for (Message message : messages) {
			messageProducer.sendMessage(message);
		}

		Thread.sleep(200);

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, messages, 1, true);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 non-persistent message to 1 destination with 1 consumer and 1 listener. Same session for
	 * consumer and producer.
	 */
	@Test
	public void testNonPersistentSingleConsumerListener() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), 1, false, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 persistent message to 1 destination with 1 consumer and 1 listener. Same session for
	 * consumer and producer.
	 */
	@Test
	public void testPersistentSingleConsumerListener() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), 1, true, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 non-persistent message to 1 destination with 1 consumer and 1 listener. Distinct sessions
	 * for consumer and producer.
	 */
	@Test
	public void testNonPersistentSingleConsumerListenerDistinctSessions() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, false, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 persistent message to 1 destination with 1 consumer and 1 listener. Distinct sessions for
	 * consumer and producer.
	 */
	@Test
	public void testPersistentSingleConsumerListenerDistinctSessions() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, true, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 non-persistent message to 1 destination with 1 consumer and 1 listener. Distinct
	 * connections for consumer and producer.
	 */
	@Test
	public void testNonPersistentSingleConsumerListenerDistinctConnections() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, false, 1);

		consumerConnection.close();
		producerConnection.close();
	}

	/**
	 * Tests the delivery of 1 persistent message to 1 destination with 1 consumer and 1 listener. Distinct connections
	 * for consumer and producer.
	 */
	@Test
	public void testPersistentSingleConsumerListenerDistinctConnections() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, true, 1);

		consumerConnection.close();
		producerConnection.close();
	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with 1 consumer and 1
	 * listener. Same session for consumer and producer.
	 */
	@Test
	public void testNonPersistentSingleConsumerListenerBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), multipleMessagesQty, false, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with 1 consumer and 1
	 * listener. Same session for consumer and producer.
	 */
	@Test
	public void testPersistentSingleConsumerListenerBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), multipleMessagesQty, true, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with 1 consumer and 1
	 * listener. Distinct sessions for consumer and producer.
	 */
	@Test
	public void testNonPersistentSingleConsumerListenerDistinctSessionsBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, false, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with 1 consumer and 1
	 * listener. Distinct sessions for consumer and producer.
	 */
	@Test
	public void testPersistentSingleConsumerListenerDistinctSessionsBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, true, 1);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with 1 consumer and 1
	 * listener. Distinct connections for consumer and producer.
	 */
	@Test
	public void testNonPersistentSingleConsumerListenerDistinctConnectionsBatch() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, false, 1);

		consumerConnection.close();
		producerConnection.close();
	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with 1 consumer and 1
	 * listener. Distinct connections for consumer and producer.
	 */
	@Test
	public void testPersistentSingleConsumerListenerDistinctConnectionsBatch() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, true, 1);

		consumerConnection.close();
		producerConnection.close();
	}

	/**
	 * Tests the delivery of 1 non-persistent message to 1 destination with {@link #multipleConsumersQty} consumers and
	 * 1 listener. Same session for consumer and producer.
	 */
	@Test
	public void testNonPersistentMultipleConsumersListener() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), 1, false, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 persistent message to 1 destination with {@link #multipleConsumersQty} consumers and 1
	 * listener. Same session for consumer and producer.
	 */
	@Test
	public void testPersistentMultipleConsumersListener() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), 1, true, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 non-persistent message to 1 destination with {@link #multipleConsumersQty} consumers and
	 * 1 listener. Distinct sessions for consumer and producer.
	 */
	@Test
	public void testNonPersistentMultipleConsumersListenerDistinctSessions() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, false, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 persistent message to 1 destination with {@link #multipleConsumersQty} consumers and 1
	 * listener. Distinct sessions for consumer and producer.
	 */
	@Test
	public void testPersistentMultipleConsumersListenerDistinctSessions() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, true, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of 1 non-persistent message to 1 destination with {@link #multipleConsumersQty} consumers and
	 * 1 listener. Distinct connections for consumer and producer.
	 */
	@Test
	public void testNonPersistentMultipleConsumersListenerDistinctConnections() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, false, multipleConsumersQty);

		consumerConnection.close();
		producerConnection.close();
	}

	/**
	 * Tests the delivery of 1 persistent message to 1 destination with {@link #multipleConsumersQty} consumers and 1
	 * listener. Distinct connections for consumer and producer.
	 */
	@Test
	public void testPersistentMultipleConsumersListenerDistinctConnections() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), 1, true, multipleConsumersQty);

		consumerConnection.close();
		producerConnection.close();
	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with
	 * {@link #multipleConsumersQty} consumers and 1 listener. Same session for consumer and producer.
	 */
	@Test
	@Category(Slow.class)
	public void testNonPersistentMultipleConsumersListenerBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), multipleMessagesQty, false, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with
	 * {@link #multipleConsumersQty} consumers and 1 listener. Same session for consumer and producer.
	 */
	@Test
	@Category(Slow.class)
	public void testPersistentMultipleConsumersListenerBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		testMessageListenerDelivery(session, session, getMethodName(), multipleMessagesQty, true, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with
	 * {@link #multipleConsumersQty} consumers and 1 listener. Distinct sessions for consumer and producer.
	 */
	@Test
	@Category(Slow.class)
	public void testNonPersistentMultipleConsumersListenerDistinctSessionsBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, false, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with
	 * {@link #multipleConsumersQty} consumers and 1 listener. Distinct sessions for consumer and producer.
	 */
	@Test
	@Category(Slow.class)
	public void testPersistentMultipleConsumersListenerDistinctSessionsBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = connection.createMessagingSession();
		MessagingSession producerSession = connection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, true, multipleConsumersQty);

		connection.close();

	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with
	 * {@link #multipleConsumersQty} consumers and 1 listener. Distinct connections for consumer and producer.
	 */
	@Test
	public void testNonPersistentMultipleConsumersListenerDistinctConnectionsBatch() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, false, multipleConsumersQty);

		consumerConnection.close();
		producerConnection.close();
	}

	/**
	 * Tests the delivery of {@link #multipleMessagesQty} non-persistent messages to 1 destination with
	 * {@link #multipleConsumersQty} consumers and 1 listener. Distinct connections for consumer and producer.
	 */
	@Test
	public void testPersistentMultipleConsumersListenerDistinctConnectionsBatch() throws Exception {

		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession consumerSession = consumerConnection.createMessagingSession();
		MessagingSession producerSession = producerConnection.createMessagingSession();

		testMessageListenerDelivery(consumerSession, producerSession, getMethodName(), multipleMessagesQty, true, multipleConsumersQty);

		consumerConnection.close();
		producerConnection.close();
	}

	@Test
	public void testReceive() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		// Creating consumer before sendMessage();

		ReceiverMessageListener messageListener = new ReceiverMessageListener();
		ReceiverJob receiverJob = new ReceiverJob(messageConsumer, messageListener);

		ExecutorService executorService = submitReceiver(messageListener, receiverJob);

		// persistent message which will never expire
		Message sentMessage = createMessage(session, true, 0);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		messageProducer.sendMessage(sentMessage);

		try {
			assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(sentMessage), 1, false);
		} finally {
			executorService.shutdownNow();
			connection.close();
		}

	}

	@Test
	public void testReceiveBatch() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();
		ReceiverJob receiverJob = new ReceiverJob(messageConsumer, messageListener);

		ExecutorService executorService = submitReceiver(messageListener, receiverJob);

		// {@link #multipleMessagesQty} persistent messages that will expire in 5 seconds.
		List<Message> messages = createMessages(session, multipleMessagesQty, true, 5000);

		MessageProducer messageProducer = session.createMessageProducer(destination);

		for (Message message : messages) {
			messageProducer.sendMessage(message);
		}

		try {
			assertDeliveries(getMethodName(), destination.getClass(), messageListener, messages, 1, false);
		} finally {
			executorService.shutdownNow();
			connection.close();
		}

	}

	@Test
	public void testCorrelation() throws Exception {

		final MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		final MessagingSession session = connection.createMessagingSession();
		final MessagingSession replySession = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());
		Destination reply = createDestination(getDestinationType(), replySession, getMethodName() + "-reply");

		MessageProducer destinationProducer = session.createMessageProducer(destination);
		final MessageProducer replyProducer = session.createMessageProducer(reply);

		MessageConsumer destinationConsumer = session.createMessageConsumer(destination);
		MessageConsumer replyConsumer = replySession.createMessageConsumer(reply);

		final Message outgoingMessage = createMessage(session);
		outgoingMessage.setBody("FOO");
		outgoingMessage.setCorrelationId(UUID.randomUUID().toString());
		outgoingMessage.setReplyTo(reply);
		outgoingMessage.setPersistent(true);

		destinationConsumer.setMessageListener(new MessageListener() {
			@Override
			public void onMessage(Message message) throws MessagingException {

				System.out.println("received " + message.getBody() + ", replying to " + message.getReplyTo().getName());

				Message replyMessage = session.createMessage();
				replyMessage.setBody("BAR");
				replyMessage.setCorrelationId(message.getCorrelationId());
				replyMessage.setPersistent(true);

				replyProducer.sendMessage(replyMessage);
				replyProducer.close();
			}
		});

		// sending a message before starting the consumer might result in discarded messages when the destination is
		// Topic.
		// making sure the reply consumer is initialized before sending the initial message:
		if (getDestinationType().equals(Topic.class)) {
			replyConsumer.receive(1);
		}

		destinationProducer.sendMessage(outgoingMessage);

		Message replyMessage = null;
		while ((replyMessage = replyConsumer.receive()) != null) {
			if (replyMessage.getCorrelationId().equals(outgoingMessage.getCorrelationId())) {
				System.out.println("received " + replyMessage.getBody() + ", which is correlated to " + outgoingMessage.getBody()
						+ " based on the correlation id " + replyMessage.getCorrelationId());
				break;
			}
		}

		connection.close();

	}

	@Test
	@Category(Slow.class)
	public void testMultipleDestinations() throws Exception {

		final MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		final MessagingSession session = connection.createMessagingSession();

		List<Destination> destinations = new ArrayList<Destination>();
		for (int i = 0; i < multipleDestinationsQty; i++) {
			destinations.add(createDestination(getDestinationType(), session, getMethodName(), i));
		}

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		for (int i = 0; i < multipleDestinationsQty; i++) {
			for (int j = 0; j < multipleConsumersPerDestinationQty; j++) {
				MessageConsumer consumer = session.createMessageConsumer(destinations.get(i));
				consumer.setMessageListener(messageListener);
			}
		}

		List<Message> messages = createMessages(session, multipleDestinationsQty * multipleMessagesPerDestinationQty);
		Iterator<Destination> destIter = destinations.iterator();

		// shared producer
		MessageProducer messageProducer = session.createMessageProducer(destinations.get(0));

		Destination destination = null;
		for (int i = 0; i < messages.size(); i++) {
			if (i == 0 || i % multipleMessagesPerDestinationQty == 0) {
				destination = destIter.next();
			}
			messageProducer.sendMessage(messages.get(i), destination);
		}

		assertDeliveries(getMethodName(), destinations.get(0).getClass(), messageListener, messages, multipleDestinationsQty,
				multipleConsumersPerDestinationQty, false);

		connection.close();

	}

	@Test
	public void testMultipleDestinationsSessionPerComponentType() throws Exception {

		final MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		final MessagingSession destinationsSession = connection.createMessagingSession();
		final MessagingSession messagesSession = connection.createMessagingSession();
		final MessagingSession consumersSession = connection.createMessagingSession();
		final MessagingSession producersSession = connection.createMessagingSession();

		List<Destination> destinations = new ArrayList<Destination>();
		for (int i = 0; i < multipleDestinationsQty; i++) {
			destinations.add(createDestination(getDestinationType(), destinationsSession, getMethodName(), i));
		}

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		for (int i = 0; i < multipleDestinationsQty; i++) {
			for (int j = 0; j < multipleConsumersPerDestinationQty; j++) {
				MessageConsumer consumer = consumersSession.createMessageConsumer(destinations.get(i));
				consumer.setMessageListener(messageListener);
			}
		}

		List<Message> messages = createMessages(messagesSession, multipleDestinationsQty * multipleMessagesPerDestinationQty);
		Iterator<Destination> destIter = destinations.iterator();

		// shared producer
		MessageProducer messageProducer = producersSession.createMessageProducer(destinations.get(0));

		Destination destination = null;
		for (int i = 0; i < messages.size(); i++) {
			if (i == 0 || i % multipleMessagesPerDestinationQty == 0) {
				destination = destIter.next();
			}
			messageProducer.sendMessage(messages.get(i), destination);
		}

		assertDeliveries(getMethodName(), destinations.get(0).getClass(), messageListener, messages, multipleDestinationsQty,
				multipleConsumersPerDestinationQty, false);

		connection.close();

	}

	@Test
	@Category(Slow.class)
	public void testMultipleDestinationsSessionPerComponent() throws Exception {

		final MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		List<Destination> destinations = new ArrayList<Destination>();
		for (int i = 0; i < multipleDestinationsQty; i++) {
			destinations.add(createDestination(getDestinationType(), connection.createMessagingSession(), getMethodName(), i));
		}

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		for (int i = 0; i < multipleDestinationsQty; i++) {
			for (int j = 0; j < multipleConsumersPerDestinationQty; j++) {
				MessageConsumer consumer = connection.createMessagingSession().createMessageConsumer(destinations.get(i));
				consumer.setMessageListener(messageListener);
			}
		}

		List<Message> messages = createMessages(connection.createMessagingSession(), multipleDestinationsQty * multipleMessagesPerDestinationQty);
		Iterator<Destination> destIter = destinations.iterator();

		// shared producer
		MessageProducer messageProducer = connection.createMessagingSession().createMessageProducer(destinations.get(0));

		Destination destination = null;
		for (int i = 0; i < messages.size(); i++) {
			if (i == 0 || i % multipleMessagesPerDestinationQty == 0) {
				destination = destIter.next();
			}
			messageProducer.sendMessage(messages.get(i), destination);
		}

		assertDeliveries(getMethodName(), destinations.get(0).getClass(), messageListener, messages, multipleDestinationsQty,
				multipleConsumersPerDestinationQty, false);

		connection.close();

	}

	@Test
	@Category(Slow.class)
	public void testMultipleDestinationsConnectionPerComponentType() throws Exception {

		final MessagingConnection destinationsConnection = getMessagingConnectionProvider().provideMessagingConnection();
		final MessagingConnection messagesConnection = getMessagingConnectionProvider().provideMessagingConnection();
		final MessagingConnection consumersConnection = getMessagingConnectionProvider().provideMessagingConnection();
		final MessagingConnection producersConnection = getMessagingConnectionProvider().provideMessagingConnection();

		final MessagingSession destinationsSession = destinationsConnection.createMessagingSession();
		final MessagingSession messagesSession = messagesConnection.createMessagingSession();
		final MessagingSession consumersSession = consumersConnection.createMessagingSession();
		final MessagingSession producersSession = producersConnection.createMessagingSession();

		List<Destination> destinations = new ArrayList<Destination>();
		for (int i = 0; i < multipleDestinationsQty; i++) {
			destinations.add(createDestination(getDestinationType(), destinationsSession, getMethodName(), i));
		}

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		for (int i = 0; i < multipleDestinationsQty; i++) {
			for (int j = 0; j < multipleConsumersPerDestinationQty; j++) {
				MessageConsumer consumer = consumersSession.createMessageConsumer(destinations.get(i));
				consumer.setMessageListener(messageListener);
			}
		}

		List<Message> messages = createMessages(messagesSession, multipleDestinationsQty * multipleMessagesPerDestinationQty);
		Iterator<Destination> destIter = destinations.iterator();

		// shared producer
		MessageProducer messageProducer = producersSession.createMessageProducer(destinations.get(0));

		Destination destination = null;
		for (int i = 0; i < messages.size(); i++) {
			if (i == 0 || i % multipleMessagesPerDestinationQty == 0) {
				destination = destIter.next();
			}
			messageProducer.sendMessage(messages.get(i), destination);
		}

		assertDeliveries(getMethodName(), destinations.get(0).getClass(), messageListener, messages, multipleDestinationsQty, multipleConsumersPerDestinationQty, false);

		destinationsConnection.close();
		messagesConnection.close();
		consumersConnection.close();
		producersConnection.close();

	}

	@Test
	@Category(Slow.class)
	public void testMultipleDestinationTypes() throws Exception {

		final MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		final MessagingSession session = connection.createMessagingSession();

		// creates topics and queues from the same session
		Class<? extends Destination> otherDestType = (Topic.class.isAssignableFrom(getDestinationType())) ? Queue.class : Topic.class;

		List<Destination> destinationsThis = new ArrayList<Destination>();
		List<Destination> destinationsOther = new ArrayList<Destination>();
		for (int i = 0; i < multipleDestinationsQty; i++) {
			destinationsThis.add(createDestination(getDestinationType(), session, getMethodName(), i));
			destinationsOther.add(createDestination(otherDestType, session, getMethodName(), i));
		}

		ReceiverMessageListener messageListenerThis = new ReceiverMessageListener();
		ReceiverMessageListener messageListenerOther = new ReceiverMessageListener();

		for (int i = 0; i < multipleDestinationsQty; i++) {
			for (int j = 0; j < multipleConsumersPerDestinationQty; j++) {
				MessageConsumer consumerThis = session.createMessageConsumer(destinationsThis.get(i));
				consumerThis.setMessageListener(messageListenerThis);
				MessageConsumer consumerOther = session.createMessageConsumer(destinationsOther.get(i));
				consumerOther.setMessageListener(messageListenerOther);
			}
		}

		int messages = multipleDestinationsQty * multipleMessagesPerDestinationQty;

		List<Message> messagesThis = createMessages(session, messages);
		List<Message> messagesOther = createMessages(session, messages);

		for (int i = 0, d = 0; i < messages; i++, d++) {
			if (d % multipleDestinationsQty == 0) {
				d = 0;
			}
			session.createMessageProducer(destinationsThis.get(d)).sendMessage(messagesThis.get(i));
			session.createMessageProducer(destinationsOther.get(d)).sendMessage(messagesOther.get(i));
		}

		assertDeliveries(getMethodName(), getDestinationType(), messageListenerThis, messagesThis, multipleDestinationsQty, multipleConsumersPerDestinationQty, false);
		assertDeliveries(getMethodName(), otherDestType, messageListenerOther, messagesOther, multipleDestinationsQty, multipleConsumersPerDestinationQty, false);

		connection.close();

	}

	@Test
	public void testConcurrentDelivery() throws Throwable {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		try {

			MessagingSession session = connection.createMessagingSession();

			Destination destination = createDestination(getDestinationType(), session, getMethodName());

			MessageConsumer messageConsumer1 = session.createMessageConsumer(destination);
			MessageConsumer messageConsumer2 = session.createMessageConsumer(destination);
			MessageConsumer messageConsumer3 = session.createMessageConsumer(destination);

			List<Message> messages = createMessages(session, multipleMessagesQty);

			CallableMessageListener messageListener = new CallableMessageListener(destination, 3, messages, outputEnabled);
			messageConsumer1.setMessageListener(messageListener);
			messageConsumer2.setMessageListener(messageListener);
			messageConsumer3.setMessageListener(messageListener);

			MessageProducer messageProducer = session.createMessageProducer(destination);

			for (Message message : messages) {
				messageProducer.sendMessage(message);
			}

			assertDeliveries(messageListener);

		} finally {
			connection.close();
		}

	}

	@Test
	public void testUnidentifiedProducer() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		try {

			MessagingSession session = connection.createMessagingSession();

			Destination destination = createDestination(getDestinationType(), session, getMethodName());

			MessageConsumer messageConsumer = session.createMessageConsumer(destination);

			ReceiverMessageListener messageListener = new ReceiverMessageListener();

			messageConsumer.setMessageListener(messageListener);

			Message sentMessage = createMessage(session, true, 0);

			MessageProducer messageProducer = session.createMessageProducer();

			messageProducer.sendMessage(sentMessage, destination);

			assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(sentMessage), 1, false);

		} finally {
			connection.close();
		}

	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnidentifiedProducerSupport() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		try {

			MessagingSession session = connection.createMessagingSession();

			Message sentMessage = createMessage(session, true, 0);

			// no Destination is provided, neither on createMessageProducer() nor on sendMessage()

			MessageProducer messageProducer = session.createMessageProducer();

			messageProducer.sendMessage(sentMessage);

		} finally {
			connection.close();
		}

	}

	@Test
	public void testIdentifiedProducer() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		try {

			MessagingSession session = connection.createMessagingSession();

			Destination destinationA = createDestination(getDestinationType(), session, getMethodName(), 1);
			Destination destinationB = createDestination(getDestinationType(), session, getMethodName(), 2);
			Destination destinationC = createDestination(getDestinationType(), session, getMethodName(), 3);

			MessageConsumer consumerA = session.createMessageConsumer(destinationA);
			MessageConsumer consumerB = session.createMessageConsumer(destinationB);
			MessageConsumer consumerC = session.createMessageConsumer(destinationC);

			ReceiverMessageListener listenerA = new ReceiverMessageListener();
			ReceiverMessageListener listenerB = new ReceiverMessageListener();
			ReceiverMessageListener listenerC = new ReceiverMessageListener();

			consumerA.setMessageListener(listenerA);
			consumerB.setMessageListener(listenerB);
			consumerC.setMessageListener(listenerC);

			Message message1 = createMessage(session, true, 0);
			Message message2 = createMessage(session, true, 0);
			Message message3 = createMessage(session, true, 0);
			Message message4 = createMessage(session, true, 0);

			MessageProducer messageProducer = session.createMessageProducer(destinationA);

			messageProducer.sendMessage(message1);
			messageProducer.sendMessage(message2, destinationA);
			messageProducer.sendMessage(message3, destinationB);
			messageProducer.sendMessage(message4, destinationC);

			assertDeliveries(getMethodName(), getDestinationType(), listenerA, Arrays.asList(message1, message2), 1, false);
			assertDeliveries(getMethodName(), getDestinationType(), listenerB, Arrays.asList(message3), 1, false);
			assertDeliveries(getMethodName(), getDestinationType(), listenerC, Arrays.asList(message4), 1, false);

		} finally {
			connection.close();
		}

	}

	@Test
	public void testAddresseeProperty() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageProducer messageProducer = session.createMessageProducer(destination);

		final MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		MessagingContext messagingContext = getMessagingContext();
		String appId = messagingContext.getApplicationId();
		String nodeId = messagingContext.getNodeId();

		String unknownAppId = "randomAppId-" + System.currentTimeMillis();
		String unknownNodeId = "randomNodeId-" + System.currentTimeMillis();

		List<Message> matching = new ArrayList<>();
		List<Message> unmatching = new ArrayList<>();

		matching.add(createAddressedMessage(session, null, null));
		matching.add(createAddressedMessage(session, appId, null));
		matching.add(createAddressedMessage(session, null, nodeId));
		matching.add(createAddressedMessage(session, appId, nodeId));

		unmatching.add(createAddressedMessage(session, unknownAppId, null));
		unmatching.add(createAddressedMessage(session, null, unknownNodeId));
		unmatching.add(createAddressedMessage(session, unknownAppId, unknownNodeId));

		for (Message message : matching) {
			messageProducer.sendMessage(message);
		}

		for (Message message : unmatching) {
			messageProducer.sendMessage(message);
		}

		if (destination instanceof Queue) {
			// addreseeAppId/addreseeNodeId is ignored for Queues. It works only for publish/subscribe model (Topics).
			matching.addAll(unmatching);
		}

		assertDeliveries(getMethodName(), destination.getClass(), messageListener, matching, 1, false);

	}

	/**
	 * <p>
	 * Test whether a consumer keeps its subscription upon punctual marshalling failures.
	 */
	@Test
	public void testMarshallingFailureResilience() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageProducer messageProducer = session.createMessageProducer(destination);

		final MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		messageConsumer.setMessageListener(messageListener);

		Message message1 = createMessage(session, true, 0);

		messageProducer.sendMessage(message1);

		sendUnmarshallableMessage(messageProducer);

		Message message2 = createMessage(session, true, 0);

		messageProducer.sendMessage(message2);

		assertDeliveries(getMethodName(), getDestinationType(), messageListener, Arrays.asList(message1, message2), 1, false);

	}

	/**
	 * <p>
	 * Test whether a consumer keeps its subscription upon punctual marshalling failures.
	 */
	@Test
	public void testMarshallingFailureResilienceWithBlockingConsumption() throws Exception {

		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();

		MessagingSession session = connection.createMessagingSession();

		Destination destination = createDestination(getDestinationType(), session, getMethodName());

		MessageProducer messageProducer = session.createMessageProducer(destination);

		final MessageConsumer messageConsumer = session.createMessageConsumer(destination);

		// starting the consumption before producing messages in order to fetch otherwise lost topic messages.
		ReceiverMessageListener messageListener = new ReceiverMessageListener();
		ReceiverJob receiverJob = new ReceiverJob(messageConsumer, messageListener);

		ExecutorService executorService = submitReceiver(messageListener, receiverJob);

		// persistent message which will never expire
		Message message1out = createMessage(session, true, 0);

		messageProducer.sendMessage(message1out);

		// unmarshallable message which shouldn't invalidate the consumer
		sendUnmarshallableMessage(messageProducer);

		// a second persistent message which will never expire
		Message message2out = createMessage(session, true, 0);

		messageProducer.sendMessage(message2out);

		try {
			assertDeliveries(getMethodName(), destination.getClass(), messageListener, Arrays.asList(message1out, message2out), 1, false);
		} finally {
			executorService.shutdownNow();
			connection.close();
		}

	}

	protected Message createAddressedMessage(MessagingSession session, String appId, String nodeId) throws Exception {
		Message message = createMessage(session, false, 0);
		if (appId != null) {
			message.getProperties().put(MessageProperties.addreseeAppId.getName(), appId);
		}
		if (nodeId != null) {
			message.getProperties().put(MessageProperties.addreseeNodeId.getName(), nodeId);
		}
		return message;
	}

	protected void testMessageListenerDelivery(MessagingSession consumerSession, MessagingSession producerSession, String methodName, int messagesQty,
			boolean persistent, int consumers) throws Exception {

		Destination destination = createDestination(getDestinationType(), consumerSession, methodName);

		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		List<MessageConsumer> consumerList = new ArrayList<MessageConsumer>();
		for (int i = 0; i < consumers; i++) {
			MessageConsumer messageConsumer = consumerSession.createMessageConsumer(destination);
			messageConsumer.setMessageListener(messageListener);
			consumerList.add(messageConsumer);
		}

		List<Message> messages = createMessages(consumerSession, messagesQty, persistent, 0);

		MessageProducer messageProducer = producerSession.createMessageProducer(destination);

		for (Message message : messages) {
			messageProducer.sendMessage(message);
		}

		assertDeliveries(methodName, destination.getClass(), messageListener, messages, consumers, false);

	}

}
