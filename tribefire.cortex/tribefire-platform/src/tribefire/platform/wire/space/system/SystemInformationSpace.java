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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.packaging.PackagingProvider;
import com.braintribe.model.processing.platformreflection.db.StandardDatabaseInformationProvider;
import com.braintribe.model.processing.platformreflection.host.HostInformationProvider;
import com.braintribe.model.processing.platformreflection.host.tomcat.TomcatHostInformationProvider;
import com.braintribe.model.processing.platformreflection.os.StandardSystemInformationProvider;
import com.braintribe.model.processing.platformreflection.processor.PlatformReflectionProcessor;
import com.braintribe.model.processing.platformreflection.tf.StandardTribefireInformationProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.check.processing.CheckBundlesProcessor;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.HttpSpace;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.PlatformSetupAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.SystemAccessCommonsSpace;
import tribefire.platform.wire.space.cortex.services.ClusterSpace;
import tribefire.platform.wire.space.module.ModuleInitializationSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.security.accesses.AuthAccessSpace;
import tribefire.platform.wire.space.security.accesses.UserSessionsAccessSpace;
import tribefire.platform.wire.space.security.accesses.UserStatisticsAccessSpace;
import tribefire.platform.wire.space.system.servlets.SystemServletsSpace;

@Managed
public class SystemInformationSpace implements WireSpace {

	@Import
	private LicenseSpace license;

	@Import
	private MasterResourcesSpace resources;

	@Import
	private GmSessionsSpace sessions;

	@Import
	private RpcSpace rpc;

	@Import
	private AuthContextSpace authContext;

	@Import
	private SystemTasksSpace systemTasks;

	@Import
	private SystemServletsSpace systemServlets;

	@Import
	private CartridgeInformationSpace cartridgeInformation;

	@Import
	private HttpSpace http;

	@Import
	private CortexAccessSpace cortexAccess;

	@Import
	private MarshallingSpace marshalling;

	@Import
	private PlatformSetupAccessSpace platformSetupAccess;

	@Import
	private MessagingSpace messaging;

	@Import
	private ClusterSpace cluster;

	@Import
	private SystemAccessCommonsSpace systemAccessCommons;

	@Import
	private ModuleInitializationSpace moduleInitialization;

	@Import
	private ResourceProcessingSpace resourceProcessing;

	@Import
	private AuthAccessSpace authAccess;

	@Import
	private UserSessionsAccessSpace userSessionsAccess;

	@Import
	private UserStatisticsAccessSpace userStatisticsAccess;

	@Managed
	public PackagingProvider packagingProvider() {
		PackagingProvider packagingProvider = new PackagingProvider();
		try {
			packagingProvider.setPackagingUrl(resources.webInf("Resources/Packaging/packaging.xml").asUrl());
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to obtain the packaging");
		}
		return packagingProvider;
	}

	@Managed
	public PlatformReflectionProcessor platformReflectionProcessor() {
		PlatformReflectionProcessor bean = new PlatformReflectionProcessor();

		StandardTribefireInformationProvider tfInfoProvider = new StandardTribefireInformationProvider();
		tfInfoProvider.setLicenseManager(license.manager());
		tfInfoProvider.setPackagingProvider(packagingProvider());
		tfInfoProvider.setSessionFactory(sessions.systemSessionFactory());
		tfInfoProvider.setPlatformSetupSupplier(platformSetupAccess.platformSetupSupplier());
		tfInfoProvider.setCompoundBlockPool(resourceProcessing.streamPipeFactory());
		tfInfoProvider.setHardwiredAuthAccessSupplier(() -> authAccess.access());
		tfInfoProvider.setHardwiredCortexAccessSupplier(() -> cortexAccess.access());
		tfInfoProvider.setHardwiredSetupAccessSupplier(() -> platformSetupAccess.access());
		tfInfoProvider.setHardwiredUserSessionsAccessSupplier(() -> userSessionsAccess.access());
		tfInfoProvider.setHardwiredUserStatisticsAccessSupplier(() -> userStatisticsAccess.access());

		bean.setTribefireInformationProvider(tfInfoProvider);
		bean.setHostInformationProviderMap(hostInformationProviderMap());
		bean.setCommandExecution(systemTasks.commandExecution());
		bean.setUserNameProvider(authContext.currentUser().userNameProvider());
		bean.setPackagingProvider(packagingProvider());
		bean.setLocalInstanceId(cartridgeInformation.instanceId());
		bean.setSystemInformationProvider(systemInformationProvider());

		bean.setEvaluator(rpc.serviceRequestEvaluator());
		bean.setJsonMarshaller(marshalling.jsonMarshaller());
		bean.setPlatformSetupSupplier(platformSetupAccess.platformSetupSupplier());
		bean.setZipPassword(TribefireRuntime.getProperty("TRIBEFIRE_PLATFORM_REFLECTION_ZIP_PASSWORD"));
		bean.setConfFolder(resources.conf("").asFile());
		bean.setModulesFolder(moduleInitialization.modulesFolder());
		bean.setDatabaseFolder(resources.database("").asFile());
		bean.setSharedStorageSupplier(systemAccessCommons.sharedStorageSupplier());
		bean.setSetupAccessSessionProvider(platformSetupAccess.sessionProvider());
		bean.setSetupInfoPath(resources.setupInfo("").asFile());
		bean.setYamlMarshaller(marshalling.yamlMarshaller());
		bean.setStreamPipeFactory(resourceProcessing.streamPipeFactory());
		bean.setSessionFactory(sessions.systemSessionFactory());

		return bean;
	}

	@Managed
	private StandardSystemInformationProvider systemInformationProvider() {
		StandardSystemInformationProvider bean = new StandardSystemInformationProvider();
		bean.setMessagingSessionProviderSupplier(() -> messaging.sessionProvider());
		bean.setDatabaseInformationProvider(databaseInformationProvider());
		bean.setLocking(cluster.locking());
		bean.setLeadershipManager(cluster.leadershipManager());
		return bean;
	}

	@Managed
	public StandardDatabaseInformationProvider databaseInformationProvider() {
		StandardDatabaseInformationProvider bean = new StandardDatabaseInformationProvider();
		bean.setCortexSessionSupplier(cortexAccess.sessionProvider());
		return bean;
	}

	@Managed
	public CheckBundlesProcessor checkBundlesProcessor() {
		CheckBundlesProcessor bean = new CheckBundlesProcessor();

		bean.setEvaluator(rpc.systemServiceRequestEvaluator());
		bean.setCortexSessionSupplier(cortexAccess.sessionProvider());
		bean.setInstanceId(cartridgeInformation.instanceId());
		bean.setThreadContextScoping(authContext.currentUser().threadContextScoping());

		return bean;
	}

	@Managed
	private HostInformationProvider hostInformationProvider() {
		TomcatHostInformationProvider bean = new TomcatHostInformationProvider();
		return bean;
	}

	@Managed
	private Map<String, HostInformationProvider> hostInformationProviderMap() {
		Map<String, HostInformationProvider> bean = new HashMap<>();
		bean.put("tomcat.*", hostInformationProvider());
		bean.put("org.apache.naming.NamingContext.*", hostInformationProvider());
		return bean;
	}

}
