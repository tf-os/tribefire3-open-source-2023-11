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
package com.braintribe.model.processing.query.planner.core.order;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.processing.query.planner.context.QueryOperandToValueConverter;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.OrderedSetRefinement;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueType;

/**
 * 
 */
public class OrderByProcessor {

	protected final QueryOperandToValueConverter valueConverter;
	protected final List<SortCriterion> sortCriteria;

	public static TupleSet applyOrdering(QueryPlannerContext valueConverter, List<SimpleOrdering> orderings, List<Value> groupValues,
			TupleSet operand) {
		return new OrderByProcessor(valueConverter).applyOrdering(orderings, groupValues, operand);
	}

	protected OrderByProcessor(QueryOperandToValueConverter valueConverter) {
		this.valueConverter = valueConverter;
		this.sortCriteria = newList();
	}

	protected TupleSet applyOrdering(List<SimpleOrdering> orderings, List<Value> groupValues, TupleSet operand) {
		OrderedSet result = newOrderedSet(groupValues);
		result.setOperand(operand);
		result.setSortCriteria(sortCriteria);

		for (SimpleOrdering simpleOrdering : orderings)
			applyOrdering(simpleOrdering);

		// If someone would try to order by a constant and nothing else, we would end up with empty sort criteria
		return sortCriteria.isEmpty() ? result.getOperand() : result;
	}

	private OrderedSet newOrderedSet(List<Value> groupValues) {
		if (groupValues.isEmpty())
			return OrderedSet.T.create();

		OrderedSetRefinement result = OrderedSetRefinement.T.create();
		result.setGroupValues(groupValues);

		return result;
	}

	protected void applyOrdering(SimpleOrdering ordering) {
		SortCriterion criterion = SortCriterion.T.create();

		Value value = valueConverter.convertOperand(ordering.getOrderBy());

		if (value.valueType() == ValueType.staticValue)
			return;

		criterion.setDescending(ordering.getDirection() == OrderingDirection.descending);
		criterion.setValue(value);

		sortCriteria.add(criterion);
	}

}
