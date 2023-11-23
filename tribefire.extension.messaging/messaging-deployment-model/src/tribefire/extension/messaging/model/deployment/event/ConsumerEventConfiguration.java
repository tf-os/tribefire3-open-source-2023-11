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
import tribefire.extension.messaging.model.MessagingComponentDeploymentType;
import tribefire.extension.messaging.model.deployment.event.rule.ConsumerEventRule;

@SelectiveInformation("Consumer Event Configuration: ${id}")
public interface ConsumerEventConfiguration extends EventConfiguration {
    EntityType<ConsumerEventConfiguration> T = EntityTypes.T(ConsumerEventConfiguration.class);

    String eventRules = "eventRules";

    @Name("Event Rules")
    @Description("Rules applicable for this configuration")
    @Priority(2.8d)
    List<ConsumerEventRule> getEventRules();
    void setEventRules(List<ConsumerEventRule> eventRules);

    @Override
    default MessagingComponentDeploymentType getDeploymentType() {
        return MessagingComponentDeploymentType.CONSUMER;
    }
}
