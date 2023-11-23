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
package com.braintribe.model.processing.websocket.server.stub;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.MessageHandler.Partial;
import javax.websocket.MessageHandler.Whole;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

public class SessionStub implements Session {

	private boolean open;
	private Basic basicRemote;
	private Map<String, List<String>> params;
	
	public SessionStub(String sessionId, String clientId, String accept) {
		this(sessionId, clientId, accept, null);
	}
	
	public SessionStub(String sessionId, String clientId, String accept, Basic basicRemote) {
		this.open = true;
		this.basicRemote = basicRemote;
		initPathParamMap(sessionId, clientId, accept);
	}
	
	private void initPathParamMap(String sessionId, String clientId, String accept) {
		this.params = new HashMap<>();
		if(sessionId != null) {
			this.params.put("sessionId", Arrays.asList(sessionId));
		}
		if(clientId != null) {
			this.params.put("clientId", Arrays.asList(clientId));
		}
		if(accept != null) {
			this.params.put("accept", Arrays.asList(accept));
		}
	}
	
	public void setBasicRemote(Basic basicRemote) {
		this.basicRemote = basicRemote;
	}

	@Override
	public WebSocketContainer getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void addMessageHandler(Class<T> clazz, Whole<T> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void addMessageHandler(Class<T> clazz, Partial<T> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<MessageHandler> getMessageHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeMessageHandler(MessageHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProtocolVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNegotiatedSubprotocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Extension> getNegotiatedExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public long getMaxIdleTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxIdleTimeout(long milliseconds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMaxBinaryMessageBufferSize(int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxBinaryMessageBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxTextMessageBufferSize(int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxTextMessageBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Async getAsyncRemote() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Basic getBasicRemote() {
		return basicRemote;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		this.open = false;
	}

	@Override
	public void close(CloseReason closeReason) throws IOException {
		this.open = false;
	}

	@Override
	public URI getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, List<String>> getRequestParameterMap() {
		return params;
	}

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getPathParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getUserProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Session> getOpenSessions() {
		// TODO Auto-generated method stub
		return null;
	}

}
