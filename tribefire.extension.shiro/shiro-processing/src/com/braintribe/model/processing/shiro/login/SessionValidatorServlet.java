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
package com.braintribe.model.processing.shiro.login;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.api.exceptions.SessionNotFoundException;
import com.braintribe.model.securityservice.ValidateUserSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.util.servlet.util.ServletTools;
import com.braintribe.utils.StringTools;

public class SessionValidatorServlet extends HttpServlet {

	private static final long serialVersionUID = -1L;

	public static final String APPLICATION_JSON = "application/json";

	private static Logger logger = Logger.getLogger(SessionValidatorServlet.class);

	protected Evaluator<ServiceRequest> requestEvaluator;
	protected MarshallerRegistry marshallerRegistry;

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Cookie[] cookies = request.getCookies();

		String cookieName = ServletTools.getSingleParameter(request, "cname", "tfsessionId");
		String returnType = ServletTools.getSingleParameter(request, "type", "txt");
		boolean logout = ServletTools.getSingleParameter(request, "logout", "false").equalsIgnoreCase("true");
		if (logout) {
			logger.warn(() -> "Logout is no longer supported by this Servlet. Please use the standard REST endpoints for this.");
		}

		Pair<String, Marshaller> returnTypePair = getEffectiveMimeType(returnType);
		String mimeType = returnTypePair.getFirst();
		Marshaller marshaller = returnTypePair.getSecond();

		logger.debug(() -> "MIME type: " + mimeType + ", Cookie name: " + cookieName);
		response.setContentType(mimeType + ";charset=UTF-8");

		PrintWriter writer = response.getWriter();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equalsIgnoreCase(cookieName)) {

					String sessionId = cookie.getValue();
					UserSession userSession = null;
					try {
						userSession = getValidatedUserSession(sessionId);
					} catch (ServletException e) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
					} catch (SessionNotFoundException e) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
					}

					if (logout) {
						cookie.setMaxAge(0);
						cookie.setPath("/");
						response.addCookie(cookie);
					}

					StringBuilder responseBody = new StringBuilder();

					switch (mimeType) {
						case "text/plain":
							if (userSession != null && !logout) {
								responseBody.append(userSession.getSessionId());
							}
							break;
						default:
							if (!logout) {
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								marshaller.marshall(baos, userSession);
								responseBody.append(baos.toString("UTF-8"));
							}
							break;
					}

					writer.print(responseBody.toString());

				}

			}
		}

		writer.print("");

	}

	private UserSession getValidatedUserSession(String sessionId) throws ServletException {

		if (StringTools.isBlank(sessionId)) {
			throw new ServletException("No active user session found.");
		}
		ValidateUserSession validateSessionRequest = ValidateUserSession.T.create();
		validateSessionRequest.setSessionId(sessionId);
		EvalContext<UserSession> evalValidateUserSession = requestEvaluator.eval(validateSessionRequest);
		UserSession validatedUserSession = evalValidateUserSession.get();
		return validatedUserSession;
	}

	private Pair<String, Marshaller> getEffectiveMimeType(String returnSimpleTypeFromParameter) {

		String returnTypeFromParameter = null;
		if (returnSimpleTypeFromParameter != null) {
			switch (returnSimpleTypeFromParameter.toLowerCase()) {
				case "txt":
					return new Pair<>("text/plain", null);
				case "json": {
					returnTypeFromParameter = "application/json";
					Marshaller marshaller = marshallerRegistry.getMarshaller("application/json");
					if (marshaller != null) {
						return new Pair<>(returnTypeFromParameter, marshaller);
					}
					break;
				}
				case "xml": {
					returnTypeFromParameter = "application/xml";
					Marshaller marshaller = marshallerRegistry.getMarshaller("application/xml");
					if (marshaller != null) {
						return new Pair<>(returnTypeFromParameter, marshaller);
					}
					break;
				}
				default:
					logger.debug(() -> "Unexpected return type " + returnSimpleTypeFromParameter);
					returnTypeFromParameter = returnSimpleTypeFromParameter;
					Marshaller marshaller = marshallerRegistry.getMarshaller(returnTypeFromParameter);
					if (marshaller != null) {
						return new Pair<>(returnTypeFromParameter, marshaller);
					}
					break;
			}
		}

		return new Pair<>("text/plain", null);
	}

	@Required
	public void setMarshallerRegistry(MarshallerRegistry marshallerRegistry) {
		this.marshallerRegistry = marshallerRegistry;
	}
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

}
