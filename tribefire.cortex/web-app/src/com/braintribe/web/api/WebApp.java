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
package com.braintribe.web.api;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import com.braintribe.logging.Logger;
import com.braintribe.web.api.registry.FilterMapping;
import com.braintribe.web.api.registry.FilterRegistration;
import com.braintribe.web.api.registry.MultipartConfig;
import com.braintribe.web.api.registry.ServletNamesFilterMapping;
import com.braintribe.web.api.registry.ServletRegistration;
import com.braintribe.web.api.registry.UrlPatternFilterMapping;
import com.braintribe.web.api.registry.WebRegistry;
import com.braintribe.web.api.registry.WebsocketEndpointRegistration;
import com.braintribe.web.api.util.AppTools;

/**
 * <p>
 * A {@link ServletContextListener} which deploys {@link Servlet}, {@link Filter} and other
 * {@link ServletContextListener} instances as obtained by a {@link WebRegistry} provided by its specializations.
 * 
 * @author dirk.scheffler
 */
public abstract class WebApp implements ServletContextListener {

	private static final Logger logger = Logger.getLogger(WebApp.class);

	protected List<ServletContextListener> configuredContextListeners;
	private WebAppInfo webAppInfo;

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		ServletContext context = contextEvent.getServletContext();
		publishServletContext(context);
		deployWebRegistry(contextEvent);
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {

		String contextPath = AppTools.getContextPath(contextEvent);

		if (configuredContextListeners != null) {
			
			int size = this.configuredContextListeners.size();

			logger.debug("Context "+contextPath+" gets destroyed. Informing "+size+" configured servlet context listeners.");

			for (int i = size-1; i >= 0; --i) {
				ServletContextListener scl = this.configuredContextListeners.get(i);
				if (scl != null) {
					try {
						logger.debug("Informing configured servlet context listener: "+scl);
						scl.contextDestroyed(contextEvent);
						logger.debug("Informed configured servlet context listener: "+scl);
					} catch(Exception e) {
						logger.error("Error while informing configured ServletContextListener "+scl+" about destroyed context: "+contextPath, e);
					}
				}
			}
			
		} else {
			logger.debug("Context "+contextPath+" gets destroyed. No servlet context listeners to be informed.");
		}

	}

	/**
	 * <p>
	 * Provides the {@link WebRegistry} to be handled by this {@code WebApp}.
	 * 
	 * @return The {@link WebRegistry} to be handled by this {@code WebApp}.
	 */
	protected abstract WebRegistry provideConfiguration();

	protected void publishServletContext(ServletContext context) {
		WebApps.publishServletContext(context);
	}

	protected WebAppInfo info(ServletContext servletContext) {

		if (webAppInfo != null) {
			return webAppInfo;
		}

		String servletContextName = servletContext.getServletContextName();
		if (servletContextName == null) {
			servletContextName = this.getClass().getSimpleName() + " (unknown)";
		}

		String contextPath = servletContext.getContextPath();
		if (contextPath == null) {
			contextPath = UUID.randomUUID().toString();
		} else {
			contextPath = contextPath.replace("/", "");
		}

		webAppInfo = new WebAppInfo();
		webAppInfo.setAppName(servletContextName);
		webAppInfo.setContextPath(contextPath);

		return webAppInfo;

	}

	protected void deployWebRegistry(ServletContextEvent contextEvent) {

		boolean debug = logger.isDebugEnabled();

		long start = 0;
		WebAppInfo info = null;
		if (debug) {
			start = System.currentTimeMillis();
			info = info(contextEvent.getServletContext());
		}

		WebRegistry webRegistry = provideConfiguration();

		if (debug) logger.debug("Loading web registry for "+info+" has finished in "+(System.currentTimeMillis()-start)+" ms");

		if (debug) logger.debug("Deploying web registry components for "+info);

		deployEventListeners(contextEvent, webRegistry, debug);

		deployFilters(contextEvent, webRegistry, debug);

		deployServlets(contextEvent, webRegistry, debug);
		
		deployWebsocketEndpoints(contextEvent, webRegistry, debug);

		if (debug) logger.debug("Web registry load and deployment for "+info+" has finished in "+(System.currentTimeMillis()-start)+" ms");

	}

	protected void deployEventListeners(ServletContextEvent contextEvent, WebRegistry webRegistry, boolean debug) {
		if (debug) logger.debug("Processing configured event listeners.");
		List<EventListener> eventListeners = webRegistry.getListeners();
		if (eventListeners != null) {
			if (debug) logger.debug("Registering "+eventListeners.size()+" listeners.");

			if (configuredContextListeners == null) {
				configuredContextListeners = new ArrayList<>(eventListeners.size());
			}

			for (EventListener listener : eventListeners) {
				if (listener instanceof ServletContextListener) {
					if (debug) logger.debug("Listener "+listener+" is a servlet context listener.");
					ServletContextListener contextListener = (ServletContextListener) listener;
					this.configuredContextListeners.add(contextListener);
					contextListener.contextInitialized(contextEvent);
				} else {
					if (debug) logger.debug("Listener "+listener+" is not a servlet context listener.");
					contextEvent.getServletContext().addListener(listener);
				}
			}
		} else {
			if (debug) logger.debug("No listeners configured.");
		}
	}

	private void deployServlets(ServletContextEvent contextEvent, WebRegistry webRegistry, boolean debug) {
		if (debug) logger.debug("Processing servlets.");
		List<ServletRegistration> servlets = webRegistry.getServlets();
		if (servlets != null) {
			if (debug) logger.debug("Registering "+servlets.size()+" servlet(s).");	
			ServletContext servletContext = contextEvent.getServletContext();
			for (ServletRegistration servlet : servlets) {

				String servletName = servlet.getName();
				if (debug) logger.debug("Working on servlet "+servletName);
				javax.servlet.ServletRegistration existingRegistration = servletContext.getServletRegistration(servletName);
				if (existingRegistration != null) {
					logger.info("A servlet with the name "+servletName+" already exists.");
				} else {
					if (debug) logger.debug("Servlet "+servletName+" has not yet been registered.");

					if (servlet.getServlet() == null && servlet.getServletClass() == null) {
						logger.warn("Registration for servlet [ " + servletName + " ] has no instance nor class set and will be ignored: " + servlet);
						continue;
					}

					javax.servlet.ServletRegistration.Dynamic servletDynamic = null;

					if (servlet.getServlet() != null) {
						servletDynamic = servletContext.addServlet(servletName, servlet.getServlet());
					} else {
						servletDynamic = servletContext.addServlet(servletName, servlet.getServletClass());
					}

					if (servletDynamic != null) {
						if (debug) logger.debug("Got a ServletDynamic object for "+servletName+". Setting parameters and mappings ("+servlet.getMappings()+") now.");
						if (debug) logger.debug("Setting init parameters: "+servlet.getInitParameters());
						servletDynamic.setInitParameters(servlet.getInitParameters());

						servletDynamic.setLoadOnStartup(servlet.getLoadOnStartup());

						if (servlet.getRunAsRole() != null) {
							servletDynamic.setRunAsRole(servlet.getRunAsRole());
						}

						MultipartConfig multiPartConfig = servlet.getMultipartConfig();
						if (multiPartConfig != null) {
							String location = null;
							if (multiPartConfig.getLocation() != null) {
								location = multiPartConfig.getLocation().getAbsolutePath();
							}
							MultipartConfigElement element = new MultipartConfigElement(location, multiPartConfig.getMaxFileSize(), 
									multiPartConfig.getMaxRequestSize(), multiPartConfig.getFileSizeThreshold());
							servletDynamic.setMultipartConfig(element);
						}

						servletDynamic.setAsyncSupported(servlet.isAsyncSupported());

						servletDynamic.addMapping(servlet.getMappingsArray());

					} else {
						throw new WebAppException("Could not obtain a ServletDynamic for "+servletName);
					}
				}
			}
		} else {
			if (debug) logger.debug("No servlets configured.");
		}
	}

	private void deployFilters(ServletContextEvent contextEvent, WebRegistry webRegistry, boolean debug) {

		if (debug) logger.debug("Processing filters.");

		List<FilterRegistration> filters = webRegistry.getFilters();
		if (filters != null) {
			if (debug) logger.debug("Registering "+filters.size()+" filter(s).");
			ServletContext servletContext = contextEvent.getServletContext();
			for (FilterRegistration filter : filters) {

				String filterName = filter.getName();
				if (debug) logger.debug("Registering filter "+filterName);

				javax.servlet.FilterRegistration existingRegistration = servletContext.getFilterRegistration(filterName);
				if (existingRegistration != null) {
					logger.info("A filter with the name "+filterName+" already exists.");
				} else {
					if (debug) logger.debug("Filter "+filterName+" has not yet been registered.");

					if (filter.getFilter() == null && filter.getFilterClass() == null) {
						logger.warn("Registration for filter [ " + filterName + " ] has no instance nor class set and will be ignored: " + filter);
						continue;
					}

					javax.servlet.FilterRegistration.Dynamic filterDynamic = null;

					if (filter.getFilter() != null) {
						filterDynamic = servletContext.addFilter(filterName, filter.getFilter());
					} else {
						filterDynamic = servletContext.addFilter(filterName, filter.getFilterClass());
					}

					if (filterDynamic != null) {
						if (debug) logger.debug("Got a FilterDynamic object for "+filterName+". Setting parameters and mappings ("+filter.getMappings()+") now.");

						filterDynamic.setInitParameters(filter.getInitParameters());

						filterDynamic.setAsyncSupported(filter.isAsyncSupported());

						for (FilterMapping mapping : filter.getMappings()) {
							if (mapping instanceof ServletNamesFilterMapping) {
								if (debug) logger.debug("Registering serlvet name mapping "+mapping);
								ServletNamesFilterMapping snfm = (ServletNamesFilterMapping) mapping;
								filterDynamic.addMappingForServletNames(snfm.getDispatcherTypes(), snfm.isMatchAfter(), snfm.getNamesArray());
							} else if (mapping instanceof UrlPatternFilterMapping) {
								if (debug) logger.debug("Registering serlvet URL pattern mapping "+mapping);
								UrlPatternFilterMapping upfm = (UrlPatternFilterMapping) mapping;
								filterDynamic.addMappingForUrlPatterns(upfm.getDispatcherTypes(), upfm.isMatchAfter(), upfm.getUrlPatternsArray());
							} else {
								logger.warn("Unsupported FilterMapping: "+mapping);
							}
						}
					} else {
						throw new WebAppException("Could not obtain a FilterDynamic for "+filterName);
					}
				}
			}
		} else {
			if (debug) logger.debug("No filters configured.");
		}
	}
	
	protected void deployWebsocketEndpoints(ServletContextEvent contextEvent, WebRegistry webRegistry, boolean debug) {
		if (debug) logger.debug("Processing configured websocket endpoints.");
		List<WebsocketEndpointRegistration> endpoints = webRegistry.getWebsocketEndpoints();
		if (endpoints != null) {
			if (debug) logger.debug("Registering "+endpoints.size()+" websocket endpoints.");

			for (WebsocketEndpointRegistration endpointRegisration : endpoints) {
				
				Endpoint actualEndpoint = endpointRegisration.getEndpoint();
				String path = endpointRegisration.getPath();
				if (!path.startsWith("/")) {
					path = "/"+path;
				}
				
				ServerContainer container = (ServerContainer) contextEvent.getServletContext().getAttribute(ServerContainer.class.getName());
				ServerEndpointConfig sec = ServerEndpointConfig.Builder.create(Endpoint.class, path).configurator(new Configurator() {
					@SuppressWarnings("unchecked")
					@Override
					public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
						return (T) actualEndpoint;
					}
				}).build();
				
				
				try {
					container.addEndpoint(sec);
					logger.info("Deployed websocket endpoint '"+actualEndpoint.getClass()+" on path: "+endpointRegisration.getPath());
				} catch (DeploymentException e) {
					logger.error("Could not deploye websocket endpoint '"+actualEndpoint.getClass()+" on path: "+endpointRegisration.getPath(),e);
				}

				
			}
		} else {
			if (debug) logger.debug("No websocket endpoints configured.");
		}
	}
	

}
