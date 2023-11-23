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
package com.braintribe.model.processing.securityservice.api;

import java.util.Date;
import java.util.Map;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;

/**
 * <p>
 * A service used to manipulate {@link UserSession user sessions};
 * 
 */
public interface UserSessionService {

	/**
	 * <p>
	 * Creates a {@link UserSession} for the {@link User} given by {@code user}.
	 * 
	 * @param user
	 *            {@link User} to have a {@link UserSession} created for.
	 * @param type
	 *            {@link UserSessionType}.
	 * @param maxIdleTime
	 *            {@link TimeSpan} to be used as the created session's max idle time (
	 *            {@link UserSession#getMaxIdleTime()}).
	 * @param maxAge
	 *            If provided, this {@link TimeSpan} is used to calculate the user session's fixed expiry date (
	 *            {@link UserSession#getFixedExpiryDate()}). Ignored if {@code fixedExpiryDate} parameter is provided.
	 * @param fixedExpiryDate
	 *            If provided, determines the user session's fixed expiry date ({@link UserSession#getFixedExpiryDate()})
	 * @param internetAddress
	 *            Internet address of the caller for whom the session is being created, to be accessible through
	 *            {@link UserSession#getCreationInternetAddress()}
	 * @param properties
	 *            If provided, determines the user session's properties ({@link UserSession#getProperties()})
	 * @param acquirationKey
	 *            A key to access the session alternatively by
	 * @return {@link UserSession} object for the {@link User} given by the {@code user} parameter.
	 */
	Maybe<UserSession> createUserSession(User user, UserSessionType type, TimeSpan maxIdleTime, TimeSpan maxAge, Date fixedExpiryDate,
			String internetAddress, Map<String, String> properties, String acquirationKey, boolean blocksAuthenticationAfterLogout);

	/**
	 * <p>
	 * Fetches a {@link UserSession} object matching the given {@code sessionId} in the underlying persistence layer.
	 * 
	 * @param sessionId
	 *            session id of the {@link UserSession} to be fetched (matching {@link UserSession#getSessionId()})
	 * 
	 * @return {@link UserSession} object matching the given {@code sessionId} or null if no {@link UserSession} is found.
	 * 
	 */
	Maybe<UserSession> findUserSession(String sessionId);

	/**
	 * <p>
	 * Fetches a {@link UserSession} object matching the given {@code sessionId} in the underlying persistence layer,
	 * updates it's {@code lastAccessedDate} to the current and recalculates the {@code idleExpiryDate}.
	 * 
	 * @param acquirationKey
	 *            acquirationKey of the {@link UserSession} to be fetched
	 * 
	 * @return {@link UserSession} object matching the given {@code sessionId} or null if no {@link UserSession} is found.
	 * 
	 */
	Maybe<UserSession> findUserSessionByAcquirationKey(String acquirationKey);

	/**
	 * <p>
	 * Finds a {@link UserSession} object matching the given {@code sessionId} in the underlying persistence layer, updates
	 * it's {@code lastAccessedDate} to the current and recalculates the {@code idleExpiryDate}.
	 * 
	 * @param sessionId
	 *            session id of the {@link UserSession} to be fetched (matching {@link UserSession#getSessionId()})
	 * 
	 */
	void touchUserSession(String sessionId, Date lastAccessDate, Date expiryDate);

	/**
	 * <p>
	 * Deletes the {@link UserSession} object matching the given {@code sessionId} in the underlying persistence layer.
	 * 
	 * @param sessionId
	 *            session id of the {@link UserSession} to be deleted.
	 */
	Maybe<DeletedSessionInfo> deleteUserSession(String sessionId);
}
