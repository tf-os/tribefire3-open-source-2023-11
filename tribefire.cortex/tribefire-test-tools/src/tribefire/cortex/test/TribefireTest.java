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
package tribefire.cortex.test;

import java.util.function.Supplier;

import com.braintribe.model.processing.tfconstants.TribefireComponent;
import com.braintribe.model.processing.tfconstants.TribefireUrlBuilder;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Provides a set of simple helpers to make writing tribefire tests more convenient.
 * 
 * @author michael.lafite
 */
// TODO: the helpers for url/user/password are based on what's used in ImpApiFactory. Code should be in a single place.
public interface TribefireTest {

	/** Returns the tribefire-services url. */
	default String tribefireServicesURL() {
		return getConfigProperty(() -> new TribefireUrlBuilder().http().buildFor(TribefireComponent.Services), "TRIBEFIRE_SERVICES_URL",
				"TRIBEFIRE_PUBLIC_SERVICES_URL", "QA_FORCE_URL");
	}

	/** Returns the default user used to connect to tribefire. */
	default String tribefireDefaultUser() {
		return getConfigProperty(() -> "cortex", "TRIBEFIRE_DEFAULT_USER", "QA_FORCE_USERNAME");
	}

	/** Returns the default password used to connect to tribefire. */
	default String tribefireDefaultPassword() {
		return getConfigProperty(() -> "cortex", "TRIBEFIRE_DEFAULT_PASSWORD", "QA_FORCE_PASSWORD");
	}

	/**
	 * Returns the value of the specified configuration property.
	 * 
	 * @param defaultValueSuplier
	 *            a supplier which provides a default value (in case the property is not set).
	 * @param propertyNames
	 *            one ore more property names that will be checked. Each property is searched in system properties and
	 *            in environment variables. System properties are converted to lower case and "_" is converted to ".".
	 */
	default String getConfigProperty(Supplier<String> defaultValueSuplier, String... propertyNames) {
		String result = null;
		for (String propertyName : propertyNames) {
			String systemPropertyName = propertyName.toLowerCase().replace("_", ".");

			result = System.getProperty(systemPropertyName);
			if (CommonTools.isEmpty(result)) {
				result = System.getenv(propertyName);
			}
			if (result != null) {
				break;
			}
		}
		if (result == null) {
			result = defaultValueSuplier.get();
		}
		return result;
	}
}
