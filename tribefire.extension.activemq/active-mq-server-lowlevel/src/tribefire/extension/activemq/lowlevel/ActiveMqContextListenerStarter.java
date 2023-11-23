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
package tribefire.extension.activemq.lowlevel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.braintribe.config.configurator.Configurator;
import com.braintribe.config.configurator.ConfiguratorException;
import com.braintribe.config.configurator.ConfiguratorPriority;
import com.braintribe.config.configurator.ConfiguratorPriority.Level;
import com.braintribe.logging.Logger;
import com.braintribe.model.activemqdeployment.ActiveMqWorker;
import com.braintribe.model.activemqdeployment.NetworkConnector;
import com.braintribe.model.processing.activemq.service.ActiveMqWorkerImpl;
import com.braintribe.model.processing.activemq.service.BrokerServiceHolder;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * This class is intended to start the ActiveMQ service as soon as possible (before deployment starts, where messaging
 * should already be in place). There are basically two possible ways: as a ServletContextListener or as a Configurator
 * The ServletContextListener kicks in too early as, for example, the Runtime Properties are not yet read from the
 * tribefire.properties file. Hence, the service is started when the configure() method is invoked. However, this class
 * also implements the ServletContextListener interface as the Configurator has no way to act on a shutdown.
 */
@ConfiguratorPriority(value = Level.normal, order = 0)
public class ActiveMqContextListenerStarter implements ServletContextListener, Configurator {

	private static final Logger logger = Logger.getLogger(ActiveMqContextListenerStarter.class);

	private static ActiveMqWorkerImpl activeMqWorker = null;

	@Override
	public void configure() throws ConfiguratorException {

		if (activeMqWorker != null) {
			logger.debug(() -> "ActiveMQ server already started.");
			return;
		}

		String deploymentStartString = TribefireRuntime.getProperty("AMQ_DEPLOYMENT_START", "false");
		boolean deploymentStart = deploymentStartString.equalsIgnoreCase("true");
		if (deploymentStart) {
			logger.debug(() -> "AMQ_DEPLOYMENT_START is set to true. Not starting in the context listener.");
			return;
		}
		logger.debug(() -> "Starting ActiveMQ server in context listener.");

		String bindAddress = TribefireRuntime.getProperty("AMQ_SERVER_BINDADDRESS", "0.0.0.0");
		String brokerName = TribefireRuntime.getProperty("AMQ_SERVER_BROKER_NAME");
		String portString = TribefireRuntime.getProperty("AMQ_SERVER_PORT", "61616");
		int port;
		try {
			port = Integer.parseInt(portString);
		} catch (NumberFormatException e) {
			throw new ConfiguratorException("Invalid port '" + portString + "' specified for AMQ_SERVER_PORT!", e);
		}

		String clusterNodesString = TribefireRuntime.getProperty("AMQ_CLUSTER_NODES");
		List<NetworkConnector> clusterNodes = clusterNodes(clusterNodesString, port, () -> NetworkConnector.T.create());
		String createVmConnectorString = TribefireRuntime.getProperty("AMQ_SERVER_CREATE_VM_CONNECTOR");
		Boolean createVmConnector = createVmConnectorString != null ? createVmConnectorString.equalsIgnoreCase("true") : null;
		String dataDirectoryString = TribefireRuntime.getProperty("AMQ_SERVER_DATA_DIRECTORY", "activemq-data");
		File dataDirectory = new File(dataDirectoryString);
		String diskUsageLimitString = TribefireRuntime.getProperty("AMQ_SERVER_DISK_USAGE_LIMIT");
		Long diskUsageLimit = diskUsageLimitString != null ? Long.parseLong(diskUsageLimitString) : null;
		String heapUsageInPercentString = TribefireRuntime.getProperty("AMQ_SERVER_HEAP_USAGE_IN_PERCENT");
		Integer heapUsageInPercent = heapUsageInPercentString != null ? Integer.parseInt(heapUsageInPercentString) : null;
		String persistenceDirectoryString = TribefireRuntime.getProperty("AMQ_SERVER_PERSISTENCE_DB_DIRECTORY", "activemq-db");
		File persistenceDirectory = new File(persistenceDirectoryString);
		String tempUsageLimitString = TribefireRuntime.getProperty("AMQ_SERVER_TEMP_USAGE_LIMIT");
		Long tempUsageLimit = tempUsageLimitString != null ? Long.parseLong(tempUsageLimitString) : null;
		String useJmxString = TribefireRuntime.getProperty("AMQ_SERVER_USE_JMX", "true");
		boolean useJmx = useJmxString.equalsIgnoreCase("true");
		String persistentString = TribefireRuntime.getProperty("AMQ_SERVER_PERSISTENT", "false");
		boolean persistent = persistentString.equalsIgnoreCase("true");
		ActiveMqWorker deployable = ActiveMqWorker.T.create();
		deployable.setExternalId("activemq.server.deployable");

		String discoveryMulticastUri = TribefireRuntime.getProperty("AMQ_DISCOVERY_MULTICAST_URI");
		String discoveryMulticastGroup = TribefireRuntime.getProperty("AMQ_DISCOVERY_MULTICAST_GROUP");
		String discoveryMulticastNetworkInterface = TribefireRuntime.getProperty("AMQ_DISCOVERY_MULTICAST_NETWORK_INTERFACE");
		String discoveryMulticastAddress = TribefireRuntime.getProperty("AMQ_DISCOVERY_MULTICAST_ADDRESS");
		String discoveryMulticastInterface = TribefireRuntime.getProperty("AMQ_DISCOVERY_MULTICAST_INTERFACE");

		activeMqWorker = new ActiveMqWorkerImpl();
		activeMqWorker.setBindAddress(bindAddress);
		activeMqWorker.setBrokerName(brokerName);
		activeMqWorker.setClusterNodes(clusterNodes);
		activeMqWorker.setCreateVmConnector(createVmConnector);
		activeMqWorker.setDataDirectory(dataDirectory);
		activeMqWorker.setDiskUsageLimit(diskUsageLimit);
		activeMqWorker.setHeapUsageInPercent(heapUsageInPercent);
		activeMqWorker.setPersistenceDbDir(persistenceDirectory);
		activeMqWorker.setPort(port);
		activeMqWorker.setTempUsageLimit(tempUsageLimit);
		activeMqWorker.setUseJmx(useJmx);
		activeMqWorker.setWorkerIdentification(deployable);
		activeMqWorker.setPersistent(persistent);
		activeMqWorker.setBrokerServiceReceiver(ActiveMqWorkerImpl.staticBrokerServiceConsumer());
		activeMqWorker.setDiscoveryMulticastUri(discoveryMulticastUri);
		activeMqWorker.setDiscoveryMulticastGroup(discoveryMulticastGroup);
		activeMqWorker.setDiscoveryMulticastNetworkInterface(discoveryMulticastNetworkInterface);
		activeMqWorker.setDiscoveryMulticastAddress(discoveryMulticastAddress);
		activeMqWorker.setDiscoveryMulticastInterface(discoveryMulticastInterface);
		activeMqWorker.setBrokerServiceReceiver(BrokerServiceHolder.holder);

		activeMqWorker.postConstruct();

		activeMqWorker.start(null);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Nothing to do
	}

	public static List<NetworkConnector> clusterNodes(String nodesString, int defaultPort, Supplier<NetworkConnector> connectorCreator) {

		List<NetworkConnector> connectors = new ArrayList<>();

		if (!StringTools.isBlank(nodesString)) {

			logger.debug(() -> "AMQ cluster nodes configuration (from AMQ_CLUSTER_NODES setting): '" + nodesString + "'");

			String[] nodesArray = StringTools.splitCommaSeparatedString(nodesString, true);
			Set<String> nodes = CollectionTools2.asSet(nodesArray);

			logger.debug(() -> "Parsed set of configured AMQ cluster nodes: " + nodes);

			Set<String> hostAddresses = NetworkTools.getHostAddresses(null, true);
			Set<String> myAddressesAndPort = hostAddresses.stream().map(h -> h + ":" + defaultPort).collect(Collectors.toSet());
			myAddressesAndPort.add("localhost:" + defaultPort);

			nodes.forEach(node -> {
				String host;
				int port;
				int index = node.indexOf(':');
				if (index > 0) {
					host = node.substring(0, index).trim();
					String portString = node.substring(index + 1).trim();
					port = Integer.parseInt(portString);
				} else {
					host = node;
					port = defaultPort;
				}

				final String hostAndPort = host + ":" + port;
				if (myAddressesAndPort.contains(hostAndPort)) {
					logger.debug(() -> "Not adding " + hostAndPort + " to the list of AMQ cluster nodes as this is our own service.");
				} else {
					logger.debug(() -> "Adding " + hostAndPort + " to the list of AMQ cluster nodes.");

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
	public void contextDestroyed(ServletContextEvent sce) {
		if (activeMqWorker != null) {
			activeMqWorker.stop(null);
		}
	}

}
