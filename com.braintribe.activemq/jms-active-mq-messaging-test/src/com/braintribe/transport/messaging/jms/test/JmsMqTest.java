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
package com.braintribe.transport.messaging.jms.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;


public class JmsMqTest {
	
	private DateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
	
	protected void awaitExpectedDeliveries(List<Message> deliveredMessages, int outgoingMessages, int consumers) {
		int expectedDeliveries = outgoingMessages * consumers;
		System.out.print(getMethodName(1)+":\r\n\twaiting delivery.");
		while(deliveredMessages.size() < expectedDeliveries) {
			System.out.print(".");
		}
		System.out.println(" done. "+deliveredMessages.size()+" of "+expectedDeliveries+" expected messages delivered.\r\n");
	}
	
	protected void awaitExpectedDeliveries(List<Message> deliveredMessages, int outgoingMessages, int consumers, long waiting) throws Exception {
		int expectedDeliveries = outgoingMessages * consumers;
		
		System.out.print(getMethodName(1)+":\r\n\twaiting delivery.");
		Thread.sleep(waiting);
		System.out.println(" done. "+deliveredMessages.size()+" of "+expectedDeliveries+" expected messages delivered.\r\n");
		
		if (deliveredMessages.size() != expectedDeliveries) {
			Assert.assertEquals("unexpected number of messages delivered", expectedDeliveries, deliveredMessages.size());
		}
	}
	
	protected void awaitForNoDelivery(List<Message> deliveredMessages, long waiting) throws Exception {
		System.out.print(getMethodName(1)+":\r\n\twaiting delivery.");
		Thread.sleep(waiting);
		if (deliveredMessages.size() == 0) {
			System.out.println(" done. no messages delivered as expected.\r\n");
		} else {
			Assert.fail("no delivery was expected, but "+deliveredMessages.size()+" messages were delivered: "+deliveredMessages);
		}
	}
	
	protected void awaitForNoDelivery(List<Message> receivedMessages, List<Message> sentMessages, long waiting) throws Exception {
		System.out.print(getMethodName(1)+":\r\n\twaiting delivery.");
		Thread.sleep(waiting);
		if (receivedMessages.size() == 0) {
			System.out.println(" done. no messages delivered as expected.\r\n");
		} else {
			Assert.fail("no delivery was expected, but "+receivedMessages.size()+" messages were delivered: "+receivedMessages+" based on the messages sent: "+sentMessages);
		}
	}
	
	protected Message createMessage(MessagingSession session) throws Exception {
		return createMessage(session, false, -1);
	}

	protected Message createMessage(MessagingSession session, boolean persistent, long expiration) throws Exception {
		
		String iden = "Message-"+df.format(new Date());
		
		Message message = session.createMessage();
		
		message.setPersistent(persistent);
		
		if (persistent && expiration >= 0) {
			message.setExpiration(expiration);
		}

		message.setBody("Body: "+iden);
		message.setCorrelationId("CorrelationId: "+iden);
		message.setHeaders(createTestHeaders(iden));
		
		return message;
	}

	protected List<Message> createMessages(MessagingSession session, int qty) throws Exception  {
		return createMessages(session, qty, false, -1);
	}
	
	protected List<Message> createMessages(MessagingSession session, int qty, boolean persistent, long expiration) throws Exception {
		List<Message> messages = new ArrayList<Message>(qty);
		
		while (messages.size() < qty) {
			messages.add(createMessage(session, persistent, expiration));
		}
		
		return messages;
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
	
	
	/**
	 * A {@link MessageListener} which stores the received {@link Message}(s) into a Collection accessible via {@link ReceiverMessageListener#getDeliveredMessages()}
	 * 
	 *
	 */
	protected static class ReceiverMessageListener implements MessageListener {
		
		private List<Message> deliveredMessages = new ArrayList<Message>();

		@Override
		public void onMessage(Message message) throws MessagingException {
			deliveredMessages.add(message);
		}
		
		public List<Message> getDeliveredMessages() {
			return deliveredMessages;
		}
		
	}
	
	/**
	 * A Runnable which consumes {@link Message}(s) from the given {@link MessageConsumer} until the number of expected deliveries is reached.
	 * 
	 * Consumed messages are then accessible via {@link ReceiverJob#getDeliveredMessages()}
	 * 
	 *
	 */
	protected static class ReceiverJob implements Runnable {
		
		private MessageConsumer messageConsumer;
		private int expectedDeliveries;
		private List<Message> deliveredMessages;
		
		public ReceiverJob(MessageConsumer messageConsumer, int expectedDeliveries) {
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
