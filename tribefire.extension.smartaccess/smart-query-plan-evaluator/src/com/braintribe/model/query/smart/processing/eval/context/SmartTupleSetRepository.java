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
package com.braintribe.model.query.smart.processing.eval.context;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.context.TupleSetRepository;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.query.smart.processing.eval.set.EvalDelegateQueryAsIs;
import com.braintribe.model.query.smart.processing.eval.set.EvalDelegateQueryJoin;
import com.braintribe.model.query.smart.processing.eval.set.EvalDelegateQuerySet;
import com.braintribe.model.query.smart.processing.eval.set.EvalOrderedConcatenation;
import com.braintribe.model.query.smart.processing.eval.set.EvalStaticTuple;
import com.braintribe.model.query.smart.processing.eval.set.EvalStaticTuples;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.model.smartqueryplan.set.DelegateQueryAsIs;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;
import com.braintribe.model.smartqueryplan.set.OrderedConcatenation;
import com.braintribe.model.smartqueryplan.set.SmartTupleSet;
import com.braintribe.model.smartqueryplan.set.StaticTuple;
import com.braintribe.model.smartqueryplan.set.StaticTuples;

//
public class SmartTupleSetRepository extends TupleSetRepository {

	private final SmartQueryEvaluationContext smartContext;

	public SmartTupleSetRepository(SmartQueryEvaluationContext context) {
		super(context);
		this.smartContext = context;
	}

	@Override
	protected EvalTupleSet newEvalTupleSetFor(TupleSet tupleSet) {
		if (tupleSet.tupleSetType() == TupleSetType.extension) {
			return newSmartEvalTupleSetFor((SmartTupleSet) tupleSet);
		} else {
			return super.newEvalTupleSetFor(tupleSet);
		}
	}

	private EvalTupleSet newSmartEvalTupleSetFor(SmartTupleSet tupleSet) {
		switch (tupleSet.smartType()) {
			case delegateQueryAsIs:
				return new EvalDelegateQueryAsIs((DelegateQueryAsIs) tupleSet, smartContext);

			case delegateQuerySet:
				return new EvalDelegateQuerySet((DelegateQuerySet) tupleSet, smartContext);

			case delegateQueryJoin:
				return new EvalDelegateQueryJoin((DelegateQueryJoin) tupleSet, smartContext);

			case orderedConcatenation:
				return new EvalOrderedConcatenation((OrderedConcatenation) tupleSet, smartContext);

			case staticTuple:
				return new EvalStaticTuple((StaticTuple) tupleSet, smartContext);
				
			case staticTuples:
				return new EvalStaticTuples((StaticTuples) tupleSet, smartContext);
		}

		throw new RuntimeQueryEvaluationException("Unsupported SmartTupleSet: " + tupleSet + " of type: " + tupleSet.smartType());
	}

}
