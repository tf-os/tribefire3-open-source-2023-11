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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CommonMessagingProperties extends GenericEntity {

	EntityType<CommonMessagingProperties> T = EntityTypes.T(CommonMessagingProperties.class);

	String serviceUrls = "serviceUrls";
	String topicsToSend = "topicsToSend";
	String topicsToListen = "topicsToListen";
	String receiveTimeout = "receiveTimeout";

	@Initializer("['localhost:29092']") // defaults: kafka: localhost:29092, pulsar: localhost:6650
	@Description("Configure the service URL")
	List<String> getServiceUrls();
	void setServiceUrls(List<String> serviceUrls);

	@Description("Topics to listen")
	@Initializer("['newTopic']")
	List<String> getTopicsToListen();
	void setTopicsToListen(List<String> topicsToListen);

	@Description("A default Receive Timeout in seconds")
	@Initializer("10")
	Integer getReceiveTimeout();
	void setReceiveTimeout(Integer receiveTimeout);
}
