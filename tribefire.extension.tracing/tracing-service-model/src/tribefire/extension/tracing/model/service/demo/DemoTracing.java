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
package tribefire.extension.tracing.model.service.demo;

import java.util.List;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

public interface DemoTracing extends DemoTracingRequest {

	EntityType<DemoTracing> T = EntityTypes.T(DemoTracing.class);

	@Override
	EvalContext<? extends DemoTracingResult> eval(Evaluator<ServiceRequest> evaluator);

	String nestedTracings = "nestedTracings";
	String beforeDuration = "beforeDuration";
	String afterDuration = "afterDuration";
	String executeParallel = "executeParallel";
	String waitToFinish = "waitToFinish";

	List<DemoTracing> getNestedTracings();
	void setNestedTracings(List<DemoTracing> nestedTracings);

	@Mandatory
	@Initializer("0l")
	long getBeforeDuration();
	void setBeforeDuration(long beforeDuration);

	@Mandatory
	@Initializer("0l")
	long getAfterDuration();
	void setAfterDuration(long afterDuration);

	@Initializer("false")
	boolean getExecuteParallel();
	void setExecuteParallel(boolean executeParallel);

	@Initializer("false")
	boolean getWaitToFinish();
	void setWaitToFinish(boolean waitToFinish);

}
