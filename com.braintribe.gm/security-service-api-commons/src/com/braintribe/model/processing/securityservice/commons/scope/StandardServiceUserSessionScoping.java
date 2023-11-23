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
package com.braintribe.model.processing.securityservice.commons.scope;

import java.util.Objects;

import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.securityservice.api.ServiceUserSessionScoping;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScopingBuilder;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.securityservice.commons.service.ContextualizedAuthorization;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.SessionIdAspect;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;

/**
 * Standard {@link ServiceUserSessionScoping} implementation.
 * 
 */
public class StandardServiceUserSessionScoping implements ServiceUserSessionScoping {

	// constants
	private static final Logger log = Logger.getLogger(StandardServiceUserSessionScoping.class);

	@Override
	public UserSessionScopingBuilder forContext(ServiceRequestContext context, String sessionId) {

		Objects.requireNonNull(context, "context must not be null");
		Objects.requireNonNull(sessionId, "sessionId must not be null");

		return new BasicServiceUserSessionScopingBuilder(context, sessionId);
	}

	@Override
	public UserSessionScopingBuilder forContext(ServiceRequestContext context, AuthorizedRequest request) {

		Objects.requireNonNull(context, "context must not be null");
		Objects.requireNonNull(request, "request must not be null");

		String sessionId = retrieveSessionId(context, request);

		return new BasicServiceUserSessionScopingBuilder(context, sessionId);

	}

	/**
	 * <p>
	 * Retrieves the user session id from the given {@link AuthorizedRequest}.
	 * 
	 * <p>
	 * Session ids set directly to the request ({@link AuthorizedRequest#getSessionId()}) take priority over ids set in
	 * metadata map ({@link ServiceRequest#getMetaData()}).
	 * 
	 * @param requestContext
	 *            The current {@link ServiceRequestContext}.
	 * @param serviceRequest
	 *            The {@link AuthorizedRequest} to have the session id retrieved from.
	 * @return A session id as retrieved from {@link AuthorizedRequest}.
	 * @throws AuthorizationException
	 *             If no session id is found in the given {@link AuthorizedRequest}.
	 */
	protected String retrieveSessionId(ServiceRequestContext requestContext, AuthorizedRequest serviceRequest) {

		String sessionId = serviceRequest.getSessionId();

		if (sessionId == null && serviceRequest.getMetaData() != null) {
			Object metaSessionId = serviceRequest.getMetaData().get(AuthorizedRequest.sessionId);
			if (metaSessionId != null) {
				sessionId = metaSessionId.toString();
			}
		}

		if (sessionId == null) {
			sessionId = requestContext.findAspect(SessionIdAspect.class);
			if (sessionId != null) {
				serviceRequest.setSessionId(sessionId);
			}
		}

		if (sessionId == null) {
			throw new AuthorizationException(
					"No session id provided in call to authorization required request [ " + serviceRequest.entityType().getTypeSignature() + " ]");
		} else if (log.isTraceEnabled()) {
			log.trace("Processing request with session id [ " + sessionId + " ]: " + serviceRequest);
		}

		return sessionId;

	}

	class BasicServiceUserSessionScopingBuilder implements UserSessionScopingBuilder {

		private final String sessionId;
		private final ServiceRequestContext requestContext;

		private BasicServiceUserSessionScopingBuilder(ServiceRequestContext requestContext, String sessionId) {
			this.requestContext = requestContext;
			this.sessionId = sessionId;
		}

		@Override
		public UserSessionScope push() throws SecurityServiceException {
			ServiceRequestSummaryLogger summaryLogger = requestContext.summaryLogger();
			ServiceRequestContext authorizedRequestContext = new ContextualizedAuthorization<>(requestContext, requestContext, sessionId, summaryLogger).authorize(true);
			
			UserSession userSession = authorizedRequestContext.getAttribute(UserSessionAspect.class);
			
			log.put(AuthorizedRequest.sessionId, userSession.getSessionId());
			
			AttributeContexts.push(authorizedRequestContext);
			
			// TODO: continue here
			return new BasicServiceUserSessionScope(userSession);

		}

		@Override
		public void runInScope(Runnable runnable) throws SecurityServiceException {
			UserSessionScope userSessionScope = push();
			try {
				runnable.run();
			} finally {
				userSessionScope.pop();
			}
		}

	}

	class BasicServiceUserSessionScope implements UserSessionScope {

		private final UserSession userSession;

		private BasicServiceUserSessionScope(UserSession userSession) {
			this.userSession = userSession;
		}

		@Override
		public UserSession getUserSession() throws SecurityServiceException {
			return userSession;
		}

		@Override
		public UserSession pop() throws SecurityServiceException {
			try {
				return userSession;
			} finally {
				AttributeContexts.pop();
				log.remove(AuthorizedRequest.sessionId);
			}
		}

	}

}
