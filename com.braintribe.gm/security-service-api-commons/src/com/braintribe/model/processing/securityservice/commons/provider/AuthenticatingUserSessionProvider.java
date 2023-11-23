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
package com.braintribe.model.processing.securityservice.commons.provider;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.DestructionAware;
import com.braintribe.cfg.Required;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.security.reason.InvalidSession;
import com.braintribe.gm.model.security.reason.SessionExpired;
import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.securityservice.api.exceptions.ExpiredSessionException;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidCredentialsException;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidSessionException;
import com.braintribe.model.processing.securityservice.api.exceptions.SessionNotFoundException;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;

public class AuthenticatingUserSessionProvider<T extends Credentials> implements Supplier<UserSession>, Consumer<Throwable>, DestructionAware {

	private static Logger logger = Logger.getLogger(AuthenticatingUserSessionProvider.class);

	protected Evaluator<ServiceRequest> evaluator;
	protected T credentials;
	protected boolean authenticating = false;

	private UserSession userSession;
	private ReentrantLock lock = new ReentrantLock();

	@Required
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Required
	@Configurable
	public void setCredentials(T credentials) {
		this.credentials = credentials;
	}

	@Override
	public void accept(Throwable authorizationFailure) throws RuntimeException {

		if (logger.isDebugEnabled()) {
			String logMsg = "Resetting current user session [ " + userSession + " ] due to [ " + throwableToString(authorizationFailure) + " ]";
			logger.debug(logMsg);
			if (logger.isTraceEnabled()) {
				logger.trace(logMsg, authorizationFailure);
			}
		}

		lock.lock();
		try {
			userSession = null;
		} finally {
			lock.unlock();
		}

	}

	@Override
	public UserSession get() throws RuntimeException {
		lock.lock();
		try {
			return provideUserSession();
		} catch (AuthorizationException | AuthenticationException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Could not authenticate with configured credentials", e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void preDestroy() {

		// working on a copy to avoid NPEs in case this.userSession is set to null by another thread.
		UserSession tmpUserSession = userSession;

		if (tmpUserSession != null) {
			try {
				Logout logout = Logout.T.create();
				logout.setSessionId(tmpUserSession.getSessionId());
				logout.eval(evaluator).get();
			} catch (Throwable e) {
				if (logger.isDebugEnabled()) {
					String errorMessage = "User session " + tmpUserSession + " couldn't be explicitly logged out due to " + e.getClass().getName()
							+ ((e.getMessage() != null) ? ": " + e.getMessage() : "");
					if (logger.isTraceEnabled()) {
						logger.trace(errorMessage, e);
					} else {
						logger.debug(errorMessage);
					}
				}
			}
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace("No user session to logout");
			}
		}

	}

	protected UserSession provideUserSession() throws AuthenticationException {

		if (userSession != null) {
			return userSession;
		}

		if (authenticating) {
			// break the circular call sequence
			return userSession;
		}

		try {
			authenticating = true;

			Maybe<? extends OpenUserSessionResponse> responseMaybe = openUserSession();

			if (responseMaybe.isUnsatisfiedBy(AuthenticationFailure.T)) {
				throw createAuthenticationException(responseMaybe.whyUnsatisfied());
			}

			return userSession = responseMaybe.get().getUserSession();

		} finally {
			authenticating = false;
		}

	}

	protected Maybe<? extends OpenUserSessionResponse> openUserSession() throws AuthenticationException {

		OpenUserSession request = OpenUserSession.T.create();
		request.setCredentials(this.credentials);

		return request.eval(evaluator).getReasoned();

	}

	private static String throwableToString(Throwable throwable) {
		if (throwable == null) {
			return null;
		}
		return throwable.getClass().getName() + ((throwable.getMessage() == null) ? "" : ": " + throwable.getMessage());
	}

	private AuthenticationException createAuthenticationException(AuthenticationFailure reason) {
		if (reason instanceof InvalidSession) {
			if (reason instanceof SessionExpired) {
				return new ExpiredSessionException(reason.stringify());
			} else if (reason instanceof SessionNotFound) {
				return new SessionNotFoundException(reason.stringify());
			} else {
				return new InvalidSessionException(reason.stringify());
			}
		} else if (reason instanceof InvalidCredentials) {
			return new InvalidCredentialsException(reason.stringify());

		} else {
			return new AuthenticationException(reason.stringify());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("AuthenticatingUserSessionProvider: ");
		if (userSession != null) {
			sb.append(", User session: " + userSession.getId());
		}
		return sb.toString();
	}
}
