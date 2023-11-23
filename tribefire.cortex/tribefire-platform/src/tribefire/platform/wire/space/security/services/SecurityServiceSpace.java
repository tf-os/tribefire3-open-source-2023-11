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
package tribefire.platform.wire.space.security.services;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.securityservice.basic.MetaDataDispatchingAuthenticator;
import com.braintribe.model.processing.securityservice.basic.SecurityServiceProcessor;
import com.braintribe.model.processing.securityservice.basic.verification.SameOriginUserSessionVerification;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.web.servlet.auth.WebLogoutInterceptor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.space.bindings.BindingsSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.common.HttpSpace;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.security.AuthenticatorsSpace;
import tribefire.platform.wire.space.security.accesses.AuthAccessSpace;
import tribefire.platform.wire.space.security.accesses.UserStatisticsAccessSpace;

@Managed
public class SecurityServiceSpace implements WireSpace {

	private static final String serviceId = "SECURITY";

	@Import
	private AuthenticatorsSpace authExperts;

	@Import
	private AuthContextSpace authContext;

	@Import
	private UserSessionServiceSpace userSessionService;

	@Import
	private UserStatisticsAccessSpace userStatisticsAccess;

	@Import
	private EnvironmentSpace environment;

	@Import
	private RpcSpace rpc;

	@Import
	protected HttpSpace http;

	@Import
	private MarshallingSpace marshalling;

	@Import
	private CortexAccessSpace cortexAccess;

	@Import
	private BindingsSpace bindings;

	@Import
	private ResourceProcessingSpace resourceProcessing;

	@Import
	protected GmSessionsSpace gmSessions;

	@Import
	private AuthAccessSpace authAccess;

	public String serviceId() {
		return serviceId;
	}

	@Managed
	public WebLogoutInterceptor webLogoutInterceptor() {
		WebLogoutInterceptor bean = new WebLogoutInterceptor();
		bean.setCookieHandler(http.cookieHandler());
		return bean;
	}

	@Managed
	public MetaDataDispatchingAuthenticator authenticator() {
		MetaDataDispatchingAuthenticator bean = new MetaDataDispatchingAuthenticator();
		bean.setCortexModelAccessorySupplier(() -> gmSessions.userModelAccessoryFactory().getForServiceDomain("cortex"));
		return bean;
	}

	@Managed
	public ServiceProcessor<SecurityRequest, Object> securityServiceProcessor() {
		SecurityServiceProcessor bean = new SecurityServiceProcessor();
		bean.setUserSessionService(userSessionService.service());
		bean.setEvaluator(rpc.serviceRequestEvaluator());
		bean.setUserSessionAccessVerificationExperts(asList(sameOriginUserSessionVerification()));
		bean.setSessionMaxIdleTime(userSessionService.defaultMaxIdleTime());
		bean.setAuthGmSessionProvider(authAccess::lowLevelSession);
		// user statistics
		bean.setEnableUserStatistics(statisticsEnabled());
		bean.setUserStatisticsGmSessionProvider(userStatisticsAccess::lowLevelSession);
		return bean;
	}

	private Boolean statisticsEnabled() {
		return environment.property(TribefireRuntime.ENVIRONMENT_USER_SESSIONS_STATISTICS_ENABLED, Boolean.class, Boolean.TRUE);
	}

	@Managed
	private SameOriginUserSessionVerification sameOriginUserSessionVerification() {
		SameOriginUserSessionVerification bean = new SameOriginUserSessionVerification();
		bean.setAllowAccessToUserSessionsWithNoCreationIp(true);
		bean.setIgnoreSourceIpNullValue(true);
		return bean;
	}

}
