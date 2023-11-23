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
package com.braintribe.model.elasticsearch.service;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.api.ServiceRequest;

public interface ReIndex extends ElasticRequest {

	final EntityType<ReIndex> T = EntityTypes.T(ReIndex.class);

	void setAccess(IncrementalAccess access);
	@Mandatory
	IncrementalAccess getAccess();

	/**
	 * The query to specify which data should be indexed
	 */
	void setQuery(EntityQuery query);
	EntityQuery getQuery();

	@Override
	EvalContext<? extends ReIndexResult> eval(Evaluator<ServiceRequest> evaluator);

}
