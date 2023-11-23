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
package com.braintribe.model.processing.securityservice.commons.service;

import java.util.Objects;
import java.util.function.Predicate;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.processing.securityservice.api.ServiceUserSessionScoping;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.ServiceRequestSummaryLogger;
import com.braintribe.model.processing.service.api.SessionIdAspect;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * A {@link ServiceProcessor} which ensures that {@link AuthorizedRequest AuthorizedRequests} are processed under an
 * authorization context.
 * 
 */
public class LegacyAuthorizingServiceProcessor2 implements ServiceProcessor<ServiceRequest, Object> {

	private ServiceProcessor<ServiceRequest, Object> delegateServiceProcessor;
	private ServiceUserSessionScoping serviceUserSessionScoping;
	private Predicate<ServiceRequest> authorizationExemptionFilter = v -> false;
	private ThreadRenamer threadRenamer = ThreadRenamer.NO_OP;

	private static final Logger log = Logger.getLogger(LegacyAuthorizingServiceProcessor2.class);

	@Required
	@Configurable
	public void setDelegateServiceProcessor(ServiceProcessor<ServiceRequest, Object> delegateServiceProcessor) {
		this.delegateServiceProcessor = delegateServiceProcessor;
	}

	@Required
	@Configurable
	public void setServiceUserSessionScoping(ServiceUserSessionScoping serviceUserSessionScoping) {
		this.serviceUserSessionScoping = serviceUserSessionScoping;
	}

	@Configurable
	public void setAuthorizationExemptionFilter(Predicate<ServiceRequest> authorizationExemptionFilter) {
		Objects.requireNonNull(authorizationExemptionFilter, "authorizationExemptionFilter cannot be set to null");
		this.authorizationExemptionFilter = authorizationExemptionFilter;
	}

	@Configurable
	public void setThreadRenamer(ThreadRenamer threadRenamer) {
		Objects.requireNonNull(threadRenamer, "threadRenamer cannot be set to null");
		this.threadRenamer = threadRenamer;
	}

	@Override
	public Object process(ServiceRequestContext context, ServiceRequest request) {

		Objects.requireNonNull(context, "context must not be null");
		Objects.requireNonNull(request, "request must not be null");

		if (requiresAuthentication(context, request)) {
			return processWithAuthorization(context, (AuthorizedRequest) request);
		} else {
			return processWithDelegate(context, request);
		}

	}

	public Object processWithAuthorization(ServiceRequestContext requestContext, AuthorizedRequest request) {
		UserSessionScope userSessionScope = pushUserSession(requestContext, request);
		threadRenamer.push(() -> "as(" + threadNamePart(requestContext) + ")");
		try {
			return processWithDelegate(requestContext, request);
		} finally {
			threadRenamer.pop();
			popUserSession(userSessionScope);
		}
	}

	protected Object processWithDelegate(ServiceRequestContext requestContext, ServiceRequest request) {

		ServiceRequestSummaryLogger summaryLogger = requestContext.summaryLogger();
		String summaryStep = summaryLogger.isEnabled() ? request.entityType().getShortName() + " processing" : null;

		threadRenamer.push(() -> "eval(" + threadNamePart(request) + ")");

		try {

			if (summaryStep != null) {
				summaryLogger.startTimer(summaryStep);
			}

			Object result = delegateServiceProcessor.process(requestContext, request);
			return result;

		} finally {

			if (summaryStep != null) {
				summaryLogger.stopTimer(summaryStep);
			}

			threadRenamer.pop();

		}

	}

	protected UserSessionScope pushUserSession(ServiceRequestContext requestContext, AuthorizedRequest request) {
		UserSessionScope userSessionScope;
		try {
			userSessionScope = serviceUserSessionScoping.forContext(requestContext, request).push();
		} catch (SecurityServiceException e) {
			throw new AuthorizationException(e.getMessage(), e);
		}
		return userSessionScope;
	}

	protected void popUserSession(UserSessionScope userSessionScope) {
		try {
			userSessionScope.pop();
		} catch (Exception e) {
			log.error("Failed to pop the user session" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}
	}

	/**
	 * <p>
	 * Determines whether the given incoming request requires authentication.
	 * 
	 * @param requestContext
	 *            The incoming {@link ServiceRequestContext}
	 * @param request
	 *            The incoming {@link ServiceRequest}
	 * @return If the given request requires authentication.
	 */
	protected boolean requiresAuthentication(ServiceRequestContext requestContext, ServiceRequest request) {

		if (request.requiresAuthentication()) {

			if (authorizationExemptionFilter.test(request)) {
				// The request, despite its type, was configured to be exempted.
				return false;
			}

			if (!requestContext.isAuthorized()) {
				// The request requires authorization but is not already authorized
				return true;
			}

			AuthorizedRequest authorizedRequest = (AuthorizedRequest) request;

			String sessionId = authorizedRequest.getSessionId();

			if (sessionId == null || sessionId.trim().isEmpty()) {
				authorizedRequest.setSessionId(requestContext.getRequestorSessionId());
				// The request is already authorized and this request doesn't require a different authorization context
				return false;
			}

			if (sessionId.equals(requestContext.getRequestorSessionId())) {
				// The request is already authorized and this request doesn't require a different authorization context
				return false;
			}

			// The request is already authorized but this request requires a different authorization context
			return true;

		}

		return false;

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
		}

		return sessionId;

	}

	private String threadNamePart(ServiceRequestContext requestContext) {

		String sessionId = requestContext.getRequestorSessionId();

		if (sessionId != null) {
			int l = sessionId.length();
			if (l > 5) {
				sessionId = sessionId.substring(l - 5);
			}
			return requestContext.getRequestorUserName() + ":" + sessionId;
		}

		return requestContext.getRequestorUserName();

	}

	private String threadNamePart(ServiceRequest request) {
		if (request == null) {
			return "null";
		}
		return request.entityType().getShortName();
	}

}
