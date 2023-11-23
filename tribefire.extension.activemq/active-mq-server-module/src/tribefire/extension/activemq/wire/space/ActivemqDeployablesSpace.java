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
package tribefire.extension.activemq.wire.space;

import java.io.File;

import com.braintribe.logging.Logger;
import com.braintribe.model.activemqdeployment.ActiveMqWorker;
import com.braintribe.model.processing.activemq.service.ActiveMqWorkerImpl;
import com.braintribe.model.processing.activemq.service.BrokerServiceHolder;
import com.braintribe.model.processing.activemq.service.HealthCheckProcessor;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.TribefirePlatformContract;

@Managed
public class ActivemqDeployablesSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(ActivemqDeployablesSpace.class);

	@Import
	private ModuleResourcesContract moduleResources;

	@Import
	private TribefirePlatformContract tribefirePlatform;

	@Managed
	public ActiveMqWorkerImpl worker(ExpertContext<ActiveMqWorker> context) {

		ActiveMqWorker deployable = context.getDeployable();

		ActiveMqWorkerImpl bean = new ActiveMqWorkerImpl();
		bean.setBindAddress(deployable.getBindAddress());
		bean.setBrokerName(deployable.getBrokerName());
		bean.setClusterNodes(deployable.getClusterNodes());
		bean.setCreateVmConnector(deployable.getCreateVmConnector());
		bean.setDataDirectory(getDirectory(deployable.getDataDirectory(), null));
		bean.setDiskUsageLimit(deployable.getDiskUsageLimit());
		bean.setHeapUsageInPercent(deployable.getHeapUsageInPercent());
		bean.setPersistenceDbDir(getDirectory(deployable.getPersistenceDbDir(), "activemq-data"));
		bean.setPort(deployable.getPort());
		bean.setTempUsageLimit(deployable.getTempUsageLimit());
		bean.setUseJmx(deployable.getUseJmx());
		bean.setWorkerIdentification(deployable);
		bean.setPersistent(deployable.getPersistent());
		bean.setBrokerServiceReceiver(BrokerServiceHolder.holder);
		bean.setDiscoveryMulticastUri(deployable.getDiscoveryMulticastUri());
		bean.setDiscoveryMulticastGroup(deployable.getDiscoveryMulticastGroup());
		bean.setDiscoveryMulticastNetworkInterface(deployable.getDiscoveryMulticastNetworkInterface());
		bean.setDiscoveryMulticastAddress(deployable.getDiscoveryMulticastAddress());
		bean.setDiscoveryMulticastInterface(deployable.getDiscoveryMulticastInterface());

		return bean;

	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor(
			@SuppressWarnings("unused") ExpertContext<com.braintribe.model.activemqdeployment.HealthCheckProcessor> context) {
		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setBrokerServiceSupplier(BrokerServiceHolder.holder);
		return bean;
	}

	protected File getDirectory(String filePath, String defaultValue) {
		boolean debug = logger.isDebugEnabled();

		if (filePath == null || filePath.trim().length() == 0) {
			if (defaultValue == null) {
				return null;
			}
			filePath = defaultValue;
		}
		File f = new File(filePath);

		if (!f.isAbsolute()) {
			try {
				if (filePath.toLowerCase().startsWith("web-inf/")) {
					filePath = filePath.substring("web-inf/".length());
				}
				File webInfedPath = tribefirePlatform.resources().cache(filePath).asFile();
				f = webInfedPath;
			} catch (Exception e) {
				throw new RuntimeException("Could not put the relative path " + filePath + " into relation of the Cache directory.", e);
			}
			if (debug)
				logger.debug("The file path " + filePath + " seems to be a relative path. Computed: " + f.getAbsolutePath());
		} else {
			if (debug)
				logger.debug("Treating the file path " + filePath + " as an absolute path.");
		}

		if (!f.exists()) {
			if (debug)
				logger.debug("The directory " + f.getAbsolutePath() + " does not yet exist.");
		} else {
			if (debug)
				logger.debug("The directory " + f.getAbsolutePath() + " exists.");
		}
		return f;
	}
}
