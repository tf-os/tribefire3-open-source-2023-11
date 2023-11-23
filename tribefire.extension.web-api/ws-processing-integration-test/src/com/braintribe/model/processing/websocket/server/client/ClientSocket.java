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
package com.braintribe.model.processing.websocket.server.client;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Class that represent a websocket client.
 * 
 */
@WebSocket
public class ClientSocket
{
	private Session session;
	private List<ServiceRequest> receivedServiceRequests;
	private Marshaller marshaller;
	
	public ClientSocket(Marshaller marshaller) {
		this.receivedServiceRequests = new ArrayList<>();
		this.marshaller = marshaller;
	}
	
	public List<ServiceRequest> getReceivedServiceRequests() {
		return receivedServiceRequests;
	}

	@OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
    }
	
	@SuppressWarnings("unused")
	@OnWebSocketClose
	public void onClose(Session session, int statusCode, String reason) {
		 this.session = null;
	}
	
	@OnWebSocketMessage
	public void onMessage(String text) throws Exception {
	    receivedServiceRequests.add((ServiceRequest) marshaller.unmarshall(new ByteArrayInputStream(text.getBytes())));
	}
	
	public boolean isConnectionOpen() {
		return session != null;
	}
	
	public boolean isMessageReceived() {
		return !receivedServiceRequests.isEmpty();
	}
	
	public boolean ping() {
		try {
			session.getRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public void closeConnection() {
		session.close();
	}
	
	public void harshlyCloseConnection() throws Exception {
		session.disconnect();
	}

	
}