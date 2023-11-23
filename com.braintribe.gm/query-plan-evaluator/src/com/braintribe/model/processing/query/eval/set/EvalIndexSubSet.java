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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.queryplan.set.IndexSubSet;

/**
 * 
 */
public class EvalIndexSubSet implements EvalTupleSet {

	protected final Iterable<Tuple> tuples;

	public EvalIndexSubSet(IndexSubSet indexSubSet, QueryEvaluationContext context) {
		Object keys = context.resolveValue(null, indexSubSet.getKeys());

		tuples = context.getAllValuesForIndices(indexSubSet.getLookupIndex(), toCollection(keys, context));
	}

	private Collection<?> toCollection(Object keys, QueryEvaluationContext context) {
		if (keys == null)
			return Collections.emptySet();

		if (keys instanceof Collection)
			return ((Collection<?>) keys).stream() //
					.map(context::resolveStaticValue) //
					.collect(Collectors.toList());

		return Arrays.asList(context.resolveStaticValue(keys));
	}

	@Override
	public Iterator<Tuple> iterator() {
		return tuples.iterator();
	}

}
