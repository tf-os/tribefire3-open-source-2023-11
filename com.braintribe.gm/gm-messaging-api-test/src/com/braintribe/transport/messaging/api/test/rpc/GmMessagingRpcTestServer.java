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

import java.util.function.Supplier;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

public class GmMessagingRpcTestServer implements MessageListener {
	
	Supplier<MessagingSession> sessionProvider;
	
	private MessagingSession session;
	private Destination rpcDestination;
	private RpcRequestProcessor processor;
	
	private MessageConsumer consumer;
	
	public GmMessagingRpcTestServer(Supplier<MessagingSession> sessionProvider, String rpcDestinationName, RpcRequestProcessor processor) throws Exception {
		this.sessionProvider = sessionProvider;
		this.session = sessionProvider.get();
		this.rpcDestination = this.session.createQueue(rpcDestinationName);
		
		this.processor = processor;
		
		this.consumer = session.createMessageConsumer(this.rpcDestination);
		this.consumer.setMessageListener(this);
	}
	
	@Override
	public void onMessage(Message message) throws MessagingException {
		
		System.out.println("received "+message.getBody()+", replying to "+message.getReplyTo().getName());
		
		Object processedBody = processor.process(message.getBody());
		
		Message replyMessage = session.createMessage();
		replyMessage.setBody(processedBody);
		replyMessage.setCorrelationId(message.getCorrelationId());
		
		session.createMessageProducer(message.getReplyTo()).sendMessage(replyMessage);
	}
	
	public void close() throws Exception {
		session.close();
	}
	
	public interface RpcRequestProcessor {
		
		Object process(Object request);
		
	}
	
}
