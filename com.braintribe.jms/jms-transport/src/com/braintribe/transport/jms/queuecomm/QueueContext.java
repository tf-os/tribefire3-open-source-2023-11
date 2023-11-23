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
package com.braintribe.transport.jms.queuecomm;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.braintribe.logging.Logger;
import com.braintribe.transport.jms.IServer;
import com.braintribe.transport.jms.queuecomm.IQueueCommunication.QueueOperation;
import com.braintribe.transport.jms.util.JmsUtil;

public class QueueContext implements IQueueContext{

	protected static Logger logger = Logger.getLogger(QueueContext.class);
	
	protected Connection connection = null;
	protected Session session = null;
	protected Session responseSession = null;

	protected String username = null;
	protected String password = null;

	protected boolean connected = false;

	protected IServer server = null;
	protected Destination destination = null;
	protected Destination receiveReplyDestination = null;

	protected String queueName = null;

	protected boolean useSeperateSessionForResponses = false;

	protected MessageConsumer receiver = null;

	protected QueueOperation queueOperation = QueueOperation.READ;

	protected String messageSelector = null;

	protected boolean transactionalSession = false;
	protected int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

	public QueueContext() {
	}
	
	public QueueContext(IServer server, Destination dest, String queueName, boolean transactionalSession,
			int acknowledgeMode) {
		this.server = server;
		this.destination = dest;
		this.queueName = queueName;
		this.transactionalSession = transactionalSession;
		this.acknowledgeMode = acknowledgeMode;
	}

	@Override
	public Message receiveMessage(long timeout) throws Exception {
		this.connect();
		Message message = null;

		long start = System.currentTimeMillis();

		while (message == null) {
			
			boolean retry = false;
			try {
				if (timeout > 0) {
					message = this.receiver.receive(timeout);
				} else {
					message = this.receiver.receive();
				}
			} catch (Throwable e) {
				if (e instanceof JMSException) {
					JMSException jmse = (JMSException) e;
					Exception linkede = jmse.getLinkedException();
					if ((linkede != null) && (linkede instanceof InterruptedException)) {
						//interrupted
						throw linkede;
					}
				}

				logger.error("Error while trying to receive a message. Retrying...", e);
				if (e instanceof JMSException) {
					logger.error("JMSException: ", ((JMSException) e).getLinkedException());
				}
				this.reconnect();
				retry = true;
			}

			if ((!retry) || (timeout > 0)) {
				long now = System.currentTimeMillis();
				if ((now - start) > timeout) {
					break;
				}
			}
		}
		return message;
	}

	@Override
	public void connect() throws Exception {
		if (this.connected == true) {
			return;
		}
		try {
			this.connection = this.server.createConnection();
		} catch(Exception e) {
			throw new Exception("Could not create a connection to server "+this.server+" for queue "+this.queueName, e);
		}
		try {
			this.session = this.connection.createSession(this.transactionalSession, this.acknowledgeMode);
		} catch(Exception e) {
			throw new Exception("Could not create a session with server "+this.server+" for queue "+this.queueName, e);
		}
		if (this.useSeperateSessionForResponses) {
			try {
				this.responseSession = this.connection.createSession(this.transactionalSession, this.acknowledgeMode);
			} catch(Exception e) {
				throw new Exception("Could not create a response session to server "+this.server+" for queue "+this.queueName, e);
			}
		} else {
			this.responseSession = session;
		}
		try {
			this.connection.start();
		} catch(Exception e) {
			throw new Exception("Could not start a connection to server "+this.server+" for queue "+this.queueName, e);
		}

		if ((this.destination == null) && ((this.queueName != null) && (this.queueName.trim().length() > 0))) {
			logger.trace("Destination is not set. Creating new one with " + this.queueName);
			try {
				this.destination = session.createQueue(this.queueName);
			} catch(Exception e) {
				throw new Exception("Could not access the destination at server "+this.server+" for queue "+this.queueName, e);
			}
		}

		if (this.queueOperation != QueueOperation.WRITE) {
			try {
				if (this.messageSelector != null) {
					this.receiver = this.session.createConsumer(this.destination, this.messageSelector);
				} else {
					this.receiver = this.session.createConsumer(this.destination);
				}
			} catch(Exception e) {
				throw new Exception("Could not create a message consumer at server "+this.server+" for queue "+this.queueName, e);
			}
		}
		this.connected = true;
	}

	@Override
	public void disconnect() {
		try {
			if (this.receiver != null) {
				this.receiver.close();
			}
			if (this.session != null) {
				this.session.close();
			}
			if (this.responseSession != null) {
				this.responseSession.close();
			}
			if (this.connection != null) {
				this.connection.close();
			}
		} catch (Exception e) {
			logger.error("Could not close JMS Server sessions and connection.", e);
		}
		this.receiver = null;
		this.session = null;
		this.responseSession = null;
		this.connection = null;
		this.connected = false;
	}

	@Override
	public String getQueueName() {
		return this.queueName;
	}

	@Override
	public Session getResponseSession() {
		return this.responseSession;
	}

	@Override
	public void reconnect() throws Exception {
		try {
			this.disconnect();
		} catch (Exception e2) {
			logger.error("Error while disconnecting (reconnect action), ", e2);
			if (e2 instanceof JMSException) {
				logger.error("JMSException: ", ((JMSException) e2).getLinkedException());
			}
		}
		try {
			this.connect();
		} catch (Exception e2) {
			logger.error("Error while connecting (reconnect action), ", e2);
			if (e2 instanceof JMSException) {
				logger.error("JMSException: ", ((JMSException) e2).getLinkedException());
			}
			logger.info("Waiting some time before retrying...");
			try {
				synchronized (this) {
					wait(1000);
				}
			} catch (Exception ignore) {
				//Ignore
			}
		}
	}

	@Override
	public void reply(Message replyMsg, Destination replyDestination) throws Exception {
		this.connect();
		MessageProducer prod = null;

		if (replyDestination == null) {
			replyDestination = this.destination;
		}
		try {
			prod = this.responseSession.createProducer(replyDestination);
			prod.send(replyMsg);
		} catch (Exception e) {
			JmsUtil.logError(logger, e, "Error while sending response " + replyMsg + ". Retrying...");
			this.reconnect();
			prod = this.responseSession.createProducer(replyDestination);
			prod.send(replyMsg);
		} finally {
			try {
				prod.close();
			} catch (Exception e) {
				JmsUtil.logError(logger, e, "Could not close message producer.");
			}
		}
	}

	@Override
	public void send(Message msg, int deliveryMode, int priority, long timeToLive) throws Exception {
		this.connect();
		MessageProducer prod = null;
		try {
			prod = this.session.createProducer(this.destination);

			if (logger.isTraceEnabled()) {
				logger.trace("Sending message to queue " + this.queueName + ": " + msg + " (destination="
						+ this.destination + ")");
			}

			if ((deliveryMode == -1) || (priority == -1) || (timeToLive == -1)) {
				prod.send(msg);
			} else {
				prod.send(msg, deliveryMode, priority, timeToLive);
			}
		} catch (Exception e) {
			JmsUtil.logError(logger, e, "Error while sending message " + msg + ". Retrying...");
			this.reconnect();
			prod = this.session.createProducer(this.destination);
			if ((deliveryMode == -1) || (priority == -1) || (timeToLive == -1)) {
				prod.send(msg);
			} else {
				prod.send(msg, deliveryMode, priority, timeToLive);
			}
		} finally {
			try {
				prod.close();
			} catch (Exception e) {
				JmsUtil.logError(logger, e, "Could not close message producer.");
			}
		}
	}

	@Override
	public void send(Message msg) throws Exception {
		this.send(msg, -1, -1, -1);
	}

	@Override
	public void commit(boolean hideErrors) throws Exception {
		if ((this.transactionalSession) && (this.session != null)) {
			try {
				this.session.commit();

				if ((this.useSeperateSessionForResponses) && (this.responseSession != null)) {
					this.responseSession.commit();
				}

			} catch (Exception e) {
				JmsUtil.logError(logger, e, "Error while commiting session.");
				if (!hideErrors) {
					throw new Exception("Could not commit session.", e);
				}
			}
		}
	}

	@Override
	public void rollback(boolean hideErrors) throws Exception {
		if ((this.transactionalSession) && (this.session != null)) {
			try {
				this.session.rollback();

				if ((this.useSeperateSessionForResponses) && (this.responseSession != null)) {
					this.responseSession.rollback();
				}

			} catch (Exception e) {
				JmsUtil.logError(logger, e, "Error while performing rollback in session.");
				if (!hideErrors) {
					throw new Exception("Could not rollback session.", e);
				}
			}
		}
	}

	@Override
	public MapMessage createMapMessage() throws Exception {
		if (this.session == null) {
			throw new Exception("Cannot create a MapMessage object. The session is null.");
		}
		return this.session.createMapMessage();
	}

	@Override
	public ObjectMessage createObjectMessage() throws Exception {
		if (this.session == null) {
			throw new Exception("Cannot create a ObjectMessage object. The session is null.");
		}
		return this.session.createObjectMessage();
	}

	@Override
	public TextMessage createTextMessage() throws Exception {
		if (this.session == null) {
			throw new Exception("Cannot create a TextMessage object. The session is null.");
		}
		return this.session.createTextMessage();
	}

	@Override
	public BytesMessage createBytesMessage() throws Exception {
		if (this.session == null) {
			throw new Exception("Cannot create a BytesMessage object. The session is null.");
		}
		return this.session.createBytesMessage();
	}

	@Override
	public QueueOperation getQueueOperation() {
		return queueOperation;
	}

	@Override
	public void setQueueOperation(QueueOperation queueOperation) {
		this.queueOperation = queueOperation;
	}

	@Override
	public String getMessageSelector() {
		return messageSelector;
	}

	@Override
	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}

	@Override
	public boolean isTransactionalSession() {
		return transactionalSession;
	}

	@Override
	public Destination getDestination() {
		return destination;
	}

	@Override
	public Destination createTemporaryResponseQueue() throws Exception {
		if (this.session == null) {
			throw new Exception("Cannot create a temporary queue. The session is null.");
		}
		this.receiveReplyDestination = this.session.createTemporaryQueue();
		if (this.messageSelector != null) {
			this.receiver = this.session.createConsumer(this.receiveReplyDestination, this.messageSelector);
		} else {
			this.receiver = this.session.createConsumer(this.receiveReplyDestination);
		}
		return this.receiveReplyDestination;
	}

	@Override
	public Destination getDestination(String destinationName) throws Exception {
		return this.session.createQueue(destinationName);
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: QueueContext.java 92413 2016-03-15 08:30:06Z roman.kurmanowytsch $";
	}
	
	
/*
 * Getters and Setters
 */
	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public void setServer(IServer arg) {
		this.server = arg;
	}

	@Override
	public int getAcknowledgeMode() {
		return acknowledgeMode;
	}

	@Override
	public void setAcknowledgeMode(int arg) {
		this.acknowledgeMode = arg;
	}

	@Override
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("QueueContext: ");
		sb.append("Server: ("+this.server+")");
		sb.append(", Queue Name: "+this.queueName);
		sb.append(", Message Selector: "+this.messageSelector);
		sb.append(", Transactional Session: "+this.transactionalSession);
		sb.append(", Ack Mode: "+this.acknowledgeMode);
		return sb.toString();
	}
}
