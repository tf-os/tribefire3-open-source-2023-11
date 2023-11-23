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
package com.braintribe.model.processing.webrpc.client;

import org.apache.http.protocol.RequestContent;

import com.braintribe.cfg.Configurable;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.InvalidSession;
import com.braintribe.model.generic.reflection.GmReflectionTools;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ReasonedServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;

/**
 * In case the intercepted {@link ServiceRequest} {@link ServiceRequest#supportsAuthentication() supports
 * authentication}, we make sure that the proper sessionId is set.
 * <p>
 * If remote authentication is required, i.e. if {@link #setUserSessionResolver(UserSessionResolver)
 * UserSessionResolver} is configured, it obtains the remote sessionId using this resolver and sets it.
 * <p>
 * If local session is used (no remote authentication), the session id is only set if it can be resolved from the
 * {@link RequestContent} as a value of {@link UserSessionAspect} and if the request
 * {@link ServiceRequest#requiresAuthentication() requires authentication}.
 * <p>
 * This is a simpler counterpart to {@link RemotifyingInterceptor} which doesn't deal with domainId mappings. This is
 * currently intended for remote session factories.
 * 
 * @author peter.gazdik
 */
public class RemoteAuthentifyingInterceptor implements ReasonedServiceAroundProcessor<ServiceRequest, Object> {

	// userSessionSupplier are either null xor keepLocalSessionId is true.
	protected UserSessionResolver userSessionResolver;

	@Configurable
	public void setUserSessionResolver(UserSessionResolver userSessionResolver) {
		this.userSessionResolver = userSessionResolver;
	}

	@Override
	public Maybe<? extends Object> processReasoned(ServiceRequestContext context, ServiceRequest request, ProceedContext proceedContext) {
		if (!needsSpecialHandling(request)) {
			return proceedContext.proceedReasoned(context, request);
		} else {
			return new RemoteAuthenticationInterception(context, request, proceedContext).proceedReasoned();
		}
	}

	protected boolean needsSpecialHandling(ServiceRequest request) {
		return request.supportsAuthentication();
	}

	// #################################################################
	// ## . . . . . . . . . Actual Remotification . . . . . . . . . . ##
	// #################################################################

	public class RemoteAuthenticationInterception {

		protected final ServiceRequestContext requestContext;
		protected final ProceedContext proceedContext;
		protected final ServiceRequest request;
		protected final ServiceRequest remoteRequest;

		private UserSession remoteSession; // only set if keepLocalSessionId = false

		public RemoteAuthenticationInterception(ServiceRequestContext requestContext, ServiceRequest request, ProceedContext proceedContext) {
			this.requestContext = requestContext;
			this.request = request;
			this.remoteRequest = GmReflectionTools.makeShallowCopy(request);
			this.proceedContext = proceedContext;
		}

		public Maybe<Object> proceedReasoned() {
			loadRemoteSessionIdIfNeeded();
			ensureCorrectSessionIdIfNeeded();

			Maybe<Object> maybe = proceedContext.proceedReasoned(requestContext, remoteRequest);
			if (keepLocalSessionId()) {
				return maybe;
			}

			if (maybe.isUnsatisfiedBy(InvalidSession.T)) {
				cleanupAfterAuthorizationProblem();
				maybe = retryWithFreshRemoteSession();
			}
			return maybe;

		}

		private Maybe<Object> retryWithFreshRemoteSession() {
			loadRemoteSession();
			injectRemoteSessionId((AuthorizableRequest) remoteRequest);

			Maybe<Object> maybe = proceedContext.proceedReasoned(requestContext, remoteRequest);
			if (maybe.isUnsatisfiedBy(InvalidSession.T)) {
				cleanupAfterAuthorizationProblem();
			}
			return maybe;
		}

		//
		// Remote Session Id
		//

		private void loadRemoteSessionIdIfNeeded() {
			if (requiresRemoteAuthentication())
				loadRemoteSession();
		}

		private boolean requiresRemoteAuthentication() {
			return request.supportsAuthentication() && !keepLocalSessionId();
		}

		private boolean keepLocalSessionId() {
			return userSessionResolver == null;
		}

		private UserSession loadRemoteSession() {
			return remoteSession = userSessionResolver.acquireUserSession(this::evalOpenUserSession);
		}

		protected OpenUserSessionResponse evalOpenUserSession(OpenUserSession request) {
			return (OpenUserSessionResponse) requestContext.eval(request).get();
		}

		private void cleanupAfterAuthorizationProblem() {
			if (remoteSession != null)
				userSessionResolver.clearUserSession(remoteSession);
		}

		/**
		 * In case of an {@link AuthorizableRequest} we may need to copy the original request and overwrite t's
		 * sessionId to the remote one (or null).
		 */
		private void ensureCorrectSessionIdIfNeeded() {
			if (remoteRequest.supportsAuthentication())
				ensureCorrectSessionId((AuthorizableRequest) remoteRequest);
		}

		private void ensureCorrectSessionId(AuthorizableRequest remoteRequest) {
			if (remoteSession != null)
				injectRemoteSessionId(remoteRequest);
			else
				ensureLocalSessionId(remoteRequest);
		}

		private void injectRemoteSessionId(AuthorizableRequest remoteRequest) {
			remoteRequest.setSessionId(remoteSession.getSessionId());
		}

		private void ensureLocalSessionId(AuthorizableRequest request) {
			if (request.getSessionId() != null)
				return;

			String sessionId = requestContext.getAspect(UserSessionAspect.class) //
					.map(UserSession::getSessionId) //
					.orElse(null);

			if (request.requiresAuthentication() && sessionId == null)
				throw new IllegalStateException("Cannot evaluate request " + request + " of type " + request.entityType().getTypeSignature()
						+ " on a remote server as no sessionId found in the request context.");

			request.setSessionId(sessionId);
		}

	}

}
