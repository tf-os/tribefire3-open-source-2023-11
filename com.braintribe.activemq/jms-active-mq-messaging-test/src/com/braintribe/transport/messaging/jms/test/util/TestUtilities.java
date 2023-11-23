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
package com.braintribe.transport.messaging.jms.test.util;

import java.util.Map;

import javax.jms.Message;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.model.messaging.Queue;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.jms.JmsMessageConsumer;
import com.braintribe.transport.messaging.jms.JmsSession;

public class TestUtilities {

	public static MessagingContext getMessagingContext() {
		MessagingContext context = new MessagingContext();
		context.setMarshaller(getMessageMarshaller());
		return context;
	}
	
	public static Marshaller getMessageMarshaller() {
		// @formatter:off
		return new Bin2Marshaller();
		// @formatter:on
	}

	public static void emptyQueue(JmsSession session, String queueName) throws Exception {
		Queue queue = session.createQueue(queueName);
		JmsMessageConsumer messageConsumer = (JmsMessageConsumer) session.createMessageConsumer(queue);
		javax.jms.MessageConsumer jmsMessageConsumer = messageConsumer.getJmsMessageConsumer();
		while(true) {
			Message msg = jmsMessageConsumer.receive(2000L);
			if (msg == null) {
				break;
			}
		}
		messageConsumer.close();
	}
	
	public static void checkNeedleInHaystack(Map<String,Object> hayStack, Map<String,Object> needle) {
		for (Map.Entry<String,Object> needleEntry : needle.entrySet()) {
			boolean found = false;
			for (Map.Entry<String,Object> haystackEntry : hayStack.entrySet()) {
				if (needleEntry.getKey().equals(haystackEntry.getKey()) &&
						needleEntry.getValue().equals(haystackEntry.getValue())) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new AssertionError("Could not find entry "+needleEntry);
			}
		}
	}
	
}
