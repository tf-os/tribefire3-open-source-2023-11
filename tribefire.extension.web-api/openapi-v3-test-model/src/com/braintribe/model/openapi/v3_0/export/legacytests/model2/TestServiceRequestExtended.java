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
package com.braintribe.model.openapi.v3_0.export.legacytests.model2;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

public interface TestServiceRequestExtended extends TestServiceRequest {

	EntityType<TestServiceRequestExtended> T = EntityTypes.T(TestServiceRequestExtended.class);

	@Override
	EvalContext<? extends TestServiceResponse> eval(Evaluator<ServiceRequest> evaluator);

	List<String> getStringList();
	void setStringList(List<String> list);

	Set<Integer> getIntSet();
	void setIntSet(Set<Integer> set);

	List<Object> getObjectList();
	void setObjectList(List<Object> list);

	Map<String, String> getStringMap();
	void setStringMap(Map<String, String> map);

	String getVeryHighPriorityProperty();
	void setVeryHighPriorityProperty(String s);

	String getLowPriorityProperty();
	void setLowPriorityProperty(String s);

	String getInvisibleProperty();
	void setInvisibleProperty(String s);
}
