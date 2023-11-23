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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

/**
 * <p>
 * General base for {@code GmMessagingApi} tests.
 * 
 */
public abstract class GmMessagingTest {
	
	private DateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
	protected boolean outputEnabled = true;
	
	protected abstract MessagingConnectionProvider<? extends MessagingConnection> getMessagingConnectionProvider();
	
	protected abstract MessagingContext getMessagingContext();
	
	protected void assertDeliveries(String testMethodName, Class<? extends Destination> destinationType, ReceiverMessageListener receiver, List<Message> outgoingMessages, int consumers, boolean expiredExpected) throws Exception {
		assertDeliveries(testMethodName, destinationType, receiver, outgoingMessages, 1, consumers, expiredExpected);
	}
	
	@SuppressWarnings("unused")
	protected void assertDeliveries(String testMethodName, Class<? extends Destination> destinationType, ReceiverMessageListener receiver, List<Message> outgoingMessages, int destinations, int consumers, boolean expiredExpected) throws Exception {
		
		long maxWaitingType = expiredExpected ? 3000 : 15000; //3 secs when expired are expected, 15 seconds otherwise
		long timeOut = System.currentTimeMillis() + maxWaitingType;
		
		int expectedDeliveries = 0;
		if (consumers > 0) {
			if (Queue.class.isAssignableFrom(destinationType)) {
				expectedDeliveries = outgoingMessages.size();
			} else {
				expectedDeliveries = outgoingMessages.size() * consumers;
			}
		}
		
		String expectedDesc = (expiredExpected) ? "possible (not mandatory)" : "expected (mandatory)";
		
		if (outputEnabled) {
			System.out.print(testMethodName+":\r\n\twaiting "+expectedDeliveries+" "+expectedDesc+" delivery(ies).");
		}
		
		boolean timedOut = false;
		
		int d = receiver.getDeliveredMessagesCount();
		int c = -1;
		while(d  < expectedDeliveries) {
			Thread.sleep(1000);
			d = receiver.getDeliveredMessagesCount();
			if (c == d) {
				if (outputEnabled) {
					System.out.print(".");
				}
				
				if (System.currentTimeMillis() > timeOut) {
					if (outputEnabled) {
						System.out.print(" (timeout of "+maxWaitingType+" ms reached)");
					}
					timedOut = true;
					break;
				}
				
			} else {
				c = d;
				if (outputEnabled) {
					System.out.print(".."+c);
				}
			}
			
		}
		if (outputEnabled) {
			System.out.println(" done.");
			System.out.println("\treached "+receiver.getDeliveredMessagesCount()+" of "+expectedDeliveries+" "+expectedDesc+" delivery(ies).\r\n");
		}
		
		if (!timedOut) {
			//expected was reached without timeout, give it more time to ensure that no more than the expected will be delivered.
			Thread.sleep(2000);
		}
		
		//assert totals
		if (expiredExpected) {
			Assert.assertTrue(testMethodName+" failed: no more than "+expectedDeliveries+" "+expectedDesc+" delivery(ies) were expected, but "+receiver.getDeliveredMessagesCount()+" messages were delivered", receiver.getDeliveredMessagesCount() <= expectedDeliveries);
		} else {
			Assert.assertEquals(testMethodName+" failed: "+expectedDeliveries+" delivery(ies) were "+expectedDesc+", but "+receiver.getDeliveredMessagesCount()+" messages were delivered", expectedDeliveries, receiver.getDeliveredMessagesCount());
		}
		
		//assert contents
		if (consumers > 0) {
			int expectedCopies = 1;
			if (Topic.class.isAssignableFrom(destinationType)) {
				expectedCopies = consumers;
			}
			
			Map<String, List<Message>> messagesById = new HashMap<String, List<Message>>();
			
			for (Message delivered : receiver.getDeliveredMessages()) {

				Assert.assertNotNull("Delivered message is null", delivered);
				Assert.assertNotNull("Delivered message id is null", delivered.getMessageId());
				
				List<Message> messages = messagesById.get(delivered.getMessageId());
				if (messages == null) {
					messages = new ArrayList<Message>();
					messagesById.put(delivered.getMessageId(), messages);
				}
				
				messages.add(delivered);
			}
			
			for (Message outgoing : outgoingMessages) {

				Assert.assertNotNull("Outgoing message is null", outgoing);
				Assert.assertNotNull("Outgoing message id is null", outgoing.getMessageId());
				
				List<Message> copies = messagesById.get(outgoing.getMessageId());
				
				int totalCopies = copies != null ? copies.size() : 0;

				if (copies != null) {
					for (Message copy : copies) {
						Assert.assertEquals("Copy has different destination type", outgoing.getDestination().getClass(), copy.getDestination().getClass());
						Assert.assertEquals("Copy has different destination name", outgoing.getDestination().getName(), copy.getDestination().getName());
					}
				}
				
				if (expiredExpected) {
					Assert.assertFalse(testMethodName+" failed: no more than "+expectedCopies+" copies of the message "+outgoing.getMessageId()+" were expected, but "+totalCopies+" were found", totalCopies > expectedCopies);
				} else {
					Assert.assertEquals(testMethodName+" failed: "+expectedCopies+" copies of the message "+outgoing.getMessageId()+" were expected, but "+totalCopies+" were found", expectedCopies, totalCopies);
				}
				if (copies != null) {
					for (Message deliveredCopy : copies) {
						assertEquals(outgoing, deliveredCopy);
					}
				}
			}
		}
		
	}
	
	protected void assertDeliveries(CallableMessageListener listener) throws Exception {
		assertDeliveries(Arrays.asList(listener));
	}

	protected void assertDeliveries(Collection<? extends CallableMessageListener> listeners) throws Exception {
		ExecutorService receiverExecutor = Executors.newFixedThreadPool(listeners.size());
		try {
			List<Future<Throwable>> results = receiverExecutor.invokeAll(listeners);
			for (Future<Throwable> result : results) {
				Throwable failure = result.get();
				if (failure != null) {
					if (failure instanceof Exception) {
						throw (Exception)failure;
					}
					if (failure instanceof AssertionError) {
						throw (AssertionError)failure;
					}
					failure.printStackTrace();
				}
			}
		} finally {
			receiverExecutor.shutdownNow();
		}
	}
	
	protected void sendAsynchronously(MessageProducer producer, List<Message> messages) throws Exception {
		List<AsyncSender> senders = new ArrayList<AsyncSender>();
		for (Message message : messages) {
			senders.add(new AsyncSender(message, producer));
		}
		sendAsynchronously(senders);
	}
	
	protected void sendAsynchronously(List<AsyncSender> senders) throws Exception {
		
		if (senders.isEmpty()) {
			return;
		}
		
		ExecutorService senderExecutor = Executors.newCachedThreadPool();
		try {
			senderExecutor.invokeAll(senders);
		} finally {
			senderExecutor.shutdownNow();
		}
	}
	
	protected ExecutorService submitReceiver(JobFeedable messageListener, ReceiverJob receiverJob) throws Exception {
		return submitReceiver(messageListener, Arrays.asList(receiverJob));
	}
	
	protected ExecutorService submitReceiver(JobFeedable messageListener, List<ReceiverJob> receiverJobs) throws Exception {
		
		int expectedJobs = receiverJobs.size();
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		try {

			for (ReceiverJob receiverJob : receiverJobs) {
				executorService.submit(receiverJob);
			}
			
			//no point in going further until all jobs are running
			while (true) {
				Thread.sleep(500);
				if (messageListener.getFeedingJobs() == expectedJobs) 
					break;
				System.out.println("Awaiting "+expectedJobs+" job(s) to be up and running");
			}
			
		} catch (Exception e) {
			executorService.shutdownNow();
			throw e;
		}
		
		return executorService;
		
	}
	
	
	protected Message createMessage(MessagingSession session) throws Exception {
		return createMessage(session, false, -1);
	}
	
	protected static void assertEquals(Message expected, Message actual) {
		
		if (expected == actual) {
			return;
		}

		Assert.assertEquals("Unexpected message id"             , expected.getMessageId()            , actual.getMessageId());
		Assert.assertEquals("Unexpected message correlation id" , expected.getCorrelationId()        , actual.getCorrelationId());
		Assert.assertEquals("Unexpected message destination"    , expected.getDestination().getName(), actual.getDestination().getName());
		Assert.assertEquals("Unexpected message persistent flag", expected.getPersistent()           , actual.getPersistent());
		Assert.assertTrue  ("Unexpected message expiration (expected:"+expected.getExpiration()+", actual:"+actual.getExpiration()+")"     , Math.abs(expected.getExpiration()-actual.getExpiration()) < 5000);
		Assert.assertEquals("Unexpected message time to live"   , expected.getTimeToLive()           , actual.getTimeToLive());
		Assert.assertEquals("Unexpected message priority"       , expected.getPriority()             , actual.getPriority());
		Assert.assertEquals("Unexpected message body"           , expected.getBody()                 , actual.getBody());
		
	}

	protected Message createMessage(MessagingSession session, boolean persistent, long timeToLive) throws Exception {
		
		String iden = "Message-"+df.format(new Date());
		
		Message message = session.createMessage();
		
		message.setPersistent(persistent);
		
		if (timeToLive >= 0) {
			message.setTimeToLive(timeToLive);
		}

		message.setBody("Body: "+iden);
		message.setCorrelationId("CorrelationId: "+iden);
		message.setHeaders(createTestHeaders(iden));
		
		return message;
	}

	protected List<Message> createMessages(MessagingSession session, int qty) throws Exception  {
		return createMessages(session, qty, false, 0);
	}
	
	protected List<Message> createMessages(MessagingSession session, int qty, boolean persistent, long timeToLive) throws Exception {
		List<Message> messages = new ArrayList<Message>(qty);
		
		while (messages.size() < qty) {
			messages.add(createMessage(session, persistent, timeToLive));
		}
		
		return messages;
	}

	protected Destination createDestination(Class<? extends Destination> destinationType, MessagingSession session, String methodName, Integer destinationCount) throws Exception {
		if (Topic.class.isAssignableFrom(destinationType)) {
			return session.createTopic("gm-test-topic-"+methodName+((destinationCount != null) ? "-"+destinationCount : ""));
		} else {
			return session.createQueue("gm-test-queue-"+methodName+((destinationCount != null) ? "-"+destinationCount : ""));
		}
	}
	
	protected Destination createDestination(Class<? extends Destination> destinationType, MessagingSession session, String methodName) throws Exception {
		return createDestination(destinationType, session, methodName, null);
	}
	
	private static Map<String, Object> createTestHeaders(String iden) {
		
		String[] testArray = new String[] {"A", "B", "C"};
		
		Map<String, Object> h = new HashMap<String, Object>();
		h.put("String-header", "String-header of "+iden);
		h.put("Long-header", Long.MIN_VALUE);
		h.put("Integer-header", Integer.MAX_VALUE);
		h.put("Boolean-header", Boolean.TRUE);
		
		//makes the marshalling fail with BinMarshaller:
		//h.put("Array-header", testArray);
		
		h.put("List-header", Arrays.asList(testArray));
		
		return h;
	}
	
	protected static interface JobFeedable {
		int getFeedingJobs();
		void incrementFeedingJob();
	}

	/**
	 * A {@link MessageListener} which stores the received {@link Message}(s) into a Collection accessible via {@link ReceiverMessageListener#getDeliveredMessages()}
	 * 
	 */
	protected static class ReceiverMessageListener implements MessageListener, JobFeedable {
		
		private List<Message> deliveredMessages = new ArrayList<Message>();
		private AtomicInteger messages = new AtomicInteger();
		private AtomicInteger feeds = new AtomicInteger();

		@Override
		public void onMessage(Message message) throws MessagingException {
			
			if (message == null) {
				return;
			}
			
			boolean added = false;
			
			synchronized (deliveredMessages) {
				added = deliveredMessages.add(message);
			}
			
			if (added) {
				messages.incrementAndGet();
			}
			
		}
		
		public List<Message> getDeliveredMessages() {
			return deliveredMessages;
		}
		
		@Override
		public int getFeedingJobs() {
			return feeds.get();
		}
		
		@Override
		public void incrementFeedingJob() {
			feeds.incrementAndGet();
		}
		
		public int getDeliveredMessagesCount() {
			return messages.get();
		}
		
	}
	
	/**
	 * A {@link MessageListener} mean to be registered to all consumers of one {@link Destination}. 
	 * 
	 * The received {@link Message}(s) into a Collection accessible via {@link ReceiverMessageListener#getDeliveredMessages()}
	 * 
	 */
	protected static class CallableMessageListener implements MessageListener, Callable<Throwable>, JobFeedable {
		
		private Destination destination;
		private List<Message> outgoingMessages;
		
		private int delegatingConsumers;
		private int expectedDeliveries;
		private int expectedCopies;
		
		private List<Message> deliveredMessages = new ArrayList<Message>();
		private AtomicInteger deliveries = new AtomicInteger();
		private AtomicInteger feeds = new AtomicInteger();
		
		private boolean outputSuccess = false;
		
		public CallableMessageListener(Destination destination, int delegatingConsumers, List<Message> outgoingMessages, boolean output) {
			this.destination = destination;
			this.delegatingConsumers = delegatingConsumers;
			this.outgoingMessages = outgoingMessages;
			this.outputSuccess = output;
			
			if (delegatingConsumers > 0 && outgoingMessages.size() > 0) {
				if (Queue.class.isAssignableFrom(destination.getClass())) {
					this.expectedDeliveries = outgoingMessages.size();
					this.expectedCopies = 1;
				} else {
					this.expectedDeliveries = delegatingConsumers * outgoingMessages.size();
					this.expectedCopies = delegatingConsumers;
				}
			}
		}
		
		@Override
		public void onMessage(Message message) throws MessagingException {
			
			if (message == null) {
				return;
			}
			
			boolean added = false;
			
			synchronized (deliveredMessages) {
				added = deliveredMessages.add(message);
			}
			
			if (added) {
				deliveries.incrementAndGet();
			}
			
		}
		
		@Override
		public Throwable call() throws Exception {
			
			waitUntilReadyForAssertions();
			
			try {
				assertDelivery();
			} catch (Throwable e) {
				System.err.println("Failed [ "+deliveries.get()+" ] deliveries of [ "+outgoingMessages.size()+" ] messages to [ "+delegatingConsumers+" ] consumers of [ "+destination.getName()+" ]: "+e.getClass().getSimpleName()+": "+e.getMessage());
				return e;
			}
			if (outputSuccess) {
				System.out.println("Successful [ "+deliveries.get()+" ] deliveries of [ "+outgoingMessages.size()+" ] messages to [ "+delegatingConsumers+" ] consumers of [ "+destination.getName()+" ] ");
			}
			return null;
		}
		
		public List<Message> getDeliveredMessages() {
			return deliveredMessages;
		}
		
		@Override
		public int getFeedingJobs() {
			return feeds.get();
		}
		
		@Override
		public void incrementFeedingJob() {
			feeds.incrementAndGet();
		}
		
		public int getDeliveredMessagesCount() {
			return deliveries.get();
		}
		
		private void waitUntilReadyForAssertions() throws Exception {
			long maxWaitingType = 10000;
			long timeOut = System.currentTimeMillis() + maxWaitingType;
			boolean timedOut = false;
			for (int l = -1; deliveries.get() < expectedDeliveries; ) {
				Thread.sleep(500);
				if (deliveries.get() == l) {
					if (System.currentTimeMillis() > timeOut) {
						timedOut = true;
						break;
					}
				} else {
					l = deliveries.get();
				}
			}
			if (!timedOut) {
				Thread.sleep(500);
			}
		}
		
		private void assertDelivery() {
			
			int receivedMessages = deliveries.get();
			
			Assert.assertEquals(expectedDeliveries+" message(s) were expected, but "+receivedMessages+" messages were delivered", expectedDeliveries, receivedMessages);
			
			if (expectedDeliveries > 0) {

				Map<String, List<Message>> messagesById = new HashMap<String, List<Message>>();
				
				for (Message delivered : deliveredMessages) {

					Assert.assertNotNull("Delivered message is null", delivered);
					Assert.assertNotNull("Delivered message id is null", delivered.getMessageId());
					
					List<Message> messages = messagesById.get(delivered.getMessageId());
					if (messages == null) {
						messages = new ArrayList<Message>();
						messagesById.put(delivered.getMessageId(), messages);
					}
					
					messages.add(delivered);
				}
				
				for (Message outgoing : outgoingMessages) {

					Assert.assertNotNull("Outgoing message is null", outgoing);
					Assert.assertNotNull("Outgoing message id is null", outgoing.getMessageId());
					
					List<Message> copies = messagesById.get(outgoing.getMessageId());
					
					int totalCopies = copies != null ? copies.size() : 0;

					if (copies != null) {
						for (Message copy : copies) {
							Assert.assertEquals("Copy has different destination type", outgoing.getDestination().getClass(), copy.getDestination().getClass());
							Assert.assertEquals("Copy has different destination name", outgoing.getDestination().getName(), copy.getDestination().getName());
						}
					}
					
					Assert.assertEquals(expectedCopies+" copies of the message "+outgoing.getMessageId()+" were expected, but "+totalCopies+" were found", expectedCopies, totalCopies);
				
					if (copies != null) {
						for (Message deliveredCopy : copies) {
							assertEquals(outgoing, deliveredCopy);
						}
					}
				}
			}
			
		}
		
	}
	
	/**
	 * A Callable which consumes {@link Message}(s) from the given {@link MessageConsumer} notifying to a given {@link ReceiverMessageListener}.
	 * 
	 * This callable is meant to run indefinately until its thread is interrupted.
	 * 
	 */
	protected static class ReceiverJob implements Callable<Boolean> {
		
		private MessageConsumer messageConsumer;
		private ReceiverMessageListener listener;
		
		public ReceiverJob(MessageConsumer messageConsumer, ReceiverMessageListener listener) {
			this.messageConsumer = messageConsumer;
			this.listener = listener;
		}

		@Override
		public Boolean call() {
			Message message = null;
			listener.incrementFeedingJob();
			while (true) {
				try {
					message = messageConsumer.receive();
					if (message != null) {
						listener.onMessage(message);
					} else {
						break;
					}
				} catch (MessagingException e) {
					e.printStackTrace();
					break;
				} catch (Exception e) {
					if (!(e instanceof InterruptedException)) {
						e.printStackTrace();
					}
					return false;
				} 
			}
			return true;
		}
		
	}
	
	/**
	 * A Runnable which consumes {@link Message}(s) from the given {@link MessageConsumer} until the number of expected deliveries is reached.
	 * 
	 * Consumed messages are then accessible via {@link OptimisticReceiverJob#getDeliveredMessages()}
	 * 
	 */
	protected static class OptimisticReceiverJob implements Runnable {
		
		private MessageConsumer messageConsumer;
		private int expectedDeliveries;
		private List<Message> deliveredMessages;
		
		public OptimisticReceiverJob(MessageConsumer messageConsumer, int expectedDeliveries) {
			this.messageConsumer = messageConsumer;
			this.expectedDeliveries = expectedDeliveries;
			this.deliveredMessages = new ArrayList<Message>(this.expectedDeliveries);
		}

		@Override
		public void run() {
			while (deliveredMessages.size() < expectedDeliveries) {
				try {
					deliveredMessages.add(messageConsumer.receive());
				} catch (MessagingException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		public List<Message> getDeliveredMessages() {
			return deliveredMessages;
		}
		
	}
	
	public class AsyncSender implements Callable<Throwable> {
		
		private Message message;
		private MessageProducer messageProducer;
		private Destination destination;

		public AsyncSender(Message message, MessageProducer messageProducer) {
			this(message, messageProducer, null);
		}
		
		public AsyncSender(Message message, MessageProducer messageProducer, Destination destination) {
			super();
			this.message = message;
			this.messageProducer = messageProducer;
			this.destination = destination;
		}

		@Override
		public Throwable call() {
			try {
				if (destination != null) {
					messageProducer.sendMessage(message, destination);
				} else {
					messageProducer.sendMessage(message);
				}
			} catch (MessagingException e) {
				e.printStackTrace();
				return e;
			}
			return null;
		}
		
	}
	
	protected StackTraceElement getStackTraceElement(int depth) {
		return (new Throwable()).getStackTrace()[depth];
	}
	
	protected String getMethodName(int depth) {
		return getStackTraceElement(depth+2).getMethodName();
	}
	
	protected String getMethodName() {
		return getMethodName(1);
	}

}
