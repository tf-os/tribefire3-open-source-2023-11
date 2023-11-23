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
package tribefire.extension.ldap.initializer;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.ldap.initializer.wire.LdapInitializerModuleWireModule;
import tribefire.extension.ldap.initializer.wire.contract.LdapInitializerModuleContract;
import tribefire.extension.ldap.initializer.wire.contract.LdapInitializerModuleMainContract;
import tribefire.extension.ldap.initializer.wire.contract.RuntimePropertiesContract;

public class LdapInitializer extends AbstractInitializer<LdapInitializerModuleMainContract> {

	@Override
	public WireTerminalModule<LdapInitializerModuleMainContract> getInitializerWireModule() {
		return LdapInitializerModuleWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<LdapInitializerModuleMainContract> initializerContext,
			LdapInitializerModuleMainContract initializerMainContract) {

		LdapInitializerModuleContract initializer = initializerMainContract.initializer();

		RuntimePropertiesContract properties = initializerMainContract.properties();
		if (properties.LDAP_INITIALIZE_DEFAULTS() && initializer.isLdapConfigured()) {

			initializerMainContract.models().registerModels();
			initializerMainContract.models().metaData();

			initializerMainContract.initializer().connection();
			initializerMainContract.initializer().ldapUserAccess();
			initializerMainContract.initializer().ldapAccess();
			initializerMainContract.initializer().authentication();
			initializerMainContract.initializer().cortexConfiguration();

		}

		initializer.connectivityCheckBundle();

	}
}
