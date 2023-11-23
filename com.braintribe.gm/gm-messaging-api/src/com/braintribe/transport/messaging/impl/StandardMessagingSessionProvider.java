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
package com.braintribe.transport.messaging.impl;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingException;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;

/**
 * Natural implementation of {@link MessagingSessionProvider}.
 * <p>
 * This component establishes and manages {@link MessagingConnection}(s) to the platform message broker, providing
 * {@link MessagingSession}(s).
 * 
 */
public class StandardMessagingSessionProvider implements MessagingSessionProvider, LifecycleAware {

	// constants
	private static final Logger log = Logger.getLogger(StandardMessagingSessionProvider.class);

	// configurable
	private MessagingConnectionProvider<?> messagingConnectionProvider;
	private boolean lazyInitialization = true;

	// cached
	private MessagingConnection messagingConnection;

	public StandardMessagingSessionProvider() {
	}

	@Required
	@Configurable
	public void setMessagingConnectionProvider(MessagingConnectionProvider<?> messagingConnectionProvider) {
		this.messagingConnectionProvider = messagingConnectionProvider;
	}

	@Configurable
	public void setLazyInitialization(boolean lazyInitialization) {
		this.lazyInitialization = lazyInitialization;
	}

	@Override
	public void postConstruct() {
		ensureConnection(true);
	}

	@Override
	public void preDestroy() {
		close();
	}

	@Override
	public MessagingSession provideMessagingSession() throws MessagingException {
		ensureConnection(false);

		return messagingConnection.createMessagingSession();
	}

	@Override
	public void close() {
		closeConnection();
		closeConnectionProvider();
	}

	private void closeConnection() {
		MessagingConnection connection = messagingConnection;

		if (connection == null)
			return;

		try {
			connection.close();

			log.debug(() -> "Closed messaging connection: " + connection);

		} catch (Exception e) {
			log.error("Failed to close messaging connection", e);
		}
	}

	private void closeConnectionProvider() {
		MessagingConnectionProvider<?> connectionProvider = messagingConnectionProvider;
		if (connectionProvider == null)
			return;

		try {
			connectionProvider.close();

			log.debug(() -> "Closed messaging connection provider: " + connectionProvider);

		} catch (Exception e) {
			log.error("Failed to close messaging connection provider", e);
		}
	}

	private void initializeConnection() {
		if (messagingConnection != null)
			return;

		synchronized (this) {
			if (messagingConnection == null) {
				messagingConnection = messagingConnectionProvider.provideMessagingConnection();

				log.debug(() -> "Initialized messaging connection: " + messagingConnection);
			}
		}
	}

	private void ensureConnection(boolean startup) {
		if (messagingConnection == null && startup ^ lazyInitialization) {
			initializeConnection();
		}
	}

	@Override
	public String toString() {
		return description();
	}

	@Override
	public String description() {
		if (messagingConnectionProvider == null) {
			return "StandardMessagingSessionProvider (unconfigured)";
		} else {
			return messagingConnectionProvider.description();
		}
	}
}
