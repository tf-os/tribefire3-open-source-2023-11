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
package tribefire.platform.config;

import java.util.Arrays;

import com.braintribe.config.configurator.Configurator;
import com.braintribe.config.configurator.ConfiguratorException;
import com.braintribe.config.configurator.ConfiguratorPriority;
import com.braintribe.config.configurator.ConfiguratorPriority.Level;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;

// TODO extract to web-app (or tomcat platform, in which case it could only use catalina.base)
@ConfiguratorPriority(value=Level.high, order=-100) // Mark this configurator with high priority to ensure it's running as first configurator.
public class ContainerRootConfigurator implements Configurator {
	
	private static final Logger log = Logger.getLogger(ContainerRootConfigurator.class);
	
	private static final String[] envVars = new String[] { "catalina.base", "jboss_home", "weblogic.home" };
	
	@Override
	public void configure() throws ConfiguratorException {
		
		
		if (isAlreadyConfigured()) {
			return;
		}
		
		for (String env : envVars) {
			
			String value = getProperty(env);
			
			if (value != null) {
				
				if (log.isInfoEnabled()) {
					log.info("Container root detected with [ "+env+" ]: [ "+value+" ]");
				}
				
				TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR, value);
				
				return;
				
			} else if (log.isTraceEnabled()) {
				log.trace("Environment variable [ "+env+" ] not defined");
			}
		}
		
		if (log.isWarnEnabled()) {
			log.warn("No container root environment variable detected and no explicit container root configured. Setting container root to runtime directory. Inspected values: "+Arrays.asList(envVars));
			TribefireRuntime.setProperty(TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR, "");
		}
		
	}

	private static boolean isAlreadyConfigured() {
		String containerRootDir = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR);
		if (containerRootDir != null) {
			log.info("Container root directory is explicitly defined. Skip automatic detection. ("+TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR+"="+containerRootDir+") ");
			return true;
		}
		log.debug("No explicit container root directory configured ("+TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR+"). Trying to detect automatically.");
		return false;
	}
	
	private static String getProperty(String propertyName) {
		
		String value = System.getProperty(propertyName);
		
		if (value != null)
			return value;
		
		return System.getenv(propertyName);
		
	}

	@Override
	public String toString() {
		String containerRootDir = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_CONTAINER_ROOT_DIR);
		return "ContainerRootConfigurator ("+(containerRootDir != null ? containerRootDir : "<not yet set>")+")";
	}
}
