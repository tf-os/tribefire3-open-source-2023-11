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
import java.util.Map;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.etcd.EtcdMessageEnvelope;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.utils.RandomTools;

/**
 * <p>
 * {@link MessageProducer} implementation for {@link EtcdMessaging}.
 * 
 * @see MessageProducer
 * @author roman.kurmanowytsch
 */
public class EtcdMessageProducer extends EtcdAbstractMessageHandler implements MessageProducer  {
		
	private static final Long defaultTimeToLive = Long.valueOf(60_000L);
	private static final Integer defaultPriority = Integer.valueOf(4);
	
	private Long timeToLive = defaultTimeToLive;
	private Integer priority = defaultPriority;
	
	private MessagingComponentStatus status = MessagingComponentStatus.OPEN;
	
	private static final Logger log = Logger.getLogger(EtcdMessageProducer.class);
	
	public EtcdMessageProducer(Destination destination) {
		super.setDestination(destination);
	}
	
	@Override
	public void sendMessage(Message message) throws MessagingException {

		Destination dest = message.getDestination() != null ? message.getDestination() : getDestination(); 
		
		if (dest == null) {
			throw new UnsupportedOperationException("This method is not supported as no destination was assigned to the message producer at creation time");
		}
		
		sendMessage(message, dest);
	}

	@Override
	public void sendMessage(Message message, Destination destination) throws MessagingException {

		if (destination == null) {
			destination = super.getDestination();
		}
		
		if (message == null) {
			throw new IllegalArgumentException("message cannot be null");
		}

		if (destination == null) {
			throw new IllegalArgumentException("destination cannot be null");
		}

		message.setMessageId(RandomTools.newStandardUuid());
		message.setDestination(destination);
		
		if (message.getPriority() == null) {
			message.setPriority(priority);
		} else {
			message.setPriority(normalizePriority(message.getPriority()));
		}
		
		if (message.getTimeToLive() == null) {
			message.setTimeToLive(timeToLive);
		}
		
		if (message.getTimeToLive() > 0L) {
			message.setExpiration(System.currentTimeMillis()+message.getTimeToLive());
		} else {
			message.setExpiration(0L);
		}
		
		MessagingContext messagingContext = super.getMessagingContext();
		messagingContext.enrichOutbound(message);
		
		if (log.isTraceEnabled()) {
			log.trace("Publishing message to "+destination.getName()+": "+message+" with message ID "+message.getMessageId()+", correlation ID: "+message.getCorrelationId()+", and body "+message.getBody());
		}
		
		try {

			Map<String,String> headers = super.createMessageProperties(message, null);
			
			byte[] messageBody = messagingContext.marshallMessage(message);
			String encodedMessageBody = Base64.getEncoder().encodeToString(messageBody);
			
			EtcdMessageEnvelope envelope = EtcdMessageEnvelope.T.create();
			envelope.setMessageId(message.getMessageId());
			envelope.setBody(encodedMessageBody);
			envelope.setExpiration(message.getExpiration());
			setAddressee(envelope, message);
			
			byte[] messageEnvelope =  messagingContext.marshal(envelope);
			
			String key = connection.getDestinationKey(destination);
			key += "#"+message.getMessageId();
			
			long ttlMilliseconds = message.getTimeToLive();
			
			
			connection.put(key, messageEnvelope, (int) (ttlMilliseconds/1000));
			
			if (log.isTraceEnabled()) {
				log.trace("Published message to "+destination.getName()+": "+message+" with message ID "+message.getMessageId()+" and correlation ID: "+message.getCorrelationId());
			}
			
		} catch (Exception e) {
			throw new MessagingException("Failed to publish message: "+e.getMessage(), e);
		}
		
	}
	
	private void setAddressee(EtcdMessageEnvelope envelope, Message message) {
		Map<String, Object> props = message.getProperties();
		
		Object val = props.get(MessageProperties.addreseeAppId.getName());
		String addresseeAppId = (val != null) ? (String) val : null;
		val = props.get(MessageProperties.addreseeNodeId.getName());
		String addresseeNodeId = (val != null) ? (String) val : null;
		
		envelope.setAddresseeNodeId(addresseeNodeId);
		envelope.setAddresseeAppId(addresseeAppId);
	}

	@Override
	public void close() throws MessagingException {
		if (status == MessagingComponentStatus.CLOSED) {
			log.debug(() -> "Producer is already closed");
			return;
		}
		
		getSession().getConnection().unregisterMessageProducer(this);
		
		status = MessagingComponentStatus.CLOSED;
		
	}
	
	@Override
	public Long getTimeToLive() {
		return timeToLive;
	}

	@Override
	public void setTimeToLive(Long timeToLive) {
		if (timeToLive == null) {
			this.timeToLive = defaultTimeToLive;
		} else {
			this.timeToLive = timeToLive;
		}
	}
	
	@Override
	public Integer getPriority() {
		return priority;
	}

	@Override
	public void setPriority(Integer priority) {
		if (priority == null) {
			this.priority = defaultPriority;
		} else {
			this.priority = normalizePriority(priority);
		}
	}
	

	/**
	 * <p>
	 * Ensures that the given not-null priority fits the 0-9 range.
	 * 
	 * @param priorityCandidate
	 *            The priority to be normalized
	 * @return A priority between 0 and 9, or {@code null} if {@code null} was given.
	 */
	protected Integer normalizePriority(Integer priorityCandidate) {
		if (priorityCandidate == null) {
			return null;
		} else if (priorityCandidate < 0) {
			return 0;
		} else if (priorityCandidate > 9) {
			return 9;
		} else {
			return priorityCandidate;
		}
	}
	
	
}
