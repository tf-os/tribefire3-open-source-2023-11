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
package com.braintribe.mq.jms_mq_messaging.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.mq.model.deployment.JmsMqConnection;
import com.braintribe.transport.messaging.jms.JmsMqConnectionProvider;
import com.braintribe.transport.messaging.jms.JmsMqMessaging;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.wire.contract.ClusterBindersContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class JmsMqMessagingModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private ClusterBindersContract clusterBinders;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(com.braintribe.mq.model.deployment.JmsMqConnection.T) //
			.component(clusterBinders.messaging()) //
			.expertFactory(this::jmsMqMessaging);
	}
	
	@Managed
	private JmsMqConnectionProvider jmsMqMessaging(ExpertContext<JmsMqConnection> expertContext) {
		JmsMqConnection deployable = expertContext.getDeployable();
		
		return new JmsMqMessaging().createConnectionProvider(toLegacyConfig(deployable), tfPlatform.messaging().context());
	}

	private com.braintribe.model.messaging.jms.JmsMqConnection toLegacyConfig(JmsMqConnection deployable) {
		com.braintribe.model.messaging.jms.JmsMqConnection result = com.braintribe.model.messaging.jms.JmsMqConnection.T.create();
		
		result.setHostAddress(deployable.getHostAddress());
		result.setUsername(deployable.getUsername());
		result.setPassword(deployable.getPassword());
		result.setTransacted(deployable.getTransacted());
		result.setAcknowledgeMode(deployable.getAcknowledgeMode());
		result.setName(deployable.getName());
		result.setHost(deployable.getHost());
		result.setEnableTracing(deployable.getEnableTracing());
		result.setCcsId(deployable.getCcsId());
		result.setUseBindingsModeConnections(deployable.getUseBindingsModeConnections());
		result.setChannel(deployable.getChannel());
		result.setQueueManager(deployable.getQueueManager());
		result.setPort(deployable.getPort());
		result.setSslPeerName(deployable.getSslPeerName());
		result.setSslCipherSuite(deployable.getSslCipherSuite());
		result.setTargetClient(deployable.getTargetClient());
		result.setDestinationExpiry(deployable.getDestinationExpiry());
		
		return result;
	}

}
