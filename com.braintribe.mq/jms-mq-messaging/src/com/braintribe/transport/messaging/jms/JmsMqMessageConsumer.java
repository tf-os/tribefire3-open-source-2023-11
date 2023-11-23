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

import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.jms.AcknowledgeMode;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;

public class JmsMqMessageConsumer extends MqMessageHandler implements MessageConsumer {

	private static final Logger logger = Logger.getLogger(JmsMqMessageConsumer.class);
	
	private Destination destination;

	private javax.jms.MessageConsumer jmsMessageConsumer = null;
	
	private String topicSelector;
	private JmsSelectorBuilder jmsSelectorBuilder = new JmsSelectorBuilder();

	protected JmsMqMessageListenerProxy messageListenerProxy = null;
	
	protected static AtomicInteger receiveCount = new AtomicInteger(0);

	public JmsMqMessageConsumer(JmsMqSession jmsMqSession, Destination destination) {
		super(jmsMqSession);
		this.destination = destination;
	}

	@Override
	public MessageListener getMessageListener() throws MessagingException {
		if (this.messageListenerProxy == null) {
			return null;
		}
		return this.messageListenerProxy.getMessageListener();
	}

	@Override
	public void setMessageListener(MessageListener messageListener) throws MessagingException {
		if (messageListener == null) {
			return;
		}
		ensureJmsMessageConsumer();
		this.messageListenerProxy = new JmsMqMessageListenerProxy(messageListener, this);
		try {
			this.jmsMessageConsumer.setMessageListener(this.messageListenerProxy);
		} catch (Exception e) {
			throw new MessagingException("Could not register message listener "+messageListener, e);
		}
	}

	@Override
	public Message receive() throws MessagingException {
		return this.receive(0);
	}

	@Override
	public Message receive(long timeout) throws MessagingException {
		int errorCount = 0;

		ensureJmsMessageConsumer();
		
		boolean effectiveTimeout = timeout != 0;
		javax.jms.Message jmsMessage = null;
		while(true) {
			
			long start = effectiveTimeout ? System.currentTimeMillis() : 0;

			try {
				jmsMessage = this.jmsMessageConsumer.receive(timeout);
				if (logger.isTraceEnabled()) logger.trace("Received message "+jmsMessage);

				AcknowledgeMode acknowledgeMode = session.getConnection().getConfiguration().getAcknowledgeMode();
				if (jmsMessage != null && acknowledgeMode != null) {
					switch(acknowledgeMode) {
						case AFTERPROCESSING:
						case AUTO:
						case ONRECEIVE:
							jmsMessage.acknowledge();
							break;
						default:
							break;
						
					}
				}

			} catch (Exception e) {
				if (e instanceof JMSException) {
					JMSException jmse = (JMSException) e;
					Throwable cause = jmse.getCause();
					if (cause instanceof InterruptedException) {
						logger.debug("Message receive interrupted.");
						return null;
					}
				}
				String destName = null;
				if (destination != null) {
					destName = destination.getName();
				}

				errorCount++;

				if (errorCount == 1) {
					JmsMqMessagingUtils.logError(logger, e, "Error while trying to receive a JMS message from destination "+destName+". Retrying...");
					session.getConnection().reconnect();
					continue;
				} else {
					throw new MessagingException("Error while trying to receive a JMS message from destination "+destName, e);
				}

			}

			if (jmsMessage == null) {
				return null;
			}

			logger.trace(() -> "Received message no. "+receiveCount.incrementAndGet());
			
			Message message = MqMessageConverter.toMessage(session, jmsMessage);
			
			if (message != null) {
				return message;
			}
			
			if (effectiveTimeout) {
				timeout = timeout - (System.currentTimeMillis()-start);
				if (timeout < 1) {
					return null;
				}
			}

		}
	}

	protected void ensureJmsMessageConsumer() throws MessagingException {
		if (jmsMessageConsumer != null) {
			return;
		}
		super.ensureJmsSession();

		int errorCount = 0;

		while(true) {

			String destinationName = null;
			try {
				destinationName = destination.getName();

				javax.jms.Destination jmsDestination = super.createJmsDestination(destination);
				if (jmsDestination == null) {
					throw new MessagingException("The destination "+destinationName+" is not known.");
				}
				if (jmsDestination instanceof javax.jms.Topic) {
					jmsMessageConsumer = this.jmsSession.createConsumer(jmsDestination, getTopicSelector());
				} else {
					jmsMessageConsumer = this.jmsSession.createConsumer(jmsDestination);
				}
				
				return;

			} catch(Exception e) {

				errorCount++;

				if (errorCount == 1) {
					JmsMqMessagingUtils.logError(logger, e, "Could not create a new JMS MessageConsumer for the destination "+destinationName+". Retrying...");
					this.session.getConnection().reconnect();
				} else {
					throw new MessagingException("Could not create a new JMS MessageConsumer for the destination "+destinationName, e);
				}
			}
		}
	}
	
	protected String getTopicSelector() {
		if (topicSelector == null) {
			topicSelector = this.jmsSelectorBuilder.build(session.getConnection().getConnectionProvider().getContext());
		}
		return topicSelector;
	}
	
	@Override
	public void close() throws MessagingException {
		if (this.jmsMessageConsumer != null) {
			try {
				this.jmsMessageConsumer.close();
			} catch (Exception e) {
				throw new MessagingException("Could not close JMS Message Consumer", e);
			} finally {
				this.jmsMessageConsumer = null;		
				this.messageListenerProxy = null;
			}
		}
		super.close();
	}

	@Override
	public void reset() {
		close();
	}
	
	static class JmsSelectorBuilder {

		static final String conjClause = "(%1$s AND %2$s)";
		static final String idNullClause = "%1$s IS NULL";
		static final String idClause = "((" + idNullClause + ") OR (%1$s = '%2$s'))";
		static final String appIdKey = MessageConverter.propertyPrefix + MessageProperties.addreseeAppId.getName();
		static final String nodeIdKey = MessageConverter.propertyPrefix + MessageProperties.addreseeNodeId.getName();

		private String cachedSelector;

		public String build(MessagingContext context) {
			if (cachedSelector != null) {
				return cachedSelector;
			}
			return build(context.getApplicationId(), context.getNodeId());
		}

		private String build(String appId, String nodeId) {

			String appIdClause;
			String nodeIdClause;

			if (appId == null) {
				appIdClause = String.format(idNullClause, appIdKey);
			} else {
				appIdClause = String.format(idClause, appIdKey, appId);
			}
			if (nodeId == null) {
				nodeIdClause = String.format(idNullClause, nodeIdKey);
			} else {
				nodeIdClause = String.format(idClause, nodeIdKey, nodeId);
			}

			cachedSelector = String.format(conjClause, appIdClause, nodeIdClause);

			return cachedSelector;

		}

	}
	public javax.jms.MessageConsumer getJmsMessageConsumer() {
		ensureJmsMessageConsumer();
		return jmsMessageConsumer;
	}

}
