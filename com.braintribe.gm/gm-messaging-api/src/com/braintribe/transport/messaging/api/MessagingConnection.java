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
 * Connection to a message broker.
 * 
 */
public interface MessagingConnection {

	/**
	 * <p>
	 * Opens (or starts) the messaging connection.
	 * 
	 * <p>
	 * Opening an already opened connection has no effect.
	 * 
	 * <p>
	 * Attempting to open a connection already closed throws a {@link MessagingException}.
	 * 
	 * <p>
	 * Opening a connection explicitly with this method is not mandatory, as implementations must ensure that the
	 * connection is opened on {@link #createMessagingSession()} calls.
	 * 
	 * @throws MessagingException
	 *             If the provider fails to open a connection to the underlying message broker, or if {@link #close()}
	 *             was already called for this connection.
	 */
	void open() throws MessagingException;

	/**
	 * <p>
	 * Closes the messaging connection, ensuring that the {@link MessagingSession}(s), {@link MessageProducer}(s) and
	 * {@link MessageConsumer}(s) created through it are also closed.
	 * 
	 * <p>
	 * Closing an already closed connection has no effect.
	 * 
	 * <p>
	 * Once closed, a connection cannot be reopened, thus after calling this method on a connection, subsequent calls to
	 * {@link #open()} must fail.
	 * 
	 * @throws MessagingException
	 *             In case of failures while closing this connection
	 */
	void close() throws MessagingException;

	/**
	 * <p>
	 * Creates a {@link MessagingSession}.
	 * 
	 * <p>
	 * This method throws a {@link MessagingException} if {@link #close()} was already called for this connection.
	 * 
	 * <p>
	 * If {@link #open()} was not explicitly called on this connection, implementations must ensure that calling this
	 * method will open the connection.
	 * 
	 * @return A {@link MessagingSession}
	 * 
	 * @throws MessagingException
	 *             If the provider fails to open a session to the underlying message broker, or if {@link #close()} was
	 *             already called for this connection.
	 */
	MessagingSession createMessagingSession() throws MessagingException;

}
