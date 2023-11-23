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

import com.braintribe.config.configurator.Configurator;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;

import tribefire.platform.config.url.AbstractUrlBasedConfigurator;

/**
 * A tribefire {@link Configurator} for URL based configured denotations.
 * 
 * @author gunther.schenk
 */
public class TribefireUrlBasedConfigurator extends AbstractUrlBasedConfigurator {
	
	@Override
	protected String buildDefaultFileName() {
		return "configuration.json";
	}

	@Override
	protected String buildUrlProperty() {
		return TribefireRuntime.ENVIRONMENT_CONFIGURATION_INJECTION_URL;
	}
	
}
