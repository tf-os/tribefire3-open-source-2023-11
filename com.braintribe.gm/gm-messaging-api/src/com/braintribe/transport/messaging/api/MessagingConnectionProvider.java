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
package com.braintribe.transport.messaging.api;

/**
 * <p>
 * Provides {@link MessagingConnection}(s) to a message broker.
 * 
 */
public interface MessagingConnectionProvider<T extends MessagingConnection> {

	/**
	 * <p>
	 * Provides a {@link MessagingConnection} to the message broker.
	 * 
	 * <p>
	 * Once provided and opened, callers must ensure that connections are always closed (with
	 * {@link MessagingConnection#close()}) when no longer used.
	 * 
	 * @return A {@link MessagingConnection} to the message broker.
	 * @throws MessagingException
	 *             In case a {@link MessagingConnection} fails to be provided.
	 */
	T provideMessagingConnection() throws MessagingException;

	/**
	 * <p>
	 * Closes this {@link MessagingConnectionProvider}.
	 * 
	 * @throws MessagingException
	 *             In case this {@link MessagingConnectionProvider} fails to be closed.
	 */
	void close() throws MessagingException;

	
	/**
	 * Returns a description describing the messaging connection provider. This can be compared to a toString method
	 * 
	 * @return A description describing the messaging connection provider.
	 */
	String description();
}
