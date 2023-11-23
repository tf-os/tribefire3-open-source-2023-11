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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.api.UserSessionScope;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.securityservice.api.UserSessionScopingBuilder;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.securityservice.commons.provider.AuthenticatingUserSessionSupplier;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.api.MinimalStack;

/**
 * <p>
 * A standard {@link UserSessionScoping} implementation.
 * 
 */
public class StandardUserSessionScoping implements UserSessionScoping {

	private MinimalStack<UserSession> userSessionStack;
	protected Evaluator<ServiceRequest> evaluator;
	private Supplier<UserSession> defaultUserSessionSupplier;
	private Consumer<Object> defaultUserSessionFailureConsumer;
	private int defaultUserSessionRetries = 2;

	private final ConcurrentHashMap<String, Supplier<UserSession>> virtualUserSuppliers = new ConcurrentHashMap<>();

	@Required
	public void setUserSessionStack(MinimalStack<UserSession> userSessionStack) {
		this.userSessionStack = userSessionStack;
	}

	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Configurable
	public void setDefaultUserSessionSupplier(Supplier<UserSession> defaultUserSessionSupplier) {
		this.defaultUserSessionSupplier = defaultUserSessionSupplier;
	}

	@Configurable
	public void setDefaultUserSessionFailureConsumer(Consumer<Object> defaultUserSessionFailureConsumer) {
		this.defaultUserSessionFailureConsumer = defaultUserSessionFailureConsumer;
	}

	@Configurable
	public void setDefaultUserSessionRetries(int defaultUserSessionRetries) {
		this.defaultUserSessionRetries = defaultUserSessionRetries;
	}

	@Override
	public UserSessionScopingBuilder forDefaultUser() {
		ensureDefault();
		return new StandardUserSessionScopingBuilder(defaultUserSessionSupplier);
	}

	@Override
	public UserSessionScopingBuilder forCredentials(Credentials credentials) {

		Objects.requireNonNull(credentials, "credentials must not be null");

		AuthenticatingUserSessionSupplier supplier = new AuthenticatingUserSessionSupplier();
		supplier.setCredentials(credentials);
		supplier.setEvaluator(evaluator);

		return new StandardUserSessionScopingBuilder(supplier);

	}

	private void ensureDefault() {
		if (defaultUserSessionSupplier == null) {
			throw new IllegalStateException("This is not configured properly to support this operation: Missing defaultUserSessionSupplier");
		}
	}

	private class StandardUserSessionScopingBuilder implements UserSessionScopingBuilder {

		private final Supplier<UserSession> userSessionSupplier;

		StandardUserSessionScopingBuilder(Supplier<UserSession> userSessionSupplier) {
			this.userSessionSupplier = userSessionSupplier;
		}

		@Override
		public UserSessionScope push() throws SecurityServiceException {

			final UserSession userSession = userSessionSupplier.get();

			userSessionStack.push(userSession);

			return new UserSessionScope() {

				@Override
				public UserSession getUserSession() throws SecurityServiceException {
					return userSession;
				}

				@Override
				public UserSession pop() throws SecurityServiceException {
					try {
						return userSession;
					} finally {
						userSessionStack.pop();
					}
				}

			};
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

}
