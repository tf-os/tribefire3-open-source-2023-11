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
package com.braintribe.model.processing.bootstrapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.braintribe.config.configurator.ClasspathConfigurator;
import com.braintribe.config.configurator.ConfiguratorContext;
import com.braintribe.logging.Logger;

public class ConfiguratorsInitializer implements ServletContextListener {

	private static final Logger logger = Logger.getLogger(ConfiguratorsInitializer.class);

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		runConfigurators(contextEvent.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		// intentionally left empty
	}

	protected void runConfigurators(ServletContext context) {
		try {
			(new ClasspathConfigurator(new ConfiguratorContext(context.getContextPath()))).configure();
		} catch (Throwable e) {
			logger.error("Failed to execute configurators: " + e.getMessage(), e);
		}
	}

}
