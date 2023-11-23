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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingSession;

/**
 * <p>
 * {@code GmMessagingApi} tests intended to produce a heavy load of messages through different distribution of
 * connections, sessions, producers and consumers.
 * 
 */
@Category(VerySlow.class)
public abstract class GmMessagingHeavyDistributionTest extends GmMessagingTest {

	// Configurable test parameters
	protected static int maxConcurrentTests = 10;
	protected static int[] messages = { 1, 40, 80 };
	protected static int sessionGroup = 5;
	protected static int messageGroupForDestination = 20;
	protected static int messageGroupForProducer = 10;
	protected static int messageGroupForConsumer = 10;

	public enum ConnectionSetup {
		SingleConnection,
		ConnectionPerSession,
		ConnectionPerSessionGroup //one connection each ${sessionGroup} sessions
	}
	
	public enum SessionSetup {
		SingleSession,
		SessionPerComponent, //each message/destination/consumers/producer is creted from its own session
		SessionPerMessage, //each message from its own session, a shared session is used for everything else.
		SessionPerDestination, //each destination from its own session, a shared session is used for everything else.
		SessionPerConsumer, //each consumer from its own session, a shared session is used for everything else.
		SessionPerProducer //each producer from its own session, a shared session is used for everything else.  
	}
	
	public enum DestinationTypeSetup {
		Queues,
		Topics,
		QueuesAndTopics
	}
	
	public enum DestinationSetup {
		SingleDestination,
		DestinationPerMessage,
		DestinationPerMessageGroup //one destination each ${messageGroupForDestination} messages
	}
	
	public enum ProducerSetup {
		SingleProducer,
		ProducerPerDestination,
		ProducerPerMessage,
		ProducerPerMessageGroup //one producer each ${messageGroupForProducer} messages
	}
	
	public enum ConsumerSetup {
		ConsumerPerDestination,
		ConsumerPerMessage,
		ConsumerPerMessageGroup //one consumer each ${messageGroupForConsumer} messages of the same destination
	}

	@Test
	@Category(Slow.class)
	public void testConcurrently() throws Throwable {
		
		MessagingConnectionProvider<?> connProv = getMessagingConnectionProvider();
		
		Set<Callable<Throwable>> callableTests = new HashSet<Callable<Throwable>>();
		
		for (DestinationTypeSetup destType : DestinationTypeSetup.values()) {
			for (ConnectionSetup conn : ConnectionSetup.values()) {
				for (SessionSetup sess : SessionSetup.values()) {
					for (DestinationSetup dest : DestinationSetup.values()) {
						for (ProducerSetup prod : ProducerSetup.values()) {
							for (ConsumerSetup cons : ConsumerSetup.values()) {
								for (int m : messages) {
									if (!skipCombination(conn, sess, destType, dest, prod, cons, m)) {
										callableTests.add(new ConcurrentDistributedTest(connProv, destType, conn, sess, dest, prod, cons, m));
									}
								}
							}
						}
					}
				}
			}
		}
		
		ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentTests);
		
		long s = System.currentTimeMillis();
		
		if (outputEnabled) {
			System.out.println("Starting "+callableTests.size()+". tests.");
		}
		
		List<Future<Throwable>> results = executorService.invokeAll(callableTests);
		
		if (outputEnabled) {
			System.out.println("Executed "+callableTests.size()+" tests in "+(System.currentTimeMillis()-s)+" ms");
		}
		
		for (Future<Throwable> future : results) {
			if (future.get() != null) {
				throw future.get();
			}
		}
		
	}
	
	public class ConcurrentDistributedTest implements Callable<Throwable> {
		
		private boolean outputTrace = false;
		
		private String testName;
		
		private DestinationTypeSetup destinationType;
		private ConnectionSetup connectionSetup; 
		private SessionSetup sessionSetup; 
		private DestinationSetup destinationSetup; 
		private ProducerSetup producerSetup; 
		private ConsumerSetup consumerSetup;
		private int totalMessages;
		
		private int totalConnections = 1;
		private int totalSessions = 1;
		private int totalDestinations = 1;
		private int totalProducers = 1;
		private int totalConsumers = 1;
		
		private MessagingConnectionProvider<?> messagingConnectionProvider;
		private List<MessagingConnection> connectionsMap;
		private List<MessagingSession> sessionsMap;
		private List<Destination> destinationsMap;
		private List<CallableMessageListener> listenersMap;
		private Map<Destination, List<MessageConsumer>> consumersMap;
		private Map<Destination, List<MessageProducer>> producersMap;
		private Map<Destination, List<Message>> messagesMap;

		private Iterator<MessagingSession> sessionsIterator;

		public ConcurrentDistributedTest(MessagingConnectionProvider<?> connProvider, DestinationTypeSetup destType, ConnectionSetup conn, SessionSetup sess, DestinationSetup dest, ProducerSetup prod, ConsumerSetup cons, int messages) {
			super();
			this.messagingConnectionProvider = connProvider;
			this.destinationType = destType;
			this.connectionSetup = conn;
			this.sessionSetup = sess;
			this.destinationSetup = dest;
			this.producerSetup = prod;
			this.consumerSetup = cons;
			this.totalMessages = messages;

			//calculate "totals"
			calculateTotals();
			
			//this.testName = "test"+connectionSetup+sessionSetup+destinationSetup+destinationType+producerSetup+consumerSetup+totalMessages+"Messages";
			this.testName = "test-"+connectionSetup+"("+totalConnections+")-"+
					sessionSetup+"("+totalSessions+")-"+
					destinationSetup+"("+totalDestinations+")-"+
					destinationType+"-"+
					producerSetup+"("+totalProducers+")-"+
					consumerSetup+"("+totalConsumers+")-"+
					"Messages"+"("+totalMessages+")";
			
			//initialize collections
			this.connectionsMap = new ArrayList<MessagingConnection>(totalConnections);
			this.sessionsMap = new ArrayList<MessagingSession>(totalSessions);
			this.destinationsMap = new ArrayList<Destination>(totalDestinations);
			this.listenersMap = new ArrayList<CallableMessageListener>(totalDestinations);
			this.consumersMap = new HashMap<Destination, List<MessageConsumer>>(totalConsumers);
			this.producersMap = new HashMap<Destination, List<MessageProducer>>(totalProducers);
			this.messagesMap = new HashMap<Destination, List<Message>>(totalDestinations);
			
			
		}

		@Override
		public Throwable call() throws Exception {
			
			if (outputEnabled && outputTrace) {
				System.out.println("Starting: "+testName+ "with:\n\tconnections: "+totalConnections+". sessions: "+totalSessions+". consumers: "+totalConsumers+". producers: "+totalProducers+". destinations: "+totalDestinations+". messages: "+totalMessages);
			}
			
			try {
				createConnections();
				createSessions();
				createDestinations();
				createMessages();
				createConsumers();
				createMessageListeners();
				
				List<AsyncSender> senders = createSenders();
				
				sendAsynchronously(senders);
				
				ExecutorService receiverExecutor = Executors.newCachedThreadPool();
				try {
					List<Future<Throwable>> results = receiverExecutor.invokeAll(listenersMap);
					for (Future<Throwable> result : results) {
						Throwable failure = result.get();
						if (failure != null) {
							if (outputEnabled) {
								System.out.println("Failed: "+testName);
							}
							return failure;
						}
					}
				} finally {
					receiverExecutor.shutdownNow();
				}
				
			} catch (Throwable t) {
				if (outputEnabled) {
					System.out.println("Failed: "+testName);
				}
				return t;
			} finally {
				closeConnections();
			}

			if (outputEnabled && outputTrace) {
				System.out.println("Completed: "+testName);
			}
			
			return null;
			
		}
		
		private List<AsyncSender> createSenders() throws Exception {
			
			Iterator<MessagingSession> localSessionsIterator = null;
			MessagingSession sharedSession = null;
			
			if (sessionSetup == SessionSetup.SessionPerComponent) {
				localSessionsIterator = this.sessionsIterator;
			} else if (sessionSetup == SessionSetup.SessionPerProducer) {
				localSessionsIterator = sessionsMap.iterator();
			} else {
				//gets the last MessagingSession in sessionsMap
				sharedSession = sessionsMap.get(sessionsMap.size()-1);
			}
			
			List<AsyncSender> asyncSenders = new ArrayList<AsyncSender>();


			MessageProducer messageProducer = null;
			if (producerSetup == ProducerSetup.SingleProducer) {
				messageProducer = createProducer(localSessionsIterator, sharedSession, messagesMap.keySet().iterator().next());
			}
			
			for (Map.Entry<Destination, List<Message>> destMsgEntry : messagesMap.entrySet()) {
				
				Destination destination = destMsgEntry.getKey();
				
				if (producerSetup == ProducerSetup.ProducerPerDestination) {
					messageProducer = createProducer(localSessionsIterator, sharedSession, destination);
				}
				
				List<Message> destinationMessages = destMsgEntry.getValue();
				for (int i = 0; i < destinationMessages.size(); i++) {
					if (producerSetup == ProducerSetup.ProducerPerMessage || (producerSetup == ProducerSetup.ProducerPerMessageGroup && i % messageGroupForProducer == 0)) {
						messageProducer = createProducer(localSessionsIterator, sharedSession, destination);
					}
					
					AsyncSender sender = null;
					if (producerSetup == ProducerSetup.SingleProducer) {
						sender = new AsyncSender(destinationMessages.get(i), messageProducer, destination);
					} else {
						sender = new AsyncSender(destinationMessages.get(i), messageProducer);
					}
					
					asyncSenders.add(sender);
				}
					
			}
			
			return asyncSenders;
		}
		
		private void calculateTotals() {

			if (destinationSetup == DestinationSetup.DestinationPerMessage) {
				totalDestinations = totalMessages;
			} else if (destinationSetup == DestinationSetup.DestinationPerMessageGroup && totalMessages > 1) {
				totalDestinations = totalMessages / messageGroupForDestination;
			}
			
			if (producerSetup == ProducerSetup.ProducerPerDestination) {
				totalProducers = totalDestinations;
			} else if (producerSetup == ProducerSetup.ProducerPerMessage) {
				totalProducers = totalMessages;
			} else if (producerSetup == ProducerSetup.ProducerPerMessageGroup && totalMessages > 1) {
				totalProducers = totalMessages / messageGroupForProducer;
			}
			
			if (consumerSetup == ConsumerSetup.ConsumerPerDestination) {
				totalConsumers = totalDestinations;
			} else if (consumerSetup == ConsumerSetup.ConsumerPerMessage) {
				totalConsumers = totalMessages;
			} else if (consumerSetup == ConsumerSetup.ConsumerPerMessageGroup && totalMessages > 1) {
				totalConsumers = totalMessages / messageGroupForConsumer;
			}
			
			if (sessionSetup == SessionSetup.SessionPerComponent) {
				totalSessions = totalMessages + totalDestinations + totalProducers + totalConsumers;
			} else if (sessionSetup == SessionSetup.SessionPerConsumer) {
				totalSessions = totalConsumers + 1;
			} else if (sessionSetup == SessionSetup.SessionPerProducer) {
				totalSessions = totalProducers + 1;
			} else if (sessionSetup == SessionSetup.SessionPerDestination) {
				totalSessions = totalDestinations + 1;
			} else if (sessionSetup == SessionSetup.SessionPerMessage) {
				totalSessions = totalMessages + 1;
			}
			
			if (connectionSetup == ConnectionSetup.ConnectionPerSession) {
				totalConnections = totalSessions;
			} else if (connectionSetup == ConnectionSetup.ConnectionPerSessionGroup && totalSessions > 1) {
				totalConnections = Double.valueOf(Math.ceil(totalSessions / 2.0)).intValue();
			}
			
		}
		
		private void createConnections() throws Exception {
			for (int i = 0; i < totalConnections; i++) {
				connectionsMap.add(i, messagingConnectionProvider.provideMessagingConnection());
			}
		}
		
		private void closeConnections() {
			for (MessagingConnection connection : connectionsMap) {
				if (connection != null) {
					try {
						connection.close();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}
		
		private void createSessions() throws Exception {
			Iterator<MessagingConnection> connIter = connectionsMap.iterator();
			MessagingConnection c = null;
			for (int i = 0; i < totalSessions; i++) {
				if (connIter.hasNext()) {
					if (i == 0) {
						c = connIter.next();
					} else if (connectionSetup == ConnectionSetup.ConnectionPerSession || (connectionSetup == ConnectionSetup.ConnectionPerSessionGroup && i % sessionGroup == 0)) {
						c = connIter.next();
					}
				}
				sessionsMap.add(i, c.createMessagingSession());
			}
			
			this.sessionsIterator = sessionsMap.iterator();
		}
		
		private void createDestinations() throws Exception {
			
			Iterator<MessagingSession> localSessionsIterator = null;
			MessagingSession sharedSession = null;
			
			if (sessionSetup == SessionSetup.SessionPerComponent) {
				localSessionsIterator = this.sessionsIterator;
			} else if (sessionSetup == SessionSetup.SessionPerDestination) {
				localSessionsIterator = sessionsMap.iterator();
			} else {
				//gets the last MessagingSession in sessionsMap
				sharedSession = sessionsMap.get(sessionsMap.size()-1);
			}
			
			Class<? extends Destination> destinationClass = destinationType == DestinationTypeSetup.Queues ? Queue.class : Topic.class;

			for (int i = 0; i < totalDestinations; i++) {
				
				if (destinationType == DestinationTypeSetup.QueuesAndTopics) {
					destinationClass = (destinationClass == Queue.class) ? Topic.class : Queue.class;
				}
						
				MessagingSession sessionToUse = sharedSession;
				if (sessionToUse == null && localSessionsIterator != null && localSessionsIterator.hasNext()) {
					sessionToUse = localSessionsIterator.next();
				}
				
				destinationsMap.add(createDestination(destinationClass, sessionToUse, testName, i));
			}
		}
		
		
		private void createMessages() throws Exception {

			Iterator<MessagingSession> localSessionsIterator = null;
			MessagingSession sharedSession = null;
			
			if (sessionSetup == SessionSetup.SessionPerComponent) {
				localSessionsIterator = this.sessionsIterator;
			} else if (sessionSetup == SessionSetup.SessionPerMessage) {
				localSessionsIterator = sessionsMap.iterator();
			} else {
				//gets the last MessagingSession in sessionsMap
				sharedSession = sessionsMap.get(sessionsMap.size()-1);
			}

			Destination sharedDestination = null;

			if (destinationSetup == DestinationSetup.SingleDestination) {
				sharedDestination = destinationsMap.iterator().next();
			}

			Destination destinationToUse = null;
			Iterator<Destination> destinationsIterator = null;
			for (int i = 0; i < totalMessages; i++) {
				
				if (sharedDestination != null) {
					destinationToUse = sharedDestination;
				} else {
					if (destinationSetup == DestinationSetup.DestinationPerMessage || (destinationSetup == DestinationSetup.DestinationPerMessageGroup && i % messageGroupForDestination == 0)) {
						if (destinationsIterator == null || !destinationsIterator.hasNext()) {
							destinationsIterator = destinationsMap.iterator();
						}
						destinationToUse = destinationsIterator.next();
					}
				}

				MessagingSession sessionToUse = sharedSession;
				if (sessionToUse == null && localSessionsIterator != null && localSessionsIterator.hasNext()) {
					sessionToUse = localSessionsIterator.next();
				}
				
				Message message = sessionToUse.createMessage();
				List<Message> destinationMessages = messagesMap.get(destinationToUse);
				if (destinationMessages == null) {
					destinationMessages = new ArrayList<Message>();
					messagesMap.put(destinationToUse, destinationMessages);
				}
				destinationMessages.add(message);
				
			}
			
		}
		
		private void createConsumer(Iterator<MessagingSession> _sessionsIterator, MessagingSession sharedSession, Destination destination) throws Exception {

			MessagingSession sessionToUse = sharedSession;
			if (sessionToUse == null) {
				if (_sessionsIterator != null && _sessionsIterator.hasNext()) {
					sessionToUse = _sessionsIterator.next();
				} else {
					sessionToUse = sessionsMap.get(sessionsMap.size()-1);
				}
			}
			
			MessageConsumer messageConsumer = sessionToUse.createMessageConsumer(destination);
			
			List<MessageConsumer> destConsumers = consumersMap.get(destination);
			if (destConsumers == null) {
				destConsumers = new ArrayList<MessageConsumer>();
				consumersMap.put(destination, destConsumers);
			}
			destConsumers.add(messageConsumer);
			
		}
		
		
		private void createConsumers() throws Exception {
			
			Iterator<MessagingSession> localSessionsIterator = null;
			MessagingSession sharedSession = null;
			
			if (sessionSetup == SessionSetup.SessionPerComponent) {
				localSessionsIterator = this.sessionsIterator;
			} else if (sessionSetup == SessionSetup.SessionPerConsumer) {
				localSessionsIterator = sessionsMap.iterator();
			} else {
				//gets the last MessagingSession in sessionsMap
				sharedSession = sessionsMap.get(sessionsMap.size()-1);
			}
			
			for (Map.Entry<Destination, List<Message>> destMsgEntry : messagesMap.entrySet()) {
				Destination destination = destMsgEntry.getKey();
				if (consumerSetup == ConsumerSetup.ConsumerPerDestination) {
					createConsumer(localSessionsIterator, sharedSession, destination);
				} else {
					List<Message> destinationMessages = destMsgEntry.getValue();
					for (int i = 0; i < destinationMessages.size(); i++) {
						if (consumerSetup == ConsumerSetup.ConsumerPerMessage || (consumerSetup == ConsumerSetup.ConsumerPerMessageGroup && i % messageGroupForConsumer == 0)) {
							createConsumer(localSessionsIterator, sharedSession, destination);
						}
					}
					
				}
			}
		}

		private void createMessageListeners() throws Exception {
			for (Map.Entry<Destination, List<MessageConsumer>> consumersEntry : consumersMap.entrySet()) {
				CallableMessageListener listener = new CallableMessageListener(consumersEntry.getKey(), consumersEntry.getValue().size(), messagesMap.get(consumersEntry.getKey()), outputTrace);
				for (MessageConsumer messageConsumer : consumersEntry.getValue()) {
					messageConsumer.setMessageListener(listener);
				}
				listenersMap.add(listener);
			}
		}
		
		private MessageProducer createProducer(Iterator<MessagingSession> _sessionsIterator, MessagingSession sharedSession, Destination destination) throws Exception {

			MessagingSession sessionToUse = sharedSession;
			if (sessionToUse == null) {
				if (_sessionsIterator != null && _sessionsIterator.hasNext()) {
					sessionToUse = _sessionsIterator.next();
				} else {
					sessionToUse = sessionsMap.get(sessionsMap.size()-1);
				}
			}
			
			MessageProducer messageProducer = sessionToUse.createMessageProducer(destination);
			
			List<MessageProducer> destProducers = producersMap.get(destination);
			if (destProducers == null) {
				destProducers = new ArrayList<MessageProducer>();
				producersMap.put(destination, destProducers);
			}
			destProducers.add(messageProducer);
			
			return messageProducer;
			
		}
 		
	}
	
	
	private static boolean skipCombination(ConnectionSetup conn, SessionSetup sess, DestinationTypeSetup destType, DestinationSetup dest, ProducerSetup prod, ConsumerSetup cons, int messages) {
		
		if (messages == 1) {
			if (cons == ConsumerSetup.ConsumerPerMessageGroup || 
				prod == ProducerSetup.ProducerPerMessageGroup || 
				destType == DestinationTypeSetup.QueuesAndTopics || 
				dest == DestinationSetup.DestinationPerMessageGroup) {
				return true;
			}
		}
		
		if (sess == SessionSetup.SingleSession && conn != ConnectionSetup.SingleConnection) {
			return true;
		}
		
		return false;
		
	}
	
}
