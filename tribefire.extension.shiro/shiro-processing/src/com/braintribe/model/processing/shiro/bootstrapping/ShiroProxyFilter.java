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
package com.braintribe.model.processing.shiro.bootstrapping;

import java.io.IOException;
import java.util.function.Supplier;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.servlet.ShiroFilter;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.shiro.ShiroConstants;
import com.braintribe.servlet.ProxyAwareHttpServletRequest;
import com.braintribe.util.servlet.util.ServletTools;
import com.braintribe.utils.StringTools;

/**
 * Delegating filter that only forwards requests to the ShiroFilter after it has been activated. This is done by the
 * {@link BootstrappingWorker} after the configuration was read from the tribefire services.
 */
public class ShiroProxyFilter implements Filter {

	private static Logger logger = Logger.getLogger(ShiroProxyFilter.class);

	private ShiroFilter delegate;
	private Supplier<ShiroFilter> delegateSupplier;
	private FilterConfig filterConfig;
	private boolean activated = false;
	private String fallbackUrl = null;

	@Configurable
	public void setFallbackUrl(String fallbackUrl) {
		this.fallbackUrl = fallbackUrl;
	}

	public void setDelegateSupplier(Supplier<ShiroFilter> delegateSupplier) {
		this.delegateSupplier = delegateSupplier;
	}

	private ShiroFilter getDelegate() {
		if (delegate == null) {
			delegate = delegateSupplier.get();
			try {
				delegate.init(filterConfig);
			} catch (ServletException e) {
				throw new RuntimeException("Could not initialize delegate filter with config: " + filterConfig, e);
			}
		}
		return delegate;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		if (activated) {
			if (logger.isTraceEnabled()) {
				SecurityManager commonManager = null;
				try {
					commonManager = SecurityUtils.getSecurityManager();
				} catch (Exception e) {
					logger.trace("Could not get common SecurityManager.");
				}
				SecurityManager delegateManager = getDelegate().getSecurityManager();

				StringBuilder sb = new StringBuilder("\n");
				sb.append("Common SM: " + commonManager + "\n");
				sb.append("Filter SM: " + delegateManager + "\n");
				if (delegateManager instanceof DefaultSecurityManager) {
					DefaultSecurityManager dsm = (DefaultSecurityManager) delegateManager;
					SessionManager sessionManager = dsm.getSessionManager();
					sb.append("Session Manager: " + sessionManager + "\n");
					if (sessionManager instanceof DefaultSessionManager) {
						DefaultSessionManager dsessm = (DefaultSessionManager) sessionManager;
						sb.append("SessionDAO: " + dsessm.getSessionDAO() + "\n");
					}
				}
				logger.trace("\n" + StringTools.asciiBoxMessage(sb.toString(), -1));
			}
			try {
				boolean delegated = false;
				if (request instanceof HttpServletRequest) {
					HttpServletRequest servletRequest = (HttpServletRequest) request;
					final String pathInfo = servletRequest.getPathInfo();
					if (pathInfo != null) {
						String lowerPathInfo = pathInfo.toLowerCase();
						if (lowerPathInfo.contains(ShiroConstants.PATH_IDENTIFIER)) {
							logger.debug(() -> "PathInfo: " + pathInfo + " indicates access to Shiro Module. Wrapping Request.");
							delegated = true;
							ProxyAwareHttpServletRequest wrappedRequest = new ProxyAwareHttpServletRequest(TribefireRuntime.getPublicServicesUrl(),
									servletRequest);
							getDelegate().doFilter(wrappedRequest, response, chain);
						}
					}
				}
				if (!delegated) {
					getDelegate().doFilter(request, response, chain);
				}
			} catch (ServletException se) {
				Throwable cause = se.getCause();
				String seMessage = se.getMessage();
				String causeMessage = cause.getMessage();
				String message = "Error while processing request: " + ServletTools.stringify(request);

				if (!StringTools.isBlank(fallbackUrl) && response instanceof HttpServletResponse) {

					if (cause instanceof RuntimeException) {
						if (StringTools.isAllBlank(seMessage, causeMessage) || seMessage == null || seMessage.contains(causeMessage)) {
							logger.info(() -> message, cause);
						} else {
							logger.info(() -> message, se);
						}
					} else {
						logger.info(() -> message, se);
					}

					HttpServletResponse sr = (HttpServletResponse) response;
					sr.sendRedirect(fallbackUrl);
					return;
				}

				if (cause instanceof RuntimeException) {
					if (StringTools.isAllBlank(seMessage, causeMessage) || seMessage == null || seMessage.contains(causeMessage)) {
						throw (RuntimeException) cause;
					}
				}
				throw se;
			}
		} else {
			chain.doFilter(request, response);
		}

	}

	@Override
	public void destroy() {
		if (delegate != null) {
			delegate.destroy();
		}
	}

	public void activate() {
		activated = true;
	}
}
