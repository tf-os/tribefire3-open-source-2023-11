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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

/**
 * <p>
 * {@link MessagingSession} implementation for {@link RabbitMqMessaging}.
 * 
 * @see MessagingSession
 */
public class RabbitMqSession implements MessagingSession {

	private RabbitMqConnection connection;
	private MessagingContext messagingContext;
	
	private Set<RabbitMqMessageConsumer> consumers = new HashSet<RabbitMqMessageConsumer>();
	private Set<RabbitMqMessageProducer> producers = new HashSet<RabbitMqMessageProducer>();
	
	private ReentrantLock sessionLock = new ReentrantLock();
	private long sessionLockTimeout = 10L;
	private TimeUnit sessionLockTimeoutUnit = TimeUnit.SECONDS;
	private MessagingComponentStatus status = MessagingComponentStatus.NEW;
	
	private static final Logger log = Logger.getLogger(RabbitMqSession.class);

	private Map<String, Queue> createdQueues = new HashMap<>();
	private Map<String, Topic> createdTopics = new HashMap<>();
	
	public RabbitMqConnection getConnection() {
		return connection;
	}

	@Override
	public MessageProducer createMessageProducer() throws MessagingException {
		return createMessageProducer(null);
	}

	@Override
	public MessageProducer createMessageProducer(Destination destination) throws MessagingException {

		assertOpen();
		
		RabbitMqMessageProducer producer = new RabbitMqMessageProducer();
		producer.setApplicationId(messagingContext.getApplicationId());
		producer.setNodeId(messagingContext.getNodeId());

		RabbitMqDestination rabbitMqDestination = null;
		if (destination != null) {
			validateDestination(destination);
			rabbitMqDestination = new RabbitMqDestination(destination);
			producer.setDestination(destination);
			producer.setRabbitMqDestination(rabbitMqDestination);
		}

		producer.setSession(this);
		
		synchronized (producers) {
			assertOpen();
			producers.add(producer);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("Producer created"+(rabbitMqDestination != null ? " for "+rabbitMqDestination : "")+": "+producer);
		} else if (log.isDebugEnabled()) {
			log.trace("Producer created"+(rabbitMqDestination != null ? " for "+rabbitMqDestination : ""));
		}
		
		try {
			producer.getChannel();
		} catch (Exception e) {
			log.error("Failed to pre-initialize channel", e);
		}
		
		return producer;
		
	}

	@Override
	public MessageConsumer createMessageConsumer(Destination destination) throws MessagingException {

		if (log.isDebugEnabled()) {
			log.debug("Creating consumer for [ "+destination+" ]");
		}

		assertOpen();
		
		validateDestination(destination);
		
		RabbitMqMessageConsumer consumer = new RabbitMqMessageConsumer();
		RabbitMqDestination rabbitMqDestination = new RabbitMqDestination(destination);
		 
		consumer.setDestination(destination);
		consumer.setRabbitMqDestination(rabbitMqDestination);
		consumer.setSession(this);
		consumer.setApplicationId(messagingContext.getApplicationId());
		consumer.setNodeId(messagingContext.getNodeId());
		
		synchronized (consumers) {
			assertOpen();
			consumers.add(consumer);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("Consumer created for "+rabbitMqDestination+": "+consumer);
		} else if (log.isDebugEnabled()) {
			log.debug("Consumer created for "+rabbitMqDestination);
		}
		
		return consumer;
	}

	@Override
	public void open() throws MessagingException {
		
		try {
			if (sessionLock.tryLock(sessionLockTimeout, sessionLockTimeoutUnit)) {
				try {
					
					if (status == MessagingComponentStatus.CLOSING || status == MessagingComponentStatus.CLOSED) {
						throw new MessagingException("Messaging session in unexpected state: "+status.toString().toLowerCase());
					}
					
					//asserts that the connection is opened
					getConnection().assertOpen();
					
					this.status = MessagingComponentStatus.OPEN;

					if (log.isDebugEnabled()) {
						log.debug("Messaging session opened: [ "+this+" ]");
					}
					
				} finally {
					sessionLock.unlock();
				}
			} else {
				throw new MessagingException("Failed to open the messaging session. Unable to acquire lock after "+sessionLockTimeout+" "+sessionLockTimeoutUnit.toString().toLowerCase());
			}
		} catch (InterruptedException e) {
			throw new MessagingException("Failed to open the messaging session. Unable to acquire lock after "+sessionLockTimeout+" "+sessionLockTimeoutUnit.toString().toLowerCase()+" : "+e.getMessage(), e);
		}
		
	}

	@Override
	public void close() throws MessagingException {
		
		try {
			if (sessionLock.tryLock(sessionLockTimeout, sessionLockTimeoutUnit)) {
				try {
					
					MessagingComponentStatus previousStatus = this.status;
					
					this.status = MessagingComponentStatus.CLOSING;
					
					if (previousStatus == MessagingComponentStatus.CLOSING || previousStatus == MessagingComponentStatus.CLOSED) {
						//closing an already closed connection shall be a no-op
						if (log.isDebugEnabled()) {
							log.debug("No-op close() call. Messaging session closing already requested. current state: "+previousStatus.toString().toLowerCase());
						}
						return;
					}
					
					if (previousStatus == MessagingComponentStatus.NEW) {
						if (log.isDebugEnabled()) {
							log.debug("Closing a messaging session which was not opened. current state: "+previousStatus.toString().toLowerCase());
						}
					}
					
					synchronized (consumers) {
						for (RabbitMqMessageConsumer consumer : consumers) {
							try {
								consumer.close(false);
							} catch (Throwable t) {
								log.error("Failed to close consumer created by this messaging session: "+consumer+": "+t.getMessage(), t);
							}
						}
						consumers.clear();
					}
					
					synchronized (producers) {
						for (RabbitMqMessageProducer producer : producers) {
							try {
								producer.close(false);
							} catch (Throwable t) {
								log.error("Failed to close producer created by this messaging session: "+producer+": "+t.getMessage(), t);
							}
						}
						producers.clear();
					}
					
					this.createdQueues.clear();
					this.createdTopics.clear();
					
					this.status = MessagingComponentStatus.CLOSED;
					
					if (log.isDebugEnabled()) {
						log.debug("Messaging session closed: [ "+this+" ]");
					} else if (log.isInfoEnabled()) {
						log.info("Messaging session closed");
					}
					
				} finally {
					sessionLock.unlock();
				}
			} else {
				throw createLockFailure("close the session", null);
			}
		} catch (InterruptedException e) {
			throw createLockFailure("close the session", e);
		}
		
	}

	@Override
	public Queue createQueue(String name) throws MessagingException {
		
		validateDestinationName(name);

		assertOpen();
		
		Queue queue = createdQueues.get(name);
		
		if (queue == null) {

			queue = Queue.T.create();
			queue.setName(name);
			
			getConnection().declare(queue);
			
			createdQueues.put(name, queue);
			
			if (log.isDebugEnabled()) {
				log.debug("Queue created [ "+queue+" ]");
			}
			
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Returning queue already created by this session: [ "+queue+" ]");
			}
		}
		
		return queue;
		
	}

	@Override
	public Topic createTopic(String name) throws MessagingException  {
		
		validateDestinationName(name);

		assertOpen();
		
		Topic topic = createdTopics.get(name);
		
		if (topic == null) {

			topic = Topic.T.create();
			topic.setName(name);
			
			getConnection().declare(topic);
			
			createdTopics.put(name, topic);
			
			if (log.isDebugEnabled()) {
				log.debug("Topic created [ "+topic+" ]");
			}
			
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Returning topic already created by this session: [ "+topic+" ]");
			}
		}
		
		return topic;
	}
	
	@Override
	public Message createMessage() throws MessagingException {
		return Message.T.create();
	}
	
	protected void setConnection(RabbitMqConnection connection) {
		this.connection = connection;
		this.messagingContext = connection.getConnectionProvider().getMessagingContext();
	}
	
	protected void unregisterMessageProducer(MessageProducer messageProducer) {
		
		if (log.isTraceEnabled()) {
			log.trace("Unregistering producer [ "+messageProducer+" ]");
		}
		
		synchronized (producers) {
			boolean result = producers.remove(messageProducer);
			if (log.isDebugEnabled()) {
				if (result) {
					log.debug("Unregistered producer: [ "+messageProducer+" ]");
				} else {
					log.debug("No-op unregistration. Producer not found to be unregistered: [ "+messageProducer+" ]");
				}
			}
		}
		
	}
	
	protected void unregisterMessageConsumer(MessageConsumer messageConsumer) {
		
		if (log.isTraceEnabled()) {
			log.trace("Unregistering consumer [ "+messageConsumer+" ]");
		}
		
		synchronized (consumers) {
			boolean result = consumers.remove(messageConsumer);
			if (log.isDebugEnabled()) {
				if (result) {
					log.debug("Unregistered consumer: [ "+messageConsumer+" ]");
				} else {
					log.debug("No-op unregistration. Consumer not found to be unregistered: [ "+messageConsumer+" ]");
				}
			}
		}
		
	}
	
	/**
	 * <p>
	 * Common validations over {@link Destination}(s) names.
	 * 
	 * @param name
	 *            The name to be validated
	 * @throws MessagingException
	 *             If the given name is invalid
	 */
	protected void validateDestinationName(String name) throws MessagingException {
		
		if (name == null || name.trim().isEmpty()) {
			throw new MessagingException("Destination name [ "+name+" ] is not valid");
		}
		
	}
	
	/**
	 * <p>
	 * Common validations over {@link Destination}(s).
	 * 
	 * @param destination
	 *            The destination to be validated
	 * @throws IllegalArgumentException
	 *             If the given destination is {@code null}
	 * @throws UnsupportedOperationException
	 *             If the type of the given destination instance is not supported
	 */
	protected void validateDestination(Destination destination) {
		
		if (destination == null) {
			throw new IllegalArgumentException("destination cannot be null");
		}

		if (!(destination instanceof Topic || destination instanceof Queue)) {
			throw new UnsupportedOperationException("Unsupported destination type: "+destination);
		}
		
	}
	
	private void assertOpen() throws MessagingException {
		if (status != MessagingComponentStatus.OPEN) {
			throw new MessagingException("Messaging session is not opened. Current state: "+status.toString().toLowerCase());
		}
	}
	
	private MessagingException createLockFailure(String operation, Exception cause) {
		return new MessagingException("Failed to "+operation+". Unable to acquire lock after "+sessionLockTimeout+" "+sessionLockTimeoutUnit.toString().toLowerCase(), cause);
	}

}
