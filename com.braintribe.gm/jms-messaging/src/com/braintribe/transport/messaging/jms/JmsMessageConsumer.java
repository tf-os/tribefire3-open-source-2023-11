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

import javax.jms.JMSException;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.jms.AcknowledgeMode;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessagingException;

public class JmsMessageConsumer extends JmsMessageHandler implements MessageConsumer {

	private static final Logger logger = Logger.getLogger(JmsMessageConsumer.class);

	protected javax.jms.MessageConsumer jmsMessageConsumer = null;
	protected JmsSession session = null;

	protected JmsMessageListenerProxy messageListenerProxy = null;

	public JmsMessageConsumer(javax.jms.MessageConsumer jmsMessageConsumer, JmsSession session) {
		this.jmsMessageConsumer = jmsMessageConsumer;
		this.session = session;
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
		this.messageListenerProxy = new JmsMessageListenerProxy(messageListener, this);
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
		
		boolean effectiveTimeout = timeout != 0;

		javax.jms.Message jmsMessage = null;
		while(true) {
			
			long start = effectiveTimeout ? System.currentTimeMillis() : 0;

			try {
				jmsMessage = this.jmsMessageConsumer.receive(timeout);
				if (logger.isTraceEnabled()) logger.trace("Received message "+jmsMessage);

				AcknowledgeMode acknowledgeMode = this.session.getConnection().configuration.getAcknowledgeMode();
				if (jmsMessage != null && (acknowledgeMode != null) && (acknowledgeMode.equals(AcknowledgeMode.ONRECEIVE))) {
					jmsMessage.acknowledge();
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
				Destination dest = this.getDestination();
				String destName = null;
				if (dest != null) {
					destName = dest.getName();
				}

				errorCount++;

				if (errorCount == 1) {
					JmsMessagingUtils.logError(logger, e, "Error while trying to receive a JMS message from destination "+destName+". Retrying...");
					this.reconnect();
					continue;
				} else {
					throw new MessagingException("Error while trying to receive a JMS message from destination "+destName, e);
				}

			}

			if (jmsMessage == null) {
				return null;
			}

			Message message = MessageConverter.toMessage(session, jmsMessage);
			
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

	protected void reconnect() throws MessagingException {
		
		javax.jms.MessageConsumer oldConsumer = this.jmsMessageConsumer;

		try {
			
			this.session.resetSessionIfNecessary();
			this.jmsMessageConsumer = this.session.createJmsMessageConsumer(getDestination());
			
		} finally {
			if (oldConsumer != null) {
				try {
					oldConsumer.close();
				} catch(Exception e) {
					logger.debug("Could not close old message consumer.", e);
				}
			}
		}
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
				
				this.session.messageConsumerClosed(this);
			}
		}
	}

	public javax.jms.MessageConsumer getJmsMessageConsumer() {
		return this.jmsMessageConsumer;
	}

	protected void resetMessageListener() throws MessagingException {
		this.reconnect();
		if (messageListenerProxy != null) {
			setMessageListener(messageListenerProxy.getMessageListener());
		}
	}
}
