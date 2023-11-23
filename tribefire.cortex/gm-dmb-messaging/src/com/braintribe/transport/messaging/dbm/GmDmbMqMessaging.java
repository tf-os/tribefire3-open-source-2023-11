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

import com.braintribe.transport.messaging.api.Messaging;
import com.braintribe.transport.messaging.api.MessagingContext;

/**
 * <p>
 * Dynamic MBean-based implementation of the generic model messaging system.
 * 
 * @see Messaging
 */
public class GmDmbMqMessaging implements Messaging<com.braintribe.model.messaging.dmb.GmDmbMqMessaging> {

	@Override
	public GmDmbMqConnectionProvider createConnectionProvider(com.braintribe.model.messaging.dmb.GmDmbMqMessaging connectionProvider, MessagingContext context) {
		
		GmDmbMqConnectionProvider gmDmbMqConnectionProvider = new GmDmbMqConnectionProvider();
		gmDmbMqConnectionProvider.setConnectionConfiguration(connectionProvider);
		gmDmbMqConnectionProvider.setMessagingContext(context);
		
		return gmDmbMqConnectionProvider;
	}

}
