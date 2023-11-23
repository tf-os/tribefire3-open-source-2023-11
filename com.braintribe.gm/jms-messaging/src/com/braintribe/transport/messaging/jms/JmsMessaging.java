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

import com.braintribe.transport.messaging.api.Messaging;

/**
 * This is the main entry point to JMS-based messaging. It is for the actual implementation to provide
 * a subclass of this abstract class.
 * <br><br>
 * Any subclass of this class has to implement the {@link #createConnectionProvider(com.braintribe.model.messaging.expert.Messaging, com.braintribe.transport.messaging.api.MessagingContext)} 
 * method.
 * @see Messaging
 */
public abstract class JmsMessaging <T extends com.braintribe.model.messaging.jms.JmsConnection> implements Messaging<T> {
	//Intentionally left blank
}
