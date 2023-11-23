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

import javax.servlet.ServletContext;

import org.apache.shiro.web.env.EnvironmentLoader;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;

/**
 * This class takes care of initializing the environment loader (via LifecycleAware mechanics). It also assigns the
 * custom session DAO.
 */
public class Bootstrapping implements LifecycleAware {

	private static Logger logger = Logger.getLogger(Bootstrapping.class);

	private ServletContext servletContext;
	private CustomEnvironmentLoader environmentLoaderListener;

	public void start() {

		logger.debug(() -> "Starting the initEnvironment method of the environmentLoaderListener: " + environmentLoaderListener);

		String ENVIRONMENT_ATTRIBUTE_KEY = EnvironmentLoader.class.getName() + ".ENVIRONMENT_ATTRIBUTE_KEY";
		servletContext.removeAttribute(ENVIRONMENT_ATTRIBUTE_KEY);

		environmentLoaderListener.initEnvironment(servletContext);

		logger.debug(() -> "Done with initializing the WebEnvironment");
	}

	public void stop() {
		logger.debug(() -> "Shutting down the WebEnvironment");
		environmentLoaderListener.destroyEnvironment(servletContext);
	}

	@Required
	@Configurable
	public void setEnvironmentLoaderListener(CustomEnvironmentLoader environmentLoaderListener) {
		this.environmentLoaderListener = environmentLoaderListener;
	}

	@Required
	@Configurable
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void postConstruct() {
		// Will be done by the Bootstrapping Worker
	}

	@Override
	public void preDestroy() {
		stop();
	}
}
