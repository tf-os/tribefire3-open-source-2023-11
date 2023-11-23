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
package tribefire.extension.messaging.model.deployment.event.rule;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import tribefire.extension.messaging.model.deployment.event.EventEndpointConfiguration;

@Abstract
@SelectiveInformation("Event Rule: ${name} - ${ruleEnabled}")
public interface EventRule extends GenericEntity {

	EntityType<EventRule> T = EntityTypes.T(EventRule.class);

	// ***************************************************************************************************
	// INTERNAL/TECHNICAL CONFIGURATION
	// ***************************************************************************************************
	String name = "name";
	String ruleEnabled = "ruleEnabled";

	@Name("Rule Enabled")
	@Description("Enables/disables this rule for processing")
	@Priority(2.8d)
	@Mandatory
	@Initializer("false")
	boolean getRuleEnabled();
	void setRuleEnabled(boolean ruleEnabled);

	@Name("Name")
	@Description("The name of the rule.")
	@Priority(2.9d)
	@Mandatory
	String getName();
	void setName(String name);

	// ***************************************************************************************************
	// DYNAMIC CONFIGURATION
	// ***************************************************************************************************
	String endpointConfiguration = "endpointConfiguration";

	@Name("Endpoint delivery configuration")
	@Description("The configuration for message delivery (multiple topics per endpoint)")
	@Priority(1.8d)
	List<EventEndpointConfiguration> getEndpointConfiguration();
	void setEndpointConfiguration(List<EventEndpointConfiguration> endpointConfiguration);
}
