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
package com.braintribe.model.processing.oracle.model.evaluable;

import java.util.List;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * PGA: Originally I wanted this to derive from {@link ListObjectEvaluable}, but that's not possible. I would have to
 * use wildcards as List parameter types, which the JTA doesn't support. So the whole {@link EntityTypeOracle}
 * implementation assumes that is not possible and would such a situation consider an inconsistency.
 */
public interface ListStringEvaluable extends ObjectEvaluable {

	EntityType<ListStringEvaluable> T = EntityTypes.T(ListStringEvaluable.class);

	/** Check that List of Strings can override List of Objects */
	@Override
	EvalContext<? extends List<String>> eval(Evaluator<ServiceRequest> evaluator);

}
