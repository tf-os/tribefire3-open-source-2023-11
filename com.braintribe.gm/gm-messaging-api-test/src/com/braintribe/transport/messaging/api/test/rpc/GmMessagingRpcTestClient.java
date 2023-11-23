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
package com.braintribe.transport.messaging.api.test.rpc;

import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingSession;

public class GmMessagingRpcTestClient {

	Supplier<MessagingSession> sessionProvider;
	
	private MessagingSession session;
	private Destination rpcDestination;
	private Destination replyToDestination;
	
	private MessageProducer producer;
	private MessageConsumer consumer;
	
	public GmMessagingRpcTestClient(Supplier<MessagingSession> sessionProvider, String rpcDestinationName, String replyToDestinationName) throws Exception {
		this.sessionProvider = sessionProvider;
		this.session = sessionProvider.get();
		this.rpcDestination = this.session.createQueue(rpcDestinationName);
		this.replyToDestination = this.session.createQueue(replyToDestinationName);
		
		this.producer = session.createMessageProducer(this.rpcDestination);
		this.consumer = session.createMessageConsumer(this.replyToDestination);
	}
	
	public Object call(Object request) throws Exception {
		
		String correlationId = UUID.randomUUID().toString();
		
		Message message = session.createMessage();
		message.setBody(request);
		message.setCorrelationId(correlationId);
		message.setReplyTo(replyToDestination);
		message.setPersistent(true);
		
		producer.sendMessage(message);
		
		Message replyMessage = null;
		while ((replyMessage = consumer.receive()) != null) {
			if (replyMessage.getCorrelationId().equals(correlationId)) {
				System.out.println("received "+replyMessage.getBody()+" from "+replyToDestination.getName()+" with correlation id "+replyMessage.getCorrelationId());
				return replyMessage.getBody();
			}
		}
		
		throw new Exception("failed request");
	}
	
	public void close() throws Exception {
		session.close();
	}
	
}
