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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.string.CommaUrlEscapeCodec;
import com.braintribe.codec.string.ListCodec;
import com.braintribe.codec.string.MapCodec;
import com.braintribe.codec.string.StringCodec;
import com.braintribe.codec.string.UrlEscapeCodec;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.usersession.PersistenceUserSession;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.securityservice.api.DeletedSessionInfo;
import com.braintribe.model.processing.securityservice.api.UserSessionService;
import com.braintribe.model.processing.securityservice.impl.Roles;
import com.braintribe.model.processing.time.TimeSpanCodec;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;
import com.braintribe.provider.Hub;

public abstract class AbstractUserSessionService implements UserSessionService, LifecycleAware {

	static final Logger log = Logger.getLogger(AbstractUserSessionService.class);

	protected Function<UserSessionType, String> sessionIdProvider;
	protected String nodeId;

	protected UserSessionType defaultUserSessionType = UserSessionType.normal;
	protected TimeSpan defaultUserSessionMaxIdleTime;

	protected List<Hub<UserSession>> internalUserSessionHolders;

	protected Codec<List<String>, String> listCodec;
	protected final Codec<TimeSpan, Double> timeSpanCodec = new TimeSpanCodec();
	protected MapCodec<String, String> mapCodec;

	public AbstractUserSessionService() {
		super();
	}

	protected abstract void deletePersistenceUserSession(PersistenceUserSession pUserSession);
	protected abstract void deletePersistenceUserSession(String sessionId);
	protected abstract void closePersistenceUserSession(String sessionId);
	protected abstract void closePersistenceUserSession(PersistenceUserSession userSession);

	protected abstract Maybe<PersistenceUserSession> findPersistenceUserSession(String sessionId);

	protected abstract Maybe<PersistenceUserSession> findPersistenceUserSessionByAcquirationKey(String acquirationKey);

	protected abstract PersistenceUserSession createPersistenceUserSession(User user, UserSessionType type, TimeSpan maxIdleTime, TimeSpan maxAge,
			Date fixedExpiryDate, String internetAddress, Map<String, String> properties, String acquirationKey,
			boolean blocksAuthenticationAfterLogout);

	@Required
	public void setSessionIdProvider(Function<UserSessionType, String> sessionIdProvider) {
		this.sessionIdProvider = sessionIdProvider;
	}

	@Required
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @param defaultUserSessionMaxIdleTime
	 *            Defaults to null - no max idle time limit.
	 */
	@Configurable
	public void setDefaultUserSessionMaxIdleTime(TimeSpan defaultUserSessionMaxIdleTime) {
		this.defaultUserSessionMaxIdleTime = defaultUserSessionMaxIdleTime;
	}

	/**
	 * @param defaultUserSessionType
	 *            Defaults to 'normal'.
	 */
	@Configurable
	public void setDefaultUserSessionType(UserSessionType defaultUserSessionType) {
		this.defaultUserSessionType = defaultUserSessionType;
	}

	@Configurable
	public void setInternalUserSessionHolders(List<Hub<UserSession>> internalUserSessionHolders) {
		this.internalUserSessionHolders = internalUserSessionHolders;
	}

	@Override
	public void postConstruct() {
		mapCodec = new MapCodec<>();
		mapCodec.setEscapeCodec(new UrlEscapeCodec());
		mapCodec.setDelimiter("&");

		ListCodec<String> listCodec = new ListCodec<String>(new StringCodec());
		listCodec.setEscapeCodec(new CommaUrlEscapeCodec());
		this.listCodec = listCodec;

		try {
			createInternalUserSessions();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to ensure the persistence of internal user sessions", e);
		}
	}

	@Override
	public void preDestroy() {
		try {
			deleteInternalUserSessions();
		} catch (Exception e) {
			log.error(() -> "Failed to cleanup the internal user sessions", e);
		}
	}

	@Override
	public Maybe<UserSession> createUserSession(User user, UserSessionType type, TimeSpan maxIdleTime, TimeSpan maxAge, Date fixedExpiryDate,
			String internetAddress, Map<String, String> properties, String acquirationKey, boolean blocksAuthenticationAfterLogout) {
		if (user == null || user.getId() == null) {
			return Reasons.build(InvalidArgument.T).text("User and user id cannot be null").toMaybe();
		}

		log.debug(() -> "Creating a user session for user '" + user.getName() + "' connected from '" + internetAddress + "'");
		PersistenceUserSession pUserSession = createPersistenceUserSession(user, type, maxIdleTime, maxAge, fixedExpiryDate, internetAddress,
				properties, acquirationKey, blocksAuthenticationAfterLogout);
		return Maybe.complete(mapToUserSession(pUserSession));
	}

	@Override
	public Maybe<UserSession> findUserSession(String sessionId) {
		log.trace(() -> "Fetching user session '" + sessionId + "'");
		Maybe<PersistenceUserSession> pUserSessionMaybe = findPersistenceUserSession(sessionId);

		if (pUserSessionMaybe.isUnsatisfied()) {
			return Maybe.empty(pUserSessionMaybe.whyUnsatisfied());
		}

		PersistenceUserSession pUserSession = pUserSessionMaybe.get();

		UserSession userSession = mapToUserSession(pUserSession);
		log.trace(() -> "Found user session '" + sessionId + "'; Returning: " + userSession);

		return Maybe.complete(userSession);
	}

	@Override
	public Maybe<UserSession> findUserSessionByAcquirationKey(String acquirationKey) {
		log.trace(() -> "Fetching user session by acquiration key '" + acquirationKey + "'");
		Maybe<PersistenceUserSession> pUserSessionMaybe = findPersistenceUserSessionByAcquirationKey(acquirationKey);

		if (pUserSessionMaybe.isUnsatisfied()) {
			return Maybe.empty(pUserSessionMaybe.whyUnsatisfied());
		}

		PersistenceUserSession pUserSession = pUserSessionMaybe.get();

		if (pUserSession.getClosed() && pUserSession.getBlocksAuthenticationAfterLogout())
			return Reasons.build(InvalidCredentials.T).text("Credentials where already logged out").toMaybe();

		UserSession userSession = mapToUserSession(pUserSession);

		log.trace(() -> "Found user session by acquirationKey '" + acquirationKey + "'; Returning: " + userSession);

		return Maybe.complete(userSession);
	}

	@Override
	public Maybe<DeletedSessionInfo> deleteUserSession(String sessionId) {
		log.debug(() -> "Deleting user session '" + sessionId + "'");

		Maybe<PersistenceUserSession> pUserSessionMaybe = findPersistenceUserSession(sessionId);

		if (pUserSessionMaybe.isUnsatisfied()) {
			return pUserSessionMaybe.whyUnsatisfied().asMaybe();
		}

		PersistenceUserSession pUserSession = pUserSessionMaybe.get();

		String acquirationKey = pUserSession.getAcquirationKey();

		if (acquirationKey != null && pUserSession.getBlocksAuthenticationAfterLogout()) {
			closePersistenceUserSession(pUserSession);
		} else {
			deletePersistenceUserSession(pUserSession);
		}

		DeletedSessionInfo info = new DeletedSessionInfo() {
			UserSession userSession = null;

			@Override
			public UserSession userSession() {
				if (userSession == null)
					userSession = mapToUserSession(pUserSession);

				return userSession;
			}

			@Override
			public String acquirationKey() {
				return pUserSession.getAcquirationKey();
			}
		};

		return Maybe.complete(info);
	}

	protected void createInternalUserSessions() {
		if (internalUserSessionHolders == null || internalUserSessionHolders.isEmpty()) {
			log.warn(() -> "Skipping internal user sessions persistence; Internal user session holder list was not configured or is empty");
			return;
		}
		for (Hub<UserSession> userSessionHolder : internalUserSessionHolders) {
			UserSession userSession = createInternalUserSession(userSessionHolder);
			userSessionHolder.accept(userSession);
		}
	}

	private UserSession createInternalUserSession(Hub<UserSession> userSessionHolder) throws RuntimeException, GmSessionException {
		UserSession userSession = userSessionHolder.get();

		PersistenceUserSession pUserSession = createPersistenceUserSession(userSession.getUser(), UserSessionType.internal, null, null, null,
				userSession.getCreationInternetAddress(), userSession.getProperties(), null, false);

		return mapToUserSession(pUserSession);
	}

	protected void deleteInternalUserSessions() {
		if (this.internalUserSessionHolders == null || this.internalUserSessionHolders.isEmpty()) {
			log.warn(() -> "Skipping internal user sessions cleanup; Internal user session holder list was not configured or is empty");
			return;
		}
		for (Hub<UserSession> userSessionHolder : this.internalUserSessionHolders) {
			deleteInternalUserSession(userSessionHolder);
		}
	}

	private void deleteInternalUserSession(Hub<UserSession> userSessionHolder) {
		UserSession userSession = userSessionHolder.get();
		if (userSession == null) {
			return;
		}
		deletePersistenceUserSession(userSession.getSessionId());
	}

	protected String generateSessionId(UserSessionType userSessionType) {
		String sessionId = null;
		try {
			sessionId = sessionIdProvider.apply(userSessionType);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to obtain the session id from the session id provider");
		}
		if (sessionId == null) {
			throw new IllegalStateException("null session id was returned from the session id provider");
		}
		return sessionId;
	}

	protected void touchPersistenceUserSessionLocally(PersistenceUserSession pUserSession) {
		pUserSession.setLastAccessedDate(new Date());
		pUserSession.setExpiryDate(calculateExpiryDate(pUserSession.getLastAccessedDate(), pUserSession.getMaxIdleTime()));
		if (pUserSession.getExpiryDate() != null) {
			if (pUserSession.getFixedExpiryDate() != null && pUserSession.getFixedExpiryDate().before(pUserSession.getExpiryDate())) {
				pUserSession.setExpiryDate(pUserSession.getFixedExpiryDate());
			}
		} else {
			pUserSession.setExpiryDate(pUserSession.getFixedExpiryDate());
		}
	}

	private UserSession mapToUserSession(PersistenceUserSession pUserSession) {
		UserSession userSession = UserSession.T.create();
		userSession.setSessionId(pUserSession.getId());
		userSession.setCreationDate(pUserSession.getCreationDate());
		userSession.setFixedExpiryDate(pUserSession.getFixedExpiryDate());
		userSession.setExpiryDate(pUserSession.getExpiryDate());
		userSession.setLastAccessedDate(pUserSession.getLastAccessedDate());
		userSession.setEffectiveRoles(stringToSet(pUserSession.getEffectiveRoles()));
		if (pUserSession.getSessionType() != null) {
			userSession.setType(UserSessionType.valueOf(pUserSession.getSessionType()));
		}
		userSession.setCreationInternetAddress(pUserSession.getCreationInternetAddress());
		userSession.setCreationNodeId(pUserSession.getCreationNodeId());
		userSession.setProperties(stringToMap(pUserSession.getProperties()));
		if (pUserSession.getMaxIdleTime() != null) {
			userSession.setMaxIdleTime(millisToTimeSpan(pUserSession.getMaxIdleTime()));
		}
		User user = User.T.create();
		user.setId(pUserSession.getUserName());
		user.setName(pUserSession.getUserName());
		user.setFirstName(pUserSession.getUserFirstName());
		user.setLastName(pUserSession.getUserLastName());
		user.setEmail(pUserSession.getUserEmail());
		userSession.setUser(user);

		return userSession;
	}

	protected Date calculateExpiryDate(Date pivot, TimeSpan ts) {
		if (ts == null) {
			return null;
		}
		return new Date(pivot.getTime() + timeSpanToMillis(ts));
	}

	protected Date calculateExpiryDate(Date pivot, Long millis) {
		if (millis == null) {
			return null;
		}
		return new Date(pivot.getTime() + millis);
	}

	protected String mapToString(Map<String, String> map) {
		if (map == null) {
			return null;
		}
		try {
			return mapCodec.encode(map);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Unable to encode map '" + map + "' to string");
		}
	}

	private Map<String, String> stringToMap(String string) {
		if (string == null) {
			return null;
		}
		try {
			return mapCodec.decode(string);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Unable to decode string '" + string + "' to map");
		}
	}

	protected String setToString(Set<String> set) {
		if (set == null) {
			return null;
		}
		try {
			return listCodec.encode(new ArrayList<>(set));
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Unable to encode set '" + set + "' to string");
		}
	}

	private Set<String> stringToSet(String string) {
		if (string == null) {
			return null;
		}
		try {
			return new HashSet<>(listCodec.decode(string));
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Unable to decode string '" + string + "' to set");
		}
	}

	protected Long timeSpanToMillis(TimeSpan timeSpan) {
		if (timeSpan == null) {
			return null;
		}
		try {
			return timeSpanCodec.encode(timeSpan).longValue();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Unable to convert '" + timeSpan + "' to milliseconds");
		}
	}

	private TimeSpan millisToTimeSpan(long millis) {
		TimeSpan maxIdleTime = TimeSpan.T.create();
		maxIdleTime.setUnit(TimeUnit.milliSecond);
		maxIdleTime.setValue(millis);
		return maxIdleTime;
	}

	protected PersistenceUserSession initPersistenceUserSession(PersistenceUserSession pUserSession, User user, TimeSpan maxIdleTime, TimeSpan maxAge,
			Date fixedExpiryDate, String internetAddress, Map<String, String> properties, String acquirationKey,
			boolean blocksAuthenticationAfterLogout, UserSessionType userSessionType, Date now) {
		pUserSession.setId(generateSessionId(userSessionType));
		pUserSession.setAcquirationKey(acquirationKey);
		pUserSession.setBlocksAuthenticationAfterLogout(blocksAuthenticationAfterLogout);
		pUserSession.setCreationDate(now);
		pUserSession.setFixedExpiryDate(fixedExpiryDate);
		pUserSession.setEffectiveRoles(setToString(Roles.userEffectiveRoles(user)));
		pUserSession.setSessionType(userSessionType.toString());
		pUserSession.setCreationInternetAddress(internetAddress);
		pUserSession.setCreationNodeId(nodeId);
		pUserSession.setProperties(mapToString(properties));
		if (maxAge != null && pUserSession.getFixedExpiryDate() == null) {
			pUserSession.setFixedExpiryDate(calculateExpiryDate(now, maxAge));
		}
		if (maxIdleTime != null) {
			pUserSession.setMaxIdleTime(timeSpanToMillis(maxIdleTime));
		} else {
			if (!userSessionType.equals(UserSessionType.internal)) {
				pUserSession.setMaxIdleTime(timeSpanToMillis(this.defaultUserSessionMaxIdleTime));
			}
		}
		touchPersistenceUserSessionLocally(pUserSession);

		pUserSession.setUserName(user.getName());
		pUserSession.setUserFirstName(user.getFirstName());
		pUserSession.setUserLastName(user.getLastName());
		pUserSession.setUserEmail(user.getEmail());
		return pUserSession;
	}

}