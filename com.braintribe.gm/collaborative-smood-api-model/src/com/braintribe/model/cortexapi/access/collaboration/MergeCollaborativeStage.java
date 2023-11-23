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
package com.braintribe.model.cortexapi.access.collaboration;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Assuming that given stages are both manipulation stages, this request merges all the manipulations from
 * {@link #getSource() source} to {@link #getTarget() target}.
 */
@Description("Merges all the current manipulations from on stage to another.")
public interface MergeCollaborativeStage extends CollaborativePersistenceRequest {

	EntityType<MergeCollaborativeStage> T = EntityTypes.T(MergeCollaborativeStage.class);

	@Mandatory
	@Description("The name of the source stage.")
	String getSource();
	void setSource(String source);

	@Mandatory
	@Description("The name of the target stage.")
	String getTarget();
	void setTarget(String target);

	@Override
	EvalContext<Boolean> eval(Evaluator<ServiceRequest> evaluator);

	@Override
	default CollaborativePersistenceRequestType collaborativeRequestType() {
		return CollaborativePersistenceRequestType.MergeStage;
	}

}
