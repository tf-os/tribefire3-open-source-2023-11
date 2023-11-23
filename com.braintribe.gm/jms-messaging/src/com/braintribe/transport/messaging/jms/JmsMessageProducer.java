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

import java.util.UUID;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Topic;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.utils.lcd.StringTools;

public class JmsMessageProducer extends JmsMessageHandler implements MessageProducer {

	private static final Logger logger = Logger.getLogger(JmsMessageProducer.class);

	public static Long defaultTopicTimeToLive = Long.valueOf(Numbers.MILLISECONDS_PER_HOUR);
	public static Long defaultTimeToLive = Long.valueOf(0L);
	public static Integer defaultPriority = Integer.valueOf(4);

	static {
		String topicTtl = null;
		try {
			topicTtl = TribefireRuntime.getProperty("JMS_DEFAULT_TOPIC_TTL");
			if (!StringTools.isBlank(topicTtl)) {
				defaultTopicTimeToLive = Long.parseLong(topicTtl);
			}
		} catch(Exception e) {
			logger.error("Cannot parse "+topicTtl, e);
		}
	}

	protected Long timeToLive = defaultTimeToLive;
	protected Integer priority = defaultPriority;

	protected javax.jms.MessageProducer jmsMessageProducer = null;
	protected JmsSession session = null;


	public JmsMessageProducer(javax.jms.MessageProducer jmsMessageProducer, JmsSession session) {
		this.jmsMessageProducer = jmsMessageProducer;
		this.session = session;
	}

	@Override
	public void sendMessage(Message message) throws MessagingException {

		if (getDestination() == null) {
			throw new UnsupportedOperationException("This method is not supported as no destination was assigned to the message producer at creation time");
		}

		this.sendMessage(message, super.getDestination());
	}

	@Override
	public void sendMessage(Message message, Destination destination) throws MessagingException {

		if (message == null) {
			throw new IllegalArgumentException("Message cannot be null");
		}

		if (destination == null) {
			throw new IllegalArgumentException("Destination cannot be null");
		}

		int errorCount = 0;

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

				if (message.getTimeToLive() == 0L) {
					if (destination.entityType().isAssignableFrom(Topic.T)) {
						message.setTimeToLive(defaultTopicTimeToLive);
					}
				}
				
				if (message.getTimeToLive() > 0L) {
					message.setExpiration(System.currentTimeMillis()+message.getTimeToLive());
				} else {
					message.setExpiration(0L);
				}


				javax.jms.MessageProducer producer = this.jmsMessageProducer;
				javax.jms.Destination jmsDestination = getJmsDestination();
				if (destination != getDestination()) {
					jmsDestination = this.session.createJmsDestination(destination);
					JmsMessageProducer newProducer = (JmsMessageProducer) this.session.createMessageProducer(destination);
					producer = newProducer.jmsMessageProducer;
				}

				javax.jms.Message jmsMessage = MessageConverter.toJmsMessage(session, message);

				producer.send(jmsDestination, jmsMessage, 
						jmsMessage.getJMSDeliveryMode(), 
						message.getPriority(),
						message.getTimeToLive());
				
				return;

			} catch (Exception e) {
				
				errorCount++;

				if (errorCount == 1) {
					JmsMessagingUtils.logError(logger, e, "Failed to publish message: "+e.getMessage()+". Retrying...");
					this.reconnect();
				} else {
					throw new MessagingException("Failed to publish message: "+e.getMessage(), e);
				}
				
			}

		}
	}

	protected void reconnect() throws MessagingException {

		javax.jms.MessageProducer oldProducer = this.jmsMessageProducer;

		try {
			
			this.session.resetSessionIfNecessary();
			this.jmsMessageProducer = this.session.createJmsMessageProducer(super.getDestination());
			
		} finally {
			if (oldProducer != null) {
				try {
					oldProducer.close();
				} catch(Exception e) {
					logger.debug("Could not close old message producer after reconnecting.", e);
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
	public void close() throws MessagingException {
		if (this.jmsMessageProducer != null) {
			try {
				this.jmsMessageProducer.close();
			} catch(Exception e) {
				throw new MessagingException("Failed to close underlying JMS message producer", e);
			}
		}
	}

}
