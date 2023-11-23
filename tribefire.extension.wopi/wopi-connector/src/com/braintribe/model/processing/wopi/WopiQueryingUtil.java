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
package com.braintribe.model.processing.wopi;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.JunctionBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.user.Role;
import com.braintribe.model.wopi.WopiAccessToken;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.WopiStatus;

/**
 * Utility methods holding all queries related to WOPI
 * 
 *
 */
public class WopiQueryingUtil {

	private static final Logger logger = Logger.getLogger(WopiQueryingUtil.class);

	/**
	 * Query a {@link WopiSession} and returns it - no matter in which {@link WopiSession#getStatus()} it is. If it does
	 * not exists null will be returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession} or null if it does not exists
	 */
	public static WopiSession queryWopiSession(PersistenceGmSession session, String correlationId) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
					.where()
						.conjunction()
							.property(WopiSession.correlationId).eq(correlationId);
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().done();

		WopiSession wopiSession = session.query().entities(query).first();
		return wopiSession;
	}

	/**
	 * Query multiple {@link WopiSession}s and returns them - no matter in which {@link WopiSession#getStatus()} it is.
	 * If it does not exists an empty {@link List} will be returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationIds
	 *            correlationIds of the {@link WopiSession}s
	 * @return {@link List} of {@link WopiSession}
	 */
	public static List<WopiSession> queryWopiSessions(PersistenceGmSession session, Set<String> correlationIds) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
					.where()
						.conjunction()
							.property(WopiSession.correlationId).in(correlationIds);
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().done();

		List<WopiSession> wopiSessions = session.query().entities(query).list();
		return wopiSessions;
	}

	/**
	 * Query a {@link WopiSession} that is in {@link WopiStatus#open} and returns it. If it does not exists null will be
	 * returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession} or null if it does not exists
	 */
	public static WopiSession queryOpenWopiSession(PersistenceGmSession session, String correlationId) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
					.where()
						.conjunction()
							.property(WopiSession.correlationId).eq(correlationId)
							.property(WopiSession.status).eq(WopiStatus.open);
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().done();

		WopiSession wopiSession = session.query().entities(query).first();
		return wopiSession;
	}

	/**
	 * Query a {@link WopiSession} that is in {@link WopiStatus#open} with {@link WopiSession#sourceReference} and
	 * returns the newest. If it does not exists null will be returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param sourceReference
	 *            sourceReference of the {@link WopiSession}
	 * @return {@link WopiSession} or null if it does not exists
	 */
	public static WopiSession queryOpenWopiSessionBySourceReference(PersistenceGmSession session, String sourceReference) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
					.where()
						.conjunction()
							.property(WopiSession.sourceReference).eq(sourceReference)
							.property(WopiSession.status).eq(WopiStatus.open);
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().orderBy(OrderingDirection.descending).property(WopiSession.creationDate).done();

		WopiSession wopiSession = session.query().entities(query).first();
		return wopiSession;
	}

	/**
	 * Query a {@link WopiSession} that is already {@link WopiStatus#closed} or {@link WopiStatus#expired}. If it does
	 * not exists null will be returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession} or null if it does not exists
	 */
	public static WopiSession queryExpiredOrClosedWopiSession(PersistenceGmSession session, String correlationId) {

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
					.conjunction()
						.property(sessionAlias, WopiSession.correlationId).eq(correlationId)
						.disjunction()
							.property(WopiSession.status).eq(WopiStatus.closed)
							.property(WopiSession.status).eq(WopiStatus.expired)
						.close();
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().done();

		WopiSession wopiSession = session.query().select(query).first();
		return wopiSession;
	}

	/**
	 * Query a {@link WopiSession} that is {@link WopiStatus#open} or {@link WopiStatus#expired}. If it does not exists
	 * null will be returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession} or null if it does not exists
	 */
	public static WopiSession queryOpenOrExpiredWopiSession(PersistenceGmSession session, String correlationId) {

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
					.conjunction()
						.property(sessionAlias, WopiSession.correlationId).eq(correlationId)
						.disjunction()
							.property(WopiSession.status).eq(WopiStatus.open)
							.property(WopiSession.status).eq(WopiStatus.expired)
						.close();
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().done();

		WopiSession wopiSession = session.query().select(query).first();
		return wopiSession;
	}

	/**
	 * Query a {@link WopiSession} that is {@link WopiStatus#open} or {@link WopiStatus#closed}. If it does not exists
	 * null will be returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession} or null if it does not exists
	 */
	public static WopiSession queryOpenOrClosedWopiSession(PersistenceGmSession session, String correlationId) {

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
					.conjunction()
						.property(sessionAlias, WopiSession.correlationId).eq(correlationId)
						.disjunction()
							.property(WopiSession.status).eq(WopiStatus.open)
							.property(WopiSession.status).eq(WopiStatus.closed)
						.close();
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().done();

		WopiSession wopiSession = session.query().select(query).first();
		return wopiSession;
	}

	/**
	 * Query a {@link WopiSession} that is {@link WopiStatus#open} or {@link WopiStatus#expired} and returns it. If it
	 * does not exists null will be returned
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession} or null if it does not exists
	 */
	public static WopiSession queryNotClosedWopiSession(PersistenceGmSession session, String correlationId) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
					.where()
						.conjunction()
							.property(WopiSession.correlationId).eq(correlationId)
							.disjunction()
								.property(WopiSession.status).eq(WopiStatus.open)
								.property(WopiSession.status).eq(WopiStatus.expired)
							.close();
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().done();

		WopiSession wopiSession = session.query().entities(query).first();

		return wopiSession;
	}

	/**
	 * Query a {@link WopiSession} and returns it. If it does not exists an exception will be thrown
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession}
	 */

	public static WopiSession queryExistingWopiSession(PersistenceGmSession session, String correlationId) {
		WopiSession wopiSession = queryWopiSession(session, correlationId);
		if (wopiSession == null) {
			throw new IllegalStateException("Could not find '" + WopiSession.T.getTypeName() + "' with correlationId: '" + correlationId + "'");
		}
		return wopiSession;
	}

	/**
	 * Query a {@link WopiSession} that is in {@link WopiStatus#open} and returns it. If it does not exists an exception
	 * will be thrown
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param correlationId
	 *            correlationId of the {@link WopiSession}
	 * @return {@link WopiSession}
	 */

	public static WopiSession queryOpenExistingWopiSession(PersistenceGmSession session, String correlationId) {
		WopiSession wopiSession = queryOpenWopiSession(session, correlationId);
		if (wopiSession == null) {
			throw new IllegalStateException("Could not find '" + WopiSession.T.getTypeName() + "' with correlationId: '" + correlationId + "'");
		}
		return wopiSession;
	}

	/**
	 * Query all {@link WopiSession}s which are {@link WopiStatus#open} or {@link WopiStatus#expired} - return 100
	 * results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryOpenOrExpiredWopiSessions(PersistenceGmSession session) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
					.where()
						.conjunction()
							.disjunction()
								.property(WopiSession.status).eq(WopiStatus.open)
								.property(WopiSession.status).eq(WopiStatus.expired)
							.close();
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().entities(query).list();

		return wopiSessions;
	}

	/**
	 * Query all {@link WopiSession}s - no matter in which {@link WopiSession#getStatus()} they are - returns 100
	 * results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryWopiSessions(PersistenceGmSession session) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
					.where()
						.conjunction();
				//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().entities(query).list();

		return wopiSessions;
	}

	/**
	 * Query all {@link WopiSession}s - based on a context - returns 100 results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryContextBasedWopiSessions(PersistenceGmSession session, String context) {
		//@formatter:off
		JunctionBuilder<EntityQueryBuilder> junctionBuilder = EntityQueryBuilder
				.from(WopiSession.T)
				.where()
					.conjunction()
						.property(WopiSession.context).eq(context);
						//@formatter:on

		junctionBuilder = addPermissionDisjunctionEntity(session, junctionBuilder);

		EntityQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().entities(query).list();

		return wopiSessions;
	}

	/**
	 * Query all expired {@link WopiSession}s - return 100 results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param wopiSessionExpirationInMs
	 *            {@link WopiSession} expiration in milliseconds
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryExpiredWopiSessionsCandidates(PersistenceGmSession session, long wopiSessionExpirationInMs) {
		GregorianCalendar cal = new GregorianCalendar();
		Date threshold = null;
		int maxAgeAsInt = (int) wopiSessionExpirationInMs;
		cal.add(Calendar.MILLISECOND, -maxAgeAsInt);

		threshold = cal.getTime();

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
					.conjunction()
						.property(sessionAlias, WopiSession.creationDate).lt(threshold)
						.property(WopiSession.status).eq(WopiStatus.open);
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().select(query).list();

		return wopiSessions;
	}

	/**
	 * Query all expired {@link WopiSession}s - based on a context - return 100 results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param wopiSessionExpirationInMs
	 *            {@link WopiSession} expiration in milliseconds
	 * @param context
	 *            Context information
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryContextBasedExpiredWopiSessionsCandidates(PersistenceGmSession session, long wopiSessionExpirationInMs,
			String context) {
		GregorianCalendar cal = new GregorianCalendar();
		Date threshold = null;
		int maxAgeAsInt = (int) wopiSessionExpirationInMs;
		cal.add(Calendar.MILLISECOND, -maxAgeAsInt);

		threshold = cal.getTime();

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
				.conjunction()
				.property(sessionAlias, WopiSession.creationDate).lt(threshold)
				.property(WopiSession.status).eq(WopiStatus.open)
				.property(WopiSession.context).eq(context);
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().select(query).list();

		return wopiSessions;
	}

	/**
	 * Query all expired {@link WopiSession}s - return 100 results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryExpiredOrClosedWopiSessions(PersistenceGmSession session) {

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
					.disjunction()
						.property(WopiSession.status).eq(WopiStatus.expired)
						.property(WopiSession.status).eq(WopiStatus.closed);
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().select(query).list();

		return wopiSessions;
	}

	/**
	 * Query all expired {@link WopiSession}s - based on a context - return 100 results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param context
	 *            Context information
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryContextBasedExpiredOrClosedWopiSessions(PersistenceGmSession session, String context) {

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
					.conjunction()
						.property(WopiSession.context).eq(context)
						.disjunction()
							.property(WopiSession.status).eq(WopiStatus.expired)
							.property(WopiSession.status).eq(WopiStatus.closed)
						.close();
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().select(query).list();

		return wopiSessions;
	}

	/**
	 * Query all open or expired {@link WopiSession}s - based on a context - return 100 results
	 * 
	 * @param session
	 *            {@link PersistenceGmSession}
	 * @param context
	 *            Context information
	 * @return {@link List} of {@link WopiSession}s
	 */
	public static List<WopiSession> queryContextBasedOpenOrExpiredWopiSessions(PersistenceGmSession session, String context) {

		String sessionAlias = "s";

		//@formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder().from(WopiSession.T, sessionAlias)
				.where()
					.conjunction()
						.property(WopiSession.context).eq(context)
						.disjunction()
							.property(WopiSession.status).eq(WopiStatus.open)
							.property(WopiSession.status).eq(WopiStatus.expired)
						.close();
		//@formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().paging(100, 0).done();

		List<WopiSession> wopiSessions = session.query().select(query).list();

		return wopiSessions;
	}

	/**
	 * Query a {@link WopiSession} with a specific accessToken
	 * 
	 * @param session
	 *            {@link PersistenceGmSession session}
	 * @param correlationId
	 *            correlationId
	 * @param accessToken
	 *            access Token
	 * @return {@link WopiSession}
	 */
	public static WopiSession queryWopiSessionWithAccessToken(PersistenceGmSession session, String correlationId, String accessToken) {
		// @formatter:off
		JunctionBuilder<SelectQueryBuilder> junctionBuilder = new SelectQueryBuilder()
				.from(WopiSession.T, "ws")
				.join("ws", WopiSession.accessTokens, "at")
				.where()
					.conjunction()
						.property("ws", WopiSession.correlationId).eq(correlationId)
						.property("at", WopiAccessToken.token).eq(accessToken);
		// @formatter:on

		junctionBuilder = addPermissionDisjunctionSelect(session, junctionBuilder);

		SelectQuery query = junctionBuilder.close().select("ws").done();

		WopiSession wopiSession = session.query().select(query).first();

		logger.debug(() -> "Got wopiSession: '" + wopiSession + "' for correlationId: '" + correlationId + "' accessToken: '" + accessToken + "'");

		return wopiSession;
	}

	/**
	 * Query all {@link Role}s
	 * 
	 * @param authSession
	 *            {@link PersistenceGmSession}
	 * @return {@link List} of {@link Role}
	 */
	public static List<Role> queryRoles(PersistenceGmSession authSession) {
		// TODO: hope we are not having too much roles - this needs to be improved
		String roleAlias = "r";

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(Role.T, roleAlias)
				.done();
		//@formatter:on

		List<Role> roles = authSession.query().select(query).list();
		return roles;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static JunctionBuilder<EntityQueryBuilder> addPermissionDisjunctionEntity(PersistenceGmSession session,
			JunctionBuilder<EntityQueryBuilder> junctionBuilder) {
		JunctionBuilder<JunctionBuilder<EntityQueryBuilder>> disjunction = junctionBuilder.disjunction();

		for (String role : checkAuthorization(session)) {
			disjunction.property(WopiSession.allowedRoles).contains().value(role);
		}
		junctionBuilder = disjunction.close(); // disjunction
		return junctionBuilder;
	}

	private static JunctionBuilder<SelectQueryBuilder> addPermissionDisjunctionSelect(PersistenceGmSession session,
			JunctionBuilder<SelectQueryBuilder> junctionBuilder) {
		JunctionBuilder<JunctionBuilder<SelectQueryBuilder>> disjunction = junctionBuilder.disjunction();

		for (String role : checkAuthorization(session)) {
			disjunction.property(WopiSession.allowedRoles).contains().value(role);
		}
		junctionBuilder = disjunction.close(); // disjunction
		return junctionBuilder;
	}

	private static Set<String> checkAuthorization(PersistenceGmSession session) {

		Objects.requireNonNull(session, "Session must not be null");

		SessionAuthorization sessionAuthorization = session.getSessionAuthorization();

		if (sessionAuthorization == null) {
			throw new IllegalStateException("Session needs to be authorized");
		}

		Set<String> userRoles = sessionAuthorization.getUserRoles();
		return userRoles;
	}

}