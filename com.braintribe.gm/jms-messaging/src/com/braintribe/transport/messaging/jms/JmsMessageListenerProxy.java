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

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.jms.AcknowledgeMode;
import com.braintribe.transport.messaging.api.MessageListener;
import com.braintribe.transport.messaging.api.MessagingException;

public class JmsMessageListenerProxy implements javax.jms.MessageListener {

	private static final Logger logger = Logger.getLogger(JmsMessageListenerProxy.class);

	protected MessageListener messageListener = null;
	protected JmsMessageConsumer messageConsumer = null;

	public JmsMessageListenerProxy(MessageListener messageListener, JmsMessageConsumer messageConsumer) {
		this.messageListener = messageListener;
		this.messageConsumer = messageConsumer;
	}

	@Override
	public void onMessage(javax.jms.Message jmsMessage) {
		if (jmsMessage == null) {
			return;
		}
		try {
			JmsSession session = messageConsumer.getSession();
			Message message = MessageConverter.toMessage(session, jmsMessage);
			if (message != null) {
				this.messageListener.onMessage(message);
			}
		} catch (MessagingException e) {
			throw new RuntimeException("Could not forward message to message listener", e);
		}

		AcknowledgeMode acknowledgeMode = messageConsumer.getSession().getConnection().configuration.getAcknowledgeMode();
		if ((acknowledgeMode != null) && (acknowledgeMode.equals(AcknowledgeMode.AFTERPROCESSING))) {
			try {
				jmsMessage.acknowledge();
			} catch (Exception e) {
				logger.error("Could not acknowledge message "+jmsMessage, e);
			}
		}

	}

	public MessageListener getMessageListener() {
		return this.messageListener;
	}

}
