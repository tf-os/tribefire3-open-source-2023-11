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
package com.braintribe.model.processing.query.eval.set.base;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.planner.builder.ValueBuilder;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.queryplan.TupleComponentPosition;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.filter.ConditionType;
import com.braintribe.model.queryplan.filter.Conjunction;
import com.braintribe.model.queryplan.filter.Contains;
import com.braintribe.model.queryplan.filter.Disjunction;
import com.braintribe.model.queryplan.filter.Equality;
import com.braintribe.model.queryplan.filter.FullText;
import com.braintribe.model.queryplan.filter.GreaterThan;
import com.braintribe.model.queryplan.filter.GreaterThanOrEqual;
import com.braintribe.model.queryplan.filter.ILike;
import com.braintribe.model.queryplan.filter.In;
import com.braintribe.model.queryplan.filter.InstanceOf;
import com.braintribe.model.queryplan.filter.Junction;
import com.braintribe.model.queryplan.filter.LessThan;
import com.braintribe.model.queryplan.filter.LessThanOrEqual;
import com.braintribe.model.queryplan.filter.Like;
import com.braintribe.model.queryplan.filter.Negation;
import com.braintribe.model.queryplan.filter.Unequality;
import com.braintribe.model.queryplan.filter.ValueComparison;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.index.MetricIndex;
import com.braintribe.model.queryplan.index.RepositoryIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.CombinedSet;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.DistinctSet;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.Intersection;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.OrderedSetRefinement;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.QuerySourceSet;
import com.braintribe.model.queryplan.set.SortCriterion;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.Union;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.JoinKind;
import com.braintribe.model.queryplan.set.join.JoinedListIndex;
import com.braintribe.model.queryplan.set.join.JoinedMapKey;
import com.braintribe.model.queryplan.set.join.ListJoin;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.set.join.PropertyJoin;
import com.braintribe.model.queryplan.set.join.SetJoin;
import com.braintribe.model.queryplan.value.ConstantValue;
import com.braintribe.model.queryplan.value.HashSetProjection;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.queryplan.value.ValueProperty;
import com.braintribe.model.queryplan.value.range.SimpleRange;
import com.braintribe.utils.CollectionTools;

/**
 * 
 */
public class TupleSetBuilder {

	protected int index = 0;

	public int getIndex() {
		return index;
	}

	public Projection aggregateProjection(TupleSet operand, Value... values) {
		AggregatingProjection result = AggregatingProjection.T.create();
		result.setOperand(operand);
		result.setValues(Arrays.asList(values));

		return result;
	}

	public Projection projection(TupleSet operand, Value... values) {
		Projection result = Projection.T.create();
		result.setOperand(operand);
		result.setValues(Arrays.asList(values));

		return result;
	}

	public QuerySourceSet querySourceSet(Class<? extends GenericEntity> clazz) {
		QuerySourceSet result = newTupleSet(QuerySourceSet.T.create());
		result.setEntityTypeSignature(clazz.getName());

		return result;
	}

	public SourceSet sourceSet(Class<? extends GenericEntity> clazz) {
		SourceSet result = newTupleSet(SourceSet.T.create());
		result.setTypeSignature(clazz.getName());

		return result;
	}

	public StaticSet staticSet(Object... os) {
		StaticSet result = newTupleSet(StaticSet.T.create());
		result.setValues(CollectionTools.getSet(os));

		return result;
	}

	public IndexRange indexRange(Class<? extends GenericEntity> clazz, String propertyName, MetricIndex metricIndex, Object lowerBound,
			Boolean lowerInclusive, Object upperBound, Boolean upperInclusive) {

		IndexRange result = newTupleSet(IndexRange.T.create());
		result.setTypeSignature(clazz.getName());
		result.setPropertyName(propertyName);
		result.setMetricIndex(metricIndex);
		result.setRange(simpleRange(lowerBound, lowerInclusive, upperBound, upperInclusive));

		if (metricIndex instanceof RepositoryMetricIndex)
			((RepositoryMetricIndex) metricIndex).setTupleComponentIndex(result.getIndex());

		return result;
	}

	private SimpleRange simpleRange(Object lowerBound, Boolean lowerInclusive, Object upperBound, Boolean upperInclusive) {
		SimpleRange result = SimpleRange.T.create();

		result.setLowerBound(toRangeValue(lowerBound, lowerInclusive));
		result.setLowerInclusive(Boolean.TRUE.equals(lowerInclusive));
		result.setUpperBound(toRangeValue(upperBound, upperInclusive));
		result.setUpperInclusive(Boolean.TRUE.equals(upperInclusive));

		return result;
	}

	private StaticValue toRangeValue(Object bound, Boolean inclusive) {
		return inclusive == null ? null : ValueBuilder.staticValue(bound);
	}

	public IndexOrderedSet indexOrderedSet(Class<? extends GenericEntity> clazz, String propertyName, MetricIndex metricIndex, boolean descending) {
		IndexOrderedSet result = newTupleSet(IndexOrderedSet.T.create());
		result.setTypeSignature(clazz.getName());
		result.setPropertyName(propertyName);
		result.setMetricIndex(metricIndex);
		result.setDescending(descending);

		if (metricIndex instanceof RepositoryIndex)
			((RepositoryIndex) metricIndex).setTupleComponentIndex(result.getIndex());

		return result;
	}

	public IndexSubSet indexSubSet(Class<? extends GenericEntity> clazz, String propertyName, Index lookupIndex, ConstantValue keys) {
		IndexSubSet result = newTupleSet(IndexSubSet.T.create());
		result.setTypeSignature(clazz.getName());
		result.setPropertyName(propertyName);
		result.setLookupIndex(lookupIndex);
		result.setKeys(keys);

		if (lookupIndex instanceof RepositoryIndex)
			((RepositoryIndex) lookupIndex).setTupleComponentIndex(result.getIndex());

		return result;
	}

	public FilteredSet filteredSet(TupleSet operand, Condition filter) {
		FilteredSet result = FilteredSet.T.create();
		result.setOperand(operand);
		result.setFilter(filter);

		return result;
	}

	public CartesianProduct cartesianProduct(TupleSet... operands) {
		CartesianProduct result = CartesianProduct.T.create();
		result.setOperands(Arrays.asList(operands));

		return result;
	}

	public DistinctSet distinctSet(TupleSet operand) {
		DistinctSet result = DistinctSet.T.create();
		result.setOperand(operand);

		return result;
	}

	public EntityJoin entityJoin(TupleSet operand, ValueProperty joinProperty, JoinKind joinKind) {
		return propertyJoin(EntityJoin.T.create(), operand, joinProperty, joinKind);
	}

	public SetJoin setJoin(TupleSet operand, ValueProperty joinProperty, JoinKind joinKind) {
		return propertyJoin(SetJoin.T.create(), operand, joinProperty, joinKind);
	}

	public ListJoin listJoin(TupleSet operand, ValueProperty joinProperty, JoinKind joinKind) {
		ListJoin result = ListJoin.T.create();
		result.setListIndex(newTupleSet(JoinedListIndex.T.create()));
		propertyJoin(result, operand, joinProperty, joinKind);

		return result;
	}

	public MapJoin mapJoin(TupleSet operand, ValueProperty joinProperty, JoinKind joinKind) {
		MapJoin result = MapJoin.T.create();
		result.setMapKey(newTupleSet(JoinedMapKey.T.create()));
		propertyJoin(result, operand, joinProperty, joinKind);

		return result;
	}

	public PaginatedSet paginatedSet(TupleSet operand, int limit, int offset) {
		PaginatedSet result = PaginatedSet.T.create();
		result.setOperand(operand);
		result.setLimit(limit);
		result.setOffset(offset);

		return result;
	}

	private <T extends PropertyJoin> T propertyJoin(T result, TupleSet operand, ValueProperty joinProperty, JoinKind joinKind) {
		newTupleSet(result);

		result.setOperand(operand);
		result.setValueProperty(joinProperty);
		result.setJoinKind(joinKind);

		return result;
	}

	public Concatenation concatenation(TupleSet firstOperand, TupleSet secondOperand) {
		return combined(Concatenation.T.create(), firstOperand, secondOperand);
	}

	public TupleSet orderedSet(TupleSet operand, Value value, boolean descending) {
		OrderedSet result = OrderedSet.T.create();
		result.setOperand(operand);
		result.setSortCriteria(Arrays.asList(sortCriterium(value, descending)));

		return result;
	}

	public OrderedSetRefinement orderedSetRefinement(TupleSet operand, Value value, boolean descending, List<Value> groupValues) {
		OrderedSetRefinement result = OrderedSetRefinement.T.create();
		result.setOperand(operand);
		result.setSortCriteria(asList(sortCriterium(value, descending)));
		result.setGroupValues(groupValues);

		return result;
	}

	public SortCriterion sortCriterium(Value value, boolean descending) {
		SortCriterion result = SortCriterion.T.create();
		result.setValue(value);
		result.setDescending(descending);

		return result;
	}

	public Union union(TupleSet firstOperand, TupleSet secondOperand) {
		return combined(Union.T.create(), firstOperand, secondOperand);
	}

	public Intersection intersection(TupleSet firstOperand, TupleSet secondOperand) {
		return combined(Intersection.T.create(), firstOperand, secondOperand);
	}

	private <T extends CombinedSet> T combined(T result, TupleSet firstOperand, TupleSet secondOperand) {
		result.setFirstOperand(firstOperand);
		result.setSecondOperand(secondOperand);

		return result;
	}

	private <T extends TupleComponentPosition> T newTupleSet(T tupleSet) {
		tupleSet.setIndex(index++);

		return tupleSet;
	}

	// ###################################
	// ## . . . . . . Value . . . . . . ##
	// ###################################

	public static QueryFunctionValue localize(Object localizedStringOperand, String locale, Value operandValue) {
		Localize localize = Localize.T.create();
		localize.setLocalizedStringOperand(localizedStringOperand);
		localize.setLocale(locale);

		QueryFunctionValue result = QueryFunctionValue.T.create();
		result.setQueryFunction(localize);
		result.setOperandMappings(asMap(localizedStringOperand, operandValue));

		return result;
	}

	public static HashSetProjection hashSetProjection(TupleSet tupleSet, Value value) {
		HashSetProjection result = HashSetProjection.T.create();
		result.setTupleSet(tupleSet);
		result.setValue(value);

		return result;
	}

	public static StaticValue staticValue(Object value) {
		StaticValue result = StaticValue.T.create();
		result.setValue(value);

		return result;
	}

	public static TupleComponent tupleComponent(TupleComponentPosition position) {
		return tupleComponent(position.getIndex());
	}

	public static TupleComponent tupleComponent(int index) {
		TupleComponent result = TupleComponent.T.create();
		result.setTupleComponentIndex(index);

		return result;
	}

	public static ValueProperty valueProperty(TupleComponentPosition position, String propertyPath) {
		ValueProperty result = ValueProperty.T.create();
		result.setValue(tupleComponent(position));
		result.setPropertyPath(propertyPath);

		return result;
	}

	public static ValueProperty modelvalueProperty(TupleComponentPosition position, String propertyPath) {
		ValueProperty result = ValueProperty.T.create();
		result.setValue(tupleComponent(position));
		result.setPropertyPath(propertyPath);

		return result;
	}

	// ###################################
	// ## . . . . . Condition . . . . . ##
	// ###################################

	public static Condition not(Condition c) {
		Negation negation = Negation.T.create();
		negation.setOperand(c);

		return negation;
	}

	public static Condition and(Condition... operands) {
		return junction(Conjunction.T.create(), operands);
	}

	public static Condition or(Condition... operands) {
		return junction(Disjunction.T.create(), operands);
	}

	private static Condition junction(Junction junction, Condition... operands) {
		junction.setOperands(Arrays.asList(operands));

		return junction;
	}

	public static ValueComparison valueComparison(Value left, Value right, ConditionType conditionType) {
		ValueComparison result = newValueComparison(conditionType);

		result.setLeftOperand(left);
		result.setRightOperand(right);

		return result;
	}

	private static ValueComparison newValueComparison(ConditionType conditionType) {
		switch (conditionType) {
			case contains:
				return Contains.T.create();
			case equality:
				return Equality.T.create();
			case fullText:
				return FullText.T.create();
			case greater:
				return GreaterThan.T.create();
			case greaterOrEqual:
				return GreaterThanOrEqual.T.create();
			case ilike:
				return ILike.T.create();
			case in:
				return In.T.create();
			case instanceOf:
				return InstanceOf.T.create();
			case less:
				return LessThan.T.create();
			case lessOrEqual:
				return LessThanOrEqual.T.create();
			case like:
				return Like.T.create();
			case unequality:
				return Unequality.T.create();
			default:
				throw new RuntimeQueryEvaluationException("Unsupported condition type: " + conditionType);
		}
	}

}
