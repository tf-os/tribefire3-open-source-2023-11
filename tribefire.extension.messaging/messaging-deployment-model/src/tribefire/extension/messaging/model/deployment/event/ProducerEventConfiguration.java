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
package tribefire.extension.messaging.model.deployment.event;

import java.util.List;

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.messaging.model.MessagingComponentDeploymentType;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;

@SelectiveInformation("Producer Event Configuration: ${id}")
public interface ProducerEventConfiguration extends EventConfiguration {
	EntityType<ProducerEventConfiguration> T = EntityTypes.T(ProducerEventConfiguration.class);

	String interceptionType = "interceptionType";
	String eventRules = "eventRules";

	@Name("Intercept type")
	@Description("EntityType for aspect interception")
	@Priority(2.9d)
	GmEntityType getInterceptionType();
	void setInterceptionType(GmEntityType interceptionType);

	@Name("Event Rules")
	@Description("Rules applicable for this configuration")
	@Priority(2.8d)
	List<ProducerEventRule> getEventRules();
	void setEventRules(List<ProducerEventRule> eventRules);

	@Override
	default MessagingComponentDeploymentType getDeploymentType() {
		return MessagingComponentDeploymentType.PRODUCER;
	}

	default <T extends ServiceRequest> EntityType<T> getAspectInterceptionEntityType() {
		return this.getInterceptionType().reflectionType().cast();
	}
}
