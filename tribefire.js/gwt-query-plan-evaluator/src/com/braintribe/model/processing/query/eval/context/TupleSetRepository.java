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
package com.braintribe.model.processing.query.eval.context;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.set.EvalAggregatingProjection;
import com.braintribe.model.processing.query.eval.set.EvalCartesianProduct;
import com.braintribe.model.processing.query.eval.set.EvalConcatenation;
import com.braintribe.model.processing.query.eval.set.EvalDistinctSet;
import com.braintribe.model.processing.query.eval.set.EvalFilteredSet;
import com.braintribe.model.processing.query.eval.set.EvalIndexOrderedSet;
import com.braintribe.model.processing.query.eval.set.EvalIndexRange;
import com.braintribe.model.processing.query.eval.set.EvalIndexSubSet;
import com.braintribe.model.processing.query.eval.set.EvalIntersection;
import com.braintribe.model.processing.query.eval.set.EvalMergeLookupJoin;
import com.braintribe.model.processing.query.eval.set.EvalMergeRangeJoin;
import com.braintribe.model.processing.query.eval.set.EvalOrderedSet;
import com.braintribe.model.processing.query.eval.set.EvalOrderedSetRefinement;
import com.braintribe.model.processing.query.eval.set.EvalPaginatedSet;
import com.braintribe.model.processing.query.eval.set.EvalProjection;
import com.braintribe.model.processing.query.eval.set.EvalQuerySourceSet;
import com.braintribe.model.processing.query.eval.set.EvalSourceSet;
import com.braintribe.model.processing.query.eval.set.EvalStaticSet;
import com.braintribe.model.processing.query.eval.set.EvalUnion;
import com.braintribe.model.processing.query.eval.set.join.EvalEntityJoin;
import com.braintribe.model.processing.query.eval.set.join.EvalIndexLookupJoin;
import com.braintribe.model.processing.query.eval.set.join.EvalIndexRangeJoin;
import com.braintribe.model.processing.query.eval.set.join.EvalListJoin;
import com.braintribe.model.processing.query.eval.set.join.EvalMapJoin;
import com.braintribe.model.processing.query.eval.set.join.EvalSetJoin;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.DistinctSet;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.Intersection;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.OrderedSetRefinement;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.QuerySourceSet;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.Union;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;
import com.braintribe.model.queryplan.set.join.IndexRangeJoin;
import com.braintribe.model.queryplan.set.join.ListJoin;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.set.join.SetJoin;

/**
 * 
 */
public class TupleSetRepository {

	protected final QueryEvaluationContext context;
	protected Map<TupleSet, EvalTupleSet> map = newMap();

	public TupleSetRepository(QueryEvaluationContext context) {
		this.context = context;
	}

	protected EvalTupleSet resolveTupleSet(TupleSet tupleSet) {
		EvalTupleSet result = map.get(tupleSet);

		if (result == null) {
			result = newEvalTupleSetFor(tupleSet);
			map.put(tupleSet, result);
		}

		return result;
	}

	protected EvalTupleSet newEvalTupleSetFor(TupleSet tupleSet) {
		switch (tupleSet.tupleSetType()) {
			case aggregatingProjection:
				return new EvalAggregatingProjection((AggregatingProjection) tupleSet, context);
			case cartesianProduct:
				return new EvalCartesianProduct((CartesianProduct) tupleSet, context);
			case concatenation:
				return new EvalConcatenation((Concatenation) tupleSet, context);
			case distinctSet:
				return new EvalDistinctSet((DistinctSet) tupleSet, context);
			case entityJoin:
				return new EvalEntityJoin((EntityJoin) tupleSet, context);
			case filteredSet:
				return new EvalFilteredSet((FilteredSet) tupleSet, context);
			case indexLookupJoin:
				return new EvalIndexLookupJoin((IndexLookupJoin) tupleSet, context);
			case indexOrderedSet:
				return new EvalIndexOrderedSet((IndexOrderedSet) tupleSet, context);
			case indexRange:
				return new EvalIndexRange((IndexRange) tupleSet, context);
			case indexRangeJoin:
				return new EvalIndexRangeJoin((IndexRangeJoin) tupleSet, context);
			case indexSubSet:
				return new EvalIndexSubSet((IndexSubSet) tupleSet, context);
			case intersection:
				return new EvalIntersection((Intersection) tupleSet, context);
			case listJoin:
				return new EvalListJoin((ListJoin) tupleSet, context);
			case mapJoin:
				return new EvalMapJoin((MapJoin) tupleSet, context);
			case mergeLookupJoin:
				return new EvalMergeLookupJoin((MergeLookupJoin) tupleSet, context);
			case mergeRangeJoin:
				return new EvalMergeRangeJoin((MergeRangeJoin) tupleSet, context);
			case orderedSet:
				return new EvalOrderedSet((OrderedSet) tupleSet, context);
			case orderedSetRefinement:
				return new EvalOrderedSetRefinement((OrderedSetRefinement) tupleSet, context);
			case pagination:
				return new EvalPaginatedSet((PaginatedSet) tupleSet, context);
			case projection:
				return new EvalProjection((Projection) tupleSet, context);
			case querySourceSet:
				return new EvalQuerySourceSet((QuerySourceSet) tupleSet, context);
			case setJoin:
				return new EvalSetJoin((SetJoin) tupleSet, context);
			case sourceSet:
				return new EvalSourceSet((SourceSet) tupleSet, context);
			case staticSet:
				return new EvalStaticSet((StaticSet) tupleSet, context);
			case union:
				return new EvalUnion((Union) tupleSet, context);
			default:
				throw new RuntimeQueryEvaluationException("Unsupported TupleSet: " + tupleSet + " of type: " + tupleSet.tupleSetType());
		}
	}

}
