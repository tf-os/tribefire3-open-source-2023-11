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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.ibm.mq.jms.MQMessageProducer;

public class JmsMqMessageProducer extends MqMessageHandler implements MessageProducer {

	private static final Logger logger = Logger.getLogger(JmsMqMessageProducer.class);

	private Destination destination;

	public static final Long defaultTimeToLive = Long.valueOf(0L);
	public static final Integer defaultPriority = Integer.valueOf(4);

	protected Long timeToLive = defaultTimeToLive;
	protected Integer priority = defaultPriority;

	protected Map<String,MQMessageProducer> jmsMessageProducers = new HashMap<>();
	protected ReentrantLock jmsMessageProducersLock = new ReentrantLock();

	protected static AtomicInteger sentCount = new AtomicInteger(0);
	
	public JmsMqMessageProducer(JmsMqSession session) {
		super(session);
	}

	public JmsMqMessageProducer(JmsMqSession jmsMqSession, Destination destination) {
		super(jmsMqSession);
		this.destination = destination;
	}

	@Override
	public void close() {
		jmsMessageProducersLock.lock();
		try {
			for (javax.jms.MessageProducer producer : jmsMessageProducers.values()) {
				try {
					producer.close();
				} catch (Exception e) {
					logger.error("Error while trying to close producer", e);
				}
			}
			jmsMessageProducers.clear();
		} finally {
			jmsMessageProducersLock.unlock();
		}
		super.close();
	}

	@Override
	public void sendMessage(Message message) throws MessagingException {

		if (destination == null) {
			throw new UnsupportedOperationException("This method is not supported as no destination was assigned to the message producer at creation time");
		}

		this.sendMessage(message, destination);
	}

	@Override
	public void sendMessage(Message message, Destination destination) throws MessagingException {
		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null");
		}

		if (destination == null) {
			throw new IllegalArgumentException("Destination cannot be null");
		}

		super.ensureJmsSession();

		int errorCount = 0;
		com.braintribe.transport.messaging.jms.JmsMqConnection connection = this.session.getConnection();

		while(true) {

			try {

				message.setDestination(destination);

				message.setMessageId(UUID.randomUUID().toString());

				if (message.getPriority() == null) {
					message.setPriority(this.priority);
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

				javax.jms.MessageProducer producer = ensureJmsMessageProducer(destination);

				javax.jms.Message jmsMessage = MqMessageConverter.toJmsMessage(session, jmsSession, this, message);

				producer.send(jmsMessage, 
						jmsMessage.getJMSDeliveryMode(), 
						message.getPriority(),
						message.getTimeToLive());

				logger.trace(() -> "Sent message no. "+sentCount.incrementAndGet());
				
				return;

			} catch (Exception e) {

				errorCount++;

				if (errorCount == 1) {
					JmsMqMessagingUtils.logError(logger, e, "Failed to publish message: "+e.getMessage()+". Retrying...");
					connection.reconnect();
				} else {
					throw new MessagingException("Failed to publish message: "+e.getMessage(), e);
				}

			}

		}
	}

	protected MQMessageProducer ensureJmsMessageProducer(Destination destination) throws MessagingException {
		super.ensureJmsSession();

		int errorCount = 0;
		com.braintribe.transport.messaging.jms.JmsMqConnection connection = this.session.getConnection();

		while (true) {

			String destinationName = null;
			try {
				javax.jms.Destination jmsDestination = null;

				if (destination != null) {

					destinationName = destination.getName();

					jmsMessageProducersLock.lock();
					try {
						MQMessageProducer messageProducer = jmsMessageProducers.get(destinationName);
						if (messageProducer != null) {
							return messageProducer;
						}
					} finally { 
						jmsMessageProducersLock.unlock();
					}


					jmsDestination = super.createJmsDestination(destination);

					if (jmsDestination == null) {
						throw new MessagingException("The destination " + destinationName + " is not known.");
					}
				}

				MQMessageProducer jmsMessageProducer = (MQMessageProducer) this.jmsSession.createProducer(jmsDestination);

				jmsMessageProducersLock.lock();
				try {
					jmsMessageProducers.put(destinationName, jmsMessageProducer);
				} finally { 
					jmsMessageProducersLock.unlock();
				}

				return jmsMessageProducer;

			} catch (Exception e) {

				String errorCompl = (destinationName == null) ? "without specified destination" : "for the destination " + destinationName;

				errorCount++;

				if (errorCount == 1) {
					JmsMqMessagingUtils.logError(logger, e, "Could not create a new JMS MessageProducer  " + errorCompl + ". Retrying...");
					connection.reconnect();
				} else {
					throw new MessagingException("Could not create a new JMS MessageProducer " + errorCompl, e);
				}

			}
		}
	}


	/**
	 * Ensure that the given not-null priority fits the 0-9 range.
	 * @param _priority The priority that should be normalized
	 * @return The normalized priority
	 */
	protected Integer normalizePriority(Integer _priority) {
		if (_priority == null) {
			return null;
		} else if (_priority < 0) {
			return 0;
		} else if (_priority > 9) {
			return 9;
		} else {
			return _priority;
		}
	}

	@Override
	public Destination getDestination() {
		return destination;
	}

	@Override
	public Long getTimeToLive() {
		return this.timeToLive;
	}

	@Override
	public void setTimeToLive(Long timeToLive) {
		this.timeToLive = timeToLive;
	}

	@Override
	public Integer getPriority() {
		return this.priority;
	}

	@Override
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public void reset() {
		this.close();
	}


}
