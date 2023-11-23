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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint.Basic;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.service.api.ServiceRequest;

public class BasicRemoteStub implements Basic {

	private List<ServiceRequest> sentServiceRequests = new ArrayList<>();
	private boolean simulateError = false;
	private Marshaller marshaller;
	
	public BasicRemoteStub(Marshaller marshaller) {
		this.marshaller = marshaller;
	}
	
	public BasicRemoteStub(Marshaller marshaller, boolean simulateError) {
		this.marshaller = marshaller;
		this.simulateError = simulateError;
	}
	
	public List<ServiceRequest> getSentServiceRequests() {
		return sentServiceRequests;
	}

	public void setSentServiceRequests(List<ServiceRequest> sentServiceRequests) {
		this.sentServiceRequests = sentServiceRequests;
	}

	@Override
	public void setBatchingAllowed(boolean allowed) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getBatchingAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void flushBatch() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendText(String text) throws IOException {
		if(simulateError) {
			throw new IOException();
		}
		this.sentServiceRequests.add((ServiceRequest) marshaller.unmarshall(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
		
	}

	@Override
	public void sendBinary(ByteBuffer data) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendText(String partialMessage, boolean isLast) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OutputStream getSendStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Writer getSendWriter() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendObject(Object data) throws IOException, EncodeException {
		// TODO Auto-generated method stub
		
	}

}
