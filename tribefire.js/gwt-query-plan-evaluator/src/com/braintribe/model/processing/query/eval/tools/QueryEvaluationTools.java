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
package com.braintribe.model.processing.query.eval.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.HasMoreAwareSet;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * General class for various util methods.
 */
public class QueryEvaluationTools {

	public static List<EvalTupleSet> resolveTupleSets(List<TupleSet> tupleSets, QueryEvaluationContext context) {
		List<EvalTupleSet> result = newList(tupleSets.size());

		for (TupleSet ts: tupleSets)
			result.add(context.resolveTupleSet(ts));

		return result;
	}

	public static void addAllTuples(TupleSet tupleSet, Collection<Tuple> tuples, QueryEvaluationContext context) {
		addAllTuples(context.resolveTupleSet(tupleSet), tuples);
	}

	public static void addAllTuples(EvalTupleSet evalTupleSet, Collection<Tuple> tuples) {
		for (Tuple tuple: evalTupleSet)
			tuples.add(tuple.detachedCopy());
	}

	public static Set<Tuple> tupleHashSet(int tupleSize) {
		return CodingSet.create(new TupleHashingComparator(tupleSize));
	}

	/**
	 * I'm using the Yoda-style name so that it does not collide with {@link HasMoreAwareSet#hasMore()} (cause in that
	 * case we would not be able to make static imports inside classes implementing that interface).
	 * 
	 * May the force be with you!
	 */
	public static boolean moreHas(EvalTupleSet tupleSet) {
		return tupleSet instanceof HasMoreAwareSet && ((HasMoreAwareSet) tupleSet).hasMore();
	}

	public static Iterator<Tuple> emptyTupleIterator() {
		return Collections.<Tuple> emptySet().iterator();
	}

}
