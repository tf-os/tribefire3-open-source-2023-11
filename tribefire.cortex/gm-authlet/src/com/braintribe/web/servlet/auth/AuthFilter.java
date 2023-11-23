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
package com.braintribe.web.servlet.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.string.MapCodec;
import com.braintribe.codec.string.UrlEscapeCodec;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.common.attribute.AttributeContextBuilder;
import com.braintribe.exception.HttpException;
import com.braintribe.exception.LogPreferences;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.security.reason.MissingCredentials;
import com.braintribe.gm.model.security.reason.SecurityReason;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.securityservice.api.attributes.LenientAuthenticationFailure;
import com.braintribe.model.processing.service.api.aspect.IsAuthorizedAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorSessionIdAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorUserNameAspect;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Unsatisfied;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.util.servlet.HttpFilter;
import com.braintribe.util.servlet.util.ServletTools;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.LazyInitialized;
import com.braintribe.web.servlet.auth.providers.CookieProvider;
import com.braintribe.web.servlet.auth.providers.CookieValueProvider;

/**
 * AuthFilter extracts {@link Credentials} with help of configured web credential {@link WebCredentialsProvider
 * providers} from {@link HttpServletRequest}. Then it tries to open a {@link UserSession} with {@link OpenUserSession}
 * and to authorize it based on optionally configured {@link #setGrantedRoles(Set) granted roles}. All relevant
 * information of an authorized {@link UserSession} will be pushed as a new {@link AttributeContext} and the filter will
 * proceed.
 * <p>
 * In case of an authentication or authorization problem the filter will act differently based its
 * {@link #setStrict(boolean) strictness}. If strict it will not proceed and will directy respond according to
 * configuration. If lenient it will proceed without a {@link UserSession} but preserve the reasoning of the missing
 * {@link UserSession} with a {@link LenientAuthenticationFailure} attribute pushed as a new {@link AttributeContext}.
 * 
 * @author dirk.scheffler
 * @author roman.kurmanowytsch
 *
 */
public class AuthFilter implements HttpFilter, InitializationAware {

	private final Logger log = Logger.getLogger(AuthFilter.class);

	private String relativeLoginPath = "/login";
	private Codec<Map<String, String>, String> urlParamCodec;
	private boolean strict = true;
	private Set<String> grantedRoles = Collections.emptySet();
	private Evaluator<ServiceRequest> requestEvaluator;
	private CookieHandler cookieHandler;
	private ThreadRenamer threadRenamer = ThreadRenamer.NO_OP;
	private Map<String, WebCredentialsProvider> webCredentialProviders = new LinkedHashMap<>();

	private Function<HttpServletRequest, String> sessionCookieProvider = new CookieValueProvider(new CookieProvider(Constants.COOKIE_SESSIONID));
	private boolean throwExceptionOnAuthFailure = false;

	@Configurable
	public void addWebCredentialProvider(String key, WebCredentialsProvider webSessionProvider) {
		synchronized (webCredentialProviders) {
			webCredentialProviders.put(key, webSessionProvider);
		}
	}

	@Configurable
	public void setThrowExceptionOnAuthFailure(boolean throwExceptionOnAuthFailure) {
		this.throwExceptionOnAuthFailure = throwExceptionOnAuthFailure;
	}

	@Configurable
	public void setGrantedRoles(Set<String> grantedRoles) {
		this.grantedRoles = Objects.requireNonNull(grantedRoles, "grantedRoles must not be null");
	}

	@Configurable
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	@Configurable
	public void setRelativeLoginPath(String relativeLoginPath) {
		this.relativeLoginPath = relativeLoginPath;
	}

	public void setUrlParamCodec(Codec<Map<String, String>, String> urlParamCodec) {
		this.urlParamCodec = urlParamCodec;
	}

	public Codec<Map<String, String>, String> getUrlParamCodec() {
		if (urlParamCodec == null) {
			MapCodec<String, String> mapCodec = new MapCodec<>();
			mapCodec.setEscapeCodec(new UrlEscapeCodec());
			mapCodec.setDelimiter("&");
			this.urlParamCodec = mapCodec;
		}
		return urlParamCodec;
	}

	@Override
	public void postConstruct() {
		if (!strict && !grantedRoles.isEmpty())
			throw new IllegalStateException("If grantedRoles is not empty strict must be true");
	}

	private class StatefulAuthFilter {
		private HttpServletRequest request;
		private HttpServletResponse response;
		private FilterChain chain;
		private AttributeContextBuilder contextBuilder = AttributeContexts.peek().derive();

		public StatefulAuthFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
			super();
			this.request = request;
			this.response = response;
			this.chain = chain;
		}

		/**
		 * @param userSession
		 *            the {@link UserSession} that was authenticated and authorized or null if there was none
		 */
		private void proceed(UserSession userSession) throws IOException, ServletException {

			if (userSession != null) {
				setCookieIfNeccessary(userSession);

				if (log.isTraceEnabled())
					log.trace("Found valid session: " + userSession);

				contextBuilder //
						.set(RequestorSessionIdAspect.class, userSession.getSessionId()) //
						.set(RequestorUserNameAspect.class, userSession.getUser().getName()) //
						.set(IsAuthorizedAspect.class, true) //
						.set(UserSessionAspect.class, userSession);
			}

			threadRenamer.push(() -> "as(" + threadNamePart(userSession) + ")");
			AttributeContexts.push(contextBuilder.build());

			try {
				chain.doFilter(request, response);
			} finally {
				threadRenamer.pop();
				AttributeContexts.pop();
			}
		}

		public void doFilter() throws IOException, ServletException {
			Maybe<UserSession> sessionMaybe = authorize();

			if (sessionMaybe.isUnsatisfied()) {
				respondOnAuthenticationFailure(sessionMaybe.whyUnsatisfied());
			} else {
				proceed(sessionMaybe.get());
			}
		}

		private void respondOnAuthenticationFailure(Reason whyUnsatisfied) throws IOException {
			Reason authFailure = maskWithAuthenticationFailureIfNecessary(whyUnsatisfied);

			String message = authFailure.stringify();

			if (shouldSendRedirectOnAuthFailure(request)) {
				sendLoginRedirect(message);
			} else if (throwExceptionOnAuthFailure) {
				throwHttpException(authFailure, message);
			} else {
				sendUnauthorized(message);
			}
		}

		private void sendUnauthorized(String message) throws IOException {
			log.debug(message);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().print(message);
		}

		private void throwHttpException(Reason authFailure, String message) {
			int statusCode = authFailure instanceof Forbidden ? HttpServletResponse.SC_FORBIDDEN : HttpServletResponse.SC_UNAUTHORIZED;

			HttpException httpException = new HttpException(statusCode, message);
			httpException.setLogPreferences(new LogPreferences(LogLevel.INFO, false, LogLevel.TRACE));
			httpException.withPayload(Unsatisfied.from(Maybe.empty(authFailure)));

			throw httpException;
		}

		private void sendLoginRedirect(String message) throws IOException {
			log.debug(message);

			// NOTE that this assumes that the current context is always tribefire services
			// That currently seems to be the case but if not we need a generic way to find public urls
			// for contexts
			String servicesPath = getPublicServicesUrl(request);

			String continuePath = servicesPath + request.getServletPath();

			if (request.getPathInfo() != null)
				continuePath += request.getPathInfo();

			if (request.getQueryString() != null) {
				continuePath += "?" + request.getQueryString();
			}

			response.sendRedirect(buildLoginPath(request, message, continuePath));
		}

		private AuthenticationFailure wrapWithAuthenticationFailureIfNecessary(Reason whyUnsatisfied) {
			if (whyUnsatisfied instanceof AuthenticationFailure)
				return (AuthenticationFailure) whyUnsatisfied;

			return Reasons.build(AuthenticationFailure.T).text("Authentication failed.").cause(whyUnsatisfied).toReason();
		}

		private Reason maskWithAuthenticationFailureIfNecessary(Reason whyUnsatisfied) {
			if (whyUnsatisfied instanceof SecurityReason)
				return whyUnsatisfied;

			return Reasons.build(AuthenticationFailure.T).text("Authentication failed.").toReason();
		}

		private String getPublicServicesUrl(HttpServletRequest request) {
			String publicServicesUrl = TribefireRuntime.getPublicServicesUrl();
			String servicesPath = publicServicesUrl == null ? ServletTools.getServletContextPath(request) : publicServicesUrl;
			return servicesPath;
		}

		private boolean shouldSendRedirectOnAuthFailure(HttpServletRequest request) {
			return relativeLoginPath != null && ServletTools.getAcceptedMimeTypes(request).contains("text/html");
		}

		/* private Maybe<UserSession> checkAccessGranted(UserSession session) { if (session != null) { boolean grantedByRoles =
		 * (CollectionTools.isEmpty(grantedRoles) || CollectionTools.containsAny(grantedRoles, session.getEffectiveRoles()));
		 * 
		 * if (grantedByRoles) return Maybe.complete(session);
		 * 
		 * if (authorizationFreePredicate.test(request)) { return Maybe.complete(session); }
		 * 
		 * } else { if (authorizationFreePredicate.test(request)) { return Maybe.complete(session); } }
		 * 
		 * return Reasons.build(Forbidden.T).text("Insufficient priviledges to access endpoint").toMaybe(session); } */

		private Maybe<UserSession> checkAccessGranted(Maybe<UserSession> sessionMaybe) {
			if (sessionMaybe.isUnsatisfied()) {
				if (strict)
					return sessionMaybe;

				Reason whyUnsatisfied = sessionMaybe.whyUnsatisfied();

				/* An AuthenticationFailure suggests that there were credentials that could not be authenticated which is maybe relevant
				 * to the nested processing. Other reasons suggest internal problems which are not meaningful for the nested level. */
				if (!sessionMaybe.isUnsatisfiedBy(MissingCredentials.T)) {
					log.info("Lenient authentication failure: " + whyUnsatisfied.stringify());
				}

				contextBuilder.set(LenientAuthenticationFailure.class, wrapWithAuthenticationFailureIfNecessary(whyUnsatisfied));

				return Maybe.complete(null);
			}

			UserSession session = sessionMaybe.get();

			boolean authorized = (CollectionTools.isEmpty(grantedRoles) || CollectionTools.containsAny(grantedRoles, session.getEffectiveRoles()));

			if (authorized)
				return Maybe.complete(session);

			return Reasons.build(Forbidden.T).text("Insufficient priviledges to access endpoint").toMaybe(session);
		}

		private String buildLoginPath(HttpServletRequest request, String message, String continuePath) {
			String servicesPath = getPublicServicesUrl(request);

			Map<String, String> params = new HashMap<>();
			if (message != null) {
				params.put(Constants.REQUEST_PARAM_MESSAGE, message);
			}
			if (continuePath != null) {
				params.put(Constants.REQUEST_PARAM_CONTINUE, continuePath);
			}

			String path = servicesPath + relativeLoginPath;
			if (params.size() > 0)
				path += "?" + getUrlParamCodec().encode(params);

			return path;

		}

		private Maybe<Credentials> findCredentials(HttpServletRequest request) {
			LazyInitialized<Reason> invalidCredentialProblems = new LazyInitialized<>(
					() -> Reasons.build(InvalidCredentials.T).text("Error while extracting credentials from http request").toReason());

			for (Map.Entry<String, WebCredentialsProvider> entry : webCredentialProviders.entrySet()) {
				WebCredentialsProvider webCredentialProvider = entry.getValue();

				Maybe<Credentials> credentialsMaybe = webCredentialProvider.provideCredentials(request);

				if (credentialsMaybe.isUnsatisfied()) {
					if (credentialsMaybe.isUnsatisfiedBy(MissingCredentials.T))
						continue;

					if (credentialsMaybe.isUnsatisfiedBy(InvalidCredentials.T)) {
						invalidCredentialProblems.get().getReasons().add(credentialsMaybe.whyUnsatisfied());
						continue;
					}

					log.error(credentialsMaybe.whyUnsatisfied().stringify());
				} else {
					return credentialsMaybe;
				}
			}

			if (invalidCredentialProblems.isInitialized()) {
				// TODO: wrap or don't wrap if there is only one invalid credentials
				return invalidCredentialProblems.get().asMaybe();
			}

			return Reasons.build(MissingCredentials.T).text("No credentials found in http request").toMaybe();
		}

		/**
		 * Does an authorization if possible depending on the configuration
		 * 
		 * @return A {@link Maybe} of a {@link UserSession} which also can be null if there was no authorization achieved and
		 *         also not required.
		 */
		private Maybe<UserSession> authorize() {
			return checkAccessGranted(openUserSession());
		}

		private Maybe<UserSession> openUserSession() {
			Maybe<Credentials> credentialsMaybe = findCredentials(request);

			if (credentialsMaybe.isUnsatisfied())
				return Maybe.empty(credentialsMaybe.whyUnsatisfied());

			Credentials credentials = credentialsMaybe.get();

			return openUserSession(credentials);
		}

		private Maybe<UserSession> openUserSession(Credentials credentials) {
			try {
				OpenUserSession vus = OpenUserSession.T.create();
				vus.setCredentials(credentials);

				EvalContext<? extends OpenUserSessionResponse> vusResponseContext = vus.eval(requestEvaluator);
				Maybe<? extends OpenUserSessionResponse> openUserSessionMaybe = vusResponseContext.getReasoned();

				if (openUserSessionMaybe.isUnsatisfiedBy(AuthenticationFailure.T)) {
					onSessionNotFound();
				}

				if (openUserSessionMaybe.isUnsatisfied()) {
					return Maybe.empty(openUserSessionMaybe.whyUnsatisfied());
				}

				return Maybe.complete(openUserSessionMaybe.get().getUserSession());

			} catch (Exception e) {
				String uuid = UUID.randomUUID().toString();
				String msg = "Exception while opening usersession with credentials (context=" + uuid + ").";
				log.error(msg + " [" + credentials + "]", e);
				return InternalError.from(e, msg).asMaybe();
			}
		}

		private void onSessionNotFound() {
			cookieHandler.invalidateCookie(request, response, null);
		}

		private void setCookieIfNeccessary(UserSession userSession) {
			String sessionCookie = sessionCookieProvider.apply(request);
			if (sessionCookie != null) {
				log.trace(() -> "Session cookie already set. No need to set it at this point again.");
				return;
			}
			log.trace(() -> "Allowing to set the session cookie.");

			cookieHandler.ensureCookie(request, response, userSession.getSessionId());
		}

	}

	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		new StatefulAuthFilter(request, response, chain).doFilter();
	}

	/**
	 * @param userSession
	 *            the UserSession from which the thread name will be build or null to see a name with unauthorized mark
	 * @return
	 */
	private String threadNamePart(UserSession userSession) {

		if (userSession != null) {
			try {
				String sessionId = userSession.getSessionId();
				int l = sessionId.length();
				if (l > 5) {
					String s = sessionId.substring(0, 2).concat("-").concat(sessionId.substring(l - 2, l));
					sessionId = s;
				}
				return userSession.getUser().getName() + ":" + sessionId;
			} catch (Exception e) {
				log.debug(() -> "Could not get user name from session", e);
				return "anonymous";
			}
		}

		return "unauthorized";
	}

	@Required
	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	@Configurable
	@Required
	public void setCookieHandler(CookieHandler cookieHandler) {
		this.cookieHandler = cookieHandler;
	}
	@Configurable
	public void setSessionCookieProvider(Function<HttpServletRequest, String> sessionCookieProvider) {
		this.sessionCookieProvider = sessionCookieProvider;
	}
	@Configurable
	public void setThreadRenamer(ThreadRenamer threadRenamer) {
		Objects.requireNonNull(threadRenamer, "threadRenamer cannot be set to null");
		this.threadRenamer = threadRenamer;
	}
}
