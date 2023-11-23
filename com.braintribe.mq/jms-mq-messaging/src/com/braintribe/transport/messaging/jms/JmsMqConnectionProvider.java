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

import javax.net.ssl.SSLSocketFactory;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.jms.JmsMqConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.braintribe.utils.lcd.StringTools;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class JmsMqConnectionProvider implements MessagingConnectionProvider<com.braintribe.transport.messaging.jms.JmsMqConnection> {

	private static final Logger logger = Logger.getLogger(JmsMqConnectionProvider.class);
	
	protected JmsMqConnection configuration = null;
	protected MQConnectionFactory connectionFactory = null;
	protected SslSocketFactoryProvider sslSocketFactoryProvider = null;

	protected Set<com.braintribe.transport.messaging.jms.JmsMqConnection> connections = new HashSet<>();
	protected ReentrantLock connectionsLock = new ReentrantLock();

	protected MessagingContext context = null;


	@Override
	public com.braintribe.transport.messaging.jms.JmsMqConnection provideMessagingConnection() throws MessagingException {
		try {
			createConnectionFactory();
			logger.trace(() -> "Successfully created queue connection");
			com.braintribe.transport.messaging.jms.JmsMqConnection connection = new com.braintribe.transport.messaging.jms.JmsMqConnection(configuration, this, connectionFactory);
			
			this.addConnection(connection);
			
			return connection;
			
		} catch(Exception e) {
			throw new MessagingException("Could not initialize JMS connection.", e);
		}
	}
	
	protected void addConnection(com.braintribe.transport.messaging.jms.JmsMqConnection connection) {
		connectionsLock.lock();
		try {
			connections.add(connection);
		} finally {
			connectionsLock.unlock();
		}
	}

	@Override
	public void close() throws MessagingException {
		Set<com.braintribe.transport.messaging.jms.JmsMqConnection> cloneSet = new HashSet<>();
		connectionsLock.lock();
		try {
			cloneSet.addAll(this.connections);
			connections.clear();
		} finally {
			connectionsLock.unlock();
		}
		
		for (com.braintribe.transport.messaging.jms.JmsMqConnection c : cloneSet) {
			try {
				c.close();
			} catch (MessagingException e) {
				logger.error("Error while closing connection "+c, e);
			}
		}

	}

	
	
	protected void createConnectionFactory() throws Exception {
		if (this.connectionFactory != null) {
			return;
		}
		try {

			connectionFactory = new MQConnectionFactory();

			if (configuration.getEnableTracing()) {
				logger.debug(() -> "Enable MQ tracing.");
				MQEnvironment.enableTracing(10);			
			}

			int ccsId = configuration.getCcsId();
			if (ccsId != -1) {
				logger.debug(() -> "Using ccsId: "+ccsId);
				connectionFactory.setCCSID(ccsId);
			}

			if (sslSocketFactoryProvider != null) {
				logger.debug(() -> "Setting a SSLSocketFactory in MQConnectionFactory");
				SSLSocketFactory sslSocketFactory = sslSocketFactoryProvider.provideSSLSocketFactory();
				connectionFactory.setSSLSocketFactory(sslSocketFactory);
			}

			String host = configuration.getHost();
			int port = configuration.getPort();
			String channel = configuration.getChannel();
			String queueManager = configuration.getQueueManager();
			boolean useBindingsModeConnections = configuration.getUseBindingsModeConnections();
			
			logger.debug(() -> String
					.format("creating new MQ connection factory: qmHost=%s:%d, qmChannel=%s, queueManager=%s, useBindingsModeConnections=%s",
							host, port, channel, queueManager, useBindingsModeConnections));

			/* Valid types are
	    		WMQConstants.WMQ_CM_BINDINGS
	    		WMQConstants.WMQ_CM_CLIENT
	    		WMQConstants.WMQ_CM_DIRECT_TCPIP
	    		WMQConstants.WMQ_CM_DIRECT_HTTP 
			 */
			if (useBindingsModeConnections) {
				connectionFactory.setTransportType(WMQConstants.WMQ_CM_BINDINGS); //0
			} else {
				connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT); //1
			}
			
			connectionFactory.setHostName(host);
			connectionFactory.setPort(port);
			connectionFactory.setChannel(channel);
			connectionFactory.setQueueManager(queueManager);
			
			String sslPeerName = configuration.getSslPeerName();
			if (sslPeerName != null) {
				logger.debug(() -> "Using sslPeerName: "+sslPeerName);
				connectionFactory.setSSLPeerName(sslPeerName );
			}

			String sslCipherSuite = configuration.getSslCipherSuite();
			if (sslCipherSuite != null) {
				logger.debug(() -> "Using sslCipherSuite: "+sslCipherSuite);
				connectionFactory.setSSLCipherSuite(sslCipherSuite);
			}

		} catch (Exception e) {
			throw new Exception("Could not initialize naming context.", e);
		}
	}

	@Configurable
	public void setSslSocketFactoryProvider(SslSocketFactoryProvider sslSocketFactoryProvider) {
		this.sslSocketFactoryProvider = sslSocketFactoryProvider;
	}

	public MessagingContext getContext() {
		return context;
	}
	public void setContext(MessagingContext context) {
		this.context = context;
	}
	public void setConfiguration(JmsMqConnection configuration) {
		this.configuration = configuration;
	}

	@Override
	public String description() {
		if (configuration == null) {
			return "MQ Messaging";
		} else {
			String addr = configuration.getHostAddress();
			if (StringTools.isBlank(addr)) {
				return "MQ Messaging";
			} else {
				return "MQ Messaging connected to "+addr;
			}
		}
	}

	@Override
	public String toString() {
		return description();
	}
}
