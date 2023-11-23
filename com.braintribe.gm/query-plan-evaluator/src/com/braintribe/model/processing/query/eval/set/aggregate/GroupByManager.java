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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cc.lcd.HashingComparator;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.tools.QueryFunctionAnalyzer;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.value.HashSetProjection;
import com.braintribe.model.queryplan.value.IndexValue;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueProperty;

/**
 * 
 * @author peter.gazdik
 */
class GroupByManager {

	private final List<Value> groupByValues;
	private final Map<Value, Boolean> explicitGroupBys = CodingMap.create(new ValueComparator());

	private static final Boolean MARKED = Boolean.TRUE;
	private static final Boolean NOT_MARKED = Boolean.FALSE;

	public GroupByManager(AggregatingProjection projection) {
		this.groupByValues = projection.getGroupByValues();

		if (active())
			for (Value groupByValue: groupByValues)
				explicitGroupBys.put(groupByValue, NOT_MARKED);
	}

	public void notifySelectValue(Value value, int position) {
		if (!active())
			return;

		if (!explicitGroupBys.containsKey(value))
			throw new RuntimeQueryEvaluationException("Value selected on position: " + position +
					" does not appear in the GROUP BY clause nor is used in an aggregate function.  ");

		explicitGroupBys.put(value, MARKED);
	}

	public void addGroupBysIfEligible(List<Value> values) {
		if (!active())
			return;

		for (Entry<Value, Boolean> entry: explicitGroupBys.entrySet())
			if (entry.getValue() != MARKED)
				values.add(entry.getKey());
	}

	private boolean active() {
		return !groupByValues.isEmpty();
	}

	private class ValueComparator implements HashingComparator<Value> {

		public ValueComparator() {
		}

		@Override
		public boolean compare(Value v1, Value v2) {
			return v1.valueType() == v2.valueType() && deepCompare(v1, v2);
		}

		private boolean deepCompare(Value v1, Value v2) {
			switch (v1.valueType()) {
				case aggregateFunction:
					throw new RuntimeQueryEvaluationException("Error, aggregate function cannot be a part of the GROUP BY clause.");
				case extension:
					throw new RuntimeQueryEvaluationException("Error. Value not expected here: " + v1);
				case hashSetProjection:
					return areEqual((HashSetProjection) v1, (HashSetProjection) v2);
				case indexValue:
					return areEqual((IndexValue) v1, (IndexValue) v2);
				case queryFunction:
					return areEqual((QueryFunctionValue) v1, (QueryFunctionValue) v2);
				case staticValue:
					return areEqual((StaticValue) v1, (StaticValue) v2);
				case tupleComponent:
					return areEqual((TupleComponent) v1, (TupleComponent) v2);
				case valueProperty:
					return areEqual((ValueProperty) v1, (ValueProperty) v2);
				default:
					throw new RuntimeQueryEvaluationException("Unsupported value: " + v1);
			}
		}

		private boolean areEqual(HashSetProjection v1, HashSetProjection v2) {
			return v1.getTupleSet() == v2.getTupleSet() && compare(v1.getValue(), v2.getValue());
		}

		private boolean areEqual(IndexValue v1, IndexValue v2) {
			return v1.getIndexId().equals(v2.getIndexId()) && compare(v1.getKeys(), v2.getKeys());
		}

		private boolean areEqual(QueryFunctionValue v1, QueryFunctionValue v2) {
			Collection<?> o1 = QueryFunctionAnalyzer.findOperands(v1.getQueryFunction());
			Collection<?> o2 = QueryFunctionAnalyzer.findOperands(v2.getQueryFunction());

			Map<Object, Value> operandMappings1 = v1.getOperandMappings();
			Map<Object, Value> operandMappings2 = v2.getOperandMappings();

			if (o1.size() != o2.size())
				return false;

			Iterator<?> it1 = o1.iterator();
			Iterator<?> it2 = o2.iterator();

			while (it1.hasNext()) {
				Value operand1 = operandMappings1.get(it1.next());
				Value operand2 = operandMappings2.get(it2.next());

				if (!compare(operand1, operand2))
					return false;
			}

			return true;
		}

		private boolean areEqual(StaticValue v1, StaticValue v2) {
			return v1.getValue() != null ? v1.getValue().equals(v2.getValue()) : v2.getValue() == null;
		}

		private boolean areEqual(TupleComponent v1, TupleComponent v2) {
			return v1.getTupleComponentIndex() == v2.getTupleComponentIndex();
		}

		private boolean areEqual(ValueProperty v1, ValueProperty v2) {
			return v1.getPropertyPath().equals(v2.getPropertyPath()) && compare(v1.getValue(), v2.getValue());
		}

		@Override
		public int computeHash(Value v) {
			return 31 * deepHash(v) + v.valueType().hashCode();
		}

		private int deepHash(Value v) {
			switch (v.valueType()) {
				case aggregateFunction:
					throw new RuntimeQueryEvaluationException("Error, aggregate function cannot be a part of the GROUP BY clause.");
				case extension:
					throw new RuntimeQueryEvaluationException("Error. Value not expected here: " + v);
				case hashSetProjection:
					return hash((HashSetProjection) v);
				case indexValue:
					return hash((IndexValue) v);
				case queryFunction:
					return hash((QueryFunctionValue) v);
				case staticValue:
					return hash((StaticValue) v);
				case tupleComponent:
					return hash((TupleComponent) v);
				case valueProperty:
					return hash((ValueProperty) v);
				default:
					throw new RuntimeQueryEvaluationException("Unsupported value: " + v);
			}
		}

		private int hash(HashSetProjection v) {
			return 31 * computeHash(v.getValue()) + v.getTupleSet().hashCode();
		}

		private int hash(IndexValue v) {
			return 31 * computeHash(v.getKeys()) + v.getIndexId().hashCode();
		}

		private int hash(QueryFunctionValue v) {
			int result = 0;

			Map<Object, Value> operandMappings = v.getOperandMappings();

			for (Object o: QueryFunctionAnalyzer.findOperands(v.getQueryFunction()))
				result = 31 * result + computeHash(operandMappings.get(o));

			return result;
		}

		private int hash(StaticValue v) {
			Object vv = v.getValue();
			return vv != null ? vv.hashCode() : 0;
		}

		private int hash(TupleComponent v) {
			return v.getTupleComponentIndex();
		}

		private int hash(ValueProperty v) {
			return 31 * computeHash(v.getValue()) + v.getPropertyPath().hashCode();
		}

	}

}
