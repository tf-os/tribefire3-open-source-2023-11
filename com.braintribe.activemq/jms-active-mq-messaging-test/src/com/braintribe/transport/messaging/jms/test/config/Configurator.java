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
package com.braintribe.transport.messaging.jms.test.config;

import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;

public class Configurator {

	private static TestConfiguration tc = new TestConfiguration();
	private static BrokerService broker;

	public Configurator() throws Exception {
		this.initialize();
	}

	public void initialize() throws Exception {
		tc.setProviderURL("tcp://localhost:61636?soTimeout=5000&daemon=true");
		startBroker();
	}

	private void startBroker() throws Exception {
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://localhost:61636"));

		ManagementContext context = new ManagementContext();
		context.setBrokerName("defaultBroker");
		context.setConnectorHost("localhost");
		context.setConnectorPort(10099);
		context.setUseMBeanServer(true);
		context.setCreateConnector(true);

		broker = new BrokerService();
		broker.setBrokerId("defaultBroker");
		broker.setBrokerName("defaultBroker");
		broker.setUseJmx(true);
		broker.setPersistent(false);

		broker.addConnector(connector);
		broker.setManagementContext(context);
		broker.start();
	}

	public void close() {
		try {
			broker.stop();
		} catch (Exception e) {
			throw new RuntimeException("Broker couldn't stop.", e);
		}
	}

	public TestConfiguration getTestConfiguration() {
		return tc;
	}

}
