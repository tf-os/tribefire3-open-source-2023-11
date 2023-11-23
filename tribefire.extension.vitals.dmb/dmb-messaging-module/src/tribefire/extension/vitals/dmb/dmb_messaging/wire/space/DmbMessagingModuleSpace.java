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
package tribefire.extension.vitals.dmb.dmb_messaging.wire.space;

import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.messaging.dmb.GmDmbMqMessaging;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.transport.messaging.dbm.GmDmbMqConnectionProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.dmb.messaging.model.deployment.DmbMessaging;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.ClusterBindersContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet be written.
 */
@Managed
public class DmbMessagingModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ClusterBindersContract clusterBinders;

	private static String messagingId = "wire://tribefire.extension.basic:dmb-messaging-module/DmbMessagingModuleSpace/dmbMessaging";

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind("cortex", this::createDmbMessaging);
	}

	private void createDmbMessaging(PersistenceInitializationContext ctx) {
		ManagedGmSession session = ctx.getSession();

		// TODO create dmb-platform-messaging-initializer 
		Module module = session.getEntityByGlobalId("module://tribefire.extension.vitals.dmb:dmb-messaging-module");

		DmbMessaging dmbMessaging = session.create(DmbMessaging.T, messagingId);
		dmbMessaging.setExternalId("messaging.dmb");
		dmbMessaging.setModule(module);

		CortexConfiguration cortexConfig = session.findEntityByGlobalId(CortexConfiguration.CORTEX_CONFIGURATION_GLOBAL_ID);
		cortexConfig.setMessaging(dmbMessaging);
	}

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(DmbMessaging.T) //
				.component(clusterBinders.messaging()) //
				.expertFactory(this::messagingConnectionProvider);
	}

	private GmDmbMqConnectionProvider messagingConnectionProvider(ExpertContext<DmbMessaging> expertContext) {
		DmbMessaging deployable = expertContext.getDeployable();

		GmDmbMqConnectionProvider bean = new GmDmbMqConnectionProvider();
		bean.setConnectionConfiguration(toLegacyConfig(deployable));
		bean.setMessagingContext(tfPlatform.messaging().context());

		return bean;
	}

	private static GmDmbMqMessaging toLegacyConfig(DmbMessaging deployable) {
		GmDmbMqMessaging result = GmDmbMqMessaging.T.create();
		result.setBrokerHost(deployable.getBrokerHost());
		result.setConnectorPort(deployable.getConnectorPort());
		result.setJmxServiceUrl(deployable.getJmxServiceUrl());
		result.setUsername(deployable.getUsername());
		result.setPassword(deployable.getPassword());

		return result;
	}

}
