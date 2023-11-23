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
package com.braintribe.model.processing.query.planner.builder;

import static com.braintribe.model.generic.reflection.StrategyOnCriterionMatch.skip;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.processing.query.eval.api.repo.IndexInfo;
import com.braintribe.model.processing.query.planner.RuntimeQueryPlannerException;
import com.braintribe.model.processing.query.planner.context.QueryAggregationManager;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.processing.query.planner.context.QuerySourceManager;
import com.braintribe.model.processing.query.planner.core.index.IndexKeys;
import com.braintribe.model.processing.query.planner.tools.Bound;
import com.braintribe.model.query.From;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operand;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.queryplan.filter.Condition;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;
import com.braintribe.model.queryplan.index.MetricIndex;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.CombinedSet;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.DistinctSet;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
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
import com.braintribe.model.queryplan.set.join.JoinKind;
import com.braintribe.model.queryplan.set.join.JoinedListIndex;
import com.braintribe.model.queryplan.set.join.JoinedMapKey;
import com.braintribe.model.queryplan.set.join.ListJoin;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.set.join.PropertyJoin;
import com.braintribe.model.queryplan.set.join.SetJoin;
import com.braintribe.model.queryplan.value.Value;

/**
 * 
 */
public class TupleSetBuilder {

	public static SourceSet sourceSet(From from, QueryPlannerContext context) {
		SourceSet result = SourceSet.T.create();

		result.setTypeSignature(from.getEntityTypeSignature());
		result.setIndex(context.sourceManager().indexForSource(from));

		return result;
	}

	public static CartesianProduct cartesianProduct(List<TupleSet> operands) {
		CartesianProduct result = CartesianProduct.T.create();
		result.setOperands(operands);

		return result;
	}

	public static DistinctSet distinct(TupleSet tupleSet, int tupleSize) {
		DistinctSet result = DistinctSet.T.create();
		result.setOperand(tupleSet);
		result.setTupleSize(tupleSize);

		return result;
	}

	public static TupleSet filteredSet(TupleSet tupleSet, Collection<com.braintribe.model.query.conditions.Condition> conditions,
			QueryPlannerContext context) {

		return filteredSet(tupleSet, ConditionBuilder.condition(conditions, context));
	}

	public static FilteredSet filteredSet(TupleSet operand, Condition filter) {
		FilteredSet result = FilteredSet.T.create();
		result.setOperand(operand);
		result.setFilter(filter);

		return result;
	}

	public static IndexSubSet indexSubSet(From from, IndexInfo indexInfo, IndexKeys keys, QueryPlannerContext context) {
		IndexSubSet result = IndexSubSet.T.create();

		result.setTypeSignature(indexInfo.getEntitySignature());
		result.setPropertyName(indexInfo.getPropertyName());
		result.setIndex(context.sourceManager().indexForSource(from));
		result.setLookupIndex(context.getIndex(from, indexInfo));
		result.setKeys(ValueBuilder.indexKeyValue(keys));

		return result;
	}

	public static IndexRange indexRange(From from, IndexInfo indexInfo, Bound lowerBound, Bound upperBound, QueryPlannerContext context) {
		IndexRange result = IndexRange.T.create();

		result.setTypeSignature(indexInfo.getEntitySignature());
		result.setPropertyName(indexInfo.getPropertyName());
		result.setIndex(context.sourceManager().indexForSource(from));
		result.setMetricIndex(context.<RepositoryMetricIndex> getIndex(from, indexInfo));
		result.setRange(RangeBuilder.rangeForBounds(lowerBound, upperBound, context));

		return result;
	}

	public static IndexLookupJoin lookupJoin(TupleSet operand, Operand sourceOperand, From joinedFrom, IndexInfo indexInfo,
			QueryPlannerContext context) {

		IndexLookupJoin result = IndexLookupJoin.T.create();

		result.setIndex(context.sourceManager().indexForSource(joinedFrom));
		result.setLookupIndex(context.getIndex(joinedFrom, indexInfo));
		result.setOperand(operand);
		result.setLookupValue(context.convertOperand(sourceOperand));

		return result;
	}

	public static MergeLookupJoin mergeLookupJoin(TupleSet srcTupleSet, Operand srcOperand, TupleSet indexedTupleSet, Operand indexedOperand,
			QueryPlannerContext context) {

		MergeLookupJoin result = MergeLookupJoin.T.create();
		result.setOperand(srcTupleSet);
		result.setValue(context.convertOperand(srcOperand));
		result.setOtherOperand(indexedTupleSet);
		result.setOtherValue(context.convertOperand(indexedOperand));

		return result;
	}

	public static TupleSet mergeRangeJoin(TupleSet srcTupleSet, List<Bound> lowerBounds, List<Bound> upperBounds, TupleSet indexedTupleSet,
			Operand indexedOperand, QueryPlannerContext context) {

		GeneratedMetricIndex generatedIndex = GeneratedMetricIndex.T.create();
		generatedIndex.setIndexKey(context.convertOperand(indexedOperand));
		generatedIndex.setOperand(indexedTupleSet);

		MergeRangeJoin result = MergeRangeJoin.T.create();
		result.setOperand(srcTupleSet);
		result.setRange(RangeBuilder.rangeForBounds(lowerBounds, upperBounds, context));
		result.setIndex(generatedIndex);

		return result;
	}

	public static IndexRangeJoin rangeJoin(TupleSet operand, From joinedFrom, List<Bound> lowerBounds, List<Bound> upperBounds, IndexInfo indexInfo,
			QueryPlannerContext context) {

		IndexRangeJoin result = IndexRangeJoin.T.create();

		result.setIndex(context.sourceManager().indexForSource(joinedFrom));
		result.setMetricIndex(context.<MetricIndex> getIndex(joinedFrom, indexInfo));
		result.setOperand(operand);
		result.setRange(RangeBuilder.rangeForBounds(lowerBounds, upperBounds, context));

		return result;
	}

	public static EntityJoin entityJoin(TupleSet ts, Join join, QueryPlannerContext context) {
		return propertyJoin(EntityJoin.T.create(), ts, join, context);
	}

	public static ListJoin listJoin(TupleSet ts, Join join, QueryPlannerContext context) {
		JoinedListIndex listIndex = JoinedListIndex.T.create();
		listIndex.setIndex(context.sourceManager().indexForJoinKey(join));

		ListJoin result = propertyJoin(ListJoin.T.create(), ts, join, context);
		result.setListIndex(listIndex);

		return result;
	}

	public static MapJoin mapJoin(TupleSet ts, Join join, QueryPlannerContext context) {
		JoinedMapKey mapKey = JoinedMapKey.T.create();
		mapKey.setIndex(context.sourceManager().indexForJoinKey(join));

		MapJoin result = propertyJoin(MapJoin.T.create(), ts, join, context);
		result.setMapKey(mapKey);

		return result;
	}

	public static PaginatedSet paginatedSet(TupleSet operand, int limit, int offset, int tupleSize) {
		return paginatedSet(operand, limit, offset, tupleSize, false);
	}

	public static PaginatedSet paginatedSet(TupleSet operand, int limit, int offset, int tupleSize, boolean operandMayApplyPagination) {
		PaginatedSet result = PaginatedSet.T.create();
		result.setOperand(operand);
		result.setLimit(limit);
		result.setOffset(offset);
		result.setTupleSize(tupleSize);
		result.setOperandMayApplyPagination(operandMayApplyPagination);

		return result;
	}

	public static AggregatingProjection aggregatedProjection(TupleSet tupleSet, QueryPlannerContext context) {
		SelectQuery query = context.query();

		QueryAggregationManager aggManager = context.aggregationManager();

		List<Value> values = newList();

		for (Object operand : query.getSelections())
			projectOperandAndMarkItsPosition(context, operand, aggManager, values);

		ensureHavingAndOrderByOperandsSelected(context, values);

		AggregatingProjection result = newAggregatedProjection(query.getGroupBy(), context);
		result.setOperand(tupleSet);
		result.setValues(values);

		return result;
	}

	/**
	 * Because we haven't processed have ORDER BY and HAVING, we might need to select extra operands that are reference from these clauses, which we
	 * are not selecting in our query.
	 * <p>
	 * EXAMPLE: {@code select p.name, count(p.age) from Person p order by p.lastName}
	 * <p>
	 * Here {@code p.lastName} is an 'extraOperand' which we convert here, later when handling having/ordering, we will reference the value by it's
	 * index only.
	 * 
	 * @see QueryPlannerContext#noticePostAggregation()
	 */
	private static void ensureHavingAndOrderByOperandsSelected(QueryPlannerContext context, List<Value> values) {
		QueryAggregationManager aggManager = context.aggregationManager();

		for (Operand operand : aggManager.getExtraOperands())
			if (aggManager.findTupleComponentIndexOf(operand) == null)
				projectOperandAndMarkItsPosition(context, operand, aggManager, values);
	}

	private static void projectOperandAndMarkItsPosition( //
			QueryPlannerContext context, Object operand, QueryAggregationManager aggManager, List<Value> values) {

		Value convertOperand = context.convertOperand(operand);
		aggManager.noticeTupleComponentIndex(operand, values.size());
		values.add(convertOperand);
	}

	private static AggregatingProjection newAggregatedProjection(GroupBy groupBy, QueryPlannerContext context) {
		AggregatingProjection result = AggregatingProjection.T.create();

		if (groupBy != null)
			result.setGroupByValues(convertOperands(groupBy.getOperands(), context));

		return result;
	}

	public static Projection projection(TupleSet tupleSet, List<Object> selections, QueryPlannerContext context) {
		List<Value> values = convertOperands(selections, context);

		Projection result = Projection.T.create();
		result.setOperand(tupleSet);
		result.setValues(values);

		return result;
	}

	private static List<Value> convertOperands(List<Object> operands, QueryPlannerContext context) {
		return operands.stream() //
				.map(context::convertOperand) //
				.collect(Collectors.toList());
	}

	public static Projection projectFirstNComponents(TupleSet tupleSet, int n) {
		List<Value> values = IntStream.range(0, n) //
				.mapToObj(ValueBuilder::tupleComponent) //
				.collect(Collectors.toList());

		return projection(tupleSet, values);
	}

	private static Projection projection(TupleSet tupleSet, List<Value> values) {
		Projection result = Projection.T.create();
		result.setOperand(tupleSet);
		result.setValues(values);

		return result;
	}

	public static SetJoin setJoin(TupleSet ts, Join join, QueryPlannerContext context) {
		return propertyJoin(SetJoin.T.create(), ts, join, context);
	}

	private static <T extends PropertyJoin> T propertyJoin(T result, TupleSet ts, Join join, QueryPlannerContext context) {
		QuerySourceManager sourceManager = context.sourceManager();

		Value tupleComponent = ValueBuilder.tupleComponent(sourceManager.indexForSource(join.getSource()));

		result.setJoinKind(joinKindFor(join.getJoinType()));
		result.setOperand(ts);
		result.setValueProperty(ValueBuilder.valueProperty(tupleComponent, join.getProperty()));
		result.setIndex(sourceManager.indexForSource(join));

		return result;
	}

	private static JoinKind joinKindFor(JoinType joinType) {
		if (joinType == null) {
			return JoinKind.inner;
		}

		switch (joinType) {
			case full:
				return JoinKind.full;
			case inner:
				return JoinKind.inner;
			case left:
				return JoinKind.left;
			case right:
				return JoinKind.right;
		}

		throw new RuntimeQueryPlannerException("Unsupported JoinTyp: " + joinType);
	}

	public static Concatenation concatenation(TupleSet firstOperand, TupleSet secondOperand) {
		return combined(Concatenation.T.create(), firstOperand, secondOperand);
	}

	public static TupleSet concatenation(Collection<TupleSet> tupleSets, int tupleSize) {
		Iterator<TupleSet> it = tupleSets.iterator();

		return concatenation(it.next(), it, tupleSize);
	}

	private static TupleSet concatenation(TupleSet first, Iterator<TupleSet> it, int tupleSize) {
		if (!it.hasNext())
			return first;

		Concatenation result = Concatenation.T.create();
		result.setFirstOperand(first);
		result.setSecondOperand(concatenation(it.next(), it, tupleSize));
		result.setTupleSize(tupleSize);

		return result;
	}

	private static <T extends CombinedSet> T combined(T result, TupleSet firstOperand, TupleSet secondOperand) {
		result.setFirstOperand(firstOperand);
		result.setSecondOperand(secondOperand);

		return result;
	}

	public static StaticSet staticSourceSet(From from, Set<?> refsOrEntities, QueryPlannerContext context) {
		int index = context.sourceManager().indexForSource(from);
		return _staticSet(index, (Set<Object>) refsOrEntities);
	}

	public static StaticSet staticSet(int index, Object... values) {
		return _staticSet(index, asSet(values));
	}

	private static StaticSet _staticSet(int index, Set<Object> values) {
		StaticSet result = StaticSet.T.create();
		result.setIndex(index);
		result.setValues(values);

		return result;
	}

	public static QuerySourceSet querySourceSet(SourceSet tupleSet, List<com.braintribe.model.query.conditions.Condition> conditions) {
		SourceReplacingContext cc = new SourceReplacingContext();

		List<com.braintribe.model.query.conditions.Condition> operands = BaseType.INSTANCE.clone(cc, conditions, skip);

		QuerySourceSet result = QuerySourceSet.T.create();
		result.setEntityTypeSignature(tupleSet.getTypeSignature());
		result.setCondition(singleCondition(operands));
		result.setOrdering(null);

		return result;
	}

	private static com.braintribe.model.query.conditions.Condition singleCondition(List<com.braintribe.model.query.conditions.Condition> operands) {
		if (operands.size() == 1)
			return first(operands);

		Conjunction conjunction = Conjunction.T.create();
		conjunction.setOperands(operands);
		return conjunction;
	}

	public static TupleSet union(Collection<TupleSet> tupleSets) {
		Iterator<TupleSet> it = tupleSets.iterator();

		return union(it.next(), it);
	}

	private static TupleSet union(TupleSet first, Iterator<TupleSet> it) {
		if (!it.hasNext())
			return first;

		Union result = Union.T.create();
		result.setFirstOperand(first);
		result.setSecondOperand(union(it.next(), it));

		return result;
	}

	static class SourceReplacingContext extends StandardCloningContext {

		@Override
		public GenericEntity preProcessInstanceToBeCloned(GenericEntity instanceToBeCloned) {
			if (instanceToBeCloned instanceof PropertyOperand) {
				GenericEntity clone = clonePropertyOpernad((PropertyOperand) instanceToBeCloned);
				registerAsVisited(instanceToBeCloned, clone);

				return clone;

			} else {
				return super.preProcessInstanceToBeCloned(instanceToBeCloned);
			}
		}

		private PropertyOperand clonePropertyOpernad(PropertyOperand po) {
			return propertyOperand(toPropertyPath(po));
		}

		private String toPropertyPath(PropertyOperand po) {
			StringJoiner sj = new StringJoiner(".");
			writePropertyPath(po.getSource(), sj);

			String pn = po.getPropertyName();
			if (pn != null)
				sj.add(pn);

			return sj.toString();
		}

		private void writePropertyPath(Source source, StringJoiner sj) {
			if (source instanceof Join) {
				Join join = (Join) source;
				writePropertyPath(join.getSource(), sj);
				sj.add(join.getProperty());
			}
		}

		@Override
		public boolean isTraversionContextMatching() {
			Object peek = getObjectStack().peek();

			return peek instanceof From;
		}

	}

	private static PropertyOperand propertyOperand(String path) {
		PropertyOperand result = PropertyOperand.T.create();
		result.setPropertyName(path);

		return result;
	}

}
