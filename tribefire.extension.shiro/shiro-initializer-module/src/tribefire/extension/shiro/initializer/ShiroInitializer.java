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
package tribefire.extension.shiro.initializer;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.shiro.initializer.wire.ShiroInitializerWireModule;
import tribefire.extension.shiro.initializer.wire.contract.ShiroInitializerContract;
import tribefire.extension.shiro.initializer.wire.contract.ShiroInitializerMainContract;
import tribefire.extension.shiro.initializer.wire.contract.ShiroRuntimePropertiesContract;

/**
 * <p>
 * This {@link AbstractInitializer initializer} initializes targeted accesses with our custom instances available from
 * initializer's contracts.
 * </p>
 */
public class ShiroInitializer extends AbstractInitializer<ShiroInitializerMainContract> {

	@Override
	public WireTerminalModule<ShiroInitializerMainContract> getInitializerWireModule() {
		return ShiroInitializerWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<ShiroInitializerMainContract> initializerContext,
			ShiroInitializerMainContract initializerMainContract) {

		//@formatter:off
		TribefireRuntime.setPropertyPrivate(
				"SHIRO_GOOGLE_SECRET", "SHIRO_GOOGLE_SECRET_ENCRYPTED", 
				"SHIRO_AZUREAD_SECRET_ENCRYPTED",
				"SHIRO_TWITTER_SECRET", "SHIRO_TWITTER_SECRET_ENCRYPTED", 
				"SHIRO_FACEBOOK_SECRET", "SHIRO_FACEBOOK_SECRET_ENCRYPTED",
				"SHIRO_GITHUB_SECRET", "SHIRO_GITHUB_SECRET_ENCRYPTED",
				"SHIRO_INSTAGRAM_SECRET_ENCRYPTED",
				"SHIRO_OKTA_SECRET_ENCRYPTED"
				);
		//@formatter:on

		GmMetaModel cortexModel = initializerMainContract.coreInstancesContract().cortexModel();
		cortexModel.getDependencies().add(initializerMainContract.initializerModelsContract().configuredDeploymentModel());

		ShiroInitializerContract shiroInitializer = initializerMainContract.shiroInitializerContract();

		ShiroRuntimePropertiesContract properties = initializerMainContract.propertiesContract();
		if (properties.SHIRO_INITIALIZE_DEFAULTS()) {
			shiroInitializer.metaData();
			shiroInitializer.authenticationConfiguration();
			shiroInitializer.login();
			shiroInitializer.sessionValidator();
			shiroInitializer.bootstrappingWorker();
			shiroInitializer.serviceRequestProcessor();
			shiroInitializer.functionalCheckBundle();
		}

	}
}
