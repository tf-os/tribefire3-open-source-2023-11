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

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Iterator;
import java.util.Set;

import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSet;
import com.braintribe.model.processing.query.eval.tools.PopulationAsTupleIterator;
import com.braintribe.model.queryplan.set.StaticSet;

/**
 * 
 */
public class EvalStaticSet extends AbstractEvalTupleSet {

	protected final int index;
	protected final Set<Object> staticValues;

	public EvalStaticSet(StaticSet staticSet, QueryEvaluationContext context) {
		super(context);

		this.index = staticSet.getIndex();
		this.staticValues = resolveStaticValues(staticSet, context);
	}

	private Set<Object> resolveStaticValues(StaticSet staticSet, QueryEvaluationContext context) {
		Set<Object> result = newSet();

		for (Object value : staticSet.getValues()) {
			Object resolvedValue = context.resolveStaticValue(value);
			if (resolvedValue != null || value == null)
				result.add(resolvedValue);
		}

		return result;
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new PopulationAsTupleIterator(staticValues, index);
	}

}
