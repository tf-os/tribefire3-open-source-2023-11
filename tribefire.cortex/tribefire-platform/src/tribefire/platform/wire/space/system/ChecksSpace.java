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
package tribefire.platform.wire.space.system;

import com.braintribe.cartridge.common.processing.deployment.ReflectBeansForDeployment;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.extensiondeployment.check.HardwiredCheckProcessor;
import com.braintribe.model.extensiondeployment.check.HardwiredParameterizedCheckProcessor;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.check.CompositeCheckProcessor;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.check.api.CheckProcessorBase;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;
import com.braintribe.model.processing.check.hw.MemoryCheckProcessor;
import com.braintribe.model.processing.check.jdbc.DatabaseConnectionsCheck;
import com.braintribe.model.processing.check.jdbc.SelectedDatabaseConnectionsCheck;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.check.BaseConnectivityCheckProcessor;
import tribefire.platform.impl.check.BaseFunctionalityCheckProcessor;
import tribefire.platform.impl.check.BaseVitalityCheckProcessor;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.services.ClusterSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;

@Managed
public class ChecksSpace implements WireSpace, ReflectBeansForDeployment {

	private static Logger logger = Logger.getLogger(ChecksSpace.class);

	@Import
	private CortexAccessSpace cortexAccess;

	@Import
	private ClusterSpace cluster;

	@Import
	private SystemTasksSpace systemTasks;

	@Import
	private MessagingSpace messaging;

	@Import
	protected DeploymentSpace deployment;

	@Import
	private SystemInformationSpace systemInformation;

	@Import
	private RpcSpace rpc;

	@Managed
	public CompositeCheckProcessor compositeCheckProcessor() {
		CompositeCheckProcessor bean = new CompositeCheckProcessor();
		return bean;
	}

	public static HardwiredParameterizedCheckProcessor parameterizedAccessCheckDeployable(Class<? extends ParameterizedAccessCheckProcessor<?>> clazz) {
		HardwiredParameterizedCheckProcessor bean = HardwiredParameterizedCheckProcessor.T.create();
		return primeCheckDeployable(bean, clazz);
	}

	public static HardwiredCheckProcessor checkProcessorDeployable(Class<? extends CheckProcessor> checkProcessorClass) {
		HardwiredCheckProcessor bean = HardwiredCheckProcessor.T.create();
		return primeCheckDeployable(bean, checkProcessorClass);
	}

	private static <T extends HardwiredDeployable> T primeCheckDeployable(T bean, Class<? extends CheckProcessorBase> checkProcessorClass) {
		String simpleName = checkProcessorClass.getSimpleName();

		bean.setExternalId("checkProcessor.hardwired." + simpleName);
		bean.setName(StringTools.prettifyCamelCase(simpleName));
		bean.setGlobalId(checkProcessorGlobalId(checkProcessorClass));

		return bean;
	}

	public static String checkProcessorGlobalId(Class<? extends CheckProcessorBase> checkProcessorClass) {
		return "hardwired:check/" + checkProcessorClass.getSimpleName();
	}
	
	@Managed
	public DatabaseConnectionsCheck databaseConnectionCheck() {
		DatabaseConnectionsCheck bean = new DatabaseConnectionsCheck();
		bean.setTimeWarnThreshold(getRuntimeLongValue("TRIBEFIRE_CHECK_JDBC_CONN_WARN"));
		bean.setDeployRegistry(deployment.registry());
		bean.setCortexSessionFactory(cortexAccess.sessionProvider());
		return bean;
	}

	@Managed
	public SelectedDatabaseConnectionsCheck selectedDatabaseConnectionsCheck() {
		SelectedDatabaseConnectionsCheck bean = new SelectedDatabaseConnectionsCheck();
		bean.setTimeWarnThreshold(getRuntimeLongValue("TRIBEFIRE_CHECK_JDBC_CONN_WARN"));
		bean.setDeployRegistry(deployment.registry());
		return bean;
	}

	@Managed
	public MemoryCheckProcessor memoryCheckProcessor() {
		MemoryCheckProcessor bean = new MemoryCheckProcessor();
		bean.setGlobalMemoryAvailableWarnThreshold(getRuntimeValue("TRIBEFIRE_CHECK_MEMORY_GLOBAL_WARN"));
		bean.setGlobalMemoryAvailableFailThreshold(getRuntimeValue("TRIBEFIRE_CHECK_MEMORY_GLOBAL_FAIL"));
		bean.setSwapAvailableWarnThreshold(getRuntimeValue("TRIBEFIRE_CHECK_MEMORY_SWAP_WARN"));
		bean.setSwapAvailableFailThreshold(getRuntimeValue("TRIBEFIRE_CHECK_MEMORY_SWAP_FAIL"));
		bean.setJavaMemoryAvailableWarnThreshold(getRuntimeValue("TRIBEFIRE_CHECK_MEMORY_JAVA_WARN"));
		bean.setJavaMemoryAvailableFailThreshold(getRuntimeValue("TRIBEFIRE_CHECK_MEMORY_JAVA_FAIL"));
		return bean;
	}

	@Managed
	public BaseFunctionalityCheckProcessor baseFunctionalityCheckProcessor() {
		BaseFunctionalityCheckProcessor bean = new BaseFunctionalityCheckProcessor();

		if (getRuntimeBooleanValue("TRIBEFIRE_CHECK_HEALTH_LOCK", true)) {
			bean.setLocking(cluster.locking());
		}
		if (getRuntimeBooleanValue("TRIBEFIRE_CHECK_HEALTH_LEADERSHIP", true)) {
			bean.setLeadershipManager(cluster.leadershipManager());
		}
		bean.setScheduledExecutorService(systemTasks.scheduledExecutor());
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		return bean;
	}

	@Managed
	public BaseConnectivityCheckProcessor baseConnectivityCheckProcessor() {
		BaseConnectivityCheckProcessor bean = new BaseConnectivityCheckProcessor();
		bean.setScheduledExecutorService(systemTasks.scheduledExecutor());
		bean.setMessagingSessionProviderSupplier(() -> messaging.sessionProvider());
		bean.setDatabaseInformationProvider(systemInformation.databaseInformationProvider());
		return bean;
	}

	@Managed
	public BaseVitalityCheckProcessor baseVitalityCheckProcessor() {
		BaseVitalityCheckProcessor bean = new BaseVitalityCheckProcessor();
		return bean;
	}

	private static String getRuntimeValue(String key) {
		try {
			String property = TribefireRuntime.getProperty(key);
			return property;
		} catch (Exception e) {
			logger.debug("Cannot get runtime value for key " + key + ".", e);
		}
		return null;
	}

	private static Long getRuntimeLongValue(String key) {
		try {
			String property = TribefireRuntime.getProperty(key);
			if (!StringTools.isBlank(property)) {
				long longValue = Long.parseLong(property);
				return longValue;
			}
		} catch (Exception e) {
			logger.debug("Cannot get runtime value for key " + key + ".", e);
		}
		return null;
	}

	private static Boolean getRuntimeBooleanValue(String key, Boolean defaultValue) {
		try {
			String property = TribefireRuntime.getProperty(key);
			if (!StringTools.isBlank(property)) {
				return Boolean.parseBoolean(property);
			}
		} catch (Exception e) {
			logger.debug("Cannot get runtime value for key " + key + ".", e);
		}
		return defaultValue;
	}
}
