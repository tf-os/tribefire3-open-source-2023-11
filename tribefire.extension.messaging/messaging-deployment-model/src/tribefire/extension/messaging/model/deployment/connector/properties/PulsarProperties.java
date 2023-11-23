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
package tribefire.extension.messaging.model.deployment.connector.properties;

import java.util.List;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;
import tribefire.extension.messaging.model.deployment.event.PulsarEndpoint;

public interface PulsarProperties extends CommonMessagingProperties {

	EntityType<PulsarProperties> T = EntityTypes.T(PulsarProperties.class);

	String connectionTimeout = "connectionTimeout";
	String operationTimeout = "operationTimeout";
	String webServiceUrl = "webServiceUrl";
	String compressionType = "compressionType";
	String batchingMaxMessages = "batchingMaxMessages";
	String accessMode = "accessMode";
	String defaultSubscriptionName = "defaultSubscriptionName";
	String defaultMaxNumMessages = "defaultMaxNumMessages";
	String subscriptionType = "subscriptionType";

	@Description("A default subscription type to be used if there is none specified")
	@Initializer("'subscriptionName'")
	String getDefaultSubscriptionName();
	void setDefaultSubscriptionName(String defaultSubscriptionName);

	@Description("A default maximum number of messages to listen")
	@Initializer("100")
	Integer getMaxNumMessages();
	void setMaxNumMessages(Integer maxNumMessages);

	@Description("A default subscription type to listen. Possible values: Exclusive, Shared, Failover, Key_Shared")
	@Initializer("'Shared'")
	String getSubscriptionType();
	void setSubscriptionType(String subscriptionType);

	@Description("A default compression type to send message. Possible values: LZ4, NONE, ZLIB, ZSTD, SNAPPY")
	@Initializer("'NONE'")
	String getCompressionType();
	void setCompressionType(String compressionType);

	@Description("Set the maximum number of messages permitted in a batch")
	@Initializer("1")
	Integer getBatchingMaxMessages();
	void setBatchingMaxMessages(Integer batchingMaxMessages);

	void setAccessMode(String accessMode);
	@Description("Configure the type of access mode that the producer requires on the topic. Possible values: Shared, Exclusive, WaitForExclusive")
	@Initializer("'Shared'")
	String getAccessMode();

	@Description("Set the duration of time in seconds to wait for a connection to a broker to be established")
	@Initializer("10")
	Integer getConnectionTimeout();
	void setConnectionTimeout(Integer connectionTimeout);

	@Description("Set operation timeout in seconds")
	@Initializer("30")
	Integer getOperationTimeout();
	void setOperationTimeout(Integer operationTimeout);

	@Description("Web service url for pulsar admin")
	@Initializer("'http://localhost:8081'")
	String getWebServiceUrl();
	void setWebServiceUrl(String webServiceUrl);

	default PulsarProperties apply(EventEndpointConfiguration config) {
		PulsarEndpoint endpoint = (PulsarEndpoint) config.getEventEndpoint();
		this.setGlobalId(endpoint.getGlobalId());
		this.setServiceUrls(List.of(endpoint.getConnectionUrl()));
		this.setWebServiceUrl(endpoint.getAdminUrl());
		this.setTopicsToListen(config.getTopics().stream().toList());
		return this;
	}
}
