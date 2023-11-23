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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.jms.AcknowledgeMode;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.transport.messaging.api.MessageProperties;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;

public class JmsConnection implements MessagingConnection, ExceptionListener {

	private static final Logger logger = Logger.getLogger(JmsConnection.class);

	protected com.braintribe.model.messaging.jms.JmsConnection configuration = null;
	private Connection jmsConnection = null;
	protected boolean closed = false;
	protected JmsConnectionProvider connectionProvider = null;
	private JmsSelectorBuilder jmsSelectorBuilder;

	protected Set<JmsSession> sessions = new HashSet<JmsSession>();
	protected ReentrantLock sessionsLock = new ReentrantLock();

	private MessagingContext messagingContext;

	public JmsConnection(com.braintribe.model.messaging.jms.JmsConnection configuration, Connection jmsConnection,
			JmsConnectionProvider connectionProvider) {
		this.jmsConnection = jmsConnection;
		this.configuration = configuration;
		this.connectionProvider = connectionProvider;
		this.jmsSelectorBuilder = new JmsSelectorBuilder();
		this.messagingContext = connectionProvider.getContext();

		try {
			this.jmsConnection.setExceptionListener(this);
		} catch (Exception e) {
			logger.debug("Could not register the connection as an exception listener.", e);
		}
	}

	@Override
	public void open() throws MessagingException {
		if (this.closed) {
			throw new MessagingException("By contract, it is not possible to open a closed connection.");
		}

		try {
			this.jmsConnection.start();
		} catch (Exception e) {
			throw new MessagingException("Could not open/start connection.", e);
		}
	}

	@Override
	public void close() throws MessagingException {
		if (this.closed) {
			return;
		}
		try {
			this.jmsConnection.close();
		} catch (Exception e) {
			throw new MessagingException("Could not close the Connection", e);
		} finally {
			this.closed = true;
			this.jmsConnection = null;
		}
	}

	@Override
	public MessagingSession createMessagingSession() throws MessagingException {
		if (this.closed) {
			throw new MessagingException("The connection is closed.");
		}

		Session jmsSession = this.createJmsSession();
		JmsSession session = new JmsSession(jmsSession, this);

		sessionsLock.lock();
		try {
			sessions.add(session);
		} finally {
			sessionsLock.unlock();
		}

		return session;

	}

	protected void sessionClosed(JmsSession session) {
		sessionsLock.lock();
		try {
			sessions.remove(session);
		} finally {
			sessionsLock.unlock();
		}
	}

	protected Session createJmsSession() throws MessagingException {
		int errorCount = 0;

		while (true) {

			try {
				this.open();
				int jmsAcknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
				AcknowledgeMode acknowledgeMode = this.configuration.getAcknowledgeMode();
				if (acknowledgeMode != null) {
					if (acknowledgeMode.equals(AcknowledgeMode.AUTO)) {
						jmsAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;
					}
				}
				Session jmsSession = this.jmsConnection.createSession(this.configuration.getTransacted(), jmsAcknowledgeMode);

				if (errorCount <= 1) {
					TemporaryQueue tq = jmsSession.createTemporaryQueue();
					tq.delete();
				}

				return jmsSession;
			} catch (Exception e) {
				errorCount++;

				if (errorCount <= 2) {
					JmsMessagingUtils.logError(logger, e, "Could not create session from connection " + this.jmsConnection + ". Retrying...");
					this.reconnect();
				} else {
					throw new MessagingException("Could not create session from connection " + this.jmsConnection, e);
				}
			}
		}
	}

	protected void reconnect() throws MessagingException {
		if (this.jmsConnection != null) {
			try {
				this.jmsConnection.close();
			} catch (Exception e) {
				logger.debug("Error while closing JMS connection before reconnecting.", e);
			}
		}
		this.jmsConnection = this.connectionProvider.createJmsConnection();
		if (this.isConnectionValid()) {

			sessionsLock.lock();
			try {
				for (JmsSession session : sessions) {
					session.resetSession();
				}
			} finally {
				sessionsLock.unlock();
			}
		}
		try {
			this.jmsConnection.setExceptionListener(this);
		} catch (Exception e) {
			logger.debug("Could not register the connection as an exception listener.", e);
		}
		this.closed = false;
		this.open();
	}

	public MessagingContext getMessagingContext() {
		return messagingContext;
	}

	@Override
	public void onException(JMSException jmsException) {

		JmsMessagingUtils.logError(logger, jmsException, "The JMS provider reported an error. Checking connection trying to reconnect if necessary.");

		if (this.closed) {
			logger.debug("We got an exception on a closed connection. Nothing to do.");
			return;
		}
		if (TribefireRuntime.isShuttingDown()) {
			logger.info(
					() -> "We got an exception but the instance is shutting down at the moment. Nothing to do. Details about the exception will be logged on DEBUG level.");
			logger.debug(() -> "Error in JMS connection.", jmsException);
			close();
			return;
		}

		// Trying to create a new session; try until it works

		long tryInterval = 5000L;
		long tryIntervalInc = 5000L;
		long tryIntervalMax = 300000L;

		boolean connectionReset = false;

		while ((!this.closed) && (!isConnectionValid())) {

			connectionReset = true;

			try {
				synchronized (this) {
					wait(tryInterval);
				}
			} catch (InterruptedException ie) {
				logger.info("onException got interrupted");
				return;
			}

			try {
				this.reconnect();
			} catch (MessagingException e) {
				logger.error("Could not reconnect", e);
			}

			tryInterval += tryIntervalInc;
			if (tryInterval > tryIntervalMax) {
				tryInterval = tryIntervalMax;
			}
		}

		if (!this.closed && connectionReset) {
			logger.info("Connection to JMS server has is alive again.");
		}
	}

	protected boolean isConnectionValid() {
		Session jmsSession = null;
		TemporaryQueue tq = null;
		try {
			this.open();
			jmsSession = this.jmsConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			tq = jmsSession.createTemporaryQueue();
			tq.delete();
			return true;
		} catch (Exception e) {
			logger.info("Could not create a new session.", e);
			return false;
		} finally {
			if (tq != null) {
				try {
					tq.delete();
				} catch (Exception e) {
					logger.debug("Could not close temporary queue " + tq, e);
				}
			}
			if (jmsSession != null) {
				try {
					jmsSession.close();
				} catch (Exception e) {
					logger.debug("Could not close temporary session " + jmsSession, e);
				}
			}
		}

	}

	protected String getSelector() {
		return this.jmsSelectorBuilder.build(connectionProvider.context);
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

}
