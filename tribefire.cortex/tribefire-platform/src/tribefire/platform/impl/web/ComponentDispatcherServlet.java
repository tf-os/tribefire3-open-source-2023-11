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
package tribefire.platform.impl.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.AuthorizableWebTerminal;
import com.braintribe.model.extensiondeployment.AuthorizedWebTerminal;
import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.lcd.CollectionTools;

public class ComponentDispatcherServlet extends HttpServlet {

	// constants
	private static final long serialVersionUID = -2104788170308353779L;
	private static final Logger log = Logger.getLogger(ComponentDispatcherServlet.class);

	// instance-managed
	private Map<String, DeployedWebTerminal> delegates = new ConcurrentHashMap<>();

	private int pathKeyLevelCount = 1;

	public ComponentDispatcherServlet() {
	}

	@Override
	public void init() throws ServletException {
		super.init();
		for (DeployedWebTerminal delegate : delegates.values()) {
			delegate.servlet.init();
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		for (DeployedWebTerminal delegate : delegates.values()) {
			delegate.servlet.init(config);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null)
			throw new ServletException("The request does not contain valid path info. pathInfo is null");
		else if (pathInfo.startsWith("/"))
			// remove preceding "/" from path info
			pathInfo = pathInfo.substring(1);

		if (pathInfo.trim().length() == 0) {
			response.sendError(404, "Required servlet path is missing");
			return;
		}

		int idx = 0;

		for (int i = 0; i < this.pathKeyLevelCount && idx != -1; i++) {
			if (pathInfo.charAt(idx) == '/') {
				idx += 1;
			}
			idx = pathInfo.indexOf('/', idx);
		}

		String key = pathInfo;
		if (idx != -1) {
			key = pathInfo.substring(0, idx);
			pathInfo = pathInfo.substring(idx, pathInfo.length());
		} else {
			pathInfo = null;
		}

		DeployedWebTerminal delegate = delegates.get(key);

		if (delegate == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {

			final int status;

			if (delegate.isAuthorizable) {
				UserSession userSession = AttributeContexts.peek().findOrNull(UserSessionAspect.class);

				if (userSession == null) {
					status = delegate.isAuthorized ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_OK;
				} else {
					if (delegate.roles.isEmpty()) {
						status = HttpServletResponse.SC_OK;
					} else {
						status = CollectionTools.containsAny(delegate.roles, userSession.getEffectiveRoles()) ? HttpServletResponse.SC_OK
								: HttpServletResponse.SC_FORBIDDEN;
					}
				}
			} else {
				status = HttpServletResponse.SC_OK;
			}

			if (status == HttpServletResponse.SC_OK) {
				HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapperImpl(request, pathInfo);
				delegate.servlet.service(requestWrapper, response);
			} else {
				response.sendError(status);
			}
		}
	}

	private static class HttpServletRequestWrapperImpl extends HttpServletRequestWrapper {

		private String pathInfo;

		public HttpServletRequestWrapperImpl(HttpServletRequest request, String pathInfo) {
			super(request);
			this.pathInfo = pathInfo;
		}

		@Override
		public String getPathInfo() {
			return this.pathInfo;
		}
	}

	public void registerAndInitServlet(WebTerminal webTerminal, HttpServlet servlet) {

		String path = webTerminal.getPathIdentifier();

		if (path == null || path.trim().length() == 0) {
			throw new DeploymentException("The deployable " + webTerminal + " does not contain a mandatory path identifier.");
		}

		try {
			servlet.init(createServletConfig(webTerminal));
		} catch (Exception e) {
			log.error("init() failed for " + servlet + ": " + e.getMessage(), e);
		}

		delegates.put(path, new DeployedWebTerminal(webTerminal, servlet));
	}

	public void unregisterAndDestroy(WebTerminal webTerminal) {

		String path = webTerminal.getPathIdentifier();
		DeployedWebTerminal removedServlet = delegates.remove(path);

		// At this point the component can be null.
		// i.e.: an extension point being notified about a deployment it didn't create proxies for.
		if (removedServlet == null) {
			log.trace(() -> "Servlet had no delegate for path [ " + path + " ]");
		} else {

			try {
				removedServlet.servlet.destroy();
			} catch (Exception e) {
				log.error("destroy() failed for " + removedServlet + ": " + e.getMessage(), e);
			}

			log.debug(() -> "Removed delegate for [ " + path + " ]: " + removedServlet);
		}
	}

	protected ServletConfig createServletConfig(WebTerminal webTerminal) {

		final String servletName = webTerminal.entityType().getShortName() + "[" + webTerminal.getExternalId() + "]";

		return new ServletConfig() {

			@Override
			public String getInitParameter(String paramName) {

				Map<String, String> properties = webTerminal.getProperties();

				if (paramName == null || properties == null) {
					return null;
				}

				String initParam = webTerminal.getProperties().get(paramName);
				return initParam;

			}

			@Override
			public Enumeration<String> getInitParameterNames() {

				Map<String, String> properties = webTerminal.getProperties();

				if (properties == null) {
					return Collections.emptyEnumeration();
				}

				return Collections.enumeration(properties.keySet());

			}

			@Override
			public ServletContext getServletContext() {
				return getServletConfig().getServletContext();
			}

			@Override
			public String getServletName() {
				return servletName;
			}

		};
	}

	private static class DeployedWebTerminal {
		boolean isAuthorizable;
		boolean isAuthorized;
		Set<String> roles;
		HttpServlet servlet;

		DeployedWebTerminal(WebTerminal webTerminal, HttpServlet servlet) {
			this.servlet = servlet;

			if (webTerminal instanceof AuthorizableWebTerminal) {
				this.isAuthorizable = true;
				if (webTerminal instanceof AuthorizedWebTerminal) {
					this.isAuthorized = true;
				}

				this.roles = ((AuthorizableWebTerminal) webTerminal).getRoles();
			}
		}
	}
}
