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
package tribefire.cortex.module.loading;

import com.braintribe.model.processing.meta.configuration.ConfigurationModelBuilderManagedImpl;
import com.braintribe.model.processing.meta.configured.ConfigurationModelBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.module.api.ConfigurationModelFactory;

/**
 * @author peter.gazdik
 */
/* package */ class SessionBasedConfigurationModelFactory implements ConfigurationModelFactory {

	private final ManagedGmSession session;

	public SessionBasedConfigurationModelFactory(ManagedGmSession session) {
		this.session = session;
	}

	@Override
	public ConfigurationModelBuilder create(String groupId, String artifactId, String version) {
		return create(groupId + ":" + artifactId, version);
	}

	@Override
	public ConfigurationModelBuilder create(String name, String version) {
		return new ConfigurationModelBuilderManagedImpl(session, name, version);
	}

}
