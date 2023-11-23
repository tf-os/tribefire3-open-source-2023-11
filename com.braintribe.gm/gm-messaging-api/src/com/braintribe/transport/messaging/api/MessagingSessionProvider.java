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
 * Offers a convenience for applications for obtaining {@link MessagingSession}s abstracting the handling of
 * {@link MessagingConnection}(s) and their lifecycle.
 * 
 *
 */
public interface MessagingSessionProvider {

	/**
	 * <p>
	 * Provides a brand new {@link MessagingSession} established exclusively for the caller.
	 * 
	 * <p>
	 * Once provided, callers must ensure that sessions are always closed (with {@link MessagingSession#close()}) when
	 * no longer used.
	 * 
	 * @return A {@link MessagingSession} established exclusively for the caller.
	 * @throws MessagingException
	 *             In case a {@link MessagingSession} fails to be provided
	 */
	MessagingSession provideMessagingSession() throws MessagingException;

	/**
	 * <p>
	 * Closes this messaging session provider.
	 */
	void close();

	/**
	 * Returns a description describing the messaging provider. This can be compared to a toString method
	 * 
	 * @return A description describing the messaging provider.
	 */
	String description();

}
