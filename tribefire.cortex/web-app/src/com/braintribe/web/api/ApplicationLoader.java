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

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.braintribe.logging.Logger;
import com.braintribe.web.api.util.AppTools;

/**
 * This is the main servlet context listener that is responsible for loading other servlet context listeners. This helps to keep the web.xml files of
 * cartridges at a minimum. This is the only servlet context listener that is necessary. It is also used by the MasterCartridge to bootstrap the
 * services.
 * <p>
 * The list of underlying servlet context listeners that should be invoked here is obtained by getting tribefire/app/web/servletContextListeners.txt
 * from the classloader(s). There could be multiple files available, so all of them are included. Usually, just a single resource is (or, should) be
 * available.
 * <p>
 * Both the CartridgeBase and the MasterCartridgeBase include such a file. It is also possible to create the file /WEB-INF/servletContextListeners.txt
 * to include additional servlet context listeners.
 * <p>
 * Since it is not allowed to add new context listeners once one of them has been executed (which is the case when this listener comes into action),
 * this class takes also care that the events are properly forwarded to the other context listeners.
 */
public class ApplicationLoader implements ServletContextListener {

	private static final Logger logger = Logger.getLogger(ApplicationLoader.class);

	protected List<ServletContextListener> lowLevelServletContextListeners = new ArrayList<ServletContextListener>();

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

		String contextPath = AppTools.getContextPath(sce);
		logger.pushContext("ApplicationLoader:" + contextPath + "#destroy");
		try {
			int size = lowLevelServletContextListeners.size();
			logger.debug(() -> "Informing " + size + " low-level servlet context listeners.");

			for (int i = size - 1; i >= 0; --i) {
				ServletContextListener scl = lowLevelServletContextListeners.get(i);
				if (scl == null)
					continue;

				try {
					logger.debug(() -> "Informing low-level servlet context listener: " + scl);
					scl.contextDestroyed(sce);
					logger.debug(() -> "Informed low-level servlet context listener: " + scl);
				} catch (Exception e) {
					logger.error("Error while informing low-level ServletContextListener " + scl + " about destroyed context: " + contextPath, e);
				}
			}

		} finally {
			lowLevelServletContextListeners.clear();
			logger.popContext();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String contextPath = AppTools.getContextPath(sce);
		logger.pushContext("ApplicationLoader:" + contextPath + "#initialize");
		try {
			loadLowLevelListeners(sce);
		} finally {
			logger.popContext();
		}
	}

	/**
	 * Loads all servlet context listeners that are specified in either the classloader resources tribefire/app/web/servletContextListeners.txt or
	 * /WEB-INF/servletContextListeners.txt.
	 * 
	 * @param sce
	 *            The servlet context event that is forwarded to the other servlet context listeners.
	 * @throws WebAppException
	 *             Thrown if any of the underlying servlet context listeners threw an exception.
	 */
	protected void loadLowLevelListeners(ServletContextEvent sce) throws WebAppException {
		String name = "servletContextListeners.txt";

		logger.debug(() -> "Trying to find " + name + " either in WEB-INF or in the classpath.");

		ServletContext servletContext = sce.getServletContext();

		Set<URL> locations = new LinkedHashSet<URL>();

		try {
			logger.debug(() -> "Trying to find " + name + " in the classpath by use of classloaders.");
			Set<ClassLoader> classLoaders = new LinkedHashSet<ClassLoader>();
			classLoaders.add(getClass().getClassLoader());
			classLoaders.add(servletContext.getClassLoader());
			classLoaders.add(Thread.currentThread().getContextClassLoader());
			for (ClassLoader cl : classLoaders) {
				logger.debug(() -> "Trying classloader: " + cl);
				if (cl != null) {
					Enumeration<URL> urls = cl.getResources("tribefire/app/web/" + name);
					if (urls != null) {
						if (urls.hasMoreElements()) {
							logger.debug("Found some URLs.");
						} else {
							logger.debug(() -> "Could not find any tribefire/app/web/" + name + " in the classpath.");
						}
						while (urls.hasMoreElements()) {
							URL url = urls.nextElement();
							logger.debug(() -> "Found url " + url);
							locations.add(url);
						}

					} else {
						logger.debug(() -> "The classloader did not return any URL for " + name);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not get tribefire/app/web/" + name, e);
		}

		try {
			URL url = servletContext.getResource("/WEB-INF/" + name);
			if (url != null) {
				logger.debug(() -> "Found url " + url);
				locations.add(url);

			} else {
				logger.debug(() -> "Could not find " + name + " in /WEB-INF/");
			}

		} catch (MalformedURLException e) {
			logger.error("Could not get WEB-INF/" + name, e);
		}

		logger.debug(() -> "Loading the following URLs: " + locations);

		Collection<String> listeners = new LinkedHashSet<>();

		for (URL url : locations) {
			logger.debug(() -> "Loading " + url);
			try {
				List<String> lines = AppTools.getContentLines(url);
				if (!isEmpty(lines)) {
					listeners.addAll(lines);
					logger.debug(() -> "Read " + lines.size() + " listener(s) from " + url + ": " + lines);

				} else {
					logger.warn("No listener was read from " + url);
				}

			} catch (Exception e) {
				logger.error("Could not read from URL " + url, e);
			}
		}

		for (String line : listeners) {
			logger.debug(() -> "Loading low level listener class: " + line);

			Class<?> cls = null;
			Object listener = null;

			try {
				cls = Class.forName(line);
			} catch (Throwable t) {
				throw new WebAppException("Could not load low-level listener class: " + line, t);
			}

			try {
				listener = cls.getDeclaredConstructor().newInstance();
			} catch (Throwable t) {
				throw new WebAppException("Could not instantiate low-level listener class: " + line, t);
			}

			try {
				if (listener instanceof ServletContextListener) {
					ServletContextListener scl = (ServletContextListener) listener;
					lowLevelServletContextListeners.add(scl);
					scl.contextInitialized(sce);
				} else {
					logger.warn("The class " + line + " is not of type " + ServletContextListener.class.getName() + ". Ignoring this class.");
				}
			} catch (Throwable t) {
				throw new WebAppException("Context initialization failed for listener class: " + line, t);
			}

		}

	}

}
