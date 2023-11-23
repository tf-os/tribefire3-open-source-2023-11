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
package com.braintribe.model.processing.securityservice.usersession.service;

import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSessionType;

public class AccessUserSessionService extends AbstractUserSessionService {
	private static final Logger log = Logger.getLogger(AccessUserSessionService.class);

	private Supplier<? extends PersistenceGmSession> persistenceUserSessionGmSessionProvider;

	@Required
	@Configurable
	public void setPersistenceUserSessionGmSessionProvider(Supplier<? extends PersistenceGmSession> persistenceUserSessionGmSessionProvider) {
		this.persistenceUserSessionGmSessionProvider = persistenceUserSessionGmSessionProvider;
	}

	private PersistenceGmSession openPersistenceUserSessionGmSession() {
		PersistenceGmSession persistenceGmSession;
		try {
			persistenceGmSession = persistenceUserSessionGmSessionProvider.get();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to obtain the gm session from the provider");
		}
		if (persistenceGmSession == null) {
			throw new IllegalStateException("null gm session returned from the provider");
		}
		return persistenceGmSession;
	}

	@Override
	protected PersistenceUserSession createPersistenceUserSession(User user, UserSessionType type, TimeSpan maxIdleTime, TimeSpan maxAge,
			Date fixedExpiryDate, String internetAddress, Map<String, String> properties, String acquirationKey,
			boolean blocksAuthenticationAfterLogout) {

		UserSessionType userSessionType = type != null ? type : this.defaultUserSessionType;
		Date now = new Date();

		PersistenceGmSession gmSession = openPersistenceUserSessionGmSession();

		PersistenceUserSession pUserSession = initPersistenceUserSession(gmSession.create(PersistenceUserSession.T), user, maxIdleTime, maxAge,
				fixedExpiryDate, internetAddress, properties, acquirationKey, blocksAuthenticationAfterLogout, userSessionType, now);

		gmSession.commit();

		return pUserSession;
	}

	@Override
	protected Maybe<PersistenceUserSession> findPersistenceUserSession(String sessionId) {
		PersistenceGmSession gmSession = openPersistenceUserSessionGmSession();

		try {
			PersistenceUserSession userSession = gmSession.query().entities(findPersistenceUserSessionBySessionIdQuery(sessionId)).first();

			if (userSession == null)
				return Reasons.build(SessionNotFound.T).text("User session '" + sessionId + "' not found").toMaybe();

			return Maybe.complete(userSession);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to query user session '" + sessionId + "'");
		}
	}

	@Override
	protected Maybe<PersistenceUserSession> findPersistenceUserSessionByAcquirationKey(String acquirationKey) {
		PersistenceGmSession gmSession = openPersistenceUserSessionGmSession();

		try {
			PersistenceUserSession userSession = gmSession.query().entities(findPersistenceUserSessionByAcquirationKeyQuery(acquirationKey)).first();

			if (userSession == null)
				return Reasons.build(SessionNotFound.T).text("User session with acquiration key '" + acquirationKey + "' not found").toMaybe();

			return Maybe.complete(userSession);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to query user session by acquiration key '" + acquirationKey + "'");
		}
	}

	private EntityQuery findPersistenceUserSessionBySessionIdQuery(String sessionId) {
		return EntityQueryBuilder.from(PersistenceUserSession.T).where().property(PersistenceUserSession.id).eq(sessionId).done();
	}

	private EntityQuery findPersistenceUserSessionByAcquirationKeyQuery(String acquirationKey) {
		return EntityQueryBuilder.from(PersistenceUserSession.T).where().property(PersistenceUserSession.acquirationKey).eq(acquirationKey)
				.orderBy(PersistenceUserSession.creationDate, OrderingDirection.descending).limit(1).done();
	}

	@Override
	public void touchUserSession(String sessionId, Date lastAccessDate, Date expiryDate) {
		PersistenceGmSession gmSession = openPersistenceUserSessionGmSession();
		PersistenceUserSession pus = gmSession.query().entity(PersistenceUserSession.T, sessionId).findLocalOrBuildShallow();
		pus.setLastAccessedDate(lastAccessDate);
		if (expiryDate != null)
			pus.setExpiryDate(expiryDate);

		try {
			gmSession.commit();
		} catch (Exception e) {
			log.error("Could not touch PersistenceUserSession with id: " + sessionId);
		}
	}

	@Override
	protected void deletePersistenceUserSession(String sessionId) {
		PersistenceGmSession gmSession = openPersistenceUserSessionGmSession();
		PersistenceUserSession userSession = gmSession.query().entity(PersistenceUserSession.T, sessionId).find();

		if (userSession == null)
			return;

		deletePersistenceUserSession(userSession);
	}

	@Override
	protected void closePersistenceUserSession(String sessionId) {
		PersistenceGmSession gmSession = openPersistenceUserSessionGmSession();
		PersistenceUserSession userSession = gmSession.query().entity(PersistenceUserSession.T, sessionId).find();

		if (userSession == null)
			return;

		closePersistenceUserSession(userSession);
	}

	@Override
	protected void closePersistenceUserSession(PersistenceUserSession userSession) {
		try {
			PersistenceGmSession gmSession = (PersistenceGmSession) userSession.session();
			userSession.setClosed(true);
			userSession.setExpiryDate(new Date());
			gmSession.commit();
		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Could not close session with id '" + userSession.getId() + "'");
		}
	}

	@Override
	protected void deletePersistenceUserSession(PersistenceUserSession userSession) {
		try {
			PersistenceGmSession gmSession = (PersistenceGmSession) userSession.session();

			gmSession.deleteEntity(userSession);
			gmSession.commit();
		} catch (RuntimeException e) {
			throw Exceptions.contextualize(e, "Could not delete session with id '" + userSession.getId() + "'");
		}
	}

}
