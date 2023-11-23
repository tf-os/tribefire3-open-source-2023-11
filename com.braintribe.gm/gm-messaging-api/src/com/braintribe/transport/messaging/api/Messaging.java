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
 * The main entry point to a GenericModel-based messaging system.
 * 
 * <p>
 * Providers of specific message brokers must implement this interface.
 * 
 */
public interface Messaging<T extends com.braintribe.model.messaging.expert.Messaging> {

	/**
	 * <p>
	 * Creates a {@link MessagingConnectionProvider} based on the given denotation type.
	 * 
	 * @param connection
	 *            The {@link com.braintribe.model.messaging.expert.Messaging} denotation type for which a
	 *            {@link MessagingConnectionProvider} must be created
	 * @param context
	 *            The {@link MessagingContext} for which the {@link MessagingConnectionProvider} must be created
	 * @return A {@link MessagingConnectionProvider} instance created based on the given denotation type and context
	 */
	MessagingConnectionProvider<? extends MessagingConnection> createConnectionProvider(T connection, MessagingContext context);

}
