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
package tribefire.extension.activemq.initializer.wire.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.activemqdeployment.ActiveMqWorker;
import com.braintribe.model.activemqdeployment.HealthCheckProcessor;
import com.braintribe.model.activemqdeployment.NetworkConnector;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools2;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;
import tribefire.extension.activemq.initializer.wire.contract.ActivemqInitializerModuleContract;
import tribefire.extension.activemq.initializer.wire.contract.ActivemqInitializerModuleModelsContract;
import tribefire.extension.activemq.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.activemq.initializer.wire.contract.RuntimePropertiesContract;

@Managed
public class ActivemqInitializerModuleSpace extends AbstractInitializerSpace implements ActivemqInitializerModuleContract {

	private static final Logger logger = Logger.getLogger(ActivemqInitializerModuleSpace.class);

	@Import
	private ActivemqInitializerModuleModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private RuntimePropertiesContract properties;

	@Override
	@Managed
	public ActiveMqWorker worker() {
		ActiveMqWorker bean = create(ActiveMqWorker.T);
		bean.setExternalId("activemq.worker");
		bean.setName("Active MQ Service Worker");
		bean.setModule(existingInstances.module());

		bean.setBindAddress(properties.AMQ_SERVER_BINDADDRESS());
		bean.setPort(properties.AMQ_SERVER_PORT());
		bean.setDataDirectory(properties.AMQ_SERVER_DATA_DIRECTORY());
		bean.setBrokerName(properties.AMQ_SERVER_BROKER_NAME());
		bean.setUseJmx(properties.AMQ_SERVER_USE_JMX());
		bean.setPersistenceDbDir(properties.AMQ_SERVER_PERSISTENCE_DB_DIRECTORY());
		bean.setHeapUsageInPercent(properties.AMQ_SERVER_HEAP_USAGE_IN_PERCENT());
		bean.setDiskUsageLimit(properties.AMQ_SERVER_DISK_USAGE_LIMIT());
		bean.setTempUsageLimit(properties.AMQ_SERVER_TEMP_USAGE_LIMIT());
		bean.setCreateVmConnector(properties.AMQ_SERVER_CREATE_VM_CONNECTOR());
		bean.setPersistent(properties.AMQ_SERVER_PERSISTENT());

		bean.setDiscoveryMulticastUri(properties.AMQ_DISCOVERY_MULTICAST_URI());
		bean.setDiscoveryMulticastGroup(properties.AMQ_DISCOVERY_MULTICAST_GROUP());
		bean.setDiscoveryMulticastNetworkInterface(properties.AMQ_DISCOVERY_MULTICAST_NETWORK_INTERFACE());
		bean.setDiscoveryMulticastAddress(properties.AMQ_DISCOVERY_MULTICAST_ADDRESS());
		bean.setDiscoveryMulticastInterface(properties.AMQ_DISCOVERY_MULTICAST_INTERFACE());

		bean.setClusterNodes(clusterNodes());

		return bean;
	}

	private List<NetworkConnector> clusterNodes() {
		String nodesString = properties.AMQ_CLUSTER_NODES();
		List<NetworkConnector> list = clusterNodes(nodesString, properties.AMQ_SERVER_PORT(), () -> create(NetworkConnector.T));
		return list;
	}

	public static List<NetworkConnector> clusterNodes(String nodesString, int defaultPort, Supplier<NetworkConnector> connectorCreator) {

		List<NetworkConnector> connectors = new ArrayList<>();

		if (!StringTools.isBlank(nodesString)) {

			logger.debug(() -> "AMQ_CLUSTER_NODES config:" + nodesString);

			String[] nodesArray = StringTools.splitCommaSeparatedString(nodesString, true);
			Set<String> nodes = CollectionTools2.asSet(nodesArray);

			logger.debug(() -> "Parsed set:" + nodes);

			Set<String> hostAddresses = NetworkTools.getHostAddresses(null, true);
			Set<String> myAddressesAndPort = hostAddresses.stream().map(h -> h + ":" + defaultPort).collect(Collectors.toSet());
			myAddressesAndPort.add("localhost:" + defaultPort);

			nodes.forEach(n -> {
				String host;
				int port = defaultPort;
				int index = n.indexOf(':');
				if (index > 0) {
					host = n.substring(0, index).trim();
					String portString = n.substring(index + 1).trim();
					port = Integer.parseInt(portString);
				} else {
					host = n;
				}

				final String hostAndPort = host + ":" + port;
				if (myAddressesAndPort.contains(hostAndPort)) {
					logger.debug(() -> "Not adding " + hostAndPort + " to the list of cluster nodes as this is our own service.");

				} else {

					logger.debug(() -> "Adding " + hostAndPort + " to the list of cluster nodes.");

					NetworkConnector nc = connectorCreator.get();
					nc.setName(host + ":" + port);
					nc.setHost(host);
					nc.setPort(port);
					connectors.add(nc);
				}
			});
		}

		return connectors;
	}

	@Override
	@Managed
	public CheckBundle functionalCheckBundle() {
		CheckBundle bean = create(CheckBundle.T);
		bean.setModule(existingInstances.module());
		bean.getChecks().add(healthCheckProcessor());
		bean.setName("ActiveMQ Checks");
		bean.setWeight(CheckWeight.under1s);
		bean.setCoverage(CheckCoverage.functional);
		bean.setIsPlatformRelevant(false);
		return bean;
	}

	@Override
	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = create(HealthCheckProcessor.T);
		bean.setModule(existingInstances.module());
		bean.setName("ActiveMQ Check Processor");
		bean.setExternalId("activemq.healthcheck");
		return bean;
	}
}
