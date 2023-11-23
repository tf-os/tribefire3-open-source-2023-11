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

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.tuple.ArrayBasedTuple;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.value.AggregateFunction;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class AggregateProjectionApplier {

	private final AggregatingProjection projection;
	private final Projection preGroupingProjection;
	private final QueryEvaluationContext context;

	private final GroupByManager groupByManager;

	private final int componentsCount;

	private final Map<TupleGroup, ArrayBasedTuple> aggregateMap = newLinkedMap();

	private final int aggregateSize;
	private final int[] aggregateIndices;
	private final AggregateFunctionExpert[] aggregateExperts;
	private final int groupBySize;
	private final int[] groupByIndices;

	public AggregateProjectionApplier(AggregatingProjection projection, QueryEvaluationContext context) {
		this.projection = projection;
		this.context = context;

		this.groupByManager = new GroupByManager(projection);

		this.preGroupingProjection = preGroupingProjection();
		this.componentsCount = preGroupingProjection.getValues().size();

		Set<Integer> aggregateIndices = findAggregateIndices();
		this.aggregateSize = aggregateIndices.size();
		this.groupBySize = componentsCount - aggregateSize;

		this.aggregateIndices = getAggregatedIndicesArray(aggregateIndices);
		this.aggregateExperts = getAggregateExpertsArray();
		this.groupByIndices = getGroupByIndicesArray(aggregateIndices);
	}

	private Projection preGroupingProjection() {
		List<Value> aggregateValues = projection.getValues();

		List<Value> resultValues = newList(aggregateValues.size());

		int position = 0;
		for (Value value: aggregateValues) {
			if (value instanceof AggregateFunction) {
				resultValues.add(((AggregateFunction) value).getOperand());

			} else {
				resultValues.add(value);
				groupByManager.notifySelectValue(value, position);
			}

			position++;
		}

		groupByManager.addGroupBysIfEligible(resultValues);

		Projection result = Projection.T.create();
		result.setOperand(projection.getOperand());
		result.setValues(resultValues);

		return result;
	}

	private Set<Integer> findAggregateIndices() {
		Set<Integer> result = newSet();

		int index = 0;
		for (Value value: projection.getValues()) {
			if (value instanceof AggregateFunction)
				result.add(index);

			index++;
		}

		return result;
	}

	private int[] getAggregatedIndicesArray(Set<Integer> aggregateIndices) {
		int[] result = new int[aggregateSize];

		int i = 0;
		for (Integer index: aggregateIndices)
			result[i++] = index;

		return result;
	}

	private AggregateFunctionExpert[] getAggregateExpertsArray() {
		AggregateFunctionExpert[] result = new AggregateFunctionExpert[aggregateSize];

		int i = 0;
		List<Value> aggregateValues = projection.getValues();
		for (int position: aggregateIndices) {
			AggregateFunction af = (AggregateFunction) aggregateValues.get(position);

			result[i++] = new AggregateFunctionExpert(position, af.getAggregationFunctionType());
		}

		return result;
	}

	private int[] getGroupByIndicesArray(Set<Integer> aggregateIndices) {
		int[] result = new int[groupBySize];

		int i = 0;
		for (int j = 0; j < componentsCount; j++)
			if (!aggregateIndices.contains(j))
				result[i++] = j;

		return result;
	}

	public Collection<? extends Tuple> createAggregatedTuples() {
		EvalTupleSet preGroupingTuples = context.resolveTupleSet(preGroupingProjection);
		Iterator<Tuple> it = preGroupingTuples.iterator();

		if (it.hasNext())
			return aggregateFor(it);
		else
			return aggreagateForEmptySet();
	}

	private Collection<? extends Tuple> aggreagateForEmptySet() {
		if (groupByIndices.length > 0)
			return Collections.emptySet();

		ArrayBasedTuple tuple = new ArrayBasedTuple(componentsCount);
		setDefaultAggregateValuesFor(tuple);

		return Arrays.asList(tuple);
	}

	private Collection<? extends Tuple> aggregateFor(Iterator<Tuple> it) {
		while (it.hasNext()) {
			Tuple tuple = it.next();

			TupleGroup tupleGroup = getTupleGroupFor(tuple);
			ArrayBasedTuple groupTuple = acquireTuple(tupleGroup, tuple);

			appendTuple(groupTuple, tuple);
		}

		for (ArrayBasedTuple aggregateTuple: aggregateMap.values())
			finalizeAggregateTuples(aggregateTuple);

		return aggregateMap.values();
	}

	private TupleGroup getTupleGroupFor(Tuple tuple) {
		Object[] os = new Object[groupBySize];
		int i = 0;
		for (int groupByIndex: groupByIndices)
			os[i++] = tuple.getValue(groupByIndex);

		return new TupleGroup(os);
	}

	private ArrayBasedTuple acquireTuple(TupleGroup tupleGroup, Tuple tuple) {
		ArrayBasedTuple result = aggregateMap.get(tupleGroup);

		if (result == null) {
			result = new ArrayBasedTuple(componentsCount);
			aggregateMap.put(tupleGroup, result);

			result.acceptValuesFrom(tuple);
			setDefaultAggregateValuesFor(result);
		}

		return result;
	}

	private void setDefaultAggregateValuesFor(ArrayBasedTuple result) {
		for (AggregateFunctionExpert expert: aggregateExperts)
			expert.initValue(result);
	}

	private void appendTuple(ArrayBasedTuple groupTuple, Tuple tuple) {
		for (AggregateFunctionExpert expert: aggregateExperts)
			expert.addTuple(groupTuple, tuple);
	}

	private void finalizeAggregateTuples(ArrayBasedTuple aggregateTuple) {
		for (AggregateFunctionExpert expert: aggregateExperts)
			expert.finalizeValue(aggregateTuple);
	}

	private static class TupleGroup {
		Object[] os;

		public TupleGroup(Object[] os) {
			this.os = os;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(os);
		}

		@Override
		public boolean equals(Object obj) {
			return Arrays.equals(os, ((TupleGroup) obj).os);
		}
	}
}
