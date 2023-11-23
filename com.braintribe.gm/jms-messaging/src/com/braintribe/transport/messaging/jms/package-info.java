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
/**
 * The classes of this package are the base for any JMS-related implementation of the {@link com.braintribe.transport.messaging.api.Messaging} interface.
 * It provides implementations of these interfaces that are common to all JMS-based APIs.
 * <br><br>
 * Actual implementation that use this artifact as a base have to extend the following classes:
 * <ul>
 *  <li>{@link com.braintribe.transport.messaging.jms.JmsMessaging}</li>
 *  <li>{@link com.braintribe.transport.messaging.jms.JmsConnectionProvider}</li>
 *  <li>{@link com.braintribe.transport.messaging.jms.AbstractJmsMessagingSessionProvider}</li>
 * </ul>
 * <br><br>
 * The following artifacts are known at the time of writing to use this base code:
 * <br><br>
 * <ul>
 *  <li><code>JmsJndiMessaging</code>, which is an abstract base for</li>
 *  <li>&nbsp;&nbsp;&nbsp;<code>JmsJBossMessaging</code> (uses JBoss JMS messaging)</li>
 *  <li><code>JmsActiveMqMessaging</code> (uses Apache ActiveMQ messaging)</li>
 * </ul>
 */
package com.braintribe.transport.messaging.jms;
