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

import java.util.Comparator;
import java.util.List;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.tools.ScalarComparator;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class TupleComparator implements Comparator<Tuple> {

	private final List<SortCriterion> sortCriteria;
	private final int valuesCount;
	private final QueryEvaluationContext context;

	public TupleComparator(List<SortCriterion> sortCriteria, QueryEvaluationContext context) {
		this.sortCriteria = sortCriteria;
		this.valuesCount = sortCriteria.size();
		this.context = context;
	}

	@Override
	public int compare(Tuple t1, Tuple t2) {
		for (int i = 0; i < valuesCount; i++) {
			SortCriterion sortCriterion = sortCriteria.get(i);
			int cmp = compareValue(t1, t2, sortCriterion.getValue());

			if (cmp != 0)
				return sortCriterion.getDescending() ? -cmp : cmp;
		}

		return 0;
	}

	private int compareValue(Tuple t1, Tuple t2, Value value) {
		Object value1 = context.resolveValue(t1, value);
		Object value2 = context.resolveValue(t2, value);

		if (value1 == value2)
			return 0;

		if (value1 == null)
			return -1;

		if (value2 == null)
			return 1;

		if (value1.getClass() != value2.getClass())
			return ScalarComparator.INSTANCE.compare(value1, value2);
		
		if (value1 instanceof String)
			return StringAlphabeticalComparator.INSTANCE.compare((String) value1, (String) value2);

		if (value1 instanceof Comparable)
			return ((Comparable<Object>) value1).compareTo(value2);

		if (value1 instanceof LocalizedString) {
			LocalizedString ls1 = (LocalizedString) value1;
			LocalizedString ls2 = (LocalizedString) value2;

			String s1 = context.resolveLocalizedString(ls1);
			String s2 = context.resolveLocalizedString(ls2);

			return StringAlphabeticalComparator.INSTANCE.compare(s1, s2);
		}

		throw new RuntimeQueryEvaluationException("Cannot compare values: [" + value1 + ", " + value2 +
				"]. Wrong type. Only Comparables and LocalizedStrings are supported!");
	}
}
