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
package tribefire.platform.wire.space.module;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.module.loading.PlatformModelApiSpace;
import tribefire.module.wire.contract.ClusterContract;
import tribefire.module.wire.contract.CryptoContract;
import tribefire.module.wire.contract.DeploymentContract;
import tribefire.module.wire.contract.HttpContract;
import tribefire.module.wire.contract.MasterUserAuthContextContract;
import tribefire.module.wire.contract.RequestProcessingContract;
import tribefire.module.wire.contract.RequestUserRelatedContract;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.SecurityContract;
import tribefire.module.wire.contract.ServletsContract;
import tribefire.module.wire.contract.SystemToolsContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.ThreadingContract;
import tribefire.module.wire.contract.TopologyContract;
import tribefire.module.wire.contract.TribefireConnectionsContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformBindersContract;
import tribefire.module.wire.contract.WebPlatformMarshallingContract;
import tribefire.module.wire.contract.WebPlatformReflectionContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;
import tribefire.module.wire.contract.WorkerContract;
import tribefire.platform.impl.module.WebPlatformHardwiredExpertsRegistry;
import tribefire.platform.impl.module.WebPlatformHardwiredRegistry;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.common.BindersSpace;
import tribefire.platform.wire.space.common.CryptoSpace;
import tribefire.platform.wire.space.common.HttpSpace;
import tribefire.platform.wire.space.common.MessagingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.common.TribefireConnectionsSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.SystemAccessCommonsSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.services.ClusterSpace;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.MasterUserAuthContextSpace;
import tribefire.platform.wire.space.security.servlets.SecurityServletSpace;
import tribefire.platform.wire.space.system.SystemTasksSpace;
import tribefire.platform.wire.space.system.TopologySpace;
import tribefire.platform.wire.space.system.servlets.ServletsSpace;
import tribefire.platform.wire.space.system.servlets.SystemServletsSpace;
import tribefire.platform.wire.space.system.servlets.WebRegistrySpace;

/**
 * @author peter.gazdik
 */
@Managed
public class TribefireWebPlatformSpace implements TribefireWebPlatformContract {

	// ALPHABETICAL ORDER!!!

	// @formatter:off
	@Import private BindersSpace binders;
	@Import private CortexAccessSpace cortexAccess;	
	@Import private ClusterSpace cluster;
	@Import private CryptoSpace crypto;
	@Import private DeploymentSpace deployment;
	@Import private GmSessionsSpace gmSessions;
	@Import private HttpSpace http;
	@Import private WebPlatformMarshallingSpace marshalling;
	@Import private MasterUserAuthContextSpace masterUserAuthContext;
	@Import private MessagingSpace messaging;
	@Import private WebPlatformReflectionSpace platformReflection;
	@Import private RequestProcessingSpace requestProcessing;
	@Import private ResourceProcessingSpace resourceProcessing;
	@Import private MasterResourcesSpace resources;
	@Import private RequestUserRelatedSpace requestUserRelated;
	@Import private RpcSpace rpc;
	@Import private SecurityServletSpace securityServlet;
	@Import private SecuritySpace security;
	@Import private ServletsSpace servlets;
	@Import private SystemAccessCommonsSpace systemAccessCommons;
	@Import private SystemServletsSpace systemServlets;
	@Import private SystemUserRelatedSpace systemUserRelated;
	@Import private SystemTasksSpace systemTasks;
	@Import private ThreadingSpace threading;
	@Import private TopologySpace topology;
	@Import private TribefireConnectionsSpace tribefireConnections;
	@Import private WebDeploymentSpace webDeployment;
	@Import private WebRegistrySpace webRegistry;
	@Import private WorkerSpace worker;
	// @formatter:on

	@Override
	@Managed
	public WebPlatformHardwiredRegistry hardwiredDeployables() {
		WebPlatformHardwiredRegistry bean = new WebPlatformHardwiredRegistry();
		bean.webRegistry = new WebRegistryConfigurationImpl(webRegistry);
		bean.actualBinder = deployment.hardwiredBindings();
		bean.requestSessionFactory = gmSessions.sessionFactory();
		bean.systemSessionFactory = gmSessions.systemSessionFactory();
		bean.components = binders;
		bean.sharedStorageSupplier = systemAccessCommons.sharedStorageSupplier();

		return bean;
	}

	@Override
	@Managed
	public WebPlatformHardwiredExpertsRegistry hardwiredExperts() {
		WebPlatformHardwiredExpertsRegistry bean = new WebPlatformHardwiredExpertsRegistry();
		bean.setPushHandlerAdder(rpc.pushProcessor()::addHandler);
		bean.setMdPerspectiveRegistry(gmSessions.mdPerspectiveRegistry());
		bean.setDenotationTransformerRegistry(cortexAccess.transformerRegistry());
		bean.setHomeServlet(systemServlets.homeServlet());
		bean.addAuthFilter(securityServlet.authFilterAdminStrict());
		bean.addAuthFilter(securityServlet.authFilterLenient());
		bean.addAuthFilter(securityServlet.authFilterStrict());

		return bean;
	}

	@Override
	@Managed
	public PlatformModelApiSpace modelApi() {
		return new PlatformModelApiSpace(hardwiredExperts());
	}

	// @formatter:off
	@Override public WebPlatformBindersContract binders() { return binders; }
	@Override public DeploymentContract deployment() { return webDeployment; }
	@Override public ClusterContract cluster() { return cluster; }
	@Override public CryptoContract crypto() { return crypto; }
	@Override public HttpContract http() { return http; }
	@Override public WebPlatformMarshallingContract marshalling() { return marshalling; }
	@Override public MasterUserAuthContextContract masterUserAuthContext() { return masterUserAuthContext; }
	@Override public MessagingSpace messaging() { return messaging; }
	@Override public WebPlatformReflectionContract platformReflection() { return platformReflection; }
	@Override public RequestProcessingContract requestProcessing() { return requestProcessing; }
	@Override public RequestUserRelatedContract requestUserRelated() { return requestUserRelated; }
	@Override public ResourceProcessingContract resourceProcessing() { return resourceProcessing; }
	@Override public WebPlatformResourcesContract resources() { return resources; }
	@Override public SecurityContract security() { return security; }
	@Override public ServletsContract servlets() { return servlets; }
	@Override public SystemToolsContract systemTools() { return systemTasks; }
	@Override public SystemUserRelatedContract systemUserRelated() { return systemUserRelated; }
	@Override public ThreadingContract threading() { return threading; }
	@Override public TopologyContract topology() { return topology; }
	@Override public TribefireConnectionsContract tribefireConnections() { return tribefireConnections; }
	@Override public WorkerContract worker() { return worker; }
	// @formatter:on
}
