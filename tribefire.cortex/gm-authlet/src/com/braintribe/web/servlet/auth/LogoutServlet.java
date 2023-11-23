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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.Codec;
import com.braintribe.codec.string.MapCodec;
import com.braintribe.codec.string.UrlEscapeCodec;
import com.braintribe.exception.HttpException;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.service.api.aspect.RequestorSessionIdAspect;
import com.braintribe.model.securityservice.Logout;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.util.servlet.remote.DefaultRemoteClientAddressResolver;
import com.braintribe.util.servlet.remote.RemoteClientAddressResolver;
import com.braintribe.utils.collection.impl.AttributeContexts;

public class LogoutServlet extends HttpServlet {

	private static final long serialVersionUID = -3371378397236984055L;
	private Logger log = Logger.getLogger(LogoutServlet.class);

	private Evaluator<ServiceRequest> requestEvaluator;

	private String relativeLoginPath = "login";

	private String logoutMessage = "Successfully logged out";
	private Codec<Map<String, String>, String> urlParamCodec;

	private RemoteClientAddressResolver remoteAddressResolver;

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Configurable
	public void setUrlParamCodec(Codec<Map<String, String>, String> urlParamCodec) {
		this.urlParamCodec = urlParamCodec;
	}

	public Codec<Map<String, String>, String> getUrlParamCodec() {
		if (urlParamCodec == null) {
			MapCodec<String, String> mapCodec = new MapCodec<String, String>();
			mapCodec.setEscapeCodec(new UrlEscapeCodec());
			mapCodec.setDelimiter("&");
			this.urlParamCodec = mapCodec;
		}
		return urlParamCodec;
	}

	@Configurable
	public void setLogoutMessage(String logoutMessage) {
		this.logoutMessage = logoutMessage;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String sessionId = AttributeContexts.peek().findOrNull(RequestorSessionIdAspect.class);
		try {
			if (sessionId != null) {
				Boolean logoutSuccess = Boolean.TRUE;
				try {

					Logout logout = Logout.T.create();
					logout.setSessionId(sessionId);
					EvalContext<Boolean> logoutResponseContext = logout.eval(requestEvaluator);
					logoutSuccess = logoutResponseContext.get();
				} catch (Exception e) {
					log.debug("Error while trying to log out session: " + sessionId, e);
				}

				log.debug("Successfully logged out session: " + sessionId + ": " + logoutSuccess);
			}

			String currentLocation = req.getParameter("currentLocation");
			if (currentLocation != null) {
				log.trace(() -> "Current location: " + currentLocation);
			}

			Map<String, String> queryParams = new HashMap<String, String>();
			queryParams.put(Constants.REQUEST_PARAM_MESSAGE, logoutMessage);
			queryParams.put(Constants.REQUEST_PARAM_MESSAGESTATUS, Constants.REQUEST_VALUE_MESSAGESTATUS_OK);
			String continueParam = req.getParameter(Constants.REQUEST_PARAM_CONTINUE);
			if (continueParam != null) {
				queryParams.put(Constants.REQUEST_PARAM_CONTINUE, continueParam);
			}

			String paramString = getUrlParamCodec().encode(queryParams);
			String loginPath = relativeLoginPath.contains("?") ? relativeLoginPath.concat("&").concat(paramString)
					: relativeLoginPath.concat("?").concat(paramString);
			resp.sendRedirect(loginPath);
		} catch (Exception e) {
			// ErrorRenderer errorRenderer = new ErrorRenderer();
			throw new HttpException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not logout session: " + sessionId, e);
		}
	}

	public static String buildCurrentRelativePath(HttpServletRequest request, Logger logger) {
		String requestURI = request.getRequestURI();
		String pathInfo = request.getPathInfo();
		logger.trace(() -> "Build relative base path based on request URI: " + request.getRequestURI() + " and pathInfo: " + pathInfo);
		System.out.println("Build relative base path based on request URI: " + request.getRequestURI() + " and pathInfo: " + pathInfo);

		String basePath = requestURI;
		if (pathInfo != null) {
			basePath = requestURI.substring(0, requestURI.length() - pathInfo.length());
		}
		String result = basePath;
		logger.trace(() -> "Calculated relative base path to: " + result);
		System.out.println("Calculated relative base path to: " + result);
		return result;
	}

	public static String buildCurrentServicesPath(HttpServletRequest request, String pathSuffix, Logger logger) {
		String basePath = buildCurrentRelativePath(request, logger);
		String servicesPath = basePath.substring(0, basePath.length() - pathSuffix.length());
		logger.trace(() -> "Calculated servicePath to: " + servicesPath);
		System.out.println("Calculated servicePath to: " + servicesPath);
		return servicesPath;
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

	@Configurable
	public void setRelativeLoginPath(String relativeLoginPath) {
		this.relativeLoginPath = relativeLoginPath;
	}
}
