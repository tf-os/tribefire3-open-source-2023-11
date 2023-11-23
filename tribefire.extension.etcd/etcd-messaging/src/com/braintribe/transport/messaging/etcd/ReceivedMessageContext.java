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
package com.braintribe.transport.messaging.etcd;

import java.util.Base64;
import java.util.Set;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.etcd.EtcdMessageEnvelope;
import com.braintribe.transport.messaging.api.MessagingContext;

public class ReceivedMessageContext {

	private String destination;
	private String messageId;
	private Message unmarshalledMessage;
	private Set<EtcdMessageConsumer> registeredConsumers;
	private long modRevision;
	private String stringKey;
	private EtcdMessageEnvelope envelope;
	private MessagingContext messagingContext;
	
	public ReceivedMessageContext(String stringKey, byte[] valueBytes, MessagingContext messagingContext) {
		
		this.stringKey = stringKey;
		this.messagingContext = messagingContext;
		int index = stringKey.lastIndexOf('/');
		String destinationPlusMessageId = stringKey.substring(index + 1);
		index = destinationPlusMessageId.indexOf('#');
		this.destination = destinationPlusMessageId.substring(0, index);
		this.messageId = destinationPlusMessageId.substring(index + 1);
		this.envelope = messagingContext.unmarshal(valueBytes);
	}
	
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public Message getUnmarshalledMessage() {
		if (unmarshalledMessage == null) {
			String encodedMessageBody = envelope.getBody();
			try {
				byte[] messageBytes = Base64.getDecoder().decode(encodedMessageBody);
				this.unmarshalledMessage = messagingContext.unmarshallMessage(messageBytes);
			} catch(Exception e) {
				throw Exceptions.unchecked(e, "Could not decode message: "+encodedMessageBody);
			}
		}
		return unmarshalledMessage;
	}
	public void setUnmarshalledMessage(Message unmarshalledMessage) {
		this.unmarshalledMessage = unmarshalledMessage;
	}
	public Set<EtcdMessageConsumer> getRegisteredConsumers() {
		return registeredConsumers;
	}
	public void setRegisteredConsumers(Set<EtcdMessageConsumer> registeredConsumers) {
		this.registeredConsumers = registeredConsumers;
	}
	public long getModRevision() {
		return modRevision;
	}
	public void setModRevision(long modRevision) {
		this.modRevision = modRevision;
	}
	public String getStringKey() {
		return stringKey;
	}
	public EtcdMessageEnvelope getEnvelope() {
		return envelope;
	}
	public void setEnvelope(EtcdMessageEnvelope envelope) {
		this.envelope = envelope;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Received message ");
		sb.append(stringKey);
		sb.append(": ");
		sb.append(messageId);
		sb.append(" to destination: ");
		sb.append(destination);
		sb.append(": ");
		sb.append(unmarshalledMessage != null ? unmarshalledMessage.getBody() : "<null>");
		return sb.toString();
	}
}
