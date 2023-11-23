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
package com.braintribe.rabbitmq.rabbit_mq_messaging.wire.space;

import java.util.List;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.rabbitmq.model.deployment.RabbitMqMessaging;
import com.braintribe.transport.messaging.rabbitmq.RabbitMqConnectionProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.ClusterBindersContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class RabbitMqMessagingModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private ClusterBindersContract clusterBinders;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(RabbitMqMessaging.T) //
			.component(clusterBinders.messaging()) //
			.expertFactory(this::rabbitMqMessaging);
	}
	
	@Managed
	private RabbitMqConnectionProvider rabbitMqMessaging(ExpertContext<RabbitMqMessaging> expertContext) {
		RabbitMqMessaging deployable = expertContext.getDeployable();

		return new com.braintribe.transport.messaging.rabbitmq.RabbitMqMessaging().createConnectionProvider(toLegacyConfig(deployable), tfPlatform.messaging().context());
	}

	private com.braintribe.model.messaging.rabbitmq.RabbitMqMessaging toLegacyConfig(RabbitMqMessaging deployable) {
		com.braintribe.model.messaging.rabbitmq.RabbitMqMessaging result = com.braintribe.model.messaging.rabbitmq.RabbitMqMessaging.T.create();
		
		result.setHost(deployable.getHost());
		result.setVirtualHost(deployable.getVirtualHost());
		result.setPort(deployable.getPort());
		result.setUri(deployable.getUri());
		result.setUsername(deployable.getUsername());
		result.setPassword(deployable.getPassword());
		result.setAddresses(deployable.getAddresses());
		result.setAutomaticRecoveryEnabled(deployable.getAutomaticRecoveryEnabled());
		result.setTopologyRecoveryEnabled(deployable.getTopologyRecoveryEnabled());
		result.setConnectionTimeout(deployable.getConnectionTimeout());
		result.setNetworkRecoveryInterval(deployable.getNetworkRecoveryInterval());
		result.setRequestedHeartbeat(deployable.getRequestedHeartbeat());
		result.setName(deployable.getName());
		
		return result;
	}

}
