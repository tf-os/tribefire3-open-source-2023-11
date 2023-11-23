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
package com.braintribe.model.processing.securityservice.usersession.cleanup;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.gm.model.user_session_service.CleanupUserSessions;
import com.braintribe.gm.model.user_session_service.CleanupUserSessionsResponse;
import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.building.EntityQueries;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.time.TimeSpanCodec;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.utils.lcd.StopWatch;

public class AccessUserSessionCleanupServiceProcessor implements ServiceProcessor<CleanupUserSessions, CleanupUserSessionsResponse> {

	private Supplier<? extends PersistenceGmSession> persistenceUserSessionGmSessionProvider;
	private TimeSpan userSessionCleanupInterrupt = millisToTimeSpan(60000);
	private Integer userSessionCleanupFetchLimit = 1000;

	private static final Codec<TimeSpan, Double> timeSpanCodec = new TimeSpanCodec();

	private static final Logger log = Logger.getLogger(AccessUserSessionCleanupServiceProcessor.class);

	@Required
	@Configurable
	public void setPersistenceUserSessionGmSessionProvider(Supplier<? extends PersistenceGmSession> persistenceUserSessionGmSessionProvider) {
		this.persistenceUserSessionGmSessionProvider = persistenceUserSessionGmSessionProvider;
	}

	/**
	 * @param userSessionCleanupInterrupt
	 *            Defaults to 60000 ms (1 minute). Null means no interrupt.
	 */
	@Configurable
	public void setUserSessionCleanupInterrupt(TimeSpan userSessionCleanupInterrupt) {
		this.userSessionCleanupInterrupt = userSessionCleanupInterrupt;
	}

	/**
	 * @param userSessionCleanupFetchLimit
	 *            Defaults to 1000. Null means no limit.
	 */
	@Configurable
	public void setUserSessionCleanupFetchLimit(Integer userSessionCleanupFetchLimit) {
		this.userSessionCleanupFetchLimit = userSessionCleanupFetchLimit;
	}

	@Override
	public CleanupUserSessionsResponse process(ServiceRequestContext requestContext, CleanupUserSessions request) {
		PersistenceGmSession gmSession = getPersistenceUserSessionGmSession();

		Long interruptInMillis = timeSpanToMillis(userSessionCleanupInterrupt);

		int countOfSessionsDeleted = 0;
		StopWatch stopWatch = new StopWatch();
		while (true) {
			List<PersistenceUserSession> pUserSessions;
			try {
				pUserSessions = gmSession.query().entities(new SessionCleanupEntityQueries().deletableSessions()).list();
			} catch (Exception e) {
				log.error(() -> "Failed querying invalid user sessions to do the cleanup", e);
				break;
			}
			if (pUserSessions == null || pUserSessions.isEmpty()) {
				break;
			}
			for (PersistenceUserSession pUserSession : pUserSessions) {
				gmSession.deleteEntity(pUserSession);
			}
			try {
				if (gmSession.getTransaction().hasManipulations()) {
					gmSession.commit();
					countOfSessionsDeleted += pUserSessions.size();
				}
			} catch (Exception e) {
				log.error(() -> "Failed to commit deletion of " + pUserSessions.size() + " invalid user sessions", e);
			}
			if ((interruptInMillis != null && stopWatch.getElapsedTime() > interruptInMillis) || userSessionCleanupFetchLimit == null
					|| pUserSessions.size() < userSessionCleanupFetchLimit) {
				break;
			}
		}

		if (log.isDebugEnabled()) {
			if (countOfSessionsDeleted > 0) {
				log.debug("Cleanup of " + countOfSessionsDeleted + " user session(s) concluded in " + stopWatch.getElapsedTime()
						+ " ms, which amounts to " + ((double) stopWatch.getElapsedTime() / countOfSessionsDeleted) + " ms per session");
			} else {
				log.debug("Cleanup of user sessions concluded; No invalid user sessions found");
			}
		}
		return CleanupUserSessionsResponse.T.create();
	}

	private PersistenceGmSession getPersistenceUserSessionGmSession() throws SecurityServiceException {
		PersistenceGmSession persistenceGmSession;
		try {
			persistenceGmSession = persistenceUserSessionGmSessionProvider.get();
		} catch (Exception e) {
			throw new RuntimeException("Failed to obtain a gm session from the provider", e);
		}
		if (persistenceGmSession == null) {
			throw new RuntimeException("null gm session returned from the provider");
		}
		return persistenceGmSession;
	}

	private class SessionCleanupEntityQueries extends EntityQueries {
		EntityQuery deletableSessions() {

			Date now = new Date();

			Condition condition = or( //
					and( //
							eq(property(PersistenceUserSession.blocksAuthenticationAfterLogout), false),
							lt(property(PersistenceUserSession.expiryDate), now), //
							ne(property(PersistenceUserSession.expiryDate), null) //
					), //
					and( //
							eq(property(PersistenceUserSession.blocksAuthenticationAfterLogout), true), //
							lt(property(PersistenceUserSession.fixedExpiryDate), now), //
							ne(property(PersistenceUserSession.fixedExpiryDate), null) //
					) //
			);

			EntityQuery query = from(PersistenceUserSession.T).where(condition);

			if (userSessionCleanupFetchLimit != null)
				query.limit(userSessionCleanupFetchLimit);

			return query;
		}

	}

	private Long timeSpanToMillis(TimeSpan timeSpan) {
		if (timeSpan == null) {
			return null;
		}
		try {
			return timeSpanCodec.encode(timeSpan).longValue();
		} catch (Exception e) {
			throw new RuntimeException("Unable to convert '" + timeSpan + "' to milliseconds", e);
		}
	}

	private TimeSpan millisToTimeSpan(long millis) {
		TimeSpan maxIdleTime = TimeSpan.T.create();
		maxIdleTime.setUnit(TimeUnit.milliSecond);
		maxIdleTime.setValue(millis);
		return maxIdleTime;
	}
}
