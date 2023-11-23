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
package com.braintribe.transport.messaging.rabbitmq;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * <p>
 * {@link MessageProducer} implementation for {@link RabbitMqMessaging}.
 * 
 * @see MessageProducer
 */
public class RabbitMqMessageProducer extends RabbitMqMessageHandler implements MessageProducer  {
	
	private static final String contentType = "application/octet-stream";
	private static final String contentEncoding = "UTF-8";
	
	private static final Long defaultTimeToLive = Long.valueOf(0L);
	private static final Integer defaultPriority = Integer.valueOf(4);
	
	private Long timeToLive = defaultTimeToLive;
	private Integer priority = defaultPriority;
	
	private MessagingComponentStatus status = MessagingComponentStatus.OPEN;
	
	private static final Logger log = Logger.getLogger(RabbitMqMessageProducer.class);
	
	public RabbitMqMessageProducer() {
		super();
	}
	
	@Override
	public void sendMessage(Message message) throws MessagingException {

		if (getDestination() == null) {
			throw new UnsupportedOperationException("This method is not supported as no destination was assigned to the message producer at creation time");
		}

		sendMessage(message, getDestination());
	}

	@Override
	public void sendMessage(Message message, Destination destination) throws MessagingException {

		if (message == null) {
			throw new IllegalArgumentException("message cannot be null");
		}

		if (destination == null) {
			throw new IllegalArgumentException("destination cannot be null");
		}

		message.setMessageId(UUID.randomUUID().toString());
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
		MessagingContext messagingContext = getSession().getConnection().getConnectionProvider().getMessagingContext();
		
		RabbitMqDestination rabbitMqDestination = getRabbitMqDestination();
		if (destination != getDestination()) {
			rabbitMqDestination = new RabbitMqDestination(destination);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("Publishing message to "+rabbitMqDestination+": "+message);
		}
		
		try {
			byte[] messageBody = messagingContext.marshal(message);
			
			AMQP.BasicProperties properties = getProperties(message, null);
			
			synchronized (this) {
				getChannel().basicPublish(rabbitMqDestination.getExchangeName(), rabbitMqDestination.getRoutingKey(), true, properties, messageBody);
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Published message to "+rabbitMqDestination+": "+message);
				logDelivery(rabbitMqDestination.getExchangeName(), rabbitMqDestination.getRoutingKey(), true, properties, messageBody, LogLevel.DEBUG);
			}
			
		} catch (Exception e) {
			throw new MessagingException("Failed to publish message: "+e.getMessage(), e);
		}
		
	}
	
	@Override
	public void close() throws MessagingException {
		close(true);
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
	
	protected synchronized void close(boolean unregisterFromSession) throws MessagingException {
		
		if (status == MessagingComponentStatus.CLOSED) {
			if (log.isDebugEnabled()) {
				log.debug("Producer is already closed");
			}
			return;
		}
		
		closeChannel();
		
		if (unregisterFromSession) {
			getSession().unregisterMessageProducer(this);
		}
		
		status = MessagingComponentStatus.CLOSED;
		
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
	
	protected AMQP.BasicProperties getProperties(Message message, String mimeType) {
		
		if (mimeType == null || mimeType.trim().isEmpty()) {
			mimeType = contentType;
		}
		
		AMQP.BasicProperties.Builder propBuilder = new AMQP.BasicProperties.Builder();
		
		propBuilder.contentType(mimeType).contentEncoding(contentEncoding).deliveryMode((message.getPersistent()) ? 2 : 1);

		if (message.getMessageId() != null) {
			propBuilder.messageId(message.getMessageId());
		}
		
		if (message.getPriority() != null) {
			propBuilder.priority(message.getPriority());
		}
		
		//the AMQP expiration is actually the time to live, not the absolute expiration time. 
		//It must be set ONLY if greater than 0 as, unlike GM Messaging and JMS, AMQP doesn't consider expiration 0 as never expiring.
		if (message.getTimeToLive() != null && message.getTimeToLive() > 0L) {
			propBuilder.expiration(Long.toString(message.getTimeToLive()));
		}
		
		if (message.getCorrelationId() != null) {
			propBuilder.correlationId(message.getCorrelationId());
		}
		
		if (message.getReplyTo() != null) {
			propBuilder.replyTo(message.getReplyTo().getName());
		}

		Map<String, Object> propHeaders = new HashMap<String, Object>();

		Map<String, Object> headers = message.getHeaders();
		if (headers != null && !headers.isEmpty()) {
			propHeaders.putAll(headers);
		}

		Map<String, Object> properties = message.getProperties();
		if (properties != null && !properties.isEmpty()) {
			properties.forEach((k, v) -> propHeaders.put(propertyPrefix + k, v));
		}

		if (!propHeaders.isEmpty()) {
			propBuilder.headers(propHeaders);
		}

		return propBuilder.build();
	}
	
	private static void logDelivery(String exchange, String routingKey, boolean mandatory, BasicProperties properties, byte[] body, LogLevel logLevel) {
		
		if (!log.isLevelEnabled(logLevel)) {
			return;
		}
		
        StringBuilder sb = new StringBuilder();
        sb.append("\r\nPublished message details:");
        sb.append("\r\n- exchange:      [").append(exchange).append("]");
        sb.append("\r\n- routing key:   [").append(routingKey).append("]");
        sb.append("\r\n- content type:  [").append(properties.getContentType()).append("]");
        sb.append("\r\n- delivery mode: [").append(properties.getDeliveryMode()).append("]");
        sb.append("\r\n- expiration:    [").append(properties.getExpiration()).append("]");
        sb.append("\r\n- priority:      [").append(properties.getPriority()).append("]");
        sb.append("\r\n- mandatory:     [").append(mandatory).append("]");
        sb.append("\r\n- body:          [").append(body != null ? new String(body) : "null").append("]");
        
        log.log(logLevel, sb.toString());
	}
	
}
