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

import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;

@SelectiveInformation("Consumer Event Rule: ${name} - ${endpoints}")
public interface ConsumerEventRule extends EventRule {
	EntityType<ConsumerEventRule> T = EntityTypes.T(ConsumerEventRule.class);

	String postProcessorType = "postProcessorType";

	@Name("Post Processor Type")
	@Description("PostProcessor to be used for message consumption")
	@Priority(1.9d)
	@Mandatory
	GmEntityType getPostProcessorType();
	void setPostProcessorType(GmEntityType postProcessorType);

	@Name("Post Processing Request Type")
	@Description("Request type to be used to trigger post processor")
	@Priority(1.8d)
	@Mandatory
	GmEntityType getPostProcessorRequestType();
	void  setPostProcessorRequestType(GmEntityType postProcessorRequestType);
}
