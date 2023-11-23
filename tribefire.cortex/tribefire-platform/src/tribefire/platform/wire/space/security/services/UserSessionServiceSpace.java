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

import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.user_session_service.CleanupUserSessions;
import com.braintribe.gm.model.user_session_service.CleanupUserSessionsResponse;
import com.braintribe.gm.model.user_session_service.UserSessionRequest;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.SchrodingerBean;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.processing.securityservice.usersession.UserSessionServiceProcessor;
import com.braintribe.model.processing.securityservice.usersession.cleanup.AccessUserSessionCleanupServiceProcessor;
import com.braintribe.model.processing.securityservice.usersession.cleanup.JdbcUserSessionCleanupServiceProcessor;
import com.braintribe.model.processing.securityservice.usersession.cleanup.UserSessionCleanupWorker;
import com.braintribe.model.processing.securityservice.usersession.service.AccessUserSessionService;
import com.braintribe.model.processing.securityservice.usersession.service.JdbcUserSessionService;
import com.braintribe.model.processing.securityservice.usersession.service.UserSessionIdProvider;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.time.TimeSpanCodec;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.model.deployment.usersession.cleanup.JdbcCleanupUserSessionsProcessor;
import tribefire.platform.wire.space.SchrodingerBeansSpace;
import tribefire.platform.wire.space.common.BindersSpace;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.security.accesses.UserSessionsAccessSpace;
import tribefire.platform.wire.space.security.accesses.UserStatisticsAccessSpace;

@Managed
public class UserSessionServiceSpace implements WireSpace {

	public static final String CLEANUP_USER_SESSIONS_PROCESSOR_ID = "service:cleanup-user-sessions";
	public static final String CLEANUP_USER_SESSIONS_PROCESSOR_NAME = "Cleanup User-Session Processor";
	public static final String USER_SESSION_SERVICE_ID = "service:user-session";
	public static final String USER_SESSION_SERVICE_NAME = "User Session Service";

	@Import
	private BindersSpace binders;

	@Import
	private EnvironmentSpace environment;

	@Import
	private UserSessionsAccessSpace userSessionAccess;

	@Import
	private UserStatisticsAccessSpace userStatisticsAccess;

	@Import
	private CartridgeInformationSpace cartridgeInformation;

	@Import
	private AuthContextSpace authContext;

	@Import
	private WorkerSpace worker;

	@Import
	private RpcSpace rpc;

	@Import
	private SchrodingerBeansSpace schrodingerBeans;

	// TODO can this be done at the end, when all spaces are loaded?
	// This triggers module/cortex loading very early...
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		worker.manager().deploy(cleanupWorker());
	}

	@Managed
	public UserSessionCleanupWorker cleanupWorker() {
		UserSessionCleanupWorker bean = new UserSessionCleanupWorker();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		String enabledString = TribefireRuntime.getProperty("TRIBEFIRE_USERSESSION_CLEANUP_ENABLED", "true");
		boolean enabled = true;
		if (!StringTools.isBlank(enabledString)) {
			enabled = enabledString.equalsIgnoreCase("true");
		}
		bean.setEnableUserSessionCleanup(enabled);
		return bean;
	}

	public ServiceProcessor<CleanupUserSessions, CleanupUserSessionsResponse> cleanupService() {
		return cleanupServiceSchrodingerBean().proxy();
	}

	@Managed
	public SchrodingerBean<? extends ServiceProcessor<CleanupUserSessions, CleanupUserSessionsResponse>> cleanupServiceSchrodingerBean() {
		SchrodingerBean<ServiceProcessor<?, ?>> bean = schrodingerBeans.newBean( //
				"CleanupUserSessionsProcessor", CortexConfiguration::getCleanupUserSessionsProcessor, binders.serviceProcessor());
		// Intermediate cast needed in Java 8, not 17
		return (SchrodingerBean<? extends ServiceProcessor<CleanupUserSessions, CleanupUserSessionsResponse>>) (SchrodingerBean<?>) bean;
	}

	public UserSessionService service() {
		return serviceSchrodingerBean().proxy();
	}

	@Managed
	public SchrodingerBean<UserSessionService> serviceSchrodingerBean() {
		return schrodingerBeans.newBean("UserSessionService", CortexConfiguration::getUserSessionService, binders.userSessionService());
	}

	@Managed
	public UserSessionService accessSessionService() {
		AccessUserSessionService bean = new AccessUserSessionService();

		// general
		bean.setPersistenceUserSessionGmSessionProvider(userSessionAccess::lowLevelSession);
		bean.setSessionIdProvider(userSessionIdFactory());
		bean.setNodeId(cartridgeInformation.nodeId());
		bean.setDefaultUserSessionMaxIdleTime(defaultMaxIdleTime());

		// internal user sessions
		// @formatter:off
		bean.setInternalUserSessionHolders(
				list(
					authContext.internalUser().userSessionProvider(),
					authContext.masterUser().userSessionProvider()
				)
		);
		// @formatter:on

		return bean;
	}

	@Managed
	public UserSessionService jdbcService(ExpertContext<tribefire.cortex.model.deployment.usersession.service.JdbcUserSessionService> context) {
		tribefire.cortex.model.deployment.usersession.service.JdbcUserSessionService deployable = context.getDeployable();
		DataSource dataSource = context.resolve(deployable.getConnectionPool(), DatabaseConnectionPool.T);

		JdbcUserSessionService bean = new JdbcUserSessionService();

		// general
		bean.setDataSource(dataSource);
		bean.setSessionIdProvider(userSessionIdFactory());
		bean.setNodeId(cartridgeInformation.nodeId());
		bean.setDefaultUserSessionMaxIdleTime(defaultMaxIdleTime());

		// internal user sessions
		bean.setInternalUserSessionHolders(list( //
				authContext.internalUser().userSessionProvider(), //
				authContext.masterUser().userSessionProvider() //
		));

		return bean;
	}

	@Managed
	public AccessUserSessionCleanupServiceProcessor accessSessionCleanupService() {
		AccessUserSessionCleanupServiceProcessor bean = new AccessUserSessionCleanupServiceProcessor();
		bean.setPersistenceUserSessionGmSessionProvider(userSessionAccess::lowLevelSession);

		return bean;
	}

	@Managed
	public JdbcUserSessionCleanupServiceProcessor jdbcCleanupService(ExpertContext<JdbcCleanupUserSessionsProcessor> context) {
		JdbcCleanupUserSessionsProcessor deployable = context.getDeployable();
		DataSource dataSource = context.resolve(deployable.getConnectionPool(), DatabaseConnectionPool.T);

		JdbcUserSessionCleanupServiceProcessor bean = new JdbcUserSessionCleanupServiceProcessor();
		bean.setDataSource(dataSource);

		return bean;
	}

	@Managed
	public ServiceProcessor<UserSessionRequest, Object> wbService() {
		UserSessionServiceProcessor bean = new UserSessionServiceProcessor();
		return bean;
	}

	@Managed
	public UserSessionIdProvider userSessionIdFactory() {
		// @formatter:off
		UserSessionIdProvider bean = new UserSessionIdProvider();
		bean.setTypePrefixes(
				map(
					entry(UserSessionType.internal, "i-"),
					entry(UserSessionType.trusted, "t-")
				)
			);
		return bean;
		// @formatter:on
	}

	@Managed
	public TimeSpanCodec timeSpanCodec() {
		TimeSpanCodec bean = new TimeSpanCodec();
		return bean;
	}

	@Managed
	public TimeSpan defaultMaxIdleTime() {
		TimeSpan bean = environment.property(TribefireRuntime.ENVIRONMENT_USER_SESSIONS_MAX_IDLE_TIME, TimeSpan.class, standardMaxIdleTime());
		return bean;
	}

	@Managed
	private TimeSpan standardMaxIdleTime() {
		TimeSpan bean = TimeSpan.T.create();
		bean.setUnit(TimeUnit.hour);
		bean.setValue(24.0);
		return bean;
	}

	public UserSession internalUserSession(User user) {
		UserSession bean = UserSession.T.create();

		Set<String> effectiveRoles = new HashSet<>();
		effectiveRoles.add("$all");
		effectiveRoles.add("$user-" + user.getName());
		for (Role userRole : user.getRoles())
			effectiveRoles.add(userRole.getName());

		Date now = new Date();

		bean.setSessionId(newInternalUserSessionId());
		bean.setType(UserSessionType.internal);
		bean.setCreationInternetAddress("0:0:0:0:0:0:0:1");
		bean.setCreationDate(now);
		bean.setLastAccessedDate(now);
		bean.setUser(user);
		bean.setEffectiveRoles(effectiveRoles);

		return bean;
	}

	private String newInternalUserSessionId() {
		try {
			return userSessionIdFactory().apply(UserSessionType.internal);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to generate an user session id");
		}
	}

}
