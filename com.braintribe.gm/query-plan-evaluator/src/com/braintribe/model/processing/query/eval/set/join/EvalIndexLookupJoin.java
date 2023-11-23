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
package com.braintribe.model.processing.query.eval.set.join;

import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class EvalIndexLookupJoin extends AbstractEvalIndexJoin {

	private final Index lookupIndex;
	private final Value lookupValue;

	public EvalIndexLookupJoin(IndexLookupJoin join, QueryEvaluationContext context) {
		super(join, context);

		this.lookupIndex = join.getLookupIndex();
		this.lookupValue = join.getLookupValue();
	}

	@Override
	protected Iterator<Tuple> joinTuplesFor(Tuple tuple) {
		Object value = context.resolveValue(tuple, lookupValue);

		return context.getAllValuesForIndex(lookupIndex, value).iterator();
	}

}
