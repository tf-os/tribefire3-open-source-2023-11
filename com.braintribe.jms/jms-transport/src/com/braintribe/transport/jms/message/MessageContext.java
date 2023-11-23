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
package com.braintribe.transport.jms.message;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.queuecomm.IQueueCommunication;
import com.braintribe.transport.jms.queuecomm.IQueueContext;

public class MessageContext implements IMessageContext {

	protected static Logger logger = Logger.getLogger(MessageContext.class);
	
	protected IQueueContext queueContext = null;
	protected Message message = null;
	protected String sessionId = null;
	protected IQueueCommunication replyQueue = null;
	
	protected Long workerTTL = null;

	public MessageContext(IQueueContext queueContext, Message message) {
		this.queueContext = queueContext;
		this.message = message;
	}

	@Override
	public IQueueContext getQueueContext() {
		return queueContext;
	}

	@Override
	public Message getMessage() {
		return message;
	}

	@Override
	public void reply(String replyText) throws Exception {
		Message replyMessage = null;
		if (this.message instanceof TextMessage) {
			replyMessage = this.queueContext.getResponseSession().createTextMessage(replyText);
		} else if (this.message instanceof MapMessage) {
			MapMessage mm = this.queueContext.getResponseSession().createMapMessage();
			mm.setString("body", replyText);
			replyMessage = mm;
		}

		String corrId = this.message.getJMSCorrelationID();
		if (corrId != null) {
			replyMessage.setJMSCorrelationID(corrId);
		} else {
			replyMessage.setJMSCorrelationID(this.message.getJMSMessageID());
		}

		String copyProperties = this.message.getStringProperty("CSP_CopyProperties");
		if ((copyProperties != null) && (copyProperties.toLowerCase().equalsIgnoreCase("true"))) {
			Enumeration<String> es = this.message.getPropertyNames();
			while (es.hasMoreElements()) {
				String _name = es.nextElement();
				Object _value = this.message.getObjectProperty(_name);
				if (!_name.startsWith("JMS")) {
					logger.trace(String.format("setting message property '%s' = '%s'", _name, _value));
					replyMessage.setObjectProperty(_name, _value);
				}
			}
		}

		Destination replyDestination = null;
		IQueueContext qc = null;

		if (this.replyQueue != null) {
			qc = this.replyQueue.getQueueContext(true);
		} else {
			qc = this.queueContext;
			replyDestination = this.message.getJMSReplyTo();
		}

		qc.reply(replyMessage, replyDestination);
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		return this.message.toString();
	}

	@Override
	public IQueueCommunication getReplyQueue() {
		return replyQueue;
	}

	@Override
	public void setReplyQueue(IQueueCommunication replyQueue) {
		this.replyQueue = replyQueue;
	}

	@Override
	public MessageContext cloneWithNewMessage(Message newMessage) {
		MessageContext newContext = new MessageContext(this.queueContext, newMessage);
		newContext.sessionId = this.sessionId;
		newContext.replyQueue = this.replyQueue;
		return newContext;
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: MessageContext.java 92413 2016-03-15 08:30:06Z roman.kurmanowytsch $";
	}
}
