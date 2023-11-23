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
package com.braintribe.gwt.security.tfh.client;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.async.client.Loader;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.security.client.AbstractSecurityService;
import com.braintribe.gwt.security.client.AuthenticationException;
import com.braintribe.gwt.security.client.Session;
import com.braintribe.gwt.security.client.SessionScope;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.ExistingSessionCredentials;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TfhSecurityService extends AbstractSecurityService {
	protected static final Logger logger = new Logger(TfhSecurityService.class);
	private Supplier<String> localeProvider;
	private Supplier<Loader<?>> preparingLoaderProvider;
	private Predicate<Session> loginSessionFilter;
	private boolean redirectToLogoutServlet = true;
	private String logoutServletUrl = "/tribefire-services/logout";
	private Evaluator<ServiceRequest> evaluator;

	@Configurable
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Configurable
	public void setLocaleProvider(Supplier<String> localeProvider) {
		this.localeProvider = localeProvider;
	}

	@Configurable
	public void setPreparingLoaderProvider(Supplier<Loader<?>> preparingLoaderProvider) {
		this.preparingLoaderProvider = preparingLoaderProvider;
	}

	/**
	 * Configures whether we should redirect to the logout Servlet in case of logout, instead of calling the service.
	 * Defaults to true.
	 */
	@Configurable
	public void setRedirectToLogoutServlet(boolean redirectToLogoutServlet) {
		this.redirectToLogoutServlet = redirectToLogoutServlet;
	}

	/**
	 * Configures the url of the logout Servlet. Defaults to "logoutServletUrl".
	 */
	@Configurable
	public void setLogoutServletUrl(String logoutServletUrl) {
		this.logoutServletUrl = logoutServletUrl;
	}

	@Override
	public Future<Boolean> loginWithExistingSession(String username, String sessionId) {

		ExistingSessionCredentials credentials = ExistingSessionCredentials.T.create();
		credentials.setReuseSession(true);
		credentials.setExistingSessionId(sessionId);

		return login(credentials);
	}

	@Override
	public Future<Boolean> login(Credentials credentials) {
		final Future<Boolean> future = new Future<>();

		OpenUserSession request;
		try {
			request = OpenUserSession.T.create();

			request.setLocale(localeProvider.get());
			request.setCredentials(credentials);
		} catch (RuntimeException e1) {
			future.onFailure(e1);
			return future;
		}

		com.braintribe.processing.async.api.AsyncCallback<Maybe<? extends OpenUserSessionResponse>> callback = new com.braintribe.processing.async.api.AsyncCallback<Maybe<? extends OpenUserSessionResponse>>() {
			@Override
			public void onSuccess(Maybe<? extends OpenUserSessionResponse> resultMaybe) {
				postProcess(resultMaybe, future);
			}
			@Override
			public void onFailure(Throwable caught) {
				future.onFailure(new com.braintribe.gwt.security.client.SecurityServiceException("error while authenticating", caught));
			}
		};

		request.eval(evaluator).getReasoned(callback);

		return future;
	}

	protected void postProcess(Maybe<? extends OpenUserSessionResponse> responseMaybe, final Future<Boolean> future) {
		// check successfully response
		if (responseMaybe.isUnsatisfied()) {
			if (responseMaybe.isUnsatisfiedBy(AuthenticationFailure.T)) {
				future.onFailure(new AuthenticationException(responseMaybe.whyUnsatisfied().asString()));
			} else {
				future.onFailure(new ReasonException(responseMaybe.whyUnsatisfied()));

			}
		}

		OpenUserSessionResponse response = responseMaybe.get();

		UserSession userSession = response.getUserSession();
		User user = userSession.getUser();
		final Session session = new Session(user.getName(), userSession.getSessionId());
		session.setFullName((user.getFirstName() == null ? "" : user.getFirstName()) + " " + (user.getLastName() == null ? "" : user.getLastName()));
		Set<String> roles = userSession.getEffectiveRoles();

		/* for (Role role: userSession.getEffectiveRoles()) { roles.add(role.getName()); } */
		session.setRoles(roles);

		// check for login filter
		if (loginSessionFilter != null && !loginSessionFilter.test(session)) {
			future.onFailure(new AuthenticationException("session filter denied session"));
			return;
		}

		createSessionScope();

		if (preparingLoaderProvider == null) {
			onSessionComplete(session, future);
			return;
		}

		try {
			Loader<Object> preparingLoader = (Loader<Object>) preparingLoaderProvider.get();
			preparingLoader.load(new AsyncCallback<Object>() {
				@Override
				public void onSuccess(Object result) {
					onSessionComplete(session, future);
				}

				@Override
				public void onFailure(Throwable caught) {
					removeSessionScope();
					future.onFailure(caught);
				}
			});
		} catch (RuntimeException e) {
			future.onFailure(e);
		}
	}

	protected void onSessionComplete(Session session, Future<Boolean> future) {
		this.session = session;
		fireSessionCreated();
		future.onSuccess(true);
	}

	@Override
	public Future<Boolean> loginSSO() {
		Future<Boolean> future = new Future<>();
		future.onFailure(new AuthenticationException("unsupported auth method"));
		return future;
	}

	@Override
	public Future<Void> loginWithExistingSession(Session session) {
		this.session = session;

		createSessionScope();

		Future<Void> future = new Future<>();

		fireSessionCreated();
		future.onSuccess(null);

		return future;
	}

	private void createSessionScope() {
		SessionScope scope = new SessionScope();
		SessionScope.scopeManager.openAndPushScope(scope);
	}

	private void removeSessionScope() {
		SessionScope.scopeManager.closeAndPopScope();
	}

	@Override
	public void login(String username, String password, AsyncCallback<Session> asyncCallback) {
		UserPasswordCredentials credentials = UserPasswordCredentials.T.create();
		UserNameIdentification userIdentification = UserNameIdentification.T.create();
		userIdentification.setUserName(username);
		credentials.setPassword(password);
		credentials.setUserIdentification(userIdentification);

		login(credentials).get(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				asyncCallback.onSuccess(session);
			}

			@Override
			public void onFailure(Throwable caught) {
				asyncCallback.onFailure(caught);

			}
		});
	}

	@Override
	public void logout(AsyncCallback<Boolean> asyncCallback) {
		logout(false, asyncCallback);
	}

	@Override
	public void logout(boolean silent, AsyncCallback<Boolean> asyncCallback) {
		if (!silent)
			fireSessionClosing();

		com.braintribe.processing.async.api.AsyncCallback<Boolean> callback = new com.braintribe.processing.async.api.AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {

				if (asyncCallback != null)
					asyncCallback.onSuccess(result);

				if (!silent)
					fireSessionClosed();

				removeSessionScope();
			}

			@Override
			public void onFailure(Throwable caught) {
				asyncCallback.onFailure(caught);
			}
		};

		if (redirectToLogoutServlet) {
			boolean containsParameter = logoutServletUrl.contains("?");
			Window.Location.replace(logoutServletUrl + (containsParameter ? "&" : "?") + "sessionId=" + session.getId());
			callback.onSuccess(true);
		} else {
			try {
				Logout logout = Logout.T.create();
				logout.setSessionId(session.getId());
				logout.eval(evaluator).get(callback);
			} catch (SecurityServiceException e) {
				asyncCallback.onFailure(e);
			}
		}
	}

	@Override
	public Future<Boolean> changePassword(String oldPassword, String newPassword) {
		Future<Boolean> future = new Future<>();

		future.onFailure(new UnsupportedOperationException("not yet implemented"));
		return future;
	}

	@Override
	public Future<Boolean> recheckUserPassword(String password) {
		Future<Boolean> future = new Future<>();
		future.onFailure(new UnsupportedOperationException("not yet implemented"));
		return future;
	}

}
