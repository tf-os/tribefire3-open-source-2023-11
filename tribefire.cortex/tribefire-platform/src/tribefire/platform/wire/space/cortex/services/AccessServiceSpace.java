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
package tribefire.platform.wire.space.cortex.services;

import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_INFO;
import static com.braintribe.model.processing.bootstrapping.TribefireRuntime.ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_WARNING;
import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.model.access.AccessService;
import com.braintribe.model.processing.access.service.impl.standard.AccessServiceImpl;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.request.InternalAccessService;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.SystemAccessesSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.system.LicenseSpace;

@Managed
public class AccessServiceSpace implements WireSpace {

	private static final String serviceId = "ACCESS";

	// @formatter:off
	@Import private AuthContextSpace authContext; 
	@Import private CortexAccessSpace cortex; 	
	@Import private DeploymentSpace deployment; 
	@Import private EnvironmentSpace environment; 
	@Import private GmSessionsSpace gmSessions; 
	@Import private LicenseSpace license; 
	@Import private SystemAccessesSpace systemAccesses; 
	// @formatter:on

	public String serviceId() {
		return serviceId;
	}

	@Managed
	public AccessServiceImpl service() {
		AccessServiceImpl bean = new AccessServiceImpl();
		bean.setLicenseManager(license.manager());
		bean.setUserRolesProvider(authContext.currentUser().rolesProvider());
		bean.setSystemModelAccessoryFactory(gmSessions.systemModelAccessoryFactory());
		bean.setUserModelAccessoryFactory(gmSessions.userModelAccessoryFactory());
		bean.setTrustedRoles(set("tf-internal"));
		bean.setQueryExecutionInfoThreshold(environment.property(ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_INFO, Long.class, 10000L));
		bean.setQueryExecutionWarningThreshold(environment.property(ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_WARNING, Long.class, 30000L));
		bean.setInternalCortexSessionSupplier(cortex::lowLevelSession);
		return bean;
	}

	@Managed 
	public AccessService internalService() {
		InternalAccessService bean = new InternalAccessService();
		bean.setDelegate(service());
		bean.setInternalSessionProvider(authContext.internalUser().userSessionProvider());
		bean.setCurrentUserSessionStack(authContext.currentUser().userSessionStack());
		
		return bean;
	}

}
