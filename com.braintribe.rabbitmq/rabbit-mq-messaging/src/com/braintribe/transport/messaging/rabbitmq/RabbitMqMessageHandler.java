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

import java.io.IOException;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * <p>
 * Common message handler object referencing a {@link MessagingSession} and {@link Destination}.
 * 
 */
public abstract class RabbitMqMessageHandler implements ConfirmListener, ReturnListener, ShutdownListener {

	public final static String propertyPrefix = "tf_property_";

	private RabbitMqSession session;
	private Destination destination;
	private RabbitMqDestination rabbitDestination;
	private Channel rabbitMqChannel;
	private String applicationId;
	private String nodeId;
	private boolean handlesTopic;
	
	private static final Logger log = Logger.getLogger(RabbitMqMessageHandler.class);
	
	public RabbitMqMessageHandler() {
	}
	
	public RabbitMqSession getSession() {
		return session;
	}

	public void setSession(RabbitMqSession session) {
		this.session = session;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
		this.handlesTopic = this.destination instanceof Topic;
	}

	public RabbitMqDestination getRabbitMqDestination() {
		return rabbitDestination;
	}

	public void setRabbitMqDestination(RabbitMqDestination rabbitDestination) {
		this.rabbitDestination = rabbitDestination;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public boolean handlesTopic() {
		return handlesTopic;
	}
	
	public Channel getChannel() throws MessagingException {
		
		boolean connectionIsRecoverable = getSession().getConnection().isAutomaticRecoveryEnabled();
		
		if (rabbitMqChannel == null || (!connectionIsRecoverable && !rabbitMqChannel.isOpen())) {
			synchronized(this) {
				if (rabbitMqChannel == null || (!connectionIsRecoverable && !rabbitMqChannel.isOpen())) {
					try {
						if (log.isTraceEnabled()) {
							log.trace("Creating a channel for "+this);
						}
						
						if (this instanceof MessageProducer) {
							rabbitMqChannel = getSession().getConnection().createPublishingChannel();
						} else {
							rabbitMqChannel = getSession().getConnection().createChannel();
						}
						
						rabbitMqChannel.addConfirmListener(this);
						rabbitMqChannel.addReturnListener(this);
						rabbitMqChannel.addShutdownListener(this);
						
						if (log.isDebugEnabled()) {
							log.debug("Created a channel for "+this+" : "+rabbitMqChannel);
						}
						
					} catch (Exception e) {
						throw new MessagingException("Failed to create a channel", e);
					}
				}
			}
		}
		
		return rabbitMqChannel;
		
	}
	
	public void closeChannel() throws MessagingException {
		if (rabbitMqChannel != null) {
			try {
				
				if (log.isTraceEnabled()) {
					log.trace("Closing channel for "+this+" : "+rabbitMqChannel);
				}
				
				rabbitMqChannel.close();
				
				if (log.isDebugEnabled()) {
					log.debug("Closed channel for "+this);
				}
				
			} catch (Exception e) {
				throw new MessagingException("Failed to close a channel", e);
			}
		}
	}
	
    @Override
	public void handleAck(long deliveryTag, boolean multiple) throws IOException {
    	if (log.isDebugEnabled()) {
        	log.debug(this.getClass().getSimpleName()+" for [ "+getRabbitMqDestination()+" ] handled ack delivery tag [ "+deliveryTag+" ], multiple [ "+multiple+" ]");
    	}
    }
    
    @Override
	public void handleNack(long deliveryTag, boolean multiple) throws IOException {
    	if (log.isDebugEnabled()) {
    		log.debug(this.getClass().getSimpleName()+" for [ "+getRabbitMqDestination()+" ] handled nack delivery tag [ "+deliveryTag+" ], multiple [ "+multiple+" ]");
    	}
    }

	@Override
	public void handleReturn(int replyCode, java.lang.String replyText, java.lang.String exchange, java.lang.String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
    	if (log.isDebugEnabled()) {
    		log.debug(this.getClass().getSimpleName()+" for [ "+getRabbitMqDestination()+" ] received return. "
    				+ "reply code [ "+replyCode+" ], "
    				+ "reply text [ "+replyText+" ], "
    				+ "exchange [ "+exchange+" ], "
    				+ "routing key [ "+routingKey+" ], "
    				+ "properties [ "+properties+" ], "
    				+ "body [ "+(body != null ? new String(body) : "null")+" ]");
    	}
	}

	@Override
	public void shutdownCompleted(ShutdownSignalException cause) {
    	if (log.isDebugEnabled()) {
    		log.debug(this.getClass().getSimpleName()+" for [ "+getRabbitMqDestination()+" ] received shutdown signal [ "+cause+" ]");
    	}
	}
	
}
