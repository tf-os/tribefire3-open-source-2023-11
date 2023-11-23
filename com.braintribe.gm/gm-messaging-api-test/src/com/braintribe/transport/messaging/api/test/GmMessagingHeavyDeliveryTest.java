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

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingSession;

/**
 * <p>
 * {@code GmMessagingApi} tests intended to produce a heavy load of messages. Not necessarily a stress test, but can be
 * configured to behave as such.
 * 
 */
@SuppressWarnings("unchecked")
@Category(VerySlow.class)
public abstract class GmMessagingHeavyDeliveryTest extends GmMessagingTest {

	// Configurable test parameters
	protected static int maxConcurrentTests = 10;
	protected static int multipleConsumersQty = 10;
	protected static int multipleMessagesQty = 1500;
	protected static int expirableMessagesTimeToLive = 60000; //60 seconds
	protected static int expiredMessagesTimeToLive = 5000; //5 seconds

	// Configurable test parameters
	protected static Set<Class<? extends Destination>> enabledDestinations = asSet( Topic.class, Queue.class );
	protected static Set<ConsumptionSetup> enabledConsumptionModes = asSet( ConsumptionSetup.Sync, ConsumptionSetup.Async );
	protected static Set<ConsumptionDistributionSetup> enabledConsumptionDistributionModes = asSet( ConsumptionDistributionSetup.SingleConsumer, ConsumptionDistributionSetup.MultipleConsumers, ConsumptionDistributionSetup.NoConsumer );
	protected static Set<MessagesPersistenceSetup> enabledPersistenceModes = asSet( MessagesPersistenceSetup.Persistent, MessagesPersistenceSetup.NonPersistent );
	protected static Set<MessagesExpirationSetup> enabledExpirationModes = asSet( MessagesExpirationSetup.Expirable, MessagesExpirationSetup.NeverExpiring, MessagesExpirationSetup.Expired );
	protected static Set<MessagesQuantitySetup> enabledDeliveryModes = asSet( MessagesQuantitySetup.SingleMessage, MessagesQuantitySetup.MultipleMessages, MessagesQuantitySetup.NoMessage );
	protected static Set<SessionDistributionMode> enabledSessionDistributionModes = asSet( SessionDistributionMode.SingleSession, SessionDistributionMode.MultipleSessions, SessionDistributionMode.MultipleConnections );

	private List<Class<? extends Destination>> destinationTypes = Arrays.asList(Topic.class, Queue.class);
	
	public enum ConsumptionSetup {
		Sync, Async;
	}
	
	public enum ConsumptionDistributionSetup {
		SingleConsumer, MultipleConsumers, NoConsumer;
	}

	public enum SessionDistributionMode {
		SingleSession, MultipleSessions, MultipleConnections;
	}
	
	public enum MessagesPersistenceSetup {
		Persistent, NonPersistent;
	}
	
	public enum MessagesExpirationSetup {
		Expirable, NeverExpiring, Expired;
	}
	
	public enum MessagesQuantitySetup {
		SingleMessage, MultipleMessages, NoMessage;
	}
	
	public enum DeliverySetup {
		SequentialDelivery, ConcurrentDelivery;
	}
	
//	public static void main(String[] args) {
//		for (ConsumptionSetup consumptionMode : ConsumptionSetup.values()) {
//			for (ConsumptionDistributionSetup consumptionDistributionMode : ConsumptionDistributionSetup.values()) {
//				for (MessagesPersistenceSetup persistenceMode : MessagesPersistenceSetup.values()) {
//					for (MessagesExpirationSetup expirationMode : MessagesExpirationSetup.values()) {
//						for (MessagesQuantitySetup deliverySetup : MessagesQuantitySetup.values()) {
//							for (SessionDistributionMode sessionDistributionMode : SessionDistributionMode.values()) {
//
//
//								
//								String method = "@Test\n";
//								method+= "public void test" + consumptionMode.toString();
//								method+= consumptionDistributionMode.toString();
//								method+= persistenceMode.toString();
//								method+= expirationMode.toString();
//								method+= deliverySetup.toString();
//								method+= sessionDistributionMode.toString();
//								method+= "()  throws Exception {\n";
//								method+= "\ttestDelivery(";
//								method+= "destinationType, getMethodName(), ";
//								method+= ConsumptionSetup.class.getSimpleName()+"."+consumptionMode.toString()+", ";
//								method+= ConsumptionDistributionSetup.class.getSimpleName()+"."+consumptionDistributionMode.toString()+", ";
//								method+= MessagesPersistenceSetup.class.getSimpleName()+"."+persistenceMode.toString()+", ";
//								method+= MessagesExpirationSetup.class.getSimpleName()+"."+expirationMode.toString()+", ";
//								method+= MessagesQuantitySetup.class.getSimpleName()+"."+deliverySetup.toString()+", ";
//								method+= SessionDistributionMode.class.getSimpleName()+"."+sessionDistributionMode.toString();
//								method+= ");\n";
//								method+= "}\n";
//								System.out.println(method);
//							}
//						}
//					}
//				}
//			}
//		}
//	}
	
	//@Test 
	public void testSequentially() throws Throwable {

		List<DeliveryTest> tests = generateTests();
		
		long s = System.currentTimeMillis();
		System.out.println("Starting "+tests.size()+" tests sequentially.");
		
		for (DeliveryTest test : tests) {
			test.test();
		}
		
		System.out.println("Executed "+tests.size()+" tests sequentially in "+(System.currentTimeMillis()-s)+" ms");
		
	}
	
	@Test
	@Category(VerySlow.class)
	public void testConcurrently() throws Throwable {
		
		List<DeliveryTest> tests = generateTests();
		
		ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentTests);
		
		long s = System.currentTimeMillis();
		System.out.println("Starting "+tests.size()+" tests concurrently.");
		
		List<Future<Throwable>> results = executorService.invokeAll(tests);
		
		System.out.println("Executed "+tests.size()+" tests concurrently in "+(System.currentTimeMillis()-s)+" ms");
		
		List<Throwable> failures = new ArrayList<Throwable>();
		
		for (Future<Throwable> future : results) {
			if (future.get() != null) {
				failures.add(future.get());
			}
		}
		
		if (!failures.isEmpty()) {
			for (Throwable failure : failures) {
				failure.printStackTrace();
			}
			throw failures.get(0);
		}

	}
	
	
	private List<DeliveryTest> generateTests() {
		
		List<DeliveryTest> tests = new ArrayList<DeliveryTest>();
		
		for (Class<? extends Destination> destType : destinationTypes) {
			for (ConsumptionSetup consumption : ConsumptionSetup.values()) {
				for (ConsumptionDistributionSetup consumptionDist : ConsumptionDistributionSetup.values()) {
					for (SessionDistributionMode sessionDist : SessionDistributionMode.values()) {
						for (MessagesPersistenceSetup persistence : MessagesPersistenceSetup.values()) {
							for (MessagesExpirationSetup expiration : MessagesExpirationSetup.values()) {
								for (MessagesQuantitySetup messages : MessagesQuantitySetup.values()) {
									for (DeliverySetup delivery : DeliverySetup.values()) {
										if (!skipCombination(destType, consumption, consumptionDist, sessionDist, persistence, expiration, messages, delivery)) {
											tests.add(new DeliveryTest(destType, consumption, consumptionDist, sessionDist, persistence, expiration, messages, delivery));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		return tests;
	}
	
	@SuppressWarnings("unused")
	private static boolean skipCombination(Class<? extends Destination> destinationType, ConsumptionSetup consumptionSetup, ConsumptionDistributionSetup consumptionDistributionSetup, SessionDistributionMode sessionDistributionMode, MessagesPersistenceSetup messagesPersistenceSetup, MessagesExpirationSetup messagesExpirationSetup, MessagesQuantitySetup messagesQuantitySetup, DeliverySetup deliverySetup) {
		
		//stip some unecessary test when there are no consumers:
		if (consumptionDistributionSetup == ConsumptionDistributionSetup.NoConsumer) {
			
			//avoid generating tests with no consumers and never expiring messages multiple messages
			if (messagesQuantitySetup == MessagesQuantitySetup.MultipleMessages && messagesExpirationSetup == MessagesExpirationSetup.NeverExpiring) {
				return true;
			}
			
			//generate only async when noconsumers, consumption mode is not important when there are no consumers.
			if (consumptionSetup != ConsumptionSetup.Async) {
				return true;
			}
			
			//no consumers and no messages doesnt need to be tested
			if (messagesQuantitySetup == MessagesQuantitySetup.NoMessage) {
				return true;
			}
			
			//only a type of delivery is enough when there is no consumers
			if (deliverySetup != DeliverySetup.SequentialDelivery) {
				return true;
			}
			
			//only single session makes sense
			if (sessionDistributionMode != SessionDistributionMode.SingleSession) {
				return true;
			}
			
		}
		
		if (messagesQuantitySetup == MessagesQuantitySetup.NoMessage) {
			//when the test has no messages, a single combination of persistence MessagesPersistenceSetup/MessagesExpirationSetup/DeliverySetup is enough
			if (messagesPersistenceSetup != MessagesPersistenceSetup.Persistent && messagesExpirationSetup != MessagesExpirationSetup.Expirable && deliverySetup != DeliverySetup.ConcurrentDelivery) {
				return true;
			}
		}
		
		if (deliverySetup == DeliverySetup.ConcurrentDelivery && messagesQuantitySetup != MessagesQuantitySetup.MultipleMessages) {
			//skip concurrent delivery if not multiple messages
			return true;
		}
		
		return false;
		
	}
	
	
	public class DeliveryTest implements Callable<Throwable> {
		
		private String testName;
		
		private Class<? extends Destination> destinationType;
		private ConsumptionSetup consumptionSetup;
		private ConsumptionDistributionSetup consumptionDistributionSetup;
		private SessionDistributionMode sessionDistributionMode;
		private MessagesPersistenceSetup messagesPersistenceSetup;
		private MessagesExpirationSetup messagesExpirationSetup;
		private MessagesQuantitySetup messagesQuantitySetup;
		private DeliverySetup deliverySetup;
		
		public DeliveryTest(Class<? extends Destination> destinationType,
				ConsumptionSetup consumptionSetup,
				ConsumptionDistributionSetup consumptionDistributionSetup,
				SessionDistributionMode sessionDistributionMode,
				MessagesPersistenceSetup messagesPersistenceSetup,
				MessagesExpirationSetup messagesExpirationSetup,
				MessagesQuantitySetup messagesQuantitySetup,
				DeliverySetup deliverySetup) {
			super();
			this.destinationType = destinationType;
			this.consumptionSetup = consumptionSetup;
			this.consumptionDistributionSetup = consumptionDistributionSetup;
			this.sessionDistributionMode = sessionDistributionMode;
			this.messagesPersistenceSetup = messagesPersistenceSetup;
			this.messagesExpirationSetup = messagesExpirationSetup;
			this.messagesQuantitySetup = messagesQuantitySetup;
			this.deliverySetup = deliverySetup;
			
			this.testName = "test"+destinationType.getSimpleName()+consumptionSetup+consumptionDistributionSetup+sessionDistributionMode+messagesPersistenceSetup+messagesExpirationSetup+messagesQuantitySetup+deliverySetup; 
			
		}
		
		@Override
		public Throwable call() throws Exception {
			return test(true);
		}

		@Override
		public String toString() {
			return testName;
		}
		
		public String getMethodBody() {
			//TODO: generate method body.
			return testName;
		}
		
		private void test() throws Throwable {
			Throwable t = test(false);
			if (t != null) {
				throw t;
			}
		}
		
		private Throwable test(boolean concurrent) {
			try {
				testDelivery(testName, destinationType, consumptionSetup, consumptionDistributionSetup, sessionDistributionMode, messagesPersistenceSetup, messagesExpirationSetup, messagesQuantitySetup, deliverySetup, concurrent);
			} catch (Throwable e) {
				System.out.println(testName+" failed with "+e.getClass().getSimpleName()+": "+e.getMessage());
				return e;
			}
			return null;
		}
		
	}
	
	@SuppressWarnings("unused")
	protected void testDelivery(String testName,
		Class<? extends Destination> destinationType,
		ConsumptionSetup consumptionSetup,
		ConsumptionDistributionSetup consumptionDistributionSetup,
		SessionDistributionMode sessionDistributionMode,
		MessagesPersistenceSetup messagesPersistenceSetup,
		MessagesExpirationSetup messagesExpirationSetup, 
		MessagesQuantitySetup messagesQuantitySetup,
		DeliverySetup deliverySetup,
		boolean concurrent)  throws Exception {
		
		long timeToLive = 0; //NeverExpiring default
		if (messagesExpirationSetup == MessagesExpirationSetup.Expired) {
			timeToLive = expiredMessagesTimeToLive;
		} else if (messagesExpirationSetup == MessagesExpirationSetup.Expirable) {
			timeToLive = expirableMessagesTimeToLive;
		}

		boolean waitExpiration = (messagesExpirationSetup == MessagesExpirationSetup.Expired);
		boolean persistent = (messagesPersistenceSetup == MessagesPersistenceSetup.Persistent);
		
		int consumers = 0;
		switch (consumptionDistributionSetup) {
			case SingleConsumer:
				consumers = 1;
				break;
			case MultipleConsumers:
				consumers = multipleConsumersQty;
				break;
			default:
				break;
		}
		
		int messages = 0;
		switch (messagesQuantitySetup) {
			case SingleMessage:
				messages = 1;
				break;
			case MultipleMessages:
				messages = multipleMessagesQty;
				break;
			default:
				break;
		}
		
		switch (sessionDistributionMode) {
			case SingleSession:
				testSingleSession(consumptionSetup, destinationType, testName, consumers, messages, persistent, timeToLive, waitExpiration, deliverySetup);
				break;
			case MultipleSessions:
				testMultipleSessions(consumptionSetup, destinationType, testName, consumers, messages, persistent, timeToLive, waitExpiration, deliverySetup);
				break;
			case MultipleConnections:
				testMultipleConnections(consumptionSetup, destinationType, testName, consumers, messages, persistent, timeToLive, waitExpiration, deliverySetup);
				break;
		}
	
	}
	
	protected boolean isEnabled(Class<? extends Destination> destinationType, ConsumptionSetup consumptionMode, ConsumptionDistributionSetup consumptionDistributionMode, MessagesPersistenceSetup persistenceMode, MessagesExpirationSetup expirationMode, MessagesQuantitySetup deliverySetup, SessionDistributionMode sessionDistributionMode) {
				
		String p = getMethodName(2)+" suppressed. ", s = " tests are disabled.";
		
		if (!enabledDestinations.contains(destinationType)) {
			System.out.println(p+destinationType.getSimpleName()+s);
			return false;
		}
		
		if (!enabledConsumptionModes.contains(consumptionMode)) {
			System.out.println(p+consumptionMode+s);
			return false;
		}
		
		if (!enabledConsumptionDistributionModes.contains(consumptionDistributionMode)) {
			System.out.println(p+consumptionDistributionMode+s);
			return false;
		}
		
		if (!enabledPersistenceModes.contains(persistenceMode)) {
			System.out.println(p+persistenceMode+s);
			return false;
		}
		
		if (!enabledExpirationModes.contains(expirationMode)) {
			System.out.println(p+expirationMode+s);
			return false;
		}
		
		if (!enabledDeliveryModes.contains(deliverySetup)) {
			System.out.println(p+deliverySetup+s);
			return false;
		}
		
		if (!enabledSessionDistributionModes.contains(sessionDistributionMode)) {
			System.out.println(p+sessionDistributionMode+s);
			return false;
		}
		
		return true;
	}
	
	protected void testSingleSession(ConsumptionSetup mode, Class<? extends Destination> destinationType, String testMethodName, int consumers, int messages, boolean persistent, long timeToLive, boolean waitUntilExpire, DeliverySetup deliverySetup) throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		try {
			
			MessagingSession session = connection.createMessagingSession();
			
			if (mode == ConsumptionSetup.Sync) {
				testSynchronousConsumption(session, session, destinationType, testMethodName, consumers, messages, persistent, timeToLive, waitUntilExpire, deliverySetup);
			} else if (mode == ConsumptionSetup.Async) {
				testAsynchronousConsumption(session, session, destinationType, testMethodName, consumers, messages, persistent, timeToLive, waitUntilExpire, deliverySetup);
			}
			
		} finally {
			connection.close();
		}
	
	}
	
	protected void testMultipleSessions(ConsumptionSetup mode, Class<? extends Destination> destinationType, String testMethodName, int consumers, int messages, boolean persistent, long timeToLive, boolean waitUntilExpire, DeliverySetup deliverySetup) throws Exception {
		
		MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		try {

			MessagingSession consumerSession = connection.createMessagingSession();
			MessagingSession producerSession = connection.createMessagingSession();
			
			if (mode == ConsumptionSetup.Sync) {
				testSynchronousConsumption(consumerSession, producerSession, destinationType, testMethodName, consumers, messages, persistent, timeToLive, waitUntilExpire, deliverySetup);
			} else if (mode == ConsumptionSetup.Async) {
				testAsynchronousConsumption(consumerSession, producerSession, destinationType, testMethodName, consumers, messages, persistent, timeToLive, waitUntilExpire, deliverySetup);
			}
			
		} finally {
			connection.close();
		}
		
	}
	
	protected void testMultipleConnections(ConsumptionSetup mode, Class<? extends Destination> destinationType, String testMethodName, int consumers, int messages, boolean persistent, long timeToLive, boolean waitUntilExpire, DeliverySetup deliverySetup) throws Exception {
		
		MessagingConnection consumerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		MessagingConnection producerConnection = getMessagingConnectionProvider().provideMessagingConnection();
		try {

			MessagingSession consumerSession = consumerConnection.createMessagingSession();
			MessagingSession producerSession = producerConnection.createMessagingSession();
			
			if (mode == ConsumptionSetup.Sync) {
				testSynchronousConsumption(consumerSession, producerSession, destinationType, testMethodName, consumers, messages, persistent, timeToLive, waitUntilExpire, deliverySetup);
			} else if (mode == ConsumptionSetup.Async) {
				testAsynchronousConsumption(consumerSession, producerSession, destinationType, testMethodName, consumers, messages, persistent, timeToLive, waitUntilExpire, deliverySetup);
			}
			
		} finally {
			try {
				consumerConnection.close();
			} finally {
				producerConnection.close();
			}
		}
		
	}
	
	private void testAsynchronousConsumption(MessagingSession consumerSession, MessagingSession producerSession, Class<? extends Destination> destinationType, String testMethodName, int consumers, int messages, boolean persistent, long timeToLive, boolean waitUntilExpire, DeliverySetup deliverySetup) throws Exception {
		
		Destination destination = createDestination(destinationType, consumerSession, testMethodName);

		List<MessageConsumer> consumerList = new ArrayList<MessageConsumer>();
		ReceiverMessageListener messageListener = new ReceiverMessageListener();
		
		boolean createConsumerAfterSending = ((consumers == 1 && Queue.class.isAssignableFrom(destinationType)) || waitUntilExpire);
		
		//For multiple consumers, topics or tests waiting for expiration: consumers must pre-exist  before 
		//calling sendMessage() for assertDeliveries() to work with exact predicted deliveries
		if (!createConsumerAfterSending && consumers > 0) {
			for (int i = 0; i < consumers; i++) {
				MessageConsumer messageConsumer = consumerSession.createMessageConsumer(destination);
				messageConsumer.setMessageListener(messageListener);
				consumerList.add(messageConsumer);
			}
		}

		List<Message> outgoingMessages = createMessages(producerSession, messages, persistent, timeToLive);
			
		MessageProducer messageProducer = producerSession.createMessageProducer(destination);
		
		if (deliverySetup == DeliverySetup.ConcurrentDelivery) {
			sendAsynchronously(messageProducer, outgoingMessages);
		} else {
			for (Message message : outgoingMessages) {
				messageProducer.sendMessage(message);
			}
		}
			

		//For single consumers of queue(s): consumers can be registered after calling sendMessage() 
		//and assertDeliveries() will still work with exact predicted deliveries. 
		//Also if waitUntilExpire was set to true, it only makes sense to register the consumers after calling sendMessage() 
		if (createConsumerAfterSending) {
			
			if (waitUntilExpire) {
				Thread.sleep(timeToLive+2000);
			}
			
			for (int i = 0; i < consumers; i++) {
				MessageConsumer messageConsumer = consumerSession.createMessageConsumer(destination);
				messageConsumer.setMessageListener(messageListener);
				consumerList.add(messageConsumer);
			}
		}
		

		assertDeliveries(testMethodName, destinationType, messageListener, outgoingMessages, consumers, waitUntilExpire);
		
	}
	
	private void testSynchronousConsumption(MessagingSession consumerSession, MessagingSession producerSession, Class<? extends Destination> destinationType, String testMethodName, int consumers, int messages, boolean persistent, long timeToLive, boolean waitUntilExpire, DeliverySetup deliverySetup) throws Exception {
		
		Destination destination = createDestination(destinationType, consumerSession, testMethodName);
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		ReceiverMessageListener messageListener = new ReceiverMessageListener();

		boolean createConsumerAfterSending = ((consumers == 1 && Queue.class.isAssignableFrom(destinationType)) || waitUntilExpire);

		//For multiple consumers, topics or tests waiting for expiration: consumers must pre-exist  before 
		//calling sendMessage() for assertDeliveries() to work with exact predicted deliveries
		if (!createConsumerAfterSending && consumers > 0) {
			executorService = submitReceiver(messageListener, createReceiverJobs(destination, consumerSession, messageListener, consumers));
		}

		List<Message> outgoingMessages = createMessages(producerSession, messages, persistent, timeToLive);
			
		MessageProducer messageProducer = producerSession.createMessageProducer(destination);
		
		if (deliverySetup == DeliverySetup.ConcurrentDelivery) {
			sendAsynchronously(messageProducer, outgoingMessages);
		} else {
			for (Message message : outgoingMessages) {
				messageProducer.sendMessage(message);
			}
		}

		//For single consumers of queue(s): consumers can be registered after calling sendMessage() 
		//and assertDeliveries() will still work with exact predicted deliveries. 
		//Also if waitUntilExpire was set to true, it only makes sense to register the consumers after calling sendMessage() 
		if (createConsumerAfterSending) {
			
			if (waitUntilExpire) {
				Thread.sleep(timeToLive+2000);
			}

			executorService = submitReceiver(messageListener, createReceiverJobs(destination, consumerSession, messageListener, consumers));
		}
		
		try {
			assertDeliveries(testMethodName, destinationType, messageListener, outgoingMessages, consumers, waitUntilExpire);
		} finally {
			executorService.shutdownNow();
		}
		
	}
	
	protected List<ReceiverJob> createReceiverJobs(Destination destination, MessagingSession consumerSession, ReceiverMessageListener messageListener, int consumers) throws Exception {
		List<ReceiverJob> receivers = new ArrayList<ReceiverJob>(consumers);
		for (int i = 0; i < consumers; i++) {
			MessageConsumer messageConsumer = consumerSession.createMessageConsumer(destination);
			receivers.add(new ReceiverJob(messageConsumer, messageListener));
		}
		return receivers;
	}
	
}
