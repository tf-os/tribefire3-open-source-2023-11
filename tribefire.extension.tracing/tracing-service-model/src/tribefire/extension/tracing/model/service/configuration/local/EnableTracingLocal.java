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
package tribefire.extension.tracing.model.service.configuration.local;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.time.TimeSpan;

import tribefire.extension.tracing.model.service.TracingRequest;

public interface EnableTracingLocal extends TracingRequest {

	EntityType<EnableTracingLocal> T = EntityTypes.T(EnableTracingLocal.class);

	@Override
	EvalContext<? extends EnableTracingLocalResult> eval(Evaluator<ServiceRequest> evaluator);

	String enableDuration = "enableDuration";

	@Name("Enable Tracing Duration")
	@Description("Duration when the tracing gets automatically disabled again - if not set it stays enabled forever")
	TimeSpan getEnableDuration();
	void setEnableDuration(TimeSpan enableDuration);
}
