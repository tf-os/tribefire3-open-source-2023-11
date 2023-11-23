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
package tribefire.platform.config.url;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry;
import com.braintribe.config.configurator.ConfiguratorException;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.utils.lcd.StringTools;

/**
 * This is a subclass of {@link AbstractExternalConfigurator} that loads entries from an environment variable. This
 * requires that the complete JSON information is put into a single environment variable. The name of the variable is
 * determined by subclasses. This class tries to get the value for the variable name using {@link System#getenv(String)}
 * and, if this does not return a non-empty value, {@link System#getProperty(String)}.
 * 
 * This way of configuring registry entries is useful if a fixed container (which holds a pre-configured tribefire,
 * e.g., a Docker container) cannot be easily changed. In that case, environment variables can be defined to change the
 * registry.
 */
public abstract class AbstractEnvironmentVariableBasedConfigurator extends AbstractExternalConfigurator {

	private static final Logger logger = Logger.getLogger(AbstractEnvironmentVariableBasedConfigurator.class);

	protected abstract String getEnvironmentVariableName();

	@Override
	protected List<RegistryEntry> getEntries() throws ConfiguratorException {

		String envName = getEnvironmentVariableName();

		List<String> envNames = new ArrayList<>(2);
		if (!StringTools.isBlank(envName)) {
			envNames.add(envName);
		}
		envNames.add(getSharedEnvironmentVariableName());

		List<RegistryEntry> result = new ArrayList<>();

		for (String name : envNames) {

			logger.debug(() -> "Trying to resolved environment variable: " + name);

			String value = System.getenv(name);
			if (StringTools.isEmpty(value)) {
				value = System.getProperty(name);
			}

			if (value != null) {
				value = value.trim();
				if (value.length() > 0) {
					logger.debug(() -> "Found a value for environment variable: " + name);
					try (Reader reader = new StringReader(value)) {
						List<RegistryEntry> list = super.readConfigurationFromInputStream(reader);
						if (list != null && !list.isEmpty()) {
							list.forEach(e -> e.setSource("Env: " + name));
							result.addAll(list);
						}
					} catch (IOException e) {
						throw new ConfiguratorException("Error while trying to read value " + value + " from environment variable: " + name, e);
					}
				} else {
					logger.debug(() -> "Found just an empty value for environment variable '" + name + "'");
				}
			} else {
				logger.debug(() -> "Could not find a value for environment variable '" + name + "'");
			}

		}

		return result;

	}

	private String getSharedEnvironmentVariableName() {
		String varName = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_CONFIGURATION_INJECTION_ENVVARIABLE + "_SHARED");
		if (StringTools.isEmpty(varName)) {
			varName = "TRIBEFIRE_CONFIGURATION_INJECTION_JSON_SHARED";
		}
		return varName;
	}

	@Override
	protected String getSourceInformation() {
		return getEnvironmentVariableName();
	}

	@Override
	public String toString() {
		String varName = getEnvironmentVariableName();
		return this.getClass().getName() + " (" + varName + ")";
	}

}
