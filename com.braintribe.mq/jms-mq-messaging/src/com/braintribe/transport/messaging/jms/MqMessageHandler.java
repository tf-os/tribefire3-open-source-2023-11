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
package com.braintribe.transport.messaging.jms;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.messaging.jms.AcknowledgeMode;
import com.braintribe.model.messaging.jms.JmsMqConnection;
import com.braintribe.transport.messaging.api.MessagingException;
import com.ibm.mq.jms.MQDestination;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQSession;
import com.ibm.mq.jms.MQTopic;

public abstract class MqMessageHandler implements Closeable {

	private static final Logger logger = Logger.getLogger(MqMessageHandler.class);
	
	protected JmsMqSession session;
	protected MQSession jmsSession;
	
	protected Map<Destination,MQDestination> destinations = new HashMap<>();
	protected ReentrantLock destinationsLock = new ReentrantLock();

	
	public MqMessageHandler(JmsMqSession session) {
		this.session = session;
	}
	
	public abstract void reset();
	
	@Override
	public void close() {
		if (jmsSession != null) {
			try {
				jmsSession.close();
			} catch(Exception e) {
				logger.error("Error while closing JMS session.", e);
			} finally {
				jmsSession = null;
			}
		}
	}
	
	protected void ensureJmsSession() throws MessagingException {
		if (jmsSession != null) {
			return;
		}
		int errorCount = 0;

		com.braintribe.transport.messaging.jms.JmsMqConnection connection = this.session.getConnection();
		connection.open();
		JmsMqConnection configuration = connection.getConfiguration();
		Connection jmsConnection = null;
		
		while (true) {

			try {
				jmsConnection = connection.getJmsConnection();
				
				int jmsAcknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
				AcknowledgeMode acknowledgeMode = configuration.getAcknowledgeMode();
				if (acknowledgeMode != null) {
					if (acknowledgeMode.equals(AcknowledgeMode.AUTO)) {
						jmsAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;
					}
				}
				boolean transacted = configuration.getTransacted();
				jmsSession = (MQSession) jmsConnection.createSession(transacted, jmsAcknowledgeMode);

				if (errorCount <= 1) {
					TemporaryQueue tq = jmsSession.createTemporaryQueue();
					tq.delete();
				}
				
				return;

			} catch(Exception e) {
				errorCount++;

				if (errorCount <= 2) {
					JmsMqMessagingUtils.logError(logger, e, "Could not create session from connection "+jmsConnection+". Retrying...");
					connection.reconnect();
				} else {
					throw new MessagingException("Could not create session from connection "+jmsConnection, e);
				}
			}
		}
	}


	protected javax.jms.Destination createJmsDestination(Destination destination) throws MessagingException {
		if (destination == null) {
			throw new MessagingException("No destination has been specified.");
		}
		String name = destination.getName();

		MQDestination knownJmsDestination = null;
		this.destinationsLock.lock();
		try {
			knownJmsDestination = JmsMqMessagingUtils.getJmsDestination(this.destinations, destination);
		} finally {
			this.destinationsLock.unlock();
		}

		if (knownJmsDestination != null) {
			return knownJmsDestination;
		}

		int errorCount = 0;

		com.braintribe.transport.messaging.jms.JmsMqConnection connection = this.session.getConnection();
		
		while(true) {

			try {

				if (destination instanceof Queue) {
					MQQueue queue = null;
					try {
						queue = (MQQueue) jmsSession.createQueue(name);
					} catch (Exception e) {
						throw new MessagingException("Error while creating JMS queue "+name, e);
					}
					this.destinationsLock.lock();
					try {
						this.destinations.put(destination, queue);
					} finally {
						this.destinationsLock.unlock();
					}
					return queue;
				} else if (destination instanceof Topic) {
					MQTopic topic = null;
					try {
						topic = (MQTopic) jmsSession.createTopic(name);
					} catch (Exception e) {
						throw new MessagingException("Error while creating JMS topic "+name, e);
					}			
					this.destinationsLock.lock();
					try {
						this.destinations.put(destination, topic);
					} finally {
						this.destinationsLock.unlock();
					}
					return topic;
				} else {
					throw new MessagingException("Unsupported destination type "+destination);
				}

			} catch(Exception e) {

				errorCount++;

				if (errorCount == 1) {
					JmsMqMessagingUtils.logError(logger, e, "Could not create JMS destination based on "+destination+". Retrying...");
					connection.reconnect();
				} else {
					throw new MessagingException("Could not create JMS destination based on "+destination, e);
				}

			}
		}
	}

	public JmsMqSession getSession() {
		return session;
	}

}
