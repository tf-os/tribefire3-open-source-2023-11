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
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.exception.HttpException;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.security.reason.SecurityReason;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.service.api.aspect.RequestedEndpointAspect;
import com.braintribe.model.processing.service.api.aspect.RequestorAddressAspect;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.util.servlet.remote.DefaultRemoteClientAddressResolver;
import com.braintribe.util.servlet.remote.RemoteAddressInformation;
import com.braintribe.util.servlet.remote.RemoteClientAddressResolver;
import com.braintribe.utils.collection.impl.AttributeContexts;

public class AuthServlet extends HttpServlet {

	private static final long serialVersionUID = -3371378397236984055L;

	public final static String TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED = "TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED";

	private Logger log = Logger.getLogger(AuthServlet.class);

	private RemoteClientAddressResolver remoteAddressResolver;
	private MarshallerRegistry marshallerRegistry;
	private CookieHandler cookieHandler;
	private Evaluator<ServiceRequest> requestEvaluator;

	@Configurable
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}

	@Configurable
	public void setRemoteAddressResolver(RemoteClientAddressResolver remoteAddressResolver) {
		this.remoteAddressResolver = remoteAddressResolver;
	}
	public RemoteClientAddressResolver getRemoteAddressResolver() {
		if (remoteAddressResolver == null) {
			remoteAddressResolver = DefaultRemoteClientAddressResolver.getDefaultResolver();
		}
		return remoteAddressResolver;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Create authentication request.

		AttributeContext attributeContext = buildAttributeContext(req);

		AttributeContexts.push(attributeContext);
		try {

			OpenUserSessionWithUserAndPassword authRequest = createOpenUserSessionRequest(req);
			Maybe<UserSession> sessionMaybe = authenticate(resp, authRequest);

			if (sessionMaybe.isUnsatisfied()) {

				Reason whyUnsatisfied = sessionMaybe.whyUnsatisfied();

				if (!sessionMaybe.isUnsatisfiedAny(SecurityReason.T, InvalidArgument.T)) {
					String logToken = UUID.randomUUID().toString();
					log.warn(logToken + ": " + whyUnsatisfied.stringify());
					whyUnsatisfied = Reasons.build(InternalError.T).text("Please check the log files and search for error ID: " + logToken)
							.toReason();
				}

				Marshaller marshaller = marshallerRegistry.getMarshaller("application/json");
				resp.setContentType("application/json");
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				marshaller.marshall(resp.getOutputStream(), whyUnsatisfied);
				return;
			}
			UserSession session = sessionMaybe.get();

			String sessionId = session.getSessionId();
			log.debug("Successfully authenticated user: " + authRequest.getUser() + " with session: " + sessionId);

			cookieHandler.ensureCookie(req, resp, sessionId, authRequest);

		} finally {
			AttributeContexts.pop();
		}
	}

	public Marshaller getMarshaller(HttpServletRequest request) {
		String contentType = request.getContentType();
		if (contentType == null) {
			contentType = "application/json";
		}
		String mimeType = getMimeType(contentType);
		Marshaller marshaller = marshallerRegistry.getMarshaller(mimeType);
		if (marshaller != null) {
			return marshaller;
		}
		throw new HttpException(HttpServletResponse.SC_NOT_ACCEPTABLE, "Unsupported Content-Type.");
	}

	private static String getMimeType(String requestContentType) {

		if (requestContentType == null)
			return requestContentType;

		if (requestContentType.indexOf(";") == -1)
			return requestContentType;

		return requestContentType.substring(0, requestContentType.indexOf(";")).trim();
	}

	protected static boolean offerStaySigned() {
		String offerStayLoggedIn = TribefireRuntime.getProperty(TRIBEFIRE_RUNTIME_OFFER_STAYSIGNED);
		if (offerStayLoggedIn != null && offerStayLoggedIn.equalsIgnoreCase("false")) {
			return false;
		} else {
			return true;
		}
	}

	private Maybe<UserSession> authenticate(@SuppressWarnings("unused") HttpServletResponse resp, OpenUserSessionWithUserAndPassword authRequest)
			throws AuthenticationException {

		EvalContext<? extends OpenUserSessionResponse> responseContext = authRequest.eval(requestEvaluator);
		Maybe<? extends OpenUserSessionResponse> reasonedResponse = responseContext.getReasoned();

		if (!reasonedResponse.isSatisfied()) {
			return reasonedResponse.whyUnsatisfied().asMaybe();
		}

		OpenUserSessionResponse response = reasonedResponse.get();

		return Maybe.complete(response.getUserSession());
	}

	protected OpenUserSessionWithUserAndPassword createOpenUserSessionRequest(HttpServletRequest request) {

		try (InputStream in = request.getInputStream()) {
			// 3. Unmarshall the request from the body
			Marshaller marshaller = getMarshaller(request);
			OpenUserSessionWithUserAndPassword authRequest = (OpenUserSessionWithUserAndPassword) marshaller.unmarshall(in,
					GmDeserializationOptions.deriveDefaults().setInferredRootType(OpenUserSessionWithUserAndPassword.T).build());

			if (authRequest == null) {
				throw new HttpException(HttpServletResponse.SC_BAD_REQUEST, "Could not decode credentials from request");
			}

			Locale locale = request.getLocale();
			if (locale != null) {
				authRequest.setLocale(locale.getLanguage());
			}

			String user = authRequest.getUser();
			RemoteClientAddressResolver resolver = getRemoteAddressResolver();
			try {
				RemoteAddressInformation remoteAddressInformation = resolver.getRemoteAddressInformation(request);
				String remoteAddress = remoteAddressInformation.getRemoteIp();
				log.info("Received an authentication request for user '" + user + "' from [" + remoteAddress + "]. Remote Address Information: "
						+ remoteAddressInformation.toString());
			} catch (Exception e) {
				String message = "Could not use the client address resolver to get the client's IP address. User: '" + user + "'";
				log.info(message);
				if (log.isDebugEnabled())
					log.debug(message, e);
			}

			return authRequest;

		} catch (IOException ioe) {
			throw new HttpException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not ready request body", ioe);
		}

	}

	/**
	 * <p>
	 * Retrieves the client's remote Internet protocol address.
	 * 
	 * @param request
	 *            The request from the client.
	 * @return The remote address of the client.
	 */
	private String getClientRemoteInternetAddress(HttpServletRequest request) {

		if (remoteAddressResolver == null) {
			remoteAddressResolver = DefaultRemoteClientAddressResolver.getDefaultResolver();
		}

		return remoteAddressResolver.getRemoteIpLenient(request);

	}

	protected AttributeContext buildAttributeContext(HttpServletRequest httpRequest) {
		return AttributeContexts.peek().derive().set(RequestedEndpointAspect.class, httpRequest.getRequestURL().toString())
				.set(RequestorAddressAspect.class, getClientRemoteInternetAddress(httpRequest)).build();
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

}
