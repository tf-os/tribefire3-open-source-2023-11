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
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.ibm.mq.jms.MQConnection;
import com.ibm.mq.jms.MQConnectionFactory;

public class JmsMqConnection implements MessagingConnection {

	private static final Logger logger = Logger.getLogger(JmsMqConnection.class);

	private com.braintribe.model.messaging.jms.JmsMqConnection configuration;
	private JmsMqConnectionProvider connectionProvider;
	private MQConnectionFactory connectionFactory;
	private MQConnection jmsConnection;
	private boolean closed = false;
	private Marshaller messageMarshaller = null;

	protected Set<JmsMqSession> sessions = new HashSet<>();
	protected ReentrantLock sessionsLock = new ReentrantLock();

	public JmsMqConnection(com.braintribe.model.messaging.jms.JmsMqConnection configuration, JmsMqConnectionProvider connectionProvider,
			MQConnectionFactory connectionFactory) {
		this.configuration = configuration;
		this.connectionProvider = connectionProvider;
		this.connectionFactory = connectionFactory;
	}

	@Override
	public void open() throws MessagingException {
		if (jmsConnection != null) {
			if (closed) {
				throw new MessagingException("By contract, it is not possible to open a closed connection.");
			}
			return;
		}
		createJmsConnection();

		if (this.messageMarshaller == null) {
			this.messageMarshaller = new Bin2Marshaller();
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
		if (jmsConnection != null) {
			try {
				this.jmsConnection.close();
			} catch (Exception e) {
				throw new MessagingException("Could not close the Connection", e);
			} finally {
				this.closed = true;
				this.jmsConnection = null;
			}
		}
	}

	@Override
	public MessagingSession createMessagingSession() throws MessagingException {
		if (this.closed) {
			throw new MessagingException("The connection is closed.");
		}

		JmsMqSession session = new JmsMqSession(this);

		sessionsLock.lock();
		try {
			sessions.add(session);
		} finally {
			sessionsLock.unlock();
		}

		return session;
	}

	protected void createJmsConnection() throws MessagingException {
		if (jmsConnection != null) {
			return;
		}
		try {

			MQConnection con = null;

			String username = this.configuration.getUsername();
			String password = this.configuration.getPassword();

			if ((username != null) && (password != null)) {
				logger.debug(() -> String.format("Creating queue connection for user '%s'", username));
				con = (MQConnection) this.connectionFactory.createConnection(username, password);
			} else {
				logger.debug(() -> "Creating anonymous queue connection");
				con = (MQConnection) this.connectionFactory.createConnection();
			}

			con.setExceptionListener(new JmsExceptionListener(logger));

			logger.trace(() -> "Successfully created queue connection");
			jmsConnection = con;

		} catch (Exception e) {
			throw new MessagingException("Could not create JMS connection.", e);
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
		this.jmsConnection = null;

		this.closed = false;
		this.open();

		if (this.isConnectionValid()) {

			sessionsLock.lock();
			try {
				for (JmsMqSession session : sessions) {
					session.resetSession();
				}
			} finally {
				sessionsLock.unlock();
			}
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

	public Marshaller getMessageMarshaller() {
		return this.messageMarshaller;
	}
	public void setMessageMarshaller(Marshaller messageMarshaller) {
		this.messageMarshaller = messageMarshaller;
	}
	public com.braintribe.model.messaging.jms.JmsMqConnection getConfiguration() {
		return configuration;
	}
	public Connection getJmsConnection() {
		return jmsConnection;
	}
	public JmsMqConnectionProvider getConnectionProvider() {
		return connectionProvider;
	}

}
