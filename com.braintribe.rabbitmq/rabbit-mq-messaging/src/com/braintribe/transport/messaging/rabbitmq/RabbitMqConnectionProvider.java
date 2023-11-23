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
package com.braintribe.transport.messaging.rabbitmq;

import com.braintribe.transport.messaging.api.MessagingComponentStatus;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.utils.lcd.StringTools;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * <p>
 * {@link MessagingConnectionProvider} implementation for providing {@link RabbitMqConnection}(s).
 * 
 * @see MessagingConnectionProvider
 * @see RabbitMqConnection
 */
public class RabbitMqConnectionProvider implements MessagingConnectionProvider<RabbitMqConnection> {

    private ConnectionFactory connectionFactory;
    private com.braintribe.model.messaging.rabbitmq.RabbitMqMessaging providerConfiguration;
    private MessagingContext messagingContext;
	private MessagingComponentStatus status = MessagingComponentStatus.OPEN;

    public RabbitMqConnectionProvider() {
    }
    
    public void setConnectionConfiguration(com.braintribe.model.messaging.rabbitmq.RabbitMqMessaging providerConfiguration) {
    	this.providerConfiguration = providerConfiguration;
    }
    
    public MessagingContext getMessagingContext() {
		return messagingContext;
	}

	public void setMessagingContext(MessagingContext messagingContext) {
		this.messagingContext = messagingContext;
	}
    
	@Override
	public RabbitMqConnection provideMessagingConnection() throws MessagingException {

		Connection connection = null;
		Connection publishingConnection = null;
		RabbitMqConnection rabbitMqConnection = null;
		ConnectionFactory connFactory = getConnectionFactory();
		
		synchronized (this) {
			ensureOpen();
			try {
				connection = connFactory.newConnection();
				publishingConnection = connFactory.newConnection(); 
			} catch (Exception e) {
				throw new MessagingException("Failed to establish a connection to the RabbitMQ server"+(e.getMessage() != null ? ": "+e.getMessage() : ""), e);
			}
			
			rabbitMqConnection = new RabbitMqConnection();
			rabbitMqConnection.setConnection(connection);
			rabbitMqConnection.setPublishingConnection(publishingConnection);
			rabbitMqConnection.setAutomaticRecoveryEnabled(connFactory.isAutomaticRecoveryEnabled());
			rabbitMqConnection.setConnectionProvider(this);
		}
		
		return rabbitMqConnection;
	}

	@Override
	public synchronized void close() {
	    this.connectionFactory = null;
	    this.providerConfiguration = null;
	    this.messagingContext = null;
		this.status = MessagingComponentStatus.CLOSED;
	}
	
	protected ConnectionFactory getConnectionFactory() throws MessagingException {
		
		if (connectionFactory == null) {
			createDelegateFactory();
		}
		
		return connectionFactory;
	}
	
	protected synchronized ConnectionFactory createDelegateFactory() throws MessagingException {
		
		ensureOpen();
		
		if (connectionFactory != null) {
			return connectionFactory;
		}
		
		connectionFactory = new ConnectionFactory();
		
		if (providerConfiguration.getHost() != null) {
			connectionFactory.setHost(providerConfiguration.getHost());
		}
		
		if (providerConfiguration.getVirtualHost() != null) {
			connectionFactory.setVirtualHost(providerConfiguration.getVirtualHost());
		}
		
		if (providerConfiguration.getPort() > 0) {
			connectionFactory.setPort(providerConfiguration.getPort());
		}
		
		if (providerConfiguration.getUsername() != null) {
			connectionFactory.setUsername(providerConfiguration.getUsername());
		}
		
		if (providerConfiguration.getPassword() != null) {
			connectionFactory.setPassword(providerConfiguration.getPassword());
		}
		
		if (providerConfiguration.getUri() != null) {
			try {
				connectionFactory.setUri(providerConfiguration.getUri());
			} catch (Exception e) {
				throw new MessagingException("Failed to set connecton URI"+(e.getMessage() != null ? ": "+e.getMessage() : ""), e);
			}
		}
		
		if (providerConfiguration.getAutomaticRecoveryEnabled() != null) {
			connectionFactory.setAutomaticRecoveryEnabled(providerConfiguration.getAutomaticRecoveryEnabled());
		}
		
		if (providerConfiguration.getTopologyRecoveryEnabled() != null) {
			connectionFactory.setTopologyRecoveryEnabled(providerConfiguration.getTopologyRecoveryEnabled());
		}
		
		if (providerConfiguration.getConnectionTimeout() != null) {
			connectionFactory.setConnectionTimeout(providerConfiguration.getConnectionTimeout());
		}
		
		if (providerConfiguration.getNetworkRecoveryInterval() != null) {
			connectionFactory.setNetworkRecoveryInterval(providerConfiguration.getNetworkRecoveryInterval());
		}
		
		if (providerConfiguration.getRequestedHeartbeat() != null) {
			connectionFactory.setRequestedHeartbeat(providerConfiguration.getRequestedHeartbeat());
		}
		
		return connectionFactory;
	}
	
	private void ensureOpen() throws MessagingException {
		if (this.status != MessagingComponentStatus.OPEN) {
			throw new MessagingException("Messaging connection provider in unexpected state: [ "+this.status.toString().toLowerCase()+" ]");
		}
	}

	@Override
	public String description() {
		if (providerConfiguration == null) {
			return "RabbitMQ Messaging";
		} else {
			String addr = providerConfiguration.getHost();
			if (StringTools.isBlank(addr)) {
				return "RabbitMQ Messaging";
			} else {
				return "RabbitMQ Messaging connected to "+addr;
			}
		}
	}
	
}
