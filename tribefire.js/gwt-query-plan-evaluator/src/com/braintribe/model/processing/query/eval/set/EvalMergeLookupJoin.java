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
package com.braintribe.model.processing.query.eval.set;

import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.queryplan.index.GeneratedIndex;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class EvalMergeLookupJoin extends AbstractEvalMergeJoin {

	protected final GeneratedIndex lookupIndex;
	protected final Value lookupValue;

	public EvalMergeLookupJoin(MergeLookupJoin tupleSet, QueryEvaluationContext context) {
		super(tupleSet, context);

		this.lookupIndex = buildIndex(tupleSet);
		this.lookupValue = tupleSet.getValue();
	}

	private GeneratedIndex buildIndex(MergeLookupJoin tupleSet) {
		GeneratedIndex generatedIndex = GeneratedIndex.T.create();
		generatedIndex.setIndexKey(tupleSet.getOtherValue());
		generatedIndex.setOperand(tupleSet.getOtherOperand());
		
		return generatedIndex;
	}

	@Override
	protected Iterator<Tuple> joinTuplesFor(Tuple tuple) {
		Object value = context.resolveValue(tuple, lookupValue);

		return context.getAllValuesForIndex(lookupIndex, value).iterator();
	}

}
