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
package com.braintribe.model.processing.mqrpc.client;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.processing.rpc.commons.api.config.GmRpcClientConfig;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.transport.messaging.api.MessagingSessionProvider;

/**
 * Basic {@link GmRpcClientConfig} for MQ RPC.
 * 
 */
public class BasicGmMqRpcClientConfig extends GmRpcClientConfig {

	private MessagingSessionProvider messagingSessionProvider;
	private String requestDestinationName;
	private EntityType<? extends Destination> requestDestinationType;
	private boolean ignoreResponses;
	private String responseTopicName;
	private InstanceId producerId;
	private long responseTimeout = 10000L;
	private int retries = 3;

	@Required
	@Configurable
	public void setMessagingSessionProvider(MessagingSessionProvider messagingSessionProvider) {
		this.messagingSessionProvider = messagingSessionProvider;
	}

	@Required
	@Configurable
	public void setRequestDestinationName(String requestDestinationName) {
		this.requestDestinationName = requestDestinationName;
	}

	@Required
	@Configurable
	public void setRequestDestinationType(EntityType<? extends Destination> requestDestinationType) {
		this.requestDestinationType = requestDestinationType;
	}

	@Configurable
	public void setIgnoreResponses(boolean ignoreResponses) {
		this.ignoreResponses = ignoreResponses;
	}

	@Configurable
	public void setResponseTopicName(String responseTopicName) {
		this.responseTopicName = responseTopicName;
	}

	@Configurable
	public void setResponseTimeout(long responseTimeout) {
		this.responseTimeout = responseTimeout;
	}

	@Configurable
	public void setRetries(int retries) {
		this.retries = retries;
	}

	public MessagingSessionProvider getMessagingSessionProvider() {
		return messagingSessionProvider;
	}

	public String getRequestDestinationName() {
		return requestDestinationName;
	}

	public EntityType<? extends Destination> getRequestDestinationType() {
		return requestDestinationType;
	}

	public boolean isIgnoreResponses() {
		return ignoreResponses;
	}

	public String getResponseTopicName() {
		return responseTopicName;
	}

	public InstanceId getProducerId() {
		return producerId;
	}

	public long getResponseTimeout() {
		return responseTimeout;
	}

	public int getRetries() {
		return retries;
	}

}
