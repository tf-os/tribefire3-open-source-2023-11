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
package tribefire.module.api;

import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.configured.ConfigurationModelBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.module.wire.contract.ModelApiContract;

/**
 * Straight forward, a factory which can be used to create new configuration models given their name and version information and their dependencies.
 * <p>
 * Configuration model is a model that does not introduce any new types but only aggregates other models (as dependencies) and potentially contains
 * {@link MetaData}.
 * 
 * @see ModelApiContract#newConfigurationModelFactory(ManagedGmSession)
 * 
 * @author peter.gazdik
 */
public interface ConfigurationModelFactory {

	/** Returns a {@link ConfigurationModelBuilder} to build a new model with a standard name, i.e. "$groupId:$artifactId". */
	ConfigurationModelBuilder create(String groupId, String artifactId, String version);

	/** Returns a {@link ConfigurationModelBuilder} to build a new model with given name. */
	ConfigurationModelBuilder create(String name, String version);

}
