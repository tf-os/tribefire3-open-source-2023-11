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
package com.braintribe.transport.messaging.jms.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.api.test.GmMessagingConnectionTest;
import com.braintribe.transport.messaging.jms.test.config.Configurator;


public class JmsMessagingConnectionTest extends GmMessagingConnectionTest {

	@BeforeClass
	public static void initTests() throws Exception {
		ConfigurationHolder.configurator = new Configurator();
	}
	@AfterClass
	public static void shutdown() {
		ConfigurationHolder.configurator.close();
	}

	@Override
	protected MessagingConnectionProvider<? extends MessagingConnection> getMessagingConnectionProvider() {
		return JmsMessagingConnectionProvider.instance.get();
	}

	@Override
	protected MessagingContext getMessagingContext() {
		return JmsMessagingConnectionProvider.instance.getMessagingContext();
	}

}
