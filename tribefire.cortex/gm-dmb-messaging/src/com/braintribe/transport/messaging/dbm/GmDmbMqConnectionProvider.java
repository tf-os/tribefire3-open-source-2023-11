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
package com.braintribe.transport.messaging.dbm;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServerConnection;

import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.MessagingException;

/**
 * <p>
 * {@link MessagingConnectionProvider} implementation for providing {@link GmDmbMqConnection}(s).
 * 
 * @see MessagingConnectionProvider
 * @see GmDmbMqConnection
 */
public class GmDmbMqConnectionProvider implements MessagingConnectionProvider<GmDmbMqConnection> {

    private com.braintribe.model.messaging.dmb.GmDmbMqMessaging providerConfiguration;
    private MessagingContext messagingContext;


	public GmDmbMqConnectionProvider() {
    }
    
    public void setConnectionConfiguration(com.braintribe.model.messaging.dmb.GmDmbMqMessaging providerConfiguration) {
    	this.providerConfiguration = providerConfiguration;
    }
    
    public MessagingContext getMessagingContext() {
		return messagingContext;
	}

	public void setMessagingContext(MessagingContext messagingContext) {
		this.messagingContext = messagingContext;
	}
    
	@Override
	public GmDmbMqConnection provideMessagingConnection() throws MessagingException {
		
		GmDmbMqConnection gmDmbMqConnection = new GmDmbMqConnection();
		
		gmDmbMqConnection.setConnectionProvider(this);
		gmDmbMqConnection.setMBeanServerConnection(createMBeanServerConnection());
		
		return gmDmbMqConnection;
	}
	
	/**
	 * <p>
	 * Creates a {@link MBeanServerConnection} based on the configuration provided via 
	 * {@link #setConnectionConfiguration(com.braintribe.model.messaging.dmb.GmDmbMqMessaging)} 
	 * 
	 * @return The {@link MBeanServerConnection} created
	 * @throws MessagingException If a {@link MBeanServerConnection} fails to be established
	 */
	protected MBeanServerConnection createMBeanServerConnection() throws MessagingException {
		
		if (this.providerConfiguration == null) {
			throw new MessagingException("No connection provider configuration was set to this connection provider");
		}
		
		return ManagementFactory.getPlatformMBeanServer();
	}

	@Override
	public void close() {
		// no-op, there is nothing to be closed so far in this MessagingConnectionProvider
	}
	
	@Override
	public String toString() {
		return description();
	}

	@Override
	public String description() {
		return "DMB Messaging";
	}
}
