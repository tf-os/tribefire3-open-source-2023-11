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
package com.braintribe.model.processing.query.eval.set.aggregate;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.aggregate.arithmetic.ArithmeticAggregateExpertRegistry;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.value.AggregationFunctionType;

/**
 * An expert that can takes care of computing one aggregate function (i.e. query with 3 aggregate functions in the
 * select clause needs three instances of this class). This class is initialized with the
 * {@link AggregationFunctionType} is able to incrementally be computing the right value for
 */
public class AggregateFunctionExpert {

	private final int position;
	private final AggregationFunctionType type;
	private ArithmeticExpert<Object> arithmeticExpert;

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	public AggregateFunctionExpert(int position, AggregationFunctionType type) {
		this.position = position;
		this.type = type;
		this.arithmeticExpert = ArithmeticAggregateExpertRegistry.getExpertIfPossible(type);
	}

	void initValue(ArrayBasedTuple groupTuple) {
		groupTuple.setValueDirectly(position, initialValue());
	}

	private Object initialValue() {
		switch (type) {
			case count:
			case countDistinct:
				return 0L;
			default:
				return null;
		}
	}

	void addTuple(ArrayBasedTuple groupTuple, Tuple tuple) {
		Object currentValue = groupTuple.getValue(position);

		Object newValue = getValueToAdd(currentValue, tuple);

		groupTuple.setValueDirectly(position, newValue);
	}

	private Object getValueToAdd(Object currentValue, Tuple tuple) {
		Object value = tuple.getValue(position);

		switch (type) {
			case avg:
				return addAvg((Entry) currentValue, value);

			case sum:
				return add(currentValue, value);

			case count:
				return ((Long) currentValue) + (value == null ? 0L : 1L);

			case countDistinct:
				// current value is Integer, value is anything
				return arithmeticExpert.add(currentValue, value);

			case max:
				return compare(currentValue, value, true);

			case min:
				return compare(currentValue, value, false);

			default:
				return null;
		}
	}

	private Object addAvg(Entry currentValue, Object value) {
		if (value == null) {
			return currentValue;
		}

		if (currentValue == null) {
			currentValue = new Entry();
		}

		currentValue.o = add(currentValue.o, value);
		currentValue.count++;
		return currentValue;
	}

	private Object add(Object v1, Object v2) {
		if (v1 == null) {
			return v2;
		}

		return acquireArithmeticExpert(v1).add(v1, v2);
	}

	private Object compare(Object v1, Object v2, boolean bigger) {
		if (v1 == null) {
			return v2;
		}

		if (v2 == null) {
			return v1;
		}

		int cmp = acquireArithmeticExpert(v1).compare(v1, v2);

		return (cmp >= 0) == bigger ? v1 : v2;
	}

	void finalizeValue(ArrayBasedTuple groupTuple) {
		if (type == AggregationFunctionType.avg) {
			Entry currentValue = (Entry) groupTuple.getValue(position);

			groupTuple.setValueDirectly(position, countAverage(currentValue));
		}
	}

	private Object countAverage(Entry e) {
		return e != null ? acquireArithmeticExpert(e.o).divide(e.o, e.count) : null;
	}

	private ArithmeticExpert<Object> acquireArithmeticExpert(Object value) {
		if (arithmeticExpert == null) {
			loadArithmeticExpertByValue(value);
		}

		return arithmeticExpert;
	}

	private void loadArithmeticExpertByValue(Object value) {
		GenericModelType gmType = typeReflection.getType(value);

		arithmeticExpert = ArithmeticAggregateExpertRegistry.getExpertFor(gmType);
	}

	private static class Entry {
		Object o;
		int count;
	}
}
