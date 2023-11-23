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
package tribefire.platform.impl.initializer;

import static com.braintribe.wire.api.util.Sets.set;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.cortex.deployment.cors.CorsConfiguration;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.utils.StringTools;

/**
 * @author peter.gazdik
 */
public class CortexConfigurationInitializer extends SimplePersistenceInitializer {

	private static final Logger logger = Logger.getLogger(CortexConfigurationInitializer.class);

	public final static String TRIBEFIRE_CORS_ALLOWED_ORIGINS = "TRIBEFIRE_CORS_ALLOWED_ORIGINS";
	public final static String TRIBEFIRE_CORS_SUPPORTS_CREDENTIALS = "TRIBEFIRE_CORS_SUPPORTS_CREDENTIALS";
	public final static String TRIBEFIRE_CORS_SUPPORT_ANY_HEADER = "TRIBEFIRE_CORS_SUPPORT_ANY_HEADER";
	public final static String TRIBEFIRE_CORS_SUPPORTED_METHODS = "TRIBEFIRE_CORS_SUPPORTED_METHODS";
	public final static String TRIBEFIRE_CORS_SUPPORTED_HEADERS = "TRIBEFIRE_CORS_SUPPORTED_HEADERS";
	public final static String TRIBEFIRE_CORS_EXPOSED_HEADERS = "TRIBEFIRE_CORS_EXPOSED_HEADERS";

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		ManagedGmSession session = context.getSession();

		CorsConfiguration corsConfig = session.create(CorsConfiguration.T);
		corsConfig.setGlobalId("config:cors");

		List<String> origins = getAllowedOriginsFromEnvironment();
		if (origins != null && !origins.isEmpty()) {
			logger.debug(() -> "Allowing these origins: " + origins);
			corsConfig.getAllowedOrigins().addAll(origins);
			corsConfig.setAllowAnyOrigin(false);
		} else {
			logger.debug(() -> "Allowing all origins.");
			corsConfig.setAllowAnyOrigin(true);
		}

		corsConfig.setSupportsCredentials(getBoolean(TRIBEFIRE_CORS_SUPPORTS_CREDENTIALS, true));
		corsConfig.setSupportAnyHeader(getBoolean(TRIBEFIRE_CORS_SUPPORT_ANY_HEADER, true));
		corsConfig.setSupportedMethods(getSupportedMethodsFromEnvironmentOrDefault());
		corsConfig.getSupportedHeaders().addAll(getSetFromEnvironment(TRIBEFIRE_CORS_SUPPORTED_HEADERS));
		corsConfig.getExposedHeaders().addAll(getSetFromEnvironment(TRIBEFIRE_CORS_EXPOSED_HEADERS));

		CortexConfiguration cortexConfig = session.create(CortexConfiguration.T);
		cortexConfig.setGlobalId(CortexConfiguration.CORTEX_CONFIGURATION_GLOBAL_ID);
		cortexConfig.setId("singleton");
		cortexConfig.setCorsConfiguration(corsConfig);
	}

	private Set<String> getSupportedMethodsFromEnvironmentOrDefault() {
		Set<String> result = getSetFromEnvironment(TRIBEFIRE_CORS_SUPPORTED_METHODS);
		if (result != null && !result.isEmpty()) {
			return result;
		}
		return set("GET", "POST", "PUT", "DELETE");
	}

	private Set<String> getSetFromEnvironment(String name) {
		String listString = TribefireRuntime.getProperty(name);
		if (!StringTools.isBlank(listString)) {
			String[] strings = StringTools.splitCommaSeparatedString(listString, true);
			if (strings != null && strings.length > 0) {
				return set(strings);
			}
		}
		return Collections.emptySet();
	}

	private boolean getBoolean(String name, boolean defaultValue) {
		String valueString = TribefireRuntime.getProperty(name);
		if (!StringTools.isBlank(valueString)) {
			return valueString.equalsIgnoreCase("true");
		}
		return defaultValue;
	}

	private List<String> getAllowedOriginsFromEnvironment() {
		String listString = TribefireRuntime.getProperty(TRIBEFIRE_CORS_ALLOWED_ORIGINS);
		if (StringTools.isBlank(listString)) {
			return null;
		}
		String[] strings = StringTools.splitCommaSeparatedString(listString, true);
		if (strings != null && strings.length > 0) {
			return Arrays.asList(strings);
		}
		return null;
	}

}
