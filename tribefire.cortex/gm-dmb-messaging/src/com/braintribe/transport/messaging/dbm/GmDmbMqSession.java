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
package com.braintribe.transport.messaging.dbm;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
 * {@link MessagingSession} implementation for {@link GmDmbMqMessaging}.
 * 
 * @see MessagingSession
 */
public class GmDmbMqSession implements MessagingSession {

	private GmDmbMqConnection connection;
	private MessagingContext messagingContext;

	private Set<MessageConsumer> consumers = new HashSet<>();
	private Set<MessageProducer> producers = new HashSet<>();

	private ReentrantLock connectionLock = new ReentrantLock();
	private long connectionLockTimeout = 2L;
	private TimeUnit connectionLockTimeoutUnit = TimeUnit.SECONDS;
	private MessagingComponentStatus status = MessagingComponentStatus.NEW;

	private static final Logger log = Logger.getLogger(GmDmbMqSession.class);

	public GmDmbMqConnection getConnection() {
		return connection;
	}

	public void setConnection(GmDmbMqConnection connection) {
		this.connection = connection;
		this.messagingContext = connection.getConnectionProvider().getMessagingContext();
	}

	public MessagingContext getMessagingContext() {
		return messagingContext;
	}

	@Override
	public MessageProducer createMessageProducer() throws MessagingException {
		return createMessageProducer(null);
	}

	@Override
	public MessageProducer createMessageProducer(Destination destination) throws MessagingException {

		if (destination != null) {
			validateDestination(destination);
		}

		assertOpen();

		GmDmbMqMessageProducer producer = new GmDmbMqMessageProducer();
		producer.setDestination(destination);
		producer.setSession(this);
		producer.setApplicationId(messagingContext.getApplicationId());
		producer.setNodeId(messagingContext.getNodeId());

		producers.add(producer);

		return producer;

	}

	@Override
	public MessageConsumer createMessageConsumer(Destination destination) throws MessagingException {

		validateDestination(destination);

		assertOpen();

		GmDmbMqMessageConsumer consumer = new GmDmbMqMessageConsumer(UUID.randomUUID().toString());

		consumer.setDestination(destination);
		consumer.setSession(this);
		consumer.setApplicationId(messagingContext.getApplicationId());
		consumer.setNodeId(messagingContext.getNodeId());

		if (destination instanceof Topic) {
			getConnection().getMessagingMBean().subscribeTopicConsumer(destination.getName(), consumer.getConsumerId());
			if (log.isTraceEnabled()) {
				log.trace("Registered topic consumer [ " + consumer + " ] to the MessagingMBean");
			}
		}

		consumers.add(consumer);

		if (log.isDebugEnabled()) {
			log.debug("Opened consumer [ " + consumer + " ]");
		}

		return consumer;
	}

	@Override
	public void open() throws MessagingException {

		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {

					if (status == MessagingComponentStatus.CLOSING || status == MessagingComponentStatus.CLOSED) {
						throw new MessagingException("Messaging session in unexpected state: " + status.toString().toLowerCase());
					}

					// asserts that the connection is opened
					getConnection().assertOpen();

					if (status == MessagingComponentStatus.OPEN) {
						// opening an already opened connection shall be a no-op
						if (log.isDebugEnabled()) {
							log.debug("Messaging session already opened.");
						}
						return;
					}

					this.status = MessagingComponentStatus.OPEN;

				} finally {
					connectionLock.unlock();
				}
			} else {
				throw new MessagingException("Failed to open the messaging session. Unable to acquire lock after " + connectionLockTimeout + " "
						+ connectionLockTimeoutUnit.toString().toLowerCase());
			}
		} catch (InterruptedException e) {
			throw new MessagingException("Failed to open the messaging session. Unable to acquire lock after " + connectionLockTimeout + " "
					+ connectionLockTimeoutUnit.toString().toLowerCase() + " : " + e.getMessage(), e);
		}

	}

	@Override
	public void close() throws MessagingException {

		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {

					if (status == MessagingComponentStatus.CLOSING || status == MessagingComponentStatus.CLOSED) {
						// closing an already closed connection shall be a no-op
						if (log.isDebugEnabled()) {
							log.debug("No-op close() call. Messaging session closing already requested. current state: "
									+ status.toString().toLowerCase());
						}
						return;
					}

					if (status == MessagingComponentStatus.NEW) {
						if (log.isDebugEnabled()) {
							log.debug("Closing a messaging session which was not opened. current state: " + status.toString().toLowerCase());
						}
					}

					this.status = MessagingComponentStatus.CLOSING;

					for (MessageConsumer consumer : consumers) {
						try {
							consumer.close();
						} catch (Throwable t) {
							log.error("Failed to close consumer created by this messaging session: " + consumer + ": " + t.getMessage(), t);
						}
					}

					this.consumers = null;
					this.producers = null;

					if (log.isDebugEnabled()) {
						log.debug("Messaging session closed.");
					}

					this.status = MessagingComponentStatus.CLOSED;

				} catch (Throwable t) {
					log.error(t);
				} finally {
					connectionLock.unlock();
				}
			} else {
				throw new MessagingException("Failed to close the messaging connection. Unable to acquire lock after " + connectionLockTimeout + " "
						+ connectionLockTimeoutUnit.toString().toLowerCase());
			}
		} catch (InterruptedException e) {
			throw new MessagingException("Failed to close the messaging connection. Unable to acquire lock after " + connectionLockTimeout + " "
					+ connectionLockTimeoutUnit.toString().toLowerCase() + " : " + e.getMessage(), e);
		}

	}

	@Override
	public Queue createQueue(String name) throws MessagingException {

		validateDestinationName(name);

		assertOpen();

		Queue queue = Queue.T.create();
		queue.setName(name);

		return queue;
	}

	@Override
	public Topic createTopic(String name) throws MessagingException {

		validateDestinationName(name);

		assertOpen();

		Topic topic = Topic.T.create();
		topic.setName(name);

		return topic;
	}

	@Override
	public Message createMessage() throws MessagingException {
		return Message.T.create();
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
			throw new MessagingException("Destination name [ " + name + " ] is not valid");
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
			throw new UnsupportedOperationException("Unsupported destination type: " + destination);
		}

	}

	/**
	 * <p>
	 * Asserts that this messaging session is in a valid state to be used: Already open. not "closing" nor "closed";
	 * 
	 * <p>
	 * This method does not try to open the messaging session.
	 * 
	 * @throws MessagingException
	 *             If this connection is NOT in a valid state to be used
	 */
	protected void assertOpen() throws MessagingException {

		try {
			if (connectionLock.tryLock(connectionLockTimeout, connectionLockTimeoutUnit)) {
				try {
					if (status != MessagingComponentStatus.OPEN) {
						throw new MessagingException("Messaging session is not opened. Current state: " + status.toString().toLowerCase());
					}
				} finally {
					connectionLock.unlock();
				}
			} else {
				throw new MessagingException("Failed to assert the state of the messaging session. Unable to acquire lock after "
						+ connectionLockTimeout + " " + connectionLockTimeoutUnit.toString().toLowerCase());
			}
		} catch (InterruptedException e) {
			throw new MessagingException("Failed to assert the state of the messaging session. Unable to acquire lock after " + connectionLockTimeout
					+ " " + connectionLockTimeoutUnit.toString().toLowerCase() + " : " + e.getMessage(), e);
		}

	}

	@Override
	public String toString() {
		return "DMB Messaging";
	}
}
