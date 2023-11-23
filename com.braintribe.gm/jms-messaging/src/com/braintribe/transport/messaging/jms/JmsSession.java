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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.Session;
import javax.jms.TemporaryQueue;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

public class JmsSession implements MessagingSession {

	private static final Logger logger = Logger.getLogger(JmsSession.class);

	private Session jmsSession = null;
	protected boolean closed = false;
	protected Map<Destination,javax.jms.Destination> destinations = new HashMap<Destination,javax.jms.Destination>();
	protected ReentrantLock destinationsLock = new ReentrantLock();
	protected JmsConnection connection = null;
	private String topicSelector;

	protected Set<JmsMessageConsumer> messageConsumers = new HashSet<JmsMessageConsumer>();
	protected ReentrantLock messageConsumersLock = new ReentrantLock();

	public JmsSession(Session jmsSession, JmsConnection connection) {
		this.jmsSession = jmsSession;
		this.connection = connection;
	}

	@Override
	public void open() throws MessagingException {
		//Nothing to do
	}

	@Override
	public void close() throws MessagingException {
		if (this.closed) {
			//Nothing to do anymore
			return;
		}
		this.closed = true;
		try {
			this.jmsSession.close();
		} catch(Exception e) {
			throw new MessagingException("Could not close JMS session.", e);
		} finally {
			this.connection.sessionClosed(this);
			this.jmsSession = null;
		}
	}

	protected void assertOpen() throws MessagingException {
		if (this.closed) {
			throw new MessagingException("This session has already been closed.");
		}
	}

	protected javax.jms.Destination createJmsDestination(Destination destination) throws MessagingException {
		if (destination == null) {
			throw new MessagingException("No destination has been specified.");
		}
		String name = destination.getName();

		javax.jms.Destination knownJmsDestination = null;
		this.destinationsLock.lock();
		try {
			knownJmsDestination = JmsMessagingUtils.getJmsDestination(this.destinations, destination);
		} finally {
			this.destinationsLock.unlock();
		}

		if (knownJmsDestination != null) {
			return knownJmsDestination;
		}

		int errorCount = 0;

		while(true) {

			try {

				if (destination instanceof Queue) {
					javax.jms.Queue queue = null;
					try {
						queue = this.connection.connectionProvider.getQueue(this, name);
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
					javax.jms.Topic topic = null;
					try {
						topic = this.connection.connectionProvider.getTopic(this, name);
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
					JmsMessagingUtils.logError(logger, e, "Could not create JMS destination based on "+destination+". Retrying...");
					this.reconnect();
				} else {
					throw new MessagingException("Could not create JMS destination based on "+destination, e);
				}

			}
		}
	}

	protected Destination createDestinationFromJmsDestination(javax.jms.Destination jmsDestination) throws MessagingException {
		if (jmsDestination == null) {
			return null;
		}

		this.destinationsLock.lock();
		try {
			for (Map.Entry<Destination,javax.jms.Destination> entry : this.destinations.entrySet()) {
				javax.jms.Destination storedJmsDestination = entry.getValue();
				if (JmsMessagingUtils.compareJmsDestination(storedJmsDestination, jmsDestination)) {
					return entry.getKey();
				}
			}
		} finally {
			this.destinationsLock.unlock();
		}

		String destinationName = JmsMessagingUtils.getJmsDestinationName(jmsDestination);
		Destination destination = null;
		if (jmsDestination instanceof javax.jms.Queue) {

			try {
				destination = this.createQueue(destinationName);
			} catch (Exception e) {
				throw new MessagingException("Could not create queue "+destinationName, e);
			}

		} else if (jmsDestination instanceof javax.jms.Topic) {
			try {
				destination = this.createTopic(destinationName);
			} catch(Exception e) {
				throw new MessagingException("Could not create topic "+destinationName, e);
			}
		} else {
			throw new MessagingException("Unsupported JMS destination type "+jmsDestination);
		}

		this.destinationsLock.lock();
		try {
			this.destinations.put(destination, jmsDestination);
		} finally {
			this.destinationsLock.unlock();
		}
		return destination;
	}

	@Override
	public Queue createQueue(String name) throws MessagingException {
		this.assertOpen();

		int errorCount = 0;

		while(true) {

			try {
				javax.jms.Queue jmsQueue = this.connection.connectionProvider.getQueue(this, name);

				Queue queue = Queue.T.create();
				queue.setName(name);

				this.destinationsLock.lock();
				try {
					this.destinations.put(queue, jmsQueue);
				} finally {
					this.destinationsLock.unlock();
				}
				return queue;

			} catch(Exception e) {

				errorCount++;

				if (errorCount == 1) {
					JmsMessagingUtils.logError(logger, e, "Could not create queue "+name+". Retrying...");
					this.reconnect();
				} else {
					throw new MessagingException("Could not create queue "+name, e);
				}

			}

		}
	}

	@Override
	public Topic createTopic(String name) throws MessagingException {
		this.assertOpen();

		int errorCount = 0;

		while(true) {

			try {
				javax.jms.Topic jmsTopic = this.connection.connectionProvider.getTopic(this, name);

				Topic topic = Topic.T.create();
				topic.setName(name);

				this.destinationsLock.lock();
				try {
					this.destinations.put(topic, jmsTopic);
				} finally {
					this.destinationsLock.unlock();
				}
				return topic;

			} catch(Exception e) {

				errorCount++;

				if (errorCount == 1) {
					JmsMessagingUtils.logError(logger, e, "Could not create topic "+name+". Retrying...");
					this.reconnect();
				} else {
					throw new MessagingException("Could not create topic "+name);
				}

			}

		}
	}

	@Override
	public Message createMessage() throws MessagingException {
		return Message.T.create();
	}

	@Override
	public MessageProducer createMessageProducer() throws MessagingException {
		return createMessageProducer(null);
	}

	@Override
	public MessageProducer createMessageProducer(Destination destination) throws MessagingException {
		this.assertOpen();

		String destinationName = null;
		try {

			JmsMessageProducer messageProducer = null;

			if (destination != null) {

				destinationName = destination.getName();

				javax.jms.Destination jmsDestination = this.createJmsDestination(destination);

				if (jmsDestination == null) {
					throw new MessagingException("The destination " + destinationName + " is not known.");
				}

				javax.jms.MessageProducer jmsMessageProducer = this.createJmsMessageProducer(destination);
				messageProducer = new JmsMessageProducer(jmsMessageProducer, this);
				messageProducer.setDestination(destination);
				messageProducer.setJmsDestination(jmsDestination);

			} else {
				javax.jms.MessageProducer jmsMessageProducer = this.createJmsMessageProducer(null);
				messageProducer = new JmsMessageProducer(jmsMessageProducer, this);
			}

			messageProducer.setSession(this);

			return messageProducer;

		} catch (Exception e) {
			throw new MessagingException("Could not create a new MessageProducer " + ((destinationName == null) ? "without specified destination" : "for the destination " + destinationName), e);
		}
	}

	protected javax.jms.MessageProducer createJmsMessageProducer(Destination destination) throws MessagingException {
		this.assertOpen();

		int errorCount = 0;

		while (true) {

			String destinationName = null;
			try {
				javax.jms.Destination jmsDestination = null;

				if (destination != null) {

					destinationName = destination.getName();

					jmsDestination = this.createJmsDestination(destination);

					if (jmsDestination == null) {
						throw new MessagingException("The destination " + destinationName + " is not known.");
					}
				}

				javax.jms.MessageProducer jmsMessageProducer = this.jmsSession.createProducer(jmsDestination);
				return jmsMessageProducer;

			} catch (Exception e) {

				String errorCompl = (destinationName == null) ? "without specified destination" : "for the destination " + destinationName;

				errorCount++;

				if (errorCount == 1) {
					JmsMessagingUtils.logError(logger, e, "Could not create a new JMS MessageProducer  " + errorCompl + ". Retrying...");
					this.reconnect();
				} else {
					throw new MessagingException("Could not create a new JMS MessageProducer " + errorCompl, e);
				}

			}
		}
	}

	@Override
	public MessageConsumer createMessageConsumer(Destination destination) throws MessagingException {
		this.assertOpen();

		String destinationName = null;
		try {
			destinationName = destination.getName();

			javax.jms.Destination jmsDestination = this.createJmsDestination(destination);
			if (jmsDestination == null) {
				throw new MessagingException("The destination "+destinationName+" is not known.");
			}
			javax.jms.MessageConsumer jmsMessageConsumer = this.createJmsMessageConsumer(destination);
			JmsMessageConsumer messageConsumer = new JmsMessageConsumer(jmsMessageConsumer, this);
			messageConsumer.setDestination(destination);
			messageConsumer.setJmsDestination(jmsDestination);
			messageConsumer.setSession(this);

			messageConsumersLock.lock();
			try {
				messageConsumers.add(messageConsumer);
			} finally {
				messageConsumersLock.unlock();
			}

			return messageConsumer;

		} catch(Exception e) {
			throw new MessagingException("Could not create a new MessageConsumer for the destination "+destinationName, e);
		}
	}

	protected javax.jms.MessageConsumer createJmsMessageConsumer(Destination destination) throws MessagingException {
		this.assertOpen();

		int errorCount = 0;

		while(true) {

			String destinationName = null;
			try {
				destinationName = destination.getName();

				javax.jms.Destination jmsDestination = this.createJmsDestination(destination);
				if (jmsDestination == null) {
					throw new MessagingException("The destination "+destinationName+" is not known.");
				}
				javax.jms.MessageConsumer jmsMessageConsumer = null;
				if (jmsDestination instanceof javax.jms.Topic) {
					jmsMessageConsumer = this.jmsSession.createConsumer(jmsDestination, getTopicSelector());
				} else {
					jmsMessageConsumer = this.jmsSession.createConsumer(jmsDestination);
				}

				return jmsMessageConsumer;

			} catch(Exception e) {

				errorCount++;

				if (errorCount == 1) {
					JmsMessagingUtils.logError(logger, e, "Could not create a new JMS MessageConsumer for the destination "+destinationName+". Retrying...");
					this.reconnect();
				} else {
					throw new MessagingException("Could not create a new JMS MessageConsumer for the destination "+destinationName, e);
				}
			}
		}
	}
	protected void reconnect() throws MessagingException {
		
		Session oldSession = this.jmsSession;
				
		try {
			this.jmsSession = this.connection.createJmsSession();
			this.closed = false;

			messageConsumersLock.lock();
			try {
				for (JmsMessageConsumer consumer : this.messageConsumers) {
					consumer.resetMessageListener();
				}
			} finally {
				messageConsumersLock.unlock();
			}

		} finally {
			if (oldSession != null) {
				try {
					oldSession.close();
				} catch(Exception e) {
					logger.debug("Error while closing old JMS session after reconnecting.", e);
				}
			}
		}
	}

	public JmsConnection getConnection() {
		return connection;
	}

	public Session getJmsSession() {
		return this.jmsSession;
	}

	protected void messageConsumerClosed(JmsMessageConsumer jmsMessageConsumer) {
		messageConsumersLock.lock();
		try {
			messageConsumers.remove(jmsMessageConsumer);
		} finally {
			messageConsumersLock.unlock();
		}
	}

	protected void resetSessionIfNecessary() throws MessagingException {
		if (!this.isSessionValid()) {
			this.reconnect();
		}
	}
	
	protected boolean isSessionValid() {
		TemporaryQueue tq = null;
		try {
			tq = jmsSession.createTemporaryQueue();
			return true;
		} catch(Exception e) {
			logger.debug("Session is not valid.", e);
			return false;
		} finally {
			if (tq != null) {
				try {
					tq.delete();
				} catch(Exception e) {
					logger.debug("Could not delete temporary queue "+tq, e);
				}
			}
		}
	}

	protected void resetSession() {
		try {
			this.reconnect();
		} catch (MessagingException e) {
			logger.error("Could not reset session.", e);
		}
	}
	
	protected String getTopicSelector() {
		if (topicSelector == null) {
			topicSelector = connection.getSelector();
		}
		return topicSelector;
	}

}
