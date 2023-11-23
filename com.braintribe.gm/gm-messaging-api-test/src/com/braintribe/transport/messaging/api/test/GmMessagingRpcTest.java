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

import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.test.rpc.GmMessagingRpcTestClient;
import com.braintribe.transport.messaging.api.test.rpc.GmMessagingRpcTestServer;
import com.braintribe.transport.messaging.api.test.rpc.GmMessagingRpcTestServer.RpcRequestProcessor;

/**
 * <p>
 * Tests the rpc-over-messaging model through {@code GmMessagingApi}.
 * 
 */
public abstract class GmMessagingRpcTest extends GmMessagingTest {

	@Test
	public void testRpcMessaging() throws Exception {
		
		final MessagingConnection connection = getMessagingConnectionProvider().provideMessagingConnection();
		final MessagingSession session = connection.createMessagingSession();
		final MessagingSession session2 = connection.createMessagingSession();
		
		Supplier<MessagingSession> sessionProvider = new Supplier<MessagingSession>() {
			@Override
			public MessagingSession get() throws RuntimeException {
				return session;
			}
		};
		Supplier<MessagingSession> sessionProvider2 = new Supplier<MessagingSession>() {
			@Override
			public MessagingSession get() throws RuntimeException {
				return session2;
			}
		};
		
		GmMessagingRpcTestServer testServer = new GmMessagingRpcTestServer(sessionProvider, "rpc-queue", new RpcRequestProcessor() {
			@Override
			public Object process(Object request) {
				return "RESPONSE TO "+request;
			}
		});


		GmMessagingRpcTestClient client = new GmMessagingRpcTestClient(sessionProvider2, "rpc-queue", "rpc-queue-reply");
		
		Object response = client.call("REQUEST");
		
		System.out.println("RECEIVED: "+response);
		
		testServer.close();
		connection.close();

	}
	
}
